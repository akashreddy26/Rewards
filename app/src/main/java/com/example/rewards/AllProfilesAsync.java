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

class AllProfilesAsync extends AsyncTask<String, Void, String> {
    @SuppressLint("StaticFieldLeak")
    LeaderBoardActivity leaderBoardActivity;
    private static final String TAG = "AllProfilesAsync";
    private static final String baseUrl = "http://inspirationrewardsapi-env.6mmagpm2pv.us-east-2.elasticbeanstalk.com";
    private static final String ALLPROFILES = "/allprofiles";

    public AllProfilesAsync(LeaderBoardActivity leaderBoardActivity) {
        this.leaderBoardActivity = leaderBoardActivity;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        leaderBoardActivity.progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected String doInBackground(String... strings) {

        String uname = strings[0];
        String pswd = strings[1];
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("studentId", "A20446287");
            jsonObject.put("username", uname);
            jsonObject.put("password", pswd);
            Log.d(TAG, "doInBackground: " + jsonObject.toString());
            return APIRequest(jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "Ending doInBackground");
        return null;
    }

    private String APIRequest(JSONObject jsonObject) {
        Log.d(TAG, "Starting APIRequest");
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {

            String urlString = baseUrl + ALLPROFILES;
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
        return "Error";
    }

    @Override
    protected void onPostExecute(String s) {
        leaderBoardActivity.progressBar.setVisibility(View.INVISIBLE);
        if (s.contains("error")) {
            leaderBoardActivity.profileResults("Failed", "");
        } else {
            leaderBoardActivity.profileResults("Success", s);
        }
    }
}