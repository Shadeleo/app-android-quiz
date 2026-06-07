package com.example.projetmobilel3informatiqueleopereira.ui.activities;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.projetmobilel3informatiqueleopereira.R;
import com.example.projetmobilel3informatiqueleopereira.databinding.ActivityListQcmBinding;
import com.example.projetmobilel3informatiqueleopereira.logic.sensor.ShakeDetector;
import com.example.projetmobilel3informatiqueleopereira.ui.adaptater.ThemeAdapter;
import com.example.projetmobilel3informatiqueleopereira.ui.viewmodels.ListQcmViewModel;

import java.util.Locale;

/**
 * Activité affichant les QCM créés par l'utilisateur.
 *
 * Cette classe présente les thèmes et questions enregistrés,
 * permet la suppression d'un thème spécifique et intègre
 * un système de suppression globale via détection de secousse
 * grâce à l'accéléromètre.
 */
public class ListQcmActivity extends AppCompatActivity {
    private ActivityListQcmBinding binding;
    private ListQcmViewModel viewModel;
    private SensorManager sensorManager;
    private ShakeDetector shakeDetector;
    private String pseudoConnecte;
    private boolean isDialogOpen = false;

    /**
     * Initialise l'activité et prépare l'affichage des QCM.
     *
     * Configure la langue, initialise le View Binding,
     * récupère le pseudo utilisateur, instancie le ViewModel
     * puis met en place l'interface, le capteur et les observers.
     *
     * @param savedInstanceState état précédent de l'activité si disponible
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyUserLocale();
        super.onCreate(savedInstanceState);

        binding = ActivityListQcmBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        pseudoConnecte = getIntent().getStringExtra("USER_PSEUDO");
        viewModel = new ViewModelProvider(this).get(ListQcmViewModel.class);

        initializeUI();
        initializeShakeSensor();
        setupObservers();

        viewModel.loadQuestions(pseudoConnecte);
    }

    /**
     * Configure les composants visuels principaux de l'écran.
     *
     * Initialise notamment le RecyclerView utilisé pour
     * afficher la liste des thèmes et des questions.
     */
    private void initializeUI() {
        binding.recyclerQcm.setLayoutManager(new LinearLayoutManager(this));
    }

    /**
     * Observe les données exposées par le ViewModel.
     *
     * Met à jour l'interface lorsque les thèmes ou les
     * questions changent, et affiche les éventuels
     * messages d'erreur remontés par le ViewModel.
     */
    private void setupObservers() {
        viewModel.themes.observe(this, themes -> refreshAdapter());
        viewModel.groupedData.observe(this, data -> refreshAdapter());

        viewModel.errorEvent.observe(this, errorMsg -> {
            if (errorMsg != null) Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Met à jour l'adaptateur du RecyclerView avec les données courantes.
     *
     * Reconstruit l'adaptateur à partir de la liste des thèmes
     * et des données groupées afin de refléter l'état actuel
     * des QCM enregistrés.
     */
    private void refreshAdapter() {
        var themes = viewModel.themes.getValue();
        var data = viewModel.groupedData.getValue();

        if (themes != null && data != null) {
            ThemeAdapter adapter = new ThemeAdapter(themes, data, this::showDeleteThemeDialog);
            binding.recyclerQcm.setAdapter(adapter);
        }
    }

    /**
     * Met à jour l'adaptateur du RecyclerView avec les données courantes.
     *
     * Reconstruit l'adaptateur à partir de la liste des thèmes
     * et des données groupées afin de refléter l'état actuel
     * des QCM enregistrés.
     */
    private void initializeShakeSensor() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        shakeDetector = new ShakeDetector(() -> {
            boolean hasQuestions = viewModel.groupedData.getValue() != null && !viewModel.groupedData.getValue().isEmpty();            if (!isDialogOpen && hasQuestions) {
                showDeleteAllDialog();
            } else if (!isDialogOpen) {
                Toast.makeText(this, R.string.no_questions_to_delete, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Affiche une confirmation avant de supprimer un thème spécifique.
     *
     * Si l'utilisateur valide l'action, toutes les questions
     * associées à ce thème pour le joueur courant sont supprimées.
     *
     * @param theme thème à supprimer
     */
    private void showDeleteThemeDialog(String theme) {
       String message = getString(R.string.dialog_delete_theme_confirm) + " " + theme;

        new AlertDialog.Builder(this, R.style.CustomAlertDialog)
                .setTitle(R.string.delete)
                .setMessage(message)
                .setPositiveButton(R.string.btn_delete_confirm, (d, w) -> viewModel.deleteTheme(theme, pseudoConnecte))
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    /**
     * Affiche une confirmation avant de supprimer tous les QCM utilisateur.
     *
     * Cette action est généralement déclenchée par une secousse
     * détectée sur l'appareil.
     */
    private void showDeleteAllDialog() {
        isDialogOpen = true;
        new AlertDialog.Builder(this, R.style.CustomAlertDialog)
                .setTitle(R.string.dialog_delete_all_title)
                .setMessage(R.string.dialog_delete_all_message)
                .setPositiveButton(R.string.btn_delete_confirm, (d, w) -> {
                    isDialogOpen = false;
                    viewModel.deleteAll(pseudoConnecte);
                })
                .setNegativeButton(R.string.btn_cancel, (d, w) -> isDialogOpen = false)
                .setOnCancelListener(dialog -> isDialogOpen = false)
                .show();
    }

    /**
     * Réactive l'écoute du capteur d'accéléromètre.
     *
     * Enregistre le ShakeDetector lorsque l'activité
     * redevient visible à l'écran.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (sensorManager != null) {
            Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(shakeDetector, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    /**
     * Désactive l'écoute du capteur lors de la mise en pause.
     *
     * Cela permet d'éviter une consommation inutile
     * de batterie lorsque l'activité n'est plus visible.
     */
    @Override
    protected void onPause() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(shakeDetector);
        }
        super.onPause();
    }

    /**
     * Applique la langue enregistrée dans les préférences utilisateur.
     *
     * Met à jour la configuration des ressources avant
     * l'affichage de l'interface.
     */
    private void applyUserLocale() {
        String lang = getSharedPreferences("Settings", MODE_PRIVATE).getString("My_Lang", "fr");
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }
}