package com.bestcasttv.bestcasttvapp.webseries.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class EpisodeModel implements Serializable {
    @SerializedName("id")
    private String id;

    @SerializedName("title")
    private String title;

    @SerializedName("urlkey")
    private String urlkey;

    @SerializedName("video_url")
    private String videoUrl;

    @SerializedName("moviesource")
    private String movieSource;

    @SerializedName("trailer")
    private String trailer;

    @SerializedName("image")
    private String image;

    @SerializedName("medium")
    private String medium;

    @SerializedName("thumbnail")
    private String thumbnail;

    @SerializedName("portrait")
    private String portrait;

    @SerializedName("portraitsmall")
    private String portraitSmall;

    @SerializedName("duration")
    private String duration;

    @SerializedName("duration_secs")
    private String durationSecs;

    @SerializedName("duration_text")
    private String durationText;

    @SerializedName("certificate")
    private String certificate;

    @SerializedName("movie_access")
    private int movieAccess;

    @SerializedName("content")
    private String content;

    @SerializedName("content_plain")
    private String contentPlain;

    @SerializedName("episode_user")
    private EpisodeUserModel episodeUser;

    public EpisodeModel() {}

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

    public String getUrlkey() {
        return urlkey;
    }

    public void setUrlkey(String urlkey) {
        this.urlkey = urlkey;
    }

    public String getVideoUrl() {
        if (videoUrl != null && !videoUrl.isEmpty()) {
            return videoUrl;
        }
        return movieSource != null ? movieSource : "";
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
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

    public String getPortrait() {
        return portrait;
    }

    public void setPortrait(String portrait) {
        this.portrait = portrait;
    }

    public String getPortraitSmall() {
        return portraitSmall;
    }

    public void setPortraitSmall(String portraitSmall) {
        this.portraitSmall = portraitSmall;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getDurationSecs() {
        if (durationSecs != null && !durationSecs.isEmpty()) {
            return durationSecs;
        }
        return duration != null ? duration : "0";
    }

    public void setDurationSecs(String durationSecs) {
        this.durationSecs = durationSecs;
    }

    public String getDurationText() {
        if (durationText != null && !durationText.isEmpty()) {
            return durationText;
        }
        return duration != null ? duration : "";
    }

    public void setDurationText(String durationText) {
        this.durationText = durationText;
    }

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    public int getMovieAccess() {
        return movieAccess;
    }

    public void setMovieAccess(int movieAccess) {
        this.movieAccess = movieAccess;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContentPlain() {
        return contentPlain;
    }

    public void setContentPlain(String contentPlain) {
        this.contentPlain = contentPlain;
    }

    public EpisodeUserModel getEpisodeUser() {
        return episodeUser;
    }

    public void setEpisodeUser(EpisodeUserModel episodeUser) {
        this.episodeUser = episodeUser;
    }
}
