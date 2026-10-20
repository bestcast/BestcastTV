package com.bestcasttv.bestcasttvapp;

import android.content.Context;
import com.bestcasttv.bestcasttvapp.api_services.RetrofitClient;

public class URLconfig {

    String DOMAIN_NAME;
    String LOGIN_URL;
    String PRIVACY_POLICY;
    String TERMS_AND_CONDITIONS;
    String HELPURL;
    String TERMS_URL;
    String MYACCOUNTURL;

    String CHANGE_PASSWORD_URL;

    Context context;

    public URLconfig(Context context) {
        this.context = context;

        this.DOMAIN_NAME = RetrofitClient.BASE_URL;
        this.LOGIN_URL = DOMAIN_NAME + "/api/login";
        this.PRIVACY_POLICY = DOMAIN_NAME + "/api/privacy-policy";
        this.TERMS_AND_CONDITIONS = DOMAIN_NAME + "/api/terms-conditions";
        this.TERMS_URL = DOMAIN_NAME + "/api/pricing";
        this.HELPURL=DOMAIN_NAME + "/api/help";
        this.MYACCOUNTURL=DOMAIN_NAME + "/api/my-account";
        this.CHANGE_PASSWORD_URL = DOMAIN_NAME + "/api/changePassword";

    }
}
