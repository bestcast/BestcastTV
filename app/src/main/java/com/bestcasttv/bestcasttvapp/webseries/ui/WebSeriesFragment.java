package com.bestcasttv.bestcasttvapp.webseries.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

import com.bestcasttv.bestcasttvapp.R;
import com.bestcasttv.bestcasttvapp.api_services.ApiService;
import com.bestcasttv.bestcasttvapp.api_services.RetrofitClient;
import com.bestcasttv.bestcasttvapp.app_utils.AppUtils;
import com.bestcasttv.bestcasttvapp.app_utils.LoadingDialog;
import com.bestcasttv.bestcasttvapp.webseries.model.WebSeriesBlockModel;
import com.bestcasttv.bestcasttvapp.webseries.model.WebSeriesModel;
import com.bumptech.glide.Glide;
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

    private TextView txtTitle;
    private TextView txtSubTitle;
    private TextView txtDescription;
    private ImageView imgBanner;

    private WebSeriesListRowFragment listFragment;
    private AppUtils appUtils;
    private LoadingDialog loadingDialog;
    private SharedPreferences sharedPreferences;

    private String token = "";
    private String profileID = "";

    public WebSeriesFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
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

        txtTitle.setText("Web Series");

        listFragment = new WebSeriesListRowFragment();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.list_fragment, listFragment);
        transaction.commit();

        listFragment.setOnContentSelectedListener(series -> updateBanner(series));
        listFragment.setOnItemClickListener(series -> {
            if (series == null || series.getId() == null) return;
            Intent intent = new Intent(getContext(), WebSeriesDetailsActivity.class);
            intent.putExtra("webseries_id", series.getId());
            startActivity(intent);
        });

        loadWebSeriesBlocks();
    }

    private void loadWebSeriesBlocks() {
        if (loadingDialog != null && getActivity() != null && !getActivity().isFinishing()) loadingDialog.show();

        ApiService apiService = RetrofitClient.getApiService(token);
        Call<JsonObject> call = apiService.getWebseriesBlockslist("1", profileID, "0", "", "");

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (loadingDialog != null && getActivity() != null && !getActivity().isFinishing()) loadingDialog.cancel();

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().toString());
                        JSONArray dataArray = jsonObject.optJSONArray("data");

                        if (dataArray != null) {
                            Gson gson = new Gson();
                            Type listType = new TypeToken<List<WebSeriesBlockModel>>() {}.getType();
                            List<WebSeriesBlockModel> blockList = gson.fromJson(dataArray.toString(), listType);

                            if (blockList != null && !blockList.isEmpty()) {
                                if (listFragment != null) {
                                    listFragment.bindData(blockList);
                                }
                                WebSeriesModel initialSeries = null;
                                for (WebSeriesBlockModel block : blockList) {
                                    if (block != null && block.getWebseriesList() != null && !block.getWebseriesList().isEmpty()) {
                                        initialSeries = block.getWebseriesList().get(0);
                                        if (initialSeries != null) break;
                                    }
                                }
                                if (initialSeries != null) {
                                    updateBanner(initialSeries);
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e("WebSeriesFragment", "Error parsing blocks", e);
                    }
                } else {
                    if (appUtils != null) appUtils.showToast("Failed to load Web Series");
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (loadingDialog != null && getActivity() != null && !getActivity().isFinishing()) loadingDialog.cancel();
                Log.e("WebSeriesFragment", "Network error", t);
                if (appUtils != null) appUtils.showToast("Network error.");
            }
        });
    }

    private void updateBanner(WebSeriesModel series) {
        if (series == null) return;
        if (getActivity() == null || !isAdded()) return;

        if (txtTitle != null) txtTitle.setText(series.getTitle());

        String certificate = series.getCertificate();
        if (txtSubTitle != null) {
            txtSubTitle.setText(certificate != null ? certificate : "");
        }

        String content = series.getContent();
        if (content != null) {
            content = content.replaceAll("<[^>]*>", "");
        }
        if (txtDescription != null) {
            txtDescription.setText(content != null ? content : "");
        }

        String bannerUrl = series.getImage();
        if (bannerUrl == null || bannerUrl.isEmpty()) {
            bannerUrl = series.getThumbnail();
        }

        if (bannerUrl != null && !bannerUrl.startsWith("http")) {
            bannerUrl = RetrofitClient.BASE_URL + "/" + bannerUrl;
        }

        if (imgBanner != null && getContext() != null) {
            Glide.with(getContext())
                    .load(bannerUrl)
                    .placeholder(R.drawable.default_landscape)
                    .error(R.drawable.default_landscape)
                    .into(imgBanner);
        }
    }
}
