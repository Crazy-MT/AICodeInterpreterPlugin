package com.crazymt.aicode.core;

import com.intellij.openapi.project.Project;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ConversationManager {

    public static ConversationManager getInstance(@NotNull Project project) {
        return project.getService(ConversationManager.class);
    }

    private String newDeviceId = UUID.randomUUID().toString();

    private String parentMessageId = UUID.randomUUID().toString();
    private String conversationId = "";

    public String getParentMessageId() {
        return parentMessageId;
    }

    public void setParentMessageId(String parentMessageId) {
        this.parentMessageId = parentMessageId;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getNewDeviceId() {
        return newDeviceId;
    }

    public void setNewDeviceId() {
        this.newDeviceId = UUID.randomUUID().toString();
    }
}
