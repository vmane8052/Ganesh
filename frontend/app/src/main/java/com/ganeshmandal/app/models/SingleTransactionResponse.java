package com.ganeshmandal.app.models;

import com.google.gson.annotations.SerializedName;

public class SingleTransactionResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private Transaction data;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Transaction getData() {
        return data;
    }
}
