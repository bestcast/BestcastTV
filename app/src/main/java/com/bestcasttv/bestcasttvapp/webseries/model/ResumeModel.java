package com.bestcasttv.bestcasttvapp.webseries.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class ResumeModel implements Serializable {
    @SerializedName("episode_id")
    private String episodeId;

    @SerializedName("season_id")
    private String seasonId;

    @SerializedName("season_title")
    private String seasonTitle;

    @SerializedName("episode_title")
    private String episodeTitle;

    @SerializedName("watch_time")
    private String watchTime;

    @SerializedName("watched_percent")
    private int watchedPercent;

    @SerializedName("release_date")
    private String releaseDate;

    public ResumeModel() {}

    public String getEpisodeId() {
        return episodeId;
    }

    public void setEpisodeId(String episodeId) {
        this.episodeId = episodeId;
    }

    public String getSeasonId() {
        return seasonId;
    }

    public void setSeasonId(String seasonId) {
        this.seasonId = seasonId;
    }

    public String getSeasonTitle() {
        return seasonTitle;
    }

    public void setSeasonTitle(String seasonTitle) {
        this.seasonTitle = seasonTitle;
    }

    public String getEpisodeTitle() {
        return episodeTitle;
    }

    public void setEpisodeTitle(String episodeTitle) {
        this.episodeTitle = episodeTitle;
    }

    public String getWatchTime() {
        return watchTime != null ? watchTime : "0";
    }

    public void setWatchTime(String watchTime) {
        this.watchTime = watchTime;
    }

    public int getWatchedPercent() {
        return watchedPercent;
    }

    public void setWatchedPercent(int watchedPercent) {
        this.watchedPercent = watchedPercent;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }
}
