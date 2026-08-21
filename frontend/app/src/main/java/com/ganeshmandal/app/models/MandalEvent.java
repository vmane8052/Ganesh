package com.ganeshmandal.app.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class MandalEvent implements Serializable {
    @SerializedName("_id")
    private String id;

    @SerializedName("dayTitle")
    private String dayTitle; // दिवस १ (श्री गणेश प्रतिष्ठापना व आगमन)

    @SerializedName("date")
    private String date; // ०८ सप्टेंबर २०२६

    @SerializedName("morningAarti")
    private String morningAarti; // सकाळची आरती (नाव / यजमान)

    @SerializedName("eveningAarti")
    private String eveningAarti; // संध्याकाळची आरती (नाव / यजमान)

    @SerializedName("lunchHost")
    private String lunchHost; // महाप्रसाद / जेवणाचा मान (नाव / यजमान)

    @SerializedName("modakHost")
    private String modakHost; // मोदकाचा मान (नाव / यजमान)

    @SerializedName("culturalProgram")
    private String culturalProgram; // सांस्कृतिक कार्यक्रम / भजन / पूजा

    @SerializedName("specialNotes")
    private String specialNotes; // विशेष सूचना / टीप

    @SerializedName("mandalId")
    private String mandalId;

    public MandalEvent() {
    }

    public MandalEvent(String dayTitle, String date, String morningAarti, String eveningAarti, String lunchHost, String modakHost, String culturalProgram, String specialNotes) {
        this.dayTitle = dayTitle;
        this.date = date;
        this.morningAarti = morningAarti;
        this.eveningAarti = eveningAarti;
        this.lunchHost = lunchHost;
        this.modakHost = modakHost;
        this.culturalProgram = culturalProgram;
        this.specialNotes = specialNotes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDayTitle() {
        return dayTitle;
    }

    public void setDayTitle(String dayTitle) {
        this.dayTitle = dayTitle;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMorningAarti() {
        return morningAarti != null ? morningAarti : "";
    }

    public void setMorningAarti(String morningAarti) {
        this.morningAarti = morningAarti;
    }

    public String getEveningAarti() {
        return eveningAarti != null ? eveningAarti : "";
    }

    public void setEveningAarti(String eveningAarti) {
        this.eveningAarti = eveningAarti;
    }

    public String getLunchHost() {
        return lunchHost != null ? lunchHost : "";
    }

    public void setLunchHost(String lunchHost) {
        this.lunchHost = lunchHost;
    }

    public String getModakHost() {
        return modakHost != null ? modakHost : "";
    }

    public void setModakHost(String modakHost) {
        this.modakHost = modakHost;
    }

    public String getCulturalProgram() {
        return culturalProgram != null ? culturalProgram : "";
    }

    public void setCulturalProgram(String culturalProgram) {
        this.culturalProgram = culturalProgram;
    }

    public String getSpecialNotes() {
        return specialNotes != null ? specialNotes : "";
    }

    public void setSpecialNotes(String specialNotes) {
        this.specialNotes = specialNotes;
    }

    public String getMandalId() { return mandalId; }
    public void setMandalId(String mandalId) { this.mandalId = mandalId; }
}
