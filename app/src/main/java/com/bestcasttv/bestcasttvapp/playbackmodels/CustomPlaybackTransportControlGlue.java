package com.bestcasttv.bestcasttvapp.playbackmodels;

import android.content.Context;
import androidx.leanback.media.MediaPlayerAdapter;
import androidx.leanback.media.PlaybackTransportControlGlue;
import androidx.leanback.widget.Action;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.PlaybackControlsRow;

import android.view.KeyEvent;
import android.view.View;


public class CustomPlaybackTransportControlGlue extends PlaybackTransportControlGlue<MediaPlayerAdapter> {

    private static final int FAST_FORWARD_TIME_MS = 10000; // 10 seconds

    public CustomPlaybackTransportControlGlue(Context context, MediaPlayerAdapter playerAdapter) {
        super(context, playerAdapter);
    }

    @Override
    protected void onCreatePrimaryActions(ArrayObjectAdapter primaryActionsAdapter) {

        // Add fast forward action
        primaryActionsAdapter.add(new PlaybackControlsRow.RewindAction(getContext()));

        super.onCreatePrimaryActions(primaryActionsAdapter);

        // Add fast rewind action
        primaryActionsAdapter.add(new PlaybackControlsRow.FastForwardAction(getContext()));
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD) {
                fastForward();
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_MEDIA_REWIND) {
                rewind();
                return true;
            }
        }
        return super.onKey(v, keyCode, event);
    }


    @Override
    public void onActionClicked(Action action) {
        if (action instanceof PlaybackControlsRow.RewindAction) {
            rewind();
        } else if (action instanceof PlaybackControlsRow.FastForwardAction) {
            fastForward();
        }
        super.onActionClicked(action);
    }

    private void fastForward() {
        long newPosition = getCurrentPosition() + FAST_FORWARD_TIME_MS;
        if (newPosition > getDuration()) {
            newPosition = getDuration();
        }
        getPlayerAdapter().seekTo(newPosition);
        play();
    }

    private void rewind() {
        long newPosition = getCurrentPosition() - FAST_FORWARD_TIME_MS;
        if (newPosition > getDuration()) {
            newPosition = getDuration();
        }
        getPlayerAdapter().seekTo(newPosition);
        play();
    }
}
