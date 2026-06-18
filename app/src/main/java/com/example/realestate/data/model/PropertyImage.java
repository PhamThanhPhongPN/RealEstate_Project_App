package com.example.realestate.data.model;

import com.google.gson.annotations.SerializedName;

public class PropertyImage {
    @SerializedName("image_id")
    private int imageId;

    @SerializedName("property_id")
    private int propertyId;

    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("sort_order")
    private int sortOrder;

    // Getters and Setters
    public int getImageId() { return imageId; }
    public void setImageId(int imageId) { this.imageId = imageId; }

    public int getPropertyId() { return propertyId; }
    public void setPropertyId(int propertyId) { this.propertyId = propertyId; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
}
