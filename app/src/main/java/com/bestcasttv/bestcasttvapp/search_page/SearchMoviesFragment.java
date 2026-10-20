package com.bestcasttv.bestcasttvapp.search_page;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bestcasttv.bestcasttvapp.R;
import com.bestcasttv.bestcasttvapp.api_services.ApiService;
import com.bestcasttv.bestcasttvapp.api_services.RetrofitClient;
import com.bestcasttv.bestcasttvapp.app_utils.AppUtils;
import com.bestcasttv.bestcasttvapp.app_utils.LoadingDialog;
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
 * Use the {@link SearchMoviesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchMoviesFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    EditText searchText;
    ImageView imgBanner;
//    ListRowFragment listFragment;
    SearchListRowFragment listFragment;

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
    List<Movies> moviesSearchModel;
    boolean scrolleEnabled = false;
    boolean hasMoreScroll = true;
    int _page = 1;
    int limit = 0;

    AppUtils appUtils;
    LoadingDialog loadingDialog;
    SharedPreferences sharedPreferences;

    public SearchMoviesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SearchMoviesFragment.
     */
    // TODO: Rename and change types and number of parameters
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

        init(view);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search_movies, container, false);
    }

    public void init(View view) {
        imgBanner = view.findViewById(R.id.img_banner);
        searchText = view.findViewById(R.id.searchText);

        searchText.requestFocus();
        showKeyboard(getContext(), searchText);

        listFragment = new SearchListRowFragment();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.list_fragment, listFragment);
        transaction.commit();

        listFragment.setOnContentSelectedListener(new SearchListRowFragment.OnSelectedListener() {
            @Override
            public void onSelection(Movies model) {
                updateOnSelectionBanner(model);
            }
        });

        listFragment.setOnItemClickListener(item -> {
            Intent intent = new Intent(requireContext(), MovieDetailsActivity.class);
            intent.putExtra("movieId", item.getId());
            startActivity(intent);
        });

        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                Log.d("TextChanged", "On: " + s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                Log.d("TextChanged", "Before: " + s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

                Log.d("TextChanged", "after" + s.toString());

                getSearchMovies(_token, profileID, s.toString());

            }
        });
    }

    public static void showKeyboard(Context context, View view) {
        view.requestFocus();
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        }, 100);
    }

    public static void hideKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void updateOnSelectionBanner(Movies movies) {

        Pattern exp = Pattern.compile("<[^>]*>", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
        String resultTagText = movies.getTagText().replaceAll(exp.pattern(), "  ");


        Glide.with(this)
                .load(movies.getThumbnail())
                .placeholder(R.drawable.default_landscape)
                .error(R.drawable.default_landscape)
                .into(imgBanner);
    }

    void getSearchMovies(String token, String profileId, String searchText) {

        loadingDialog.show();
        moviesMainCategoryModelList = new ArrayList<>();
        moviesSearchModel = new ArrayList<>();
        moviesSearchModel.clear();
        moviesMainCategoryModelList.clear();

        ApiService apiService = RetrofitClient.getApiService(token);
        Call<JsonObject> call = apiService.getSearchMovieslist(searchText, profileId);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                Log.d("Search_Response", response.toString());
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject data = response.body();

                    Log.d("Search_Response", data.toString());

                    try {
                        JSONObject jsonObject = new JSONObject(data.toString());
                        JSONArray jsonArrayMovies = jsonObject.getJSONArray("data");

                        for (int i = 0; i < jsonArrayMovies.length(); i++) {

                            JSONObject jsonObjectMovie = jsonArrayMovies.getJSONObject(i);

                            String _image = RetrofitClient.BASE_URL + "/" + jsonObjectMovie.getString("image");
                            String _thumbnail = RetrofitClient.BASE_URL + "/" + jsonObjectMovie.getString("thumbnail");
                            String _portraitsmall = RetrofitClient.BASE_URL + "/" + jsonObjectMovie.getString("portraitsmall");
                            String _portrait = RetrofitClient.BASE_URL + "/" + jsonObjectMovie.getString("portrait");

                            Usermovies usermovies = null;

                            try {
                                Object json = new JSONTokener("usermovies").nextValue();

                                if (json instanceof JSONObject) {
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

                                    }
                                }

                            } catch (Exception e) {
                                Log.d("UsermoviesException", e.toString());
                            }

                            moviesSearchModel.add(new Movies(
                                    jsonObjectMovie.getString("id").toString(),
                                    jsonObjectMovie.getString("title").toString(),
                                    jsonObjectMovie.getString("topten").toString(),
                                    jsonObjectMovie.getString("trailer").toString(),
                                    jsonObjectMovie.getString("certificate").toString(),
                                    jsonObjectMovie.getString("duration").toString(),
                                    jsonObjectMovie.getString("tag_text").toString(),
                                    jsonObjectMovie.getString("published_date").toString(),
                                    jsonObjectMovie.getString("published_date").toString(),
                                    jsonObjectMovie.getString("published_date").toString(),
                                    _image,
                                    _portraitsmall,
                                    _portrait,
                                    usermovies));

                            loadingDialog.cancel();
                        }

                        moviesMainCategoryModelList.add(new MoviesMainCategoryModel(
                                "1",
                                "",
                                moviesSearchModel));
                        listFragment.bindData(moviesMainCategoryModelList);



                    } catch (JSONException e) {
                        loadingDialog.cancel();
                        Log.d("Search_Error", e.toString());
                    }

                    loadingDialog.cancel();

                } else {
                    loadingDialog.cancel();
                    Log.d("Search_Error", "");
                    appUtils.showToast("No data found.");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d("Search_Error", t.toString());
                loadingDialog.cancel();

                appUtils.showToast("Network error.");
            }
        });

        loadingDialog.cancel();
    }

}