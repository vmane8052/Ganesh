package com.ganeshmandal.app.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TransactionResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("data")
    private List<Transaction> data;

    public boolean isSuccess() { return success; }
    public List<Transaction> getData() { return data; }
}
