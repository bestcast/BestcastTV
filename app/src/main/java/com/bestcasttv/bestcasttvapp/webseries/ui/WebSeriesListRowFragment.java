package com.bestcasttv.bestcasttvapp.webseries.ui;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.FocusFinder;
import androidx.leanback.widget.BrowseFrameLayout;
import com.bestcasttv.bestcasttvapp.R;
import com.bestcasttv.bestcasttvapp.main_page.MainPageActivity;

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
import com.bestcasttv.bestcasttvapp.webseries.presenter.CustomRowHeaderPresenter;
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

    private final WebSeriesCardPresenter cardPresenter = new WebSeriesCardPresenter();
    private final ArrayObjectAdapter rootAdapter = new ArrayObjectAdapter(listRowPresenter);

    {
        listRowPresenter.setShadowEnabled(false);
        listRowPresenter.setSelectEffectEnabled(false);
        listRowPresenter.setHeaderPresenter(new CustomRowHeaderPresenter());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (view instanceof ViewGroup) {
            ((ViewGroup) view).setClipChildren(false);
            ((ViewGroup) view).setClipToPadding(false);
        }

        setAdapter(rootAdapter);

        setOnItemViewSelectedListener(new ItemViewSelectedListener());
        setOnItemViewClickedListener(new ItemViewClickListener());

        if (view instanceof BrowseFrameLayout) {
            BrowseFrameLayout browseFrameLayout = (BrowseFrameLayout) view;
            browseFrameLayout.setOnFocusSearchListener((focused, direction) -> {
                if (direction == View.FOCUS_LEFT) {
                    View next = FocusFinder.getInstance().findNextFocus(browseFrameLayout, focused, View.FOCUS_LEFT);
                    if (next != null && next != focused) {
                        return next;
                    }
                    if (getActivity() instanceof MainPageActivity) {
                        View target = ((MainPageActivity) getActivity()).getSelectedMenuView();
                        if (target != null && target.isShown()) {
                            return target;
                        }
                    }
                } else if (direction == View.FOCUS_UP) {
                    View next = FocusFinder.getInstance().findNextFocus(browseFrameLayout, focused, View.FOCUS_UP);
                    if (next != null && next != focused) {
                        return next;
                    }
                    if (getActivity() != null) {
                        View heroPlay = getActivity().findViewById(R.id.btn_hero_play);
                        if (heroPlay != null && heroPlay.getVisibility() == View.VISIBLE) {
                            return heroPlay;
                        }
                    }
                }
                return null;
            });
        }
    }

    public boolean hasData() {
        return rootAdapter != null && rootAdapter.size() > 0;
    }

    public void bindData(List<WebSeriesBlockModel> dataList) {
        if (dataList == null || dataList.isEmpty()) return;

        rootAdapter.clear();

        for (WebSeriesBlockModel block : dataList) {
            if (block == null) continue;
            List<WebSeriesModel> result = block.getWebseriesList();
            if (result == null || result.isEmpty()) continue;

            ArrayObjectAdapter arrayObjectAdapter = new ArrayObjectAdapter(cardPresenter);

            for (WebSeriesModel series : result) {
                if (series != null) {
                    arrayObjectAdapter.add(series);
                }
            }

            if (arrayObjectAdapter.size() > 0) {
                String title = block.getTitle() != null ? block.getTitle() : "";
                HeaderItem headerItem = new HeaderItem(title);
                ListRow listRow = new ListRow(headerItem, arrayObjectAdapter);
                rootAdapter.add(listRow);
            }
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

    public boolean hasItems() {
        androidx.leanback.widget.VerticalGridView gridView = getVerticalGridView();
        return gridView != null && gridView.getChildCount() > 0;
    }

    public void requestFocusOnFirstItem() {
        androidx.leanback.widget.VerticalGridView gridView = getVerticalGridView();
        if (gridView != null && gridView.getChildCount() > 0) {
            gridView.requestFocus();
        } else if (getView() != null) {
            getView().requestFocus();
        }
    }
}
