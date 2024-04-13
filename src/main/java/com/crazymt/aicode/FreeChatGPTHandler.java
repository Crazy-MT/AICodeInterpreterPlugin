package com.crazymt.aicode;

import com.crazymt.aicodeinterpreter.bean.ModelResult;
import com.crazymt.aicodeinterpreter.net.NetCallback;
import com.crazymt.aicodeinterpreter.bean.OpenAIToken;
import com.crazymt.aicode.core.ConversationManager;
import com.crazymt.aicodeinterpreter.net.HttpUtilsKt;
import com.crazymt.aicode.parser.OfficialParser;
import com.google.gson.Gson;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.http2.StreamResetException;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;

/**
 * @author wuzi
 */
public class FreeChatGPTHandler extends AbstractHandler {
    private String baseUrl = "https://chat.openai.com";

    private Project myProject;

    private static final Logger LOG = LoggerFactory.getLogger(FreeChatGPTHandler.class);

    private final Stack<String> gpt35Stack = new Stack<>();

    private static final int CONNECTION_TIMEOUT = 50000;
    private static final int READ_TIMEOUT = 50000;

    public void handle(Project project,String question, String fileName, NetCallback<ModelResult> callback) {
        Call call = null;

        myProject = project;

        try {
            String newDeviceId = ConversationManager.getInstance(myProject).getNewDeviceId();
            Map<String, String> headers = new HashMap<>();
//            headers.put("oai-device-id", newDeviceId);
            headers.put("accept", "*/*");
            headers.put("accept-language", "zh-CN,zh;q=0.9");
            headers.put("content-type", "application/json");
            headers.put("oai-device-id", newDeviceId);
            headers.put("oai-language", "en-US");
            headers.put("origin", baseUrl);
            headers.put("referer", baseUrl);
            headers.put("user-agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 16_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.6 Mobile/15E148 Safari/604.1 Edg/123.0.0.0");
            Request request = new Request.Builder().url("https://chat.openai.com/backend-anon/sentinel/chat-requirements").headers(Headers.of(headers)).post(RequestBody.create("", MediaType.parse("application/json"))).build();
            OkHttpClient.Builder builder = new OkHttpClient.Builder().connectTimeout(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS).readTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS);
            builder.hostnameVerifier(getHostNameVerifier());

            builder.sslSocketFactory(getSslContext().getSocketFactory(), (X509TrustManager) getTrustAllManager());

            OkHttpClient httpClient = builder.build();
            call = httpClient.newCall(request);
        /*call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.out.println("MTMTMT IOException:" + e.getMessage());

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                System.out.println("MTMTMT response:" + response.body().string());
                String content = response.body().string();
                OpenAIToken result = new Gson().fromJson(content, OpenAIToken.class);
                String token = result.getToken();
                String oaiDeviceId = newDeviceId;
            }
        });*/


            String content = call.execute().body().string();
            OpenAIToken result = new Gson().fromJson(content, OpenAIToken.class);
            String token = result.getToken();
            System.out.println("MTMTMT response:token" + token);
            RequestProvider provider = new RequestProvider().createFree(project, question, newDeviceId, token, fileName);
            Request requestChat = new Request.Builder()
                    .url(provider.getUrl())
                    .headers(Headers.of(provider.getHeader()))
                    .post(RequestBody.create(MediaType.parse("application/json"),
                            provider.getData()))
                    .build();
//            OpenAISettingsState instance = OpenAISettingsState.getInstance();
            OkHttpClient.Builder builderChat = new OkHttpClient.Builder()
                    .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
                    .readTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS);
            builderChat.hostnameVerifier(getHostNameVerifier());
            builderChat.sslSocketFactory(getSslContext().getSocketFactory(), (X509TrustManager) getTrustAllManager());
            OkHttpClient httpClientChat = builderChat.build();
            EventSourceListener listener = new EventSourceListener() {

                boolean handler = false;

                @Override
                public void onOpen(@NotNull EventSource eventSource, @NotNull Response response) {
                    LOG.info("ChatGPT: conversation open. Url = {}",eventSource.request().url());
                }

                @Override
                public void onClosed(@NotNull EventSource eventSource) {
                    LOG.info("ChatGPT: conversation close. Url = {}",eventSource.request().url());
                    if (!handler) {
                        if (callback != null) {
                            callback.onFail("Connection to remote server failed. There are usually several reasons for this:<br />1. Request too frequently, please try again later.<br />2. It may be necessary to set up a proxy to request.");
                        }
//                        component.setContent("Connection to remote server failed. There are usually several reasons for this:<br />1. Request too frequently, please try again later.<br />2. It may be necessary to set up a proxy to request.");
                    }
                }

                @Override
                public void onEvent(@NotNull EventSource eventSource, @Nullable String id, @Nullable String type, @NotNull String data) {
                    handler = true;
                    if (StringUtil.isEmpty(data)) {
                        return;
                    }
                    if (data.contains("[DONE]")) {
                        return;
                    }
                    try {
                        OfficialParser.ParseResult parseResult = OfficialParser.
                                parseFreeGPT35WithStream(project, data);
                        if (data.contains("\"is_complete\": true")) {
//                                mainPanel.getContentPanel().getMessages().add(OfficialBuilder.assistantMessage(gpt35Stack.pop()));
                            gpt35Stack.clear();

                            if (callback != null) {
                                callback.onSuccess(new ModelResult(HttpUtilsKt.getSourceFreeGPT(), "en", "zh", question, parseResult.getHtml(), ""));
                            }
                        } else {
                            gpt35Stack.push(parseResult.getSource());
                        }
                        // Copy action only needed source content
//                        component.setSourceContent(parseResult.getSource());
//                        component.setContent(parseResult.getHtml());
                        /*if (callback != null) {
                            callback.onSuccess(new ModelResult(HttpUtilsKt.getSourceFreeGPT(), "en", "zh", question, parseResult.getHtml(), ""));
                        }*/
                    } catch (Exception e) {
                        LOG.error("ChatGPT: Parse response error, e={}, message={}", e, e.getMessage());
//                        component.setContent(e.getMessage());
                        if (callback != null) {
                            callback.onFail(e.getMessage());
                        }
                    } finally {
//                        mainPanel.getExecutorService().shutdown();
                    }
                }

                @Override
                public void onFailure(@NotNull EventSource eventSource, @Nullable Throwable t, @Nullable Response response) {
                    if (t != null) {
                        if (t instanceof StreamResetException) {
                            LOG.info("ChatGPT: Request failure, throwable StreamResetException, cause: {}", t.getMessage());
//                            component.setContent("Request failure, cause: " + t.getMessage());
//                            mainPanel.aroundRequest(false);
                            if (callback != null) {
                                callback.onFail(t.getMessage());
                            }
                            t.printStackTrace();
                            return;
                        }
                        LOG.info("ChatGPT: conversation failure. Url={}, response={}, errorMessage={}",eventSource.request().url(), response, t.getMessage());
//                        component.setContent("Response failure, cause: " + t.getMessage() + ", please try again. <br><br> Tips: if proxy is enabled, please check if the proxy server is working.");
//                        mainPanel.aroundRequest(false);
                        if (callback != null) {
                            callback.onFail(t.getMessage());
                        }
                        t.printStackTrace();
                    } else {
                        String responseString = "";
                        if (response != null) {
                            try {
                                responseString = response.body().string();
                            } catch (IOException e) {
//                                mainPanel.aroundRequest(false);
                                LOG.error("ChatGPT: parse response error, cause: {}", e.getMessage());
//                                component.setContent("Response failure, cause: " + e.getMessage());
                                if (callback != null) {
                                    callback.onFail(e.getMessage());
                                }
                                throw new RuntimeException(e);
                            }
                        }
                        LOG.info("ChatGPT: conversation failure. Url={}, response={}",eventSource.request().url(), response);
//                        component.setContent("Response failure, please try again. Error message: " + responseString);
                        if (callback != null) {
                            callback.onFail(responseString);
                        }
                    }
                }
            };
            EventSource.Factory factory = EventSources.createFactory(httpClientChat);
            factory.newEventSource(requestChat, listener);
//            return factory.newEventSource(requestChat, listener);

        } catch (Exception e) {
//            component.setSourceContent(e.getMessage());
//            component.setContent(e.getMessage());
//            mainPanel.aroundRequest(false);
            if (callback != null) {
                callback.onFail(e.getMessage());
            }
        } finally {
//            mainPanel.getExecutorService().shutdown();
        }
//        return null;
    }
}
