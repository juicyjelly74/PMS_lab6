package com.example.brightdays.myshowsme;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class ProfileListAdapter extends ArrayAdapter {

    private List<Series> seriesList;
    private LayoutInflater layoutInflater;
    private TreeSet<Integer> sectionHeader = new TreeSet<Integer>();
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_SEPARATOR = 1;



    public ProfileListAdapter(Activity context, ArrayList<Series> series)
    {
        super(context, R.layout.item, series);
        this.layoutInflater = context.getLayoutInflater();
        this.seriesList = series;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        Series s = seriesList.get(position);
        if (s.getShowStatus() != null) {
            view = layoutInflater.inflate(R.layout.item, parent, false);
            TextView text = (TextView) view.findViewById(R.id.title);
            ImageView image = (ImageView) view.findViewById(R.id.image);
            text.setText(s.getTitle());
            image.setImageBitmap(s.getThumbnail());
        } else {
            view = layoutInflater.inflate(R.layout.list_item_section, parent, false);
            TextView text = (TextView) view.findViewById(R.id.section_id);
            text.setText(s.getTitle());
        }
        return view;

    }

}
