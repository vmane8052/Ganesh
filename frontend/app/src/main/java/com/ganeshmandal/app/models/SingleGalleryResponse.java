package com.ganeshmandal.app.models;

import com.google.gson.annotations.SerializedName;

public class SingleGalleryResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private GalleryPhoto data;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public GalleryPhoto getData() {
        return data;
    }
}
