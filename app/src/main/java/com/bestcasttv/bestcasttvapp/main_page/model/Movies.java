package com.bestcasttv.bestcasttvapp.main_page.model;

public class Movies {
    String id;
    String title;
    String topten;
    String trailer;
    String certificate;
    String duration;
    String tagText;
    String publishedDate;
    String userlist;
    String userlike;
    String thumbnail;
    String portraitsmall;
    String portrait;
    Usermovies usermovies;

    public Movies(String id, String title, String topten, String trailer, String certificate, String duration, String tagText, String publishedDate, String userlist, String userlike, String thumbnail, String portraitsmall, String portrait, Usermovies usermovies) {
        this.id = id;
        this.title = title;
        this.topten = topten;
        this.trailer = trailer;
        this.certificate = certificate;
        this.duration = duration;
        this.tagText = tagText;
        this.publishedDate = publishedDate;
        this.userlist = userlist;
        this.userlike = userlike;
        this.thumbnail = thumbnail;
        this.portraitsmall = portraitsmall;
        this.portrait = portrait;
        this.usermovies = usermovies;
    }

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

    public String getTopten() {
        return topten;
    }

    public void setTopten(String topten) {
        this.topten = topten;
    }

    public String getTrailer() {
        return trailer;
    }

    public void setTrailer(String trailer) {
        this.trailer = trailer;
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

    public String getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(String publishedDate) {
        this.publishedDate = publishedDate;
    }

    public String getUserlist() {
        return userlist;
    }

    public void setUserlist(String userlist) {
        this.userlist = userlist;
    }

    public String getUserlike() {
        return userlike;
    }

    public void setUserlike(String userlike) {
        this.userlike = userlike;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getPortraitsmall() {
        return portraitsmall;
    }

    public void setPortraitsmall(String portraitsmall) {
        this.portraitsmall = portraitsmall;
    }

    public String getPortrait() {
        return portrait;
    }

    public void setPortrait(String portrait) {
        this.portrait = portrait;
    }

    public Usermovies getUsermovies() {
        return usermovies;
    }

    public void setUsermovies(Usermovies usermovies) {
        this.usermovies = usermovies;
    }
}
