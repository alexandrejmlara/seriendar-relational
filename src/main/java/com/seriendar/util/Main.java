package com.seriendar.util;

import com.seriendar.dao.TVShow;
import com.seriendar.dao.TVShowEpisode;
import com.seriendar.dao.TVShowEpisodesList;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Alexandre Lara on 18/07/2016.
 */
public class Main {

    public static void main( String[] args ){


//        DayOfWeek dayOfWeek = new DayOfWeek(18,7,2016);
//        List<TVShowEpisode> tvShowEpisodeList = dayOfWeek.getEpisodesOfTheDay();
//        for(TVShowEpisode tvShowEpisode : tvShowEpisodeList){
//            System.out.println(tvShowEpisode.getSeriesName());
//        }
//        System.out.println();
        TVDBApiData tvdb = new TVDBApiData();
        try {
            tvdb.getToken();
            TVShow tv = tvdb.getTVShow(266091L);
            TVShowEpisodesList tvShowEpisodesList = tvdb.getTVShowEpisodesList(266091L);
            MongoDBUtil mongoDBUtil = new MongoDBUtil();
            mongoDBUtil.saveTVShow(tv, tvShowEpisodesList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
