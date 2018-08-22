package com.zgr.runnzzer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomListAdapter extends ArrayAdapter<String> {

    CustomListAdapter(Context context, ArrayList<String> list) {
        super(context,R.layout.custom_row , list);
    }

    @NonNull
    @Override
    public View getView (int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //Get the view
        @SuppressLint("ViewHolder")
        View view = LayoutInflater.from(getContext()).inflate(R.layout.custom_row , parent , false);
        String text = getItem(position);
        TextView textView =  view.findViewById(R.id.title_text);
        ImageView imageView =  view.findViewById(R.id.imageView);
        //set Text
        textView.setText(text);
        //set The Image
        imageView.setImageResource(R.mipmap.running_icon);
        return view;
    }
}
