package com.example.rewards;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import static java.net.HttpURLConnection.HTTP_OK;

class LoginAsync extends AsyncTask<String, Void, String> {
    @SuppressLint("StaticFieldLeak")
    MainActivity mainActivity;
    EditActivity editActivity;
    ProfileActivity profileActivity;
    CreateProfileActivity createProfileActivity;
    private static final String TAG = "LoginAsync";
    private static final String baseUrl = "http://inspirationrewardsapi-env.6mmagpm2pv.us-east-2.elasticbeanstalk.com";
    private static final String loginEndPoint = "/login";
    private boolean isMainActivity = false;
    private boolean isEditProfileActivity = false;
    private boolean isProfileActivity = false;
    private boolean profileUpdated = false;


    public LoginAsync(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        isMainActivity = true;
    }

    public LoginAsync(EditActivity editActivity, boolean profileUpdated) {
        this.editActivity = editActivity;
        isEditProfileActivity = true;
        this.profileUpdated = profileUpdated;
    }

    public LoginAsync(ProfileActivity profileActivity) {
        this.profileActivity = profileActivity;
        isProfileActivity = true;
    }

    public LoginAsync(CreateProfileActivity createProfileActivity){
        this.createProfileActivity = createProfileActivity;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if(isEditProfileActivity){
            editActivity.progressBar.setVisibility(View.VISIBLE);
        }
        if(isMainActivity){
            mainActivity.progressBar.setVisibility(View.VISIBLE);
        }
        if(isProfileActivity){
            profileActivity.progressBar.setVisibility(View.VISIBLE);
        }

    }

    @Override
    protected String doInBackground(String... strings) {
        String uname = strings[0];
        String pass = strings[1];
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("studentId", "A20446287");
            jsonObject.put("username", uname);
            jsonObject.put("password", pass);
            return doAPICall(jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String doAPICall(JSONObject jsonObject) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {

            String urlString = baseUrl + loginEndPoint;
            Uri uri = Uri.parse(urlString);
            URL url = new URL(uri.toString());
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");

            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.connect();

            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
            out.write(jsonObject.toString());
            out.close();

            int responseCode = connection.getResponseCode();

            StringBuilder result = new StringBuilder();

            if (responseCode == HTTP_OK) {

                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while (null != (line = reader.readLine())) {
                    result.append(line).append("\n");
                }
                return result.toString();
            } else {
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                String line;
                while (null != (line = reader.readLine())) {
                    result.append(line).append("\n");
                }
                return result.toString();
            }

        } catch (Exception e) {
            Log.d(TAG, "doAuth: " + e.getClass().getName() + ": " + e.getMessage());

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, "doInBackground: Error closing stream: " + e.getMessage());
                }
            }
        }
        return "Oops, Some error has occurred";
    }


    @Override
    protected void onPostExecute(String s) {
        if (isMainActivity) {
            mainActivity.progressBar.setVisibility(View.INVISIBLE);
            if (s.contains("error")) {
                mainActivity.sendResult("Failed", s);
            } else {
                mainActivity.sendResult("Success", s);
            }
        }

        if(isEditProfileActivity) {
            editActivity.progressBar.setVisibility(View.INVISIBLE);
            if (s.contains("error")) {
                editActivity.sendResult("Failed", s);
            } else {
                editActivity.sendResult("Success", s);
                if(profileUpdated){
                    editActivity.afterUpdate();
                }
            }
        }

        if(isProfileActivity){
            profileActivity.progressBar.setVisibility(View.INVISIBLE);
            if(s.contains("error")){
                profileActivity.sendResult("Failed",s);
            }
            else{
                profileActivity.sendResult("Success",s);
            }
        }
    }



}
