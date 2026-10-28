package com.bestcasttv.bestcasttvapp.main_page;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.leanback.widget.BrowseFrameLayout;

import android.annotation.SuppressLint;
import android.graphics.Rect;
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
import android.animation.ValueAnimator;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.view.animation.DecelerateInterpolator;
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
    View navScrimOverlay;
    private ValueAnimator navBarAnimator;
    private ValueAnimator navCategoryAnimator;
    private int navCollapsedWidth;
    private int navExpandedWidth;
    private int navSubWidth;
    private boolean isSwitchingFragment = false;
    private boolean isDpadNavigatingToSearch = false;
    private boolean isDpadNavigatingToSignOut = false;
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
        navScrimOverlay = findViewById(R.id.navScrimOverlay);

        navCollapsedWidth = getResources().getDimensionPixelSize(R.dimen.side_nav_width_collapsed);
        navExpandedWidth = getResources().getDimensionPixelSize(R.dimen.side_nav_width_expanded);
        navSubWidth = getResources().getDimensionPixelSize(R.dimen.side_nav_sub_width);

        if (navBar != null) {
            navBar.getLayoutParams().width = navCollapsedWidth;
            navBar.requestLayout();
        }
        if (navCategoryBar != null) {
            navCategoryBar.getLayoutParams().width = 0;
            navCategoryBar.requestLayout();
        }
        if (navScrimOverlay != null) {
            navScrimOverlay.setVisibility(View.GONE);
            navScrimOverlay.setAlpha(0f);
            navScrimOverlay.setOnClickListener(v -> {
                if (CATEGORY_SIDE_MENU) {
                    closeCategoryMenu();
                }
                if (SIDE_MENU) {
                    closeMenu();
                }
            });
        }

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

        navBar.setOnFocusSearchListener((focused, direction) -> {
            if (direction == View.FOCUS_RIGHT) {
                if (fragmentContainer != null) {
                    return fragmentContainer;
                }
            }
            if (focused == null || focused == navBar || !isNavMenuItem(focused)) {
                View target = getSelectedMenuView();
                if (target != null) {
                    return target;
                }
            }
            return null;
        });

        navBar.setOnChildFocusListener(new BrowseFrameLayout.OnChildFocusListener() {
            @Override
            public boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
                View target = getSelectedMenuView();
                if (target != null) {
                    target.requestFocus();
                    return true;
                }
                return true;
            }

            @Override
            public void onRequestChildFocus(View child, View focused) {
            }
        });

        btnSearch.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                if (!Constants.MENU_SEARCH.equals(selectedMenu) && !isDpadNavigatingToSearch) {
                    View target = getSelectedMenuView();
                    if (target != null && target != btnSearch) {
                        target.requestFocus();
                        return;
                    }
                }
                isDpadNavigatingToSearch = false;
                if (!isSwitchingFragment) {
                    if (!SIDE_MENU && !CATEGORY_SIDE_MENU) {
                        openMenu();
                        SIDE_MENU = true;
                    }
                }
            }
        });

        View.OnFocusChangeListener standardMenuFocusListener = (v, hasFocus) -> {
            if (hasFocus) {
                isDpadNavigatingToSearch = false;
                isDpadNavigatingToSignOut = false;
                if (!isSwitchingFragment) {
                    if (!SIDE_MENU && !CATEGORY_SIDE_MENU) {
                        openMenu();
                        SIDE_MENU = true;
                    }
                }
            }
        };
        btnHome.setOnFocusChangeListener(standardMenuFocusListener);
        btnMovie.setOnFocusChangeListener(standardMenuFocusListener);
        if (btnWebseries != null) btnWebseries.setOnFocusChangeListener(standardMenuFocusListener);
        btnCategory.setOnFocusChangeListener(standardMenuFocusListener);
        btnMyist.setOnFocusChangeListener(standardMenuFocusListener);

        signOutBtTextView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                if (!Constants.MENU_SIGNOUT.equals(selectedMenu) && !isDpadNavigatingToSignOut) {
                    View target = getSelectedMenuView();
                    if (target != null && target != signOutBtTextView) {
                        target.requestFocus();
                        return;
                    }
                }
                isDpadNavigatingToSignOut = false;
                if (!isSwitchingFragment) {
                    if (!SIDE_MENU && !CATEGORY_SIDE_MENU) {
                        openMenu();
                        SIDE_MENU = true;
                    }
                }
            }
        });

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
                .circleCrop()
                .into(profileImage);
        profileNameText.setText(profileName);
        profileNameText.setVisibility(View.GONE);
        profileNameText.setAlpha(0f);

        getCategoryList(_token);


    }

    public void changeFragment(Fragment fragment) {
        isSwitchingFragment = true;
        closeCategoryMenu();
        if (fragmentContainer != null) {
            fragmentContainer.setTranslationX(0f);
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.commitAllowingStateLoss();

        View target = getSelectedMenuView();
        if (target != null) {
            target.requestFocus();
        }

        if (fragmentContainer != null) {
            fragmentContainer.postDelayed(() -> {
                isSwitchingFragment = false;
                if (SIDE_MENU || CATEGORY_SIDE_MENU || isFocusInNavBar()) {
                    View menuTarget = getSelectedMenuView();
                    if (menuTarget != null) {
                        menuTarget.requestFocus();
                    }
                }
            }, 150);
        } else {
            isSwitchingFragment = false;
        }
    }

    private void handleMenuClick(View view) {
        if (lastSelectedMenu != null) {
            lastSelectedMenu.setActivated(false);
        }
        view.setActivated(true);
        lastSelectedMenu = view;
        view.requestFocus();

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

        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (view == btnHome && keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                isDpadNavigatingToSearch = true;
            } else if (view == btnSearch && keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                isDpadNavigatingToSearch = false;
            } else if (view == btnMyist && keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                isDpadNavigatingToSignOut = true;
            } else if (view == signOutBtTextView && keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                isDpadNavigatingToSignOut = false;
            }

            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_CENTER:
                case KeyEvent.KEYCODE_ENTER:
                    handleMenuClick(view);
                    return true;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    if (!SIDE_MENU && !CATEGORY_SIDE_MENU) {
                        switchToLastSelectedMenu();
                        openMenu();
                        return true;
                    } else if (CATEGORY_SIDE_MENU) {
                        closeCategoryMenu();
                        if (btnCategory != null) {
                            btnCategory.requestFocus();
                        }
                        return true;
                    }
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    if (SIDE_MENU && CATEGORY_SIDE_MENU) {
                        if (categoryListView != null) {
                            categoryListView.requestFocus();
                            categoryListView.setSelection(0);
                        }
                        return true;
                    } else if (SIDE_MENU) {
                        closeMenu();
                        returnFocusToContent();
                        return true;
                    }
                    break;
            }
        }
        return false;
    }


    public View getSelectedMenuView() {
        if (selectedMenu == null) return btnHome;
        switch (selectedMenu) {
            case Constants.MENU_SEARCH:
                return btnSearch;
            case Constants.MENU_HOME:
                return btnHome;
            case Constants.MENU_MOVIE:
                return btnMovie;
            case Constants.MENU_WEBSERIES:
                return btnWebseries != null ? btnWebseries : btnHome;
            case Constants.MENU_CATEGORY:
                return btnCategory;
            case Constants.MENU_MYLIST:
                return btnMyist;
            case Constants.MENU_SIGNOUT:
                return signOutBtTextView;
            default:
                return lastSelectedMenu != null ? lastSelectedMenu : btnHome;
        }
    }

    public void switchToLastSelectedMenu() {
        View target = getSelectedMenuView();
        if (target != null) {
            target.requestFocus();
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && SIDE_MENU && !CATEGORY_SIDE_MENU) {
            closeMenu();
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && CATEGORY_SIDE_MENU) {
            if (categoryListView != null && !categoryListView.hasFocus()) {
                categoryListView.requestFocus();
                categoryListView.setSelection(0);
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return super.dispatchKeyEvent(event);
    }


    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        if (CATEGORY_SIDE_MENU) {
            closeCategoryMenu();
            if (btnCategory != null) {
                btnCategory.requestFocus();
            }
            return;
        } else if (SIDE_MENU) {
            closeMenu();
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
        if (SIDE_MENU) return;
        SIDE_MENU = true;

        // Hide keyboard if open (e.g. from Search page)
        View currentFocus = getCurrentFocus();
        if (currentFocus != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
            }
        }

        if (navBarAnimator != null && navBarAnimator.isRunning()) {
            navBarAnimator.cancel();
        }

        int currentWidth = (navBar.getWidth() > 0) ? navBar.getWidth() : navCollapsedWidth;
        int targetWidth = navExpandedWidth;
        int shiftDelta = targetWidth - navCollapsedWidth;
        float currentTransX = fragmentContainer != null ? fragmentContainer.getTranslationX() : 0f;

        navBarAnimator = ValueAnimator.ofFloat(0f, 1f);
        navBarAnimator.setDuration(220);
        navBarAnimator.setInterpolator(new DecelerateInterpolator());
        navBarAnimator.addUpdateListener(animation -> {
            float fraction = (float) animation.getAnimatedValue();
            int width = (int) (currentWidth + (targetWidth - currentWidth) * fraction);
            ViewGroup.LayoutParams lp = navBar.getLayoutParams();
            if (lp != null) {
                lp.width = width;
                navBar.setLayoutParams(lp);
            }
            if (fragmentContainer != null) {
                fragmentContainer.setTranslationX(currentTransX + (shiftDelta - currentTransX) * fraction);
            }
        });
        navBarAnimator.start();

        if (navScrimOverlay != null) {
            navScrimOverlay.setVisibility(View.VISIBLE);
            navScrimOverlay.animate().alpha(1.0f).setDuration(220).start();
        }

        if (profileNameText != null) {
            profileNameText.setVisibility(View.VISIBLE);
            profileNameText.animate().alpha(1.0f).setDuration(220).start();
        }
    }

    public void closeMenu() {
        if (!SIDE_MENU && (navBar.getWidth() <= navCollapsedWidth)) return;
        SIDE_MENU = false;

        if (navBarAnimator != null && navBarAnimator.isRunning()) {
            navBarAnimator.cancel();
        }

        int currentWidth = (navBar.getWidth() > 0) ? navBar.getWidth() : navExpandedWidth;
        int targetWidth = navCollapsedWidth;
        float currentTransX = fragmentContainer != null ? fragmentContainer.getTranslationX() : 0f;

        navBarAnimator = ValueAnimator.ofFloat(0f, 1f);
        navBarAnimator.setDuration(200);
        navBarAnimator.setInterpolator(new DecelerateInterpolator());
        navBarAnimator.addUpdateListener(animation -> {
            float fraction = (float) animation.getAnimatedValue();
            int width = (int) (currentWidth + (targetWidth - currentWidth) * fraction);
            ViewGroup.LayoutParams lp = navBar.getLayoutParams();
            if (lp != null) {
                lp.width = width;
                navBar.setLayoutParams(lp);
            }
            if (fragmentContainer != null) {
                fragmentContainer.setTranslationX(currentTransX * (1f - fraction));
            }
        });
        navBarAnimator.start();

        if (profileNameText != null) {
            profileNameText.animate().alpha(0.0f).setDuration(150).withEndAction(() -> {
                if (!SIDE_MENU && profileNameText != null) {
                    profileNameText.setVisibility(View.GONE);
                }
            }).start();
        }

        if (navScrimOverlay != null && !CATEGORY_SIDE_MENU) {
            navScrimOverlay.animate().alpha(0.0f).setDuration(180).withEndAction(() -> {
                if (!SIDE_MENU && !CATEGORY_SIDE_MENU && navScrimOverlay != null) {
                    navScrimOverlay.setVisibility(View.GONE);
                }
            }).start();
        }

        if (!isSwitchingFragment) {
            returnFocusToContent();
        }
    }

    public void openCategoryMenu() {
        if (CATEGORY_SIDE_MENU) return;
        CATEGORY_SIDE_MENU = true;

        if (navCategoryAnimator != null && navCategoryAnimator.isRunning()) {
            navCategoryAnimator.cancel();
        }

        int currentWidth = navCategoryBar.getWidth();
        int baseShift = navExpandedWidth - navCollapsedWidth;
        float currentTransX = fragmentContainer != null ? fragmentContainer.getTranslationX() : baseShift;
        float targetTransX = baseShift + navSubWidth;

        navCategoryAnimator = ValueAnimator.ofFloat(0f, 1f);
        navCategoryAnimator.setDuration(220);
        navCategoryAnimator.setInterpolator(new DecelerateInterpolator());
        navCategoryAnimator.addUpdateListener(animation -> {
            float fraction = (float) animation.getAnimatedValue();
            int width = (int) (currentWidth + (navSubWidth - currentWidth) * fraction);
            ViewGroup.LayoutParams lp = navCategoryBar.getLayoutParams();
            if (lp != null) {
                lp.width = width;
                navCategoryBar.setLayoutParams(lp);
            }
            if (fragmentContainer != null) {
                fragmentContainer.setTranslationX(currentTransX + (targetTransX - currentTransX) * fraction);
            }
        });
        navCategoryAnimator.start();

        if (navScrimOverlay != null) {
            navScrimOverlay.setVisibility(View.VISIBLE);
            navScrimOverlay.animate().alpha(1.0f).setDuration(220).start();
        }
    }

    public void closeCategoryMenu() {
        if (!CATEGORY_SIDE_MENU && navCategoryBar.getWidth() == 0) return;
        CATEGORY_SIDE_MENU = false;

        if (navCategoryAnimator != null && navCategoryAnimator.isRunning()) {
            navCategoryAnimator.cancel();
        }

        int currentWidth = navCategoryBar.getWidth();
        int targetShift = SIDE_MENU ? (navExpandedWidth - navCollapsedWidth) : 0;
        float currentTransX = fragmentContainer != null ? fragmentContainer.getTranslationX() : targetShift;

        navCategoryAnimator = ValueAnimator.ofFloat(0f, 1f);
        navCategoryAnimator.setDuration(180);
        navCategoryAnimator.setInterpolator(new DecelerateInterpolator());
        navCategoryAnimator.addUpdateListener(animation -> {
            float fraction = (float) animation.getAnimatedValue();
            int width = (int) (currentWidth * (1f - fraction));
            ViewGroup.LayoutParams lp = navCategoryBar.getLayoutParams();
            if (lp != null) {
                lp.width = width;
                navCategoryBar.setLayoutParams(lp);
            }
            if (fragmentContainer != null) {
                fragmentContainer.setTranslationX(currentTransX + (targetShift - currentTransX) * fraction);
            }
        });
        navCategoryAnimator.start();

        if (navScrimOverlay != null && !SIDE_MENU) {
            navScrimOverlay.animate().alpha(0.0f).setDuration(180).withEndAction(() -> {
                if (!SIDE_MENU && !CATEGORY_SIDE_MENU && navScrimOverlay != null) {
                    navScrimOverlay.setVisibility(View.GONE);
                }
            }).start();
        }
    }

    public void returnFocusToContent() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
        boolean focused = false;
        if (fragment instanceof HomeFragment) {
            focused = ((HomeFragment) fragment).requestFocusOnContent();
        } else if (fragment instanceof com.bestcasttv.bestcasttvapp.webseries.ui.WebSeriesFragment) {
            focused = ((com.bestcasttv.bestcasttvapp.webseries.ui.WebSeriesFragment) fragment).requestFocusOnContent();
        } else if (fragment instanceof com.bestcasttv.bestcasttvapp.mylist.MyListFragment) {
            focused = ((com.bestcasttv.bestcasttvapp.mylist.MyListFragment) fragment).requestFocusOnContent();
        } else if (fragment instanceof SearchMoviesFragment) {
            ((SearchMoviesFragment) fragment).requestFocusOnContent();
            focused = true;
        } else if (fragment != null && fragment.getView() != null) {
            focused = fragment.getView().requestFocus();
        }
        if (!focused) {
            View target = getSelectedMenuView();
            if (target != null) {
                target.requestFocus();
            }
        }
    }

    public boolean isSideMenuOpen() {
        return SIDE_MENU || CATEGORY_SIDE_MENU;
    }

    public boolean isFocusInNavBar() {
        View current = getCurrentFocus();
        return isNavMenuItem(current);
    }

    public boolean isNavMenuItem(View view) {
        if (view == null) return false;
        int id = view.getId();
        return id == R.id.btn_search || id == R.id.btn_home || id == R.id.btn_movies
                || id == R.id.btn_webseries || id == R.id.btn_category || id == R.id.btn_mylist
                || id == R.id.signOutBT;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            View currentFocus = getCurrentFocus();
            if (currentFocus == null || !isNavMenuItem(currentFocus)) {
                if (SIDE_MENU || CATEGORY_SIDE_MENU) {
                    View target = getSelectedMenuView();
                    if (target != null) {
                        target.requestFocus();
                    }
                }
            } else if (currentFocus == btnSearch && !Constants.MENU_SEARCH.equals(selectedMenu)) {
                View target = getSelectedMenuView();
                if (target != null) {
                    target.requestFocus();
                }
            } else if (currentFocus == signOutBtTextView && !Constants.MENU_SIGNOUT.equals(selectedMenu)) {
                View target = getSelectedMenuView();
                if (target != null) {
                    target.requestFocus();
                }
            }
        }
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
                                        case KeyEvent.KEYCODE_DPAD_RIGHT:
                                            closeCategoryMenu();
                                            closeMenu();
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