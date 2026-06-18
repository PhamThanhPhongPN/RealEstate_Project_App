package com.example.realestate.data.model;

import com.google.gson.annotations.SerializedName;

public class Feature {
    @SerializedName("feature_id")
    private int featureId;

    @SerializedName("name")
    private String name;

    @SerializedName("icon_name")
    private String iconName;

    // Getters and Setters
    public int getFeatureId() { return featureId; }
    public void setFeatureId(int featureId) { this.featureId = featureId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIconName() { return iconName; }
    public void setIconName(String iconName) { this.iconName = iconName; }
}
