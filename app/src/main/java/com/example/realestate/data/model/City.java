package com.example.realestate.data.model;

import com.google.gson.annotations.SerializedName;

public class City {
    @SerializedName("city_id")
    private int cityId;

    @SerializedName("name")
    private String name;

    @SerializedName("country")
    private String country;

    // Getters and Setters
    public int getCityId() { return cityId; }
    public void setCityId(int cityId) { this.cityId = cityId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    
    @Override
    public String toString() {
        return name; // Useful for ArrayAdapters
    }
}
