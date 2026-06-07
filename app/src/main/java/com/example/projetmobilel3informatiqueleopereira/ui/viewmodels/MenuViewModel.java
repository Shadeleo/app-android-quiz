package com.example.projetmobilel3informatiqueleopereira.ui.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.projetmobilel3informatiqueleopereira.R;
import com.example.projetmobilel3informatiqueleopereira.logic.auth.AuthManager;
import com.example.projetmobilel3informatiqueleopereira.logic.profile.ProfileManager;
import com.example.projetmobilel3informatiqueleopereira.logic.quiz.QuizManager;
import com.example.projetmobilel3informatiqueleopereira.logic.quiz.QuizRepository;

/**
 * ViewModel principal de l'écran Menu de l'application.
 *
 * Cette classe prépare et expose les données nécessaires à l'interface :
 * - informations du profil utilisateur
 * - état du tutoriel
 * - gestion de l'importation de quiz via l'IA (Gemini)
 *
 * Elle agit comme couche intermédiaire entre l'interface utilisateur
 * et les managers métier (AuthManager, ProfileManager, QuizManager).
 */
public class MenuViewModel extends AndroidViewModel {
    private final AuthManager authManager;
    private final QuizManager quizManager;
    private final MutableLiveData<ProfileState> _profileState = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _isTutoCompleted = new MutableLiveData<>();
    private final MutableLiveData<Integer> _toastMessageRes = new MutableLiveData<>();
    public final LiveData<ProfileState> profileState = _profileState;
    public final LiveData<Boolean> isTutoCompleted = _isTutoCompleted;
    public final LiveData<Integer> toastMessageRes = _toastMessageRes;

    /**
     * Initialise le ViewModel et les gestionnaires métier nécessaires.
     *
     * @param application instance de l'application permettant
     * d'accéder aux ressources et aux managers de logique métier.
     */
    public MenuViewModel(@NonNull Application application) {
        super(application);
        this.authManager = new AuthManager(application);

        QuizRepository repo = new QuizRepository(application);
        this.quizManager = new QuizManager(repo, authManager.getCurrentUser());
    }

    /**
     * Met à jour l'état général du menu.
     *
     * Cette méthode recharge les informations du profil utilisateur
     * ainsi que l'état du tutoriel afin d'afficher les éléments
     * disponibles ou verrouillés dans le menu principal.
     *
     * Elle est généralement appelée dans le cycle de vie onResume
     * de l'activité associée.
     */
    public void refreshState() {
        _isTutoCompleted.setValue(authManager.isTutoCompleted());

        ProfileManager profileManager = new ProfileManager(getApplication());

        String user = authManager.getCurrentUser();
        String displayName = (user != null) ? user : getApplication().getString(R.string.default_user_name);

        ProfileState state = new ProfileState(
                displayName,
                profileManager.getSuccessRate(),
                profileManager.getSavedShape(),
                profileManager.getSavedColor()
        );
        _profileState.setValue(state);
    }

    /**
     * Génère le prompt à envoyer à l'IA Gemini pour produire un quiz.
     *
     * Le prompt est construit à partir du nom du fichier PDF importé
     * afin de générer des questions en lien avec son contenu.
     *
     * @param fileName nom du document analysé
     * @return chaîne contenant le prompt formaté pour Gemini
     */
    public String buildGeminiPrompt(String fileName) {
        return quizManager.buildPrompt(fileName);
    }

    /**
     * Lance l'importation d'un quiz à partir d'un JSON généré par l'IA.
     *
     * Cette méthode valide les données reçues puis délègue le traitement
     * au QuizManager qui analysera le JSON et enregistrera les questions
     * dans la base de données.
     *
     * @param json contenu JSON généré par l'IA
     * @param theme nom du thème associé au quiz
     */
    public void importJson(String json, String theme) {
        if (theme == null || theme.trim().isEmpty()) {
            _toastMessageRes.setValue(R.string.import_theme_required);
            return;
        }

        if (json == null || json.trim().isEmpty()) {
            _toastMessageRes.setValue(R.string.import_error);
            return;
        }

        quizManager.processJsonAndSave(json, theme.trim(), new QuizManager.QuizActionDelegate() {
            @Override
            public void showMessage(String msg) {
                handleImportResponse(msg);
            }
        });
    }

    /**
     * Analyse le message de retour du QuizManager afin
     * de déterminer le type de notification à afficher.
     *
     * Selon le contenu du message, un message de succès
     * ou d'erreur est envoyé à l'interface utilisateur.
     *
     * @param msg message retourné par le QuizManager
     */
    private void handleImportResponse(String msg) {
        if (msg == null) {
            _toastMessageRes.postValue(R.string.import_error);
            return;
        }

        String normalizedMsg = msg.toLowerCase().trim();

        if (normalizedMsg.contains("succès") || normalizedMsg.contains("success")) {
            _toastMessageRes.postValue(R.string.import_success);
        } else if (normalizedMsg.contains("titre invalide") || normalizedMsg.contains("thème invalide") || normalizedMsg.contains("theme invalide")) {
            _toastMessageRes.postValue(R.string.import_theme_required);
        } else {
            _toastMessageRes.postValue(R.string.import_error);
        }
    }

    /**
     * Objet représentant l'état du profil utilisateur.
     *
     * Cette classe contient toutes les informations nécessaires
     * à l'affichage du profil dans l'interface :
     * - pseudonyme
     * - taux de réussite global
     * - avatar sélectionné
     * - couleur de l'avatar
     */
    public static class ProfileState {
        public final String pseudo;
        public final int successRate;
        public final int avatarRes;
        public final int avatarColor;

        public ProfileState(String pseudo, int successRate, int avatarRes, int avatarColor) {
            this.pseudo = pseudo;
            this.successRate = successRate;
            this.avatarRes = avatarRes;
            this.avatarColor = avatarColor;
        }
    }

    /**
     * Réinitialise l'événement de notification après affichage.
     *
     * Cette méthode permet d'éviter que le même message Toast
     * soit déclenché plusieurs fois lors des changements
     * de configuration de l'activité.
     */
    public void onToastShown() {
        _toastMessageRes.setValue(null);
    }
}