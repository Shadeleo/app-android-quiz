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
 * ViewModel responsable de la gestion de l'inscription des utilisateurs.
 *
 * Cette classe valide les informations saisies par l'utilisateur
 * puis communique avec le UserRepository pour créer un nouveau compte
 * dans la base de données.
 *
 * Les résultats de l'opération (succès, erreur, état de chargement)
 * sont exposés à l'interface via des objets LiveData.
 */
public class RegisterViewModel extends AndroidViewModel {
    private final UserRepository repository;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final MutableLiveData<Boolean> _isRegistered = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> _errorMsgRes = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public final LiveData<Boolean> isRegistered = _isRegistered;
    public final LiveData<Integer> errorMsgRes = _errorMsgRes;
    public final LiveData<Boolean> isLoading = _isLoading;

    /**
     * Initialise le ViewModel et configure l'accès au UserRepository.
     *
     * @param application instance de l'application permettant
     * d'accéder aux ressources et à la base de données.
     */
    public RegisterViewModel(@NonNull Application application) {
        super(application);
        this.repository = new UserRepository(application);
    }

    /**
     * Lance le processus d'inscription d'un nouvel utilisateur.
     *
     * Cette méthode effectue d'abord une validation locale des données
     * saisies (pseudo et mot de passe). Si les critères sont respectés,
     * l'inscription est exécutée sur un thread de fond afin de ne pas
     * bloquer l'interface utilisateur.
     *
     * Le repository vérifie ensuite si le pseudo existe déjà dans la base
     * avant d'insérer le nouvel utilisateur.
     *
     * @param pseudo pseudonyme choisi par l'utilisateur
     * @param password mot de passe choisi par l'utilisateur
     */
    public void register(String pseudo, String password) {
        if (!validateInputs(pseudo, password)) return;

        _isLoading.setValue(true);

        executorService.execute(() -> {
            try {
                String cleanPseudo = pseudo.trim();

                if (repository.checkExists(cleanPseudo) == null) {
                    repository.insert(new User(cleanPseudo, password));
                    _isRegistered.postValue(true);
                } else {
                    _errorMsgRes.postValue(R.string.error_pseudo_taken);
                }
            } catch (Exception e) {
                _errorMsgRes.postValue(R.string.error_db_technical);
            } finally {
                _isLoading.postValue(false);
            }
        });
    }

    /**
     * Vérifie que les informations saisies respectent les règles de sécurité.
     *
     * Les critères sont les suivants :
     * - le pseudo doit contenir entre 3 et 15 caractères
     * - le pseudo doit contenir uniquement des lettres et des chiffres
     * - le mot de passe doit contenir au moins 8 caractères
     *
     * Si une règle n'est pas respectée, un message d'erreur est envoyé
     * à l'interface utilisateur.
     *
     * @param pseudo pseudonyme saisi
     * @param password mot de passe saisi
     * @return true si les entrées sont valides, false sinon
     */
    private boolean validateInputs(String pseudo, String password) {
        if (pseudo == null || pseudo.trim().length() < 3 || pseudo.trim().length() > 15) {
            _errorMsgRes.setValue(R.string.error_pseudo_length);
            return false;
        }

        if (!pseudo.matches("^[a-zA-Z0-9]*$")) {
            _errorMsgRes.setValue(R.string.error_pseudo_special_chars);
            return false;
        }

        if (password == null || password.length() < 8) {
            _errorMsgRes.setValue(R.string.error_password_length);
            return false;
        }

        return true;
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