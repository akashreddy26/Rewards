package com.example.rewards;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class RewardActivity extends AppCompatActivity {

    TextView textView_name, textView_pointsReceived, textView_department, textView_position, story, char_num;
    EditText pointsToSend, comments;
    ImageView profilePicture;

    UserProfiles data;
    String currentUser, pass;
    String name;
    int pointsToAward;
    private static final int MAX_CHARS = 80;
    private static final String TAG = "RewardActivity";
    String fname,lname;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reward);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.icon);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getIntent().hasExtra("profile_data")) {
            data = (UserProfiles) getIntent().getSerializableExtra("profile_data");
        }
        if (getIntent().hasExtra("username") && getIntent().hasExtra("password")) {
            currentUser = getIntent().getStringExtra("username");
            pass = getIntent().getStringExtra("password");
        }
        if(getIntent().hasExtra("pointsToAward")){
            pointsToAward = getIntent().getIntExtra("pointsToAward",0);
        }
        name = data.getFirstName() + "," + data.getLastName();
        getSupportActionBar().setTitle(name);

        if(getIntent().hasExtra("firstname") && getIntent().hasExtra("lastname")){
            fname = getIntent().getStringExtra("firstname");
            lname = getIntent().getStringExtra("lastname");
        }

        textView_name = findViewById(R.id.award_name);
        textView_pointsReceived = findViewById(R.id.award_ptsAwarded);
        textView_department = findViewById(R.id.award_dept);
        textView_position = findViewById(R.id.award_position);
        story = findViewById(R.id.award_story);
        char_num = findViewById(R.id.award_char);

        profilePicture = findViewById(R.id.award_img);

        pointsToSend = findViewById(R.id.award_points_Sent);
        comments = findViewById(R.id.award_comments);
        comments.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_CHARS)});
        comments.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String char_text = "(" + s.toString().length() + " of " + MAX_CHARS + ")";
                char_num.setText(char_text);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        profilePicture.setImageBitmap(getBitmapImage(data.getImageByteEncoded()));
        String s = data.getLastName() + " ," + data.getFirstName();
        textView_name.setText(s);
        textView_pointsReceived.setText(Integer.toString(data.pointsToAward));
        textView_department.setText(data.getDepartment());
        textView_position.setText(data.getPosition());
        story.setText(data.getStory());


    }

    private boolean checkField() {
        return arePointsEntered() && ifCommentAvailable();
    }

    private boolean ifCommentAvailable() {
        if (comments.getText().toString().isEmpty()) {
            new CustomToast(RewardActivity.this).showCustomToast("Please enter some comment", Color.RED);
            comments.setError("Enter Some Comment");
            return false;
        }
        return true;
    }

    private boolean arePointsEntered() {
        if (pointsToSend.getText().toString().isEmpty()) {
            pointsToSend.setError("Value cannot be empty");
            return false;
        } else if (Integer.parseInt(pointsToSend.getText().toString()) < 0
                || Integer.parseInt(pointsToSend.getText().toString()) > pointsToAward) {
            pointsToSend.setError("Enter values according to range");
            new CustomToast(RewardActivity.this).showCustomToast("Points have to be between 0 and "
                    + pointsToAward, Color.RED);
            return false;
        } else {
            return true;
        }

    }

    private int getTotalPoints(UserProfiles data) {
        int sum = 0;
        for(Rewards content : data.rewards){
            sum += content.getValue();
        }
        return sum;
    }

    private Bitmap getBitmapImage(String imageByteEncoded) {
        byte[] decodedString = Base64.decode(imageByteEncoded, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_award, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_award:
                if (checkField()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RewardActivity.this);
                    name = name.replace(",", " ");
                    builder.setTitle("Add Reward Points?")
                            .setIcon(R.drawable.icon)
                            .setMessage("Add rewards for " + name)
                            .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    new RewardsAPIAsyncTask(
                                            RewardActivity.this,
                                            data,
                                            Integer.parseInt(pointsToSend.getText().toString()),
                                            comments.getText().toString()).execute(currentUser, pass,fname,lname);
                                }
                            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            return;
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
        }
        return super.onOptionsItemSelected(item);
    }



    public void sendResult(String result, String response) {
        Log.d(TAG, "profileResults: " + response);
        //CustomToast customToast = new CustomToast(RewardActivity.this);
        String status = "";
        String message = "";
        if (result.toLowerCase().contains("failed")) {
            try {
                JSONObject json = new JSONObject(response);
                String s = json.getString("errordetails");
                JSONObject jsonObject = new JSONObject(s);
                status = jsonObject.getString("status");
                message = jsonObject.getString("message");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            startActivity(new Intent(RewardActivity.this, LeaderBoardActivity.class)
                    .putExtra("username", currentUser)
                    .putExtra("password", pass)
                    .putExtra("pointsToAward",pointsToAward));
            finish();
        }
    }


    @Override
    public void onBackPressed() {
        startActivity(new Intent(RewardActivity.this,ProfileActivity.class).putExtra("username",currentUser).putExtra("password",pass));
    }
}
