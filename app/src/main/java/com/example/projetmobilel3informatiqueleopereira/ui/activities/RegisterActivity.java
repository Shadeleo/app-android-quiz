package com.example.projetmobilel3informatiqueleopereira.ui.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.projetmobilel3informatiqueleopereira.R;
import com.example.projetmobilel3informatiqueleopereira.databinding.ActivityRegisterBinding;
import com.example.projetmobilel3informatiqueleopereira.ui.viewmodels.RegisterViewModel;

import java.util.Locale;

/**
 * Activité gérant la création d'un nouveau compte utilisateur.
 * Valide les entrées et communique avec le ViewModel pour l'enregistrement en base de données.
 */
public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private RegisterViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 1. Appliquer la langue avant d'afficher l'interface
        applyLocaleConfiguration();

        super.onCreate(savedInstanceState);

        // 2. Initialisation du View Binding
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 3. Initialisation du ViewModel
        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        setupListeners();
        setupObservers();
    }

    /**
     * Configure les actions des boutons.
     */
    private void setupListeners() {
        binding.btnConfirmRegister.setOnClickListener(v -> {
            String pseudo = binding.etRegPseudo.getText().toString().trim();
            String password = binding.etRegPassword.getText().toString().trim();

            if (validateFields(pseudo, password)) {
                viewModel.register(pseudo, password);
            }
        });
        binding.btnBackToLogin.setOnClickListener(v -> finish());
    }

    /**
     * Abonne l'activité aux changements d'état du RegisterViewModel.
     */
    private void setupObservers() {
        // Enregistrement réussi
        viewModel.isRegistered.observe(this, registered -> {
            if (Boolean.TRUE.equals(registered)) {
                Toast.makeText(this, R.string.register_success, Toast.LENGTH_LONG).show();
                // Retourne à l'activité de Login
                finish();
            }
        });

        // Gestion des erreurs via les IDs de ressources (ex: Pseudo déjà pris)
        viewModel.errorMsgRes.observe(this, resId -> {
            if (resId != null) {
                Toast.makeText(this, getString(resId), Toast.LENGTH_SHORT).show();
            }
        });

        // État de chargement pour le feedback visuel
        viewModel.isLoading.observe(this, loading -> {
            binding.btnConfirmRegister.setEnabled(!loading);
            // Optionnel : binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        });
    }

    /**
     * Validation simple côté client pour éviter les requêtes inutiles.
     */
    private boolean validateFields(String pseudo, String password) {
        if (pseudo.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, R.string.error_fields_empty, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (password.length() < 4) {
            Toast.makeText(this, R.string.error_password_too_short, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * Applique la préférence linguistique de l'utilisateur.
     */
    private void applyLocaleConfiguration() {
        String lang = getSharedPreferences("Settings", MODE_PRIVATE)
                .getString("My_Lang", Locale.getDefault().getLanguage());

        Locale locale = new Locale(lang);
        Locale.setDefault(locale);

        android.content.res.Configuration config = new android.content.res.Configuration();
        config.setLocale(locale);

        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }
}