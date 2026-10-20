package com.bestcasttv.bestcasttvapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;
import android.widget.TextView;

public class RegisterAlertScreen extends FragmentActivity {

    TextView emailDescription;

    String getEmail="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_alert_screen);

        emailDescription=findViewById(R.id.emailDescription);

        getEmail=getIntent().getStringExtra("register_email");

        emailDescription.setText("Almost there! We just sent an email to "+getEmail);

    }
}