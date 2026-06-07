package com.example.projetmobilel3informatiqueleopereira.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.projetmobilel3informatiqueleopereira.R;
import com.example.projetmobilel3informatiqueleopereira.databinding.ActivityLoginBinding;
import com.example.projetmobilel3informatiqueleopereira.logic.auth.AuthManager;
import com.example.projetmobilel3informatiqueleopereira.ui.viewmodels.LoginViewModel;

import java.util.Locale;

/**
 * Activité responsable de l'authentification utilisateur.
 *
 * Cette classe gère la saisie des identifiants, la validation
 * des champs, l'appel au LoginViewModel pour vérifier la connexion
 * et la redirection vers le menu principal en cas de succès.
 */
public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private LoginViewModel viewModel;
    private AuthManager authManager;

    /**
     * Initialise l'activité et prépare l'écran de connexion.
     *
     * Configure la langue, initialise le View Binding,
     * instancie les composants métier puis met en place
     * les listeners et les observers.
     *
     * @param savedInstanceState état précédent de l'activité si disponible
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyUserLocale();

        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authManager = new AuthManager(this);
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        setupListeners();
        setupObservers();
    }

    /**
     * Configure les actions associées aux boutons de l'écran.
     *
     * Gère la redirection vers l'inscription et déclenche
     * la tentative de connexion après validation locale
     * des champs saisis.
     */
    private void setupListeners() {
        binding.btnRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );
        binding.btnLogin.setOnClickListener(v -> {
            String pseudo = binding.etPseudo.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();

            if (validateInput(pseudo, password)) {
                viewModel.handleLogin(pseudo, password);
            }
        });
    }

    /**
     * Observe les états exposés par le LoginViewModel.
     *
     * Gère la réussite de connexion, l'affichage des messages
     * d'erreur et l'état d'activation du bouton de connexion.
     */
    private void setupObservers() {
        viewModel.userResult.observe(this, user -> {
            if (user != null) {
                authManager.saveSession(user.pseudo);
                navigateToMenu(user.pseudo);
            }
        });
        viewModel.errorMessageRes.observe(this, resId -> {
            if (resId != null) {
                Toast.makeText(this, getString(resId), Toast.LENGTH_LONG).show();
            }
        });
        viewModel.isLoading.observe(this, isLoading -> {
            binding.btnLogin.setEnabled(!isLoading);
        });
    }

    /**
     * Vérifie la validité minimale des identifiants saisis.
     *
     * Contrôle que le pseudo et le mot de passe ne sont pas vides
     * avant de lancer la logique d'authentification.
     *
     * @param pseudo pseudo saisi par l'utilisateur
     * @param password mot de passe saisi
     * @return true si les champs sont exploitables
     */
    private boolean validateInput(String pseudo, String password) {
        if (pseudo.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, R.string.error_fields_empty, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * Finalise la connexion et ouvre le menu principal.
     *
     * Affiche un message de bienvenue, transmet le pseudo
     * utilisateur à l'activité suivante puis ferme l'écran
     * de connexion.
     *
     * @param pseudo pseudo de l'utilisateur connecté
     */
    private void navigateToMenu(@NonNull String pseudo) {
        String welcomeMsg = getString(R.string.welcome_back, pseudo);
        Toast.makeText(this, welcomeMsg, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, MenuActivity.class);
        intent.putExtra("USER_PSEUDO", pseudo);
        startActivity(intent);
        finish();
    }

    /**
     * Applique la langue enregistrée dans les préférences utilisateur.
     *
     * Met à jour la configuration locale avant l'affichage
     * des ressources de l'interface.
     */
    private void applyUserLocale() {
        String lang = getSharedPreferences("Settings", MODE_PRIVATE)
                .getString("My_Lang", Locale.getDefault().getLanguage());

        Locale locale = new Locale(lang);
        Locale.setDefault(locale);

        android.content.res.Configuration config = new android.content.res.Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }
}