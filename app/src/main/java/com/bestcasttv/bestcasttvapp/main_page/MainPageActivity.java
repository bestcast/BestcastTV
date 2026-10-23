package com.bestcasttv.bestcasttvapp.main_page;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.leanback.widget.BrowseFrameLayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bestcasttv.bestcasttvapp.LoginActivity;
import com.bestcasttv.bestcasttvapp.R;
import com.bestcasttv.bestcasttvapp.api_services.ApiService;
import com.bestcasttv.bestcasttvapp.api_services.RetrofitClient;
import com.bestcasttv.bestcasttvapp.app_utils.AppUtils;
import com.bestcasttv.bestcasttvapp.app_utils.LoadingDialog;
import com.bestcasttv.bestcasttvapp.app_utils.SignoutAlertDialog;
import com.bestcasttv.bestcasttvapp.main_page.model.Genres;
import com.bestcasttv.bestcasttvapp.main_page.ui.AllMoviesFragment;
import com.bestcasttv.bestcasttvapp.main_page.ui.HomeFragment;
import com.bestcasttv.bestcasttvapp.mylist.MyListFragment;
import com.bestcasttv.bestcasttvapp.search_page.SearchMoviesFragment;
import com.bestcasttv.bestcasttvapp.utils.Common;
import com.bestcasttv.bestcasttvapp.utils.Constants;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainPageActivity extends FragmentActivity implements View.OnKeyListener {

    BrowseFrameLayout navBar;
    BrowseFrameLayout navCategoryBar;
    FrameLayout fragmentContainer;
    ImageView profileImage;
    TextView profileNameText;
    TextView btnSearch;
    TextView btnHome;
    TextView btnMovie;
    TextView btnWebseries;
    TextView btnCategory;
    TextView btnMyist;
    TextView signOutBtTextView;

    boolean SIDE_MENU = false;
    boolean CATEGORY_SIDE_MENU = false;
    String selectedMenu = Constants.MENU_HOME;

    View lastSelectedMenu;

    List<Genres> allCategoryItems;
    AppUtils appUtils;
    LoadingDialog loadingDialog;
    SharedPreferences sharedPreferences;
    String _token = "";
    String profileName = "";
    String profilePicture = "";
    String profileID = "";
    String profilePictureID = "";

    ListView categoryListView;

    SignoutAlertDialog signoutAlertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);


        fragmentContainer = findViewById(R.id.container);
        navBar = findViewById(R.id.blfNavBar);
        navCategoryBar = findViewById(R.id.blfSubNavBar);

        profileImage = findViewById(R.id.profileImage);
        profileNameText = findViewById(R.id.profileName);
        btnSearch = findViewById(R.id.btn_search);
        btnHome = findViewById(R.id.btn_home);
        btnMovie = findViewById(R.id.btn_movies);
        btnWebseries = findViewById(R.id.btn_webseries);
        btnCategory = findViewById(R.id.btn_category);
        btnMyist = findViewById(R.id.btn_mylist);
        categoryListView = findViewById(R.id.categoryListView);
        signOutBtTextView = findViewById(R.id.signOutBT);


        btnSearch.setOnKeyListener(this);
        btnHome.setOnKeyListener(this);
        btnMovie.setOnKeyListener(this);
        if (btnWebseries != null) btnWebseries.setOnKeyListener(this);
        btnCategory.setOnKeyListener(this);
        btnMyist.setOnKeyListener(this);
        signOutBtTextView.setOnKeyListener(this);

        View.OnFocusChangeListener menuFocusListener = (v, hasFocus) -> {
            if (hasFocus) {
                if (!SIDE_MENU && !CATEGORY_SIDE_MENU) {
                    openMenu();
                    SIDE_MENU = true;
                }
            }
        };
        btnSearch.setOnFocusChangeListener(menuFocusListener);
        btnHome.setOnFocusChangeListener(menuFocusListener);
        btnMovie.setOnFocusChangeListener(menuFocusListener);
        if (btnWebseries != null) btnWebseries.setOnFocusChangeListener(menuFocusListener);
        btnCategory.setOnFocusChangeListener(menuFocusListener);
        btnMyist.setOnFocusChangeListener(menuFocusListener);
        signOutBtTextView.setOnFocusChangeListener(menuFocusListener);

        View.OnClickListener menuClickListener = this::handleMenuClick;
        btnSearch.setOnClickListener(menuClickListener);
        btnHome.setOnClickListener(menuClickListener);
        btnMovie.setOnClickListener(menuClickListener);
        if (btnWebseries != null) btnWebseries.setOnClickListener(menuClickListener);
        btnCategory.setOnClickListener(menuClickListener);
        btnMyist.setOnClickListener(menuClickListener);
        signOutBtTextView.setOnClickListener(menuClickListener);


        allCategoryItems = new ArrayList<>();
        appUtils = new AppUtils(this);
        loadingDialog = new LoadingDialog(this);
        signoutAlertDialog = new SignoutAlertDialog(this);

        sharedPreferences = getSharedPreferences(getString(R.string.shared_name), Context.MODE_PRIVATE);
        _token = sharedPreferences.getString(getString(R.string.token), "");

        profileName = sharedPreferences.getString(getString(R.string.profileName), "");
        profilePicture = sharedPreferences.getString(getString(R.string.profilePicture), "");
        profileID = sharedPreferences.getString(getString(R.string.profileID), "");
        profilePictureID = sharedPreferences.getString(getString(R.string.profilePictureID), "");


        lastSelectedMenu = btnHome;
        lastSelectedMenu.setActivated(true);
        changeFragment(HomeFragment.newInstance("", "1"));

        Glide.with(this)
                .load(profilePicture)
                .placeholder(R.drawable.default_profile)
                .error(R.drawable.default_profile)
                .into(profileImage);
        profileNameText.setText(profileName);

        getCategoryList(_token);


    }

    public void changeFragment(Fragment fragment) {
        closeMenu();
        closeCategoryMenu();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.commit();

    }

    private void handleMenuClick(View view) {
        if (lastSelectedMenu != null) {
            lastSelectedMenu.setActivated(false);
        }
        view.setActivated(true);
        lastSelectedMenu = view;

        int itemId = view.getId();

        if (itemId == R.id.btn_category) {
            if (!CATEGORY_SIDE_MENU) {
                openCategoryMenu();
                CATEGORY_SIDE_MENU = true;
            }
        } else {
            closeCategoryMenu();
            CATEGORY_SIDE_MENU = false;
        }

        if (itemId == R.id.btn_search) {
            selectedMenu = Constants.MENU_SEARCH;
            changeFragment(new SearchMoviesFragment());
        } else if (itemId == R.id.btn_home) {
            selectedMenu = Constants.MENU_HOME;
            changeFragment(HomeFragment.newInstance("", "1"));
        } else if (itemId == R.id.btn_movies) {
            selectedMenu = Constants.MENU_MOVIE;
            changeFragment(AllMoviesFragment.newInstance());
        } else if (itemId == R.id.btn_webseries) {
            selectedMenu = Constants.MENU_WEBSERIES;
            changeFragment(new com.bestcasttv.bestcasttvapp.webseries.ui.WebSeriesFragment());
        } else if (itemId == R.id.btn_category) {
            selectedMenu = Constants.MENU_CATEGORY;
            if (!CATEGORY_SIDE_MENU) {
                openCategoryMenu();
                CATEGORY_SIDE_MENU = true;
            }
        } else if (itemId == R.id.btn_mylist) {
            selectedMenu = Constants.MENU_MYLIST;
            changeFragment(new MyListFragment());
        } else if (itemId == R.id.signOutBT) {
            selectedMenu = Constants.MENU_SIGNOUT;
            signoutAlertDialog.show();
            signoutAlertDialog.setOnDialogListener(new SignoutAlertDialog.SignOutOnclickListener() {
                @Override
                public void setOnClickSignOut() {
                    signoutAlertDialog.dismiss();
                    getLogOut(_token);
                }
            });
        }
    }

    private void navigateToDetailFragment(String itemId) {

        closeMenu();
        closeCategoryMenu();

        HomeFragment detailFragment = HomeFragment.newInstance(itemId, "1");
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container, detailFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

    }

    @Override
    public boolean onKey(View view, int keyCode, KeyEvent event) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                handleMenuClick(view);
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (!SIDE_MENU && !CATEGORY_SIDE_MENU) {
                    switchToLastSelectedMenu();
                    openMenu();
                    SIDE_MENU = true;
                } else if (CATEGORY_SIDE_MENU) {
                    CATEGORY_SIDE_MENU = false;
                    closeCategoryMenu();
                }
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (SIDE_MENU && CATEGORY_SIDE_MENU) {
                    navCategoryBar.requestFocus();
                }
                break;
        }
        return false;
    }


    public void switchToLastSelectedMenu() {
        switch (selectedMenu) {
            case Constants.MENU_SEARCH:
                btnSearch.requestFocus();
                break;
            case Constants.MENU_HOME:
                btnHome.requestFocus();
                break;
            case Constants.MENU_MOVIE:
                btnMovie.requestFocus();
                break;
            case Constants.MENU_WEBSERIES:
                if (btnWebseries != null) btnWebseries.requestFocus();
                break;
            case Constants.MENU_CATEGORY:
                btnCategory.requestFocus();
                break;
            case Constants.MENU_MYLIST:
                btnMyist.requestFocus();
                break;
            case Constants.MENU_SIGNOUT:
                signOutBtTextView.requestFocus();
                break;
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && SIDE_MENU && !CATEGORY_SIDE_MENU) {
            SIDE_MENU = false;
            closeMenu();
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && CATEGORY_SIDE_MENU) {
            closeCategoryMenu();
            closeMenu();
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
            if (fragment instanceof HomeFragment) {
                ((HomeFragment) fragment).requestFocusOnContent();
            } else {
                fragmentContainer.requestFocus();
            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return super.dispatchKeyEvent(event);
    }


    @Override
    public void onBackPressed() {
        if (SIDE_MENU && !CATEGORY_SIDE_MENU) {
            SIDE_MENU = false;
            closeMenu();
            return;
        } else if (CATEGORY_SIDE_MENU) {
            CATEGORY_SIDE_MENU = false;
            closeCategoryMenu();
            return;
        }

        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.container);
        if (currentFragment instanceof SearchMoviesFragment) {
            if (((SearchMoviesFragment) currentFragment).handleBackPressed()) {
                return;
            }
        }

        if (!Constants.MENU_HOME.equals(selectedMenu)) {
            selectedMenu = Constants.MENU_HOME;
            if (lastSelectedMenu != null) {
                lastSelectedMenu.setActivated(false);
            }
            btnHome.setActivated(true);
            lastSelectedMenu = btnHome;
            changeFragment(HomeFragment.newInstance("", "1"));
            return;
        }

        super.onBackPressed();
    }

    public void openMenu() {
        Animation animSlide = AnimationUtils.loadAnimation(this, R.anim.slide_in_left);
        navBar.startAnimation(animSlide);

        navBar.requestLayout();
        navBar.getLayoutParams().width = Common.getWidthInPercent(this, 16);
    }

    public void closeMenu() {
        navBar.requestLayout();
        navBar.getLayoutParams().width = Common.getWidthInPercent(this, 5);

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
        if (fragment instanceof HomeFragment) {
            ((HomeFragment) fragment).requestFocusOnContent();
        } else {
            fragmentContainer.requestFocus();
        }
        SIDE_MENU = false;
    }

    public void openCategoryMenu() {
        Animation animSlide = AnimationUtils.loadAnimation(this, R.anim.slide_in_left);
        navCategoryBar.startAnimation(animSlide);
        navCategoryBar.requestLayout();
        navCategoryBar.getLayoutParams().width = Common.getWidthInPercent(this, 20);

    }

    public void closeCategoryMenu() {
        navCategoryBar.requestLayout();
        navCategoryBar.getLayoutParams().width = Common.getWidthInPercent(this, 0);

        fragmentContainer.requestFocus();
        CATEGORY_SIDE_MENU = false;

    }

    public void signoutAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainPageActivity.this);
        builder.setTitle("Sign Out")
                .setMessage("Singing out of the this app means you'll also sign out of \nall other Bestcast apps on this device.")
                .setPositiveButton("Sign Out", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getLogOut(_token);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                });

        builder.create();
    }


    void getCategoryList(String token) {

        loadingDialog.show();
        allCategoryItems.clear();
        ApiService apiService = RetrofitClient.getApiService(token);
        Call<JsonObject> call = apiService.getGenrelist();
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                Log.d("Genre_Response", response.toString());
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject data = response.body();

                    Log.d("genrelist_Response", data.toString());

                    try {
                        JSONObject jsonObject = new JSONObject(data.toString());
                        JSONArray jsonArray = jsonObject.getJSONArray("data");

                        for (int i = 0; i < jsonArray.length(); i++) {

                            JSONObject responseCatgory = jsonArray.getJSONObject(i);
                            String _bannerId = responseCatgory.getString("id").toString();
                            String _bannerTitle = responseCatgory.getString("title").toString();

                            Genres genreslists = new Genres(
                                    _bannerId,
                                    _bannerTitle);

                            allCategoryItems.add(genreslists);

                        }

                        CategoryCustomAdapter adapter = new CategoryCustomAdapter(MainPageActivity.this, allCategoryItems);
                        categoryListView.setAdapter(adapter);

                        categoryListView.setOnKeyListener(new View.OnKeyListener() {
                            @Override
                            public boolean onKey(View v, int keyCode, KeyEvent event) {

                                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                                    int selectedPosition = categoryListView.getSelectedItemPosition();
                                    switch (keyCode) {
                                        case KeyEvent.KEYCODE_DPAD_LEFT:
                                            closeCategoryMenu();
                                            if (btnCategory != null) {
                                                btnCategory.requestFocus();
                                            }
                                            return true;
                                        case KeyEvent.KEYCODE_DPAD_UP:
                                            if (selectedPosition > 0) {
                                                categoryListView.setSelection(selectedPosition - 1);
                                                return true;
                                            }
                                            break;
                                        case KeyEvent.KEYCODE_DPAD_DOWN:
                                            if (selectedPosition < categoryListView.getCount() - 1) {
                                                categoryListView.setSelection(selectedPosition + 1);
                                                return true;
                                            }
                                            break;
                                        case KeyEvent.KEYCODE_DPAD_CENTER:
                                        case KeyEvent.KEYCODE_ENTER:
                                            if (selectedPosition >= 0 && selectedPosition < allCategoryItems.size()) {
                                                String categoryID = allCategoryItems.get(selectedPosition).getId();
                                                changeFragment(HomeFragment.newInstance(categoryID, "1"));
                                            }
                                            return true;
                                        default:
                                            break;
                                    }
                                }

                                return false;
                            }
                        });

                    } catch (JSONException e) {
                        loadingDialog.cancel();
                        Log.d("Jsongenre_Error", e.toString());
                    }

                    loadingDialog.cancel();

                } else {
                    loadingDialog.cancel();
                    Log.d("genrelist_Error", "");
                    appUtils.showToast("No data found.");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d("genrelist_Error", t.toString());
                loadingDialog.cancel();

                appUtils.showToast("Network error.");
            }
        });

        loadingDialog.cancel();
    }

    private void getLogOut(String token) {
        loadingDialog.show();
        ApiService apiService = RetrofitClient.getApiService(token);

        Call<JsonObject> call = apiService.logout();
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                Log.d("Logout_Response", response.toString());
                if (response.isSuccessful() && response.body() != null) {
//                    String data = response.body();
                    JsonObject data = response.body();

                    Log.d("Logout_Response", data.toString());
                    try {
                        JSONObject jsonObject = new JSONObject(data.toString());
                        String status = jsonObject.getString("status");

                        if ("success".equals(status)) {
                            appUtils.showToast("Logout Successful.");

                            HomeFragment.clearCache();

                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean(getString(R.string.shared_login_status), false);
                            editor.commit();

                            Intent mainIntent = new Intent(MainPageActivity.this, LoginActivity.class);
                            finishAffinity();
                            startActivity(mainIntent);

                            loadingDialog.cancel();
                        } else {
                            appUtils.showToast("Logout failed.");
                        }

                    } catch (JSONException e) {
                        loadingDialog.cancel();
                        Log.d("json Logout_Error", e.toString());
                    }

                    loadingDialog.cancel();

                } else {
                    loadingDialog.cancel();
                    Log.d("json Logout_Error", "");
                    appUtils.showToast("Input Error.");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d("Logout_Error", t.toString());
                loadingDialog.cancel();

                appUtils.showToast("Network error.");
            }
        });

    }
}