package com.example.rewards;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static java.net.HttpURLConnection.HTTP_OK;

class RewardsAPIAsyncTask extends AsyncTask<String, Void, String> {
    RewardActivity awardActivity;
    UserProfiles data;
    int points;
    String comments;
    private static final String TAG = "RewardsAPIAsyncTask";
    private static final String baseUrl = "http://inspirationrewardsapi-env.6mmagpm2pv.us-east-2.elasticbeanstalk.com";
    private static final String rewardsEndPoint = "/rewards";

    public RewardsAPIAsyncTask(RewardActivity awardActivity, UserProfiles data, int points, String comments) {
        this.awardActivity = awardActivity;
        this.data = data;
        this.points = points;
        this.comments = comments;
    }

    @Override
    protected String doInBackground(String... strings) {
        String uname = strings[0];
        String pswd = strings[1];
        String fname = strings[2];
        String lname = strings[3];
        JSONObject target = new JSONObject();
        try {
            target.put("studentId", "A20446287");
            target.put("username", data.getUsername());
            String name = fname + " " + lname;
            target.put("name",name);
            DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
            target.put("date",dateFormat.format(Calendar.getInstance().getTime()));
            target.put("notes", comments);
            target.put("value", points);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JSONObject source = new JSONObject();
        try {
            source.put("studentId", "A20446287");
            source.put("username", uname);
            source.put("password", pswd);
        } catch (Exception e) {
            e.printStackTrace();
        }

        JSONObject object = new JSONObject();
        try {
            object.put("target",target);
            object.put("source",source);
            Log.d(TAG, "doInBackground: "+ object.toString());
            return doAPICall(object);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String doAPICall(JSONObject jsonObject) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {

            String urlString = baseUrl + rewardsEndPoint;
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
        return "Error Occured";
    }

    @Override
    protected void onPostExecute(String s) {
        if (s.contains("error")) {
            awardActivity.sendResult("Failed", s);
        } else {
            awardActivity.sendResult("Success", s);
        }
    }
}

