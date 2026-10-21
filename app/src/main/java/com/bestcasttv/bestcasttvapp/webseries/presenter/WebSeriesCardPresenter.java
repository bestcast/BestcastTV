package com.bestcasttv.bestcasttvapp.webseries.presenter;

import android.content.Context;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.leanback.widget.ImageCardView;
import androidx.leanback.widget.Presenter;

import com.bestcasttv.bestcasttvapp.R;
import com.bestcasttv.bestcasttvapp.api_services.RetrofitClient;
import com.bestcasttv.bestcasttvapp.webseries.model.WebSeriesModel;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

public class WebSeriesCardPresenter extends Presenter {

    private static final int CARD_WIDTH_DP = 260;
    private static final int CARD_HEIGHT_DP = 146;

    private int widthPx = -1;
    private int heightPx = -1;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        Context context = parent.getContext();

        if (widthPx <= 0) {
            float density = context.getResources().getDisplayMetrics().density;
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
        updateCardBackgroundColor(cardView, false);
        return new ViewHolder(cardView);
    }

    private static void updateCardBackgroundColor(ImageCardView view, boolean selected) {
        int color = selected ? Color.parseColor("#E2000000") : Color.TRANSPARENT;
        view.setBackgroundColor(color);
        view.setInfoAreaBackgroundColor(color);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        if (item instanceof WebSeriesModel) {
            WebSeriesModel webSeries = (WebSeriesModel) item;
            ImageCardView cardView = (ImageCardView) viewHolder.view;

            cardView.setTitleText(webSeries.getTitle());

            // Dynamic Subtitle: Certificate and/or Year
            StringBuilder subtitle = new StringBuilder();
            if (webSeries.getCertificate() != null && !webSeries.getCertificate().trim().isEmpty()) {
                subtitle.append(webSeries.getCertificate().trim());
            }
            if (webSeries.getYear() != null && !webSeries.getYear().trim().isEmpty()) {
                if (subtitle.length() > 0) subtitle.append(" • ");
                subtitle.append(webSeries.getYear().trim());
            }
            cardView.setContentText(subtitle.length() > 0 ? subtitle.toString() : null);

            String imageUrl = webSeries.getThumbnail();
            if (imageUrl == null || imageUrl.isEmpty()) {
                imageUrl = webSeries.getImage();
            }

            if (imageUrl != null && !imageUrl.startsWith("http")) {
                imageUrl = RetrofitClient.BASE_URL + "/" + imageUrl;
            }

            // High performance bitmap decoding scaled precisely to card view dimensions
            Glide.with(cardView.getContext())
                    .load(imageUrl)
                    .override(widthPx > 0 ? widthPx : 520, heightPx > 0 ? heightPx : 292)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.default_background)
                    .error(R.drawable.default_background)
                    .centerCrop()
                    .into(cardView.getMainImageView());
        }
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {
        ImageCardView cardView = (ImageCardView) viewHolder.view;
        // Release bitmap resource immediately to prevent memory pressure
        if (cardView.getMainImageView() != null) {
            Glide.with(cardView.getContext()).clear(cardView.getMainImageView());
        }
        cardView.setBadgeImage(null);
        cardView.setMainImage(null);
    }
}
