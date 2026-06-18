package com.example.realestate.data.model;

import com.google.gson.annotations.SerializedName;

public class District {
    @SerializedName("district_id")
    private int districtId;

    @SerializedName("city_id")
    private int cityId;

    @SerializedName("name")
    private String name;

    // Getters and Setters
    public int getDistrictId() { return districtId; }
    public void setDistrictId(int districtId) { this.districtId = districtId; }

    public int getCityId() { return cityId; }
    public void setCityId(int cityId) { this.cityId = cityId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    @Override
    public String toString() {
        return name; // Useful for ArrayAdapters
    }
}
