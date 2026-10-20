package com.bestcasttv.bestcasttvapp.main_page;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.leanback.widget.Presenter;

import com.bestcasttv.bestcasttvapp.R;
import com.bestcasttv.bestcasttvapp.main_page.model.Genres;

public class CategoryItemPresenter  extends Presenter {

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.selected_list_item_textview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        Genres genresItem = (Genres) item;
        TextView textView = viewHolder.view.findViewById(R.id.listItemTextview);
        textView.setText(genresItem.getTitle());
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {
        // Clean up resources if necessary
    }
}

