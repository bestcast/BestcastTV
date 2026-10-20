package com.bestcasttv.bestcasttvapp.login_activity;

public class LoginQRcodeRequest {
    String qrcode;
    String country_code;

    public LoginQRcodeRequest(String qrcode, String country_code) {
        this.qrcode = qrcode;
        this.country_code = country_code;
    }

    public String getQrcode() {
        return qrcode;
    }

    public void setQrcode(String qrcode) {
        this.qrcode = qrcode;
    }

    public String getCountry_code() {
        return country_code;
    }

    public void setCountry_code(String country_code) {
        this.country_code = country_code;
    }
}
