package com.example.brightdays.myshowsme;

import java.util.List;
import java.util.Map;


public class DataManager {
    private static DataManager ourInstance = new DataManager();
    private List<Series> seriesList;
    private Map<Integer, Series> myMap;
    private List<Episode> episodeListUnwatched;
    private List<Episode> episodeListNext;

    public List<Episode> getEpisodeListNext() {
        return episodeListNext;
    }

    public void setEpisodeListNext(List<Episode> episodeListNext) {
        this.episodeListNext = episodeListNext;
    }

    public List<Episode> getEpisodeListUnwatched() {
        return episodeListUnwatched;
    }

    public void setEpisodeListUnwatched(List<Episode> episodeListUnwatched) {
        this.episodeListUnwatched = episodeListUnwatched;
    }

    public Map<Integer, Series> getMyMap() {
        return myMap;
    }

    public void setMyMap(Map<Integer, Series> myMap) {
        this.myMap = myMap;
    }

    public List<Series> getSeriesList() {
        return seriesList;
    }

    public void setSeriesList(List<Series> seriesList) {
        this.seriesList = seriesList;
    }



    public static DataManager getInstance() {
        return ourInstance;
    }

    private DataManager() {
    }
}
