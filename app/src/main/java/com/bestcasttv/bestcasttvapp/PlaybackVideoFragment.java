package com.bestcasttv.bestcasttvapp;

import static androidx.core.content.ContextCompat.getSystemService;
import static androidx.databinding.DataBindingUtil.setContentView;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import androidx.core.content.ContextCompat;
import androidx.leanback.app.VideoSupportFragment;
import androidx.leanback.app.VideoSupportFragmentGlueHost;
import androidx.leanback.media.MediaPlayerAdapter;
import androidx.leanback.media.PlaybackGlue;
import androidx.leanback.media.PlaybackTransportControlGlue;
import androidx.leanback.widget.PlaybackControlsRow;
import androidx.leanback.widget.PlaybackSeekDataProvider;

import com.bestcasttv.bestcasttvapp.api_services.ApiService;
import com.bestcasttv.bestcasttvapp.api_services.RetrofitClient;
import com.bestcasttv.bestcasttvapp.app_utils.AppUtils;
import com.bestcasttv.bestcasttvapp.app_utils.LoadingDialog;
import com.bestcasttv.bestcasttvapp.move_details_page.MovieData;
import com.bestcasttv.bestcasttvapp.move_details_page.model.MyListRequestModel;
import com.bestcasttv.bestcasttvapp.playbackmodels.CustomPlaybackTransportControlGlue;
import com.bestcasttv.bestcasttvapp.playbackmodels.MySeekProvider;
import com.bestcasttv.bestcasttvapp.playbackmodels.WatchingModel;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Handles video playback with media controls.
 */
public class PlaybackVideoFragment extends VideoSupportFragment {

    //    private PlaybackTransportControlGlue<MediaPlayerAdapter> mTransportControlGlue;
    private CustomPlaybackTransportControlGlue mTransportControlGlue;

    private Timer seekTimer;

    String _token = "";
    String profileName = "";
    String profilePicture = "";
    String profileID = "";
    String profilePictureID = "";

    AppUtils appUtils;
    LoadingDialog loadingDialog;
    SharedPreferences sharedPreferences;
    MovieData movie;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.play_back_screen);

        appUtils = new AppUtils(getActivity());
        loadingDialog = new LoadingDialog(getActivity());


        sharedPreferences = getActivity().getSharedPreferences(getString(R.string.shared_name), Context.MODE_PRIVATE);
        _token = sharedPreferences.getString(getString(R.string.token), "");
        profileName = sharedPreferences.getString(getString(R.string.profileName), "");
        profilePicture = sharedPreferences.getString(getString(R.string.profilePicture), "");
        profileID = sharedPreferences.getString(getString(R.string.profileID), "");
        profilePictureID = sharedPreferences.getString(getString(R.string.profilePictureID), "");


        String movieData = getActivity().getIntent().getStringExtra(DetailsActivity.MOVIE);
        String watchedPercent = getActivity().getIntent().getStringExtra(DetailsActivity.WATCHEDPERCENT);
        String watchingTime = getActivity().getIntent().getStringExtra(DetailsActivity.WATCHINGTIME);
        Log.d("movieData", movieData);

        Gson gson = new Gson();
        movie = gson.fromJson(movieData, MovieData.class);
        Log.d("movieDataTitle", movie.getTitle());
        Log.d("movieDataID", movie.getId());
        Log.d("watchedPercent", watchedPercent);
        Log.d("watchingTime", watchingTime);



        Pattern exp = Pattern.compile("<[^>]*>", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
        String resultTagText = movie.getTagText().replaceAll(exp.pattern(), "  ");

//        final MovieData movie =(MovieData) getActivity().getIntent().getSerializableExtra(DetailsActivity.MOVIE);

        VideoSupportFragmentGlueHost glueHost =
                new VideoSupportFragmentGlueHost(PlaybackVideoFragment.this);

        MediaPlayerAdapter playerAdapter = new MediaPlayerAdapter(getActivity());
        playerAdapter.setRepeatAction(PlaybackControlsRow.RepeatAction.INDEX_NONE);


//        mTransportControlGlue = new PlaybackTransportControlGlue<>(getActivity(), playerAdapter);
        mTransportControlGlue = new CustomPlaybackTransportControlGlue(getActivity(), playerAdapter);
        mTransportControlGlue.setHost(glueHost);
        mTransportControlGlue.setTitle(movie.getTitle());
        mTransportControlGlue.setSubtitle(resultTagText);
        mTransportControlGlue.setControlsOverlayAutoHideEnabled(true);
        mTransportControlGlue.setSeekEnabled(true);
        playerAdapter.setDataSource(Uri.parse(movie.getVideoUrl()));
//        playerAdapter.setDataSource(Uri.parse("https://bestcast-mobile-download-movies.s3.amazonaws.com/Movies/trailer-720p.mp4"));


        mTransportControlGlue.addPlayerCallback(new PlaybackGlue.PlayerCallback() {
            @Override
            public void onPreparedStateChanged(PlaybackGlue glue) {
                super.onPreparedStateChanged(glue);
                if (glue.isPrepared()) {
                    if (!watchingTime.equals("")) {

                        int convertseconds = Integer.parseInt(watchingTime)*1000;
                        long seconds = Long.parseLong(String.valueOf(convertseconds));
                        Log.d("seekDurationseconds", String.valueOf(seconds));
                        mTransportControlGlue.seekTo(seconds);
                    }
                    mTransportControlGlue.play();
                    getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                }

            }

            @Override
            public void onPlayStateChanged(PlaybackGlue glue) {
                super.onPlayStateChanged(glue);
            }
        });

        playerAdapter.fastForward();
        playerAdapter.previous();
        customizeProgressBarColors(getActivity());
        playerAdapter.getMediaPlayer().setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(MediaPlayer mp) {
//                mTransportControlGlue.play();
//                mTransportControlGlue.playWhenPrepared();
            }
        });



