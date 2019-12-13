package com.example.rewards;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import static java.net.HttpURLConnection.HTTP_OK;

class UpdateProfileAsyncTask extends AsyncTask<String,Void,String> {

    private static final String TAG = "UpdateProfileAsyncTask";
    EditActivity editProfileActivity;
    List<Rewards> rewardsList;
    private static final String baseUrl = "http://inspirationrewardsapi-env.6mmagpm2pv.us-east-2.elasticbeanstalk.com";
    private static final String createEndPoint = "/profiles";
    public UpdateProfileAsyncTask(EditActivity editProfileActivity, List<Rewards> rewardsList) {
        this.editProfileActivity = editProfileActivity;
        this.rewardsList = rewardsList;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.d(TAG, "onPreExecute: ");
    }

    @Override
    protected String doInBackground(String... strings) {


        String uname = strings[0];
        String pswd = strings[1];
        boolean check;
        check = strings[2].equals("1");
        String firstName = strings[3];
        String lastName = strings[4];
        String dept = strings[5];
        String position = strings[6];
        String story = strings[7];
        String location = strings[8];
        String encodedImage = strings[9];
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("studentId", "A20446287");
            jsonObject.put("username", uname);
            jsonObject.put("password", pswd);
            jsonObject.put("admin", check);
            jsonObject.put("firstName", firstName);
            jsonObject.put("lastName", lastName);
            jsonObject.put("pointsToAward", 1000);
            jsonObject.put("department", dept);
            jsonObject.put("position", position);
            jsonObject.put("story", story);
            jsonObject.put("location", location);
            jsonObject.put("imageBytes", encodedImage);
            if(rewardsList.size() == 0){
                JSONArray jsonArray = new JSONArray();
                jsonObject.put("rewardRecords",jsonArray);
            }else{
                JSONArray jsonArray = new JSONArray(rewardsList);
                jsonObject.put("rewardRecords",jsonArray);
            }
            return doAPICall(jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "Ending doInBackground");
        return null;
    }

    private String doAPICall(JSONObject jsonObject) {
        Log.d(TAG, "Starting doAPICall");
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {

            String urlString = baseUrl + createEndPoint;
            Uri uri = Uri.parse(urlString);
            URL url = new URL(uri.toString());
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PUT");

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
        Log.d(TAG, "Ending doAPICall");
        return "Error Occured";
    }

    @Override
    protected void onPostExecute(String s) {
        if(s.contains("error")){
            editProfileActivity.updatedResults("Update Failed",s);
        }
        else{
            editProfileActivity.updatedResults("Updated Successfully",s);
        }
    }
}
