package com.ganeshmandal.app.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GalleryListResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("data")
    private List<GalleryPhoto> data;

    public boolean isSuccess() {
        return success;
    }

    public List<GalleryPhoto> getData() {
        return data;
    }
}
