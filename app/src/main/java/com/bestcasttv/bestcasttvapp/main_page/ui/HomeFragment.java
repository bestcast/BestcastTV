package com.bestcasttv.bestcasttvapp.main_page.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.leanback.widget.VerticalGridView;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bestcasttv.bestcasttvapp.LoginActivity;
import com.bumptech.glide.Glide;
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
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_CATEGORY = "categoryName";
    private static final String ARG_PAGE = "pageId";

    // TODO: Rename and change types of parameters
    private String mCategory = "";
    private String mPageId = "1";
    private String mParam2;

    TextView txtTitle;
    TextView txtSubTitle;
    TextView txtDescription;
    ImageView imgBanner;
    ListRowFragment listFragment;

    String _token = "";
    String profileName = "";
    String profilePicture = "";
    String profileID = "";
    String profilePictureID = "";

    String _mainMovieTitle = "";
    String _certificateText = "";
    String _mainMoviePicture = "";
    String _mainMovieId = "";
    String _mainMovieUrl = "";
    String _mainMovieCategory = "";

    List<MoviesMainCategoryModel> moviesMainCategoryModelList;
    boolean scrolleEnabled = false;
    boolean hasMoreScroll = true;
    int _page = 1;
    int limit = 0;
    int lastScrollPosition = 0;
    private int lastPosition = -1;

    AppUtils appUtils;
    LoadingDialog loadingDialog;
    SharedPreferences sharedPreferences;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param categoryName Parameter 1.
     * @param pageID
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String categoryName, String pageID) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY, categoryName);
        args.putString(ARG_PAGE, pageID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCategory = getArguments().getString(ARG_CATEGORY);
            mPageId = getArguments().getString(ARG_PAGE);
        }

    }

