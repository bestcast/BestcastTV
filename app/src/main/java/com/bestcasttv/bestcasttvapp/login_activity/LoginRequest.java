package com.bestcasttv.bestcasttvapp.login_activity;

public class LoginRequest {
    String email;
    String password;
    String device;
    String country_code;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public LoginRequest(String email, String password, String device) {
        this.email = email;
        this.password = password;
        this.device = device;
    }

    public LoginRequest(String email, String password, String device, String country_code) {
        this.email = email;
        this.password = password;
        this.device = device;
        this.country_code = country_code;
    }
}
