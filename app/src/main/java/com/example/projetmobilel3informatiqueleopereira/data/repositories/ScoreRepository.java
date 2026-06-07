package com.example.projetmobilel3informatiqueleopereira.data.repositories;

import android.content.Context;
import com.example.projetmobilel3informatiqueleopereira.data.local.AppDatabase;
import com.example.projetmobilel3informatiqueleopereira.data.models.Score;
import java.util.List;

/**
 * Repository chargé de la gestion et de l'accès aux scores.
 *
 * Cette classe sert d'intermédiaire entre l'application et la
 * base de données Room pour toutes les opérations liées aux
 * scores des joueurs (récupération des résultats, filtrage par
 * thème, etc.).
 */
public class ScoreRepository {
    private final AppDatabase db;

    /**
     * Initialise le repository et récupère l'instance de la base de données.
     *
     * @param context contexte de l'application utilisé pour obtenir
     * l'instance singleton de AppDatabase.
     */
    public ScoreRepository(Context context) {
        this.db = AppDatabase.getInstance(context.getApplicationContext());
    }

    /**
     * Récupère la liste des thèmes pour lesquels l'utilisateur
     * possède des questions enregistrées.
     *
     * @param user pseudo de l'utilisateur
     * @return liste des thèmes associés à l'utilisateur
     */
    public List<String> getThemes(String user) {
        return db.questionDao().getAllThemesByUser(user);
    }

    /**
     * Récupère l'ensemble des scores enregistrés pour un utilisateur.
     *
     * @param user pseudo du joueur
     * @return liste des scores associés à cet utilisateur
     */
    public List<Score> getAllScores(String user) {
        return db.scoreDao().getScoresByUser(user);
    }

    /**
     * Récupère les scores d'un utilisateur pour un thème spécifique.
     *
     * @param user pseudo du joueur
     * @param theme thème pour lequel récupérer les scores
     * @return liste des scores correspondant au thème
     */
    public List<Score> getScoresByTheme(String user, String theme) {
        return db.scoreDao().getScoresByThemeAndUser(user, theme);
    }
}