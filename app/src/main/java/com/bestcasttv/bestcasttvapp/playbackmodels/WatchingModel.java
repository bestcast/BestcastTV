package com.bestcasttv.bestcasttvapp.playbackmodels;

public class WatchingModel {
    int watching;
    int watch_time;
    int watched_percent;

    public WatchingModel(int watching, int watch_time, int watched_percent) {
        this.watching = watching;
        this.watch_time = watch_time;
        this.watched_percent = watched_percent;
    }
}
