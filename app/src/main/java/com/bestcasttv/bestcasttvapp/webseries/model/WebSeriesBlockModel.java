package com.bestcasttv.bestcasttvapp.webseries.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class WebSeriesBlockModel implements Serializable {
    @SerializedName("id")
    private String id;

    @SerializedName("title")
    private String title;

    @SerializedName("movies")
    private List<WebSeriesModel> webseriesList;

    public WebSeriesBlockModel() {}

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

    public List<WebSeriesModel> getWebseriesList() {
        return webseriesList != null ? webseriesList : new ArrayList<>();
    }

    public void setWebseriesList(List<WebSeriesModel> webseriesList) {
        this.webseriesList = webseriesList;
    }
}
