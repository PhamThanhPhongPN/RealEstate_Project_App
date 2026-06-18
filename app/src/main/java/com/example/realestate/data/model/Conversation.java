package com.example.realestate.data.model;

import com.google.gson.annotations.SerializedName;

public class Conversation {
    @SerializedName("conversation_id")
    private int conversationId;

    @SerializedName("property_id")
    private Integer propertyId;

    @SerializedName("buyer_id")
    private int buyerId;

    @SerializedName("seller_id")
    private int sellerId;

    @SerializedName("last_message_at")
    private String lastMessageAt;

    @SerializedName("created_at")
    private String createdAt;

    // Joined fields from API
    @SerializedName("property_title")
    private String propertyTitle;

    @SerializedName("buyer_name")
    private String buyerName;

    @SerializedName("buyer_avatar")
    private String buyerAvatar;

    @SerializedName("seller_name")
    private String sellerName;

    @SerializedName("seller_avatar")
    private String sellerAvatar;

    @SerializedName("last_message")
    private String lastMessage;

    @SerializedName("unread_count")
    private int unreadCount;

    // Getters and Setters
    public int getConversationId() { return conversationId; }
    public void setConversationId(int conversationId) { this.conversationId = conversationId; }

    public Integer getPropertyId() { return propertyId; }
    public void setPropertyId(Integer propertyId) { this.propertyId = propertyId; }

    public int getBuyerId() { return buyerId; }
    public void setBuyerId(int buyerId) { this.buyerId = buyerId; }

    public int getSellerId() { return sellerId; }
    public void setSellerId(int sellerId) { this.sellerId = sellerId; }

    public String getLastMessageAt() { return lastMessageAt; }
    public void setLastMessageAt(String lastMessageAt) { this.lastMessageAt = lastMessageAt; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getPropertyTitle() { return propertyTitle; }
    public void setPropertyTitle(String propertyTitle) { this.propertyTitle = propertyTitle; }

    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }

    public String getBuyerAvatar() { return buyerAvatar; }
    public void setBuyerAvatar(String buyerAvatar) { this.buyerAvatar = buyerAvatar; }

    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }

    public String getSellerAvatar() { return sellerAvatar; }
    public void setSellerAvatar(String sellerAvatar) { this.sellerAvatar = sellerAvatar; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }
}
