package com.example.realestate.data.model;

import com.google.gson.annotations.SerializedName;

public class Category {
    @SerializedName("type_id")
    private int typeId;

    @SerializedName("parent_id")
    private Integer parentId;

    @SerializedName("name")
    private String name;

    @SerializedName("is_active")
    private int isActive;

    // Getters and Setters
    public int getTypeId() { return typeId; }
    public void setTypeId(int typeId) { this.typeId = typeId; }

    public Integer getParentId() { return parentId; }
    public void setParentId(Integer parentId) { this.parentId = parentId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getIsActive() { return isActive; }
    public void setIsActive(int isActive) { this.isActive = isActive; }
}
