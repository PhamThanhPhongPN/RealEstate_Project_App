package com.example.realestate.data.model;

import com.google.gson.annotations.SerializedName;

public class CommonResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("error")
    private String error;

    // Getters and Setters
    public boolean isSuccess() { return success || (message != null && error == null); }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}
