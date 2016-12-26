package com.example.brightdays.myshowsme;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class ProfileFragment extends android.support.v4.app.Fragment implements AbsListView.OnItemClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private DownloadSeries mDownloadTask = null;

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private ArrayAdapter mAdapter;
    private View mProgressView;
    private DataManager dataManager;

    // TODO: Rename and change types of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ProfileFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        dataManager = DataManager.getInstance();
        Log.d("onCreate", "Yes");
        // TODO: Change Adapter to display your content
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class DownloadSeries extends AsyncTask<Void, Void, Boolean> {

        private final String profileUrl = "http://api.myshows.ru/profile/shows/";

        DownloadSeries() {
        }

        public Bitmap getBitmapFromURL(String src) {
            try {
                java.net.URL url = new java.net.URL(src);
                HttpURLConnection connection = (HttpURLConnection) url
                        .openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        private List<Series> addSections(List<Series> series)
        {
            List<Series> newList = new ArrayList<>();
            Series s = new Series();
            s.setTitle(series.get(0).getWatchStatus());
            newList.add(s);
            for (int i = 0; i < series.size() - 1; i++)
                if (series.get(i).getWatchStatus().compareTo(series.get(i + 1).getWatchStatus()) != 0)
                {
                    newList.add(series.get(i));
                    Series temp = new Series();
                    temp.setTitle(series.get(i + 1).getWatchStatus());
                    newList.add(temp);
                }
                else
                    newList.add(series.get(i));
            newList.add(series.get(series.size() - 1));
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
                    Type type = new TypeToken<Map<Integer, Series>>(){}.getType();
                    Map<Integer, Series> myMap = gson.fromJson(sb.toString(), type);
                    List<Series> seriesList = new ArrayList<>();
                    for (Map.Entry<Integer, Series> entry : myMap.entrySet()) {
                        Series s = entry.getValue();
                        s.setThumbnail(getBitmapFromURL(s.getImage()));
                        seriesList.add(s);
                    }
                    Collections.sort(seriesList, new Comparator<Series>() {
                        @Override
                        public int compare(Series lhs, Series rhs) {
                            return rhs.getWatchStatus().compareTo(lhs.getWatchStatus());
                        }
                    });
                    dataManager.setSeriesList(addSections(seriesList));
                    dataManager.setMyMap(myMap);
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
            mDownloadTask = null;
            showProgress(false);
            if (success) {
                if (dataManager.getSeriesList() != null) {
                    mAdapter = new ProfileListAdapter(getActivity(), (ArrayList) dataManager.getSeriesList());
                    ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);
                }
            } else {

            }
        }

        @Override
        protected void onCancelled() {
            mDownloadTask = null;
            showProgress(false);
        }
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        mListView.setOnItemClickListener(this);
        if (dataManager.getMyMap() == null) {
            mProgressView = view.findViewById(R.id.profile_progress_bar);
            showProgress(true);
            mDownloadTask = new DownloadSeries();
            mDownloadTask.execute((Void) null);
        }
        else
        {
            mAdapter = new ProfileListAdapter(getActivity(), (ArrayList) dataManager.getSeriesList());
            ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);
        }

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.

            //mListener.onFragmentInteraction(DummyContent.ITEMS.get(position).id);
        }
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
    }

}
