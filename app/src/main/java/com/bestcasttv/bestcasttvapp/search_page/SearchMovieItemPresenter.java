package com.bestcasttv.bestcasttvapp.search_page;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;
import androidx.leanback.widget.Presenter;

import com.bestcasttv.bestcasttvapp.R;
import com.bestcasttv.bestcasttvapp.main_page.model.Movies;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

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
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        view.setBackground(ContextCompat.getDrawable(context, R.drawable.card_rounded_bg));
        view.setClipToOutline(true);
        view.setForeground(ContextCompat.getDrawable(context, R.drawable.movie_card_focus_outline));

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

        Context context = viewHolder.view.getContext();
        float density = context.getResources().getDisplayMetrics().density;
        int radiusPx = Math.round(10 * density);

        Glide.with(context)
                .load(url)
                .override(widthPx > 0 ? widthPx : 300, heightPx > 0 ? heightPx : 450)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.default_portrate_large)
                .error(R.drawable.default_portrate_large)
                .transform(new CenterCrop(), new RoundedCorners(radiusPx))
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
