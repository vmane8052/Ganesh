package com.ganeshmandal.app.models;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("_id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("phone")
    private String phone;

    @SerializedName("pin")
    private String pin;

    @SerializedName("role")
    private String role; // "ADMIN" or "USER"

    @SerializedName("roleInMandal")
    private String roleInMandal; // e.g. "सामान्य सदस्य", "उपाध्यक्ष"

    @SerializedName("photoUrl")
    private String photoUrl; // Base64 or Image URI string

    public User() {}

    public User(String name, String phone, String pin, String role, String roleInMandal, String photoUrl) {
        this.name = name;
        this.phone = phone;
        this.pin = pin;
        this.role = role;
        this.roleInMandal = roleInMandal;
        this.photoUrl = photoUrl;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getPin() { return pin; }
    public String getRole() { return role; }
    public String getRoleInMandal() { return roleInMandal != null && !roleInMandal.isEmpty() ? roleInMandal : ("ADMIN".equalsIgnoreCase(role) ? "मुख्य व्यवस्थापक" : "सामान्य सदस्य"); }
    public String getPhotoUrl() { return photoUrl; }
    public boolean isAdmin() { return "ADMIN".equalsIgnoreCase(role); }

    public void setName(String name) { this.name = name; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setPin(String pin) { this.pin = pin; }
    public void setRole(String role) { this.role = role; }
    public void setRoleInMandal(String roleInMandal) { this.roleInMandal = roleInMandal; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
}
