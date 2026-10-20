package com.bestcasttv.bestcasttvapp.main_page.model;

import java.util.List;

public class MoviesMainCategoryModel {
    String id;
    String title;
    List<Movies> movies;

    public MoviesMainCategoryModel(String id, String title, List<Movies> movies) {
        this.id = id;
        this.title = title;
        this.movies = movies;
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

    public List<Movies> getMovies() {
        return movies;
    }

    public void setMovies(List<Movies> movies) {
        this.movies = movies;
    }
}
