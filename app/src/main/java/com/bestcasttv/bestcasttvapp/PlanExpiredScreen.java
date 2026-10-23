package com.bestcasttv.bestcasttvapp;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bestcasttv.bestcasttvapp.api_services.ApiService;
import com.bestcasttv.bestcasttvapp.api_services.RetrofitClient;
import com.bestcasttv.bestcasttvapp.app_utils.AppUtils;
import com.bestcasttv.bestcasttvapp.app_utils.LoadingDialog;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlanExpiredScreen extends FragmentActivity {

    LinearLayout planExpiredLayout;
    LinearLayout deviceExiceededLayout;
    TextView emailDescription;
    Button signOutBT;
    String planStatus = "";
    String _token = "";

    AppUtils appUtils;
    LoadingDialog loadingDialog;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_expired_screen);

        appUtils = new AppUtils(this);
        loadingDialog = new LoadingDialog(this);

        planExpiredLayout = findViewById(R.id.planExpiredLayout);
        deviceExiceededLayout = findViewById(R.id.deviceExiceededLayout);
        emailDescription = findViewById(R.id.emailDescription);
        signOutBT = findViewById(R.id.signOutBT);

        sharedPreferences = getSharedPreferences(getString(R.string.shared_name), Context.MODE_PRIVATE);
        _token = sharedPreferences.getString(getString(R.string.token), "");

        planStatus = getIntent().getStringExtra("planStatus");
        if (planStatus == null) planStatus = "1";

        if ("1".equals(planStatus)) {
            planExpiredLayout.setVisibility(View.VISIBLE);
            deviceExiceededLayout.setVisibility(View.GONE);
        } else {
            String emailID = sharedPreferences.getString(getString(R.string.email), "");
            String _emailtext = "Not " + emailID + "? Create an account to enjoy your own Bestcast today.";

            emailDescription.setText(_emailtext);
            deviceExiceededLayout.setVisibility(View.VISIBLE);
            planExpiredLayout.setVisibility(View.GONE);
        }

        if (signOutBT != null) {
            signOutBT.setFocusable(true);
            signOutBT.setFocusableInTouchMode(true);
            signOutBT.requestFocus();

            signOutBT.setOnClickListener(v -> performSignOut());

            signOutBT.setOnKeyListener((v, keyCode, event) -> {
                if ((keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) && event.getAction() == KeyEvent.ACTION_DOWN) {
                    performSignOut();
                    return true;
                }
                return false;
            });
        }
    }

    private void performSignOut() {
        if (appUtils != null && appUtils.isNetworkAvailable()) {
            getLogOut(_token);
        } else {
            clearSessionAndNavigateToLogin();
        }
    }

    private void clearSessionAndNavigateToLogin() {
        if (loadingDialog != null) loadingDialog.cancel();
        if (sharedPreferences != null) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(getString(R.string.shared_login_status), false);
            editor.clear();
            editor.commit();
        }
        Intent mainIntent = new Intent(PlanExpiredScreen.this, LoginActivity.class);
        finishAffinity();
        startActivity(mainIntent);
    }

    private void getLogOut(String token) {
        if (loadingDialog != null) loadingDialog.show();
        ApiService apiService = RetrofitClient.getApiService(token);

        Call<JsonObject> call = apiService.logout();
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (loadingDialog != null) loadingDialog.cancel();

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().toString());
                        String status = jsonObject.optString("status");

                        if ("success".equals(status)) {
                            if (appUtils != null) appUtils.showToast("Logout Successful.");
                        }
                    } catch (JSONException e) {
                        Log.e("Logout_Error", "JSON parsing error", e);
                    }
                }
                clearSessionAndNavigateToLogin();
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                Log.e("Logout_Error", "Network failure", t);
                clearSessionAndNavigateToLogin();
            }
        });
    }
}
