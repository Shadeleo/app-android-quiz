package com.example.projetmobilel3informatiqueleopereira.logic.profile;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.projetmobilel3informatiqueleopereira.R;

/**
 * Gestionnaire de profil utilisateur.
 *
 * Cette classe centralise la gestion des données persistantes
 * liées au profil du joueur : statistiques globales et
 * personnalisation de l'avatar. Les informations sont stockées
 * via SharedPreferences afin d'être conservées entre les sessions.
 */
public class ProfileManager {
    private static final String PREF_NAME = "USER_SESSION";
    private static final String KEY_SUCCESS_RATE = "USER_SUCCESS_RATE";
    private static final String KEY_TOTAL_QUIZ = "NB_QUIZ_TOTAL";
    private static final String KEY_SHAPE = "USER_SHAPE";
    private static final String KEY_COLOR = "USER_COLOR";
    private final SharedPreferences prefs;

    /**
     * Initialise l'accès aux préférences partagées du profil.
     *
     * @param context contexte de l'application utilisé pour accéder
     * aux SharedPreferences.
     */
    public ProfileManager(Context context) {
        this.prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Met à jour les statistiques globales du joueur.
     *
     * La moyenne de réussite est recalculée en utilisant une
     * moyenne glissante basée sur le nombre total de quiz
     * déjà joués.
     *
     * @param lastScorePercent score obtenu lors du dernier quiz (en %)
     */
    public void updateGlobalStats(int lastScorePercent) {
        int oldAvg = prefs.getInt(KEY_SUCCESS_RATE, 0);
        int total = prefs.getInt(KEY_TOTAL_QUIZ, 0);

        int newAvg = ((oldAvg * total) + lastScorePercent) / (total + 1);

        prefs.edit()
                .putInt(KEY_SUCCESS_RATE, newAvg)
                .putInt(KEY_TOTAL_QUIZ, total + 1)
                .apply();
    }

    /**
     * Récupère le taux de réussite moyen du joueur.
     *
     * @return pourcentage moyen de réussite enregistré
     */
    public int getSuccessRate() {
        return prefs.getInt(KEY_SUCCESS_RATE, 0);
    }

    /**
     * Retourne la forme d'avatar actuellement enregistrée.
     *
     * @return ressource drawable correspondant à la forme de l'avatar
     */
    public int getSavedShape() {
        return prefs.getInt(KEY_SHAPE, R.drawable.shape_square);
    }

    /**
     * Retourne la couleur actuellement enregistrée pour l'avatar.
     *
     * Une couleur bleue est utilisée par défaut si aucune
     * configuration n'est encore sauvegardée.
     *
     * @return valeur entière représentant la couleur de l'avatar
     */
    public int getSavedColor() {
        return prefs.getInt(KEY_COLOR, 0xFF0000FF);
    }

    /**
     * Sauvegarde la configuration actuelle de l'avatar.
     *
     * Enregistre la forme et la couleur sélectionnées
     * dans les préférences utilisateur.
     *
     * @param shapeRes ressource drawable de la forme choisie
     * @param colorInt valeur entière de la couleur sélectionnée
     */
    public void saveAvatarSettings(int shapeRes, int colorInt) {
        prefs.edit()
                .putInt(KEY_SHAPE, shapeRes)
                .putInt(KEY_COLOR, colorInt)
                .apply();
    }
}