package com.example.projetmobilel3informatiqueleopereira.logic.auth;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Gestionnaire responsable de l'authentification locale et de la session utilisateur.
 *
 * Cette classe centralise l'accès aux préférences partagées (SharedPreferences)
 * afin de gérer l'état de connexion, l'acceptation des conditions d'utilisation
 * ainsi que la progression du joueur dans le mode Story.
 */
public class AuthManager {
    private static final String PREF_NAME = "USER_SESSION";
    private static final String KEY_USER = "current_user";
    private static final String KEY_TERMS = "terms_accepted";
    private static final String KEY_TUTO_DONE = "tuto_completed";
    private static final String KEY_LEVEL = "STORY_LEVEL_REACHED";
    private final SharedPreferences prefs;

    /**
     * Initialise le gestionnaire d'authentification.
     *
     * Récupère l'instance de SharedPreferences utilisée pour stocker
     * les informations de session et de progression.
     *
     * @param context contexte de l'application
     */
    public AuthManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Vérifie si un utilisateur est actuellement connecté.
     *
     * @return true si une session utilisateur est active, sinon false
     */
    public boolean isUserLoggedIn() {
        return getCurrentUser() != null;
    }

    /**
     * Récupère le pseudo de l'utilisateur actuellement connecté.
     *
     * @return pseudo de l'utilisateur ou null si aucune session active
     */
    public String getCurrentUser() {
        return prefs.getString(KEY_USER, null);
    }

    /**
     * Enregistre la session utilisateur dans les préférences.
     *
     * @param pseudo pseudo de l'utilisateur connecté
     */
    public void saveSession(String pseudo) {
        prefs.edit().putString(KEY_USER, pseudo).apply();
    }

    /**
     * Vérifie si l'utilisateur a accepté les conditions d'utilisation.
     *
     * @return true si les CGU ont été acceptées
     */
    public boolean areTermsAccepted() {
        return prefs.getBoolean(KEY_TERMS, false);
    }

    /**
     * Enregistre l'acceptation des conditions d'utilisation.
     */
    public void acceptTerms() {
        prefs.edit().putBoolean(KEY_TERMS, true).apply();
    }

    /**
     * Indique si le tutoriel de l'application a été complété.
     *
     * @return true si le tutoriel est terminé
     */
    public boolean isTutoCompleted() {
        return prefs.getBoolean(KEY_TUTO_DONE, false);
    }

    /**
     * Met à jour l'état de complétion du tutoriel.
     *
     * @param status true si le tutoriel est terminé
     */
    public void setTutoCompleted(boolean status) {
        prefs.edit().putBoolean(KEY_TUTO_DONE, status).apply();
    }

    /**
     * Met à jour le niveau maximum atteint par le joueur.
     *
     * Le niveau est enregistré uniquement s'il est supérieur
     * au niveau actuellement stocké.
     *
     * @param level niveau atteint par le joueur
     */
    public void setLevelReached(int level) {
        int current = getLevelReached();
        if (level > current) {
            prefs.edit().putInt(KEY_LEVEL, level).apply();
        }
    }

    /**
     * Récupère le niveau maximum atteint dans le mode Story.
     *
     * @return niveau enregistré (1 par défaut)
     */
    public int getLevelReached() {
        return prefs.getInt(KEY_LEVEL, 1);
    }
}