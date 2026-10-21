package com.bestcasttv.bestcasttvapp.webseries.model;

import com.google.gson.annotations.SerializedName;

public class UserEpisodeRequestModel {
    @SerializedName("watch_time")
    private int watchTime;

    @SerializedName("watching")
    private int watching;

    @SerializedName("watched_percent")
    private int watchedPercent;

    @SerializedName("watched")
    private int watched;

    @SerializedName("movieDuration")
    private int movieDuration;

    public UserEpisodeRequestModel() {}

    public UserEpisodeRequestModel(int watchTime, int watching, int watchedPercent, int watched, int movieDuration) {
        this.watchTime = watchTime;
        this.watching = watching;
        this.watchedPercent = watchedPercent;
        this.watched = watched;
        this.movieDuration = movieDuration;
    }

    public int getWatchTime() {
        return watchTime;
    }

    public void setWatchTime(int watchTime) {
        this.watchTime = watchTime;
    }

    public int getWatching() {
        return watching;
    }

    public void setWatching(int watching) {
        this.watching = watching;
    }

    public int getWatchedPercent() {
        return watchedPercent;
    }

    public void setWatchedPercent(int watchedPercent) {
        this.watchedPercent = watchedPercent;
    }

    public int getWatched() {
        return watched;
    }

    public void setWatched(int watched) {
        this.watched = watched;
    }

    public int getMovieDuration() {
        return movieDuration;
    }

    public void setMovieDuration(int movieDuration) {
        this.movieDuration = movieDuration;
    }
}
