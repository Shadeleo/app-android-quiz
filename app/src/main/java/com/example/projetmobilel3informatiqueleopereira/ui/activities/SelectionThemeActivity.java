package com.example.projetmobilel3informatiqueleopereira.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.projetmobilel3informatiqueleopereira.R;
import com.example.projetmobilel3informatiqueleopereira.databinding.ActivitySelectionThemeBinding;
import com.example.projetmobilel3informatiqueleopereira.databinding.LayoutBottomSheetQuestionsBinding;
import com.example.projetmobilel3informatiqueleopereira.ui.viewmodels.SelectionThemeViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Locale;

/**
 * Activité permettant de sélectionner un thème de quiz.
 *
 * Cette classe affiche les thèmes disponibles pour l'utilisateur,
 * permet de lancer une partie selon le mode choisi et propose
 * une prévisualisation des questions via un BottomSheet.
 */
public class SelectionThemeActivity extends AppCompatActivity {
    private ActivitySelectionThemeBinding binding;
    private SelectionThemeViewModel viewModel;
    private String currentUser;

    /**
     * Initialise l'activité et charge les thèmes disponibles.
     *
     * Configure la langue, initialise le View Binding,
     * récupère l'utilisateur courant puis met en place
     * les observations nécessaires avant de charger les thèmes.
     *
     * @param savedInstanceState état précédent de l'activité si disponible
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyLocaleConfiguration();
        super.onCreate(savedInstanceState);

        binding = ActivitySelectionThemeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        currentUser = getIntent().getStringExtra("USER_PSEUDO");
        viewModel = new ViewModelProvider(this).get(SelectionThemeViewModel.class);

        if (currentUser == null || currentUser.isEmpty()) {
            Toast.makeText(this, R.string.error_session_expired, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupObservers();
        viewModel.loadThemes(currentUser, getString(R.string.all_themes));
    }

    /**
     * Observe la liste des thèmes exposée par le ViewModel.
     *
     * Met à jour l'affichage des thèmes et configure
     * les interactions utilisateur pour le lancement
     * d'une partie ou l'aperçu d'un thème.
     */
    private void setupObservers() {
        viewModel.themes.observe(this, themes -> {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_theme_selection, themes);
            binding.listThemes.setAdapter(adapter);

            binding.listThemes.setOnItemClickListener((p, v, pos, id) ->
                    launchGameMode(themes.get(pos))
            );

            binding.listThemes.setOnItemLongClickListener((p, v, pos, id) -> {
                if (pos == 0) {
                    Toast.makeText(this, R.string.preview_unavailable, Toast.LENGTH_SHORT).show();
                } else {
                    showThemePreview(themes.get(pos));
                }
                return true;
            });
        });
    }

    /**
     * Lance l'activité de jeu adaptée au mode sélectionné.
     *
     * Détermine si la partie doit être jouée en mode classique
     * ou en mode Duel avec caméra puis transmet les paramètres
     * nécessaires à l'activité cible.
     *
     * @param theme thème sélectionné par l'utilisateur
     */
    private void launchGameMode(String theme) {
        boolean isDuel = getIntent().getBooleanExtra("IS_DUEL", false);
        boolean useCamera = getIntent().getBooleanExtra("USE_CAMERA", false);

        Class<?> targetClass = (isDuel && useCamera) ? DuelCameraActivity.class : GameActivity.class;

        Intent intent = new Intent(this, targetClass);
        intent.putExtra("THEME_CHOISI", theme);
        intent.putExtra("USER_PSEUDO", currentUser);
        intent.putExtra("IS_DUEL", isDuel);

        startActivity(intent);
    }

    /**
     * Affiche un aperçu des questions d'un thème dans un BottomSheet.
     *
     * Charge le contenu du thème sélectionné via le ViewModel
     * puis met à jour dynamiquement la vue de prévisualisation.
     *
     * @param theme thème dont les questions doivent être affichées
     */
    private void showThemePreview(String theme) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        LayoutBottomSheetQuestionsBinding sheetBinding = LayoutBottomSheetQuestionsBinding.inflate(getLayoutInflater());
        dialog.setContentView(sheetBinding.getRoot());

        sheetBinding.txtSheetTitle.setText(getString(R.string.preview_title, theme));
        sheetBinding.txtContentQuestions.setText(R.string.loading);

        viewModel.themePreview.observe(this, content -> {
            if (content != null) {
                sheetBinding.txtContentQuestions.setText(
                        content.isEmpty() ? getString(R.string.no_questions_found) : content
                );
            }
        });

        viewModel.loadThemePreview(theme);
        dialog.show();
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
}
