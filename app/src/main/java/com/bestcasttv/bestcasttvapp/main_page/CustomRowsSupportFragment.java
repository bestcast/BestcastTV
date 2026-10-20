package com.bestcasttv.bestcasttvapp.main_page;

import android.os.Bundle;

import androidx.leanback.app.RowsSupportFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bestcasttv.bestcasttvapp.main_page.model.Genres;
import com.bestcasttv.bestcasttvapp.main_page.ui.CategoryItemAdapter;

import java.util.List;

public class CustomRowsSupportFragment extends RowsSupportFragment {

    private RecyclerView mRecyclerView;
    private CategoryItemAdapter mAdapter;
    List<Genres> allCategoryItems;

    public CustomRowsSupportFragment(List<Genres> allCategoryItems) {
        this.allCategoryItems=allCategoryItems;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize your RecyclerView and Adapter
        mRecyclerView = new RecyclerView(getActivity());
        mAdapter = new CategoryItemAdapter(allCategoryItems, getActivity());

        // Set layout manager and adapter for RecyclerView
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mAdapter);

        // Set RecyclerView as the primary view for the fragment
//        setAdapter(mAdapter);
//        setAdapter(mAdapter);
    }
}

