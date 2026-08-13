package com.telusko.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * Metrics for the AI features, recorded per feature rather than per model call.
 * <p>
 * Spring AI already publishes {@code gen_ai.client.operation} and {@code gen_ai.client.token.usage},
 * which answer "is OpenAI slow?" and "what are we spending?". They cannot answer the questions an
 * on-call engineer actually asks: <em>which</em> feature broke, and is retrieval still finding
 * anything. Those need the application's own names attached, which is what this class adds.
 */
@Component
public class AiMetrics {

    /** Timer: how long a feature took, split by success or failure. */
    private static final String FEATURE_TIMER = "ecommerce.ai.feature";

    /** How many documents retrieval returned for a request. */
    private static final String RETRIEVAL_DOCS = "ecommerce.ai.retrieval.documents";

    /** Requests where retrieval found nothing at all. */
    private static final String RETRIEVAL_EMPTY = "ecommerce.ai.retrieval.empty";

    /** Times the app fell back because an AI call failed. */
    private static final String DEGRADED = "ecommerce.ai.degraded";

    private final MeterRegistry registry;

    public AiMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    /**
     * Times an AI feature and tags the result.
     * <p>
     * The exception is re-thrown untouched - this only observes. Failures are counted as
     * {@code outcome=failure} so a rising error rate is visible on the dashboard even when the
     * caller swallows the error to keep the user's request working.
     */
    public <T> T record(String feature, Supplier<T> call) {
        Timer.Sample sample = Timer.start(registry);
        String outcome = "success";
        try {
            return call.get();
        } catch (RuntimeException ex) {
            outcome = "failure";
            throw ex;
        } finally {
            sample.stop(Timer.builder(FEATURE_TIMER)
                    .description("Latency of an end-to-end AI feature, by outcome")
                    .tag("feature", feature)
                    .tag("outcome", outcome)
                    .register(registry));
        }
    }

    /**
     * Records how much grounding a request actually got.
     * <p>
     * This is the single most useful RAG signal. A retrieval that quietly returns nothing does not
     * error - the assistant just answers from general knowledge and starts making things up. A
     * rising empty-retrieval rate is how you find out that indexing broke or the similarity
     * threshold is too strict, long before a customer complains.
     */
    public void recordRetrieval(String feature, int documentsFound) {
        DistributionSummary.builder(RETRIEVAL_DOCS)
                .description("Number of documents returned by vector search")
                .tag("feature", feature)
                .register(registry)
                .record(documentsFound);

        if (documentsFound == 0) {
            Counter.builder(RETRIEVAL_EMPTY)
                    .description("Retrievals that returned no documents")
                    .tag("feature", feature)
                    .register(registry)
                    .increment();
        }
    }

    /**
     * Counts a fallback: the AI call failed and the app served a degraded but working response.
     * These are invisible in HTTP metrics because the request still returns 200.
     */
    public void recordDegraded(String feature, String reason) {
        Counter.builder(DEGRADED)
                .description("AI calls that failed and fell back to a non-AI response")
                .tag("feature", feature)
                .tag("reason", reason)
                .register(registry)
                .increment();
    }
}
