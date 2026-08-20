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

    @SerializedName("memberPhone")
    private String memberPhone;

    @SerializedName("receiptNo")
    private String receiptNo;

    public Transaction(String type, double amount, String details, String date, String category, String memberName, String memberPhone) {
        this.type = type;
        this.amount = amount;
        this.details = details;
        this.date = date;
        this.category = category;
        this.memberName = memberName;
        this.memberPhone = memberPhone;
    }

    public Transaction(String type, double amount, String details, String date, String category, String memberName, String memberPhone, String receiptNo) {
        this(type, amount, details, date, category, memberName, memberPhone);
        this.receiptNo = receiptNo;
    }

    public String getId() { return id; }
    public String getType() { return type; }
    public double getAmount() { return amount; }
    public String getDetails() { return details; }
    public String getDate() { return date; }
    public String getCategory() { return category; }
    public String getMemberName() { return memberName; }
    public String getMemberPhone() { return memberPhone; }
    public String getReceiptNo() { return receiptNo; }
    public boolean isJama() { return "JAMA".equalsIgnoreCase(type); }
}
