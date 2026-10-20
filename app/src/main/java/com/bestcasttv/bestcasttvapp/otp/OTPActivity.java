package com.bestcasttv.bestcasttvapp.otp;

import static com.bestcasttv.bestcasttvapp.search_page.SearchMoviesFragment.showKeyboard;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.bestcasttv.bestcasttvapp.LoginActivity;
import com.bestcasttv.bestcasttvapp.PlanExpiredScreen;
import com.bestcasttv.bestcasttvapp.R;
import com.bestcasttv.bestcasttvapp.URLconfig;
import com.bestcasttv.bestcasttvapp.api_services.ApiService;
import com.bestcasttv.bestcasttvapp.api_services.RetrofitClient;
import com.bestcasttv.bestcasttvapp.app_utils.AppUtils;
import com.bestcasttv.bestcasttvapp.app_utils.LoadingDialog;
import com.bestcasttv.bestcasttvapp.login_activity.LoginRequest;
import com.bestcasttv.bestcasttvapp.who_watching.WhoWatchingActivity;
import com.chaos.view.PinView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OTPActivity extends FragmentActivity {

    EditText username;
    EditText password;
    TextView errorText;
    Button loginBT;
    Button registerBT;

    String str_username;
    String str_password;

    SharedPreferences sharedPreferences;
    boolean rememberstatus;
    ProgressDialog progressDialog;

    AppUtils appUtils = new AppUtils(this);
    LoadingDialog loadingDialog;
    URLconfig urLconfig = new URLconfig(this);

    private static CountDownTimer countDownTimer;
    PinView pinView;
    Button submitBT;
    Button resendButton;
    TextView resendcodeTextview;
    TextView otpTextContentView;
    TextView resendOTPTextViewTimer;
    TextView resendOTPContentTextView;
    TextView secondsContentTextView;


    String mobileNumber = "";
    String countryCode = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otpactivity);

        loadingDialog = new LoadingDialog(this);
        sharedPreferences = getSharedPreferences(getString(R.string.shared_name), Context.MODE_PRIVATE);


        submitBT = findViewById(R.id.submitBT);
        resendButton = findViewById(R.id.resendButton);
        errorText = findViewById(R.id.errorText);
        pinView = findViewById(R.id.pinview);
        resendOTPContentTextView = findViewById(R.id.resendOTPContentTextView);
        secondsContentTextView = findViewById(R.id.secondsContentTextView);
        resendOTPTextViewTimer = findViewById(R.id.resendOTPTextViewTimer);
        otpTextContentView = findViewById(R.id.otpTextContentView);

        pinView.requestFocus();
        showKeyboard(OTPActivity.this, pinView);

        pinView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showKeyboard(OTPActivity.this, v);
                }
            }
        });

        mobileNumber = getIntent().getStringExtra("mobileNumber");
        countryCode = getIntent().getStringExtra("countryCode");
        otpTextContentView.setText("Enter the 4-digit PIN or OTP code sent to " + countryCode + mobileNumber);


        //Start Timer
        startTimer(30000);

//        otp_view.setOTP(tempOTP);
        resendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTimer(30000);
                if (appUtils.isNetworkAvailable()) {
                    sendOTP(mobileNumber, countryCode);
                }
            }
        });


        submitBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // getting the PinView data
                String otp = pinView.getText().toString();
                loginWithOTP(mobileNumber, otp, countryCode);

            }
        });
    }

    private void startTimer(long noOfMinutes) {
        countDownTimer = new CountDownTimer(noOfMinutes, 1000) {
            public void onTick(long millisUntilFinished) {
                long millis = millisUntilFinished;
                String hms1 = String.format("%01d",
                        TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
                resendOTPTextViewTimer.setText(hms1);

                resendOTPContentTextView.setText("OTP can resend in");
                resendButton.setVisibility(View.GONE);
                secondsContentTextView.setVisibility(View.VISIBLE);
                resendOTPTextViewTimer.setVisibility(View.VISIBLE);
            }

            public void onFinish() {
                countDownTimer = null;//set CountDownTimer to null

                resendOTPContentTextView.setText("Didn't get the OTP? ");
                resendButton.setVisibility(View.VISIBLE);
                secondsContentTextView.setVisibility(View.GONE);
                resendOTPTextViewTimer.setVisibility(View.GONE);

            }
        }.start();
    }

    public void sendOTP(String _mobileNumber, String _countryCode) {
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
                        if (status.equals("")) {
                            String _message = jsonObject.getString("message");
                            appUtils.showToast(_message);
                        } else {
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

    public void loginWithOTP(
            String email,
            String otp,
            String countryCode
    ) {
        loadingDialog.show();
        errorText.setVisibility(View.GONE);
        ApiService apiService = RetrofitClient.getApiService("");
        OTPLoginRequestModel loginObject = new OTPLoginRequestModel(email, otp, "tv", countryCode);
        Call<JsonObject> call = apiService.loginWithOtp(loginObject);
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
                                Intent mainIntent = new Intent(OTPActivity.this, PlanExpiredScreen.class);
                                mainIntent.putExtra("planStatus", "1");
                                finish();
                                startActivity(mainIntent);
                            } else if (_plan_device_status.equals("0")) {
                                Intent mainIntent = new Intent(OTPActivity.this, PlanExpiredScreen.class);
                                mainIntent.putExtra("planStatus", "2");
                                finish();
                                startActivity(mainIntent);
                            } else {

                                editor.putBoolean(getString(R.string.shared_login_status), true);
                                editor.commit();

                                Intent mainIntent = new Intent(OTPActivity.this, WhoWatchingActivity.class);
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


}