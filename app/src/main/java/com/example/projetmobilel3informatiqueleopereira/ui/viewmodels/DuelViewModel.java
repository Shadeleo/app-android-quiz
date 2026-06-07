package com.example.projetmobilel3informatiqueleopereira.ui.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.projetmobilel3informatiqueleopereira.data.models.Question;
import com.example.projetmobilel3informatiqueleopereira.logic.game.DuelGameManager;
import com.example.projetmobilel3informatiqueleopereira.logic.quiz.QuizRepository;

/**
 * ViewModel responsable de la gestion du mode Duel.
 *
 * Cette classe coordonne la logique de la partie entre deux joueurs,
 * gère l'état courant du jeu (question, scores, fin de partie)
 * et communique avec le repository pour charger les questions.
 */
public class DuelViewModel extends AndroidViewModel {
    private final QuizRepository repository;
    private DuelGameManager gameManager;
    private final MutableLiveData<Question> _currentQuestion = new MutableLiveData<>();
    private final MutableLiveData<Integer> _scoreJ1 = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> _scoreJ2 = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> _isGameOver = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> _noDataError = new MutableLiveData<>(false);
    public final LiveData<Question> currentQuestion = _currentQuestion;
    public final LiveData<Integer> scoreJ1 = _scoreJ1;
    public final LiveData<Integer> scoreJ2 = _scoreJ2;
    public final LiveData<Boolean> isGameOver = _isGameOver;
    public final LiveData<Boolean> noDataError = _noDataError;

    /**
     * Initialise le ViewModel et le repository des questions.
     *
     * @param application instance de l'application utilisée pour accéder
     * aux ressources et au repository.
     */
    public DuelViewModel(@NonNull Application application) {
        super(application);
        this.repository = new QuizRepository(application);
    }

    /**
     * Charge les questions nécessaires au mode Duel.
     *
     * Les questions sont récupérées via le QuizRepository,
     * puis utilisées pour initialiser le DuelGameManager
     * qui gérera la logique de la partie.
     *
     * @param user pseudo du joueur
     * @param theme thème sélectionné pour le duel
     */
    public void loadQuestions(String user, String theme) {
        repository.getQuestionsForUser(user, theme, list -> {
            if (list == null || list.isEmpty()) {
                _noDataError.postValue(true);
            } else {
                gameManager = new DuelGameManager(list);
                nextQuestion();
            }
        });
    }

    /**
     * Passe à la question suivante du duel.
     *
     * Si toutes les questions ont été posées,
     * la fin de partie est signalée à l'interface.
     */
    public void nextQuestion() {
        if (gameManager == null) return;

        if (gameManager.isGameOver()) {
            _isGameOver.postValue(true);
        } else {
            _currentQuestion.postValue(gameManager.getCurrentQuestion());
        }
    }

    /**
     * Traite les réponses des deux joueurs.
     *
     * Convertit les réponses détectées en valeurs numériques,
     * les transmet au moteur de jeu puis met à jour les scores
     * exposés à l'interface.
     *
     * @param j1Str réponse détectée pour le joueur 1
     * @param j2Str réponse détectée pour le joueur 2
     */
    public void submitAnswers(String j1Str, String j2Str) {
        if (gameManager != null) {
            int j1 = parseAnswer(j1Str);
            int j2 = parseAnswer(j2Str);

            gameManager.submitAnswers(j1, j2);

            _scoreJ1.postValue(gameManager.getScoreJ1());
            _scoreJ2.postValue(gameManager.getScoreJ2());
        }
    }

    /**
     * Convertit une réponse textuelle en valeur numérique.
     *
     * Retourne 0 si la valeur est invalide ou ne peut
     * pas être convertie en entier.
     *
     * @param answer réponse détectée sous forme de chaîne
     * @return valeur entière correspondante
     */
    private int parseAnswer(String answer) {
        if (answer == null || answer.trim().isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(answer.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}