//        mTransportControlGlue.seekTo(mediaPlayer.getCurrentPosition() + 10000);

        mTransportControlGlue.playWhenPrepared();

//        mediaPlayer.pause();
//        attachSeekBarListener();

        setMovieSeekPosition();


    }

    private void attachSeekBarListener() {
        View rootView = getView();
        if (rootView != null) {
            SeekBar seekBar = rootView.findViewById(androidx.leanback.R.id.playback_progress);
            if (seekBar != null) {
                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        Log.d("SeekStatus", "True");
                        if (fromUser) {
                            long newPosition = (long) ((progress / 100.0) * mTransportControlGlue.getDuration());
                            mTransportControlGlue.seekTo(newPosition);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        // Pause the player while seeking
                        mTransportControlGlue.pause();
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        // Resume playback after seeking
                        mTransportControlGlue.play();
                    }
                });
            }
        }
    }


    public void setMovieSeekPosition() {

        seekTimer = new Timer();
        seekTimer.schedule(new TimerTask() {
            @Override
            public void run() {


                if (mTransportControlGlue.isPlaying()) {

                    int currentPostion = (int) mTransportControlGlue.getCurrentPosition();
                    int duration = (int) mTransportControlGlue.getDuration();
                    Log.d("ControlSeekPosition", String.valueOf(currentPostion));
                    Log.d("ControlDuration", String.valueOf(duration));

                    double percentage = ((double) currentPostion / (double) duration) * 100.0;
                    Log.d("ControlPercentage", String.valueOf((int) percentage));

                    long watchSeconds = currentPostion / 1000;
                    Log.d("watchSeconds", String.valueOf(watchSeconds));


                    WatchingModel myListRequestModel = new WatchingModel(
                            1, (int) watchSeconds, (int) percentage
                    );
                    setUsermovieWatching(_token, profileID, movie.getId(), myListRequestModel);
                }

            }

        }, 0, 3000);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mTransportControlGlue != null) {
            mTransportControlGlue.pause();
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    void setUsermovieWatching(String token, String _profileID, String _movieID, WatchingModel postValues) {

        ApiService apiService = RetrofitClient.getApiService(token);

        String url = "/api/setusermovie/" + _movieID + "?profile_id=" + _profileID;
        Call<JsonObject> call = apiService.setUsermovieWatching(url, postValues);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                Log.d("SetUserMovie_Response", response.toString());
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject data = response.body();
                    Log.d("SetUserMovie_Response", data.toString());
                    if (response.code() == 200) {
                        Log.d("SetUserMovieStatus", "set user movie watching added");
                    } else {
                        Log.e("SetUserMovieStatus:", response.toString());
                    }

                } else {
                    Log.d("ResponseUserMovie_Error", "");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d("SetUserMovie_Error", t.toString());
            }
        });

    }

    private void customizeProgressBarColors(Context context) {
        View rootView = getView();
        if (rootView != null) {
            ProgressBar progressBar = rootView.findViewById(androidx.leanback.R.id.playback_progress);
            if (progressBar != null) {
                Drawable progressDrawable = ContextCompat.getDrawable(context, R.drawable.custom_progress_drawable);
                progressBar.setProgressDrawable(progressDrawable);
            }
        }
    }
}