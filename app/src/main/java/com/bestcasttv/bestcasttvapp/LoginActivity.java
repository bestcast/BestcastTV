package com.bestcasttv.bestcasttvapp;

import static com.bestcasttv.bestcasttvapp.search_page.SearchMoviesFragment.showKeyboard;

import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bestcasttv.bestcasttvapp.api_services.ApiService;
import com.bestcasttv.bestcasttvapp.api_services.RetrofitClient;
import com.bestcasttv.bestcasttvapp.app_utils.AppUtils;
import com.bestcasttv.bestcasttvapp.app_utils.LoadingDialog;
import com.bestcasttv.bestcasttvapp.login_activity.LoginQRcodeRequest;
import com.bestcasttv.bestcasttvapp.login_activity.LoginRequest;
import com.bestcasttv.bestcasttvapp.otp.OTPActivity;
import com.bestcasttv.bestcasttvapp.otp.OTPRequest;
import com.bestcasttv.bestcasttvapp.playbackmodels.WatchingModel;
import com.bestcasttv.bestcasttvapp.who_watching.WhoWatchingActivity;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class LoginActivity extends FragmentActivity {

    ImageView qrImageView;
    EditText countryCode;
    EditText username;
    EditText password;
    EditText mobileNumber;
    TextView errorText;
    Button loginBT;
    Button registerBT;
    Button submitBT;

    String str_username;
    String str_mobileNumber;
    String str_countryCode;
    String str_password;

    SharedPreferences sharedPreferences;
    boolean rememberstatus;
    ProgressDialog progressDialog;

    AppUtils appUtils = new AppUtils(this);
    LoadingDialog loadingDialog;
    URLconfig urLconfig = new URLconfig(this);
    Timer timer = new Timer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loadingDialog = new LoadingDialog(this);

        sharedPreferences = getSharedPreferences(getString(R.string.shared_name), Context.MODE_PRIVATE);
        final boolean loginStatus = sharedPreferences.getBoolean(getString(R.string.shared_login_status), false);

        appUtils = new AppUtils(this);
        urLconfig = new URLconfig(this);

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        countryCode = findViewById(R.id.countryCode);
        mobileNumber = findViewById(R.id.mobileNumber);
        qrImageView = findViewById(R.id.qrImageView);
        errorText = findViewById(R.id.errorText);
        loginBT = findViewById(R.id.loginBT);
        registerBT = findViewById(R.id.registerBT);
        submitBT = findViewById(R.id.submitBT);

        countryCode.requestFocus();
        showKeyboard(this, countryCode);

        countryCode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showKeyboard(LoginActivity.this, v);
                }
            }
        });

        mobileNumber.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showKeyboard(LoginActivity.this, v);
                }
            }
        });


        if (appUtils.isNetworkAvailable()) {
            getqrcode();
            loginwithQRcode("+91"); // Default country code for polling
        } else {
            appUtils.infoAlert("check your Network connection");
        }


//        password.setOnKeyListener(new View.OnKeyListener() {
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                if (event.getAction() == KeyEvent.ACTION_UP) {
//                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
//                        // Handle focus change to the EditText when UP key is pressed
//                        username.requestFocus();
//                        return true;
//                    }
//                }
//                return false;
//            }
//        });


        findViewById(R.id.view_container).setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        if (countryCode != null) {
                            countryCode.requestFocus();
                            showKeyboard(LoginActivity.this, countryCode);
                            return true;  // Indicate that the key event has been handled
                        }
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        submitBT.requestFocus();
//                        if (password != null) {
//                            password.requestFocus();
//                            showKeyboard(LoginActivity.this, password);
//                            return true;  // Indicate that the key event has been handled
//                        }
                    }
                }
                return false;
            }
        });

        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(false);

        //TODO remove default
