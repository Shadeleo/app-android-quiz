package com.example.projetmobilel3informatiqueleopereira.ui.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.projetmobilel3informatiqueleopereira.R;
import com.example.projetmobilel3informatiqueleopereira.databinding.ActivityCreateQuizBinding;
import com.example.projetmobilel3informatiqueleopereira.ui.viewmodels.CreateQuizViewModel;
import com.example.projetmobilel3informatiqueleopereira.ui.viewmodels.factory.QuizViewModelFactory;

import java.util.Locale;

/**
 * Activité permettant à l'utilisateur de créer manuellement des questions de quiz.
 *
 * Cette classe gère la saisie des informations du quiz (thème, question,
 * propositions et réponse correcte) puis délègue la logique de sauvegarde
 * au CreateQuizViewModel selon l'architecture MVVM.
 */
public class CreateQuizActivity extends AppCompatActivity {
    private ActivityCreateQuizBinding binding;
    private CreateQuizViewModel viewModel;

    /**
     * Initialise l'activité et prépare l'interface de création de quiz.
     *
     * Configure la langue, initialise le View Binding, récupère la session
     * utilisateur puis instancie le ViewModel via une Factory afin d'injecter
     * le pseudo du joueur.
     *
     * @param savedInstanceState état précédent de l'activité si existant
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyUserLocale();

        super.onCreate(savedInstanceState);

        binding = ActivityCreateQuizBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String currentUserPseudo = resolveUserSession();

        QuizViewModelFactory factory = new QuizViewModelFactory(getApplication(), currentUserPseudo);
        viewModel = new ViewModelProvider(this, factory).get(CreateQuizViewModel.class);

        initializeUI();
        setupObservers();
    }

    /**
     * Configure les éléments interactifs de l'interface.
     *
     * Initialise le spinner de sélection de la réponse correcte
     * et associe le bouton de sauvegarde à l'action d'enregistrement
     * de la question via le ViewModel.
     */
    private void initializeUI() {
        setupCorrectAnswerSpinner();

        binding.btnSave.setOnClickListener(v -> {
            viewModel.saveQuestion(
                    binding.etTheme.getText().toString().trim(),
                    binding.etQuestion.getText().toString().trim(),
                    binding.etC1.getText().toString().trim(),
                    binding.etC2.getText().toString().trim(),
                    binding.etC3.getText().toString().trim(),
                    binding.etC4.getText().toString().trim(),
                    binding.spCorrect.getSelectedItemPosition()
            );
        });
    }

    /**
     * Configure le Spinner permettant de sélectionner la réponse correcte.
     *
     * Les libellés sont récupérés depuis les ressources traduites
     * afin de supporter le multilingue.
     */
    private void setupCorrectAnswerSpinner() {
        String[] options = getResources().getStringArray(R.array.quiz_options);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                options
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spCorrect.setAdapter(adapter);
    }

    /**
     * Observe les états exposés par le ViewModel.
     *
     * Gère l'affichage des messages utilisateur, l'activation du
     * bouton de sauvegarde et la fermeture automatique de l'écran
     * après l'enregistrement d'une question.
     */
    private void setupObservers() {
        viewModel.toastMessageRes.observe(this, resId -> {
            if (resId != null) {
                Toast.makeText(this, getString(resId), Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.isSaving.observe(this, isSaving ->
                binding.btnSave.setEnabled(!isSaving));

        viewModel.closeActivity.observe(this, shouldClose -> {
            if (Boolean.TRUE.equals(shouldClose)) {
                finish();
            }
        });
    }

    /**
     * Récupère le pseudo de l'utilisateur actif.
     *
     * La méthode vérifie d'abord la présence du pseudo dans l'Intent
     * puis utilise les SharedPreferences comme solution de secours.
     *
     * @return pseudo de l'utilisateur ou valeur par défaut
     */
    @NonNull
    private String resolveUserSession() {
        String pseudo = getIntent().getStringExtra("USER_PSEUDO");
        if (pseudo == null) {
            SharedPreferences userPrefs = getSharedPreferences("USER_SESSION", MODE_PRIVATE);
            pseudo = userPrefs.getString("current_user", getString(R.string.default_anonymous));
        }
        return pseudo;
    }

    /**
     * Applique la langue enregistrée dans les préférences utilisateur.
     *
     * Cette configuration est exécutée avant l'initialisation
     * de l'interface afin d'afficher correctement les ressources
     * localisées.
     */
    private void applyUserLocale() {
        SharedPreferences langPrefs = getSharedPreferences("Settings", MODE_PRIVATE);
        String langCode = langPrefs.getString("My_Lang", Locale.getDefault().getLanguage());

        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);

        android.content.res.Configuration config = new android.content.res.Configuration();
        config.setLocale(locale);

        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }
}