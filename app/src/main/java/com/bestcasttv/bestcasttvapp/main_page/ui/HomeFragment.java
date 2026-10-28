package com.bestcasttv.bestcasttvapp.main_page.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bestcasttv.bestcasttvapp.LoginActivity;
import com.bestcasttv.bestcasttvapp.R;
import com.bestcasttv.bestcasttvapp.api_services.ApiService;
import com.bestcasttv.bestcasttvapp.api_services.RetrofitClient;
import com.bestcasttv.bestcasttvapp.app_utils.AppUtils;
import com.bestcasttv.bestcasttvapp.app_utils.LoadingDialog;
import com.bestcasttv.bestcasttvapp.main_page.ListRowFragment;
import com.bestcasttv.bestcasttvapp.main_page.model.Movies;
import com.bestcasttv.bestcasttvapp.main_page.model.MoviesMainCategoryModel;
import com.bestcasttv.bestcasttvapp.main_page.model.Usermovies;
import com.bestcasttv.bestcasttvapp.move_details_page.MovieDetailsActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private static final String ARG_CATEGORY = "categoryName";
    private static final String ARG_PAGE = "pageId";

    // Bounded LRU in-memory cache to prevent memory leaks on low-RAM TV hardware
    private static final int MAX_CACHE_ENTRIES = 8;
    private static final Map<String, List<MoviesMainCategoryModel>> sCachedBlocksByPage =
            Collections.synchronizedMap(new LinkedHashMap<String, List<MoviesMainCategoryModel>>(MAX_CACHE_ENTRIES, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, List<MoviesMainCategoryModel>> eldest) {
                    return size() > MAX_CACHE_ENTRIES;
                }
            });

    private static final Map<String, BannerData> sCachedBannersByPage =
            Collections.synchronizedMap(new LinkedHashMap<String, BannerData>(MAX_CACHE_ENTRIES, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, BannerData> eldest) {
                    return size() > MAX_CACHE_ENTRIES;
                }
            });

    public static void clearCache() {
        sCachedBlocksByPage.clear();
        sCachedBannersByPage.clear();
    }

    private static class BannerData {
        String title = "";
        String certificate = "";
        String description = "";
        String imageUrl = "";
        String id = "";
        String videoUrl = "";
        String duration = "";
        String publishedDate = "";
    }

    private String mCategory = "";
    private String mPageId = "1";

    private TextView txtTitle;
    private TextView txtSubTitle;
    private TextView txtDescription;
    private ImageView imgBanner;
    private ListRowFragment listFragment;

    private View heroActionsLayout;
    private View btnHeroPlay;
    private View btnHeroDetails;

    private View emptyErrorLayout;
    private TextView errorMessageText;
    private View btnRetry;

    private String _token = "";
    private String profileName = "";
    private String profilePicture = "";
    private String profileID = "";
    private String profilePictureID = "";

    private String _mainMovieTitle = "";
    private String _certificateText = "";
    private String _mainMoviePicture = "";
    private String _mainMovieId = "";
    private String _mainMovieUrl = "";
    private String _mainMovieCategory = "";
    private String _mainMovieDuration = "";
    private String _mainMoviePublishedDate = "";

    private Movies currentSelectedMovie = null;

    private List<MoviesMainCategoryModel> moviesMainCategoryModelList = new ArrayList<>();
    private MoviesMainCategoryModel continueWatchingBlock = null;

    private AppUtils appUtils;
    private LoadingDialog loadingDialog;
    private SharedPreferences sharedPreferences;

    // Debounce banner Glide image loading to prevent dropped frames during fast D-pad navigation
    private final Handler bannerHandler = new Handler(Looper.getMainLooper());
    private Runnable pendingBannerRunnable;
    private String currentLoadedBannerUrl = "";

    private final Pattern htmlTagPattern = Pattern.compile("<[^>]*>", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance(String categoryName, String pageID) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY, categoryName != null ? categoryName : "");
        args.putString(ARG_PAGE, (pageID != null && !pageID.isEmpty()) ? pageID : "1");
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCategory = getArguments().getString(ARG_CATEGORY, "");
            mPageId = getArguments().getString(ARG_PAGE, "1");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        appUtils = new AppUtils(getContext());
        if (getActivity() != null) {
            loadingDialog = new LoadingDialog(getActivity());
            sharedPreferences = getActivity().getSharedPreferences(getString(R.string.shared_name), Context.MODE_PRIVATE);
            _token = sharedPreferences.getString(getString(R.string.token), "");
            profileName = sharedPreferences.getString(getString(R.string.profileName), "");
            profilePicture = sharedPreferences.getString(getString(R.string.profilePicture), "");
            profileID = sharedPreferences.getString(getString(R.string.profileID), "");
            profilePictureID = sharedPreferences.getString(getString(R.string.profilePictureID), "");
        }

        init(view);

        String cacheKey = getCacheKey();
        boolean hasCache = sCachedBlocksByPage.containsKey(cacheKey) && !sCachedBlocksByPage.get(cacheKey).isEmpty();

        if (hasCache) {
            moviesMainCategoryModelList = new ArrayList<>(sCachedBlocksByPage.get(cacheKey));
            if (listFragment != null) {
                listFragment.bindData(moviesMainCategoryModelList);
            }

            BannerData cachedBanner = sCachedBannersByPage.get(cacheKey);
            if (cachedBanner != null) {
                applyBannerData(cachedBanner);
            }
            view.post(() -> {
                if (getActivity() instanceof com.bestcasttv.bestcasttvapp.main_page.MainPageActivity) {
                    com.bestcasttv.bestcasttvapp.main_page.MainPageActivity act = (com.bestcasttv.bestcasttvapp.main_page.MainPageActivity) getActivity();
                    if (act.isSideMenuOpen() || act.isFocusInNavBar()) {
                        return;
                    }
                }
                requestFocusOnContent();
            });
        }

        String effectivePageId = (mPageId != null && !mPageId.isEmpty()) ? mPageId : "1";
        getBannerMoviesDetails(_token, profileID, mCategory, effectivePageId, !hasCache);
    }

    private String getCacheKey() {
        return (profileID != null ? profileID : "") + "_" + (mCategory != null ? mCategory : "") + "_" + (mPageId != null ? mPageId : "1");
    }

    public void init(View view) {
        imgBanner = view.findViewById(R.id.img_banner);
        txtTitle = view.findViewById(R.id.title);
        txtSubTitle = view.findViewById(R.id.subtitle);
        txtDescription = view.findViewById(R.id.description);

        heroActionsLayout = view.findViewById(R.id.hero_actions_layout);
        btnHeroPlay = view.findViewById(R.id.btn_hero_play);
        btnHeroDetails = view.findViewById(R.id.btn_hero_details);

        emptyErrorLayout = view.findViewById(R.id.empty_error_layout);
        errorMessageText = view.findViewById(R.id.error_message_text);
        btnRetry = view.findViewById(R.id.btn_retry);

        if (btnHeroPlay != null) {
            btnHeroPlay.setOnClickListener(v -> {
                String targetId = (currentSelectedMovie != null && currentSelectedMovie.getId() != null)
                        ? currentSelectedMovie.getId() : _mainMovieId;
                if (targetId != null && !targetId.isEmpty()) {
                    getTokenValid(_token, targetId, "", "");
                }
            });
        }

        if (btnHeroDetails != null) {
            btnHeroDetails.setOnClickListener(v -> {
                String targetId = (currentSelectedMovie != null && currentSelectedMovie.getId() != null)
                        ? currentSelectedMovie.getId() : _mainMovieId;
                if (targetId != null && !targetId.isEmpty() && getContext() != null) {
                    Intent intent = new Intent(getContext(), MovieDetailsActivity.class);
                    intent.putExtra("movieId", targetId);
                    startActivity(intent);
                }
            });
        }

        if (btnRetry != null) {
            btnRetry.setOnClickListener(v -> {
                if (emptyErrorLayout != null) emptyErrorLayout.setVisibility(View.GONE);
                String effectivePageId = (mPageId != null && !mPageId.isEmpty()) ? mPageId : "1";
                getBannerMoviesDetails(_token, profileID, mCategory, effectivePageId, true);
            });
        }

        // Safe child fragment reuse to avoid transaction leaks on view recreation
        listFragment = (ListRowFragment) getChildFragmentManager().findFragmentById(R.id.list_fragment);
        if (listFragment == null) {
            listFragment = new ListRowFragment();
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.replace(R.id.list_fragment, listFragment);
            transaction.commitAllowingStateLoss();
        }

        listFragment.setOnContentSelectedListener(this::updateOnSelectionBannerDebounced);

        listFragment.setOnItemClickListener(item -> {
            if (item == null) return;
            String getWatchedPercent = "";
            String getWatchingTime = "";
            if (item.getUsermovies() != null) {
                getWatchedPercent = item.getUsermovies().getWatchedPercent() != null ? item.getUsermovies().getWatchedPercent() : "";
                getWatchingTime = item.getUsermovies().getWatchTime() != null ? item.getUsermovies().getWatchTime() : "";
            }

            getTokenValid(_token, item.getId(), getWatchedPercent, getWatchingTime);
        });
    }

    public boolean requestFocusOnContent() {
        if (listFragment != null && listFragment.hasItems()) {
            listFragment.requestFocusOnFirstItem();
            return true;
        } else if (btnHeroPlay != null && btnHeroPlay.getVisibility() == View.VISIBLE) {
            return btnHeroPlay.requestFocus();
        } else if (heroActionsLayout != null && heroActionsLayout.getVisibility() == View.VISIBLE) {
            return heroActionsLayout.requestFocus();
        } else if (listFragment != null && listFragment.getView() != null && listFragment.getView().getVisibility() == View.VISIBLE) {
            listFragment.requestFocusOnFirstItem();
            return true;
        } else if (getView() != null) {
            return getView().requestFocus();
        }
        return false;
    }

    private void dismissLoading() {
        if (loadingDialog != null && getActivity() != null && !getActivity().isFinishing()) {
            try {
                loadingDialog.cancel();
            } catch (Exception ignored) {}
        }
        if (getView() != null) {
            getView().post(() -> {
                if (getActivity() != null && !getActivity().isFinishing()) {
                    if (getActivity() instanceof com.bestcasttv.bestcasttvapp.main_page.MainPageActivity) {
                        com.bestcasttv.bestcasttvapp.main_page.MainPageActivity act = (com.bestcasttv.bestcasttvapp.main_page.MainPageActivity) getActivity();
                        if (act.isSideMenuOpen() || act.isFocusInNavBar()) {
                            return;
                        }
                    }
                    requestFocusOnContent();
                }
            });
        }
    }

    private void showLoading() {
        if (loadingDialog != null && getActivity() != null && !getActivity().isFinishing()) {
            try {
                loadingDialog.show();
            } catch (Exception ignored) {}
        }
    }

    private void getTokenValid(String token, String _id, String _getWatchedPercent, String _getWatchingTime) {
        showLoading();
        ApiService apiService = RetrofitClient.getApiService(token);
        Call<JsonObject> call = apiService.getTokenexist();

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                dismissLoading();
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().toString());
                        String status = jsonObject.optString("status", "");

                        if ("error".equals(status)) {
                            if (sharedPreferences != null) {
                                sharedPreferences.edit().clear().apply();
                            }
                            if (getActivity() != null && !getActivity().isFinishing()) {
                                Intent mainIntent = new Intent(getActivity(), LoginActivity.class);
                                getActivity().finishAffinity();
                                startActivity(mainIntent);
                            }
                        } else {
                            if (isAdded() && getContext() != null) {
                                Intent intent = new Intent(getContext(), MovieDetailsActivity.class);
                                intent.putExtra("movieId", _id != null ? _id : "");
                                intent.putExtra("WatchedPercent", _getWatchedPercent);
                                intent.putExtra("WatchingTime", _getWatchingTime);
                                startActivity(intent);
                            }
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing token response: " + e.getMessage());
                        if (appUtils != null) appUtils.showToast("Response error.");
                    }
                } else {
                    if (appUtils != null) appUtils.showToast("Input Error.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                dismissLoading();
                Log.e(TAG, "getTokenexist failed: " + t.getMessage());
                if (appUtils != null) appUtils.showToast("Network error.");
            }
        });
    }

    private void updateOnSelectionBannerDebounced(Movies movies) {
        if (movies == null) return;
        currentSelectedMovie = movies;

        // Immediately update text views for responsive remote feel
        if (txtTitle != null) txtTitle.setText(movies.getTitle() != null ? movies.getTitle() : "");
        if (txtSubTitle != null) {
            txtSubTitle.setText(formatSubtitle(movies.getPublishedDate(), movies.getCertificate(), movies.getDuration()));
        }

        String rawTag = movies.getTagText() != null ? movies.getTagText() : "";
        String resultTagText = htmlTagPattern.matcher(rawTag).replaceAll("  ");
        if (txtDescription != null) txtDescription.setText(resultTagText);

        // Debounce large Glide banner image decoding to avoid D-pad navigation stutter
        if (pendingBannerRunnable != null) {
            bannerHandler.removeCallbacks(pendingBannerRunnable);
        }

        String imageUrl = movies.getThumbnail();
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            imageUrl = movies.getPortrait();
        }

        final String finalImageUrl = imageUrl;
        pendingBannerRunnable = () -> loadBannerImageSafely(finalImageUrl);
        bannerHandler.postDelayed(pendingBannerRunnable, 150);
    }

    private String formatSubtitle(String releaseDate, String certificate, String duration) {
        StringBuilder sb = new StringBuilder();
        if (releaseDate != null && !releaseDate.trim().isEmpty()) {
            try {
                if (appUtils != null) {
                    String year = appUtils.getDateTimeToYear(releaseDate);
                    if (year != null && !year.trim().isEmpty()) {
                        sb.append(year.trim());
                    }
                }
            } catch (Exception ignored) {
                if (releaseDate.length() >= 4) {
                    sb.append(releaseDate.substring(0, 4));
                }
            }
        }
        if (certificate != null && !certificate.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(" • ");
            sb.append(certificate.trim());
        }
        if (duration != null && !duration.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(" • ");
            sb.append(duration.trim());
        }
        return sb.toString();
    }

    private void loadBannerImageSafely(String url) {
        if (url == null || url.trim().isEmpty() || url.equals(currentLoadedBannerUrl)) return;
        if (!isAdded() || getContext() == null || getActivity() == null || getActivity().isFinishing()) return;

        currentLoadedBannerUrl = url;
        try {
            // Constrain decoded bitmap size to 1280x720 to save native memory on TV devices
            Glide.with(this)
                    .load(url)
                    .override(1280, 720)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.default_landscape)
                    .error(R.drawable.default_landscape)
                    .into(imgBanner);
        } catch (Exception e) {
            Log.e(TAG, "Error loading banner image: " + e.getMessage());
        }
    }

    private void applyBannerData(BannerData data) {
        if (data == null) return;
        _mainMovieTitle = data.title;
        _certificateText = data.certificate;
        _mainMovieCategory = data.description;
        _mainMoviePicture = data.imageUrl;
        _mainMovieId = data.id;
        _mainMovieUrl = data.videoUrl;
        _mainMovieDuration = data.duration;
        _mainMoviePublishedDate = data.publishedDate;

        updateBanner();
    }

    public void updateBanner() {
        if (txtTitle != null) txtTitle.setText(_mainMovieTitle != null ? _mainMovieTitle : "");
        if (txtSubTitle != null) {
            txtSubTitle.setText(formatSubtitle(_mainMoviePublishedDate, _certificateText, _mainMovieDuration));
        }
        if (txtDescription != null) txtDescription.setText(_mainMovieCategory != null ? _mainMovieCategory : "");

        loadBannerImageSafely(_mainMoviePicture);
    }

    private void getBannerMoviesDetails(String token, String profileId, String categoryID, String pageId, boolean showProgress) {
        if (showProgress) showLoading();

        ApiService apiService = RetrofitClient.getApiService(token);
        Call<JsonObject> call = apiService.getBannerlist(pageId, profileId);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().toString());
                        JSONObject dataObj = jsonObject.optJSONObject("data");
                        if (dataObj != null) {
                            JSONObject moviesObj = dataObj.optJSONObject("movies");
                            if (moviesObj != null) {
                                String movieTitle = moviesObj.optString("title", "");
                                String certificate = moviesObj.optString("certificate", "");
                                String tagText = moviesObj.optString("tag_text", "");
                                String videoUrl = moviesObj.optString("video_url", "");
                                String id = moviesObj.optString("id", "");
                                String duration = moviesObj.optString("duration", "");
                                String releaseDate = moviesObj.optString("release_date", "");

                                String imagePath = moviesObj.optString("image", "");
                                if (imagePath.isEmpty()) {
                                    imagePath = moviesObj.optString("thumbnail", "");
                                }
                                String imageUrl = imagePath.startsWith("http") ? imagePath : (RetrofitClient.BASE_URL + "/" + imagePath);

                                String resultTagText = htmlTagPattern.matcher(tagText).replaceAll("  ");

                                BannerData bannerData = new BannerData();
                                bannerData.title = movieTitle;
                                bannerData.certificate = certificate;
                                bannerData.description = resultTagText;
                                bannerData.imageUrl = imageUrl;
                                bannerData.id = id;
                                bannerData.videoUrl = videoUrl;
                                bannerData.duration = duration;
                                bannerData.publishedDate = releaseDate;

                                sCachedBannersByPage.put(getCacheKey(), bannerData);
                                applyBannerData(bannerData);
                            }
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "JsonBanner_Error: " + e.getMessage());
                    }
                }

                // Proceed to fetch Continue Watching and Blocks
                getUsermoviesWatchinglist(token, profileId, categoryID, pageId);
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                Log.e(TAG, "Banner_Error: " + t.getMessage());
                // Fall back to continue fetching blocks
                getUsermoviesWatchinglist(token, profileId, categoryID, pageId);
            }
        });
    }

    private void getUsermoviesWatchinglist(String token, String profileId, String categoryID, String pageId) {
        ApiService apiService = RetrofitClient.getApiService(token);
        Call<JsonObject> call = apiService.getUsermoviesWatchinglist(profileId, "1");
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                continueWatchingBlock = null;

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().toString());
                        JSONArray jsonArrayMovies = jsonObject.optJSONArray("data");

                        if (jsonArrayMovies != null && jsonArrayMovies.length() > 0) {
                            List<Movies> userWatchingList = new ArrayList<>();
                            for (int i = 0; i < jsonArrayMovies.length(); i++) {
                                JSONObject jsonObjectMovie = jsonArrayMovies.optJSONObject(i);
                                if (jsonObjectMovie == null) continue;

                                Movies movie = parseMovieJsonObject(jsonObjectMovie);
                                if (movie != null) {
                                    userWatchingList.add(movie);
                                }
                            }

                            if (!userWatchingList.isEmpty()) {
                                continueWatchingBlock = new MoviesMainCategoryModel(
                                        "0",
                                        "Continue watching",
                                        userWatchingList
                                );
                            }
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "MyList_Error: " + e.getMessage());
                    }
                }

                int screenId = 1;
                try {
                    screenId = Integer.parseInt(pageId);
                } catch (Exception ignored) {}

                getBlockMoviesList(token, profileId, categoryID, screenId);
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                Log.e(TAG, "MyList_Error: " + t.getMessage());
                int screenId = 1;
                try {
                    screenId = Integer.parseInt(pageId);
                } catch (Exception ignored) {}

                getBlockMoviesList(token, profileId, categoryID, screenId);
            }
        });
    }

    private void getBlockMoviesList(String token, String profileId, String categoryID, int screenId) {
        ApiService apiService = RetrofitClient.getApiService(token);
        Call<JsonObject> call = apiService.getBlockslist(screenId, profileId, categoryID);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                dismissLoading();

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().toString());
                        JSONArray jsonArray = jsonObject.optJSONArray("data");

                        List<MoviesMainCategoryModel> newCategoryList = new ArrayList<>();

                        // Prepend Continue Watching if available and not filtered by specific category
                        if (continueWatchingBlock != null && (categoryID == null || categoryID.isEmpty())) {
                            newCategoryList.add(continueWatchingBlock);
                        }

                        if (jsonArray != null) {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject responseCat = jsonArray.optJSONObject(i);
                                if (responseCat == null) continue;

                                String categoryTitle = responseCat.optString("title", "");
                                String categoryId = responseCat.optString("id", "");

                                // Exclude "webseries" block from the Home page (pageId = 1)
                                if (isHomePage() && categoryTitle != null && isWebSeriesTitle(categoryTitle)) {
                                    continue;
                                }

                                JSONArray jsonArrayMovies = responseCat.optJSONArray("movies");

                                List<Movies> categoryMovies = new ArrayList<>();
                                if (jsonArrayMovies != null) {
                                    for (int j = 0; j < jsonArrayMovies.length(); j++) {
                                        JSONObject jsonObjectMovie = jsonArrayMovies.optJSONObject(j);
                                        if (jsonObjectMovie == null) continue;

                                        Movies movie = parseMovieJsonObject(jsonObjectMovie);
                                        if (movie != null) {
                                            categoryMovies.add(movie);
                                        }
                                    }
                                }

                                if (!categoryMovies.isEmpty()) {
                                    newCategoryList.add(new MoviesMainCategoryModel(
                                            categoryId,
                                            categoryTitle,
                                            categoryMovies
                                    ));
                                }
                            }
                        }

                        moviesMainCategoryModelList = newCategoryList;
                        sCachedBlocksByPage.put(getCacheKey(), new ArrayList<>(newCategoryList));

                        if (listFragment != null) {
                            listFragment.bindData(moviesMainCategoryModelList);
                            if (getView() != null) {
                                getView().post(() -> {
                                    if (getActivity() instanceof com.bestcasttv.bestcasttvapp.main_page.MainPageActivity) {
                                        com.bestcasttv.bestcasttvapp.main_page.MainPageActivity act = (com.bestcasttv.bestcasttvapp.main_page.MainPageActivity) getActivity();
                                        if (act.isSideMenuOpen() || act.isFocusInNavBar()) {
                                            return;
                                        }
                                    }
                                    requestFocusOnContent();
                                });
                            }
                        }

                        if (emptyErrorLayout != null) {
                            emptyErrorLayout.setVisibility(moviesMainCategoryModelList.isEmpty() ? View.VISIBLE : View.GONE);
                        }

                    } catch (JSONException e) {
                        Log.e(TAG, "BlockList JSON error: " + e.getMessage());
                    }
                } else {
                    if (moviesMainCategoryModelList.isEmpty()) {
                        if (emptyErrorLayout != null) emptyErrorLayout.setVisibility(View.VISIBLE);
                        if (appUtils != null) appUtils.showToast("No data found.");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                dismissLoading();
                Log.e(TAG, "BlockList network error: " + t.getMessage());
                if (moviesMainCategoryModelList.isEmpty()) {
                    if (emptyErrorLayout != null) emptyErrorLayout.setVisibility(View.VISIBLE);
                    if (appUtils != null) appUtils.showToast("Network error.");
                }
            }
        });
    }

    private Movies parseMovieJsonObject(JSONObject jsonObjectMovie) {
        if (jsonObjectMovie == null) return null;

        String id = jsonObjectMovie.optString("id", "");
        String title = jsonObjectMovie.optString("title", "");
        String topten = jsonObjectMovie.optString("topten", "");
        String trailer = jsonObjectMovie.optString("trailer", "");
        String certificate = jsonObjectMovie.optString("certificate", "");
        String duration = jsonObjectMovie.optString("duration", "");
        String tagText = jsonObjectMovie.optString("tag_text", "");
        String publishedDate = jsonObjectMovie.optString("published_date", "");
        String userlist = jsonObjectMovie.optString("userlist", "0");
        String userlike = jsonObjectMovie.optString("userlike", "0");

        String rawImage = jsonObjectMovie.optString("image", "");
        String rawThumbnail = jsonObjectMovie.optString("thumbnail", "");
        String rawPortraitsmall = jsonObjectMovie.optString("portraitsmall", "");
        String rawPortrait = jsonObjectMovie.optString("portrait", "");

        String image = resolveUrl(rawImage);
        String thumbnail = resolveUrl(rawThumbnail);
        String portraitsmall = resolveUrl(rawPortraitsmall);
        String portrait = resolveUrl(rawPortrait);

        Usermovies usermovies = null;
        if (jsonObjectMovie.has("usermovies") && !jsonObjectMovie.isNull("usermovies")) {
            try {
                Object umRaw = jsonObjectMovie.get("usermovies");
                if (umRaw instanceof JSONObject) {
                    JSONObject umObj = (JSONObject) umRaw;
                    usermovies = new Usermovies(
                            umObj.optString("id", ""),
                            umObj.optString("movie_id", ""),
                            umObj.optString("mylist", "0"),
                            umObj.optString("likes", "0"),
                            umObj.optString("watch_time", "0"),
                            umObj.optString("watching", "0"),
                            umObj.optString("watched", "0"),
                            umObj.optString("watched_percent", "0"),
                            umObj.optString("viewed", "0")
                    );
                }
            } catch (Exception e) {
                Log.d(TAG, "Error parsing usermovies: " + e.getMessage());
            }
        }

        return new Movies(
                id,
                title,
                topten,
                trailer,
                certificate,
                duration,
                tagText,
                publishedDate,
                userlist,
                userlike,
                image.isEmpty() ? thumbnail : image,
                portraitsmall,
                portrait,
                usermovies
        );
    }

    private String resolveUrl(String rawPath) {
        if (rawPath == null || rawPath.trim().isEmpty()) return "";
        if (rawPath.startsWith("http://") || rawPath.startsWith("https://")) {
            return rawPath;
        }
        return RetrofitClient.BASE_URL + "/" + rawPath;
    }

    private boolean isHomePage() {
        return mPageId == null || mPageId.trim().isEmpty() || "1".equals(mPageId.trim());
    }

    private boolean isWebSeriesTitle(String title) {
        if (title == null) return false;
        String normalized = title.trim().toLowerCase().replaceAll("[\\s_-]", "");
        return normalized.contains("webseries") || normalized.contains("series");
    }

    @Override
    public void onDestroyView() {
        if (pendingBannerRunnable != null) {
            bannerHandler.removeCallbacks(pendingBannerRunnable);
            pendingBannerRunnable = null;
        }
        bannerHandler.removeCallbacksAndMessages(null);

        dismissLoading();
        loadingDialog = null;

        if (imgBanner != null) {
            try {
                Glide.with(this).clear(imgBanner);
            } catch (Exception ignored) {}
            imgBanner = null;
        }

        txtTitle = null;
        txtSubTitle = null;
        txtDescription = null;
        heroActionsLayout = null;
        btnHeroPlay = null;
        btnHeroDetails = null;
        emptyErrorLayout = null;
        errorMessageText = null;
        btnRetry = null;
        listFragment = null;

        super.onDestroyView();
    }
}
