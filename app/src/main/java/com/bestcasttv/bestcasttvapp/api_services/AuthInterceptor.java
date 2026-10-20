package com.bestcasttv.bestcasttvapp.api_services;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    String token;

    public AuthInterceptor(String _token) {

        this.token = _token;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {


        Request chainRequest = chain.request();
        if(chainRequest.header("No-Authentication")==null){
            chainRequest = chainRequest.newBuilder()
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .build();
        }


        return chain.proceed(chainRequest);
    }

}
