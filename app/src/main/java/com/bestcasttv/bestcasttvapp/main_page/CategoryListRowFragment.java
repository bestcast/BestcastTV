package com.bestcasttv.bestcasttvapp.main_page;

import android.os.Build;
import android.os.Bundle;
import android.view.View;

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

import com.bestcasttv.bestcasttvapp.main_page.model.Genres;

import java.util.List;

public class CategoryListRowFragment extends RowsSupportFragment {

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
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setAdapter(rootAdapter);

        setOnItemViewSelectedListener(new ItemViewSelectedListener());
        setOnItemViewClickedListener(new ItemViewClickListener());
    }

    public void bindData(List<Genres> dataList) {

        for (int i = 0; i < dataList.size(); i++) {
           ArrayObjectAdapter arrayObjectAdapter = new ArrayObjectAdapter(new CategoryItemPresenter());

            for (Genres detail : dataList) {
                arrayObjectAdapter.add(detail);
            }

            HeaderItem headerItem = new HeaderItem(0,"Categories");
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
        void onClick(Genres model);
    }

    public interface OnSelectedListener {
        void onSelection(Genres model);
    }


    private class ItemViewSelectedListener implements OnItemViewSelectedListener {
        @Override
        public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
            if (item instanceof Genres) {
                if (itemSelectedListener != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        itemSelectedListener.onSelection((Genres) item);
                    }
                }
            }
        }
    }

    private class ItemViewClickListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
            if (item instanceof Genres) {
                if (itemClickListener != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        itemClickListener.onClick((Genres) item);
                    }
                }
            }
        }
    }

    public View requestFocus() {
        View view = getView();
        if (view != null) {
            view.requestFocus();
        }
        return view;
    }
}