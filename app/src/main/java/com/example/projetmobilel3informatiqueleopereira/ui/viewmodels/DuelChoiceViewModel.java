package com.example.projetmobilel3informatiqueleopereira.ui.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.projetmobilel3informatiqueleopereira.R;
import com.example.projetmobilel3informatiqueleopereira.logic.auth.AuthManager;

/**
 * ViewModel chargé de préparer les données nécessaires au choix du mode Duel.
 *
 * Cette classe gère la récupération du pseudonyme de l'utilisateur
 * et expose cette information sous forme de LiveData afin d'être
 * utilisée par l'interface utilisateur.
 */
public class DuelChoiceViewModel extends AndroidViewModel {
    private final AuthManager authManager;
    private final MutableLiveData<String> _userPseudo = new MutableLiveData<>();
    public final LiveData<String> userPseudo = _userPseudo;

    /**
     * Initialise le ViewModel et les dépendances nécessaires.
     *
     * Instancie le gestionnaire d'authentification afin de
     * pouvoir récupérer les informations de session utilisateur.
     *
     * @param application instance de l'application
     */
    public DuelChoiceViewModel(@NonNull Application application) {
        super(application);
        this.authManager = new AuthManager(application);
    }

    /**
     * Initialise le pseudonyme de l'utilisateur courant.
     *
     * Si un pseudo est fourni par l'activité précédente,
     * celui-ci est utilisé en priorité. Sinon, le ViewModel
     * tente de récupérer le pseudo enregistré dans la session
     * active via l'AuthManager.
     *
     * @param intentPseudo pseudo éventuellement transmis via l'Intent
     */
    public void initUser(String intentPseudo) {
        if (intentPseudo != null && !intentPseudo.trim().isEmpty()) {
            _userPseudo.setValue(intentPseudo);
        } else {
            String current = authManager.getCurrentUser();
            String defaultName = getApplication().getString(R.string.default_user_name);
            _userPseudo.setValue(current != null ? current : defaultName);
        }
    }
}