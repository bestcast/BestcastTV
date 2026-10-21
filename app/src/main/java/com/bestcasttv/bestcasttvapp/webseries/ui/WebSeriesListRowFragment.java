package com.bestcasttv.bestcasttvapp.webseries.ui;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.leanback.app.RowsSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.FocusHighlight;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;
import androidx.leanback.widget.OnItemViewClickedListener;
import androidx.leanback.widget.OnItemViewSelectedListener;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;

import com.bestcasttv.bestcasttvapp.webseries.model.WebSeriesBlockModel;
import com.bestcasttv.bestcasttvapp.webseries.model.WebSeriesModel;
import com.bestcasttv.bestcasttvapp.webseries.presenter.WebSeriesCardPresenter;

import java.util.List;

public class WebSeriesListRowFragment extends RowsSupportFragment {

    private OnSelectedListener itemSelectedListener;
    private OnClickListener itemClickListener;

    private final ListRowPresenter listRowPresenter = new ListRowPresenter(FocusHighlight.ZOOM_FACTOR_MEDIUM) {
        @Override
        public boolean isUsingDefaultListSelectEffect() {
            return false;
        }
    };

    private final ArrayObjectAdapter rootAdapter = new ArrayObjectAdapter(listRowPresenter);

    {
        listRowPresenter.setShadowEnabled(false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setAdapter(rootAdapter);

        setOnItemViewSelectedListener(new ItemViewSelectedListener());
        setOnItemViewClickedListener(new ItemViewClickListener());
    }

    public void bindData(List<WebSeriesBlockModel> dataList) {
        rootAdapter.clear();
        for (WebSeriesBlockModel block : dataList) {
            List<WebSeriesModel> result = block.getWebseriesList();
            ArrayObjectAdapter arrayObjectAdapter = new ArrayObjectAdapter(new WebSeriesCardPresenter());

            for (WebSeriesModel series : result) {
                arrayObjectAdapter.add(series);
            }

            HeaderItem headerItem = new HeaderItem(block.getTitle());
            ListRow listRow = new ListRow(headerItem, arrayObjectAdapter);
            rootAdapter.add(listRow);
        }
    }

    public void setOnContentSelectedListener(OnSelectedListener listener) {
        this.itemSelectedListener = listener;
    }

    public void setOnItemClickListener(OnClickListener listener) {
        this.itemClickListener = listener;
    }

    public interface OnClickListener {
        void onClick(WebSeriesModel model);
    }

    public interface OnSelectedListener {
        void onSelection(WebSeriesModel model);
    }

    private class ItemViewSelectedListener implements OnItemViewSelectedListener {
        @Override
        public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
            if (item instanceof WebSeriesModel) {
                if (itemSelectedListener != null) {
                    itemSelectedListener.onSelection((WebSeriesModel) item);
                }
            }
        }
    }

    private class ItemViewClickListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
            if (item instanceof WebSeriesModel) {
                if (itemClickListener != null) {
                    itemClickListener.onClick((WebSeriesModel) item);
                }
            }
        }
    }
}
