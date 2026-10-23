package com.bestcasttv.bestcasttvapp.main_page;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.leanback.app.RowsSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.BrowseFrameLayout;
import androidx.leanback.widget.FocusHighlight;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;
import androidx.leanback.widget.OnItemViewClickedListener;
import androidx.leanback.widget.OnItemViewSelectedListener;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;
import androidx.leanback.widget.VerticalGridView;

import android.os.Bundle;
import android.view.FocusFinder;
import android.view.View;
import android.view.ViewGroup;

import com.bestcasttv.bestcasttvapp.R;
import com.bestcasttv.bestcasttvapp.main_page.model.Movies;
import com.bestcasttv.bestcasttvapp.main_page.model.MoviesMainCategoryModel;
import com.bestcasttv.bestcasttvapp.webseries.presenter.CustomRowHeaderPresenter;

import java.util.List;

public class ListRowFragment extends RowsSupportFragment {

    private OnSelectedListener itemSelectedListener;
    private OnClickListener itemClickListener;

    private final CustomRowHeaderPresenter headerPresenter = new CustomRowHeaderPresenter();
    private final ListRowPresenter listRowPresenter = new ListRowPresenter(FocusHighlight.ZOOM_FACTOR_MEDIUM) {
        @Override
        public boolean isUsingDefaultListSelectEffect() {
            return false;
        }
    };

    private final ArrayObjectAdapter rootAdapter = new ArrayObjectAdapter(listRowPresenter);
    {
        listRowPresenter.setShadowEnabled(false);
        listRowPresenter.setSelectEffectEnabled(false);
        listRowPresenter.setHeaderPresenter(headerPresenter);
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

        // TV Remote Navigation: Handle Left exit to sidebar & Up exit to hero banner
        if (view instanceof BrowseFrameLayout) {
            BrowseFrameLayout browseFrameLayout = (BrowseFrameLayout) view;
            browseFrameLayout.setOnFocusSearchListener((focused, direction) -> {
                if (direction == View.FOCUS_LEFT) {
                    View next = FocusFinder.getInstance().findNextFocus(browseFrameLayout, focused, View.FOCUS_LEFT);
                    if (next != null && next != focused) {
                        return next;
                    }
                    if (getActivity() != null) {
                        View sidebar = getActivity().findViewById(R.id.btn_home);
                        if (sidebar == null || !sidebar.isShown()) {
                            sidebar = getActivity().findViewById(R.id.btn_movies);
                        }
                        if (sidebar != null && sidebar.isShown()) {
                            return sidebar;
                        }
                    }
                } else if (direction == View.FOCUS_UP) {
                    View next = FocusFinder.getInstance().findNextFocus(browseFrameLayout, focused, View.FOCUS_UP);
                    if (next != null && next != focused) {
                        return next;
                    }
                    if (getActivity() != null) {
                        View heroPlay = getActivity().findViewById(R.id.btn_hero_play);
                        if (heroPlay != null && heroPlay.isShown()) {
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

    public void clear() {
        if (rootAdapter != null) {
            rootAdapter.clear();
        }
    }

    public void bindData(List<MoviesMainCategoryModel> dataList) {
        if (dataList == null || dataList.isEmpty()) return;

        rootAdapter.clear();

        for (int i = 0; i < dataList.size(); i++) {
            MoviesMainCategoryModel category = dataList.get(i);
            if (category == null) continue;

            List<Movies> result = category.getMovies();
            if (result == null || result.isEmpty()) continue;

            ArrayObjectAdapter arrayObjectAdapter = new ArrayObjectAdapter(new ItemPresenter());

            for (Movies detail : result) {
                if (detail != null) {
                    arrayObjectAdapter.add(detail);
                }
            }

            if (arrayObjectAdapter.size() > 0) {
                String title = category.getTitle() != null ? category.getTitle() : "";
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
        void onClick(Movies model);
    }

    public interface OnSelectedListener {
        void onSelection(Movies model);
    }

    private class ItemViewSelectedListener implements OnItemViewSelectedListener {
        @Override
        public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
            if (item instanceof Movies) {
                if (itemSelectedListener != null) {
                    itemSelectedListener.onSelection((Movies) item);
                }
            }
        }
    }

    private class ItemViewClickListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
            if (item instanceof Movies) {
                if (itemClickListener != null) {
                    itemClickListener.onClick((Movies) item);
                }
            }
        }
    }

    public void requestFocusOnFirstItem() {
        VerticalGridView gridView = getVerticalGridView();
        if (gridView != null && gridView.getChildCount() > 0) {
            gridView.requestFocus();
        } else if (getView() != null) {
            getView().requestFocus();
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
