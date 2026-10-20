package com.bestcasttv.bestcasttvapp;

import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.MediaController;
import android.widget.VideoView;

import com.bestcasttv.bestcasttvapp.who_watching.WhoWatchingActivity;

public class SplashscreenActivity extends FragmentActivity {

    private final int SPLASH_DISPLAY_LENGTH = 5000;
    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

        VideoView view =findViewById(R.id.idVideoView);

        sharedPreferences = getSharedPreferences(getString(R.string.shared_name), Context.MODE_PRIVATE);
        final boolean loginStatus = sharedPreferences.getBoolean(getString(R.string.shared_login_status), false);
        String _token = sharedPreferences.getString(getString(R.string.token), "");

        String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.bestcast_logo_reveal;
        Uri uri = Uri.parse(videoPath);
        view.setVideoURI(uri);

        // Add media controls (play, pause, etc.)
//        MediaController mediaController = new MediaController(this);
//        view.setMediaController(mediaController);
//        mediaController.setAnchorView(view);

        // Start the video
        view.start();


//        String path = "android.resource://" + getPackageName() + "/" + R.raw.bestcast_logo_reveal;
//        view.setVideoURI(Uri.parse(path));
//        view.start();

        new Handler().postDelayed(new Runnable(){
            @Override

            public void run() {
                if (!loginStatus&&_token.equals("")) {
                    Intent mainIntent = new Intent(SplashscreenActivity.this, LoginActivity.class);
                    finish();
                    startActivity(mainIntent);

                }else {
                    Intent mainIntent = new Intent(SplashscreenActivity.this, WhoWatchingActivity.class);
//                    Intent mainIntent = new Intent(SplashscreenActivity.this, LoginActivity.class);
                    finish();
                    startActivity(mainIntent);
                }
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}