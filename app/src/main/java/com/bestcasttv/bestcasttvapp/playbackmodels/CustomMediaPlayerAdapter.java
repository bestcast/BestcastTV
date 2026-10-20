package com.bestcasttv.bestcasttvapp.playbackmodels;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import androidx.leanback.media.MediaPlayerAdapter;

public class CustomMediaPlayerAdapter extends MediaPlayerAdapter {

    Context context;
    public CustomMediaPlayerAdapter(Context context) {
        super(context);
        context=context;
    }

    public boolean setDataSource(Uri uri) {
        try {
            MediaPlayer mediaPlayer = getMediaPlayer();
            mediaPlayer.setDataSource(context, uri);
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

//    @Override
//    public void onPrepared(MediaPlayer mp) {
//        super.onPrepared(mp);
//        // Custom behavior when the media player is prepared
//    }

    @Override
    public void play() {
        super.play();
        // Custom behavior when playback starts
    }

    @Override
    public void pause() {
        super.pause();
        // Custom behavior when playback pauses
    }

    @Override
    public void seekTo(long position) {
        super.seekTo(position);
        // Custom behavior when seeking
    }

    @Override
    public void fastForward() {
        super.fastForward();
    }

    @Override
    public void rewind() {
        super.rewind();
    }

    @Override
    public boolean isPrepared() {
        return super.isPrepared();
    }


}

