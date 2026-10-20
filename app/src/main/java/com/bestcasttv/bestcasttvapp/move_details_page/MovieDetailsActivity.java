package com.bestcasttv.bestcasttvapp.move_details_page;

import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ProgressBar;

import com.bestcasttv.bestcasttvapp.LoginActivity;
import com.bestcasttv.bestcasttvapp.who_watching.WhoWatchingActivity;
import com.bumptech.glide.Glide;
import com.bestcasttv.bestcasttvapp.DetailsActivity;
import com.bestcasttv.bestcasttvapp.PlaybackActivity;
import com.bestcasttv.bestcasttvapp.R;
import com.bestcasttv.bestcasttvapp.api_services.ApiService;
import com.bestcasttv.bestcasttvapp.api_services.RetrofitClient;
import com.bestcasttv.bestcasttvapp.app_utils.AppUtils;
import com.bestcasttv.bestcasttvapp.app_utils.LoadingDialog;
import com.bestcasttv.bestcasttvapp.databinding.ActivityMovieDetailsBinding;
import com.bestcasttv.bestcasttvapp.move_details_page.model.LikesModel;
import com.bestcasttv.bestcasttvapp.move_details_page.model.MyListRequestModel;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieDetailsActivity extends FragmentActivity {

    ActivityMovieDetailsBinding binding;

    AppUtils appUtils = new AppUtils(this);
    LoadingDialog loadingDialog;
    SharedPreferences sharedPreferences;

    MovieData movieData;
    int movieId = 0;

    String _token = "";
    String profileName = "";
    String profilePicture = "";
    String profileID = "";
    String profilePictureID = "";

    boolean _isRated = false;
    boolean _isLike = false;
    boolean _isDisLike = false;

    String _WatchedPercent = "";
    String _WatchingTime = "";
    boolean onPressPlay = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_movie_details);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_movie_details);

        String _movieId = getIntent().getStringExtra("movieId");
//        _WatchedPercent = getIntent().getStringExtra("WatchedPercent");
//        _WatchingTime = getIntent().getStringExtra("WatchingTime");

        movieId = Integer.parseInt(_movieId);
        loadingDialog = new LoadingDialog(this);

        sharedPreferences = getSharedPreferences(getString(R.string.shared_name), Context.MODE_PRIVATE);
        _token = sharedPreferences.getString(getString(R.string.token), "");
        profileName = sharedPreferences.getString(getString(R.string.profileName), "");
        profilePicture = sharedPreferences.getString(getString(R.string.profilePicture), "");
        profileID = sharedPreferences.getString(getString(R.string.profileID), "");
        profilePictureID = sharedPreferences.getString(getString(R.string.profilePictureID), "");
        Log.d("Item_ID:", _movieId + "----profileID:" + profileID);

        getUserMoviesDetails(_token, profileID, movieId);

        if (!_WatchedPercent.equals("")) {
            binding.seekViewProgressBar.setVisibility(View.VISIBLE);
            binding.seekViewProgressBar.setProgress(Integer.parseInt(_WatchedPercent));
        } else {
            binding.seekViewProgressBar.setVisibility(View.GONE);
        }


        binding.addToMylist.setOnKeyListener((view, keyCode, keyEvent) -> {
            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
//                castFragment.requestFocus();
            }
            return false;
        });


        binding.play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onPressPlay=true;
                getUserMoviesDetails(_token, profileID, movieId);
