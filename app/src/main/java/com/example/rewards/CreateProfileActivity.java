package com.example.rewards;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
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

public class CreateProfileActivity extends AppCompatActivity {

    ImageView profilePic;
    EditText username, password, firstName, lastName, department, position, story;
    CheckBox isAdmin;
    TextView charCount;
    ProgressBar progressBar;

    private LocationManager locationManager;
    Location currentLocation;
    private Criteria criteria;
    public static int MAX_CHARS = 360;
    private static final String TAG = "CreateProfileActivity";
    private int OPEN_CAMERA_GALLERY = 0;
    private int OPEN_CAMERA_CAPTURE = 1;
    private final static int ALL_PERMISSIONS = 333;
    String imageFileName;
    Bitmap bitmap;

    String status = "";
    String message = "";

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_profile);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (check_Permissions()) {
            Log.i(TAG, "onCreate: REQUESTS_OK");

        }


        profilePic = findViewById(R.id.PROFILE_PIC);
        username = findViewById(R.id.USERNAME);
        password = findViewById(R.id.PASSWORD);
        isAdmin = findViewById(R.id.IS_ADMIN);
        firstName = findViewById(R.id.FName);
        lastName = findViewById(R.id.LName);
        department = findViewById(R.id.DEPARTMENT);
        position = findViewById(R.id.POSITION);
        story = findViewById(R.id.story_new);
        charCount = findViewById(R.id.CHAR_COUNT);
        progressBar = findViewById(R.id.progressBar_new);



        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOptionsDialog();
            }
        });

        story.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_CHARS)});
        story.addTextChangedListener(new TextWatcher() {
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
        setLocation();
        Log.e(TAG, "onCreate: " + getPlace(currentLocation));

    }

    private void setLocation() {
        String bestProvider = locationManager.getBestProvider(criteria, true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        currentLocation = locationManager.getLastKnownLocation(bestProvider);
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

    private void showOptionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.icon);
        builder.setTitle("Profile Picture");
        builder.setMessage("Choose Picture From:");
        builder.setPositiveButton("Camera", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cameraImage();
            }
        }).setNegativeButton("Gallery", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, OPEN_CAMERA_GALLERY);            }
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

    public void sendResult(String result, String response) {
        Log.d(TAG, "profileResults: " + response);
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
            finish();
        }
    }

    private void cameraImage() {
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
        this.imageFileName = image.getAbsolutePath();
        return image;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_create_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.saveProfile:
                saveData();
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveData() {
        if (validateField()) {
            new NewProfileAsyncTask(this).execute(
                    username.getText().toString(),
                    password.getText().toString(),
                    isAdmin.isChecked() ? "1" : "0",
                    firstName.getText().toString(),
                    lastName.getText().toString(),
                    department.getText().toString(),
                    position.getText().toString(),
                    story.getText().toString(),
                    getPlace(currentLocation),
                    getEncodedImage(bitmap)
            );
        } else {
            new CustomToast(CreateProfileActivity.this).showCustomToast("Error Occured", Color.RED);
        }
    }

    private String getEncodedImage(Bitmap bitmap) {
        if (bitmap == null) {
            return "";
        }
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArray);
        byte[] imageBytes = byteArray.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    private boolean validateField() {
        if ((username.getText().toString().isEmpty()) || (password.getText().toString().isEmpty())
                || (firstName.getText().toString().isEmpty()) || (lastName.getText().toString().isEmpty())
                || (department.getText().toString().isEmpty()) || (position.getText().toString().isEmpty())
                || (story.getText().toString().isEmpty() || story.getText().toString().length() > 360))
        {
            new CustomToast(CreateProfileActivity.this).showCustomToast("Enter all fields", Color.GREEN);

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
                    setLocation();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == OPEN_CAMERA_CAPTURE) {
            if (resultCode == RESULT_OK) {
                bitmap = BitmapFactory.decodeFile(imageFileName);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG,70,byteArrayOutputStream);
                Bitmap final_image = BitmapFactory.decodeStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
                profilePic.setImageBitmap(final_image);
            }
        }
        if (requestCode == OPEN_CAMERA_GALLERY) {
            if (resultCode == RESULT_OK) {
                Uri imageUri = data.getData();
                if (imageUri == null) {
                    return;
                }
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                    profilePic.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private boolean check_Permissions() {
        int Camera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int WriteStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int ReadStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int GPS_FINE = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int GPS_COARSE = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int INTERNET = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET);
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

        if (INTERNET != PackageManager.PERMISSION_GRANTED) {
            permissionRequired.add(Manifest.permission.INTERNET);
        }

        if (!permissionRequired.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionRequired.toArray(new String[permissionRequired.size()]), ALL_PERMISSIONS);
            return false;
        }

        return true;
    }



}
