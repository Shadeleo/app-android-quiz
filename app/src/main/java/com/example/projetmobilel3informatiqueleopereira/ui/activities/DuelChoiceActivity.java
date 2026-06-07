package com.example.projetmobilel3informatiqueleopereira.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.projetmobilel3informatiqueleopereira.R;
import com.example.projetmobilel3informatiqueleopereira.databinding.ActivityDuelChoiceBinding;
import com.example.projetmobilel3informatiqueleopereira.ui.viewmodels.DuelChoiceViewModel;

import java.util.Locale;

/**
 * Activité permettant de choisir le type de duel.
 *
 * Cette classe sert d'écran intermédiaire où l'utilisateur
 * sélectionne le mode de duel (local via caméra ou futur
 * mode en ligne) avant d'accéder à la sélection du thème.
 */
public class DuelChoiceActivity extends AppCompatActivity {
    private ActivityDuelChoiceBinding binding;
    private DuelChoiceViewModel viewModel;

    /**
     * Initialise l'activité et prépare la sélection du mode Duel.
     *
     * Configure la langue, initialise le View Binding,
     * instancie le ViewModel puis récupère le pseudo de
     * l'utilisateur pour l'associer à la session de jeu.
     *
     * @param savedInstanceState état précédent de l'activité si existant
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyUserLocale();

        super.onCreate(savedInstanceState);

        binding = ActivityDuelChoiceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(DuelChoiceViewModel.class);

        String pseudo = getIntent().getStringExtra("USER_PSEUDO");
        viewModel.initUser(pseudo);

        setupListeners();
    }

    /**
     * Configure les actions associées aux boutons de sélection de mode.
     *
     * Permet de lancer un duel local utilisant la caméra ou
     * d'afficher un message indiquant qu'une fonctionnalité
     * en ligne sera disponible prochainement.
     */
    private void setupListeners() {
        binding.btnLocalDuel.setOnClickListener(v -> navigateToThemeSelection());
        binding.btnOnlineDuel.setOnClickListener(v ->
                Toast.makeText(this, R.string.feature_coming_soon, Toast.LENGTH_SHORT).show()
        );
    }

    /**
     * Lance l'activité de sélection du thème pour le mode Duel.
     *
     * Transmet les paramètres nécessaires (pseudo utilisateur
     * et configuration du mode de jeu) à l'activité suivante.
     */
    private void navigateToThemeSelection() {
        Intent intent = new Intent(this, SelectionThemeActivity.class);

        intent.putExtra("USER_PSEUDO", viewModel.userPseudo.getValue());
        intent.putExtra("IS_DUEL", true);
        intent.putExtra("USE_CAMERA", true);

        startActivity(intent);
    }

    /**
     * Applique la langue choisie par l'utilisateur.
     *
     * Charge la préférence linguistique stockée dans les
     * SharedPreferences et met à jour la configuration
     * des ressources de l'application.
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