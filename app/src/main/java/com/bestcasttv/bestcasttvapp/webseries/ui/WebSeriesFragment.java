package com.bestcasttv.bestcasttvapp.webseries.ui;

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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.bestcasttv.bestcasttvapp.app_utils.LoadingDialog;
import com.bestcasttv.bestcasttvapp.webseries.model.WebSeriesBlockModel;
import com.bestcasttv.bestcasttvapp.webseries.model.WebSeriesModel;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WebSeriesFragment extends Fragment {

    // In-memory cache for optimistic instant loading
    private static List<WebSeriesBlockModel> sCachedBlockList = null;
    private static WebSeriesModel sCachedSelectedSeries = null;

    private TextView txtTitle;
    private TextView txtSubTitle;
    private TextView txtDescription;
    private ImageView imgBanner;
    private ProgressBar progressLoading;

    private LinearLayout heroActionsLayout;
    private TextView btnHeroPlay;
    private TextView btnHeroDetails;

    private LinearLayout emptyErrorLayout;
    private Button btnRetry;

    private WebSeriesListRowFragment listFragment;
    private AppUtils appUtils;
    private LoadingDialog loadingDialog;
    private SharedPreferences sharedPreferences;

    private String token = "";
    private String profileID = "";

    // Debounce banner Glide image loading to prevent dropped frames during fast D-pad navigation
    private final Handler bannerHandler = new Handler(Looper.getMainLooper());
    private Runnable pendingBannerRunnable;
    private String currentLoadedBannerUrl = "";

    public WebSeriesFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_web_series, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        appUtils = new AppUtils(getContext());
        loadingDialog = new LoadingDialog(getActivity());

        if (getActivity() != null) {
            sharedPreferences = getActivity().getSharedPreferences(getString(R.string.shared_name), Context.MODE_PRIVATE);
            token = sharedPreferences.getString(getString(R.string.token), "");
            profileID = sharedPreferences.getString(getString(R.string.profileID), "");
        }

        txtTitle = view.findViewById(R.id.title);
        txtSubTitle = view.findViewById(R.id.subtitle);
        txtDescription = view.findViewById(R.id.description);
        imgBanner = view.findViewById(R.id.img_banner);
        progressLoading = view.findViewById(R.id.progress_loading_webseries);

        heroActionsLayout = view.findViewById(R.id.hero_actions_layout);
        btnHeroPlay = view.findViewById(R.id.btn_hero_play);
        btnHeroDetails = view.findViewById(R.id.btn_hero_details);

        emptyErrorLayout = view.findViewById(R.id.empty_error_layout);
        btnRetry = view.findViewById(R.id.btn_retry);

        View.OnClickListener actionClickListener = v -> {
            WebSeriesModel target = sCachedSelectedSeries != null ? sCachedSelectedSeries : findFirstSeries(sCachedBlockList);
            if (target != null && target.getId() != null && getContext() != null) {
                Intent intent = new Intent(getContext(), WebSeriesDetailsActivity.class);
                intent.putExtra("webseries_id", target.getId());
                intent.putExtra("webseries_data", target);
                startActivity(intent);
            }
        };

        if (btnHeroPlay != null) btnHeroPlay.setOnClickListener(actionClickListener);
        if (btnHeroDetails != null) btnHeroDetails.setOnClickListener(actionClickListener);

        if (btnRetry != null) {
            btnRetry.setOnClickListener(v -> {
                if (emptyErrorLayout != null) emptyErrorLayout.setVisibility(View.GONE);
                loadWebSeriesBlocks();
            });
        }

        listFragment = new WebSeriesListRowFragment();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.list_fragment, listFragment);
        transaction.commit();

        listFragment.setOnContentSelectedListener(this::updateBanner);
        listFragment.setOnItemClickListener(series -> {
            if (series == null || series.getId() == null) return;
            Intent intent = new Intent(getContext(), WebSeriesDetailsActivity.class);
            intent.putExtra("webseries_id", series.getId());
            intent.putExtra("webseries_data", series);
            startActivity(intent);
        });

        // Optimistic UI: Immediately render from cache if available (0ms delay)
        if (sCachedBlockList != null && !sCachedBlockList.isEmpty()) {
            listFragment.bindData(sCachedBlockList);
            if (sCachedSelectedSeries != null) {
                updateBannerImmediate(sCachedSelectedSeries);
            } else {
                WebSeriesModel first = findFirstSeries(sCachedBlockList);
                if (first != null) updateBannerImmediate(first);
            }
        }

        loadWebSeriesBlocks();
    }

    private WebSeriesModel findFirstSeries(List<WebSeriesBlockModel> blocks) {
        if (blocks == null) return null;
        for (WebSeriesBlockModel block : blocks) {
            if (block != null && block.getWebseriesList() != null && !block.getWebseriesList().isEmpty()) {
                WebSeriesModel series = block.getWebseriesList().get(0);
                if (series != null) return series;
            }
        }
        return null;
    }

    private void loadWebSeriesBlocks() {
        boolean hasCache = (sCachedBlockList != null && !sCachedBlockList.isEmpty());

        if (!hasCache) {
            if (loadingDialog != null && getActivity() != null && !getActivity().isFinishing()) {
                loadingDialog.show();
            }
        } else {
            if (progressLoading != null) progressLoading.setVisibility(View.VISIBLE);
        }

        ApiService apiService = RetrofitClient.getApiService(token);
        Call<JsonObject> call = apiService.getWebseriesBlockslist("1", profileID, "0", "", "");

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (loadingDialog != null && getActivity() != null && !getActivity().isFinishing()) {
                    loadingDialog.cancel();
                }
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
                if (progressLoading != null) progressLoading.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().toString());
                        JSONArray dataArray = jsonObject.optJSONArray("data");

                        if (dataArray != null) {
                            Gson gson = new Gson();
                            Type listType = new TypeToken<List<WebSeriesBlockModel>>() {}.getType();
                            List<WebSeriesBlockModel> blockList = gson.fromJson(dataArray.toString(), listType);

                            if (blockList != null && !blockList.isEmpty()) {
                                sCachedBlockList = blockList;
                                if (listFragment != null) {
                                    listFragment.bindData(blockList);
                                }
                                if (emptyErrorLayout != null) emptyErrorLayout.setVisibility(View.GONE);
                                if (sCachedSelectedSeries == null) {
                                    WebSeriesModel initialSeries = findFirstSeries(blockList);
                                    if (initialSeries != null) {
                                        updateBannerImmediate(initialSeries);
                                    }
                                }
                            } else {
                                if (emptyErrorLayout != null) emptyErrorLayout.setVisibility(View.VISIBLE);
                            }
                        } else {
                            if (emptyErrorLayout != null) emptyErrorLayout.setVisibility(View.VISIBLE);
                        }
                    } catch (Exception e) {
                        Log.e("WebSeriesFragment", "Error parsing blocks", e);
                        if (emptyErrorLayout != null && (sCachedBlockList == null || sCachedBlockList.isEmpty())) {
                            emptyErrorLayout.setVisibility(View.VISIBLE);
                        }
                    }
                } else {
                    if (appUtils != null && !hasCache) appUtils.showToast("Failed to load Web Series");
                    if (emptyErrorLayout != null && (sCachedBlockList == null || sCachedBlockList.isEmpty())) {
                        emptyErrorLayout.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (loadingDialog != null && getActivity() != null && !getActivity().isFinishing()) {
                    loadingDialog.cancel();
                }
                if (progressLoading != null) progressLoading.setVisibility(View.GONE);
                Log.e("WebSeriesFragment", "Network error", t);
                if (appUtils != null && !hasCache) appUtils.showToast("Network error.");
                if (emptyErrorLayout != null && (sCachedBlockList == null || sCachedBlockList.isEmpty())) {
                    emptyErrorLayout.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    /**
     * Updates text immediately for 0ms visual feedback, then debounces image loading
     * by 120ms to keep scrolling buttery-smooth at 60fps on Android TV.
     */
    private void updateBanner(WebSeriesModel series) {
        if (series == null) return;
        sCachedSelectedSeries = series;

        updateTextMetadata(series);

        // Debounce Glide network/disk image request
        if (pendingBannerRunnable != null) {
            bannerHandler.removeCallbacks(pendingBannerRunnable);
        }
        pendingBannerRunnable = () -> loadBannerImage(series);
        bannerHandler.postDelayed(pendingBannerRunnable, 120);
    }

    private void updateBannerImmediate(WebSeriesModel series) {
        if (series == null) return;
        sCachedSelectedSeries = series;
        updateTextMetadata(series);
        loadBannerImage(series);
    }

    private void updateTextMetadata(WebSeriesModel series) {
        if (getActivity() == null || !isAdded()) return;

        if (txtTitle != null) {
            String title = series.getTitle();
            txtTitle.setText(title != null ? title : "Web Series");
        }

        if (txtSubTitle != null) {
            StringBuilder subtitle = new StringBuilder();
            if (series.getCertificate() != null && !series.getCertificate().trim().isEmpty()) {
                subtitle.append(series.getCertificate().trim());
            }
            if (series.getYear() != null && !series.getYear().trim().isEmpty()) {
                if (subtitle.length() > 0) subtitle.append(" • ");
                subtitle.append(series.getYear().trim());
            }
            if (series.getSeasons() != null && !series.getSeasons().isEmpty()) {
                int count = series.getSeasons().size();
                if (subtitle.length() > 0) subtitle.append(" • ");
                subtitle.append(count).append(count > 1 ? " Seasons" : " Season");
            } else if (series.getDuration() != null && !series.getDuration().trim().isEmpty()) {
                if (subtitle.length() > 0) subtitle.append(" • ");
                subtitle.append(series.getDuration().trim());
            }
            txtSubTitle.setText(subtitle.toString());
        }

        if (txtDescription != null) {
            String content = series.getContent();
            if (content == null || content.trim().isEmpty()) {
                content = series.getTagText();
            }
            if (content != null) {
                content = content.replaceAll("<[^>]*>", "").trim();
            }
            txtDescription.setText(content != null ? content : "");
        }
    }

    private void loadBannerImage(WebSeriesModel series) {
        if (getActivity() == null || !isAdded() || imgBanner == null || getContext() == null) return;

        String bannerUrl = series.getImage();
        if (bannerUrl == null || bannerUrl.isEmpty()) {
            bannerUrl = series.getThumbnail();
        }

        if (bannerUrl != null && !bannerUrl.startsWith("http")) {
            bannerUrl = RetrofitClient.BASE_URL + "/" + bannerUrl;
        }

        if (bannerUrl == null || bannerUrl.isEmpty() || bannerUrl.equals(currentLoadedBannerUrl)) {
            return;
        }

        currentLoadedBannerUrl = bannerUrl;

        Glide.with(this)
                .load(bannerUrl)
                .transition(DrawableTransitionOptions.withCrossFade(250))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(imgBanner.getDrawable())
                .error(R.drawable.default_landscape)
                .into(imgBanner);
    }

    public boolean requestFocusOnContent() {
        if (listFragment != null && listFragment.hasItems()) {
            listFragment.requestFocusOnFirstItem();
            return true;
        } else if (btnHeroPlay != null && btnHeroPlay.getVisibility() == View.VISIBLE) {
            return btnHeroPlay.requestFocus();
        } else if (heroActionsLayout != null && heroActionsLayout.getVisibility() == View.VISIBLE) {
            return heroActionsLayout.requestFocus();
        } else if (listFragment != null) {
            listFragment.requestFocusOnFirstItem();
            return true;
        } else if (getView() != null) {
            return getView().requestFocus();
        }
        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (pendingBannerRunnable != null) {
            bannerHandler.removeCallbacks(pendingBannerRunnable);
        }
        if (loadingDialog != null) {
            loadingDialog.cancel();
        }
    }
}
