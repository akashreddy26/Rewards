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

import static java.net.HttpURLConnection.HTTP_OK;

class DeleteAllUsersAsyncTask extends AsyncTask<String,Void,String> {
    private static final String TAG = "DeleteAllUsersAsyncTask";
    private static final String baseUrl = "http://inspirationrewardsapi-env.6mmagpm2pv.us-east-2.elasticbeanstalk.com";
    private static final String createEndPoint = "/allprofiles";
    MainActivity mainActivity;
    public DeleteAllUsersAsyncTask(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    protected String doInBackground(String... strings) {
        String uname = strings[0];
        String pass = strings[1];
        try {
            JSONObject jsonObject = new JSONObject();

            JSONObject adminData = new JSONObject();                        // {"studentId": "A20424868", "username":"username", "password":"password"}
            adminData.put("studentId","A20446287");
            adminData.put("username",uname);
            adminData.put("password",pass);

            jsonObject.put("admin",adminData);                  // "admin" : {"studentId": "A20424868", "username":"username", "password":"password"}
            jsonObject.put("username","Visagan");                   //"username" : "username"
            Log.d(TAG, "doInBackground: "+jsonObject.toString());
            return doAPICall(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    private String doAPICall(JSONObject jsonObject) {
        Log.d(TAG, "Starting API CALL");
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {

            String urlString = baseUrl + createEndPoint; /*"https://jsonplaceholder.typicode.com/todos/1";*/
            Uri uri = Uri.parse(urlString);
            URL url = new URL(uri.toString());
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("DELETE");
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
                    Log.e(TAG, "Error in doInBackground() " + e.getMessage());
                }
            }
        }
        Log.d(TAG, "Ending API CALL");
        return "Some error has occurred";
    }

    @Override
    protected void onPostExecute(String s) {
        if(s.contains("error")){
            mainActivity.sendAllProfileDeleteResponse("Delete Failed",s);
        }
        else{
            mainActivity.sendAllProfileDeleteResponse("All profiles deleted successfully",s);
        }
    }
}