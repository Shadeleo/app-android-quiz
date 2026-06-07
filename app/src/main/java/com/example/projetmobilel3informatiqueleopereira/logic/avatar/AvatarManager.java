package com.example.projetmobilel3informatiqueleopereira.logic.avatar;

import android.content.Context;
import androidx.core.content.ContextCompat;
import com.example.projetmobilel3informatiqueleopereira.R;
import com.example.projetmobilel3informatiqueleopereira.logic.auth.AuthManager;
import com.example.projetmobilel3informatiqueleopereira.logic.profile.ProfileManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestionnaire du système de personnalisation des avatars.
 *
 * Cette classe détermine les éléments visuels disponibles
 * pour l'avatar du joueur (formes et couleurs) en fonction
 * de sa progression dans le jeu. Elle permet également
 * de sauvegarder et récupérer la configuration actuelle
 * de l'avatar.
 */
public class AvatarManager {
    private final Context context;
    private final AuthManager authManager;
    private final ProfileManager profileManager;

    /**
     * Initialise le gestionnaire d'avatar et les dépendances nécessaires.
     *
     * @param context contexte de l'application utilisé pour accéder
     * aux ressources et aux gestionnaires de profil et d'authentification.
     */
    public AvatarManager(Context context) {
        this.context = context.getApplicationContext();
        this.authManager = new AuthManager(context);
        this.profileManager = new ProfileManager(context);
    }

    /**
     * Récupère les formes d'avatar débloquées selon le niveau du joueur.
     *
     * Certaines formes deviennent accessibles à mesure que le joueur
     * progresse dans le mode Story.
     *
     * @return liste des ressources drawable représentant les formes disponibles
     */
    public List<Integer> getUnlockedShapes() {
        int level = authManager.getLevelReached();

        List<Integer> shapes = new ArrayList<>();
        shapes.add(R.drawable.shape_square);

        if (level >= 2) shapes.add(R.drawable.shape_circle);
        if (level >= 3) shapes.add(R.drawable.shape_triangle);
        return shapes;
    }

    /**
     * Récupère les couleurs d'avatar disponibles selon la progression.
     *
     * Les couleurs supplémentaires sont débloquées à partir d'un
     * certain niveau afin d'encourager la progression du joueur.
     *
     * @return liste des couleurs disponibles pour l'avatar
     */
    public List<Integer> getUnlockedColors() {
        int level = authManager.getLevelReached();
        List<Integer> colors = new ArrayList<>();

        colors.add(ContextCompat.getColor(context, R.color.avatar_blue));

        if (level >= 4) {
            colors.add(ContextCompat.getColor(context, R.color.avatar_red));
            colors.add(ContextCompat.getColor(context, R.color.avatar_green));
            colors.add(ContextCompat.getColor(context, R.color.avatar_yellow));
            colors.add(ContextCompat.getColor(context, R.color.avatar_purple));
        }
        return colors;
    }

    /**
     * Sauvegarde la configuration actuelle de l'avatar.
     *
     * La persistance est déléguée au ProfileManager afin
     * de centraliser la gestion des préférences utilisateur.
     *
     * @param shape ressource drawable de la forme sélectionnée
     * @param color couleur sélectionnée pour l'avatar
     */
    public void saveAvatar(int shape, int color) {
        profileManager.saveAvatarSettings(shape, color);
    }

    /**
     * Récupère la forme actuellement enregistrée pour l'avatar.
     *
     * @return ressource drawable correspondant à la forme sauvegardée
     */
    public int getCurrentShape() {
        return profileManager.getSavedShape();
    }

    /**
     * Récupère la couleur actuellement enregistrée pour l'avatar.
     *
     * @return valeur de couleur utilisée pour l'avatar
     */
    public int getCurrentColor() {
        return profileManager.getSavedColor();
    }
}