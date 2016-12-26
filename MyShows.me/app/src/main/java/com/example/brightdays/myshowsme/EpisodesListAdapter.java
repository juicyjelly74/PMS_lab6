package com.example.brightdays.myshowsme;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class EpisodesListAdapter extends ArrayAdapter {

    private List<Episode> episodesList;
    private LayoutInflater layoutInflater;


    public EpisodesListAdapter(Activity content, ArrayList<Episode> arrayList)
    {
        super(content, R.layout.episodes_item, arrayList);
        this.layoutInflater = content.getLayoutInflater();
        this.episodesList = arrayList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        Episode e = episodesList.get(position);
        if (e.getAirDate() != null) {
            view = layoutInflater.inflate(R.layout.episodes_item, parent, false);
            TextView episodeTitle = (TextView) view.findViewById(R.id.episode_title);
            TextView episodeDate = (TextView) view.findViewById(R.id.episode_date);
            episodeTitle.setText(e.getTitle());
            episodeDate.setText(e.getAirDate());
        } else {
            view = layoutInflater.inflate(R.layout.list_item_section, parent, false);
            TextView text = (TextView) view.findViewById(R.id.section_id);
            text.setText(e.getTitle());
        }
        return view;
    }

}
