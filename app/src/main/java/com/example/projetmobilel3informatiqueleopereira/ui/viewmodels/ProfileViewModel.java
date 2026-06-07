package com.example.projetmobilel3informatiqueleopereira.ui.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.projetmobilel3informatiqueleopereira.data.models.Score;
import com.example.projetmobilel3informatiqueleopereira.data.repositories.ScoreRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ViewModel responsable de la gestion des statistiques utilisateur.
 *
 * Cette classe récupère les scores enregistrés par l'utilisateur
 * et permet de les filtrer selon différents critères :
 * - thème du quiz
 * - période temporelle
 *
 * Les données filtrées sont ensuite exposées à l'interface via LiveData
 * afin d'alimenter les graphiques et les statistiques affichés.
 */
public class ProfileViewModel extends AndroidViewModel {
    private final ScoreRepository repository;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static final long ONE_DAY_MS = 86_400_000L;
    private static final long SEVEN_DAYS_MS = 604_800_000L;
    private static final long THIRTY_DAYS_MS = 2_592_000_000L;
    private final MutableLiveData<List<String>> _themes = new MutableLiveData<>();
    private final MutableLiveData<List<Score>> _filteredScores = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public final LiveData<List<String>> themes = _themes;
    public final LiveData<List<Score>> filteredScores = _filteredScores;

    /**
     * Initialise le ViewModel et configure l'accès au ScoreRepository.
     *
     * @param application instance de l'application permettant l'accès
     * aux ressources et au repository des scores.
     */
    public ProfileViewModel(@NonNull Application application) {
        super(application);
        this.repository = new ScoreRepository(application);
    }

    /**
     * Charge la liste des thèmes associés aux scores de l'utilisateur.
     *
     * Cette liste est utilisée pour remplir le menu déroulant permettant
     * de filtrer les statistiques par thème.
     *
     * L'opération est exécutée sur un thread de fond afin d'éviter
     * de bloquer l'interface utilisateur.
     *
     * @param user pseudonyme de l'utilisateur connecté
     */
    public void loadThemes(String user) {
        executorService.execute(() -> {
            List<String> userThemes = repository.getThemes(user);
            _themes.postValue(userThemes);
        });
    }

    /**
     * Charge les scores de l'utilisateur et applique les filtres sélectionnés.
     *
     * Les données sont filtrées selon :
     * - le thème choisi par l'utilisateur
     * - la période temporelle sélectionnée (tout, 24h, 7 jours, 30 jours)
     *
     * Les scores filtrés sont ensuite publiés dans LiveData afin
     * de mettre à jour automatiquement l'interface utilisateur.
     *
     * @param user pseudonyme de l'utilisateur
     * @param selectedTheme thème sélectionné dans le filtre
     * @param periodeIndex index représentant la période sélectionnée
     * @param allThemesLabel texte représentant l'option "Tous les thèmes"
     */
    public void loadAndFilterData(String user, String selectedTheme, int periodeIndex, String allThemesLabel) {
        _isLoading.postValue(true);

        executorService.execute(() -> {
            try {
                long dateLimite = calculateDateLimite(periodeIndex);

                List<String> activeThemes = repository.getThemes(user);
                List<Score> baseScores;

                if (selectedTheme.equals(allThemesLabel)) {
                    baseScores = repository.getAllScores(user);
                } else {
                    baseScores = repository.getScoresByTheme(user, selectedTheme);
                }

                List<Score> finalScores = new ArrayList<>();
                if (baseScores != null) {
                    for (Score s : baseScores) {
                        boolean themeStillExists =
                                s.theme != null &&
                                        activeThemes != null &&
                                        activeThemes.contains(s.theme);

                        if (themeStillExists && s.timestamp >= dateLimite) {
                            finalScores.add(s);
                        }
                    }
                }

                _filteredScores.postValue(finalScores);
            } finally {
                _isLoading.postValue(false);
            }
        });
    }

    /**
     * Calcule la date limite utilisée pour filtrer les scores.
     *
     * Selon la période sélectionnée, la méthode retourne le timestamp
     * correspondant au début de la période :
     * - 24 heures
     * - 7 jours
     * - 30 jours
     * - ou aucun filtrage (tout afficher)
     *
     * @param index index de la période sélectionnée
     * @return timestamp minimal accepté pour les scores
     */
    private long calculateDateLimite(int index) {
        long now = System.currentTimeMillis();
        switch (index) {
            case 1: return now - ONE_DAY_MS;
            case 2: return now - SEVEN_DAYS_MS;
            case 3: return now - THIRTY_DAYS_MS;
            default: return 0;
        }
    }

    /**
     * Méthode appelée lorsque le ViewModel est détruit.
     *
     * Elle permet de libérer les ressources utilisées par
     * l'ExecutorService afin d'éviter les fuites de threads.
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}