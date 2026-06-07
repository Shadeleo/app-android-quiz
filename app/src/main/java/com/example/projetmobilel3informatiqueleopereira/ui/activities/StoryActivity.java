package com.example.projetmobilel3informatiqueleopereira.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.projetmobilel3informatiqueleopereira.R;
import com.example.projetmobilel3informatiqueleopereira.databinding.ActivityStoryBinding;
import com.example.projetmobilel3informatiqueleopereira.ui.adaptater.StoryAdapter;
import com.example.projetmobilel3informatiqueleopereira.ui.viewmodels.StoryViewModel;

import java.util.Locale;

/**
 * Activité gérant le mode Histoire du jeu.
 *
 * Cette classe affiche les différents niveaux disponibles,
 * gère leur état de verrouillage selon la progression du joueur
 * et déclenche le lancement d'une partie pour le niveau sélectionné.
 */
public class StoryActivity extends AppCompatActivity {
    private ActivityStoryBinding binding;
    private StoryViewModel viewModel;

    /**
     * Initialise l'activité et prépare l'affichage du mode Histoire.
     *
     * Configure la langue, initialise le View Binding,
     * instancie le ViewModel puis met en place les observers
     * et charge les données de progression.
     *
     * @param savedInstanceState état précédent de l'activité si disponible
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        applyLocaleConfiguration();

        super.onCreate(savedInstanceState);

        binding = ActivityStoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(StoryViewModel.class);

        setupObservers();
        refreshData();
    }

    /**
     * Observe les données exposées par le StoryViewModel.
     *
     * Met à jour la liste des niveaux affichés et gère
     * les interactions utilisateur pour lancer un niveau
     * ou afficher l'état de verrouillage.
     */
    private void setupObservers() {
        viewModel.levels.observe(this, levels -> {
            int progress = viewModel.getProgressValue();

            StoryAdapter adapter = new StoryAdapter(this, levels, progress);
            binding.listLevels.setAdapter(adapter);

            binding.listLevels.setOnItemClickListener((parent, view, position, id) -> {
                int levelClicked = position + 1;

                if (levelClicked < progress) {
                    Toast.makeText(this, R.string.level_already_won, Toast.LENGTH_SHORT).show();
                } else if (levelClicked == progress) {
                    viewModel.onLevelClicked(position);
                } else {
                    String lockMsg = getString(R.string.level_locked, progress);
                    Toast.makeText(this, lockMsg, Toast.LENGTH_SHORT).show();
                }
            });
        });

        viewModel.startLevelEvent.observe(this, theme -> {
            if (theme != null) {
                lancerGameActivity(theme, viewModel.getProgressValue());
                viewModel.consumeStartLevelEvent();
            }
        });
    }

    /**
     * Lance une partie en mode Histoire pour le niveau sélectionné.
     *
     * Prépare les paramètres nécessaires au moteur de jeu
     * puis démarre GameActivity avec les informations
     * de thème et de progression.
     *
     * @param theme thème associé au niveau
     * @param levelNumber numéro du niveau sélectionné
     */
    private void lancerGameActivity(String theme, int levelNumber) {
        SharedPreferences prefs = getSharedPreferences("USER_SESSION", MODE_PRIVATE);
        String pseudo = prefs.getString("USER_PSEUDO", "Joueur");

        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("USER_PSEUDO", pseudo);
        intent.putExtra("THEME_CHOISI", theme);
        intent.putExtra("STORY_LEVEL_NUMBER", levelNumber);
        intent.putExtra("IS_STORY_MODE", true);

        startActivity(intent);
    }

    /**
     * Recharge les données de progression du mode Histoire.
     *
     * Transmet au ViewModel les libellés des niveaux
     * afin d'actualiser l'affichage selon la langue active.
     */
    private void refreshData() {
        viewModel.loadProgress(
                getString(R.string.level_1),
                getString(R.string.level_2),
                getString(R.string.level_3),
                getString(R.string.level_4),
                getString(R.string.level_coming_soon)
        );
    }

    /**
     * Applique la langue enregistrée dans les préférences utilisateur.
     *
     * Met à jour la configuration des ressources avant
     * l'affichage de l'interface.
     */
    private void applyLocaleConfiguration() {
        String lang = getSharedPreferences("Settings", MODE_PRIVATE).getString("My_Lang", "fr");
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    /**
     * Rafraîchit les données de progression au retour sur l'activité.
     *
     * Permet de mettre à jour l'état des niveaux après
     * la fin d'une partie dans GameActivity.
     */
    @Override
    protected void onResume() {
        super.onResume();
        refreshData();
    }
}