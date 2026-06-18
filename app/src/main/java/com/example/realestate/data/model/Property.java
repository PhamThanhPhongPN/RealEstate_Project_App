package com.example.realestate.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class Property {
    @SerializedName("property_id")
    private int propertyId;

    @SerializedName("owner_id")
    private int ownerId;

    @SerializedName("type_id")
    private int typeId;

    @SerializedName("district_id")
    private Integer districtId;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("listing_type")
    private String listingType; // "sale" or "rent"

    @SerializedName("address")
    private String address;

    @SerializedName("latitude")
    private Double latitude;

    @SerializedName("longitude")
    private Double longitude;

    @SerializedName("price_usd")
    private double priceUsd;

    @SerializedName("area_m2")
    private Double areaM2;

    @SerializedName("bedrooms")
    private Integer bedrooms;

    @SerializedName("bathrooms")
    private Integer bathrooms;

    @SerializedName("direction")
    private String direction;

    @SerializedName("video_url")
    private String videoUrl;

    @SerializedName("mod_status")
    private String modStatus; // "pending", "approved", "rejected"

    @SerializedName("listing_status")
    private String listingStatus; // "active", "negotiating", etc.

    @SerializedName("vip_tier")
    private String vipTier; // "none", "silver", "gold"

    @SerializedName("vip_expires_at")
    private String vipExpiresAt;

    @SerializedName("expires_at")
    private String expiresAt;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    // Optional relationships
    @SerializedName("owner")
    private User owner;

    @SerializedName("type_name")
    private String typeName;

    @SerializedName("district_name")
    private String districtName;

    @SerializedName("city_name")
    private String cityName;

    @SerializedName("images")
    private List<PropertyImage> images = new ArrayList<>();

    @SerializedName("features")
    private List<Feature> features = new ArrayList<>();

    @SerializedName("primary_image")
    private String primaryImage;

    // Getters and Setters
    public int getPropertyId() { return propertyId; }
    public void setPropertyId(int propertyId) { this.propertyId = propertyId; }

    public int getOwnerId() { return ownerId; }
    public void setOwnerId(int ownerId) { this.ownerId = ownerId; }

    public int getTypeId() { return typeId; }
    public void setTypeId(int typeId) { this.typeId = typeId; }

    public Integer getDistrictId() { return districtId; }
    public void setDistrictId(Integer districtId) { this.districtId = districtId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getListingType() { return listingType; }
    public void setListingType(String listingType) { this.listingType = listingType; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public double getPriceUsd() { return priceUsd; }
    public void setPriceUsd(double priceUsd) { this.priceUsd = priceUsd; }

    public Double getAreaM2() { return areaM2; }
    public void setAreaM2(Double areaM2) { this.areaM2 = areaM2; }

    public Integer getBedrooms() { return bedrooms; }
    public void setBedrooms(Integer bedrooms) { this.bedrooms = bedrooms; }

    public Integer getBathrooms() { return bathrooms; }
    public void setBathrooms(Integer bathrooms) { this.bathrooms = bathrooms; }

    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public String getModStatus() { return modStatus; }
    public void setModStatus(String modStatus) { this.modStatus = modStatus; }

    public String getListingStatus() { return listingStatus; }
    public void setListingStatus(String listingStatus) { this.listingStatus = listingStatus; }

    public String getVipTier() { return vipTier; }
    public void setVipTier(String vipTier) { this.vipTier = vipTier; }

    public String getVipExpiresAt() { return vipExpiresAt; }
    public void setVipExpiresAt(String vipExpiresAt) { this.vipExpiresAt = vipExpiresAt; }

    public String getExpiresAt() { return expiresAt; }
    public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public String getTypeName() { return typeName; }
    public void setTypeName(String typeName) { this.typeName = typeName; }

    public String getDistrictName() { return districtName; }
    public void setDistrictName(String districtName) { this.districtName = districtName; }

    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }

    public List<PropertyImage> getImages() { return images; }
    public void setImages(List<PropertyImage> images) { this.images = images; }

    public List<Feature> getFeatures() { return features; }
    public void setFeatures(List<Feature> features) { this.features = features; }

    public String getPrimaryImage() { return primaryImage; }
    public void setPrimaryImage(String primaryImage) { this.primaryImage = primaryImage; }
}
