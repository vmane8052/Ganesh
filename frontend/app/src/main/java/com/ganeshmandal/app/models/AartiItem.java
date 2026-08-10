package com.ganeshmandal.app.models;

public class AartiItem {
    private String id;
    private String icon;
    private String title;
    private String lyrics;

    public AartiItem(String id, String icon, String title, String lyrics) {
        this.id = id;
        this.icon = icon;
        this.title = title;
        this.lyrics = lyrics;
    }

    public String getId() {
        return id;
    }

    public String getIcon() {
        return icon;
    }

    public String getTitle() {
        return title;
    }

    public String getLyrics() {
        return lyrics;
    }
}
