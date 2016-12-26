package com.example.brightdays.myshowsme;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.spec.ECPoint;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;


public class FirstFragment extends android.support.v4.app.Fragment {
    // Store instance variables
    private String title;
    private int page;
    private DataManager dataManager;
    private DownloadEpisodesUnwatched downloadEpisodesUnwatched;
    private View mProgressView;
    private ArrayAdapter mAdapter;

    /**
     * The fragment's ListView/GridView.
     */
    private ListView mListView;

    // newInstance constructor for creating fragment with arguments
    public static FirstFragment newInstance(int page, String title) {
        FirstFragment fragmentFirst = new FirstFragment();
        Bundle args = new Bundle();
        args.putInt("someInt", page);
        args.putString("someTitle", title);
        fragmentFirst.setArguments(args);
        return fragmentFirst;
    }

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataManager = DataManager.getInstance();
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }



    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_first, container, false);
        mListView = (ListView) view.findViewById(R.id.episodeUnwatchedListView);
        Log.d("OnCreateView", "Yes");
        if (dataManager.getEpisodeListUnwatched() == null) {
            mProgressView = view.findViewById(R.id.episode_unwatched_progress);
            showProgress(true);
            downloadEpisodesUnwatched = new DownloadEpisodesUnwatched();
            downloadEpisodesUnwatched.execute((Void) null);
        }
        else
        {
            mAdapter = new EpisodesListAdapter(getActivity(), (ArrayList) dataManager.getEpisodeListUnwatched());
            ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);
        }
        //TextView tvLabel = (TextView) view.findViewById(R.id.tvLabel);
        //tvLabel.setText(page + " -- " + title);
        return view;
    }

    public class DownloadEpisodesUnwatched extends AsyncTask<Void, Void, Boolean> {

        private final String profileUrl = "http://api.myshows.ru/profile/episodes/unwatched/";

        private List<Episode> addSections(List<Episode> episodes)
        {
            List<Episode> newList = new ArrayList<>();
            Episode e = new Episode();
            String title = null;
            try {
                title = dataManager.getMyMap().get(episodes.get(0).getShowId()).getTitle();
            }
            catch (Exception ex)
            {

            }
            e.setTitle(title != null ? title : "undefined");
            newList.add(e);
            for (int i = 0; i < episodes.size() - 1; i++)
                if (episodes.get(i).getShowId() != episodes.get(i + 1).getShowId())
                {
                    newList.add(episodes.get(i));
                    Episode temp = new Episode();
                    title = null;
                    try {
                        title = dataManager.getMyMap().get(episodes.get(i + 1).getShowId()).getTitle();
                    }
                    catch (Exception ex)
                    {

                    }
                    temp.setTitle(title != null ? title : "undefined");
                    newList.add(temp);
                }
                else
                    newList.add(episodes.get(i));
            newList.add(episodes.get(episodes.size() - 1));
            return newList;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            try {
                URL url = new URL(profileUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                    String str;
                    StringBuilder sb = new StringBuilder();
                    while ((str = bufferedReader.readLine()) != null)
                        sb.append(str);
                    Log.d("Response", sb.toString());
                    Gson gson = new GsonBuilder().create();
                    Type type = new TypeToken<Map<Integer, Episode>>(){}.getType();
                    Map<Integer, Episode> myMap = gson.fromJson(sb.toString(), type);
                    List<Episode> episodeList = new ArrayList<>();
                    for (Map.Entry<Integer, Episode> entry : myMap.entrySet()) {
                        Episode s = entry.getValue();
                        episodeList.add(s);
                    }
                    Collections.sort(episodeList, new Comparator<Episode>() {
                        @Override
                        public int compare(Episode lhs, Episode rhs) {
                            return Integer.valueOf(lhs.getShowId()).compareTo(rhs.getShowId());
                        }
                    });
                    dataManager.setEpisodeListUnwatched(addSections(episodeList));
                    return true;
                } catch (Exception e) {
                    return false;
                } finally {
                    urlConnection.disconnect();
                }
            }
            catch (Exception e)
            {
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            downloadEpisodesUnwatched = null;
            showProgress(false);
            if (success) {
                if (dataManager.getEpisodeListUnwatched() != null) {
                    mAdapter = new EpisodesListAdapter(getActivity(), (ArrayList) dataManager.getEpisodeListUnwatched());
                    ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);
                }
            } else {

            }
        }

        @Override
        protected void onCancelled() {
            downloadEpisodesUnwatched = null;
            showProgress(false);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d("FirstFragment", "Detach");
    }
}