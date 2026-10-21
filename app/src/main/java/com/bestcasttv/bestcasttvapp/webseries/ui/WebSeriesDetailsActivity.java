package com.bestcasttv.bestcasttvapp.webseries.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bestcasttv.bestcasttvapp.R;
import com.bestcasttv.bestcasttvapp.api_services.ApiService;
import com.bestcasttv.bestcasttvapp.api_services.RetrofitClient;
import com.bestcasttv.bestcasttvapp.app_utils.AppUtils;
import com.bestcasttv.bestcasttvapp.app_utils.LoadingDialog;
import com.bestcasttv.bestcasttvapp.webseries.model.EpisodeModel;
import com.bestcasttv.bestcasttvapp.webseries.model.ResumeModel;
import com.bestcasttv.bestcasttvapp.webseries.model.SeasonModel;
import com.bestcasttv.bestcasttvapp.webseries.model.WebSeriesModel;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WebSeriesDetailsActivity extends FragmentActivity {

    private ImageView imgSeriesBanner;
    private TextView txtSeriesTitle;
    private TextView txtSeriesYear;
    private TextView txtSeriesCertificate;
    private TextView txtSeriesSeasonsCount;
    private TextView txtSeriesDescription;
    private Button btnPlayResume;

    private RecyclerView recyclerSeasons;
    private RecyclerView recyclerEpisodes;

    private SeasonAdapter seasonAdapter;
    private EpisodeAdapter episodeAdapter;

    private AppUtils appUtils;
    private LoadingDialog loadingDialog;
    private SharedPreferences sharedPreferences;

    private String token = "";
    private String profileID = "";
    private String webSeriesId = "";

    private WebSeriesModel webSeriesData;
    private ResumeModel resumeData;
    private List<SeasonModel> seasonList = new ArrayList<>();
    private SeasonModel selectedSeason;
    private int selectedSeasonIndex = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_series_details);

        webSeriesId = getIntent().getStringExtra("webseries_id");
        if (webSeriesId == null) webSeriesId = "1";

        appUtils = new AppUtils(this);
        loadingDialog = new LoadingDialog(this);

        sharedPreferences = getSharedPreferences(getString(R.string.shared_name), Context.MODE_PRIVATE);
        token = sharedPreferences.getString(getString(R.string.token), "");
        profileID = sharedPreferences.getString(getString(R.string.profileID), "");

        imgSeriesBanner = findViewById(R.id.img_series_banner);
        txtSeriesTitle = findViewById(R.id.txt_series_title);
        txtSeriesYear = findViewById(R.id.txt_series_year);
        txtSeriesCertificate = findViewById(R.id.txt_series_certificate);
        txtSeriesSeasonsCount = findViewById(R.id.txt_series_seasons_count);
        txtSeriesDescription = findViewById(R.id.txt_series_description);
        btnPlayResume = findViewById(R.id.btn_play_resume);

        recyclerSeasons = findViewById(R.id.recycler_seasons);
        recyclerEpisodes = findViewById(R.id.recycler_episodes);

        recyclerSeasons.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerEpisodes.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerSeasons.setNestedScrollingEnabled(false);
        recyclerEpisodes.setNestedScrollingEnabled(false);

        btnPlayResume.setOnClickListener(v -> onPlayResumeClicked());

        loadWebSeriesDetails();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadWebSeriesDetails();
    }

    private void loadWebSeriesDetails() {
        if (loadingDialog != null && !isFinishing()) loadingDialog.show();

        ApiService apiService = RetrofitClient.getApiService(token);
        Call<JsonObject> call = apiService.getWebseriesWatchdetail(webSeriesId, profileID);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (loadingDialog != null && !isFinishing()) loadingDialog.cancel();

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().toString());
                        JSONObject dataObj = jsonObject.optJSONObject("data");

                        if (dataObj != null) {
                            Gson gson = new Gson();
                            webSeriesData = gson.fromJson(dataObj.toString(), WebSeriesModel.class);
                            resumeData = webSeriesData.getResume();
                            seasonList = webSeriesData.getSeasons();

                            updateUIWithData();
                        }
                    } catch (Exception e) {
                        Log.e("WebSeriesDetails", "Parsing error", e);
                    }
                } else {
                    if (appUtils != null) appUtils.showToast("Failed to load web series details");
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (loadingDialog != null && !isFinishing()) loadingDialog.cancel();
                Log.e("WebSeriesDetails", "Network error", t);
                if (appUtils != null) appUtils.showToast("Network error.");
            }
        });
    }

    private void updateUIWithData() {
        if (webSeriesData == null) return;

        txtSeriesTitle.setText(webSeriesData.getTitle());
        txtSeriesCertificate.setText(webSeriesData.getCertificate() != null ? webSeriesData.getCertificate() : "U/A 13+");

        String seriesYear = "";
        if (webSeriesData.getYear() != null && !webSeriesData.getYear().trim().isEmpty()) {
            seriesYear = webSeriesData.getYear().trim();
        } else {
            String dateStr = webSeriesData.getReleaseDate();
            if (dateStr == null || dateStr.trim().isEmpty()) {
                dateStr = webSeriesData.getPublishedDate();
            }
            if (dateStr != null && !dateStr.trim().isEmpty() && appUtils != null) {
                try {
                    seriesYear = appUtils.getDateTimeToYear(dateStr);
                } catch (Exception ignored) {}
            }
        }
        if (txtSeriesYear != null) {
            if (!seriesYear.isEmpty()) {
                txtSeriesYear.setVisibility(View.VISIBLE);
                txtSeriesYear.setText(seriesYear);
            } else {
                txtSeriesYear.setVisibility(View.GONE);
            }
        }

        if (txtSeriesSeasonsCount != null) {
            int count = seasonList != null ? seasonList.size() : 0;
            txtSeriesSeasonsCount.setText(count + (count == 1 ? " Season" : " Seasons"));
        }

        String content = webSeriesData.getContent();
        if (content != null) {
            content = content.replaceAll("<[^>]*>", "").trim();
        }
        txtSeriesDescription.setText(content != null ? content : "");

        String bannerUrl = webSeriesData.getImage();
        if (bannerUrl == null || bannerUrl.isEmpty()) {
            bannerUrl = webSeriesData.getThumbnail();
        }
        if (bannerUrl != null && !bannerUrl.startsWith("http")) {
            bannerUrl = RetrofitClient.BASE_URL + "/" + bannerUrl;
        }

        Glide.with(this)
                .load(bannerUrl)
                .placeholder(R.drawable.default_landscape)
                .error(R.drawable.default_landscape)
                .into(imgSeriesBanner);

        if (resumeData != null && resumeData.getEpisodeId() != null && !resumeData.getEpisodeId().isEmpty()) {
            String sTitle = resumeData.getSeasonTitle() != null ? resumeData.getSeasonTitle() : "";
            String eTitle = resumeData.getEpisodeTitle() != null ? resumeData.getEpisodeTitle() : "";
            if (sTitle.toLowerCase().startsWith("season")) {
                btnPlayResume.setText("Resume " + sTitle + " - " + eTitle);
            } else {
                btnPlayResume.setText("Resume Season " + sTitle + " " + eTitle);
            }
        } else {
            btnPlayResume.setText("Play S1 E1");
        }

        if (seasonList != null && !seasonList.isEmpty()) {
            if (selectedSeasonIndex < 0 || selectedSeasonIndex >= seasonList.size()) {
                selectedSeasonIndex = 0;
            }
            selectedSeason = seasonList.get(selectedSeasonIndex);
            List<EpisodeModel> currentEpisodes = (selectedSeason != null && selectedSeason.getEpisodes() != null)
                    ? selectedSeason.getEpisodes()
                    : new ArrayList<>();

            if (seasonAdapter == null) {
                seasonAdapter = new SeasonAdapter(seasonList, (season, position) -> {
                    selectedSeasonIndex = position;
                    selectedSeason = season;
                    if (episodeAdapter != null) {
                        List<EpisodeModel> eps = (season != null && season.getEpisodes() != null)
                                ? season.getEpisodes()
                                : new ArrayList<>();
                        episodeAdapter.updateEpisodes(eps);
                    }
                });
                seasonAdapter.setSelectedPosition(selectedSeasonIndex);
                recyclerSeasons.setAdapter(seasonAdapter);
            } else {
                seasonAdapter.setSelectedPosition(selectedSeasonIndex);
            }

            if (episodeAdapter == null) {
                episodeAdapter = new EpisodeAdapter(currentEpisodes, (episode, position) -> launchPlayer(episode));
                recyclerEpisodes.setAdapter(episodeAdapter);
            } else {
                episodeAdapter.updateEpisodes(currentEpisodes);
            }
        }
    }

    private void onPlayResumeClicked() {
        if (seasonList == null || seasonList.isEmpty()) return;

        EpisodeModel targetEpisode = null;

        if (resumeData != null && resumeData.getEpisodeId() != null && !resumeData.getEpisodeId().isEmpty()) {
            for (SeasonModel season : seasonList) {
                if (season == null || season.getEpisodes() == null) continue;
                for (EpisodeModel episode : season.getEpisodes()) {
                    if (episode != null && resumeData.getEpisodeId().equals(episode.getId())) {
                        targetEpisode = episode;
                        break;
                    }
                }
                if (targetEpisode != null) break;
            }
        }

        if (targetEpisode == null && seasonList.get(0) != null && seasonList.get(0).getEpisodes() != null && !seasonList.get(0).getEpisodes().isEmpty()) {
            targetEpisode = seasonList.get(0).getEpisodes().get(0);
        }

        if (targetEpisode != null) {
            launchPlayer(targetEpisode);
        } else {
            if (appUtils != null) appUtils.showToast("No episodes available");
        }
    }

    private void launchPlayer(EpisodeModel episode) {
        if (episode == null) return;

        Log.d("WebSeriesDetails", "launchPlayer called for episode id: " + episode.getId() + " title: " + episode.getTitle() + " videoUrl: " + episode.getVideoUrl());

        if (episode.getVideoUrl() != null && !episode.getVideoUrl().isEmpty()) {
            startPlaybackActivity(episode, null);
            return;
        }

        if (loadingDialog != null) loadingDialog.show();
        ApiService apiService = RetrofitClient.getApiService(token);
        Call<JsonObject> call = apiService.getUserEpisode(episode.getId(), profileID);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (loadingDialog != null) loadingDialog.cancel();
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().toString());
                        JSONObject dataObj = jsonObject.optJSONObject("data");
                        if (dataObj != null) {
                            String url = dataObj.optString("video_url", "");
                            if (url.isEmpty()) url = dataObj.optString("moviesource", "");
                            if (url.isEmpty()) url = dataObj.optString("trailer", "");
                            if (!url.isEmpty()) {
                                episode.setVideoUrl(url);
                            }
                            String watchTime = "0";
                            JSONObject epUserObj = dataObj.optJSONObject("episode_user");
                            if (epUserObj != null) {
                                watchTime = epUserObj.optString("watch_time", "0");
                            }
                            startPlaybackActivity(episode, watchTime);
                            return;
                        }
                    } catch (Exception e) {
                        Log.e("WebSeriesDetails", "Error parsing getuserepisode", e);
                    }
                }
                startPlaybackActivity(episode, null);
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (loadingDialog != null) loadingDialog.cancel();
                startPlaybackActivity(episode, null);
            }
        });
    }

    private void startPlaybackActivity(EpisodeModel episode, String watchTimeOverride) {
        if (episode == null || episode.getVideoUrl() == null || episode.getVideoUrl().isEmpty()) {
            if (appUtils != null) appUtils.showToast("Video source not available for " + (episode != null ? episode.getTitle() : "episode"));
            return;
        }

        Intent intent = new Intent(this, WebSeriesPlaybackActivity.class);
        intent.putExtra("episode", episode);
        intent.putExtra("webseries", webSeriesData);
        if (watchTimeOverride != null) {
            intent.putExtra("watch_time", watchTimeOverride);
        } else if (resumeData != null && episode.getId().equals(resumeData.getEpisodeId())) {
            intent.putExtra("watch_time", resumeData.getWatchTime());
        }
        startActivity(intent);
    }
}
