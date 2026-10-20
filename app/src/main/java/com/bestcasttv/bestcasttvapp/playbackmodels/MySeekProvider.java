package com.bestcasttv.bestcasttvapp.playbackmodels;

import android.media.MediaPlayer;

import androidx.leanback.widget.PlaybackSeekDataProvider;

public class MySeekProvider extends PlaybackSeekDataProvider {

//    @Override
    public long getSeekPosition(long currentPosition, long duration, int delta) {
        // Custom seek logic here, for example, jump by 10 seconds
        return Math.min(currentPosition + delta * 10000, duration);
    }

//    @Override
    public long[] getSeekPositions(long currentPosition, long duration, int seekInterval, int seekSteps) {
        // Example of creating an array of seek positions
        long[] seekPositions = new long[seekSteps];
        long seekTime = duration / seekSteps;
        for (int i = 0; i < seekSteps; i++) {
            seekPositions[i] = seekTime * i;
        }
        return seekPositions;
    }
}