//                Gson gson = new Gson();
//                String jsonMovieData = gson.toJson(movieData);
//                Log.d("AllMovieData: ", jsonMovieData);
//                Intent intent = new Intent(MovieDetailsActivity.this, PlaybackActivity.class);
//                intent.putExtra(DetailsActivity.MOVIE, jsonMovieData);
//                intent.putExtra(DetailsActivity.WATCHEDPERCENT, _WatchedPercent);
//                intent.putExtra(DetailsActivity.WATCHINGTIME, _WatchingTime);
//                startActivity(intent);
            }
        });


        binding.addLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLikeData();
            }
        });

        binding.addDislike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDisLikeData();
            }
        });

        binding.addToMylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setRateData();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Onresume", "Load");
        getUserMoviesDetails(_token, profileID, movieId);
    }

    private void setLikeData() {

        Drawable img;
        int isLikeValue = 0;
        _isDisLike = false;
        if (_isLike) {
            _isLike = false;
            isLikeValue = 0;
            img = ContextCompat.getDrawable(this, R.drawable.outline_thumb_up_24);
        } else {
            _isLike = true;
            isLikeValue = 1;
            img = ContextCompat.getDrawable(this, R.drawable.baseline_thumb_up_24);
        }

        Drawable imgDisLike = ContextCompat.getDrawable(this, R.drawable.outline_thumb_down_24);
        binding.addLike.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
        binding.addDislike.setCompoundDrawablesWithIntrinsicBounds(imgDisLike, null, null, null);

        LikesModel likesModel = new LikesModel(isLikeValue);
        setUsermovieLikes(_token, profileID, String.valueOf(movieId), likesModel);

    }

    private void setLikeImageResource() {
        Drawable img;
        if (_isLike) {
            img = ContextCompat.getDrawable(this, R.drawable.baseline_thumb_up_24);
        } else {
            img = ContextCompat.getDrawable(this, R.drawable.outline_thumb_up_24);
        }
        binding.addLike.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
    }

    private void setDisLikeData() {
        int isDisLikeValue = 0;
        Drawable img;
        _isLike = false;
        if (_isDisLike) {
            _isDisLike = false;
            isDisLikeValue = 0;
            img = ContextCompat.getDrawable(this, R.drawable.outline_thumb_down_24);
        } else {
            _isDisLike = true;
            isDisLikeValue = 2;
            img = ContextCompat.getDrawable(this, R.drawable.baseline_thumb_down_24);
        }
        Drawable imgLike = ContextCompat.getDrawable(this, R.drawable.outline_thumb_up_24);

        binding.addDislike.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
        binding.addLike.setCompoundDrawablesWithIntrinsicBounds(imgLike, null, null, null);
        LikesModel likesModel = new LikesModel(isDisLikeValue);
        setUsermovieLikes(_token, profileID, String.valueOf(movieId), likesModel);
    }

    private void setDisLikeImageResource() {
        Drawable img;
        if (_isDisLike) {
            img = ContextCompat.getDrawable(this, R.drawable.baseline_thumb_down_24);
        } else {
            img = ContextCompat.getDrawable(this, R.drawable.outline_thumb_down_24);
        }
        binding.addDislike.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
    }

    private void setRateData() {

        Drawable img;

        int isMyList = 0;
        if (_isRated) {
            _isRated = false;
            isMyList = 0;
            img = ContextCompat.getDrawable(this, R.drawable.baseline_add_24);
        } else {
            _isRated = true;
            isMyList = 1;
            img = ContextCompat.getDrawable(this, R.drawable.baseline_check_24);
        }

        binding.addToMylist.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);

        MyListRequestModel myListRequestModel = new MyListRequestModel(isMyList);
        setUsermovieMylist(_token, profileID, String.valueOf(movieId), myListRequestModel);
    }

    private void setRateImageResource() {
        Drawable img;
        if (_isRated) {
            img = ContextCompat.getDrawable(this, R.drawable.baseline_check_24);
        } else {
            img = ContextCompat.getDrawable(this, R.drawable.baseline_add_24);
        }
        binding.addToMylist.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
    }

    private void setData(MovieData response) {
        binding.title.setText(response.getTitle());

        String yearMoive = "";
        try {
            yearMoive = appUtils.getDateTimeToYear(response.getReleaseDate());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        String subTileText = yearMoive + "  " + response.getCertificate() + "  " + response.getDurationText();

        binding.subtitle.setText(subTileText);
        binding.description.setText(response.getContent());

        Glide.with(this)
                .load(response.getImage())
                .placeholder(R.drawable.default_landscape)
                .error(R.drawable.default_landscape)
                .into(binding.imgBanner);

        binding.description.setText(response.getContent());
//        binding.description.setEllipsized(isEllipsized -> {
//            binding.showMore.setVisibility(isEllipsized ? View.VISIBLE : View.GONE);
//            binding.showMore.setOnClickListener(v -> Common.descriptionDialog(
//                    this,
//                    response.getTitle(),
//                    getSubtitle(response),
//                    response.getOverview()
//            ));
//        });
    }

    void getUserMoviesDetails(String token, String profileId, int movieID) {
        binding.progressBar.setVisibility(View.VISIBLE);
        loadingDialog.show();
        ApiService apiService = RetrofitClient.getApiService(token);
        String url = "/api/getusermovie/" + movieID + "?profile_id=" + profileId;
        Call<JsonObject> call = apiService.getUsermovie(url);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                Log.d("UserMovie_Response", response.toString());
                if (response.code() == 201) {
                    getTokenValid(token);
                }
                if (response.code() == 200) {
                    if (response.isSuccessful() && response.body() != null) {
                        JsonObject data = response.body();

                        Log.d("UserMovie_Response", data.toString());

                        try {
                            JSONObject jsonObject = new JSONObject(data.toString());
                            JSONObject jsonObject1 = jsonObject.getJSONObject("data");

                            String _id = jsonObject1.getString("id").toString();
                            String _urlkey = jsonObject1.getString("urlkey").toString();
                            String _title = jsonObject1.getString("title").toString();
                            String _contentTagText = jsonObject1.getString("content").toString();
                            String _content = jsonObject1.getString("content_plain").toString();
                            String _published_date = jsonObject1.getString("published_date").toString();
                            String _release_date = jsonObject1.getString("release_date").toString();

                            String _image = RetrofitClient.BASE_URL + "/" + jsonObject1.getString("image").toString();
                            String _medium = RetrofitClient.BASE_URL + "/" + jsonObject1.getString("medium").toString();
                            String _thumbnail = RetrofitClient.BASE_URL + "/" + jsonObject1.getString("thumbnail").toString();
                            String _portraitsmall = RetrofitClient.BASE_URL + "/" + jsonObject1.getString("portraitsmall").toString();
                            String _portrait = RetrofitClient.BASE_URL + "/" + jsonObject1.getString("portrait").toString();

                            String _duration = jsonObject1.getString("duration").toString();
                            String _durationText = jsonObject1.getString("duration_text").toString();
                            String _certificate = jsonObject1.getString("certificate").toString();
                            String _certificate_text = jsonObject1.getString("certificate_text").toString();
                            String _tagText = jsonObject1.getString("tag_text").toString();
                            String _topten = jsonObject1.getString("topten").toString();
                            String _trailer = jsonObject1.getString("trailer").toString();
                            String _trailer480p = jsonObject1.getString("trailer_480p").toString();
//                        String _videoUrl = jsonObject1.getString("video_url").toString();
                            String _videoUrl = jsonObject1.getString("video_url_720p").toString();
                            String _moviesource = jsonObject1.getString("moviesource").toString();
                            String _subtitle_status = jsonObject1.getString("subtitle_status").toString();

                            movieData = new MovieData(
                                    _id.toString(),
                                    _urlkey.toString(),
                                    _title.toString(),
                                    _content.toString(),
                                    _published_date.toString(),
                                    _release_date.toString(),
                                    _image.toString(),
                                    _medium.toString(),
                                    _thumbnail.toString(),
                                    _portraitsmall.toString(),
                                    _portrait.toString(),
                                    _duration.toString(),
                                    _durationText.toString(),
                                    _certificate.toString(),
                                    _certificate_text.toString(),
                                    _tagText.toString(),
                                    _topten.toString(),
                                    _trailer.toString(),
                                    _trailer480p.toString(),
                                    _videoUrl.toString(),
                                    _moviesource.toString(),
                                    _subtitle_status.toString());

                            if (jsonObject1.getJSONObject("usermovies") != null || jsonObject1.getString("usermovies") != "") {

                                JSONObject jsonObject2 = jsonObject1.getJSONObject("usermovies");

                                int myList = jsonObject2.getInt("mylist");
                                int likeStatus = jsonObject2.getInt("likes");
                                _WatchingTime = jsonObject2.getString("watch_time").toString();
                                String watching = jsonObject2.getString("watching").toString();
                                String watched = jsonObject2.getString("watched").toString();
                                _WatchedPercent = jsonObject2.getString("watched_percent").toString();

                                Log.d("_WatchingTime_percent", _WatchingTime + "-- " + _WatchedPercent);
                                if (!_WatchedPercent.equals("")) {
                                    binding.seekViewProgressBar.setVisibility(View.VISIBLE);
                                    binding.seekViewProgressBar.setProgress(Integer.parseInt(_WatchedPercent));
                                } else {
                                    binding.seekViewProgressBar.setVisibility(View.GONE);
                                }
                                if (myList == 0) {
                                    _isRated = false;
                                } else {
                                    _isRated = true;
                                }

                                //TODO change if need based on like status 1, 2
                                if (likeStatus == 2) {
                                    _isDisLike = true;
                                    _isLike = false;
                                } else if (likeStatus == 1) {
                                    _isDisLike = false;
                                    _isLike = true;
                                } else {
                                    _isDisLike = false;
                                    _isLike = false;
                                }

                                setLikeImageResource();
                                setDisLikeImageResource();
                                setRateImageResource();


                            }

                            setData(movieData);

                            if (onPressPlay){

                                onPressPlay=false;
                                Gson gson = new Gson();
                                String jsonMovieData = gson.toJson(movieData);
                                Log.d("AllMovieData: ", jsonMovieData);
                                Intent intent = new Intent(MovieDetailsActivity.this, PlaybackActivity.class);
                                intent.putExtra(DetailsActivity.MOVIE, jsonMovieData);
                                intent.putExtra(DetailsActivity.WATCHEDPERCENT, _WatchedPercent);
                                intent.putExtra(DetailsActivity.WATCHINGTIME, _WatchingTime);
                                startActivity(intent);

                                onPressPlay=false;
                            }


                        } catch (JSONException e) {
                            loadingDialog.cancel();
                            Log.d("UserMovie_Error", e.toString());
                        }
                        binding.progressBar.setVisibility(View.GONE);
                        loadingDialog.cancel();

                    } else {
                        loadingDialog.cancel();
                        Log.d("UserMovie_Error", "");
                        appUtils.showToast("No data found.");
                    }
                }
                binding.progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d("UserMovie_Error", t.toString());
                loadingDialog.cancel();
                binding.progressBar.setVisibility(View.GONE);
                appUtils.showToast("Network error.");
            }
        });
        binding.progressBar.setVisibility(View.GONE);
        loadingDialog.cancel();
    }

    void getTokenValid(String token) {

        loadingDialog.show();
        ApiService apiService = RetrofitClient.getApiService(token);
        Call<JsonObject> call = apiService.getTokenexist();

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                Log.d("tokenexist_Response", response.toString());
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject data = response.body();
                    try {
                        JSONObject  jsonObject = new JSONObject(data.toString());
                        String status = jsonObject.getString("status");
                        if ("error".equals(status)) {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.clear();
                            editor.commit();

                            Intent mainIntent = new Intent(MovieDetailsActivity.this, LoginActivity.class);
                            finishAffinity();
                            startActivity(mainIntent);
                        }

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }


                    loadingDialog.cancel();

                } else {
                    loadingDialog.cancel();
                    Log.d("tokenexist_Error", "");
                    appUtils.showToast("Input Error.");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d("tokenexist_Error", t.toString());
                loadingDialog.cancel();

                appUtils.showToast("Network error.");
            }
        });

    }

    void setUsermovieMylist(String token, String _profileID, String _movieID, MyListRequestModel postValues) {

        loadingDialog.show();
        ApiService apiService = RetrofitClient.getApiService(token);

        String url = "/api/setusermovie/" + _movieID + "?profile_id=" + _profileID;
        Call<JsonObject> call = apiService.setUsermovieMylist(url, postValues);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                Log.d("SetUserMovie_Response", response.toString());
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject data = response.body();
                    Log.d("SetUserMovie_Response", data.toString());
                    if (response.code() == 200) {
                        Log.d("SetUserMovieStatus", "set user movie added");
                    } else {
                        Log.e("SetUserMovieStatus:", response.toString());
                    }

                    loadingDialog.cancel();

                } else {
                    loadingDialog.cancel();
                    Log.d("ResponseUserMovie_Error", "");
                    appUtils.showToast("Input Error.");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d("SetUserMovie_Error", t.toString());
                loadingDialog.cancel();

                appUtils.showToast("Network error.");
            }
        });

    }

    void setUsermovieLikes(String token, String _profileID, String _movieID, LikesModel postValues) {

        loadingDialog.show();
        ApiService apiService = RetrofitClient.getApiService(token);

        String url = "/api/setusermovie/" + _movieID + "?profile_id=" + _profileID;
        Call<JsonObject> call = apiService.setUsermovieLikes(url, postValues);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                Log.d("SetUserMovie_Response", response.toString());
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject data = response.body();
                    Log.d("SetUserMovie_Response", data.toString());
                    if (response.code() == 200) {
                        Log.d("SetUserMovieStatus", "set user movie added");
                    } else {
                        Log.e("SetUserMovieStatus:", response.toString());
                    }

                    loadingDialog.cancel();

                } else {
                    loadingDialog.cancel();
                    Log.d("ResponseUserMovie_Error", "");
                    appUtils.showToast("Input Error.");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d("SetUserMovie_Error", t.toString());
                loadingDialog.cancel();

                appUtils.showToast("Network error.");
            }
        });

    }
}