package com.bestcasttv.bestcasttvapp.api_services;

import android.util.Log;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    public static final String BASE_URL = "https://bestcast.co";

    private static Retrofit retrofit;
    static String _token = "";

    public static OkHttpClient.Builder getUnsafeOkHttpClientBuilder() {
        try {
            final TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[]{};
                    }
                }
            };

            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
            builder.connectTimeout(30, TimeUnit.SECONDS);
            builder.readTimeout(30, TimeUnit.SECONDS);
            builder.writeTimeout(30, TimeUnit.SECONDS);
            return builder;
        } catch (Exception e) {
            Log.e("RetrofitClient", "Error creating unsafe OkHttpClient", e);
            return new OkHttpClient.Builder();
        }
    }

    public static Retrofit getClient(OkHttpClient defaultHttpClient) {
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(defaultHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit;
    }

    public static Retrofit createService(String authToken) {
        OkHttpClient defaultHttpClient = getUnsafeOkHttpClientBuilder()
                .addInterceptor(
                        chain -> {
                            Request request = chain.request().newBuilder()
                                    .headers(getJsonHeader(_token))
                                    .build();
                            return chain.proceed(request);
                        })
                .build();

        return getClient(defaultHttpClient);
    }

    public static Headers getJsonHeader(String authToken) {
        Headers.Builder builder = new Headers.Builder();
        builder.add("Content-Type", "application/json; charset=UTF-8");
        builder.add("Accept", "application/json");
        Log.d("TokenStatus", authToken != null ? authToken : "");
        if (authToken != null && !authToken.isEmpty()) {
            builder.add("Authorization", "Bearer " + authToken);
        }
        return builder.build();
    }

    public static ApiService getApiService(String token) {
        _token = token != null ? token : "";
        return RetrofitClient.createService(_token).create(ApiService.class);
    }
}
