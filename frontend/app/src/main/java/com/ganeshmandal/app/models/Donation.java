package com.ganeshmandal.app.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Donation implements Serializable {
    @SerializedName("_id")
    private String id;

    @SerializedName("donorName")
    private String donorName; // देणगीदाराचे नाव

    @SerializedName("donorPhone")
    private String donorPhone; // मोबाईल नंबर

    @SerializedName("donationType")
    private String donationType; // CASH, ITEM, ONLINE

    @SerializedName("amount")
    private double amount; // रोख रक्कम (₹)

    @SerializedName("itemDetails")
    private String itemDetails; // वस्तू देणगीचे वर्णन (उदा. २१ चांदीचे मोदक, ५० किलो धान्य, चांदीचा मुकुट)

    @SerializedName("date")
    private String date; // तारीख

    @SerializedName("address")
    private String address; // गाव / पत्ता

    @SerializedName("receiptNo")
    private String receiptNo; // पावती क्रमांक

    @SerializedName("mandalId")
    private String mandalId;

    public Donation() {
    }

    public Donation(String donorName, String donorPhone, String donationType, double amount, String itemDetails, String date, String address, String receiptNo) {
        this.donorName = donorName;
        this.donorPhone = donorPhone;
        this.donationType = donationType;
        this.amount = amount;
        this.itemDetails = itemDetails;
        this.date = date;
        this.address = address;
        this.receiptNo = receiptNo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDonorName() {
        return donorName;
    }

    public void setDonorName(String donorName) {
        this.donorName = donorName;
    }

    public String getDonorPhone() {
        return donorPhone != null ? donorPhone : "";
    }

    public void setDonorPhone(String donorPhone) {
        this.donorPhone = donorPhone;
    }

    public String getDonationType() {
        return donationType != null ? donationType : "CASH";
    }

    public void setDonationType(String donationType) {
        this.donationType = donationType;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getItemDetails() {
        return itemDetails != null ? itemDetails : "";
    }

    public void setItemDetails(String itemDetails) {
        this.itemDetails = itemDetails;
    }

    public String getDate() {
        return date != null ? date : "";
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getAddress() {
        return address != null ? address : "";
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getReceiptNo() {
        return receiptNo != null ? receiptNo : "";
    }

    public void setReceiptNo(String receiptNo) {
        this.receiptNo = receiptNo;
    }

    public boolean isItem() {
        return "ITEM".equalsIgnoreCase(donationType);
    }

    public String getMandalId() { return mandalId; }
    public void setMandalId(String mandalId) { this.mandalId = mandalId; }
}
