package com.example.rewards;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int ALL_PERMISSIONS = 100;
    private static final int LOCATIONS = 101;
    LocationManager locationManager;
    Location currentLocation;
    Criteria criteria;
    Button newProfile;
    Button login;
    EditText username;
    EditText password;
    CheckBox checkBox_remember_credentials;
    public ProgressBar progressBar;
    String api_response = "";
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        newProfile = findViewById(R.id.newAccount_mainActivity);
        login = findViewById(R.id.login_mainActivity);
        username = findViewById(R.id.username_mainActivity);
        password = findViewById(R.id.password_mainActivity);
        checkBox_remember_credentials = findViewById(R.id.savedata_mainActivity);
        checkBox_remember_credentials.setChecked(false);
        progressBar = findViewById(R.id.progressBar_profile);
        progressBar.setVisibility(View.INVISIBLE);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        criteria = new Criteria();
        criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
        criteria.setAccuracy(Criteria.ACCURACY_MEDIUM);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);



        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateCred()) {
                    new LoginAsync(MainActivity.this).execute(username.getText().toString(), password.getText().toString());
                }
            }
        });

        newProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CreateProfileActivity.class));
            }
        });
        String t_uname;
        String t_pwd;

        SharedPreferences preferences = getSharedPreferences("credentials", Context.MODE_PRIVATE);
        if (preferences != null) {
            t_uname = preferences.getString("username", "");
            t_pwd = preferences.getString("password", "");
            if (t_uname != null && t_pwd != null) {
                username.setText(t_uname);
                password.setText(t_pwd);
                checkBox_remember_credentials.setChecked(preferences.getBoolean("remember", false));
            }
        }

    }

    private boolean validateCred() {
        if (validateUname() && validatePwd()) {
            return true;
        } else {
            return false;
        }
    }

    private boolean validatePwd() {
        if (password.getText().toString().isEmpty()) {
            password.setError("Enter your password");
            return false;
        }
        return true;
    }

    private boolean validateUname() {
        if (username.getText().toString().isEmpty()) {
            username.setError("Enter your Username");
            return false;
        }
        return true;
    }

    public void sendResult(String result, String response) {
        CustomToast customToast = new CustomToast(MainActivity.this);
        api_response = response;
        String status="";String message="";
        Log.d(TAG, "profileResults: " + response);
        if (result.toLowerCase().contains("failed")) {
            try {
                JSONObject json = new JSONObject(api_response);
                String s = json.getString("errordetails");
                JSONObject jsonObject = new JSONObject(s);
                status = jsonObject.getString("status");
                message = jsonObject.getString("message");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            customToast.showCustomToast(status + " " + message, Color.RED);
        } else {
            customToast.showCustomToast("Sign In: " + result, Color.GREEN);
            logInSuccessful();
        }

    }

    private void logInSuccessful() {
        Log.d(TAG, "logged In Successfully");
        getSharedPreferences("credentials",MODE_PRIVATE).edit().clear().apply();
        if (checkBox_remember_credentials.isChecked()) {
            SharedPreferences preferences = getSharedPreferences("credentials", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("username", username.getText().toString());
            editor.putString("password", password.getText().toString());
            editor.putBoolean("remember", true);
            editor.apply();
        }
        try {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class).putExtra("response_data", api_response));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private boolean check_Permissions() {
        int GPS_FINE = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int GPS_COARSE = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        List<String> permissionRequired = new ArrayList<>();
        if (GPS_FINE != PackageManager.PERMISSION_GRANTED) {
            permissionRequired.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (GPS_COARSE != PackageManager.PERMISSION_GRANTED) {
            permissionRequired.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        if (!permissionRequired.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionRequired.toArray(new String[permissionRequired.size()]), ALL_PERMISSIONS);
            return false;
        }

        return true;
    }

    private String getPlace(Location loc) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            return city + ", " + state;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case ALL_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "onRequestPermissionsResult: Permissions Granted");
                    setLocation();
                }
            }
            case LOCATIONS : {
                if (grantResults.length > 0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    setLocation();
                }
            }
        }
    }

    private void setLocation() {
        String bestProvider = locationManager.getBestProvider(criteria, true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, LOCATIONS);
        }
        currentLocation = locationManager.getLastKnownLocation(bestProvider);
        Log.d(TAG, "setLocation: " + String.valueOf(currentLocation));
        Log.d(TAG, "setLocation: " + getPlace(currentLocation));
    }



    public void sendAllProfileDeleteResponse(String result, String response) {
        Log.d(TAG, "AllProfileDeleteResponse: " + response);
        CustomToast customToast = new CustomToast(MainActivity.this);
        if (result.toLowerCase().contains("failed")) {
            customToast.showCustomToast("Process: " + result, Color.RED);
        } else {
            customToast.showCustomToast("Process: " + result, Color.GREEN);

        }
    }
}
