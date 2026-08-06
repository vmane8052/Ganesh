package com.ganeshmandal.app.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class UserListResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("data")
    private List<User> data;

    public boolean isSuccess() { return success; }
    public List<User> getData() { return data; }
}
