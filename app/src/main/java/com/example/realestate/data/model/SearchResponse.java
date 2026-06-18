package com.example.realestate.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class SearchResponse {
    @SerializedName("properties")
    private List<Property> properties = new ArrayList<>();

    @SerializedName("total")
    private int total = 0;

    @SerializedName("pages")
    private int pages = 0;

    @SerializedName("page")
    private int page = 1;

    // Getters and Setters
    public List<Property> getProperties() { return properties; }
    public void setProperties(List<Property> properties) { this.properties = properties; }

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public int getPages() { return pages; }
    public void setPages(int pages) { this.pages = pages; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
}
