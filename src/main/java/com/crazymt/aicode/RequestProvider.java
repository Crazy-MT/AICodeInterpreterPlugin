package com.crazymt.aicode;

import com.crazymt.aicode.core.ConversationManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Wuzi
 */
public class RequestProvider {

    public static final String FREE_GPT35 = "https://chat.openai.com/backend-api/conversation";

    private String url;
    private String data;
    private Map<String, String> header;

    public String getUrl() {
        return url;
    }

    public String getData() {
        return data;
    }

    public Map<String, String> getHeader() {
        return header;
    }

    public RequestProvider createFree(Project project, String question, String oaiDeviceId, String token, String file) {
        RequestProvider provider = new RequestProvider();
        provider.url = FREE_GPT35;
        provider.header = getFreeGPT35TurboHeaders(oaiDeviceId, token);
        provider.data = buildGpt35TurboFree(project, question, file).toString();
        return provider;
    }

    public Map<String, String> getFreeGPT35TurboHeaders(String oaiDeviceId, String token) {
        Map<String,String> headers = new HashMap<>();
        headers.put("accept", "text/event-stream");
        headers.put("accept-language", "zh-CN,zh;q=0.9");
        headers.put("content-type", "application/json");
        headers.put("oai-device-id", oaiDeviceId);
        headers.put("oai-language", "en-US");
        headers.put("openai-sentinel-chat-requirements-token", token);
        headers.put("origin", "https://chat.openai.com");
        headers.put("referer", "https://chat.openai.com/");
        headers.put(
                "user-agent",
                "Mozilla/5.0 (iPhone; CPU iPhone OS 16_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.6 Mobile/15E148 Safari/604.1 Edg/123.0.0.0"
        );
        return headers;
    }

    public static JsonObject buildGpt35TurboFree(Project project, String text, String file) {
        JsonObject result = new JsonObject();
        result.addProperty("action", "next");
        result.addProperty("parent_message_id", ConversationManager.getInstance(project).getParentMessageId());
        String conversationId = ConversationManager.getInstance(project).getConversationId();
        if (StringUtil.isNotEmpty(conversationId)) {
            result.addProperty("conversation_id", conversationId);
        }

        JsonObject conversationMode = new JsonObject();
        conversationMode.addProperty("kind", "primary_assistant");
        result.add("conversation_mode", conversationMode);

        result.addProperty("timezone_offset_min",-480);
        result.addProperty("history_and_training_disabled",false);
        result.addProperty("force_paragen",false);
        result.addProperty("force_paragen_model_slug","");
        result.addProperty("force_nulligen",false);
        result.addProperty("force_rate_limit",false);
        result.addProperty("websocket_request_id", UUID.randomUUID().toString());
        result.addProperty("model","text-davinci-002-render-sha");
        JsonArray messages = new JsonArray();
        JsonObject message0 = new JsonObject();
        message0.addProperty("id",UUID.randomUUID().toString());

        JsonObject messageAuthor = new JsonObject();
        messageAuthor.addProperty("role", "user");
        message0.add("author", messageAuthor);

        JsonObject messageContent = new JsonObject();
        messageContent.addProperty("content_type", "text");

        JsonArray messageParts = new JsonArray();
        messageParts.add("You are a senior software engineer. This piece of code is a part of the " + file +" file. I need you to explain its purpose and provide a code example. Please keep it concise. Code: "+ text);
        messageContent.add("parts", messageParts);

        JsonObject messageMetadata = new JsonObject();
        message0.add("metadata", messageMetadata);


        message0.add("content", messageContent);
        messages.add(message0);
        result.add("messages",messages);

        return result;
    }
}
