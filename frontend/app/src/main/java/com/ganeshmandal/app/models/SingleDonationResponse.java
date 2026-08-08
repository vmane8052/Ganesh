package com.ganeshmandal.app.models;

import com.google.gson.annotations.SerializedName;

public class SingleDonationResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private Donation data;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Donation getData() {
        return data;
    }
}
