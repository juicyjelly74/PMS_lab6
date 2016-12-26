package com.example.brightdays.myshowsme;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class NextEpisodesListAdapter extends ArrayAdapter {

    private List<Episode> episodesList;
    private LayoutInflater layoutInflater;


    public NextEpisodesListAdapter(Activity content, ArrayList<Episode> arrayList)
    {
        super(content, R.layout.next_episodes_item, arrayList);
        this.layoutInflater = content.getLayoutInflater();
        this.episodesList = arrayList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        Episode e = episodesList.get(position);
        if (e.getAirDate() != null) {
            view = layoutInflater.inflate(R.layout.next_episodes_item, parent, false);
            TextView episodeTitle = (TextView) view.findViewById(R.id.next_episode_title);
            TextView episodeDate = (TextView) view.findViewById(R.id.next_episode_date);
            TextView showTitle = (TextView) view.findViewById(R.id.next_show_title);
            String seriesTitle = null;
            try
            {
                DataManager dataManager = DataManager.getInstance();
                seriesTitle = dataManager.getMyMap().get(e.getShowId()).getTitle();
            }
            catch (Exception ex)
            {

            }
            showTitle.setText(seriesTitle != null ? seriesTitle : "undefined");
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
