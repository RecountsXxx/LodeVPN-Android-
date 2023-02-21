package com.lazycoder.cakevpn.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lazycoder.cakevpn.R;

import java.util.List;

public class ListItemAdapter extends BaseAdapter {
Context context;
String countryies[];
int images[];
LayoutInflater inflater;
    public ListItemAdapter(Context context,String[] coutrues,int[] imagesCountry){
this.context = context;
this.countryies = coutrues;
this.images = imagesCountry;
inflater = LayoutInflater.from(context);
    }
    @Override
    public int getCount() {
        return countryies.length;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflater.inflate(R.layout.list_item_main,null);
        TextView textView = (TextView)  view.findViewById(R.id.countryLocationList);
        ImageView imageView = (ImageView) view.findViewById(R.id.imageLocationList);
        textView.setText(countryies[i]);
        imageView.setImageResource(images[i]);
        return view;
    }
}
