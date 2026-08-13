package com.telusko.service;

import com.telusko.config.AiMetrics;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The retrieval half of RAG: find the indexed records that are closest in meaning to a question.
 * <p>
 * Every AI feature in the app - the customer assistant, product search, recommendations, the cart
 * helper - needs the same two things: only show a user what they are allowed to see, and drop
 * matches that are not actually relevant. Keeping that in one place means there is exactly one
 * file to edit when the rules change, and no feature can accidentally skip them.
 * <p>
 * Access control is expressed as a vector-store filter rather than a Java {@code .filter()} on the
 * results. That matters for correctness, not just speed: filtering afterwards means topK is spent
 * on documents the user may not see, so a user whose nearest neighbours all belong to someone else
 * would get an empty answer even when relevant public documents exist.
 */
@Service
@RequiredArgsConstructor
public class RagRetrievalService {

    private final VectorStore vectorStore;
    private final AiMetrics aiMetrics;

    /**
     * Cosine similarity below this is treated as "no match". Without a floor the store always
     * returns its topK nearest rows, so an unrelated question still pulled in documents and the
     * model was invited to answer from them.
     */
    private static final double MIN_SIMILARITY = 0.3;

    /**
     * Catalog browsing gets a lower floor than the assistant, because shoppers type two or three
     * words against product documents that are several lines long, and a short query is simply less
     * similar to a long text even when it is exactly right.
     * <p>
     * Measured against this catalog: "Electronics" scored 0.29 and "cheap gift under 2000" 0.28
     * against products they genuinely matched, so both were discarded by the 0.3 floor and search
     * returned nothing. Real non-matches sat far lower - "yoga mat" scored 0.17 against goggles -
     * so 0.2 separates the two cleanly. Precision is not lost by relaxing this: the model still has
     * to pick from the candidates, and it is much better at "does this answer the question" than a
     * distance cutoff is.
     */
    private static final double CATALOG_MIN_SIMILARITY = 0.2;

    /**
     * Retrieval for anything a specific person asks.
     * <p>
     * Non-admins see public catalog data plus their own private records. Admins are trusted with
     * everything, which is what makes the back-office assistant useful.
     */
    public List<Document> searchForUser(String query, String userEmail, boolean isAdmin, int topK) {
        if (topK <= 0) topK = 5;

        FilterExpressionBuilder b = new FilterExpressionBuilder();

        SearchRequest.Builder request = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(MIN_SIMILARITY);

        if (!isAdmin) {
            // "public, or mine" - the whole access rule in one expression.
            Filter.Expression onlyWhatTheyMaySee = b.or(
                    b.eq("visibility", "PUBLIC"),
                    b.eq("userEmail", userEmail == null ? "" : userEmail)
            ).build();
            request.filterExpression(onlyWhatTheyMaySee);
        }

        List<Document> matches = vectorStore.similaritySearch(request.build());
        return observed("assistant", matches);
    }

    /**
     * Catalog-only retrieval, used by product search and recommendations.
     * <p>
     * This deliberately cannot reach private records. Product search used to run an unfiltered
     * similarity search, so another customer's order, cart or address could be pulled into the
     * context of an ordinary product query.
     */
    public List<Document> searchCatalog(String query, int topK) {
        if (topK <= 0) topK = 12;

        FilterExpressionBuilder b = new FilterExpressionBuilder();

        // Discontinued products are excluded here rather than after the search. Soft-deleting a
        // product hides it from the catalog listing, but smart search kept offering it, so a
        // shopper could open a product page for something the store had withdrawn.
        Filter.Expression filter = b.and(
                b.and(b.eq("type", "PRODUCT"), b.eq("visibility", "PUBLIC")),
                b.eq("active", true)
        ).build();

        List<Document> matches = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(topK)
                        .similarityThreshold(CATALOG_MIN_SIMILARITY)
                        .filterExpression(filter)
                        .build());

        return observed("catalog-search", matches);
    }

    /**
     * Records how many documents a search produced, then returns them unchanged.
     * Every retrieval path funnels through here so no feature can silently skip the measurement.
     */
    private List<Document> observed(String feature, List<Document> matches) {
        List<Document> safe = matches == null ? List.of() : matches;
        aiMetrics.recordRetrieval(feature, safe.size());
        return safe;
    }

    /** Retrieval restricted to one kind of indexed record, e.g. PRODUCT or COUPON. */
    public List<Document> searchByType(String query, String type, int topK) {
        if (topK <= 0) topK = 5;

        FilterExpressionBuilder b = new FilterExpressionBuilder();
        Filter.Expression filter = b.and(
                b.eq("type", type),
                b.eq("visibility", "PUBLIC")
        ).build();

        List<Document> matches = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(topK)
                        .similarityThreshold(MIN_SIMILARITY)
                        .filterExpression(filter)
                        .build());

        return observed("catalog-search", matches);
    }

    /**
     * Catalog retrieval that skips a product the caller already knows about.
     * <p>
     * Recommending the item currently in the cart back to the customer is the classic way these
     * features look broken, so the exclusion happens here rather than in every caller.
     */
    public List<Document> searchCatalogExcluding(String query, List<Long> excludedProductIds, int topK) {
        if (topK <= 0) topK = 5;

        FilterExpressionBuilder b = new FilterExpressionBuilder();
        FilterExpressionBuilder.Op filter = b.and(
                b.eq("type", "PRODUCT"),
                b.eq("active", true)
        );

        if (excludedProductIds != null && !excludedProductIds.isEmpty()) {
            filter = b.and(filter, b.nin("productId", excludedProductIds.stream()
                    .map(id -> (Object) id)
                    .toList()));
        }

        List<Document> matches = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(topK)
                        .similarityThreshold(MIN_SIMILARITY)
                        .filterExpression(filter.build())
                        .build());

        return observed("recommendations", matches);
    }

    /**
     * Joins matches into one block of text ready to drop into a prompt.
     * Returns an empty string when nothing matched, which callers use to decide whether to fall
     * back to general knowledge instead of pretending they have grounding.
     */
    public String asContext(List<Document> matches) {
        if (matches == null || matches.isEmpty()) {
            return "";
        }
        return matches.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n----\n"));
    }
}
