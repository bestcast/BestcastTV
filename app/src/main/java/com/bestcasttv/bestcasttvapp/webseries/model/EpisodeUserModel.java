package com.bestcasttv.bestcasttvapp.webseries.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class EpisodeUserModel implements Serializable {
    @SerializedName("watch_time")
    private String watchTime;

    @SerializedName("watched_percent")
    private int watchedPercent;

    @SerializedName("watched")
    private int watched;

    public EpisodeUserModel() {}

    public EpisodeUserModel(String watchTime, int watchedPercent, int watched) {
        this.watchTime = watchTime;
        this.watchedPercent = watchedPercent;
        this.watched = watched;
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

    public int getWatched() {
        return watched;
    }

    public void setWatched(int watched) {
        this.watched = watched;
    }
}
