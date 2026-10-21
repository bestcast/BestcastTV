package com.bestcasttv.bestcasttvapp.search_page;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.leanback.widget.Presenter;

import com.bestcasttv.bestcasttvapp.R;
import com.bestcasttv.bestcasttvapp.main_page.model.Movies;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

public class SearchMovieItemPresenter extends Presenter {

    private int widthPx = -1;
    private int heightPx = -1;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        Context context = parent.getContext();
        if (widthPx <= 0) {
            int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
            int screenHeight = context.getResources().getDisplayMetrics().heightPixels;
            widthPx = Math.round(screenWidth * 0.12f);
            heightPx = Math.round(screenHeight * 0.30f);
        }

        android.view.View view = LayoutInflater.from(context).inflate(R.layout.item_view, parent, false);
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params != null) {
            params.width = widthPx;
            params.height = heightPx;
            view.setLayoutParams(params);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        if (!(item instanceof Movies)) return;
        Movies contentMovies = (Movies) item;
        ImageView imageView = viewHolder.view.findViewById(R.id.poster_image);
        if (imageView == null) return;

        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        String url = contentMovies.getPortrait();
        if (url == null || url.trim().isEmpty()) {
            url = contentMovies.getPortraitsmall();
        }
        if (url == null || url.trim().isEmpty()) {
            url = contentMovies.getThumbnail();
        }

        Glide.with(viewHolder.view.getContext())
                .load(url)
                .override(widthPx > 0 ? widthPx : 300, heightPx > 0 ? heightPx : 450)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.default_portrate_large)
                .error(R.drawable.default_portrate_large)
                .centerCrop()
                .into(imageView);
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {
        ImageView imageView = viewHolder.view.findViewById(R.id.poster_image);
        if (imageView != null) {
            Glide.with(viewHolder.view.getContext()).clear(imageView);
        }
    }
}

