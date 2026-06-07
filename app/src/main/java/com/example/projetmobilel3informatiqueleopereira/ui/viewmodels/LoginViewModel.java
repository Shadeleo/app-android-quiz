package com.example.projetmobilel3informatiqueleopereira.ui.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.projetmobilel3informatiqueleopereira.R;
import com.example.projetmobilel3informatiqueleopereira.data.models.User;
import com.example.projetmobilel3informatiqueleopereira.data.repositories.UserRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ViewModel responsable de la gestion de l'authentification utilisateur.
 *
 * Cette classe sert d'intermédiaire entre l'interface utilisateur (LoginActivity)
 * et la couche de données (UserRepository).
 *
 * Elle valide les entrées utilisateur, exécute l'authentification sur un thread
 * de fond et expose les résultats via des objets LiveData observables.
 */
public class LoginViewModel extends AndroidViewModel {
    private final UserRepository repository;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final MutableLiveData<User> _userResult = new MutableLiveData<>();
    private final MutableLiveData<Integer> _errorMessageRes = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public final LiveData<User> userResult = _userResult;
    public final LiveData<Integer> errorMessageRes = _errorMessageRes;
    public final LiveData<Boolean> isLoading = _isLoading;

    /**
     * Initialise le ViewModel et configure l'accès au repository utilisateur.
     *
     * @param application instance de l'application utilisée pour accéder
     * aux ressources et au repository.
     */
    public LoginViewModel(@NonNull Application application) {
        super(application);
        this.repository = new UserRepository(application);
    }

    /**
     * Gère une tentative de connexion utilisateur.
     *
     * Cette méthode vérifie d'abord la validité des champs saisis,
     * puis lance l'opération d'authentification de manière asynchrone
     * via un ExecutorService pour éviter de bloquer le thread principal.
     *
     * Si l'utilisateur est trouvé dans la base de données,
     * le résultat est publié dans userResult.
     * Sinon, un message d'erreur est envoyé à l'interface.
     *
     * @param pseudo pseudonyme saisi par l'utilisateur
     * @param password mot de passe saisi par l'utilisateur
     */
    public void handleLogin(String pseudo, String password) {
        if (pseudo == null || pseudo.trim().isEmpty() || password == null || password.isEmpty()) {
            _errorMessageRes.setValue(R.string.error_fill_all_fields);
            return;
        }
        _isLoading.setValue(true);

        executorService.execute(() -> {
            try {
                User user = repository.login(pseudo.trim(), password);

                if (user != null) {
                    _userResult.postValue(user);
                } else {
                    _errorMessageRes.postValue(R.string.error_invalid_credentials);
                }
            } catch (Exception e) {
                _errorMessageRes.postValue(R.string.error_technical);
            } finally {
                _isLoading.postValue(false);
            }
        });
    }

    /**
     * Méthode appelée lorsque le ViewModel est détruit.
     *
     * Elle permet de libérer les ressources utilisées par
     * l'ExecutorService afin d'éviter toute fuite de threads.
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}