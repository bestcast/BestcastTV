package com.bestcasttv.bestcasttvapp.main_page.ui;

import android.os.Bundle;

/**
 * Fragment dedicated to displaying All Movies.
 * Leverages the robust TV leanback rows, banner debouncing, and in-memory caching
 * configured specifically for the Movies section (pageId = "2").
 */
public class AllMoviesFragment extends HomeFragment {

    public static AllMoviesFragment newInstance() {
        return newInstance("");
    }

    public static AllMoviesFragment newInstance(String categoryName) {
        AllMoviesFragment fragment = new AllMoviesFragment();
        Bundle args = new Bundle();
        args.putString("categoryName", categoryName != null ? categoryName : "");
        args.putString("pageId", "2");
        fragment.setArguments(args);
        return fragment;
    }
}
