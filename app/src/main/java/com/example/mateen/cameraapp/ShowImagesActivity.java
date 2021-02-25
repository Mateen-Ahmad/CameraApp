package com.example.mateen.cameraapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class ShowImagesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_images);

        ListView listView = findViewById(R.id.images_list);
        ArrayList<Image> imagesList = new ArrayList<>();

        // Fetching all images form database
        SQLiteDatabase database = openOrCreateDatabase("db_images", Context.MODE_PRIVATE, null);
        TextView textView = findViewById(R.id.textView);
        textView.setText("All Images");
        String query="Select * from images ";
        Cursor data = database.rawQuery(query, null, null);
        imagesList.clear();
        if (data.getCount() > 0) {
            while (data.moveToNext()) {
                byte[] image = data.getBlob(data.getColumnIndex("name"));
                String address = data.getString(data.getColumnIndex("address"));
                String latitude = data.getString(data.getColumnIndex("latitude"));
                String longitude = data.getString(data.getColumnIndex("longitude"));
                imagesList.add(new Image(image, address, latitude, longitude));
            }
        } else {
//            Toast.makeText(this, "There is no image with these Coordinates", Toast.LENGTH_SHORT).show();
        }
        data.close();

        // Passing List of images to Adapter
        ImageAdapter customAdapter = new ImageAdapter(this, R.layout.image_view, imagesList);
        listView.setAdapter(customAdapter);
        customAdapter.notifyDataSetChanged();
    }
}