package com.telusko.aigeminiapp.rag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class KnowledgeBase
{
    private static final Logger log = LoggerFactory.getLogger(KnowledgeBase.class);
    private static final String KNOWLEDGE_PATTERN = "classpath:/knowledge/*.md";

    private final VectorStore vectorStore;
    private final JdbcTemplate jdbcTemplate;
    private final String tableName;

    public KnowledgeBase(VectorStore vectorStore,
                         JdbcTemplate jdbcTemplate,
                         @Value("${spring.ai.vectorstore.pgvector.table-name}") String tableName) {
        this.vectorStore = vectorStore;
        this.jdbcTemplate = jdbcTemplate;
        this.tableName = tableName;
    }

    /**
     * Now that the vectors live in a real database, they survive a restart. Ingesting
     * again would just insert every chunk a second time, so check first.
     */
    public void loadIfEmpty() throws IOException {
        Integer existing = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM " + tableName, Integer.class);

        if (existing != null && existing > 0) {
            log.info("Knowledge base already holds {} chunks, skipping ingestion", existing);
            return;
        }
        load();
    }

    /** @return how many chunks were stored, so the startup log can prove it worked */
    public int load() throws IOException {
        Resource[] files = new PathMatchingResourcePatternResolver()
                .getResources(KNOWLEDGE_PATTERN);

        if (files.length == 0) {
            log.warn("No knowledge files matched {} - RAG answers will be empty", KNOWLEDGE_PATTERN);
            return 0;
        }

        // Chunking matters more than people expect. Too big and the model drowns in
        // irrelevant text; too small and a chunk loses the context that made it meaningful.
        TokenTextSplitter splitter = TokenTextSplitter.builder()
                .withChunkSize(250)
                .withMinChunkSizeChars(150)
                .build();

        List<Document> chunks = new ArrayList<>();

        for (Resource file : files) {
            TextReader reader = new TextReader(file);
            // Metadata rides along with every chunk, so answers can cite their source.
            reader.getCustomMetadata().put("source", file.getFilename());

            chunks.addAll(splitter.apply(reader.get()));
        }

        // Embeddings are generated here, then written to Postgres in one shot.
        vectorStore.add(chunks);

        log.info("Knowledge base ready: {} chunks from {} file(s)", chunks.size(), files.length);
        return chunks.size();
    }
}
