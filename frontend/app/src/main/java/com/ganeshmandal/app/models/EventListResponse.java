package com.ganeshmandal.app.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class EventListResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("data")
    private List<MandalEvent> data;

    public boolean isSuccess() {
        return success;
    }

    public List<MandalEvent> getData() {
        return data;
    }
}
