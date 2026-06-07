package com.example.projetmobilel3informatiqueleopereira.ui.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.projetmobilel3informatiqueleopereira.R;
import com.example.projetmobilel3informatiqueleopereira.data.models.Question;
import com.example.projetmobilel3informatiqueleopereira.data.repositories.QuestionRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ViewModel responsable de la gestion de la sélection des thèmes de quiz.
 *
 * Cette classe récupère les thèmes disponibles pour un utilisateur,
 * permet d'afficher un aperçu des questions associées à un thème
 * et expose les données à l'interface via LiveData.
 *
 * Les opérations d'accès aux données sont exécutées sur un thread
 * de fond afin d'éviter de bloquer l'interface utilisateur.
 */
public class SelectionThemeViewModel extends AndroidViewModel {
    private final QuestionRepository repository;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final MutableLiveData<List<String>> _themes = new MutableLiveData<>();
    private final MutableLiveData<String> _themePreview = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public final LiveData<List<String>> themes = _themes;
    public final LiveData<String> themePreview = _themePreview;

    /**
     * Initialise le ViewModel et configure l'accès au QuestionRepository.
     *
     * @param application instance de l'application permettant
     * d'accéder aux ressources et à la base de données.
     */
    public SelectionThemeViewModel(@NonNull Application application) {
        super(application);
        this.repository = new QuestionRepository(application);
    }

    /**
     * Charge la liste des thèmes disponibles pour l'utilisateur.
     *
     * Cette méthode récupère les thèmes stockés dans la base de données
     * puis ajoute automatiquement une option globale permettant
     * de jouer avec toutes les questions disponibles.
     *
     * Le résultat est publié dans LiveData afin d'être observé
     * par l'activité affichant la liste des thèmes.
     *
     * @param pseudo pseudonyme de l'utilisateur
     * @param allThemesLabel texte correspondant à l'option "Tous les thèmes"
     */
    public void loadThemes(String pseudo, String allThemesLabel) {
        _isLoading.postValue(true);
        executorService.execute(() -> {
            try {
                List<String> dbThemes = repository.getAllThemesByUser(pseudo);
                List<String> combinedList = new ArrayList<>();


                combinedList.add(allThemesLabel);

                if (dbThemes != null) {
                    combinedList.addAll(dbThemes);
                }
                _themes.postValue(combinedList);
            } finally {
                _isLoading.postValue(false);
            }
        });
    }

    /**
     * Charge un aperçu des questions appartenant à un thème donné.
     *
     * Les questions sont récupérées depuis le repository puis
     * transformées en une chaîne de texte lisible permettant
     * à l'utilisateur de visualiser rapidement le contenu du thème.
     *
     * @param theme nom du thème sélectionné
     */
    public void loadThemePreview(String theme) {
        executorService.execute(() -> {
            List<Question> questions = repository.getQuestionsByTheme(theme);

            if (questions == null || questions.isEmpty()) {
                _themePreview.postValue(getApplication().getString(R.string.no_questions_found));
                return;
            }

            _themePreview.postValue(buildPreviewString(questions));
        });
    }

    /**
     * Construit une chaîne de caractères représentant un aperçu des questions.
     *
     * Chaque question est affichée avec son texte suivi
     * de l'indication de la réponse correcte.
     *
     * @param questions liste des questions du thème
     * @return chaîne formatée contenant l'aperçu des questions
     */
    private String buildPreviewString(List<Question> questions) {
        StringBuilder sb = new StringBuilder();
        for (Question q : questions) {
            sb.append("📌 ").append(q.question).append("\n");

            String formattedAnswer = getApplication().getString(
                    R.string.preview_correct_answer,
                    q.correctAnswer
            );

            sb.append(formattedAnswer).append("\n\n");
        }
        return sb.toString();
    }

    /**
     * Méthode appelée lors de la destruction du ViewModel.
     *
     * Elle permet d'arrêter l'ExecutorService afin d'éviter
     * toute fuite de threads en arrière-plan.
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}