package com.telusko.teluskoappclient.web;

import com.telusko.teluskoappclient.routing.ToolRouter;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/mcp/api")
public class MCPController
{
    private final ChatClient chatClient;
    private final ToolRouter toolRouter;

    // NOTE: we build a ChatClient WITHOUT default tool callbacks.
    // We do NOT call .defaultToolCallbacks(...) here on purpose, because we want
    // to decide the tools ourselves for every single request.
    public MCPController(ChatClient.Builder chatClientBuilder, ToolRouter toolRouter) {
        this.toolRouter = toolRouter;
        this.chatClient = chatClientBuilder
                .defaultSystem("""
                        You are a helpful AI assistant with access to tools.
                        Use a tool only when it is provided to you for the current request.
                        Always mention which tool provided the information.
                        """)
                .build();
    }

    @GetMapping("/chat")
    public Map<String, Object> chatSmart(@RequestParam String query) {
        ToolCallback[] tools = toolRouter.selectToolsFor(query);   // <-- routing happens here

        ChatResponse response = chatClient
                .prompt(query)
                .toolCallbacks(tools)          // only these tools go to the LLM
                .call()
                .chatResponse();

        return buildResult("SMART (routed)", query,
                toolRouter.serversFor(query), tools, response);
    }
    @GetMapping("/tools")
    public Map<String, Object> listTools() {

        return toolRouter.describeAllServers();
    }
    @GetMapping("/chat-all")
    public Map<String, Object> chatAll(@RequestParam String query) {

        // Collect every discovered tool.
        ToolCallback[] tools = toolRouter.allTools();

        ChatResponse response = chatClient
                .prompt(query)

                // ALL tools are attached.
                .toolCallbacks(tools)

                .call()
                .chatResponse();

        return buildResult(
                "NAIVE (All Tools)",
                query,
                Set.of("ALL"),
                tools,
                response
        );
    }
    private Map<String, Object> buildResult(String mode,
                                            String query,
                                            Set<String> routedServers,
                                            ToolCallback[] tools,
                                            ChatResponse response) {

        Map<String, Object> result = new LinkedHashMap<>();

        result.put("mode", mode);
        result.put("query", query);
        result.put("routedServers", routedServers);

        // Number of tools sent to the LLM.
        result.put("toolsSentToLLM", tools.length);

        // Tool names attached to this request.
        result.put("toolNames", toolRouter.toolNames(tools));

        // Token usage reported by the LLM.
        if (response.getMetadata().getUsage() != null) {

            result.put(
                    "promptTokens",
                    response.getMetadata()
                            .getUsage()
                            .getPromptTokens());

            result.put(
                    "totalTokens",
                    response.getMetadata()
                            .getUsage()
                            .getTotalTokens());
        }

        // Final LLM response.
        result.put(
                "answer",
                response.getResult()
                        .getOutput()
                        .getText());

        return result;
    }
}
