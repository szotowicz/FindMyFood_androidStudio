package com.tmc.tomik.findmyfood;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.sql.*;

public class MainNavigationActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Objects for other references
    private HomeFragment _homeObj = null;
    private LoginFragment _loginObj = null;
    private AccountSettingsFragment _accountSettingsObj = null;
    private RateRestaurantsFragment _rateRestaurantsObj = null;
    private RegistrationUserFragment _registrationUserFragment = null;
    private ChangePasswordFragment _changePasswordFragment = null;

    private int currentFragment = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_navigation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Remove previous version of filter file
        removeFile(getString(R.string.filter_file_name));

        // Load favorite filter if user is logged (or empty) and update name of menu item
        if (someoneIsLogged()) {
            writeToFile(getString(R.string.filter_file_name), readFromFile(getString(R.string.favorite_filter_file_name)));
            updateNameOfLoginItemMenu("Wyloguj");
        } else {
            writeToFile(getString(R.string.filter_file_name), "");
            updateNameOfLoginItemMenu("Zaloguj");
        }

        // Start default fragment
        currentFragment = R.id.nav_home;
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.contentFrame, _homeObj = new HomeFragment(), "start");
        transaction.commit();

        // Listener for search button on keyboard
        EditText locationEditText = (EditText) findViewById(R.id.searchEngineEditText);
        if (locationEditText != null) {
            locationEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        findLocationOnClick(findViewById(android.R.id.content));
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_navigation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        if (currentFragment != id) {
            currentFragment = id;

            switch (id) {
                case R.id.nav_home:
                    hideAndShowSearchEngineOnToolbar(true);
                    transaction.replace(R.id.contentFrame, _homeObj = new HomeFragment(), "home");
                    transaction.commit();
                    break;
                case R.id.nav_rate_restaurants:
                    if (someoneIsLogged()) {
                        hideAndShowSearchEngineOnToolbar(false);
                        transaction.replace(R.id.contentFrame, _rateRestaurantsObj = new RateRestaurantsFragment(), "rate_restaurants");
                        transaction.commit();
                    } else {
                        currentFragment = 0;
                        Toast.makeText(this, "Musisz być zalogowany, aby móc ocenić restauracje", Toast.LENGTH_SHORT).show();
                    }

                    break;
                case R.id.nav_account_settings:
                    if (someoneIsLogged()) {
                        hideAndShowSearchEngineOnToolbar(false);
                        transaction.replace(R.id.contentFrame, _accountSettingsObj = new AccountSettingsFragment(), "account_settings");
                        transaction.commit();
                    } else {
                        currentFragment = 0;
                        Toast.makeText(this, "Musisz być zalogowany, aby móc edytować ustawienia konta", Toast.LENGTH_SHORT).show();
                    }

                    break;
                case R.id.nav_login:
                    if (someoneIsLogged()) {
                        removeFile(getString(R.string.config_file_name));
                        removeFile(getString(R.string.filter_file_name));
                        removeFile(getString(R.string.favorite_filter_file_name));
                        Toast.makeText(this, "Wylogowano", Toast.LENGTH_SHORT).show();
                        updateNameOfLoginItemMenu("Zaloguj");
                    }
                    hideAndShowSearchEngineOnToolbar(false);
                    transaction.replace(R.id.contentFrame, _loginObj = new LoginFragment(), "login");
                    transaction.commit();
                    break;
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

///////////////////////////////////////////////////////////////////////////////////////

    private AlphaAnimation buttonClickEffect = new AlphaAnimation(1F, 0.5F);

    private int dpToPixels(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private void hideAndShowSearchEngineOnToolbar(boolean visibility) {
        EditText searchEngineEditText = (EditText) findViewById(R.id.searchEngineEditText);
        Button searchEngineBtn = (Button) findViewById(R.id.findLocationButton);

        if (searchEngineEditText != null) {
            if (visibility) {
                searchEngineEditText.setVisibility(View.VISIBLE);
            } else {
                searchEngineEditText.setVisibility(View.GONE);
            }
        }

        if (searchEngineBtn != null) {
            if (visibility) {
                searchEngineBtn.setVisibility(View.VISIBLE);
            } else {
                searchEngineBtn.setVisibility(View.GONE);
            }
            searchEngineBtn.setClickable(visibility);
        }
    }

    private void updateNameOfLoginItemMenu(String newName) {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();
        MenuItem nav_login = menu.findItem(R.id.nav_login);
        if (nav_login != null) {
            nav_login.setTitle(newName);
        }
    }

    private boolean someoneIsLogged() {
        String filename = getString(R.string.config_file_name);
        try {
            File directory = this.getFilesDir();
            File configFile = new File(directory, filename);

            if (configFile.exists() && configFile.isFile()) {
                StringBuilder text = new StringBuilder();
                BufferedReader br = new BufferedReader(new FileReader(configFile));
                String line;

                while ((line = br.readLine()) != null) {
                    text.append(line);
                }
                br.close();
                if (!text.toString().trim().equals("")) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void removeFile(String fileName) {
        try {
            String filename = fileName;
            File directory = this.getFilesDir();
            File configFile = new File(directory, filename);
            configFile.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeToFile(String fileName, String content) {
        FileOutputStream outputStream;

        try {
            String filename = fileName;
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(content.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String readFromFile(String fileName) {
        String filename = fileName;
        try {
            File directory = this.getFilesDir();
            File configFile = new File(directory, filename);

            if (configFile.exists() && configFile.isFile()) {
                StringBuilder text = new StringBuilder();
                BufferedReader br = new BufferedReader(new FileReader(configFile));
                String line;

                while ((line = br.readLine()) != null) {
                    text.append(line);
                }
                br.close();
                if (text.toString().trim() != "") {
                    return text.toString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

///////////////////////////////////////////////////////////////////////////////////////
///////////////////////////// ONCLICK METHODS /////////////////////////////////////////

    public void findLocationOnClick(View view) {
        view.startAnimation(buttonClickEffect);

        if (_homeObj != null) {
            EditText locationEditText = (EditText) findViewById(R.id.searchEngineEditText);
            if (locationEditText != null) {
                String locationName = locationEditText.getText().toString();
                if (!locationName.equals("")) {
                    Geocoder geocoder = new Geocoder(this);
                    try {
                        List<Address> addressList = geocoder.getFromLocationName(locationName, 1);

                        if (addressList != null && addressList.size() > 0) {
                            Address address = addressList.get(0);
                            LatLng addressLatLng = new LatLng(address.getLatitude(), address.getLongitude());

                            _homeObj.findLocation(addressLatLng);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    //// HOME FRAGMENT ////
    public void resizeMapOnClick(View view) {
        view.startAnimation(buttonClickEffect);
        ViewGroup.LayoutParams params = findViewById(R.id.map).getLayoutParams();
        Button bButton = findViewById(R.id.resizeMapViewBtn);
//TODO: add better marks or pictures instead of 'v'
        //Toast.makeText(getActivity(), "Selected: " , Toast.LENGTH_SHORT).show();
        int minMapSize = (int) (getResources().getDimension(R.dimen.zoom_button_size) / getResources().getDisplayMetrics().density) * 2;

        if (params.height > dpToPixels(minMapSize)) {
            params.height = dpToPixels(minMapSize);
            bButton.setText("v");
        } else {
            params.height = dpToPixels(((int) (getResources().getDimension(R.dimen.map_max_size) / getResources().getDisplayMetrics().density)));
            bButton.setText("^");
        }
        findViewById(R.id.map).setLayoutParams(params);
    }

    public void increaseSearchRadiusOnClick(View view) {
        view.startAnimation(buttonClickEffect);

        if (_homeObj != null) {
            _homeObj.changeSearchRadius(0.5);
        }
    }

    public void decreaseSearchRadiusOnClick(View view) {
        view.startAnimation(buttonClickEffect);
        if (_homeObj != null) {
            _homeObj.changeSearchRadius(-0.5);
        }
    }

    public void showFilterWindowOnClick(View view) {
        view.startAnimation(buttonClickEffect);
        startActivity(new Intent(MainNavigationActivity.this, FilterWindow.class));
    }

    //// RATE RESTAURANT FRAGMENT ////
    public void searchRestaurantByNameOnClick(View view) {
        view.startAnimation(buttonClickEffect);

        if (_rateRestaurantsObj != null) {
            _rateRestaurantsObj.searchRestaurant();
        }
    }

    public void rateRestaurantOnClick(View view) {
        view.startAnimation(buttonClickEffect);

        if (_rateRestaurantsObj != null) {
            _rateRestaurantsObj.rateRestaurant();
        }
    }

    public void setRateStar1(View view) {
        view.startAnimation(buttonClickEffect);

        if (_rateRestaurantsObj != null) {
            _rateRestaurantsObj.setRateRestaurant("1");
        }
    }

    public void setRateStar2(View view) {
        view.startAnimation(buttonClickEffect);

        if (_rateRestaurantsObj != null) {
            _rateRestaurantsObj.setRateRestaurant("2");
        }
    }

    public void setRateStar3(View view) {
        view.startAnimation(buttonClickEffect);

        if (_rateRestaurantsObj != null) {
            _rateRestaurantsObj.setRateRestaurant("3");
        }
    }

    public void setRateStar4(View view) {
        view.startAnimation(buttonClickEffect);

        if (_rateRestaurantsObj != null) {
            _rateRestaurantsObj.setRateRestaurant("4");
        }
    }

    public void setRateStar5(View view) {
        view.startAnimation(buttonClickEffect);

        if (_rateRestaurantsObj != null) {
            _rateRestaurantsObj.setRateRestaurant("5");
        }
    }

    //// ACCOUNT SETTINGS ////
    public void goToChangePasswordOnClick(View view) {
        view.startAnimation(buttonClickEffect);
        if (_accountSettingsObj != null) {
            currentFragment = 0;
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();

            hideAndShowSearchEngineOnToolbar(false);
            transaction.replace(R.id.contentFrame, _changePasswordFragment = new ChangePasswordFragment(), "change_current_password");
            transaction.commit();
        }
    }

    public void confirmFavoriteFilterOnClick(View view) {
        view.startAnimation(buttonClickEffect);
        if (_accountSettingsObj != null) {
            _accountSettingsObj.confirmFavoriteFilter();
        }
    }

    //// CHANGE PASSWORD ////
    public void changePasswordOnClick(View view) {
        view.startAnimation(buttonClickEffect);
        if (_changePasswordFragment != null) {
            _changePasswordFragment.changeCurrentPassword(this);
        }
    }

    public void logoutMeAfterChangingPassword() {
        removeFile(getString(R.string.config_file_name));
        removeFile(getString(R.string.filter_file_name));
        removeFile(getString(R.string.favorite_filter_file_name));
        Toast.makeText(this, "Hasło zostało zmienione", Toast.LENGTH_SHORT).show();
        updateNameOfLoginItemMenu("Zaloguj");

        hideAndShowSearchEngineOnToolbar(false);

        currentFragment = R.id.nav_login;
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.contentFrame, _loginObj = new LoginFragment(), "login");
        transaction.commit();
    }

    //// LOGIN FRAGMENT ////
    public void signInOnClick(View view) {
        if (_loginObj != null) {

            /*
            // TODO: tmp for remove
            writeToFile(getString(R.string.config_file_name), "admin&admin@tomik.com&8C6976E5B5410415BDE908BD4DEE15DFB167A9C873FC4BB8A81F6F2AB448A918");
            removeFile(getString(R.string.filter_file_name));
            updateNameOfLoginItemMenu("Wyloguj");
            Toast.makeText(this, "Zalogowano", Toast.LENGTH_SHORT).show();
            */

            _loginObj.signIn(this);
        }
        view.startAnimation(buttonClickEffect);
    }

    public void goToRegistrationOnClick(View view) {
        view.startAnimation(buttonClickEffect);
        if (_loginObj != null) {
            currentFragment = 0;
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();

            hideAndShowSearchEngineOnToolbar(false);
            transaction.replace(R.id.contentFrame, _registrationUserFragment = new RegistrationUserFragment(), "registration_new_user");
            transaction.commit();
        }
    }

    public void moveMeToHomeFragmentAfterSignIn() {
        updateNameOfLoginItemMenu("Wyloguj");
        hideAndShowSearchEngineOnToolbar(false);

        currentFragment = R.id.nav_home;
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.contentFrame, _homeObj = new HomeFragment(), "home");
        transaction.commit();
    }

    //// REGISTRATION FRAGMENT ////
    public void createNewUserAccountOnClick(View view) {
        view.startAnimation(buttonClickEffect);
        if (_registrationUserFragment != null) {
            _registrationUserFragment.registryNewUser(this);
        }
    }

    public void moveMeToLoginFragmentAfterRegistration() {
        Toast.makeText(this, "Konto zostało utworzone", Toast.LENGTH_SHORT).show();

        hideAndShowSearchEngineOnToolbar(false);

        currentFragment = R.id.nav_login;
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.contentFrame, _loginObj = new LoginFragment(), "login");
        transaction.commit();
    }

///////////////////////////////////////////////////////////////////////////////////////
}