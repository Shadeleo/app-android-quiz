package com.example.projetmobilel3informatiqueleopereira.ui.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.projetmobilel3informatiqueleopereira.R;
import com.example.projetmobilel3informatiqueleopereira.databinding.ActivityDuelCameraBinding;
import com.example.projetmobilel3informatiqueleopereira.logic.vision.HandTrackingService;
import com.example.projetmobilel3informatiqueleopereira.ui.viewmodels.DuelViewModel;


/**
 * Activité gérant le mode Duel avec caméra et détection des mains.
 *
 * Cette classe coordonne l'affichage du duel, le suivi des mains
 * via la caméra, le compte à rebours de chaque manche et les
 * interactions avec le DuelViewModel pour faire progresser la partie.
 */
public class DuelCameraActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final long ROUND_DURATION_MS = 7000;
    private ActivityDuelCameraBinding binding;
    private DuelViewModel viewModel;
    private HandTrackingService visionService;
    private CountDownTimer gameTimer;

    /**
     * Initialise l'activité et prépare le mode Duel.
     *
     * Met en place le View Binding, initialise le ViewModel,
     * configure le service de vision puis charge les questions
     * correspondant à l'utilisateur et au thème sélectionné.
     *
     * @param savedInstanceState état précédent de l'activité si disponible
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDuelCameraBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(DuelViewModel.class);

        initializeVisionService();
        setupObservers();

        String pseudo = getIntent().getStringExtra("USER_PSEUDO");
        String theme = getIntent().getStringExtra("THEME_CHOISI");
        viewModel.loadQuestions(pseudo, theme);
    }

    /**
     * Initialise le service de détection des mains.
     *
     * Connecte les résultats de détection à l'overlay graphique
     * afin de mettre à jour en temps réel les positions des mains
     * et les réponses détectées pour chaque joueur.
     */
    private void initializeVisionService() {
        visionService = new HandTrackingService(this, (resultJ1, resultJ2, x1, y1, x2, y2) -> {
            binding.overlay.j1HandX = x1; binding.overlay.j1HandY = y1;
            binding.overlay.j2HandX = x2; binding.overlay.j2HandY = y2;
            binding.overlay.j1Reponse = resultJ1; binding.overlay.j2Reponse = resultJ2;
            binding.overlay.postInvalidate();
        });
    }

    /**
     * Observe les données exposées par le ViewModel.
     *
     * Met à jour l'interface selon l'état courant de la partie :
     * question affichée, scores, fin de jeu ou absence de données.
     */
    private void setupObservers() {
        viewModel.currentQuestion.observe(this, q -> {
            if (q == null) return;
            binding.tvQuestion.setText(q.question);
            binding.box1.setText(q.choice1);
            binding.box2.setText(q.choice2);
            binding.box3.setText(q.choice3);
            binding.box4.setText(q.choice4);
            startRoundTimer();
        });

        viewModel.scoreJ1.observe(this, s ->
                binding.tvScoreJ1.setText(getString(R.string.score_title, s)));

        viewModel.scoreJ2.observe(this, s ->
                binding.tvScoreJ2.setText(getString(R.string.score_title, s)));

        viewModel.isGameOver.observe(this, over -> {
            if (Boolean.TRUE.equals(over)) {
                handleGameOver();
            }
        });

        viewModel.noDataError.observe(this, hasError -> {
            if (Boolean.TRUE.equals(hasError)) {
                Toast.makeText(this, R.string.error_no_questions, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    /**
     * Démarre le compte à rebours associé à la question en cours.
     *
     * À la fin du temps imparti, les réponses détectées sont
     * automatiquement soumises puis la partie passe à la
     * question suivante.
     */
    private void startRoundTimer() {
        if (gameTimer != null) gameTimer.cancel();

        gameTimer = new CountDownTimer(ROUND_DURATION_MS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                binding.tvTimer.setText(getString(R.string.timer_seconds, millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                viewModel.submitAnswers(binding.overlay.j1Reponse, binding.overlay.j2Reponse);
                viewModel.nextQuestion();
            }
        }.start();

        checkCameraPermissionAndStart();
    }

    /**
     * Vérifie l'autorisation d'accès à la caméra puis démarre l'analyse vidéo.
     *
     * Si la permission est déjà accordée, le flux caméra est lancé
     * immédiatement. Sinon, une demande de permission est envoyée
     * à l'utilisateur.
     */
    private void checkCameraPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            visionService.startCamera(this, binding.viewFinder);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }
    }

    /**
     * Gère la fin de la partie.
     *
     * Affiche un message récapitulatif puis ferme l'activité
     * lorsque le duel est terminé.
     */
    private void handleGameOver() {
        String msg = getString(R.string.game_over) + " " + binding.tvScoreJ1.getText();
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        finish();
    }

    /**
     * Met en pause les éléments temporisés de la partie.
     *
     * Le timer en cours est annulé afin d'éviter une exécution
     * en arrière-plan lorsque l'activité n'est plus visible.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (gameTimer != null) gameTimer.cancel();
    }

    /**
     * Libère les ressources utilisées par l'activité avant sa destruction.
     *
     * Annule le timer actif afin d'éviter toute fuite mémoire
     * ou exécution résiduelle après fermeture de l'écran.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (gameTimer != null) gameTimer.cancel();
    }
}