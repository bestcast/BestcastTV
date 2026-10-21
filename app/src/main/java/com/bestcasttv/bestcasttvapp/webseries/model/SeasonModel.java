package com.bestcasttv.bestcasttvapp.webseries.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SeasonModel implements Serializable {
    @SerializedName("id")
    private String id;

    @SerializedName("title")
    private String title;

    @SerializedName("season_number")
    private int seasonNumber;

    @SerializedName("episodes")
    private List<EpisodeModel> episodes;

    public SeasonModel() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getSeasonNumber() {
        return seasonNumber;
    }

    public void setSeasonNumber(int seasonNumber) {
        this.seasonNumber = seasonNumber;
    }

    public List<EpisodeModel> getEpisodes() {
        return episodes != null ? episodes : new ArrayList<>();
    }

    public void setEpisodes(List<EpisodeModel> episodes) {
        this.episodes = episodes;
    }
}
