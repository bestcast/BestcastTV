package com.bestcasttv.bestcasttvapp.webseries.ui;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.bestcasttv.bestcasttvapp.R;

public class WebSeriesPlaybackActivity extends FragmentActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_series_playback);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.playback_fragment_container, new WebSeriesPlaybackVideoFragment())
                    .commit();
        }
    }
}
