package com.example.rewards;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
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
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.graphics.Bitmap.CompressFormat.JPEG;

public class EditActivity extends AppCompatActivity {
    ImageView proPic;
    EditText uname, pass, fname, lname, dept, pos, storyAbt;
    CheckBox ifAdmin;
    TextView charCount;
    ProgressBar progressBar;
    private LocationManager locationManager;
    private Criteria criteria;
    private static final int MAX_CHARS = 360;
    private int OPEN_CAMERA_GALLERY = 1;
    private int OPEN_CAMERA_CAPTURE = 2;
    private final static int ALL_PERMISSIONS = 100;
    private static final String TAG = "EditActivity";
    Location currentLocation;

    String user, password;
    Bitmap profileImgBitmap;
    String t_user, t_pass;
    String imgSrc;
    boolean admin;
    String first_name, last_name, username, department, position, story, imageBytes, location;
    int pointsToAward;
    Bitmap bitmap;
    private String api_response;
    List<Rewards> rewardsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        SharedPreferences preferences = getSharedPreferences("credentials", Context.MODE_PRIVATE);
        rewardsList = new ArrayList<>();


        if (checkPermit()) {
            Log.i(TAG, "onCreate: REQUESTS_OK");
        }

        if (getIntent().hasExtra("username") && getIntent().hasExtra("password")) {
            user = getIntent().getStringExtra("username");
            password = getIntent().getStringExtra("password");
        } else if (preferences != null) {
            t_user = preferences.getString("username", "");
            t_pass = preferences.getString("password", "");
            if (t_user != null && t_pass != null) {
                user = t_user;
                password = t_pass;
            }
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Error")
                    .setIcon(R.drawable.icon)
                    .setMessage("Oops, Something went Bad")
                    .setCancelable(false)
                    .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(EditActivity.this, ProfileActivity.class));
                        }
                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }

        proPic = findViewById(R.id.PROFILE_PIC);
        uname = findViewById(R.id.username_profile);
        pass = findViewById(R.id.password_profile);
        ifAdmin = findViewById(R.id.admin_checkbox);
        fname = findViewById(R.id.firstname_profile);
        lname = findViewById(R.id.lastname_profile);
        dept = findViewById(R.id.dept_profile);
        pos = findViewById(R.id.position_profile);
        storyAbt = findViewById(R.id.story_profile);
        charCount = findViewById(R.id.CHAR_COUNT);
        progressBar = findViewById(R.id.progressBar_profile);
        progressBar.setVisibility(View.INVISIBLE);
        uname.setClickable(false);
        storyAbt.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_CHARS)});
        storyAbt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String char_text = "(" + s.toString().length() + " of " + MAX_CHARS + ")";
                charCount.setText(char_text);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        criteria = new Criteria();
        criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
        criteria.setAccuracy(Criteria.ACCURACY_MEDIUM);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);

        if (currentLocation == null) {
            fixLocation();
        }

        new LoginAsync(EditActivity.this, false).execute(user, password);
        proPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOptionsDialog();
            }
        });

        View logo = toolbar.getChildAt(0);
        logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: logo "+username+password);
                startActivity(new Intent(EditActivity.this, ProfileActivity.class)
                        .putExtra("username",username)
                        .putExtra("password",password));
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_edit_profile:
                saveData();
        }
        return super.onOptionsItemSelected(item);
    }


    private boolean checkPermit() {
        int Camera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int WriteStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int ReadStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int GPS_FINE = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int GPS_COARSE = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        List<String> permissionRequired = new ArrayList<>();
        if (Camera != PackageManager.PERMISSION_GRANTED) {
            permissionRequired.add(Manifest.permission.CAMERA);
        }
        if (WriteStorage != PackageManager.PERMISSION_GRANTED) {
            permissionRequired.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (ReadStorage != PackageManager.PERMISSION_GRANTED) {
            permissionRequired.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case ALL_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "onRequestPermissionsResult: Permissions Granted");
                    fixLocation();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == OPEN_CAMERA_CAPTURE) {
            if (resultCode == RESULT_OK) {
                camImg();
            }
        }
        if (requestCode == OPEN_CAMERA_GALLERY) {
            if (resultCode == RESULT_OK) {
                galleryImg(data);
            }
        }
    }

    private void fixLocation() {
        String bestProvider = locationManager.getBestProvider(criteria, true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
        }
        currentLocation = locationManager.getLastKnownLocation(bestProvider);
    }

    private String getLocation(Location loc) {
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

    private void showOptionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.icon);
        builder.setTitle("Choose Profile Picture");
        builder.setMessage("Take Picture From:");
        builder.setPositiveButton("Camera", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                pictureCamera();
            }
        }).setNegativeButton("Gallery", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                imageFromGallery();
            }
        });
        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void pictureCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {

            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.rewards.fileprovider", photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(cameraIntent, OPEN_CAMERA_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_temp";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        this.imgSrc = image.getAbsolutePath();
        return image;
    }

    private void imageFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, OPEN_CAMERA_GALLERY);
    }

    private void saveData() {
        Log.d(TAG, "Starting saveData");
        if (findFields()) {
            Log.d(TAG, "check field: Start");
            new UpdateProfileAsyncTask(this, rewardsList).execute(
                    uname.getText().toString(),
                    pass.getText().toString(),
                    ifAdmin.isChecked() ? "1" : "0",
                    fname.getText().toString(),
                    lname.getText().toString(),
                    dept.getText().toString(),
                    pos.getText().toString(),
                    storyAbt.getText().toString(),
                    getLocation(currentLocation),
                    getEncodedImage(bitmap)
            );
            Log.d(TAG, "Ending checkfield");
        } else {
            new CustomToast(EditActivity.this).showCustomToast("Oops, Something went Bad", Color.RED);
        }
        Log.d(TAG, "Ending saveData");
    }

    private String getEncodedImage(Bitmap bitmap) {
        if (bitmap == null) {
            bitmap = profileImgBitmap;
        }
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        bitmap.compress(JPEG, 70, byteArray);
        byte[] imageBytes = byteArray.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    private boolean findFields() {
        return isUname() && crtPwd()
                && crtFname() && crtLname()
                && crtDept() && crtPosition()
                && crtStory();
    }

    private boolean crtStory() {
        if (storyAbt.getText().toString().isEmpty() || storyAbt.getText().toString().length() > 360) {
            storyAbt.setError("Please Enter Story");
            return false;
        } else {
            return true;
        }
    }

    private boolean crtPosition() {
        if (pos.getText().toString().isEmpty()) {
            pos.setError("Please Enter Position");
            return false;
        } else {
            return true;
        }
    }

    private boolean crtDept() {
        if (dept.getText().toString().isEmpty()) {
            dept.setError("Please Enter Department Name");
            return false;
        } else {
            return true;
        }
    }

    private boolean crtLname() {
        if (lname.getText().toString().isEmpty()) {
            lname.setError("Please Enter Last Name");
            return false;
        } else {
            return true;
        }
    }

    private boolean crtFname() {
        if (fname.getText().toString().isEmpty()) {
            fname.setError("Please Enter First Name");
            return false;
        } else {
            return true;
        }
    }

    private boolean crtPwd() {
        if (pass.getText().toString().isEmpty()) {
            pass.setError("Please Enter Valid Password");
            return false;
        } else {
            return true;
        }
    }

    private boolean isUname() {
        if (uname.getText().toString().isEmpty()) {
            uname.setError("Please Enter Valid UserName");
            return false;
        } else {
            return true;
        }
    }

    private void galleryImg(Intent data) {
        Uri imageUri = data.getData();
        if (imageUri == null) {
            return;
        }
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            proPic.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void camImg() {
        bitmap = BitmapFactory.decodeFile(imgSrc);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,70,byteArrayOutputStream);
        Bitmap final_image = BitmapFactory.decodeStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
        proPic.setImageBitmap(final_image);

    }

    public void sendResult(String result, String response) {
        api_response = response;
       // String status = "";
        //String message = "";
        if (result.toLowerCase().contains("failed")) {
            try {
                JSONObject json = new JSONObject(response);
                String s = json.getString("errors");
                JSONObject jsonObject = new JSONObject(s);
                //status = jsonObject.getString("status");
                //message = jsonObject.getString("message");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            successfulLog();
        }
    }

    private void successfulLog() {
        parseJSONData(api_response);
    }

    private void parseJSONData(String api_response) {
        try {
            JSONObject jsonObject = new JSONObject(api_response);
            first_name = jsonObject.getString("firstName");
            last_name = jsonObject.getString("lastName");
            username = jsonObject.getString("username");
            department = jsonObject.getString("department");
            story = jsonObject.getString("story");
            admin = jsonObject.getBoolean("admin");
            position = jsonObject.getString("position");
            pointsToAward = jsonObject.getInt("pointsToAward");
            imageBytes = jsonObject.getString("imageBytes");
            location = jsonObject.getString("position");
            password = jsonObject.getString("password");
            JSONArray jsonArray = jsonObject.getJSONArray("rewards");
            for (int i = 0; i < jsonArray.length(); i++) {
                Rewards rewards = new Rewards();
                JSONObject object = jsonArray.getJSONObject(i);
                rewards.setUsername(object.getString("username"));
                rewards.setName(object.getString("name"));
                rewards.setDate(object.getString("date"));
                rewards.setNotes(object.getString("notes"));
                rewards.setValue(object.getInt("value"));
                rewardsList.add(rewards);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        putData();
    }

    private void putData() {
        uname.setText(username);
        fname.setText(first_name);
        lname.setText(last_name);
        pass.setText(password);
        pos.setText(position);
        ifAdmin.setChecked(admin);
        dept.setText(department);
        storyAbt.setText(story);
        proPic.setImageBitmap(getDecodedProfileBitmap(imageBytes));
    }

    private Bitmap getDecodedProfileBitmap(String imageBytes) {
        byte[] decodedString = Base64.decode(imageBytes, Base64.DEFAULT);
        profileImgBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        return profileImgBitmap;
    }

    public void updatedResults(String result, String response) {
        Log.d(TAG, "updatedResults: " + response);
        CustomToast customToast = new CustomToast(EditActivity.this);
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
            customToast.showCustomToast(status + " " + message, Color.RED);
        } else {
            customToast.showCustomToast("Process: " + result, Color.GREEN);
            new LoginAsync(EditActivity.this, true).execute(user, password);

        }
    }

    public void afterUpdate() {
        Intent intent = new Intent(EditActivity.this, ProfileActivity.class);
        intent.putExtra("response_data", api_response);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: " + username+password);
        startActivity(new Intent(EditActivity.this, ProfileActivity.class)
                .putExtra("username",username)
                .putExtra("password",password));
        finish();
    }
}
