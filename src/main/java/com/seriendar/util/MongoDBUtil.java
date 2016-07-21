package com.seriendar.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.seriendar.dao.TVShow;
import com.seriendar.dao.TVShowEpisode;
import com.seriendar.dao.TVShowEpisodesList;
import org.bson.Document;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

/**
 * Gerencia as operações no banco de dados MongoDB.
 *
 * Instancia a conexão do cliente com o banco de dados, abre a conexão
 * com o banco de dados MongoDB.
 *
 * Também atua na serialização e desserialização de objetos utilizando Gson.
 *
 */
public class MongoDBUtil {

    EntityManagerFactory factory;

    EntityManager manager;
    /**
     * Cliente do MongoDB que mantém a conexão com o servidor
     */
    private MongoClient mongoClient = null;

    /**
     * Base de dados do MongoDB
     */
    private MongoDatabase db = null;

    /**
     * Objeto Gson para serialização e desserialização de objetos Java
     */
    private Gson gson = null;

    /**
     * Construtor que inicializa o banco de dados, a conexão do cliente
     * e o objeto Gson.
     *
     */
    public MongoDBUtil() {
        factory = Persistence.createEntityManagerFactory("tvshows");
        manager = factory.createEntityManager();
        mongoClient = new MongoClient();
        db = mongoClient.getDatabase(Constants.DB_NAME);
        gson = new GsonBuilder().create();

    }

    /**
     * Recupera uma série com o identificador único da mesma
     *
     * @param id Identificador único da série
     * @return A série que contém o identificador passado como parâmetro
     */
    public TVShow getTVSHow(Integer id) {
        TVShow tvShow = manager.find(TVShow.class, id);

        return tvShow;
    }

    /**
     * Recupera uma lista de episódios de uma série em específico.
     *
     * @param id Identificador da série
     * @return Lista de episódios de uma série
     */
    public List<TVShowEpisode> getTVShowEpisodesList(Integer id) {

        Query query = manager
                .createQuery("select t from TVShowEpisode as t "+
                        "where t.tvShowId = :paramId");
        query.setParameter("paramId", id);

        List<TVShowEpisode> tvShowEpisodeList = query.getResultList();

        return tvShowEpisodeList;

    }

    /**
     * Salva a série no banco de dados MongoDB.
     *
     * @param tvShow Série a ser salva no banco de dados MongoDB
     * @param tvShowEpisodesList Lista de episódios da série a ser salva no banco de dados MongoDB
     */
    public void saveTVShow(TVShow tvShow, TVShowEpisodesList tvShowEpisodesList) {

        manager.getTransaction().begin();
        manager.persist(tvShow);
        manager.getTransaction().commit();

        saveTVShowEpisodes(tvShow.getId(), tvShowEpisodesList);
    }

    /**
     * Recupera a lista de episódios em um dia específico
     *
     * @param firstAired Data de referência
     * @return Lista de episódios de um dia específico
     */
    public List<TVShowEpisode> getEpisodesOfTheDay(String firstAired) {
        MongoCollection episodesCollection = db.getCollection(Constants.DB_COLLECTION_EPISODES);
        MongoCursor<Document> mCursorEpisodes = episodesCollection.find(eq("firstAired", firstAired)).iterator();
        List<TVShowEpisode> listEpisodes = new ArrayList<TVShowEpisode>();
        while (mCursorEpisodes.hasNext()) {
            Document episode = mCursorEpisodes.next();

            MongoCollection tvShowsCollection = db.getCollection(Constants.DB_COLLECTION_TVSHOWS);
            MongoCursor<Document> mCursorTvShows = tvShowsCollection.find(eq("_id", episode.get("tvShowId"))).iterator();

            if (mCursorTvShows.hasNext()) {
                Document docTvShow = mCursorTvShows.next();
                episode.put("seriesName", docTvShow.get("seriesName"));
            }
            TVShowEpisode tvShowEpisode = gson.fromJson(episode.toJson(), TVShowEpisode.class);
            listEpisodes.add(tvShowEpisode);
        }

        return listEpisodes;

    }

    /**
     * Salva os episódios da série no banco de dados MongoDB.
     *
     * @param tvShowId Identificador único da série
     * @param tvShowEpisodesList Lista de episódios a serem salvos no banco de dados MongoDB
     */
    private void saveTVShowEpisodes(Integer tvShowId, TVShowEpisodesList tvShowEpisodesList) {

        for (TVShowEpisode episode : tvShowEpisodesList.getData()) {

            episode.setTvShowId(tvShowId);
            manager.getTransaction().begin();
            manager.persist(episode);
            manager.getTransaction().commit();

        }
    }

}