//    List<Movies> userMainCategoryModelList = new ArrayList<>();

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        appUtils = new AppUtils(getContext());
        loadingDialog = new LoadingDialog(getActivity());

        sharedPreferences = getActivity().getSharedPreferences(getString(R.string.shared_name), Context.MODE_PRIVATE);
        _token = sharedPreferences.getString(getString(R.string.token), "");

        profileName = sharedPreferences.getString(getString(R.string.profileName), "");
        profilePicture = sharedPreferences.getString(getString(R.string.profilePicture), "");
        profileID = sharedPreferences.getString(getString(R.string.profileID), "");
        profilePictureID = sharedPreferences.getString(getString(R.string.profilePictureID), "");

        moviesMainCategoryModelList = new ArrayList<>();
        Log.d("SelectedCategoryId", mCategory);
        getBannerMoviesDetails(_token, profileID, mCategory, "1");
        init(view);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_home, container, false);


    }

    public void init(View view) {
        imgBanner = view.findViewById(R.id.img_banner);
        txtTitle = view.findViewById(R.id.title);
        txtSubTitle = view.findViewById(R.id.subtitle);
        txtDescription = view.findViewById(R.id.description);

        listFragment = new ListRowFragment();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.list_fragment, listFragment);
        transaction.commit();

        listFragment.setOnContentSelectedListener(new ListRowFragment.OnSelectedListener() {
            @Override
            public void onSelection(Movies model) {
                updateOnSelectionBanner(model);
            }
        });

        listFragment.setOnItemClickListener(item -> {

            String getWatchedPercent = "";
            String getWatchingTime = "";
            if (item.getUsermovies() != null) {
                getWatchedPercent = item.getUsermovies().getWatchedPercent();
                getWatchingTime = item.getUsermovies().getWatchTime();
            }

            getTokenValid(_token,
                    item.getId(),
                    getWatchedPercent,
                    getWatchingTime
                    );



        });
    }

    void getTokenValid(String token, String _id, String _getWatchedPercent, String _getWatchingTime) {

        loadingDialog.show();
        ApiService apiService = RetrofitClient.getApiService(token);
        Call<JsonObject> call = apiService.getTokenexist();

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                Log.d("tokenexist_Response", response.body().toString());
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject data = response.body();
                    try {
                        JSONObject  jsonObject = new JSONObject(data.toString());
                        String status = jsonObject.getString("status");

                        Log.d("TokenStatus",status);
                        if ("error".equals(status)) {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.clear();
                            editor.commit();

                            Intent mainIntent = new Intent(getActivity(), LoginActivity.class);
                            getActivity().finishAffinity();
                            startActivity(mainIntent);
                        }else {
                            Intent intent = new Intent(requireContext(), MovieDetailsActivity.class);
                            intent.putExtra("movieId", _id);
                            intent.putExtra("WatchedPercent", _getWatchedPercent);
                            intent.putExtra("WatchingTime", _getWatchingTime);
                            startActivity(intent);
                        }

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }


                    loadingDialog.cancel();

                } else {
                    loadingDialog.cancel();
                    Log.d("tokenexist_Error", "");
                    appUtils.showToast("Input Error.");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d("tokenexist_Error", t.toString());
                loadingDialog.cancel();

                appUtils.showToast("Network error.");
            }
        });

    }

    public void updateOnSelectionBanner(Movies movies) {
        txtTitle.setText(movies.getTitle());
        txtSubTitle.setText(movies.getCertificate());

        Pattern exp = Pattern.compile("<[^>]*>", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
        String resultTagText = movies.getTagText().replaceAll(exp.pattern(), "  ");

        txtDescription.setText(resultTagText);

        Glide.with(this)
                .load(movies.getThumbnail())
                .placeholder(R.drawable.default_landscape)
                .error(R.drawable.default_landscape)
                .into(imgBanner);
    }

    public void updateBanner() {
        txtTitle.setText(_mainMovieTitle);
        txtSubTitle.setText(_certificateText);
        txtDescription.setText(_mainMovieCategory);


        Log.d("_mainMoviePicture", _mainMoviePicture);
        try {
            Glide.with(getContext())
                    .load(_mainMoviePicture)
                    .placeholder(R.drawable.default_landscape)
                    .error(R.drawable.default_landscape)
                    .into(imgBanner);
        } catch (Exception e) {
            Log.e("GlideImageException", e.toString());
        }

    }


    void getBannerMoviesDetails(String token, String profileId, String categoryID, String pageId) {

        loadingDialog.show();
        ApiService apiService = RetrofitClient.getApiService(token);
        Call<JsonObject> call = apiService.getBannerlist(pageId, profileId);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                Log.d("Banner_Response", response.toString());
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject data = response.body();

                    Log.d("Banner_Response", data.toString());

                    try {
                        JSONObject jsonObject = new JSONObject(data.toString());
                        JSONObject jsonObject1 = jsonObject.getJSONObject("data");
                        JSONObject jsonObject2 = jsonObject1.getJSONObject("movies");

                        String movieTitle = jsonObject2.getString("title").toString();
                        String _certificate = jsonObject2.getString("certificate").toString();
                        String _tagText = jsonObject2.getString("tag_text").toString();
                        String _videoUrl = jsonObject2.getString("video_url").toString();
                        String _id = jsonObject2.getString("id").toString();

                        String _image = RetrofitClient.BASE_URL + "/" + jsonObject2.getString("image");
                        String _medium = RetrofitClient.BASE_URL + "/" + jsonObject2.getString("medium");
                        String _thumbnail = RetrofitClient.BASE_URL + "/" + jsonObject2.getString("thumbnail");
                        String _portraitsmall = RetrofitClient.BASE_URL + "/" + jsonObject2.getString("portraitsmall");
                        String _portrait = RetrofitClient.BASE_URL + "/" + jsonObject2.getString("portrait");

                        Pattern exp = Pattern.compile("<[^>]*>", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
                        String resultTagText = _tagText.replaceAll(exp.pattern(), "  ");

                        _mainMovieTitle = movieTitle;
//                        _mainMoviePicture = _thumbnail;
                        _mainMoviePicture = _image;
                        _certificateText = _certificate;
                        _mainMovieId = _id.toString();
                        _mainMovieUrl = _videoUrl.toString();
                        _mainMovieCategory = resultTagText;

                        updateBanner();
//                        loadingDialog.cancel();

                    } catch (JSONException e) {
                        loadingDialog.cancel();
                        Log.d("JsonBanner_Error", e.toString());
                    }

                    getUsermoviesWatchinglist(token, profileId, categoryID);
//                    loadingDialog.cancel();

                } else {
                    loadingDialog.cancel();
                    Log.d("getBanner_Error", "");
                    appUtils.showToast("No data found.");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d("Banner_Error", t.toString());
                loadingDialog.cancel();

                appUtils.showToast("Network error.");
            }
        });
    }

    void getBlockMoviesLits(String token, String profileId, String categoryID, int pageId) {

        loadingDialog.show();
        limit = 0;
        if (scrolleEnabled == false) {
            moviesMainCategoryModelList.clear();
        }
        ApiService apiService = RetrofitClient.getApiService(token);
        Call<JsonObject> call = apiService.getBlockslist(pageId, profileId, categoryID);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                Log.d("BlockList_Response", response.toString());
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject data = response.body();

                    Log.d("BlockList_Response", data.toString());

                    try {
                        JSONObject jsonObject = new JSONObject(data.toString());
                        JSONArray jsonArray = jsonObject.getJSONArray("data");

                        for (int i = 0; i < jsonArray.length(); i++) {

                            limit++;

                            JSONObject responseCatgory = jsonArray.getJSONObject(i);
                            String categoryTitle = responseCatgory.getString("title").toString();
                            String categoryId = responseCatgory.getString("id").toString();

                            JSONArray jsonArrayMovies = responseCatgory.getJSONArray("movies");

                            List<Movies> userMainCategoryModelList = new ArrayList<>();
                            for (int j = 0; j < jsonArrayMovies.length(); j++) {

                                JSONObject jsonObjectMovie = jsonArrayMovies.getJSONObject(j);

                                String _image = RetrofitClient.BASE_URL + "/" + jsonObjectMovie.getString("image");
                                String _thumbnail = RetrofitClient.BASE_URL + "/" + jsonObjectMovie.getString("thumbnail");
                                String _portraitsmall = RetrofitClient.BASE_URL + "/" + jsonObjectMovie.getString("portraitsmall");
                                String _portrait = RetrofitClient.BASE_URL + "/" + jsonObjectMovie.getString("portrait");

                                Usermovies usermovies = null;

                                try {
                                    Object json = new JSONTokener("usermovies").nextValue();
                                    if (json instanceof JSONObject) {
                                        if (jsonObjectMovie.getString("usermovies") != "") {
                                            if (jsonObjectMovie.getJSONObject("usermovies") != null) {
                                                JSONObject jsonObject1 = jsonObjectMovie.getJSONObject("usermovies");
                                                usermovies = new Usermovies(
                                                        jsonObject1.getString("id").toString(),
                                                        jsonObject1.getString("movie_id").toString(),
                                                        jsonObject1.getString("mylist").toString(),
                                                        jsonObject1.getString("likes").toString(),
                                                        jsonObject1.getString("watch_time").toString(),
                                                        jsonObject1.getString("watching").toString(),
                                                        jsonObject1.getString("watched").toString(),
                                                        jsonObject1.getString("watched_percent").toString(),
                                                        jsonObject1.getString("viewed").toString());

                                                Log.d("TestTiming", jsonObject1.getString("watch_time").toString());

                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.d("UsermoviesException", e.toString());
                                }

                                userMainCategoryModelList.add(new Movies(
                                        jsonObjectMovie.getString("id").toString(),
                                        jsonObjectMovie.getString("title").toString(),
                                        jsonObjectMovie.getString("topten").toString(),
                                        jsonObjectMovie.getString("trailer").toString(),
                                        jsonObjectMovie.getString("certificate").toString(),
                                        jsonObjectMovie.getString("duration").toString(),
                                        jsonObjectMovie.getString("tag_text").toString(),
                                        jsonObjectMovie.getString("published_date").toString(),
                                        jsonObjectMovie.getString("userlist").toString(),
                                        jsonObjectMovie.getString("userlike").toString(),
                                        _image,//_thumbnail,
                                        _portraitsmall,
                                        _portrait,
                                        usermovies));

                                Log.d("MovTitleName", jsonObjectMovie.getString("title").toString());

                            }

                            Log.d("MovTitleSize", String.valueOf(userMainCategoryModelList.size()));
//                            Log.d("MovTitle", userMainCategoryModelList.get(2).getTitle());

                            moviesMainCategoryModelList.add(new MoviesMainCategoryModel(
                                    categoryId,
                                    categoryTitle,
                                    userMainCategoryModelList));

                            loadingDialog.cancel();
                        }


                        if (moviesMainCategoryModelList != null && moviesMainCategoryModelList.size() != 0) {
                            listFragment.bindData(moviesMainCategoryModelList);
                        }


                        if (limit < 5) {
                            hasMoreScroll = false;
                            scrolleEnabled = false;
                        }


//                        if (hasMoreScroll) {
//                            _page++;
//                        }


                        VerticalGridView verticalGridView = (VerticalGridView) listFragment.getView();
                        if (verticalGridView != null) {
                            // Attach a scroll listener to the VerticalGridView
                            verticalGridView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                                @Override
                                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                                    super.onScrolled(recyclerView, dx, dy);

                                    // Get the number of items in the adapter
                                    RecyclerView.Adapter adapter = recyclerView.getAdapter();
                                    if (adapter == null) return;
                                    int totalItemCount = adapter.getItemCount();

//
                                    if (verticalGridView.getChildCount() > 0) {
                                        // Get the adapter position of the last visible child view
                                        int lastVisibleItemPosition = verticalGridView.getChildAdapterPosition(
                                                verticalGridView.getChildAt(verticalGridView.getChildCount() - 1));

                                        // Check if the last visible item is the last item in the list
                                        if (lastVisibleItemPosition == totalItemCount - 1) {
                                            // Trigger event only if the last item is fully visible and not previously triggered
                                            if (lastPosition != lastVisibleItemPosition) {
                                                Log.d("ScrollListener", "Reached the end of the list");
                                                lastPosition = lastVisibleItemPosition; // Update last known position
                                                _page++;
                                                getBlockMoviesLits(token, profileId, categoryID, _page);
                                            }
                                        } else {
                                            lastPosition = -1; // Reset if not at the end
                                        }

                                        // Log the current visible position
                                        Log.d("ScrollListener", "Current visible position: " + lastVisibleItemPosition);
                                    }

                                }

                                @Override
                                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                                    super.onScrollStateChanged(recyclerView, newState);

//
                                    // Handle scroll state changes
                                    switch (newState) {
                                        case RecyclerView.SCROLL_STATE_IDLE:
                                            Log.d("ScrollListener", "Scroll Idle");
                                            break;
                                        case RecyclerView.SCROLLBARS_OUTSIDE_OVERLAY:
                                            Log.d("ScrollListener", "Scroll End");
                                            break;
                                        case RecyclerView.SCROLL_STATE_DRAGGING:
                                            Log.d("ScrollListener", "Scroll Dragging");
                                            break;
                                        case RecyclerView.SCROLL_STATE_SETTLING:
                                            Log.d("ScrollListener", "Scroll Settling");
                                            break;
                                    }
                                }
                            });
                        }

                    } catch (JSONException e) {
                        loadingDialog.cancel();
                        Log.d("BlockList_Error", e.toString());
                    }

                    loadingDialog.cancel();

                } else {
                    loadingDialog.cancel();
                    Log.d("BlockList_Error", "");
//                    appUtils.showToast("No data found.");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d("BlockList_Error", t.toString());
                loadingDialog.cancel();

                appUtils.showToast("Network error.");
            }
        });

        loadingDialog.cancel();
    }

    void getUsermoviesWatchinglist(String token, String profileId, String categoryID) {

        loadingDialog.show();
//        userMainCategoryModelList.clear();

        ApiService apiService = RetrofitClient.getApiService(token);
        Call<JsonObject> call = apiService.getUsermoviesWatchinglist(profileId, "1");
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                Log.d("MyList_Response", response.toString());
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject data = response.body();

                    Log.d("MyList_Response", data.toString());

                    try {
                        JSONObject jsonObject = new JSONObject(data.toString());
                        JSONArray jsonArrayMovies = jsonObject.getJSONArray("data");

                        List<Movies> userMainCategoryModelList = new ArrayList<>();
                        for (int i = 0; i < jsonArrayMovies.length(); i++) {

                            JSONObject jsonObjectMovie = jsonArrayMovies.getJSONObject(i);

                            String _image = RetrofitClient.BASE_URL + "/" + jsonObjectMovie.getString("image");
                            String _thumbnail = RetrofitClient.BASE_URL + "/" + jsonObjectMovie.getString("thumbnail");
                            String _portraitsmall = RetrofitClient.BASE_URL + "/" + jsonObjectMovie.getString("portraitsmall");
                            String _portrait = RetrofitClient.BASE_URL + "/" + jsonObjectMovie.getString("portrait");

                            Usermovies usermovies = null;

                            try {
                                Object json = new JSONTokener("usermovies").nextValue();

//                                if (json instanceof JSONObject) {
                                if (jsonObjectMovie.getJSONObject("usermovies") != null) {
                                    Log.d("usermoviesValues", jsonObjectMovie.getJSONObject("usermovies").getString("watch_time"));
                                    JSONObject jsonObject1 = jsonObjectMovie.getJSONObject("usermovies");
                                    usermovies = new Usermovies(
                                            jsonObject1.getString("id").toString(),
                                            jsonObject1.getString("movie_id").toString(),
                                            jsonObject1.getString("mylist").toString(),
                                            jsonObject1.getString("likes").toString(),
                                            jsonObject1.getString("watch_time").toString(),
                                            jsonObject1.getString("watching").toString(),
                                            jsonObject1.getString("watched").toString(),
                                            jsonObject1.getString("watched_percent").toString(),
                                            jsonObject1.getString("viewed").toString());

                                    Log.d("TestTiming1", jsonObject1.getString("watch_time").toString());
                                }
//                                }

                            } catch (Exception e) {
                                Log.d("UsermoviesException", e.toString());
                            }

                            userMainCategoryModelList.add(new Movies(
                                    jsonObjectMovie.getString("id").toString(),
                                    jsonObjectMovie.getString("title").toString(),
                                    jsonObjectMovie.getString("topten").toString(),
                                    jsonObjectMovie.getString("trailer").toString(),
                                    jsonObjectMovie.getString("certificate").toString(),
                                    jsonObjectMovie.getString("duration").toString(),
                                    jsonObjectMovie.getString("tag_text").toString(),
                                    jsonObjectMovie.getString("published_date").toString(),
                                    jsonObjectMovie.getString("userlist").toString(),
                                    jsonObjectMovie.getString("userlike").toString(),
                                    _image,
                                    _portraitsmall,
                                    _portrait,
                                    usermovies));

                            loadingDialog.cancel();
                        }

                        if (userMainCategoryModelList != null && userMainCategoryModelList.size() != 0) {
                            moviesMainCategoryModelList.add(new MoviesMainCategoryModel(
                                    "0",
                                    "Continue watching",
                                    userMainCategoryModelList));

                            listFragment.bindData(moviesMainCategoryModelList);
                        }
                        getBlockMoviesLits(token, profileId, categoryID, Integer.parseInt(mPageId));


                    } catch (JSONException e) {
                        loadingDialog.cancel();
                        Log.d("MyList_Error", e.toString());
                    }

                    loadingDialog.cancel();

                } else {
                    loadingDialog.cancel();
                    Log.d("MyList_Error", "");
                    appUtils.showToast("No data found.");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d("MyList_Error", t.toString());
                loadingDialog.cancel();

                appUtils.showToast("Network error.");
            }
        });

        loadingDialog.cancel();
    }

}