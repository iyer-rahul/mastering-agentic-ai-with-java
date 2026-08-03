package com.telusko.aigeminiapp.web;

import com.telusko.aigeminiapp.tools.CourseTools;
import com.telusko.aigeminiapp.tools.DateTimeTools;
import com.telusko.aigeminiapp.web.dto.ChatReply;
import com.telusko.aigeminiapp.web.dto.ChatRequest;
import com.telusko.aigeminiapp.web.dto.StudyPlan;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class ChatController
{
    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final CourseTools courseTools;
    private final DateTimeTools dateTimeTools;

    public ChatController(ChatClient chatClient,
                          ChatMemory chatMemory,
                          CourseTools courseTools,
                          DateTimeTools dateTimeTools) {
        this.chatClient = chatClient;
        this.chatMemory = chatMemory;
        this.courseTools = courseTools;
        this.dateTimeTools = dateTimeTools;
    }

    /**
     * Normal chat. Tools fire automatically when the model asks for them, and the
     * conversation id keeps the history alive across turns.
     */
    @PostMapping("/chat")
    public ChatReply chat(@RequestBody ChatRequest request) {
        String conversationId = request.conversationIdOrNew();

        ChatResponse response = chatClient.prompt()
                .user(request.message())
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .chatResponse();

        return ChatReply.from(conversationId, response);
    }

    /**
     * Same thing, but tokens arrive as they are generated. Open it in a browser and
     * watch the answer type itself out.
     */
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@RequestParam String message,
                               @RequestParam(defaultValue = "stream-demo") String conversationId) {

        return chatClient.prompt()
                .user(message)
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                .stream()
                .content();
    }

    /**
     * No prose, just JSON that fits the {@link StudyPlan} record. Runs in a throwaway
     * conversation so old chit-chat cannot leak into the plan.
     */
    @PostMapping("/study-plan")
    public StudyPlan studyPlan(@RequestBody ChatRequest request) {
        return chatClient.prompt()
                .user("""
                        Build a week-by-week study plan for this learner: %s
                        First look at the real catalog with your tools, then pick exactly one
                        course id from it and plan around that course's duration.
                        """.formatted(request.message()))
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID,
                        "plan-" + UUID.randomUUID()))
                .call()
                .entity(StudyPlan.class);
    }

    /**
     * Exactly what the model sees. Handy in a demo: this JSON is what gets shipped to
     * Gemini as the OpenAI {@code tools} array on every single request.
     */
    @GetMapping("/tools")
    public List<Map<String, String>> tools() {
        return Arrays.stream(ToolCallbacks.from(courseTools, dateTimeTools))
                .map(ToolCallback::getToolDefinition)
                .map(definition -> Map.of(
                        "name", definition.name(),
                        "description", definition.description(),
                        "inputSchema", definition.inputSchema()))
                .toList();
    }

    /** Forget one conversation. */
    @DeleteMapping("/chat/{conversationId}")
    public Map<String, String> forget(@PathVariable String conversationId) {
        chatMemory.clear(conversationId);
        return Map.of("status", "cleared", "conversationId", conversationId);
    }

}
