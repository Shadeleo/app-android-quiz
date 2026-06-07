package com.example.projetmobilel3informatiqueleopereira.ui.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.projetmobilel3informatiqueleopereira.data.models.Question;
import com.example.projetmobilel3informatiqueleopereira.data.repositories.QuestionRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ViewModel chargé de gérer l'affichage des QCM créés par l'utilisateur.
 *
 * Cette classe récupère les questions depuis le QuestionRepository,
 * les organise par thème et expose les données à l'interface via LiveData.
 *
 * Elle gère également la suppression de thèmes ou de toutes les questions.
 */
public class ListQcmViewModel extends AndroidViewModel {
    private final QuestionRepository repository;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final MutableLiveData<List<String>> _themes = new MutableLiveData<>();
    private final MutableLiveData<Map<String, List<Question>>> _groupedData = new MutableLiveData<>();
    private final MutableLiveData<String> _errorEvent = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public final LiveData<List<String>> themes = _themes;
    public final LiveData<Map<String, List<Question>>> groupedData = _groupedData;
    public final LiveData<String> errorEvent = _errorEvent;
    public final LiveData<Boolean> isLoading = _isLoading;

    /**
     * Initialise le ViewModel et configure l'accès au repository.
     *
     * @param application instance de l'application permettant l'accès
     * aux ressources et au repository de données.
     */
    public ListQcmViewModel(@NonNull Application application) {
        super(application);
        this.repository = new QuestionRepository(application);
    }

    /**
     * Charge toutes les questions associées à un utilisateur.
     *
     * Cette opération est exécutée sur un thread de fond via ExecutorService
     * afin d'éviter de bloquer le thread principal de l'interface.
     *
     * Les questions récupérées sont ensuite regroupées par thème
     * avant d'être envoyées à l'interface.
     *
     * @param pseudo pseudonyme de l'utilisateur connecté
     */
    public void loadQuestions(String pseudo) {
        _isLoading.postValue(true);

        executorService.execute(() -> {
            try {
                List<Question> questions = repository.getQuestions(pseudo);
                processAndGroupQuestions(questions);
            } catch (Exception e) {
                _errorEvent.postValue("Erreur lors du chargement des données.");
            } finally {
                _isLoading.postValue(false);
            }
        });
    }

    /**
     * Regroupe une liste de questions par thème.
     *
     * Cette méthode transforme une liste simple de questions
     * en une structure Map permettant d'afficher les questions
     * regroupées par thème dans l'interface utilisateur.
     *
     * Les thèmes sont également stockés séparément pour faciliter
     * l'affichage dans les listes.
     *
     * @param questions liste des questions récupérées depuis la base de données
     */
    private void processAndGroupQuestions(List<Question> questions) {
        Map<String, List<Question>> themeMap = new HashMap<>();
        List<String> themeList = new ArrayList<>();

        if (questions != null && !questions.isEmpty()) {
            for (Question q : questions) {
                String themeKey = (q.theme == null || q.theme.isEmpty()) ? "Divers" : q.theme;

                if (!themeMap.containsKey(themeKey)) {
                    themeMap.put(themeKey, new ArrayList<>());
                    themeList.add(themeKey);
                }
                themeMap.get(themeKey).add(q);
            }
        }
        _themes.postValue(themeList);
        _groupedData.postValue(themeMap);
    }

    /**
     * Supprime toutes les questions appartenant à un thème spécifique.
     *
     * Après la suppression, les données sont rechargées afin
     * de mettre à jour l'affichage dans l'interface.
     *
     * @param theme thème à supprimer
     * @param pseudo pseudonyme du propriétaire des questions
     */
    public void deleteTheme(String theme, String pseudo) {
        executorService.execute(() -> {
            repository.deleteTheme(theme, pseudo);
            loadQuestions(pseudo);
        });
    }

    /**
     * Supprime toutes les questions appartenant à l'utilisateur.
     *
     * Une fois la suppression effectuée, les données sont
     * automatiquement rechargées pour rafraîchir l'interface.
     *
     * @param pseudo pseudonyme de l'utilisateur
     */
    public void deleteAll(String pseudo) {
        executorService.execute(() -> {
            repository.deleteAll(pseudo);
            loadQuestions(pseudo);
        });
    }

    /**
     * Appelé lorsque le ViewModel est détruit.
     *
     * Cette méthode libère les ressources utilisées par
     * l'ExecutorService afin d'éviter les fuites de threads.
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}