//        username.setText("support@bestcast.co");
//        password.setText("password");

        loginBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (appUtils.isNetworkAvailable()) {
                    getLoginValidation();
                } else {
                    appUtils.infoAlert("check your Network connection");
                }
            }
        });

        submitBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (appUtils.isNetworkAvailable()) {
                    getMobileNumberValidation();
                } else {
                    appUtils.infoAlert("check your Network connection");
                }
            }
        });


        registerBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (appUtils.isNetworkAvailable()) {
                    Intent mainIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                    startActivity(mainIntent);
                } else {
                    appUtils.infoAlert("check your Network connection");
                }
            }
        });

    }


    public void showKeyboard1(View view) {
        view.requestFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public void getLoginValidation() {
        str_username = username.getText().toString();
        str_password = password.getText().toString();

        if (str_username.trim().equals("")) {
            username.setError("Enter a valid username");
            username.requestFocus();
            showKeyboard(LoginActivity.this, username);
        } else if (str_password.trim().equals("")) {
            password.requestFocus();
            showKeyboard(LoginActivity.this, password);
            password.setError("Enter a valid password");
        } else {


            if (appUtils.isNetworkAvailable()) {
                loginProcess(str_username, str_password);
            } else {
                appUtils.infoAlert(getString(R.string.check_network_connection));
            }

        }


    }

    public void getMobileNumberValidation() {

        str_mobileNumber = mobileNumber.getText().toString();
        str_countryCode = countryCode.getText().toString();

        if (str_countryCode.trim().equals("")) {
            countryCode.setError("Enter a valid country code");
            countryCode.requestFocus();
            showKeyboard(LoginActivity.this, countryCode);
        } else if (str_mobileNumber.trim().equals("")) {
            mobileNumber.setError("Enter a valid mobile number");
            mobileNumber.requestFocus();
            showKeyboard(LoginActivity.this, mobileNumber);
        } else {

            if (appUtils.isNetworkAvailable()) {
                sendOTP(str_mobileNumber, str_countryCode);
            } else {
                appUtils.infoAlert(getString(R.string.check_network_connection));
            }

        }


    }

    public void loginwithQRcode(String _countryCode) {

        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                String _qrcode = sharedPreferences.getString(getString(R.string.qrcode), "");
                loginWithQRcode(_qrcode, _countryCode);

            }

        }, 0, 3000);

    }


    public void loginProcess(
            String userName,
            String password
    ) {
        loadingDialog.show();
        errorText.setVisibility(View.GONE);
        ApiService apiService = RetrofitClient.getApiService("");
        String currentCountryCode = countryCode.getText().toString();
        LoginRequest loginObject = new LoginRequest(userName, password, "tv", currentCountryCode);
        Gson gson = new Gson();
        String json = gson.toJson(loginObject);
        Log.d("String_sample", json);
        Call<JsonObject> call = apiService.login(loginObject);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                Log.d("Login_Response", response.toString());
                if (response.isSuccessful() && response.body() != null) {
//                    String data = response.body();
                    JsonObject data = response.body();

                    Log.d("Login_Response", data.toString());
                    try {
                        JSONObject jsonObject = new JSONObject(data.toString());
                        String status = jsonObject.getString("status");

                        if ("success".equals(status)) {
//                            appUtils.showToast("Login Successful.");
                            JSONObject loginDataJSONObject1 = jsonObject.getJSONObject("results");
                            JSONObject loginDataJSONObject = loginDataJSONObject1.getJSONObject("user");

                            String _id = loginDataJSONObject.getString("id");
                            String _email = loginDataJSONObject.getString("email");
                            String _phone = loginDataJSONObject.getString("phone");
                            String _name = loginDataJSONObject.getString("name");
                            String _firstname = loginDataJSONObject.getString("firstname");
                            String _lastname = loginDataJSONObject.getString("lastname");
                            String _dob = loginDataJSONObject.getString("dob");
                            String _gender = loginDataJSONObject.getString("gender");
                            String _plan = loginDataJSONObject.getString("plan");
                            String _plan_expiry = loginDataJSONObject.getString("plan_expiry");
                            String _photo = loginDataJSONObject.getString("photo");
                            String _otp = loginDataJSONObject.getString("otp");
                            String _tvcode = loginDataJSONObject.getString("tvcode");
                            String _referal_code = loginDataJSONObject.getString("referal_code");
                            String _credits_used = loginDataJSONObject.getString("credits_used");
                            String _refferer = loginDataJSONObject.getString("refferer");
                            String _plan_status = loginDataJSONObject.getString("plan_status");
                            String _plan_device_status = loginDataJSONObject.getString("plan_device_status");
                            String _token = loginDataJSONObject1.getString("token");


                            SharedPreferences.Editor editor = sharedPreferences.edit();

                            editor.putString(getString(R.string.id), _id);
                            editor.putString(getString(R.string.email), _email);
                            editor.putString(getString(R.string.phone), _phone);
                            editor.putString(getString(R.string.name), _name);
                            editor.putString(getString(R.string.firstname), _firstname);
                            editor.putString(getString(R.string.lastname), _lastname);
                            editor.putString(getString(R.string.dob), _dob);
                            editor.putString(getString(R.string.gender), _gender);
                            editor.putString(getString(R.string.plan), _plan);
                            editor.putString(getString(R.string.plan_expiry), _plan_expiry);
                            editor.putString(getString(R.string.photo), _photo);
                            editor.putString(getString(R.string.otp), _otp);
                            editor.putString(getString(R.string.tvcode), _tvcode);
                            editor.putString(getString(R.string.referal_code), _referal_code);
                            editor.putString(getString(R.string.credits_used), _credits_used);
                            editor.putString(getString(R.string.refferer), _refferer);
                            editor.putString(getString(R.string.token), _token);

                            if (_plan_status.equals("0")) {
                                Intent mainIntent = new Intent(LoginActivity.this, PlanExpiredScreen.class);
                                mainIntent.putExtra("planStatus", "1");
                                finish();
                                startActivity(mainIntent);
                            } else if (_plan_device_status.equals("0")) {
                                Intent mainIntent = new Intent(LoginActivity.this, PlanExpiredScreen.class);
                                mainIntent.putExtra("planStatus", "2");
                                finish();
                                startActivity(mainIntent);
                            } else {

                                editor.putBoolean(getString(R.string.shared_login_status), true);
                                editor.commit();

                                Intent mainIntent = new Intent(LoginActivity.this, WhoWatchingActivity.class);
                                finish();
                                startActivity(mainIntent);
                            }


                            editor.commit();


                        } else {
                            String _message = jsonObject.getString("message");
                            errorText.setVisibility(View.VISIBLE);
                            errorText.setText(_message);
//                            appUtils.showToast("Invalid Credentials");
                        }

                    } catch (JSONException e) {
                        loadingDialog.cancel();
                        Log.d("json Login_Error", e.toString());
                    }

                    loadingDialog.cancel();

                } else {
                    loadingDialog.cancel();
                    Log.d("json Login_Error", "");
                    appUtils.showToast("Input Error.");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d("Login_Error", t.toString());
                loadingDialog.cancel();

                appUtils.showToast("Network error.");
            }
        });

    }


    public void getqrcode() {
        loadingDialog.show();
        ApiService apiService = RetrofitClient.getApiService("");
        Call<JsonObject> call = apiService.generateqrcode();
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                Log.d("generateqrcode_Response", response.toString());
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject data = response.body();

                    try {
                        JSONObject jsonObject = new JSONObject(data.toString());
                        String status = jsonObject.getString("status");

                        if ("success".equals(status)) {
                            JSONObject loginDataJSONObject = jsonObject.getJSONObject("results");
                            String _qrcode = loginDataJSONObject.getString("qrcode");

                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(getString(R.string.qrcode), _qrcode);
                            editor.commit();


                            try {
                                qrImageView.setImageBitmap(encodeAsBitmap(_qrcode));
                            } catch (WriterException e) {
                                throw new RuntimeException(e);
                            }


                            showKeyboard1(mobileNumber);
                        } else {
                            String _message = jsonObject.getString("message");
                        }

                    } catch (JSONException e) {
                        loadingDialog.cancel();
                        Log.d("json QR_Error", e.toString());
                    }

                    loadingDialog.cancel();

                } else {
                    loadingDialog.cancel();
                    Log.d("json QR_Error", "");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d("QR_Error", t.toString());
                loadingDialog.cancel();
            }
        });

    }
    public void loginWithQRcode(String _qrcode, String _countryCode) {
        errorText.setVisibility(View.GONE);
        ApiService apiService = RetrofitClient.getApiService("");
        LoginQRcodeRequest loginObject = new LoginQRcodeRequest(_qrcode, _countryCode);
        Call<JsonObject> call = apiService.loginbyqrcode(loginObject);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                Log.d("Login_Response", response.toString());
                if (response.isSuccessful() && response.body() != null) {
//                    String data = response.body();
                    JsonObject data = response.body();

                    Log.d("Login_Response", data.toString());
                    try {
                        JSONObject jsonObject = new JSONObject(data.toString());
                        String status = jsonObject.getString("status");

                        if ("success".equals(status)) {
//                            appUtils.showToast("Login Successful.");
                            JSONObject loginDataJSONObject1 = jsonObject.getJSONObject("results");
                            JSONObject loginDataJSONObject = loginDataJSONObject1.getJSONObject("user");

                            String _id = loginDataJSONObject.getString("id");
                            String _email = loginDataJSONObject.getString("email");
                            String _phone = loginDataJSONObject.getString("phone");
                            String _name = loginDataJSONObject.getString("name");
                            String _firstname = loginDataJSONObject.getString("firstname");
                            String _lastname = loginDataJSONObject.getString("lastname");
                            String _dob = loginDataJSONObject.getString("dob");
                            String _gender = loginDataJSONObject.getString("gender");
                            String _plan = loginDataJSONObject.getString("plan");
                            String _plan_expiry = loginDataJSONObject.getString("plan_expiry");
                            String _photo = loginDataJSONObject.getString("photo");
                            String _otp = loginDataJSONObject.getString("otp");
                            String _tvcode = loginDataJSONObject.getString("tvcode");
                            String _referal_code = loginDataJSONObject.getString("referal_code");
                            String _credits_used = loginDataJSONObject.getString("credits_used");
                            String _refferer = loginDataJSONObject.getString("refferer");
                            String _plan_status = loginDataJSONObject.getString("plan_status");
                            String _plan_device_status = loginDataJSONObject.getString("plan_device_status");
                            String _token = loginDataJSONObject1.getString("token");


                            SharedPreferences.Editor editor = sharedPreferences.edit();

                            editor.putString(getString(R.string.id), _id);
                            editor.putString(getString(R.string.email), _email);
                            editor.putString(getString(R.string.phone), _phone);
                            editor.putString(getString(R.string.name), _name);
                            editor.putString(getString(R.string.firstname), _firstname);
                            editor.putString(getString(R.string.lastname), _lastname);
                            editor.putString(getString(R.string.dob), _dob);
                            editor.putString(getString(R.string.gender), _gender);
                            editor.putString(getString(R.string.plan), _plan);
                            editor.putString(getString(R.string.plan_expiry), _plan_expiry);
                            editor.putString(getString(R.string.photo), _photo);
                            editor.putString(getString(R.string.otp), _otp);
                            editor.putString(getString(R.string.tvcode), _tvcode);
                            editor.putString(getString(R.string.referal_code), _referal_code);
                            editor.putString(getString(R.string.credits_used), _credits_used);
                            editor.putString(getString(R.string.refferer), _refferer);
                            editor.putString(getString(R.string.token), _token);

                            if (_plan_status.equals("0")) {
                                Intent mainIntent = new Intent(LoginActivity.this, PlanExpiredScreen.class);
                                mainIntent.putExtra("planStatus", "1");
                                finish();
                                startActivity(mainIntent);
                            } else if (_plan_device_status.equals("0")) {
                                Intent mainIntent = new Intent(LoginActivity.this, PlanExpiredScreen.class);
                                mainIntent.putExtra("planStatus", "2");
                                finish();
                                startActivity(mainIntent);
                            } else {

                                editor.putBoolean(getString(R.string.shared_login_status), true);
                                editor.commit();

                                Intent mainIntent = new Intent(LoginActivity.this, WhoWatchingActivity.class);
                                finish();
                                startActivity(mainIntent);
                            }


                            editor.commit();


                        } else {
                            String _message = jsonObject.getString("message");
//                            errorText.setVisibility(View.VISIBLE);
//                            errorText.setText(_message);
//                            appUtils.showToast("Invalid Credentials");
                        }

                    } catch (JSONException e) {
                        Log.d("json Login_Error", e.toString());
                    }

                } else {
                    Log.d("json Login_Error", "");
//                    appUtils.showToast("Input Error.");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d("Login_Error", t.toString());
                loadingDialog.cancel();

            }
        });

    }
    public void sendOTP(String _mobileNumber, String _countryCode) {
        loadingDialog.show();
        errorText.setVisibility(View.GONE);
        ApiService apiService = RetrofitClient.getApiService("");
        OTPRequest otpRequest = new OTPRequest(_mobileNumber, _countryCode);
        Call<JsonObject> call = apiService.sendOTP(otpRequest);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                Log.d("OTP_Response", response.toString());
                if (response.isSuccessful() && response.body() != null) {
//                    String data = response.body();
                    JsonObject data = response.body();

                    Log.d("OTP_Response", data.toString());
                    try {
                        JSONObject jsonObject = new JSONObject(data.toString());
                        String status = jsonObject.getString("status");
                        if (status.equals("success")){
                            String _message = jsonObject.getString("message");
                            appUtils.showToast(_message);
                            Intent mainIntent = new Intent(LoginActivity.this, OTPActivity.class);
                            mainIntent.putExtra("mobileNumber",_mobileNumber);
                            mainIntent.putExtra("countryCode",_countryCode);
                            startActivity(mainIntent);
                        }else {
                            String _message = jsonObject.getString("message");
                            appUtils.showToast(_message);
                        }

                    } catch (JSONException e) {
                        Log.d("json OTP_Error", e.toString());
                    }

                } else {
                    Log.d("json OTP_Error", "");
//                    appUtils.showToast("Input Error.");
                }
                loadingDialog.cancel();
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d("OTP_Error", t.toString());
                loadingDialog.cancel();

            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        timer.cancel();
    }

    Bitmap encodeAsBitmap(String str) throws WriterException {
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix bitMatrix = writer.encode(str, BarcodeFormat.QR_CODE, 400, 400);

        int w = bitMatrix.getWidth();
        int h = bitMatrix.getHeight();
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                pixels[y * w + x] = bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
        return bitmap;
    }
}