package com.bestcasttv.bestcasttvapp.who_watching;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.bestcasttv.bestcasttvapp.LoginActivity;
import com.bestcasttv.bestcasttvapp.PlanExpiredScreen;
import com.bestcasttv.bestcasttvapp.R;
import com.bestcasttv.bestcasttvapp.api_services.ApiService;
import com.bestcasttv.bestcasttvapp.api_services.RetrofitClient;
import com.bestcasttv.bestcasttvapp.app_utils.AppUtils;
import com.bestcasttv.bestcasttvapp.app_utils.LoadingDialog;
import com.bestcasttv.bestcasttvapp.main_page.MainPageActivity;
import com.bestcasttv.bestcasttvapp.main_page.ui.HomeFragment;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WhoWatchingActivity extends FragmentActivity {

    String planStatus = "";
    String _token = "";

    AppUtils appUtils = new AppUtils(this);
    LoadingDialog loadingDialog;
    SharedPreferences sharedPreferences;

    RecyclerView whowatchingRecylclerview;
    LinearLayout whoWhatchListLayout;
    ArrayList<WhoWatchingItem> whoWatchingItemArrayList;
    WhoWatchingItem whoWatchingItem;
    WhoWatchingAdapter whoWatchingAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_who_watchin);

        loadingDialog = new LoadingDialog(this);


        sharedPreferences = getSharedPreferences(getString(R.string.shared_name), Context.MODE_PRIVATE);
        final boolean loginStatus = sharedPreferences.getBoolean(getString(R.string.shared_login_status), false);
        _token = sharedPreferences.getString(getString(R.string.token), "");


        planStatus = getIntent().getStringExtra("planStatus");

        whowatchingRecylclerview = findViewById(R.id.whowatchingRecylclerview);
        whoWhatchListLayout = findViewById(R.id.whoWhatchListLayout);
        whoWatchingItemArrayList = new ArrayList<>();

        if (appUtils.isNetworkAvailable()) {
            Log.d("CheckToken", _token);
            getUserProfileList(_token);
        } else {
            appUtils.infoAlert(getString(R.string.check_network_connection));
        }
        whowatchingRecylclerview.setLayoutManager(new LinearLayoutManager(this));
        whowatchingRecylclerview.setHasFixedSize(true);


    }


    public void getUserProfileList(String token) {

        loadingDialog.show();
        whoWatchingItemArrayList.clear();
        ApiService apiService = RetrofitClient.getApiService(token);

        Call<JsonObject> call = apiService.getUserList();
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.d("WW_Response", response.toString());
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject data = response.body();

                    Log.d("whoWatching_Response", data.toString());
                    try {
                        JSONObject jsonObject = new JSONObject(data.toString());
                        JSONArray jsonArray = jsonObject.getJSONArray("data");

                        for (int i = 0; i < jsonArray.length(); i++) {


                            JSONObject responseData = jsonArray.getJSONObject(i);
                            JSONObject jsonObjectProfileIcon = responseData.getJSONObject("profileicon");

                            String profilePicUrl = RetrofitClient.BASE_URL + "/" + jsonObjectProfileIcon.getString("thumbnail").toString();

                            whoWatchingItem = new WhoWatchingItem(
                                    responseData.get("id").toString(),
                                    responseData.get("name").toString(),
                                    jsonObjectProfileIcon.getString("id").toString(),
                                    jsonObjectProfileIcon.getString("title").toString(),
                                    profilePicUrl,
                                    responseData.get("last_login").toString(),
                                    (Integer) responseData.get("language"),
                                    (Integer) responseData.get("is_child"),
                                    (Integer) responseData.get("is_have_pin"),
                                    false,
                                    false
                            );

                            whoWatchingItemArrayList.add(whoWatchingItem);
                        }

                        // Skip the selection part and directly select the first profile
                        if (!whoWatchingItemArrayList.isEmpty()) {
                            getUserDetails(token, whoWatchingItemArrayList.get(0));
                        } else {
                            whoWatchingAdapter = new WhoWatchingAdapter(whoWatchingItemArrayList, WhoWatchingActivity.this);
                            whowatchingRecylclerview.setAdapter(whoWatchingAdapter);

                            whoWatchingAdapter.setOnItemClickListener(new WhoWatchingAdapter.OnRecyclerViewItemClickListener() {
                                @Override
                                public void onListItemClicked(int position, View v) {
                                    whoWatchingItem = whoWatchingAdapter.filterlist.get(position);

                                    getUserDetails(token, whoWatchingItem);
                                }
                            });

                            whoWhatchListLayout.setVisibility(View.VISIBLE);
                        }

                    } catch (JSONException e) {
                        loadingDialog.cancel();
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.clear();
                        editor.commit();

                        Intent mainIntent = new Intent(WhoWatchingActivity.this, LoginActivity.class);
                        finishAffinity();
                        startActivity(mainIntent);
                        Log.d("Jsonwhowatching_Error", e.toString());
                    }

                    loadingDialog.cancel();

                } else {
                    loadingDialog.cancel();
                    Log.d("whowatching_Error", "");
                    appUtils.showToast("No data found.");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d("whowatching_Error", t.toString());
                loadingDialog.cancel();

                appUtils.showToast("Network error.");
            }
        });

        loadingDialog.cancel();

    }

    void getUserDetails(String token, WhoWatchingItem whoWatchingModel) {

        loadingDialog.show();
        ApiService apiService = RetrofitClient.getApiService(token);
        Call<JsonObject> call = apiService.getUser();
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                Log.d("getUser_Response", response.toString());
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject data = response.body();

                    Log.d("User_Response", data.toString());

                    try {
                        JSONObject jsonObject = new JSONObject(data.toString());
                        JSONObject jsonObject1 = jsonObject.getJSONObject("results");
                        JSONObject jsonObject2 = jsonObject1.getJSONObject("user");

                        String _plan_expiry = jsonObject2.getString("plan_expiry").toString();
                        String _plan_status = jsonObject2.getString("plan_status").toString();
                        String _plan_device_status = jsonObject2.getString("plan_device_status").toString();

                        if (_plan_status.equals("0")) {
                            Intent mainIntent = new Intent(WhoWatchingActivity.this, PlanExpiredScreen.class);
                            mainIntent.putExtra("planStatus", "1");
                            finish();
                            startActivity(mainIntent);
                        } else if (_plan_device_status.equals("0")) {
                            Intent mainIntent = new Intent(WhoWatchingActivity.this, PlanExpiredScreen.class);
                            mainIntent.putExtra("planStatus", "2");
                            finish();
                            startActivity(mainIntent);
                        } else {

                            SharedPreferences.Editor editor = sharedPreferences.edit();

                            editor.putString(getString(R.string.profileID), whoWatchingModel.profileID);
                            editor.putString(getString(R.string.profileName), whoWatchingModel.profileName);
                            editor.putString(getString(R.string.profilePictureID), whoWatchingModel.profilePictureID);
                            editor.putString(getString(R.string.profilePictureTitle), whoWatchingModel.profilePictureTitle);
                            editor.putString(getString(R.string.profilePicture), whoWatchingModel.profilePicture);
                            editor.putString(getString(R.string.isChild), String.valueOf(whoWatchingModel.isChild));
                            editor.commit();

                            HomeFragment.clearCache();

                            Intent mainIntent = new Intent(WhoWatchingActivity.this, MainPageActivity.class);
                            finishAffinity();
                            startActivity(mainIntent);

                            loadingDialog.cancel();
                        }

                    } catch (JSONException e) {
                        loadingDialog.cancel();
                        Log.d("JsonUser_Error", e.toString());
                    }

                    loadingDialog.cancel();

                } else {
                    loadingDialog.cancel();
                    Log.d("getUser_Error", "");
                    appUtils.showToast("No data found.");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d("getUser_Error", t.toString());
                loadingDialog.cancel();

                appUtils.showToast("Network error.");
            }
        });

        loadingDialog.cancel();
    }

}