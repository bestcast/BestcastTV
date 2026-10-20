package com.bestcasttv.bestcasttvapp.otp;

public class OTPLoginRequestModel {
    String email;
    String otp;
    String device;
    String country_code;

    public OTPLoginRequestModel(String email, String otp, String device, String country_code) {
        this.email = email;
        this.otp = otp;
        this.device = device;
        this.country_code = country_code;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getCountry_code() {
        return country_code;
    }

    public void setCountry_code(String country_code) {
        this.country_code = country_code;
    }
}
