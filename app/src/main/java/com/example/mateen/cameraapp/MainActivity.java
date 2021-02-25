package com.example.mateen.cameraapp;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST = 10;
    private static final int FINE_LOCATION_PERMISSION_REQUEST_CODE = 20;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 30;
    private ImageView imageView;
    private TextView locationText;
    private SQLiteDatabase database;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private String address, latitude, longitude;
    private CancellationToken cancellationToken;

    private LocationRequest locationRequest;
    public static final int REQUEST_CHECK_SETTING = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        locationText=findViewById(R.id.textView);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        CancellationToken cancellationToken;
        // Creating database and table if not exist already
        database = openOrCreateDatabase("db_images", Context.MODE_PRIVATE, null);
        database.execSQL("CREATE TABLE IF NOT EXISTS images (ID INTEGER PRIMARY KEY AUTOINCREMENT, name BLOB, address TEXT, latitude TEXT, longitude TEXT);");

    }

    public void openCamera(View view) {

        // Turn on Location prompt
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(getApplicationContext()).checkLocationSettings(builder.build());
        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                } catch (ApiException e) {
                    switch (e.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException)e;
                                resolvableApiException.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTING);
                            } catch (IntentSender.SendIntentException sendIntentException) {
                                sendIntentException.printStackTrace();
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            break;
                    }
                }
            }
        });

        // Asking for permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, CAMERA_REQUEST);
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_PERMISSION_REQUEST_CODE);
            }
        }

    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
            final byte[] byteArray = stream.toByteArray();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "Location Not Enabled", Toast.LENGTH_LONG).show();
                return;
            }

            fusedLocationProviderClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, cancellationToken).addOnCompleteListener(new OnCompleteListener<Location>() {

                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    //Initialize location
                    Location location = task.getResult();
                    if (location != null) {
                        try {
                            Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                            List<Address> addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            address = ""+ addressList.get(0).getAddressLine(0);
                            latitude = Double.toString(addressList.get(0).getLatitude());
                            longitude = Double.toString(addressList.get(0).getLongitude());

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        ContentValues values = new ContentValues();
                        values.put("name", byteArray);
                        values.put("address", address);
                        values.put("latitude", latitude);
                        values.put("longitude", longitude);
                        long result = database.insert("images", null, values);
                        if (result == -1) {
                            Toast.makeText(getApplicationContext(), "Error while insertion", Toast.LENGTH_LONG).show();
                        } else {
                            Image imageFromDatabase = getRecentlyAddedImage();
                            byte[] image=imageFromDatabase.getImage();
                            imageView.setImageBitmap(BitmapFactory.decodeByteArray(image,0, image.length) );
                            locationText.setText(String.format("Location: Address: %s Latitude: %s Longitude: %s", imageFromDatabase.getAddress(), imageFromDatabase.getLatitude(), imageFromDatabase.getLongitude()));
                            Toast.makeText(getApplicationContext(), "Image Saved", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "Loc", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    public Image getRecentlyAddedImage() {
        Cursor cursor = database.rawQuery(" SELECT * FROM images", null, null);
        if (cursor != null && cursor.moveToLast()) {
            byte[] image = cursor.getBlob(cursor.getColumnIndex("name"));
            String address = cursor.getString(cursor.getColumnIndex("address"));
            String latitude = cursor.getString(cursor.getColumnIndex("latitude"));
            String longitude = cursor.getString(cursor.getColumnIndex("longitude"));
            cursor.close();
            return new Image(image, address, latitude, longitude);
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return null;
    }
    public void showImagesScreen(View view) {
        Intent intent = new Intent(MainActivity.this, ShowImagesActivity.class);
        startActivity(intent);
    }
}