package com.crazymt.aicode.parser;

import com.crazymt.aicodeinterpreter.bean.OpenAIStreamBean;
import com.crazymt.aicode.core.ConversationManager;
import com.crazymt.aicode.utils.HtmlUtil;
import com.google.gson.Gson;
import com.intellij.openapi.project.Project;

public class OfficialParser {
    public static ParseResult parseFreeGPT35WithStream(Project project, String response) {
        OpenAIStreamBean resultData = new Gson().fromJson(response, OpenAIStreamBean.class);

        String conversationId = resultData.getConversation_id();
        String parentId = resultData.getMessage().getMetadata().getParent_id();
        ConversationManager.getInstance(project).setParentMessageId(parentId);
        ConversationManager.getInstance(project).setConversationId(conversationId);

        String answer = resultData.getMessage().getContent().getParts().get(0);

        ParseResult parseResult = new ParseResult();
        parseResult.source = answer;
        parseResult.html = HtmlUtil.md2html(answer);
        return parseResult;
    }

    public static class ParseResult {
        private String source;
        private String html;

        public String getSource() {
            return source;
        }

        public String getHtml() {
            return html;
        }
    }

}
