package com.example.rewards;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LeaderBoardActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "LeaderBoardActivity";
    RecyclerView recyclerView;
    LAdapter mAdapter;

    String getProfile_api_response;
    List<UserProfiles> profilesContent;
    String user;
    String pass;
    String temp_uname;
    String temp_pass;
    ProgressBar progressBar;
    int pointsToAward;
    String fname,lname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lboard);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        profilesContent = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar_leaderboard);
        progressBar.setVisibility(View.INVISIBLE);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.icon);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        SharedPreferences sharedPreferences = getSharedPreferences("credentials", Context.MODE_PRIVATE);
        if (getIntent().hasExtra("username") && getIntent().hasExtra("password") && getIntent().hasExtra("pointsToAward")) {
            user = getIntent().getStringExtra("username");
            pass = getIntent().getStringExtra("password");
            pointsToAward = getIntent().getIntExtra("pointsToAward",0);
            Log.d(TAG, "onCreate: " + user + pass);
            new AllProfilesAsync(LeaderBoardActivity.this).execute(user, pass);
        } else if (sharedPreferences != null) {
            temp_uname = sharedPreferences.getString("username", "");
            temp_pass = sharedPreferences.getString("password", "");
            if (temp_uname != null && temp_pass != null) {
                user = temp_uname;
                pass = temp_pass;
            }
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Error")
                    .setIcon(R.drawable.icon)
                    .setMessage("Oops, Something Went Bad")
                    .setCancelable(false)
                    .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(LeaderBoardActivity.this, ProfileActivity.class));
                        }
                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }

        if(getIntent().hasExtra("firstname") && getIntent().hasExtra("lastname")){
            fname = getIntent().getStringExtra("firstname");
            lname = getIntent().getStringExtra("lastname");
        }

        View logo = toolbar.getChildAt(0);
        logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LeaderBoardActivity.this,ProfileActivity.class)
                .putExtra("username",user)
                .putExtra("password",pass));
                finish();
            }
        });
    }

    private void parseJSONData(String getProfile_api_response) {
        try {
            JSONArray jsonArray = new JSONArray(getProfile_api_response);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                final UserProfiles profilesData = new UserProfiles();
                profilesData.setSid(object.getString("studentId"));
                profilesData.setFirstName(object.getString("firstName"));
                profilesData.setLastName(object.getString("lastName"));
                profilesData.setUsername(object.getString("username"));
                profilesData.setStory(object.getString("story"));
                profilesData.setPosition(object.getString("position"));
                profilesData.setPointsToAward(object.getInt("pointsToAward"));
                profilesData.setAdmin(object.getBoolean("admin"));
                profilesData.setDepartment(object.getString("department"));
                profilesData.setImageByteEncoded(object.getString("imageBytes"));
                profilesData.setLocation(object.getString("location"));
                Log.d(TAG, "parseJSONData: " + object.toString());
                String val = "null";
                if (object.has("rewards")) {
                    if (object.getString("rewards").startsWith(val)) {
                        String s = object.getString("rewards");
                        String ans = s.replace(val, "");
                        Log.d(TAG, "parseJSONData: " + String.valueOf(ans));
                    } else {
                        JSONArray objectJSONArray = object.getJSONArray("rewards");
                        for (int j = 0; j < objectJSONArray.length(); j++) {
                            Rewards rewards = new Rewards();
                            JSONObject jsonObject = objectJSONArray.getJSONObject(j);
                            rewards.setUsername(jsonObject.getString("username"));
                            rewards.setName(jsonObject.getString("name"));
                            rewards.setDate(jsonObject.getString("date"));
                            rewards.setNotes(jsonObject.getString("notes"));
                            rewards.setValue(jsonObject.getInt("value"));
                            profilesData.rewards.add(rewards);
                        }
                    }
                }
                profilesContent.add(profilesData);
                Collections.sort(profilesContent, new Comparator<UserProfiles>() {
                    @Override
                    public int compare(UserProfiles o1, UserProfiles o2) {
                        int user1 = 0;
                        for(Rewards content : o1.rewards){
                            user1 = user1+content.getValue();
                        }
                        int user2 = 0;
                        for(Rewards content : o2.rewards){
                            user2 = user2 + content.getValue();
                        }
                        return user2 - user1;
                    }
                });
                mAdapter = new LAdapter(LeaderBoardActivity.this, profilesContent, user);
                recyclerView.setAdapter(mAdapter);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onClick(View v) {
        int i = recyclerView.getChildLayoutPosition(v);
        UserProfiles data = profilesContent.get(i);
        if (data.getUsername().equals(user)) {
            new CustomToast(LeaderBoardActivity.this).showCustomToast("Self Reward not POSSIBLE", Color.RED);
            return;
        }
        Intent intent = new Intent(LeaderBoardActivity.this, RewardActivity.class);
        intent.putExtra("profile_data", data);
        intent.putExtra("username", user);
        intent.putExtra("password", pass);
        intent.putExtra("pointsToAward",pointsToAward);
        intent.putExtra("firstname",fname);
        intent.putExtra("lastname",lname);
        startActivity(intent);
        finish();
    }

    public void profileResults(String result, String response) {
        Log.d(TAG, "profileResults: " + response);
        if (result.toLowerCase().contains("failed")) {
            try {
                JSONObject json = new JSONObject(response);
                String s = json.getString("errors");
                //JSONObject jsonObject = new JSONObject(s);
            } catch (JSONException e) {
                e.printStackTrace();
            }
          } else {
            getProfile_api_response = response;
            parseJSONData(getProfile_api_response);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(LeaderBoardActivity.this,ProfileActivity.class)
        .putExtra("username",user)
        .putExtra("password",pass));
        finish();

    }
}
