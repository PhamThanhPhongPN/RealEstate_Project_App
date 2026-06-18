package com.example.realestate.data.model;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("user")
    private User user;

    @SerializedName("error")
    private String error;

    @SerializedName("requiresVerification")
    private Boolean requiresVerification;

    // Getters and Setters
    public boolean isSuccess() { return success || user != null || (message != null && error == null); }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public Boolean getRequiresVerification() { return requiresVerification; }
    public void setRequiresVerification(Boolean requiresVerification) { this.requiresVerification = requiresVerification; }
}
