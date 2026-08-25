package com.ganeshmandal.app.models;

import com.google.gson.annotations.SerializedName;

public class GenericResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("debugOtp")
    private String debugOtp;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getDebugOtp() { return debugOtp; }
}
