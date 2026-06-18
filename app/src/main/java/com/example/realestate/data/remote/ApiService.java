package com.example.realestate.data.remote;

import com.example.realestate.data.model.*;
import java.util.List;
import java.util.Map;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {

    // ── Authentication Endpoints ──
    @POST("api/auth/register")
    Call<AuthResponse> register(@Body Map<String, String> body);

    @POST("api/auth/verify-otp")
    Call<AuthResponse> verifyOtp(@Body Map<String, String> body);

    @POST("api/auth/login")
    Call<AuthResponse> login(@Body Map<String, String> body);

    @POST("api/auth/logout")
    Call<CommonResponse> logout();

    @GET("api/auth/me")
    Call<AuthResponse> getMe();

    @PUT("api/auth/profile")
    Call<AuthResponse> updateProfile(@Body Map<String, String> body);

    @POST("api/auth/forgot-password")
    Call<CommonResponse> forgotPassword(@Body Map<String, String> body);

    @POST("api/auth/reset-password")
    Call<CommonResponse> resetPassword(@Body Map<String, String> body);

    @PUT("api/auth/change-password")
    Call<CommonResponse> changePassword(@Body Map<String, String> body);

    // ── Properties Catalog & Search Endpoints ──
    @GET("api/properties/search")
    Call<SearchResponse> searchProperties(
        @Query("keyword") String keyword,
        @Query("type_id") Integer typeId,
        @Query("city_id") Integer cityId,
        @Query("district_id") Integer districtId,
        @Query("listing_type") String listingType,
        @Query("minPrice") String minPrice,
        @Query("maxPrice") String maxPrice,
        @Query("bedrooms") Integer bedrooms,
        @Query("bathrooms") Integer bathrooms,
        @Query("direction") String direction,
        @Query("sort") String sort,
        @Query("limit") int limit,
        @Query("page") int page
    );

    @GET("api/properties/metadata")
    Call<SearchMetadata> getMetadata();

    @GET("api/properties/{id}")
    Call<Property> getPropertyById(@Path("id") int id);

    @GET("api/properties/{id}/similar")
    Call<List<Property>> getSimilarProperties(@Path("id") int id);

    @GET("api/properties/me")
    Call<List<Property>> getMyProperties();

    @POST("api/properties")
    Call<Property> createProperty(@Body Map<String, Object> body);

    @PUT("api/properties/{id}")
    Call<CommonResponse> updateProperty(@Path("id") int id, @Body Map<String, Object> body);

    @DELETE("api/properties/{id}")
    Call<CommonResponse> deleteProperty(@Path("id") int id);

    @PATCH("api/properties/{id}/status")
    Call<CommonResponse> updatePropertyStatus(@Path("id") int id, @Body Map<String, String> body);

    @PATCH("api/properties/{id}/renew")
    Call<CommonResponse> renewListing(@Path("id") int id);

    // ── Image Uploads ──
    @Multipart
    @POST("api/media/upload")
    Call<CommonResponse> uploadImages(
        @Part("property_id") RequestBody propertyId,
        @Part List<MultipartBody.Part> images
    );

    @DELETE("api/media/{image_id}")
    Call<CommonResponse> deleteImage(@Path("image_id") int imageId);

    // ── Favorites Endpoints ──
    @GET("api/favorites")
    Call<List<Property>> getFavorites();

    @GET("api/favorites/ids")
    Call<List<Integer>> getFavoriteIds();

    @POST("api/favorites/{property_id}")
    Call<Map<String, Boolean>> toggleFavorite(@Path("property_id") int propertyId);

    // ── Chat & Conversation Endpoints ──
    @GET("api/conversations")
    Call<List<Conversation>> getConversations();

    @POST("api/conversations")
    Call<Map<String, String>> createConversation(@Body Map<String, String> body);

    @GET("api/conversations/{id}/messages")
    Call<List<Message>> getConversationMessages(
        @Path("id") int id,
        @Query("before") Integer before,
        @Query("limit") int limit
    );

    // ── Subscription Endpoints ──
    @POST("api/subscriptions/simulate")
    Call<CommonResponse> simulateSubscription(@Body Map<String, String> body);

    // ── Reports Endpoints ──
    @POST("api/reports")
    Call<CommonResponse> submitReport(@Body Map<String, Object> body);

    // ── Notifications Endpoints ──
    @GET("api/notifications")
    Call<List<Notification>> getNotifications();

    @PATCH("api/notifications/read-all")
    Call<CommonResponse> markAllNotificationsRead();

    @PATCH("api/notifications/{id}/read")
    Call<CommonResponse> markNotificationRead(@Path("id") int notifId);

    // ── Profile Image / Avatar Endpoints ──
    @Multipart
    @POST("api/auth/avatar")
    Call<AuthResponse> uploadAvatar(@Part MultipartBody.Part file);

    // ── Recently Viewed Endpoints ──
    @GET("api/properties/recently-viewed")
    Call<List<Property>> getRecentlyViewed();
}
