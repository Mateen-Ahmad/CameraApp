package com.example.mateen.cameraapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class ImageAdapter extends BaseAdapter {
    private final int layout;
    private final ArrayList<Image> data;
    private final Context context;

    //    parameterized constructor
    public ImageAdapter(Context context, int layout, ArrayList<Image> data) {
        this.context = context;
        this.data = data;
        this.layout = layout;
    }

    public int getCount() {
        return data.size();
    }

    public Object getItem(int position) {

        return data.get(position);
    }

    public long getItemId(int position) {

        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(context, layout, null);
        }

        TextView textView = convertView.findViewById(R.id.TextView1);
        ImageView imageView = convertView.findViewById(R.id.ImageView1);

        Image datalist = data.get(position);
        textView.setText(String.format("%s %s %s", datalist.getAddress(), datalist.getLatitude(), datalist.getLongitude()));

        byte[] recordImage = datalist.getImage();
        Bitmap bitmap = BitmapFactory.decodeByteArray(recordImage, 0, recordImage.length);
        imageView.setImageBitmap(bitmap);

        return convertView;
    }

}
