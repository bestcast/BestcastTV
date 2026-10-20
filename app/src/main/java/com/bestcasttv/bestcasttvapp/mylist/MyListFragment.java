package com.bestcasttv.bestcasttvapp.mylist;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
 * Use the {@link MyListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyListFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
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
    List<Movies> moviesMyListModel;
    List<Movies> moviesWatchingModel;
    List<Movies> moviesRecentlyModel;
    boolean scrolleEnabled = false;
    boolean hasMoreScroll = true;
    int _page = 1;
    int limit = 0;

    AppUtils appUtils;
    LoadingDialog loadingDialog;
    SharedPreferences sharedPreferences;

    public MyListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MyListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MyListFragment newInstance(String param1, String param2) {
        MyListFragment fragment = new MyListFragment();
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

        moviesMainCategoryModelList = new ArrayList<>();
        moviesMyListModel = new ArrayList<>();
        moviesWatchingModel = new ArrayList<>();
        moviesRecentlyModel = new ArrayList<>();

        getUsermoviesMylist(_token, profileID, "1");

        init(view);
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
            Intent intent = new Intent(requireContext(), MovieDetailsActivity.class);
            intent.putExtra("movieId", item.getId());
            startActivity(intent);
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


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_list, container, false);
    }

    void getUsermoviesMylist(String token, String profileId, String searchType) {

        loadingDialog.show();
        moviesMyListModel.clear();

        ApiService apiService = RetrofitClient.getApiService(token);
        Call<JsonObject> call = apiService.getUsermovieslist(profileId, searchType);
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

                            moviesMyListModel.add(new Movies(
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

                        if (moviesMyListModel != null&&moviesMyListModel.size()!=0) {
                            moviesMainCategoryModelList.add(new MoviesMainCategoryModel(
                                    "1",
                                    "My List & More Movies",
                                    moviesMyListModel));

                            listFragment.bindData(moviesMainCategoryModelList);
                        }else {
                            getUsermoviesMylist(_token, profileID, "");
                        }


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