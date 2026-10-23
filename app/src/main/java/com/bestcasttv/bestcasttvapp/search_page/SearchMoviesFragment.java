package com.bestcasttv.bestcasttvapp.search_page;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bestcasttv.bestcasttvapp.R;
import com.bestcasttv.bestcasttvapp.api_services.ApiService;
import com.bestcasttv.bestcasttvapp.api_services.RetrofitClient;
import com.bestcasttv.bestcasttvapp.app_utils.AppUtils;
import com.bestcasttv.bestcasttvapp.main_page.model.Movies;
import com.bestcasttv.bestcasttvapp.main_page.model.MoviesMainCategoryModel;
import com.bestcasttv.bestcasttvapp.main_page.model.Usermovies;
import com.bestcasttv.bestcasttvapp.move_details_page.MovieDetailsActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchMoviesFragment extends Fragment {

    private static final String TAG = "SearchMoviesFragment";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private EditText searchText;
    private ImageView imgBanner;
    private ProgressBar progressLoadingSearch;
    private View emptyResultsView;
    private TextView emptyText;
    private SearchListRowFragment listFragment;

    private String _token = "";
    private String profileName = "";
    private String profilePicture = "";
    private String profileID = "";
    private String profilePictureID = "";

    private AppUtils appUtils;
    private SharedPreferences sharedPreferences;

    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private final Handler bannerHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private Runnable bannerRunnable;
    private Call<JsonObject> activeSearchCall;
    private String lastSearchedQuery = "";

    public SearchMoviesFragment() {
        // Required empty public constructor
    }

    public static SearchMoviesFragment newInstance(String param1, String param2) {
        SearchMoviesFragment fragment = new SearchMoviesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search_movies, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        appUtils = new AppUtils(getContext());

        if (getActivity() != null) {
            sharedPreferences = getActivity().getSharedPreferences(getString(R.string.shared_name), Context.MODE_PRIVATE);
            _token = sharedPreferences.getString(getString(R.string.token), "");
            profileName = sharedPreferences.getString(getString(R.string.profileName), "");
            profilePicture = sharedPreferences.getString(getString(R.string.profilePicture), "");
            profileID = sharedPreferences.getString(getString(R.string.profileID), "");
            profilePictureID = sharedPreferences.getString(getString(R.string.profilePictureID), "");
        }

        init(view);
    }

    public void init(View view) {
        imgBanner = view.findViewById(R.id.img_banner);
        searchText = view.findViewById(R.id.searchText);
        progressLoadingSearch = view.findViewById(R.id.progress_loading_search);
        emptyResultsView = view.findViewById(R.id.empty_results_view);
        emptyText = view.findViewById(R.id.empty_text);

        listFragment = new SearchListRowFragment();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.list_fragment, listFragment);
        transaction.commit();

        listFragment.setOnContentSelectedListener(new SearchListRowFragment.OnSelectedListener() {
            @Override
            public void onSelection(Movies model) {
                updateOnSelectionBanner(model);
            }
        });

        listFragment.setOnItemClickListener(item -> {
            if (item != null && item.getId() != null) {
                Intent intent = new Intent(requireContext(), MovieDetailsActivity.class);
                intent.putExtra("movieId", item.getId());
                startActivity(intent);
            }
        });

        // Key and D-pad handling for Search Input field
        searchText.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                    if (listFragment != null && listFragment.hasResults()) {
                        hideKeyboard(getContext(), searchText);
                        return listFragment.focusFirstItem();
                    }
                    return false;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                    if (searchText.getText() == null || searchText.getText().length() == 0 || searchText.getSelectionStart() <= 0) {
                        hideKeyboard(getContext(), searchText);
                        if (getActivity() != null) {
                            View btnSearch = getActivity().findViewById(R.id.btn_search);
                            if (btnSearch != null) {
                                btnSearch.requestFocus();
                                return true;
                            }
                        }
                    }
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
                    final String query = searchText.getText() != null ? searchText.getText().toString().trim() : "";
                    hideKeyboard(getContext(), searchText);
                    if (!query.isEmpty()) {
                        if (!query.equals(lastSearchedQuery)) {
                            if (searchRunnable != null) {
                                searchHandler.removeCallbacks(searchRunnable);
                            }
                            performSearch(query);
                        } else if (listFragment != null && listFragment.hasResults()) {
                            return listFragment.focusFirstItem();
                        }
                    } else {
                        showKeyboard(getContext(), searchText);
                    }
                    return true;
                }
            }
            return false;
        });

        searchText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH 
                    || actionId == EditorInfo.IME_ACTION_DONE 
                    || actionId == EditorInfo.IME_NULL) {
                hideKeyboard(getContext(), searchText);
                final String query = searchText.getText() != null ? searchText.getText().toString().trim() : "";
                if (!query.isEmpty() && !query.equals(lastSearchedQuery)) {
                    if (searchRunnable != null) {
                        searchHandler.removeCallbacks(searchRunnable);
                    }
                    performSearch(query);
                } else if (listFragment != null && listFragment.hasResults()) {
                    listFragment.focusFirstItem();
                }
                return true;
            }
            return false;
        });

        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                final String query = s != null ? s.toString().trim() : "";

                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }

                if (query.isEmpty()) {
                    cancelActiveCall();
                    if (progressLoadingSearch != null) {
                        progressLoadingSearch.setVisibility(View.GONE);
                    }
                    if (emptyResultsView != null) {
                        emptyResultsView.setVisibility(View.GONE);
                    }
                    if (listFragment != null) {
                        listFragment.clearData();
                    }
                    lastSearchedQuery = "";
                    return;
                }

                // Debounce search by 350ms to prevent spamming backend and eliminating race conditions
                searchRunnable = () -> performSearch(query);
                searchHandler.postDelayed(searchRunnable, 350);
            }
        });

        searchText.requestFocus();
        showKeyboard(getContext(), searchText);
    }

    public boolean handleBackPressed() {
        // If movie list or any child in the list has focus, return focus to search bar
        if (listFragment != null && listFragment.getView() != null && listFragment.getView().hasFocus()) {
            if (searchText != null) {
                searchText.requestFocus();
                return true;
            }
        }
        // If search bar has text, clear text
        if (searchText != null && searchText.getText() != null && searchText.getText().length() > 0) {
            searchText.setText("");
            searchText.requestFocus();
            return true;
        }
        // If search bar is empty and not on sidebar, move to sidebar
        if (getActivity() != null) {
            View btnSearch = getActivity().findViewById(R.id.btn_search);
            if (btnSearch != null && !btnSearch.hasFocus()) {
                btnSearch.requestFocus();
                return true;
            }
        }
        return false;
    }

    public static void showKeyboard(Context context, View view) {
        if (context == null || view == null) return;
        view.requestFocus();
        view.postDelayed(() -> {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 100);
    }

    public static void hideKeyboard(Context context, View view) {
        if (context == null || view == null) return;
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void updateOnSelectionBanner(Movies movies) {
        if (movies == null || imgBanner == null || !isAdded()) return;

        if (bannerRunnable != null) {
            bannerHandler.removeCallbacks(bannerRunnable);
        }

        // Debounce backdrop switching by 150ms for smooth D-pad scrolling
        bannerRunnable = () -> {
            if (!isAdded() || getActivity() == null || getActivity().isFinishing()) return;

            String bannerUrl = movies.getThumbnail();
            if (bannerUrl == null || bannerUrl.trim().isEmpty()) {
                bannerUrl = movies.getPortrait();
            }

            Glide.with(SearchMoviesFragment.this)
                    .load(bannerUrl)
                    .override(1280, 720)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .transition(DrawableTransitionOptions.withCrossFade(250))
                    .placeholder(R.drawable.default_landscape)
                    .error(R.drawable.default_landscape)
                    .into(imgBanner);
        };

        bannerHandler.postDelayed(bannerRunnable, 150);
    }

    private void performSearch(String query) {
        cancelActiveCall();
        lastSearchedQuery = query;

        if (progressLoadingSearch != null) {
            progressLoadingSearch.setVisibility(View.VISIBLE);
        }
        if (emptyResultsView != null) {
            emptyResultsView.setVisibility(View.GONE);
        }

        ApiService apiService = RetrofitClient.getApiService(_token);
        activeSearchCall = apiService.getSearchMovieslist(query, profileID);
        activeSearchCall.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (call.isCanceled() || !isAdded()) return;

                if (progressLoadingSearch != null) {
                    progressLoadingSearch.setVisibility(View.GONE);
                }

                if (response.isSuccessful() && response.body() != null) {
                    parseAndDisplayResults(response.body(), query);
                } else {
                    showEmptyState(query);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                if (call.isCanceled() || !isAdded()) return;

                if (progressLoadingSearch != null) {
                    progressLoadingSearch.setVisibility(View.GONE);
                }
                Log.e(TAG, "Search network error: ", t);
                showEmptyState(query);
            }
        });
    }

    private void parseAndDisplayResults(JsonObject data, String query) {
        try {
            JSONObject jsonObject = new JSONObject(data.toString());
            JSONArray jsonArrayMovies = jsonObject.optJSONArray("data");

            if (jsonArrayMovies == null || jsonArrayMovies.length() == 0) {
                showEmptyState(query);
                return;
            }

            List<Movies> moviesSearchModel = new ArrayList<>();
            for (int i = 0; i < jsonArrayMovies.length(); i++) {
                JSONObject jsonObjectMovie = jsonArrayMovies.optJSONObject(i);
                if (jsonObjectMovie == null) continue;

                String _image = buildImageUrl(jsonObjectMovie.optString("image", ""));
                String _thumbnail = buildImageUrl(jsonObjectMovie.optString("thumbnail", ""));
                String _portraitsmall = buildImageUrl(jsonObjectMovie.optString("portraitsmall", ""));
                String _portrait = buildImageUrl(jsonObjectMovie.optString("portrait", ""));

                Usermovies usermovies = null;
                JSONObject userMoviesJson = jsonObjectMovie.optJSONObject("usermovies");
                if (userMoviesJson != null) {
                    usermovies = new Usermovies(
                            userMoviesJson.optString("id", ""),
                            userMoviesJson.optString("movie_id", ""),
                            userMoviesJson.optString("mylist", ""),
                            userMoviesJson.optString("likes", ""),
                            userMoviesJson.optString("watch_time", ""),
                            userMoviesJson.optString("watching", ""),
                            userMoviesJson.optString("watched", ""),
                            userMoviesJson.optString("watched_percent", ""),
                            userMoviesJson.optString("viewed", "")
                    );
                }

                String backdrop = !_image.isEmpty() ? _image : _thumbnail;

                moviesSearchModel.add(new Movies(
                        jsonObjectMovie.optString("id", ""),
                        jsonObjectMovie.optString("title", ""),
                        jsonObjectMovie.optString("topten", ""),
                        jsonObjectMovie.optString("trailer", ""),
                        jsonObjectMovie.optString("certificate", ""),
                        jsonObjectMovie.optString("duration", ""),
                        jsonObjectMovie.optString("tag_text", ""),
                        jsonObjectMovie.optString("published_date", ""),
                        jsonObjectMovie.optString("published_date", ""),
                        jsonObjectMovie.optString("published_date", ""),
                        backdrop,
                        _portraitsmall,
                        _portrait,
                        usermovies
                ));
            }

            if (moviesSearchModel.isEmpty()) {
                showEmptyState(query);
            } else {
                if (emptyResultsView != null) {
                    emptyResultsView.setVisibility(View.GONE);
                }
                List<MoviesMainCategoryModel> categoryList = new ArrayList<>();
                categoryList.add(new MoviesMainCategoryModel(
                        "1",
                        "Results for \"" + query + "\" (" + moviesSearchModel.size() + ")",
                        moviesSearchModel
                ));
                if (listFragment != null) {
                    listFragment.bindData(categoryList);
                }

                // Pre-render the banner for the first result
                updateOnSelectionBanner(moviesSearchModel.get(0));
            }

        } catch (Exception e) {
            Log.e(TAG, "Search JSON parsing error", e);
            showEmptyState(query);
        }
    }

    private void showEmptyState(String query) {
        if (listFragment != null) {
            listFragment.clearData();
        }
        if (emptyResultsView != null) {
            emptyResultsView.setVisibility(View.VISIBLE);
            if (emptyText != null) {
                emptyText.setText("No movies found for \"" + query + "\"");
            }
        }
    }

    private String buildImageUrl(String path) {
        if (path == null || path.trim().isEmpty() || "null".equalsIgnoreCase(path.trim())) {
            return "";
        }
        String trimmed = path.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }
        if (trimmed.startsWith("/")) {
            return RetrofitClient.BASE_URL + trimmed;
        }
        return RetrofitClient.BASE_URL + "/" + trimmed;
    }

    private void cancelActiveCall() {
        if (activeSearchCall != null && !activeSearchCall.isCanceled()) {
            activeSearchCall.cancel();
            activeSearchCall = null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
        if (bannerRunnable != null) {
            bannerHandler.removeCallbacks(bannerRunnable);
        }
        cancelActiveCall();
    }
}
