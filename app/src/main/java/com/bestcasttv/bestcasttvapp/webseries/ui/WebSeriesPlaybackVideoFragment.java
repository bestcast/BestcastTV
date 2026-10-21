package com.bestcasttv.bestcasttvapp.webseries.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.leanback.app.VideoSupportFragment;
import androidx.leanback.app.VideoSupportFragmentGlueHost;
import androidx.leanback.media.MediaPlayerAdapter;
import androidx.leanback.media.PlaybackGlue;
import androidx.leanback.widget.PlaybackControlsRow;

import com.bestcasttv.bestcasttvapp.R;
import com.bestcasttv.bestcasttvapp.api_services.ApiService;
import com.bestcasttv.bestcasttvapp.api_services.RetrofitClient;
import com.bestcasttv.bestcasttvapp.playbackmodels.CustomPlaybackTransportControlGlue;
import com.bestcasttv.bestcasttvapp.webseries.model.EpisodeModel;
import com.bestcasttv.bestcasttvapp.webseries.model.SeasonModel;
import com.bestcasttv.bestcasttvapp.webseries.model.UserEpisodeRequestModel;
import com.bestcasttv.bestcasttvapp.webseries.model.WebSeriesModel;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WebSeriesPlaybackVideoFragment extends VideoSupportFragment {

    private CustomPlaybackTransportControlGlue mTransportControlGlue;
    private MediaPlayerAdapter playerAdapter;
    private Timer progressTimer;

    private String token = "";
    private String profileID = "";

    private EpisodeModel currentEpisode;
    private WebSeriesModel webSeries;
    private EpisodeModel nextEpisode;
    private boolean isTrailer = false;
    private long startWatchTimeSec = 0;

    private LinearLayout layoutNextOverlay;
    private TextView txtCountdownTitle;
    private TextView txtNextEpisodeTitle;
    private Button btnPlayNext;
    private Button btnCancelNext;

    private boolean overlayShown = false;
    private boolean overlayCancelled = false;
    private CountDownTimer overlayTimer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getActivity() != null) {
            SharedPreferences sp = getActivity().getSharedPreferences(getString(R.string.shared_name), Context.MODE_PRIVATE);
            token = sp.getString(getString(R.string.token), "");
            profileID = sp.getString(getString(R.string.profileID), "");

            currentEpisode = (EpisodeModel) getActivity().getIntent().getSerializableExtra("episode");
            webSeries = (WebSeriesModel) getActivity().getIntent().getSerializableExtra("webseries");
            isTrailer = getActivity().getIntent().getBooleanExtra("isTrailer", false);

            String watchTimeStr = getActivity().getIntent().getStringExtra("watch_time");
            if (watchTimeStr != null && !watchTimeStr.isEmpty()) {
                try {
                    startWatchTimeSec = Long.parseLong(watchTimeStr);
                } catch (NumberFormatException ignored) {}
            }
        }

        findNextEpisode();

        VideoSupportFragmentGlueHost glueHost = new VideoSupportFragmentGlueHost(this);
        playerAdapter = new MediaPlayerAdapter(getActivity());
        playerAdapter.setRepeatAction(PlaybackControlsRow.RepeatAction.INDEX_NONE);

        mTransportControlGlue = new CustomPlaybackTransportControlGlue(getActivity(), playerAdapter);
        mTransportControlGlue.setHost(glueHost);
        mTransportControlGlue.setTitle(currentEpisode != null ? currentEpisode.getTitle() : "");
        mTransportControlGlue.setSubtitle(webSeries != null ? webSeries.getTitle() : "");
        mTransportControlGlue.setControlsOverlayAutoHideEnabled(true);
        mTransportControlGlue.setSeekEnabled(true);

        if (currentEpisode != null && currentEpisode.getVideoUrl() != null) {
            playerAdapter.setDataSource(Uri.parse(currentEpisode.getVideoUrl()));
        }

        mTransportControlGlue.addPlayerCallback(new PlaybackGlue.PlayerCallback() {
            @Override
            public void onPreparedStateChanged(PlaybackGlue glue) {
                super.onPreparedStateChanged(glue);
                if (glue.isPrepared()) {
                    if (startWatchTimeSec > 0) {
                        mTransportControlGlue.seekTo(startWatchTimeSec * 1000);
                        startWatchTimeSec = 0; // seek only once
                    }
                    mTransportControlGlue.play();
                    if (getActivity() != null) {
                        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    }
                }
            }

            @Override
            public void onPlayStateChanged(PlaybackGlue glue) {
                super.onPlayStateChanged(glue);
            }
        });

        mTransportControlGlue.playWhenPrepared();
        startProgressTrackingTimer();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupOverlayViews();
    }

    private void setupOverlayViews() {
        if (getActivity() == null) return;

        layoutNextOverlay = getActivity().findViewById(R.id.layout_next_overlay);
        txtCountdownTitle = getActivity().findViewById(R.id.txt_countdown_title);
        txtNextEpisodeTitle = getActivity().findViewById(R.id.txt_next_episode_title);
        btnPlayNext = getActivity().findViewById(R.id.btn_play_next);
        btnCancelNext = getActivity().findViewById(R.id.btn_cancel_next);

        if (btnPlayNext != null) {
            btnPlayNext.setOnClickListener(v -> playNextEpisode());
        }

        if (btnCancelNext != null) {
            btnCancelNext.setOnClickListener(v -> hideNextOverlay(true));
        }
    }

    private void findNextEpisode() {
        nextEpisode = null;
        if (isTrailer || webSeries == null || currentEpisode == null) return;

        List<SeasonModel> seasons = webSeries.getSeasons();
        if (seasons == null) return;

        boolean foundCurrent = false;
        for (SeasonModel season : seasons) {
            for (EpisodeModel ep : season.getEpisodes()) {
                if (foundCurrent) {
                    nextEpisode = ep;
                    return;
                }
                if (ep.getId().equals(currentEpisode.getId())) {
                    foundCurrent = true;
                }
            }
        }
    }

    private void startProgressTrackingTimer() {
        progressTimer = new Timer();
        progressTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (getActivity() == null || mTransportControlGlue == null || !mTransportControlGlue.isPlaying()) {
                    return;
                }

                long currentPosMs = mTransportControlGlue.getCurrentPosition();
                long durationMs = mTransportControlGlue.getDuration();

                if (durationMs <= 0) return;

                int currentSec = (int) (currentPosMs / 1000);
                int durationSec = (int) (durationMs / 1000);
                int watchedPercent = (int) (((double) currentPosMs / (double) durationMs) * 100.0);

                if (!isTrailer && currentEpisode != null) {
                    sendProgressToBackend(currentEpisode.getId(), currentSec, watchedPercent, durationSec);
                }

                long remainingMs = durationMs - currentPosMs;
                if (!isTrailer && nextEpisode != null && remainingMs <= 10000 && !overlayShown && !overlayCancelled) {
                    getActivity().runOnUiThread(() -> showNextOverlay());
                }

                if (remainingMs <= 500 && nextEpisode != null) {
                    getActivity().runOnUiThread(() -> playNextEpisode());
                }
            }
        }, 0, 15000);
    }

    private void sendProgressToBackend(String episodeId, int watchTimeSec, int watchedPercent, int durationSec) {
        ApiService apiService = RetrofitClient.getApiService(token);
        UserEpisodeRequestModel requestBody = new UserEpisodeRequestModel(
                watchTimeSec, 1, watchedPercent, watchTimeSec, durationSec
        );

        Call<JsonObject> call = apiService.setUserEpisode(episodeId, profileID, requestBody);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    Log.d("WebSeriesPlayer", "Saved episode progress: " + watchedPercent + "%");
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                Log.e("WebSeriesPlayer", "Error saving progress", t);
            }
        });
    }

    private void showNextOverlay() {
        if (layoutNextOverlay == null || nextEpisode == null || overlayCancelled) return;

        overlayShown = true;
        layoutNextOverlay.setVisibility(View.VISIBLE);
        if (txtNextEpisodeTitle != null) {
            txtNextEpisodeTitle.setText(nextEpisode.getTitle());
        }

        if (btnPlayNext != null) btnPlayNext.requestFocus();

        if (overlayTimer != null) overlayTimer.cancel();
        overlayTimer = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (txtCountdownTitle != null) {
                    txtCountdownTitle.setText("Next Episode starting in " + (millisUntilFinished / 1000) + "s...");
                }
            }

            @Override
            public void onFinish() {
                playNextEpisode();
            }
        }.start();
    }

    private void hideNextOverlay(boolean cancelledByUser) {
        if (overlayTimer != null) {
            overlayTimer.cancel();
            overlayTimer = null;
        }
        if (layoutNextOverlay != null) {
            layoutNextOverlay.setVisibility(View.GONE);
        }
        if (cancelledByUser) {
            overlayCancelled = true;
        }
    }

    private void playNextEpisode() {
        if (nextEpisode == null) return;

        hideNextOverlay(false);

        currentEpisode = nextEpisode;
        findNextEpisode();
        overlayShown = false;
        overlayCancelled = false;

        mTransportControlGlue.setTitle(currentEpisode.getTitle());
        if (currentEpisode.getVideoUrl() != null) {
            playerAdapter.setDataSource(Uri.parse(currentEpisode.getVideoUrl()));
            mTransportControlGlue.playWhenPrepared();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mTransportControlGlue != null) {
            mTransportControlGlue.pause();
        }
        if (getActivity() != null) {
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (progressTimer != null) {
            progressTimer.cancel();
            progressTimer = null;
        }
        if (overlayTimer != null) {
            overlayTimer.cancel();
            overlayTimer = null;
        }
    }
}
