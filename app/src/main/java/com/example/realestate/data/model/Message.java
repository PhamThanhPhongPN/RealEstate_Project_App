package com.example.realestate.data.model;

import com.google.gson.annotations.SerializedName;

public class Message {
    @SerializedName("message_id")
    private int messageId;

    @SerializedName("conversation_id")
    private int conversationId;

    @SerializedName("sender_id")
    private int senderId;

    @SerializedName("body")
    private String body;

    @SerializedName("type")
    private String type = "text"; // "text", "image"

    @SerializedName("is_read")
    private int isRead = 0;

    @SerializedName("sent_at")
    private String sentAt;

    @SerializedName("sender_name")
    private String senderName;

    @SerializedName("sender_avatar")
    private String senderAvatar;

    // Constructors
    public Message() {}

    public Message(int conversationId, int senderId, String body) {
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.body = body;
    }

    // Getters and Setters
    public int getMessageId() { return messageId; }
    public void setMessageId(int messageId) { this.messageId = messageId; }

    public int getConversationId() { return conversationId; }
    public void setConversationId(int conversationId) { this.conversationId = conversationId; }

    public int getSenderId() { return senderId; }
    public void setSenderId(int senderId) { this.senderId = senderId; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getIsRead() { return isRead; }
    public void setIsRead(int isRead) { this.isRead = isRead; }

    public String getSentAt() { return sentAt; }
    public void setSentAt(String sentAt) { this.sentAt = sentAt; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getSenderAvatar() { return senderAvatar; }
    public void setSenderAvatar(String senderAvatar) { this.senderAvatar = senderAvatar; }
}
