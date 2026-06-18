package com.example.realestate.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class SearchMetadata {
    @SerializedName("categories")
    private List<Category> categories = new ArrayList<>();

    @SerializedName("cities")
    private List<City> cities = new ArrayList<>();

    @SerializedName("districts")
    private List<District> districts = new ArrayList<>();

    @SerializedName("features")
    private List<Feature> features = new ArrayList<>();

    // Getters and Setters
    public List<Category> getCategories() { return categories; }
    public void setCategories(List<Category> categories) { this.categories = categories; }

    public List<City> getCities() { return cities; }
    public void setCities(List<City> cities) { this.cities = cities; }

    public List<District> getDistricts() { return districts; }
    public void setDistricts(List<District> districts) { this.districts = districts; }

    public List<Feature> getFeatures() { return features; }
    public void setFeatures(List<Feature> features) { this.features = features; }
}
