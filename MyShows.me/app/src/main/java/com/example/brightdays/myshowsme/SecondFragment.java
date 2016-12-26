package com.example.brightdays.myshowsme;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class SecondFragment extends android.support.v4.app.Fragment {

    // Store instance variables
    private String title;
    private int page;
    private DataManager dataManager;
    private DownloadEpisodesNext downloadEpisodesNext;
    private View mProgressView;
    private ArrayAdapter mAdapter;

    /**
     * The fragment's ListView/GridView.
     */
    private ListView mListView;

    // newInstance constructor for creating fragment with arguments
    public static SecondFragment newInstance(int page, String title) {
        SecondFragment fragmentSecond = new SecondFragment();
        Bundle args = new Bundle();
        args.putInt("someInt", page);
        args.putString("someTitle", title);
        fragmentSecond.setArguments(args);
        return fragmentSecond;
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
        View view = inflater.inflate(R.layout.fragment_second, container, false);
        mListView = (ListView) view.findViewById(R.id.episodeNextListView);
        Log.d("OnCreateView", "Yes");
        if (dataManager.getEpisodeListNext() == null) {
            mProgressView = view.findViewById(R.id.episode_next_progress);
            showProgress(true);
            downloadEpisodesNext = new DownloadEpisodesNext();
            downloadEpisodesNext.execute((Void) null);
        }
        else
        {
            mAdapter = new NextEpisodesListAdapter(getActivity(), (ArrayList) dataManager.getEpisodeListUnwatched());
            ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);
        }
        //TextView tvLabel = (TextView) view.findViewById(R.id.tvLabel);
        //tvLabel.setText(page + " -- " + title);
        return view;
    }

    public class DownloadEpisodesNext extends AsyncTask<Void, Void, Boolean> {

        private final String profileUrl = "http://api.myshows.ru/profile/episodes/next/";

        private List<Episode> addSections(List<Episode> episodes)
        {
            List<Episode> newList = new ArrayList<>();
            Episode e = new Episode();
            e.setTitle(episodes.get(0).getFormattedDate());
            newList.add(e);
            for (int i = 0; i < episodes.size() - 1; i++)
                if (episodes.get(i).getFormattedDate().compareTo(episodes.get(i + 1).getFormattedDate()) != 0)
                {
                    newList.add(episodes.get(i));
                    Episode temp = new Episode();
                    temp.setTitle(episodes.get(i + 1).getFormattedDate());
                    newList.add(temp);
                }
                else
                    newList.add(episodes.get(i));
            newList.add(episodes.get(episodes.size() - 1));
            return newList;
        }

        private String formatDate(Episode e)
        {
            try {
                String[] splitted = e.getAirDate().split("\\.");
                String monthString = new DateFormatSymbols().getMonths()[Integer.valueOf(splitted[1])];
                e.setMonth(Integer.valueOf(splitted[1]));
                e.setYear(Integer.valueOf(splitted[2]));
                return monthString + " " + splitted[2];
            }
            catch (Exception ex)
            {
                return "undefined";
            }
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
                        s.setFormattedDate(formatDate(s));
                        episodeList.add(s);
                    }
                    Collections.sort(episodeList, new Comparator<Episode>() {
                        @Override
                        public int compare(Episode lhs, Episode rhs) {
                            if (lhs.getYear() == rhs.getYear())
                                return Integer.valueOf(lhs.getMonth()).compareTo(rhs.getMonth());
                            return Integer.valueOf(lhs.getYear()).compareTo(rhs.getYear());
                        }
                    });
                    dataManager.setEpisodeListNext(addSections(episodeList));
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
            downloadEpisodesNext = null;
            showProgress(false);
            if (success) {
                if (dataManager.getEpisodeListUnwatched() != null) {
                    mAdapter = new NextEpisodesListAdapter(getActivity(), (ArrayList) dataManager.getEpisodeListNext());
                    ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);
                }
            } else {

            }
        }

        @Override
        protected void onCancelled() {
            downloadEpisodesNext = null;
            showProgress(false);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d("FirstFragment", "Detach");
    }

}
