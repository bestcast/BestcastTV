package com.bestcasttv.bestcasttvapp.webseries.presenter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.leanback.widget.ImageCardView;
import androidx.leanback.widget.Presenter;

import com.bestcasttv.bestcasttvapp.R;
import com.bestcasttv.bestcasttvapp.api_services.RetrofitClient;
import com.bestcasttv.bestcasttvapp.webseries.model.WebSeriesModel;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

public class WebSeriesCardPresenter extends Presenter {

    private static final int CARD_WIDTH_DP = 240;
    private static final int CARD_HEIGHT_DP = 135;

    private int widthPx = -1;
    private int heightPx = -1;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        Context context = parent.getContext();
        float density = context.getResources().getDisplayMetrics().density;

        if (widthPx <= 0) {
            widthPx = Math.round(CARD_WIDTH_DP * density);
            heightPx = Math.round(CARD_HEIGHT_DP * density);
        }

        ImageCardView cardView = new ImageCardView(context) {
            @Override
            public void setSelected(boolean selected) {
                updateCardBackgroundColor(this, selected);
                super.setSelected(selected);
            }
        };

        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);
        cardView.setMainImageDimensions(widthPx, heightPx);
        if (cardView.getMainImageView() != null) {
            cardView.getMainImageView().setScaleType(ImageView.ScaleType.CENTER_CROP);
        }

        cardView.setBackground(ContextCompat.getDrawable(context, R.drawable.card_rounded_bg));
        cardView.setClipToOutline(true);
        cardView.setForeground(ContextCompat.getDrawable(context, R.drawable.movie_card_focus_outline));

        TextView titleView = cardView.findViewById(androidx.leanback.R.id.title_text);
        if (titleView != null) {
            titleView.setTextColor(Color.WHITE);
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            titleView.setTypeface(null, Typeface.BOLD);
            titleView.setSingleLine(true);
        }

        TextView contentView = cardView.findViewById(androidx.leanback.R.id.content_text);
        if (contentView != null) {
            contentView.setTextColor(Color.parseColor("#AAAAAA"));
            contentView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            contentView.setSingleLine(true);
        }

        ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        int marginPx = Math.round(4 * density);
        params.setMargins(marginPx, marginPx, marginPx, marginPx);
        cardView.setLayoutParams(params);

        updateCardBackgroundColor(cardView, false);
        return new ViewHolder(cardView);
    }

    private static void updateCardBackgroundColor(ImageCardView view, boolean selected) {
        int color = selected ? Color.parseColor("#2A2A2A") : Color.parseColor("#141414");
        view.setInfoAreaBackgroundColor(color);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        if (item instanceof WebSeriesModel) {
            WebSeriesModel webSeries = (WebSeriesModel) item;
            ImageCardView cardView = (ImageCardView) viewHolder.view;

            cardView.setTitleText(webSeries.getTitle());

            // Dynamic Subtitle: Certificate • Year • Seasons/Duration
            StringBuilder subtitle = new StringBuilder();
            if (webSeries.getCertificate() != null && !webSeries.getCertificate().trim().isEmpty()) {
                subtitle.append(webSeries.getCertificate().trim());
            }
            if (webSeries.getYear() != null && !webSeries.getYear().trim().isEmpty()) {
                if (subtitle.length() > 0) subtitle.append(" • ");
                subtitle.append(webSeries.getYear().trim());
            }
            if (webSeries.getSeasons() != null && !webSeries.getSeasons().isEmpty()) {
                int count = webSeries.getSeasons().size();
                if (subtitle.length() > 0) subtitle.append(" • ");
                subtitle.append(count).append(count > 1 ? " Seasons" : " Season");
            } else if (webSeries.getDuration() != null && !webSeries.getDuration().trim().isEmpty()) {
                if (subtitle.length() > 0) subtitle.append(" • ");
                subtitle.append(webSeries.getDuration().trim());
            }
            cardView.setContentText(subtitle.length() > 0 ? subtitle.toString() : null);

            String imageUrl = webSeries.getThumbnail();
            if (imageUrl == null || imageUrl.isEmpty()) {
                imageUrl = webSeries.getImage();
            }

            if (imageUrl != null && !imageUrl.startsWith("http")) {
                imageUrl = RetrofitClient.BASE_URL + "/" + imageUrl;
            }

            Context context = cardView.getContext();
            float density = context.getResources().getDisplayMetrics().density;
            int radiusPx = Math.round(8 * density);

            // High performance bitmap decoding scaled precisely to card view dimensions
            Glide.with(context)
                    .load(imageUrl)
                    .override(widthPx > 0 ? widthPx : 480, heightPx > 0 ? heightPx : 270)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.default_landscape)
                    .error(R.drawable.default_landscape)
                    .transform(new CenterCrop(), new RoundedCorners(radiusPx))
                    .into(cardView.getMainImageView());
        }
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {
        ImageCardView cardView = (ImageCardView) viewHolder.view;
        if (cardView.getMainImageView() != null) {
            Glide.with(cardView.getContext()).clear(cardView.getMainImageView());
        }
        cardView.setBadgeImage(null);
        cardView.setMainImage(null);
    }
}
