package com.telusko.config;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.config.MeterFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    /** Placeholder used when the provider does not report which model answered. */
    private static final String RESPONSE_MODEL = "gen_ai.response.model";
    private static final String UNKNOWN = "none";

    /**
     * Gives every {@code gen_ai.*} meter the same set of tag keys.
     * <p>
     * Spring AI tags chat calls with {@code gen_ai.response.model} but image calls without it,
     * because the images API does not echo a model back. Prometheus refuses to register a second
     * meter of the same name with a different tag-key set, so the image metrics were being dropped
     * with "registration has failed: Prometheus requires that all meters with the same name have
     * the same set of tag keys" - and image generation was invisible on the dashboard.
     */
    @Bean
    public MeterFilter genAiResponseModelTagFilter() {
        return new MeterFilter() {
            @Override
            public Meter.Id map(Meter.Id id) {
                if (!id.getName().startsWith("gen_ai.")) {
                    return id;
                }
                if (id.getTag(RESPONSE_MODEL) != null) {
                    return id;
                }
                return id.withTag(Tag.of(RESPONSE_MODEL, UNKNOWN));
            }
        };
    }
}
