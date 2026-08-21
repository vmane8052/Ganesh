package com.ganeshmandal.app.models;

import com.google.gson.annotations.SerializedName;

public class Mandal {
    @SerializedName("_id")
    private String id;

    @SerializedName("mandalId")
    private String mandalId; // e.g. "M001", "M002"

    @SerializedName("mandalName")
    private String mandalName;

    @SerializedName("address")
    private String address;

    @SerializedName("contactPhone")
    private String contactPhone;

    @SerializedName("status")
    private String status; // "active" or "inactive"

    public Mandal() {}

    public Mandal(String mandalName, String address, String contactPhone) {
        this.mandalName = mandalName;
        this.address = address;
        this.contactPhone = contactPhone;
        this.status = "active";
    }

    public String getId() { return id; }
    public String getMandalId() { return mandalId; }
    public String getMandalName() { return mandalName; }
    public String getAddress() { return address; }
    public String getContactPhone() { return contactPhone; }
    public String getStatus() { return status; }

    public void setMandalId(String mandalId) { this.mandalId = mandalId; }
    public void setMandalName(String mandalName) { this.mandalName = mandalName; }
    public void setAddress(String address) { this.address = address; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public void setStatus(String status) { this.status = status; }
}
