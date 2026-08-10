package com.ganeshmandal.app.models;

import com.google.gson.annotations.SerializedName;

public class GalleryPhoto {
    @SerializedName("_id")
    private String id;

    @SerializedName("title")
    private String title;

    @SerializedName("imageUrl")
    private String imageUrl;

    @SerializedName("uploadedBy")
    private String uploadedBy;

    @SerializedName("year")
    private String year;

    @SerializedName("createdAt")
    private String createdAt;

    public GalleryPhoto() {}

    public GalleryPhoto(String title, String imageUrl, String uploadedBy, String year) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.uploadedBy = uploadedBy;
        this.year = year != null ? year : "2026";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public String getYear() {
        return year != null ? year : "2026";
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
