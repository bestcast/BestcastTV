package com.bestcasttv.bestcasttvapp.webseries.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class WebSeriesModel implements Serializable {
    @SerializedName("id")
    private String id;

    @SerializedName("title")
    private String title;

    @SerializedName("content")
    private String content;

    @SerializedName("trailer")
    private String trailer;

    @SerializedName("image")
    private String image;

    @SerializedName("medium")
    private String medium;

    @SerializedName("thumbnail")
    private String thumbnail;

    @SerializedName("logo")
    private String logo;

    @SerializedName("certificate")
    private String certificate;

    @SerializedName("duration")
    private String duration;

    @SerializedName("tag_text")
    private String tagText;

    @SerializedName("topten")
    private int topTen;

    @SerializedName("movie_access")
    private int movieAccess;

    @SerializedName("seasons")
    private List<SeasonModel> seasons;

    @SerializedName("resume")
    private ResumeModel resume;

    @SerializedName("release_date")
    private String releaseDate;

    @SerializedName("published_date")
    private String publishedDate;

    @SerializedName("year")
    private String year;

    public WebSeriesModel() {}

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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTrailer() {
        return trailer != null ? trailer : "";
    }

    public void setTrailer(String trailer) {
        this.trailer = trailer;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getMedium() {
        return medium;
    }

    public void setMedium(String medium) {
        this.medium = medium;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getTagText() {
        return tagText;
    }

    public void setTagText(String tagText) {
        this.tagText = tagText;
    }

    public int getTopTen() {
        return topTen;
    }

    public void setTopTen(int topTen) {
        this.topTen = topTen;
    }

    public int getMovieAccess() {
        return movieAccess;
    }

    public void setMovieAccess(int movieAccess) {
        this.movieAccess = movieAccess;
    }

    public List<SeasonModel> getSeasons() {
        return seasons != null ? seasons : new ArrayList<>();
    }

    public void setSeasons(List<SeasonModel> seasons) {
        this.seasons = seasons;
    }

    public ResumeModel getResume() {
        return resume;
    }

    public void setResume(ResumeModel resume) {
        this.resume = resume;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(String publishedDate) {
        this.publishedDate = publishedDate;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }
}
