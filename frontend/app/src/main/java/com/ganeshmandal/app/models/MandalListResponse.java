package com.ganeshmandal.app.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MandalListResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("data")
    private List<Mandal> data;

    public boolean isSuccess() { return success; }
    public List<Mandal> getData() { return data; }
}
