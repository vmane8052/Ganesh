package com.ganeshmandal.app.models;

import com.google.gson.annotations.SerializedName;

public class SingleEventResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private MandalEvent data;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public MandalEvent getData() {
        return data;
    }
}
