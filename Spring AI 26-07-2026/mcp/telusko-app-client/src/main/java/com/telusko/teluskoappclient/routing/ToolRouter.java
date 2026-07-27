package com.telusko.teluskoappclient.routing;

import io.modelcontextprotocol.client.McpSyncClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ToolRouter
{
    private  List<McpSyncClient> mcpClients;

    private record ToolDef(String server,
                           String toolName,
                           List<String> keywords)
    {
        boolean matches(String lowerQuery)
        {
            return keywords.stream().anyMatch(lowerQuery::contains);
        }
    }
    private static final List<ToolDef> TOOL_TABLE = List.of(
            // ---- math-server (7 tools) ----
            new ToolDef("math-server", "add",        List.of("add", "sum", "plus", "addition")),
            new ToolDef("math-server", "subtract",   List.of("subtract", "minus", "difference")),
            new ToolDef("math-server", "multiply",   List.of("multiple", "times", "product")),
            new ToolDef("math-server", "power",      List.of("power", "exponent", "raised", "squared", "cubed")),
            new ToolDef("math-server", "modulus",    List.of("modulus", "modulo", "remainder")),

            // ---- news-server (3 tools) ----
            new ToolDef("news-server", "getNews",           List.of("news", "article")),
            new ToolDef("news-server", "getTopHeadlines",   List.of("headline", "breaking", "top news")),
            new ToolDef("news-server", "getNewsByCategory", List.of("category", "business news", "sports news", "tech news")),

            // ---- datetime-server (3 tools) ----
            new ToolDef("datetime-server", "getCurrentDateAndTime",         List.of("current time", "current date", "time now", "what time", "today", "right now")),
            new ToolDef("datetime-server", "getCurrentDateAndTimeTimeZoned", List.of("timezone", "time zone", "/", "utc", "gmt")),
            new ToolDef("datetime-server", "getDayOfWeek",                  List.of("day of week", "day of the week", "which day", "what day"))
    );
    public ToolRouter(List<McpSyncClient> mcpClients)
    {
        this.mcpClients=mcpClients;
    }
    private Set<String> matchedToolNames(String query) {

        String q = query.toLowerCase();

        Set<String> names = new LinkedHashSet<>();

        for (ToolDef def : TOOL_TABLE) {
            if (def.matches(q)) {
                names.add(def.toolName());
            }
        }
        return names;
    }

    public ToolCallback[] selectToolsFor(String query) {

        Set<String> wanted = matchedToolNames(query);

        if (wanted.isEmpty()) {

            System.out.println(
                    "[ToolRouter] No tool matched -> Sending 0 tools");

            return new ToolCallback[0];
        }

        ToolCallback[] selected = Arrays.stream(allTools())
                .filter(tc ->
                        wanted.contains(tc.getToolDefinition().name()))
                .toArray(ToolCallback[]::new);

        System.out.println(
                "[ToolRouter] Query matched "
                        + wanted
                        + " -> Sending "
                        + selected.length
                        + " tool(s)");

        return selected;
    }


    public Set<String> serversFor(String query)
    {

        Set<String> wanted = matchedToolNames(query);

        Set<String> servers = new LinkedHashSet<>();

        for (ToolDef def : TOOL_TABLE) {

            if (wanted.contains(def.toolName())) {
                servers.add(def.server());
            }
        }

        return servers;
    }

    public ToolCallback[] allTools() {

        return new SyncMcpToolCallbackProvider(mcpClients)
                .getToolCallbacks();
    }

    //display all tools names
    public List<String> toolNames(ToolCallback[] tools) {

        return Arrays.stream(tools)
                .map(t -> t.getToolDefinition().name())
                .toList();
    }

    // debuger helper to see if everything working as per our need
    public Map<String, Object> describeAllServers() {

        Map<String, Object> out = new LinkedHashMap<>();

        int total = 0;

        for (McpSyncClient client : mcpClients) {

            String serverName = client.getServerInfo().name();

            ToolCallback[] tools =
                    new SyncMcpToolCallbackProvider(List.of(client))
                            .getToolCallbacks();

            Map<String, Object> info = new LinkedHashMap<>();

            info.put("toolCount", tools.length);
            info.put("tools", toolNames(tools));

            out.put(serverName, info);

            total += tools.length;
        }

        out.put("totalToolsAcrossAllServers", total);

        return out;
    }

}
