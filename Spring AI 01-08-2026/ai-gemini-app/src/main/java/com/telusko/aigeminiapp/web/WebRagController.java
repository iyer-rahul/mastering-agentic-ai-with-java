package com.telusko.aigeminiapp.web;

import com.telusko.aigeminiapp.web.dto.ChatReply;
import com.telusko.aigeminiapp.web.dto.ChatRequest;
import com.telusko.aigeminiapp.web.dto.Snippet;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class WebRagController
{

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public WebRagController(ChatClient chatClient, VectorStore vectorStore) {
        this.chatClient = chatClient;
        this.vectorStore = vectorStore;
    }

    /**
     * Ask anything about our policies, batches or certificates.
     * <p>
     * The advisor does three things before the model ever sees the question: embeds it,
     * searches the vector store, and pastes the winning chunks into the prompt as context.
     */
    @PostMapping("/ask")
    public ChatReply ask(@RequestBody ChatRequest request) {
        String conversationId = request.conversationIdOrNew();

        ChatResponse response = chatClient.prompt()
                .user(request.message())
                .advisors(QuestionAnswerAdvisor.builder(vectorStore)
                        .searchRequest(SearchRequest.builder()
                                .topK(4)                    // 4 chunks is usually plenty
                                .similarityThreshold(0.5)   // below this, treat it as noise
                                .build())
                        .build())
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .chatResponse();

        return ChatReply.from(conversationId, response);
    }

    /**
     * Retrieval only, no model. This is the endpoint you actually debug with: if the
     * chunks here are wrong, no amount of prompt tweaking will fix the answer.
     */
    @GetMapping("/knowledge/search")
    public List<Snippet> search(@RequestParam String q,
                                @RequestParam(defaultValue = "4") int topK) {

        List<Document> hits = vectorStore.similaritySearch(SearchRequest.builder()
                .query(q)
                .topK(topK)
                .similarityThresholdAll()
                .build());

        return hits == null ? List.of() : hits.stream().map(Snippet::from).toList();
    }
}
