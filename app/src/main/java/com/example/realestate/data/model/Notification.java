package com.example.realestate.data.model;
import com.google.gson.annotations.SerializedName;

public class Notification {
    @SerializedName("notification_id")
    private int notificationId;

    @SerializedName("user_id")
    private int userId;

    @SerializedName("type")
    private String type;

    @SerializedName("title")
    private String title;

    @SerializedName("body")
    private String body;

    @SerializedName("ref_id")
    private Integer refId;

    @SerializedName("ref_type")
    private String refType;

    @SerializedName("is_read")
    private int isRead;

    @SerializedName("created_at")
    private String createdAt;

    // Getters and Setters
    public int getNotificationId() { return notificationId; }
    public void setNotificationId(int notificationId) { this.notificationId = notificationId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public Integer getRefId() { return refId; }
    public void setRefId(Integer refId) { this.refId = refId; }

    public String getRefType() { return refType; }
    public void setRefType(String refType) { this.refType = refType; }

    public boolean isRead() { return isRead == 1; }
    public void setRead(boolean read) { this.isRead = read ? 1 : 0; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
