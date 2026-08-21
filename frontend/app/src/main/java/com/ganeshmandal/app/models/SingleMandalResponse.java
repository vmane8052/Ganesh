package com.ganeshmandal.app.models;

import com.google.gson.annotations.SerializedName;

public class SingleMandalResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private Mandal data;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Mandal getData() { return data; }
}
