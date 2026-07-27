package com.telusko.mcpapp1.web;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mcp/api")
public class MCPController
{
    private ChatClient chatClient;

//    public MCPController(ChatClient.Builder chatClientBuilder,
//                         ToolCallbackProvider toolCallbackProvider)
//    {
//        this.chatClient= chatClientBuilder.defaultToolCallbacks(toolCallbackProvider)
//                .build();
//    }
public MCPController(ChatClient.Builder chatClientBuilder,
                     ToolCallbackProvider toolCallbackProvider)
{
    this.chatClient= chatClientBuilder
            .defaultSystem(
                    """
                        You are a helpful AI assistant with access to multiple tools.
                        Use the appropriate tool when the user asks about:
                        - Current date and time (any timezone)
                        - News headlines on any topic
                        - Mathematical calculations
                        - Files in the user's demo folder
                        
                        If a tool is available for the task, use it.
                        Always mention which tool provided the information.
                        """
            )
            .defaultToolCallbacks(toolCallbackProvider)
            .build();
}
    //Spring AI automatically create MCP Client during application startup
//    Reads mcp-servers.json
//    Starts the local Filesystem MCP Server (npx.cmd ...)
//    Creates an MCP Client
//    Connects to the server
//    Performs initialization
//    Discovers all available tools
//    Stores them in ToolCallbackProvider
//    So your application (the Host) does not create the client manually.

    @GetMapping("/chat")
    public String getAnswerMcp(@RequestParam String query)
    {
        return chatClient
                .prompt(query)
                .call()
                .content();
    }

    @GetMapping("/chat-custom")
    public String getAnswer(@RequestParam String question) {
        String response =
                chatClient
                        .prompt(question)
                        .call()
                        .content();
        System.out.println("[LLM Response] " + response);
        return response;
    }

}
