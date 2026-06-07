package com.example.projetmobilel3informatiqueleopereira.ui.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.example.projetmobilel3informatiqueleopereira.data.repositories.QuestionRepository;
import com.example.projetmobilel3informatiqueleopereira.logic.auth.AuthManager;
import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel responsable de la gestion du mode Histoire.
 *
 * Cette classe prépare la liste des niveaux disponibles,
 * gère la progression du joueur et déclenche le lancement
 * d'un niveau lorsque celui-ci est sélectionné.
 *
 * Elle communique avec :
 * - AuthManager pour récupérer la progression du joueur
 * - QuestionRepository pour s'assurer que les questions
 *   nécessaires au niveau sont présentes en base de données.
 */
public class StoryViewModel extends AndroidViewModel {
    private final QuestionRepository repository;
    private final AuthManager authManager;
    public MutableLiveData<List<String>> levels = new MutableLiveData<>();
    public MutableLiveData<Integer> currentProgress = new MutableLiveData<>();
    public MutableLiveData<String> startLevelEvent = new MutableLiveData<>();

    /**
     * Initialise le ViewModel et les dépendances nécessaires
     * au fonctionnement du mode Histoire.
     *
     * @param application instance de l'application permettant
     * d'accéder aux repositories et aux gestionnaires métier.
     */
    public StoryViewModel(@NonNull Application application) {
        super(application);
        this.repository = new QuestionRepository(application);
        this.authManager = new AuthManager(application);
    }

    /**
     * Charge les niveaux disponibles et met à jour la progression du joueur.
     *
     * Cette méthode construit dynamiquement la liste des niveaux
     * affichés dans l'interface et récupère le niveau actuel atteint
     * par l'utilisateur via l'AuthManager.
     *
     * @param l1 nom du niveau 1
     * @param l2 nom du niveau 2
     * @param l3 nom du niveau 3
     * @param l4 nom du niveau 4
     * @param lSoon texte utilisé pour les niveaux futurs
     */
    public void loadProgress(String l1, String l2, String l3, String l4, String lSoon) {
        currentProgress.setValue(authManager.getLevelReached());
        List<String> list = new ArrayList<>();
        list.add("1. " + l1);
        list.add("2. " + l2);
        list.add("3. " + l3);
        list.add("4. " + l4);
        for (int i = 5; i <= 10; i++) list.add(i + ". " + lSoon);
        levels.setValue(list);
    }

    /**
     * Gère la sélection d'un niveau par l'utilisateur.
     *
     * Si le niveau sélectionné correspond au niveau actuel
     * de progression du joueur, les questions associées
     * sont vérifiées ou injectées en base de données
     * puis l'événement de lancement du niveau est déclenché.
     *
     * @param position position du niveau sélectionné dans la liste
     */
    public void onLevelClicked(int position) {
        int levelToPlay = position + 1;
        int progress = authManager.getLevelReached();

        if (levelToPlay == progress) {
            String theme = getThemeForLevel(levelToPlay);
            new Thread(() -> {
                repository.checkAndInjectSystemQuestions(theme);
                startLevelEvent.postValue(theme);
            }).start();
        }
    }

    /**
     * Associe un niveau du mode Histoire à un thème de quiz.
     *
     * Chaque niveau correspond à une catégorie spécifique
     * utilisée pour charger les questions du jeu.
     *
     * @param level numéro du niveau
     * @return nom du thème associé au niveau
     */
    public String getThemeForLevel(int level) {
        switch (level) {
            case 2: return "COULEURS";
            case 3: return "CHIFFRES";
            case 4: return "FRANCAIS";
            default: return "TUTO";
        }
    }

    /**
     * Réinitialise l'événement de lancement d'un niveau.
     *
     * Cette méthode est utilisée après la navigation vers
     * l'activité de jeu afin d'éviter que l'événement soit
     * déclenché plusieurs fois lors des changements de configuration.
     */
    public void consumeStartLevelEvent() {

        startLevelEvent.setValue(null);
    }

    /**
     * Retourne la progression actuelle du joueur dans le mode Histoire.
     *
     * @return numéro du niveau actuellement débloqué
     */
    public int getProgressValue() { return authManager.getLevelReached(); }
}