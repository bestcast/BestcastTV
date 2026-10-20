package com.bestcasttv.bestcasttvapp.api_services;

import com.bestcasttv.bestcasttvapp.login_activity.LoginQRcodeRequest;
import com.bestcasttv.bestcasttvapp.login_activity.LoginRequest;
import com.bestcasttv.bestcasttvapp.move_details_page.model.LikesModel;
import com.bestcasttv.bestcasttvapp.move_details_page.model.MyListRequestModel;
import com.bestcasttv.bestcasttvapp.otp.OTPLoginRequestModel;
import com.bestcasttv.bestcasttvapp.otp.OTPRequest;
import com.bestcasttv.bestcasttvapp.playbackmodels.WatchedModel;
import com.bestcasttv.bestcasttvapp.playbackmodels.WatchingModel;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface ApiService {

    @POST("/api/login")
    Call<JsonObject> login(@Body LoginRequest loginUser);

    @POST("/api/login-with-otp")
    Call<JsonObject> loginWithOtp(@Body OTPLoginRequestModel loginUser);

    @POST("/api/loginbyqrcode")
    Call<JsonObject> loginbyqrcode(@Body LoginQRcodeRequest loginQRUser);

    @POST("/api/send-otp")
    Call<JsonObject> sendOTP(@Body OTPRequest otpRequest);

    @GET("/api/generateqrcode")
    Call<JsonObject> generateqrcode();

    @POST("/api/register")
    Call<JsonObject> register(@Body LoginRequest loginUser);

    @POST("/api/logout")
    Call<JsonObject> logout();

    @POST("/api/tokenexist")
    Call<JsonObject> getTokenexist();

    @GET("/api/userprofilelist")
    Call<JsonObject> getUserList();

    @POST("/api/user")
    Call<JsonObject> getUser();

    @GET("/api/bannerlist")
    Call<JsonObject> getBannerlist(
            @Query("page_id") String pageId,
            @Query("profile_id") String profileId
    );

    @GET("/api/blockslist")
    Call<JsonObject> getBlockslist(
            @Query("page_id") int pageId,
            @Query("profile_id") String profileId,
            @Query("genre_id") String genreId
    );


    @GET("/api/usermovieslist")
    Call<JsonObject> getUsermovieslist(
            @Query("profile_id") String profileId,
            @Query("mylist") String mylist
    );

    @GET("/api/usermovieslist")
    Call<JsonObject> getUsermoviesWatchinglist(
            @Query("profile_id") String profileId,
            @Query("watching") String watchinglist
    );

    @GET("/api/movieslist")
    Call<JsonObject> getSearchMovieslist(
            @Query("search") String searchText,
            @Query("profile_id") String profileId
    );


    @GET("/api/genrelist")
    Call<JsonObject> getGenrelist();

    @GET
    Call<JsonObject> getUsermovie(@Url String ur);

    @POST
    Call<JsonObject> setUsermovieLikes(
            @Url String url,
            @Body LikesModel likesModel
    );

    @POST
    Call<JsonObject> setUsermovieMylist(
            @Url String url,
            @Body MyListRequestModel myListRequestModel
    );

    @POST
    Call<JsonObject> setUsermovieWatching(
            @Url String url,
            @Body WatchingModel watchingModel
    );

    @POST
    Call<JsonObject> setUsermovieWatched(
            @Url String url,
            @Body WatchedModel watchedModel
    );


}
