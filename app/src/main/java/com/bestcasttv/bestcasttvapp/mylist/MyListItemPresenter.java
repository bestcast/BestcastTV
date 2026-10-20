package com.bestcasttv.bestcasttvapp.mylist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.leanback.widget.Presenter;

import com.bumptech.glide.Glide;
import com.bestcasttv.bestcasttvapp.R;
import com.bestcasttv.bestcasttvapp.main_page.model.Movies;

public class MyListItemPresenter extends Presenter {
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        android.view.View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view, parent, false);
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.width = getWidthInPercent(parent.getContext(), 32);
        params.height = getHeightInPercent(parent.getContext(), 12);
        return new ViewHolder(view);
    }

    private int getWidthInPercent(Context context, int percent) {
        int width = context.getResources().getDisplayMetrics().widthPixels;
        return (width * percent) / 100;
    }

    private int getHeightInPercent(Context context, int percent) {
        int height = context.getResources().getDisplayMetrics().heightPixels;
        return (height * percent) / 100;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        Movies contentMovies = (Movies) item;
        ImageView imageView = viewHolder.view.findViewById(R.id.poster_image);
        String url = contentMovies.getPortrait();
        Glide.with(viewHolder.view.getContext())
                .load(url)
                .placeholder(R.drawable.default_portrate_large)
                .error(R.drawable.default_portrate_large)
                .into(imageView);
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {
    }
}

