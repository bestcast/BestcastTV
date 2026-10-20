package com.bestcasttv.bestcasttvapp.otp;

public class OTPRequest {
    String email;
    String country_code;

    public OTPRequest(String email, String country_code) {
        this.email = email;
        this.country_code = country_code;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCountry_code() {
        return country_code;
    }

    public void setCountry_code(String country_code) {
        this.country_code = country_code;
    }
}
