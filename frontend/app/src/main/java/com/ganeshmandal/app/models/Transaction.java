package com.ganeshmandal.app.models;

import com.google.gson.annotations.SerializedName;

public class Transaction {
    @SerializedName("_id")
    private String id;

    @SerializedName("type")
    private String type; // "JAMA" or "KHARCH"

    @SerializedName("amount")
    private double amount;

    @SerializedName("details")
    private String details;

    @SerializedName("date")
    private String date;

    @SerializedName("category")
    private String category;

    @SerializedName("memberName")
    private String memberName;

    public Transaction(String type, double amount, String details, String date, String category, String memberName) {
        this.type = type;
        this.amount = amount;
        this.details = details;
        this.date = date;
        this.category = category;
        this.memberName = memberName;
    }

    public String getId() { return id; }
    public String getType() { return type; }
    public double getAmount() { return amount; }
    public String getDetails() { return details; }
    public String getDate() { return date; }
    public String getCategory() { return category; }
    public String getMemberName() { return memberName; }
    public boolean isJama() { return "JAMA".equalsIgnoreCase(type); }
}
