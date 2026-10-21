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

public class WebSeriesCardPresenter extends Presenter {

    private static final int CARD_WIDTH_DP = 260;
    private static final int CARD_HEIGHT_DP = 146;

    private int dpToPx(Context context, int dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        Context context = parent.getContext();
        ImageCardView cardView = new ImageCardView(context) {
            @Override
            public void setSelected(boolean selected) {
                updateCardBackgroundColor(this, selected);
                super.setSelected(selected);
            }
        };

        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);
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

            String imageUrl = webSeries.getThumbnail();
            if (imageUrl == null || imageUrl.isEmpty()) {
                imageUrl = webSeries.getImage();
            }

            if (imageUrl != null && !imageUrl.startsWith("http")) {
                imageUrl = RetrofitClient.BASE_URL + "/" + imageUrl;
            }

            int widthPx = dpToPx(cardView.getContext(), CARD_WIDTH_DP);
            int heightPx = dpToPx(cardView.getContext(), CARD_HEIGHT_DP);
            cardView.setMainImageDimensions(widthPx, heightPx);
            cardView.getMainImageView().setScaleType(ImageView.ScaleType.FIT_XY);

            Glide.with(cardView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.default_background)
                    .error(R.drawable.default_background)
                    .into(cardView.getMainImageView());
        }
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {
        ImageCardView cardView = (ImageCardView) viewHolder.view;
        cardView.setBadgeImage(null);
        cardView.setMainImage(null);
    }
}
