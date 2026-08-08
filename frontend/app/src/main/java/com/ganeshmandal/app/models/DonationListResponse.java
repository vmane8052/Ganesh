package com.ganeshmandal.app.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class DonationListResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("data")
    private List<Donation> data;

    public boolean isSuccess() {
        return success;
    }

    public List<Donation> getData() {
        return data;
    }
}
