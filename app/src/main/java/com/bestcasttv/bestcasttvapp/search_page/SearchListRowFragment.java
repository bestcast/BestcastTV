package com.bestcasttv.bestcasttvapp.search_page;

import android.os.Build;
import android.os.Bundle;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.View;

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

import com.bestcasttv.bestcasttvapp.R;
import com.bestcasttv.bestcasttvapp.main_page.model.Movies;
import com.bestcasttv.bestcasttvapp.main_page.model.MoviesMainCategoryModel;

import java.util.List;

public class SearchListRowFragment extends RowsSupportFragment {

    private OnSelectedListener itemSelectedListener;
    private OnClickListener itemClickListener;
    private final SearchMovieItemPresenter movieItemPresenter = new SearchMovieItemPresenter();

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

        // Configure D-pad focus searching on the BrowseFrameLayout
        if (view instanceof BrowseFrameLayout) {
            BrowseFrameLayout browseFrameLayout = (BrowseFrameLayout) view;
            browseFrameLayout.setOnFocusSearchListener((focused, direction) -> {
                if (direction == View.FOCUS_UP) {
                    if (getActivity() != null) {
                        View searchBar = getActivity().findViewById(R.id.searchText);
                        if (searchBar != null) {
                            return searchBar;
                        }
                    }
                } else if (direction == View.FOCUS_LEFT) {
                    View next = FocusFinder.getInstance().findNextFocus(browseFrameLayout, focused, View.FOCUS_LEFT);
                    if (next != null && next != focused) {
                        return next;
                    }
                    if (getActivity() != null) {
                        View sidebar = getActivity().findViewById(R.id.btn_search);
                        if (sidebar != null) {
                            return sidebar;
                        }
                    }
                }
                return null;
            });
        }

        // Also configure key listener on VerticalGridView as an extra layer of navigation reliability
        VerticalGridView gridView = getVerticalGridView();
        if (gridView != null) {
            gridView.setOnKeyListener((v, keyCode, event) -> {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        if (getActivity() != null) {
                            View searchBar = getActivity().findViewById(R.id.searchText);
                            if (searchBar != null) {
                                searchBar.requestFocus();
                                return true;
                            }
                        }
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        View currentFocus = gridView.findFocus();
                        if (currentFocus != null) {
                            View next = FocusFinder.getInstance().findNextFocus(gridView, currentFocus, View.FOCUS_LEFT);
                            if (next == null || next == currentFocus) {
                                if (getActivity() != null) {
                                    View sidebar = getActivity().findViewById(R.id.btn_search);
                                    if (sidebar != null) {
                                        sidebar.requestFocus();
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
                return false;
            });
        }
    }

    public boolean hasResults() {
        return rootAdapter != null && rootAdapter.size() > 0;
    }

    public boolean focusFirstItem() {
        if (!hasResults()) {
            return false;
        }
        VerticalGridView gridView = getVerticalGridView();
        if (gridView != null) {
            gridView.setSelectedPosition(0);
            boolean focused = gridView.requestFocus();
            if (!focused) {
                gridView.post(() -> {
                    gridView.setSelectedPosition(0);
                    gridView.requestFocus();
                });
            }
            return true;
        }
        View view = getView();
        if (view != null) {
            return view.requestFocus();
        }
        return false;
    }

    public void bindData(List<MoviesMainCategoryModel> dataList) {
        rootAdapter.clear();
        if (dataList == null || dataList.isEmpty()) {
            return;
        }

        for (int i = 0; i < dataList.size(); i++) {
            MoviesMainCategoryModel category = dataList.get(i);
            if (category == null || category.getMovies() == null || category.getMovies().isEmpty()) {
                continue;
            }

            List<Movies> result = category.getMovies();
            ArrayObjectAdapter arrayObjectAdapter = new ArrayObjectAdapter(movieItemPresenter);

            for (Movies detail : result) {
                if (detail != null) {
                    arrayObjectAdapter.add(detail);
                }
            }

            String title = category.getTitle();
            HeaderItem headerItem = new HeaderItem(title != null ? title : "");
            ListRow listRow = new ListRow(headerItem, arrayObjectAdapter);
            rootAdapter.add(listRow);
        }
    }

    public void clearData() {
        rootAdapter.clear();
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
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        itemSelectedListener.onSelection((Movies) item);
                    }
                }
            }
        }
    }

    private class ItemViewClickListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
            if (item instanceof Movies) {
                if (itemClickListener != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        itemClickListener.onClick((Movies) item);
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
