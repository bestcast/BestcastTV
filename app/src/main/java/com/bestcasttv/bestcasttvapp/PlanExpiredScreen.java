package com.bestcasttv.bestcasttvapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bestcasttv.bestcasttvapp.api_services.ApiService;
import com.bestcasttv.bestcasttvapp.api_services.RetrofitClient;
import com.bestcasttv.bestcasttvapp.app_utils.AppUtils;
import com.bestcasttv.bestcasttvapp.app_utils.LoadingDialog;
import com.bestcasttv.bestcasttvapp.move_details_page.MovieDetailsActivity;
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
    String planStatus="";
    String _token = "";

    AppUtils appUtils = new AppUtils(this);
    LoadingDialog loadingDialog;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_expired_screen);

        loadingDialog = new LoadingDialog(this);

        planExpiredLayout = findViewById(R.id.planExpiredLayout);
        deviceExiceededLayout = findViewById(R.id.deviceExiceededLayout);
        emailDescription = findViewById(R.id.emailDescription);
        signOutBT = findViewById(R.id.signOutBT);

        sharedPreferences = getSharedPreferences(getString(R.string.shared_name), Context.MODE_PRIVATE);
        final boolean loginStatus = sharedPreferences.getBoolean(getString(R.string.shared_login_status), false);
        _token = sharedPreferences.getString(getString(R.string.token), "");

        planStatus=getIntent().getStringExtra("planStatus");



        //1-plan status 2-expired status
        if (planStatus.equals("1")){
            planExpiredLayout.setVisibility(View.VISIBLE);
            deviceExiceededLayout.setVisibility(View.GONE);
        }else {
            String emailID = sharedPreferences.getString(getString(R.string.email), "");

            String _emailtext = "Not " + emailID + "? Create an account to enjoy your own Bestcast today.";

            emailDescription.setText(_emailtext);
            deviceExiceededLayout.setVisibility(View.VISIBLE);
            planExpiredLayout.setVisibility(View.GONE);
        }

        signOutBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (appUtils.isNetworkAvailable()) {
                    getLogOut(_token);
                } else {
                    appUtils.infoAlert(getString(R.string.check_network_connection));
                }

            }
        });


    }

    private void getLogOut(String token) {
        loadingDialog.show();
        ApiService apiService = RetrofitClient.getApiService(token);

        Call<JsonObject> call = apiService.logout();
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                Log.d("Logout_Response", response.toString());
                if (response.isSuccessful() && response.body() != null) {
//                    String data = response.body();
                    JsonObject data = response.body();

                    Log.d("Logout_Response", data.toString());
                    try {
                        JSONObject jsonObject = new JSONObject(data.toString());
                        String status = jsonObject.getString("status");

                        if ("success".equals(status)) {
                            appUtils.showToast("Logout Successful.");

                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean(getString(R.string.shared_login_status), false);
                            editor.clear();
                            editor.commit();

                            loadingDialog.cancel();

                            Intent mainIntent = new Intent(PlanExpiredScreen.this, LoginActivity.class);
                            finish();
                            startActivity(mainIntent);

                        } else {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.clear();
                            editor.commit();

                            Intent mainIntent = new Intent(PlanExpiredScreen.this, LoginActivity.class);
                            finishAffinity();
                            startActivity(mainIntent);
//                            appUtils.showToast("Logout failed.");
                        }

                    } catch (JSONException e) {
                        loadingDialog.cancel();
                        Log.d("json Logout_Error", e.toString());
                    }

                    loadingDialog.cancel();

                } else {
                    loadingDialog.cancel();
                    Log.d("json Logout_Error", "");
                    appUtils.showToast("Input Error.");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d("Logout_Error", t.toString());
                loadingDialog.cancel();

                appUtils.showToast("Network error.");
            }
        });

    }
}