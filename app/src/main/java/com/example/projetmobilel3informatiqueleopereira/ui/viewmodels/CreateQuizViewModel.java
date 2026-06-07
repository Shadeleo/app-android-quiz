package com.example.projetmobilel3informatiqueleopereira.ui.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.projetmobilel3informatiqueleopereira.R;
import com.example.projetmobilel3informatiqueleopereira.data.models.Question;
import com.example.projetmobilel3informatiqueleopereira.logic.quiz.QuizManager;
import com.example.projetmobilel3informatiqueleopereira.logic.quiz.QuizRepository;

/**
 * ViewModel responsable de la création et de l'enregistrement des questions de quiz.
 *
 * Cette classe gère la validation des données saisies par l'utilisateur,
 * construit les objets Question et délègue leur sauvegarde au QuizManager.
 * Elle expose également l'état de l'opération via des LiveData observables.
 */
public class CreateQuizViewModel extends AndroidViewModel {
    private final QuizManager quizManager;
    private final MutableLiveData<Integer> _toastMessageRes = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _isSaving = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> _closeActivity = new MutableLiveData<>(false);
    public final LiveData<Integer> toastMessageRes = _toastMessageRes;
    public final LiveData<Boolean> isSaving = _isSaving;
    public final LiveData<Boolean> closeActivity = _closeActivity;

    /**
     * Initialise le ViewModel et prépare la gestion des quiz.
     *
     * Crée le repository et le QuizManager nécessaires pour
     * enregistrer les questions associées à l'utilisateur courant.
     *
     * @param application instance de l'application
     * @param pseudo pseudo de l'utilisateur créant les questions
     */
    public CreateQuizViewModel(@NonNull Application application, String pseudo) {
        super(application);
        QuizRepository repository = new QuizRepository(application);
        this.quizManager = new QuizManager(repository, pseudo);
    }

    /**
     * Valide les données saisies puis enregistre une nouvelle question.
     *
     * Vérifie que tous les champs sont remplis et que le format
     * de la question est valide avant de créer l'objet Question
     * et de lancer la sauvegarde via le QuizManager.
     *
     * @param theme thème associé à la question
     * @param qTxt texte de la question
     * @param c1 premier choix de réponse
     * @param c2 deuxième choix de réponse
     * @param c3 troisième choix de réponse
     * @param c4 quatrième choix de réponse
     * @param correctPos position de la réponse correcte dans la liste
     */
    public void saveQuestion(String theme, String qTxt, String c1, String c2, String c3, String c4, int correctPos) {
        if (isAnyFieldEmpty(theme, qTxt, c1, c2, c3, c4)) {
            _toastMessageRes.setValue(R.string.error_all_fields_required);
            return;
        }
        if (!isQuestionFormatValid(qTxt)) {
            _toastMessageRes.setValue(R.string.error_invalid_question_format);
            return;
        }

        _isSaving.setValue(true);

        Question q = createQuestionObject(qTxt, c1, c2, c3, c4, correctPos);

        quizManager.saveSingleQuestion(q, theme, new QuizManager.QuizActionDelegate() {
            @Override
            public void showMessage(String msg) {
                handleSaveResult(msg);
            }

        });
    }

    /**
     * Vérifie si au moins un champ de saisie est vide.
     *
     * @param fields liste des champs à contrôler
     * @return true si un champ est vide ou null
     */
    private boolean isAnyFieldEmpty(String... fields) {
        for (String field : fields) {
            if (field == null || field.trim().isEmpty()) return true;
        }
        return false;
    }

    /**
     * Construit un objet Question à partir des données saisies.
     *
     * Initialise les champs de la question et convertit
     * l'index de la réponse correcte vers le format attendu
     * par le modèle (1 à 4).
     *
     * @param qTxt texte de la question
     * @param c1 premier choix
     * @param c2 deuxième choix
     * @param c3 troisième choix
     * @param c4 quatrième choix
     * @param correctPos position de la réponse correcte
     * @return instance de Question prête à être enregistrée
     */
    private Question createQuestionObject(String qTxt, String c1, String c2, String c3, String c4, int correctPos) {
        Question q = new Question();
        q.question = qTxt;
        q.choice1 = c1;
        q.choice2 = c2;
        q.choice3 = c3;
        q.choice4 = c4;
        q.correctAnswer = correctPos + 1; // Ajustement de l'index (0-3 vers 1-4)
        return q;
    }

    /**
     * Analyse le résultat retourné par le QuizManager.
     *
     * Détermine si l'opération de sauvegarde a réussi
     * puis met à jour les LiveData correspondantes
     * pour informer l'interface utilisateur.
     *
     * @param msg message retourné par le gestionnaire de quiz
     */
    private void handleSaveResult(String msg) {
        if (msg == null) {
            _toastMessageRes.postValue(R.string.msg_save_error);
            _isSaving.postValue(false);
            return;
        }

        String normalizedMsg = msg.toLowerCase().trim();

        boolean isSuccess =
                normalizedMsg.contains("succès")
                        || normalizedMsg.contains("success")
                        || normalizedMsg.contains("ajoutée")
                        || normalizedMsg.contains("ajoutee")
                        || normalizedMsg.contains("enregistrée")
                        || normalizedMsg.contains("enregistree");

        if (isSuccess) {
            _toastMessageRes.postValue(R.string.msg_save_success);
            _closeActivity.postValue(true);
        } else {
            _toastMessageRes.postValue(R.string.msg_save_error);
            _isSaving.postValue(false);
        }
    }

    /**
     * Vérifie que le format de la question est valide.
     *
     * Autorise uniquement certains caractères et limite
     * la longueur afin d'éviter les entrées invalides.
     *
     * @param question texte de la question
     * @return true si le format est valide
     */
    private boolean isQuestionFormatValid(String question) {
        if (question == null) return false;

        return question.matches("^[a-zA-Z0-9+\\-*/= ]{1,100}$");
    }
}