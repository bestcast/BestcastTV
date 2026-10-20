package com.bestcasttv.bestcasttvapp;

import static com.bestcasttv.bestcasttvapp.search_page.SearchMoviesFragment.showKeyboard;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.bestcasttv.bestcasttvapp.api_services.ApiService;
import com.bestcasttv.bestcasttvapp.api_services.RetrofitClient;
import com.bestcasttv.bestcasttvapp.app_utils.AppUtils;
import com.bestcasttv.bestcasttvapp.app_utils.LoadingDialog;
import com.bestcasttv.bestcasttvapp.login_activity.LoginRequest;
import com.bestcasttv.bestcasttvapp.who_watching.WhoWatchingActivity;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends FragmentActivity {

    EditText countryCode;
    EditText username;
    EditText password;
    TextView errorText;
    Button registerBT;

    String str_username;
    String str_countryCode;
    String str_password;

    SharedPreferences sharedPreferences;
    boolean rememberstatus;
    ProgressDialog progressDialog;

    AppUtils appUtils = new AppUtils(this);
    LoadingDialog loadingDialog;
    URLconfig urLconfig = new URLconfig(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        loadingDialog = new LoadingDialog(this);

        sharedPreferences = getSharedPreferences(getString(R.string.shared_name), Context.MODE_PRIVATE);
        final boolean loginStatus = sharedPreferences.getBoolean(getString(R.string.shared_login_status), false);

        appUtils = new AppUtils(this);
        urLconfig = new URLconfig(this);

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        countryCode = findViewById(R.id.countryCode);
        errorText = findViewById(R.id.errorText);
        registerBT = findViewById(R.id.registerBT);

        progressDialog = new ProgressDialog(RegisterActivity.this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(false);

        countryCode.requestFocus();
        showKeyboard(this, countryCode);

        countryCode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showKeyboard(RegisterActivity.this, v);
                }
            }
        });

        username.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showKeyboard(RegisterActivity.this, v);
                }
            }
        });

        password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showKeyboard(RegisterActivity.this, v);
                }
            }
        });

        //TODO remove default
//        username.setText("support@bestcast.co");
//        password.setText("password");

        registerBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (appUtils.isNetworkAvailable()) {
                    getValidation();
                } else {
                    appUtils.infoAlert("check your Network connection");
                }
            }
        });

        password.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        username.requestFocus();
                        showKeyboard(RegisterActivity.this, username);
                        return true;  // Indicate that the key event has been handled
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        registerBT.requestFocus();
                        return true;  // Indicate that the key event has been handled
                    }
                }
                return false;
            }
        });
    }

    public void getValidation() {
        str_username = username.getText().toString();
        str_password = password.getText().toString();
        str_countryCode = countryCode.getText().toString();

        if (str_countryCode.trim().equals("")) {
            countryCode.setError("Enter a valid country code");
            countryCode.requestFocus();
        } else if (str_username.trim().equals("")) {
            username.setError("Enter a valid username");
            username.requestFocus();
        } else if (str_password.trim().equals("")) {
            password.setError("Enter a valid password");
            password.requestFocus();
        } else {


            if (appUtils.isNetworkAvailable()) {
                registerProcess(str_username, str_password, str_countryCode);
            } else {
                appUtils.infoAlert(getString(R.string.check_network_connection));
            }

        }
    }

    public void registerProcess(
            String userName,
            String password,
            String countryCode
    ) {
        loadingDialog.show();
        errorText.setVisibility(View.GONE);
        ApiService apiService = RetrofitClient.getApiService("");
        LoginRequest loginObject = new LoginRequest(userName, password, "tv", countryCode);

        Call<JsonObject> call = apiService.register(loginObject);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                Log.d("Register_Response", response.toString());
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject data = response.body();

                    Log.d("Register_Response", data.toString());
                    try {
                        JSONObject jsonObject = new JSONObject(data.toString());
                        String status = jsonObject.getString("status");

                        if ("success".equals(status)) {

                            Intent mainIntent = new Intent(RegisterActivity.this, RegisterAlertScreen.class);
                            mainIntent.putExtra("register_email", userName);
                            finish();
                            startActivity(mainIntent);

                        } else {
                            String _message = jsonObject.getString("message");
                            errorText.setVisibility(View.VISIBLE);
                            errorText.setText(_message);
                        }

                    } catch (JSONException e) {
                        loadingDialog.cancel();
                        Log.d("json Register_Error", e.toString());
                    }

                    loadingDialog.cancel();

                } else {
                    loadingDialog.cancel();
                    Log.d("json Register_Error", "");
                    appUtils.showToast("Input Error.");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d("Register_Error", t.toString());
                loadingDialog.cancel();

                appUtils.showToast("Network error.");
            }
        });

    }
}