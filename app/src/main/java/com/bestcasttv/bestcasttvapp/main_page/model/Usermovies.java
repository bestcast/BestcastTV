package com.bestcasttv.bestcasttvapp.main_page.model;

public class Usermovies {
    String id;
    String movieId;
    String mylist;
    String likes;
    String watchTime;
    String watching;
    String watched;
    String watchedPercent;
    String viewed;

    public Usermovies(String id, String movieId, String mylist, String likes, String watchTime, String watching, String watched, String watchedPercent, String viewed) {
        this.id = id;
        this.movieId = movieId;
        this.mylist = mylist;
        this.likes = likes;
        this.watchTime = watchTime;
        this.watching = watching;
        this.watched = watched;
        this.watchedPercent = watchedPercent;
        this.viewed = viewed;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public String getMylist() {
        return mylist;
    }

    public void setMylist(String mylist) {
        this.mylist = mylist;
    }

    public String getLikes() {
        return likes;
    }

    public void setLikes(String likes) {
        this.likes = likes;
    }

    public String getWatchTime() {
        return watchTime;
    }

    public void setWatchTime(String watchTime) {
        this.watchTime = watchTime;
    }

    public String getWatching() {
        return watching;
    }

    public void setWatching(String watching) {
        this.watching = watching;
    }

    public String getWatched() {
        return watched;
    }

    public void setWatched(String watched) {
        this.watched = watched;
    }

    public String getWatchedPercent() {
        return watchedPercent;
    }

    public void setWatchedPercent(String watchedPercent) {
        this.watchedPercent = watchedPercent;
    }

    public String getViewed() {
        return viewed;
    }

    public void setViewed(String viewed) {
        this.viewed = viewed;
    }
}
