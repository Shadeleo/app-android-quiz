package com.example.projetmobilel3informatiqueleopereira.ui.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.projetmobilel3informatiqueleopereira.R;
import com.example.projetmobilel3informatiqueleopereira.data.local.AppDatabase;
import com.example.projetmobilel3informatiqueleopereira.data.models.Question;
import com.example.projetmobilel3informatiqueleopereira.data.models.Score;
import com.example.projetmobilel3informatiqueleopereira.databinding.ActivityGameBinding;
import com.example.projetmobilel3informatiqueleopereira.logic.auth.AuthManager;
import com.example.projetmobilel3informatiqueleopereira.logic.profile.ProfileManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Activité principale de gestion d'une session de jeu.
 *
 * Cette classe pilote le déroulement d'une partie en mode Solo,
 * Duel ou Story : chargement des questions, affichage des réponses,
 * gestion du timer, validation des choix du joueur et sauvegarde
 * des résultats en fin de partie.
 */
public class GameActivity extends AppCompatActivity {
    private static final long QUESTION_TIMEOUT = 5000;
    private ActivityGameBinding binding;
    private AuthManager authManager;
    private AppDatabase db;
    private CountDownTimer gameTimer;
    private List<Question> questionList = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int scoreCount = 0;
    private int selectedBoxIndex = 0;
    private boolean isJ1Turn = true;
    private String currentUser;
    private String themeChoisi;
    private boolean isDuelMode;
    private boolean isStoryMode;
    private int storyLevelNumber;

    /**
     * Initialise l'activité et prépare la session de jeu.
     *
     * Configure la langue, initialise le View Binding,
     * récupère les dépendances, extrait les paramètres
     * de session puis charge le contenu de la partie.
     *
     * @param savedInstanceState état précédent de l'activité si disponible
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyLocaleConfiguration();
        super.onCreate(savedInstanceState);

        binding = ActivityGameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeDependencies();
        extractIntentData();
        setupVisualSkins();
        loadGameContent();
        setupListeners();
    }

    /**
     * Initialise les dépendances nécessaires au fonctionnement du jeu.
     *
     * Récupère notamment le gestionnaire d'authentification
     * et l'instance de la base de données locale.
     */
    private void initializeDependencies() {
        authManager = new AuthManager(this);
        db = AppDatabase.getInstance(getApplicationContext());
    }

    /**
     * Récupère les paramètres transmis à l'activité.
     *
     * Initialise les informations de session comme
     * l'utilisateur courant, le thème choisi et
     * le mode de jeu sélectionné.
     */
    private void extractIntentData() {
        currentUser = getIntent().getStringExtra("USER_PSEUDO");
        themeChoisi = getIntent().getStringExtra("THEME_CHOISI");
        isDuelMode = getIntent().getBooleanExtra("IS_DUEL", false);
        isStoryMode = getIntent().getBooleanExtra("IS_STORY_MODE", false);
        storyLevelNumber = getIntent().getIntExtra("STORY_LEVEL_NUMBER", 1);
    }

    /**
     * Configure l'apparence visuelle des avatars des joueurs.
     *
     * Charge la forme et la couleur sauvegardées dans les préférences
     * puis applique ces réglages aux personnages affichés à l'écran.
     * En mode Duel, un second avatar est également initialisé.
     */
    private void setupVisualSkins() {
        var prefs = getSharedPreferences("USER_SESSION", MODE_PRIVATE);
        int shape = prefs.getInt("USER_SHAPE", R.drawable.shape_square);
        int colorJ1 = prefs.getInt("USER_COLOR", Color.BLUE);

        binding.playerChar.setImageResource(shape);
        binding.playerChar.setColorFilter(colorJ1);

        if (isDuelMode) {
            binding.playerChar2.setVisibility(View.VISIBLE);
            binding.playerChar2.setImageResource(shape);
            binding.playerChar2.setColorFilter(Color.RED);
            binding.playerChar2.setAlpha(0.5f);
        }
    }

    /**
     * Associe les actions de sélection aux différentes réponses.
     *
     * Chaque zone de réponse permet de déplacer l'avatar actif
     * vers le choix sélectionné.
     */
    private void setupListeners() {
        binding.box1.setOnClickListener(v -> moveActivePlayerTo(binding.box1, 1));
        binding.box2.setOnClickListener(v -> moveActivePlayerTo(binding.box2, 2));
        binding.box3.setOnClickListener(v -> moveActivePlayerTo(binding.box3, 3));
        binding.box4.setOnClickListener(v -> moveActivePlayerTo(binding.box4, 4));
    }

    /**
     * Charge les questions de la partie en arrière-plan.
     *
     * La source des questions dépend du mode de jeu et du thème choisi.
     * Une fois les données récupérées, la liste est mélangée puis
     * la première question est affichée sur le thread principal.
     */
    private void loadGameContent() {
        new Thread(() -> {
            List<Question> raw;

            boolean isAllThemes =
                    themeChoisi == null
                            || themeChoisi.trim().isEmpty()
                            || themeChoisi.equalsIgnoreCase(getString(R.string.all_themes))
                            || themeChoisi.equalsIgnoreCase(getString(R.string.theme_all_categories))
                            || themeChoisi.equalsIgnoreCase(getString(R.string.theme_mixed))
                            || themeChoisi.equalsIgnoreCase("All Themes");

            if (isStoryMode || "TUTO".equals(themeChoisi)) {
                raw = db.questionDao().getQuestionsByTheme(themeChoisi);
            } else if (isAllThemes) {
                raw = db.questionDao().getAllByUser(currentUser);
            } else {
                raw = db.questionDao().getQuestionsByThemeAndUser(themeChoisi, currentUser);
            }

            runOnUiThread(() -> {
                if (raw != null && !raw.isEmpty()) {
                    questionList.clear();
                    questionList.addAll(raw);
                    Collections.shuffle(questionList);
                    displayCurrentQuestion();
                } else {
                    handleEmptyQuestions();
                }
            });
        }).start();
    }

    /**
     * Affiche la question courante et ses choix de réponse.
     *
     * Met à jour le contenu de l'interface puis démarre
     * le compte à rebours associé à la question.
     */
    private void displayCurrentQuestion() {
        if (gameTimer != null) gameTimer.cancel();

        Question q = questionList.get(currentQuestionIndex);
        binding.tvQuestion.setText(q.question);
        binding.box1.setText(q.choice1);
        binding.box2.setText(q.choice2);
        binding.box3.setText(q.choice3);
        binding.box4.setText(q.choice4);

        startCountdown();
    }

    /**
     * Déplace l'avatar actif vers la réponse sélectionnée.
     *
     * Calcule la position cible à partir de la vue choisie,
     * lance l'animation de déplacement puis enregistre
     * l'index de la réponse sélectionnée.
     *
     * @param target vue correspondant à la réponse choisie
     * @param index index de la réponse sélectionnée
     */
    private void moveActivePlayerTo(View target, int index) {
        ImageView activeChar = (isDuelMode && !isJ1Turn) ? binding.playerChar2 : binding.playerChar;

        float targetX = target.getX() + (target.getWidth() / 2f) - (activeChar.getWidth() / 2f);
        float targetY = target.getY() + (target.getHeight() / 2f) - (activeChar.getHeight() / 2f);

        activeChar.animate().x(targetX).y(targetY).setDuration(300).start();
        selectedBoxIndex = index;

        if (isDuelMode) toggleTurn();
    }

    /**
     * Change le joueur actif en mode Duel.
     *
     * Met à jour l'état du tour courant et ajuste
     * la transparence des avatars pour indiquer
     * visuellement quel joueur doit répondre.
     */
    private void toggleTurn() {
        isJ1Turn = !isJ1Turn;
        binding.playerChar.setAlpha(isJ1Turn ? 1.0f : 0.5f);
        binding.playerChar2.setAlpha(isJ1Turn ? 0.5f : 1.0f);
    }

    /**
     * Démarre le compte à rebours de la question en cours.
     *
     * Met à jour l'affichage du temps restant chaque seconde
     * puis valide automatiquement la réponse à la fin du délai.
     */
    private void startCountdown() {
        gameTimer = new CountDownTimer(QUESTION_TIMEOUT, 1000) {
            public void onTick(long m) {
                binding.tvTimer.setText(getString(R.string.timer_seconds, (int) (m / 1000)));
            }
            public void onFinish() { validateAnswer(); }
        }.start();
    }

    /**
     * Vérifie si la réponse sélectionnée est correcte.
     *
     * Incrémente le score en cas de bonne réponse
     * puis passe à la question suivante. Sinon,
     * la partie est terminée immédiatement.
     */
    private void validateAnswer() {
        Question q = questionList.get(currentQuestionIndex);
        if (selectedBoxIndex == q.correctAnswer) {
            scoreCount++;
            advanceToNextQuestion();
        } else {
            endGame();
        }
    }

    /**
     * Passe à la question suivante de la partie.
     *
     * Réinitialise la sélection courante puis affiche
     * la prochaine question si disponible. Sinon,
     * la session de jeu prend fin.
     */
    private void advanceToNextQuestion() {
        currentQuestionIndex++;
        if (currentQuestionIndex < questionList.size()) {
            selectedBoxIndex = 0;
            displayCurrentQuestion();
        } else {
            endGame();
        }
    }

    /**
     * Termine la partie et gère les traitements de fin de session.
     *
     * Arrête le timer, met à jour la progression Story si nécessaire,
     * sauvegarde les résultats puis affiche un dialogue de fin de partie.
     */
    private void endGame() {
        if (gameTimer != null) gameTimer.cancel();

        if (isStoryMode && scoreCount >= (questionList.size() / 2)) {
            authManager.setLevelReached(storyLevelNumber + 1);
            if (storyLevelNumber == 1) authManager.setTutoCompleted(true);
        }

        persistFinalResults();

        new AlertDialog.Builder(this)
                .setTitle(R.string.game_over)
                .setMessage(getString(R.string.victory, scoreCount))
                .setCancelable(false)
                .setPositiveButton(R.string.dialog_ok, (d, w) -> finish())
                .show();
    }

    /**
     * Sauvegarde les résultats finaux de la partie.
     *
     * Calcule le pourcentage de réussite, enregistre le score
     * dans la base de données et met à jour les statistiques
     * globales du profil utilisateur.
     */
    private void persistFinalResults() {
        if (questionList.isEmpty()) return;

        int percentage = (scoreCount * 100) / questionList.size();

        Score scoreEntity = new Score();
        scoreEntity.owner = currentUser;
        scoreEntity.theme = (themeChoisi != null) ? themeChoisi : getString(R.string.theme_mixed);
        scoreEntity.pourcentage = (float) percentage;
        scoreEntity.timestamp = System.currentTimeMillis();

        ProfileManager profileManager = new ProfileManager(this);
        profileManager.updateGlobalStats(percentage);

        new Thread(() -> db.scoreDao().insertScore(scoreEntity)).start();
    }

    /**
     * Gère le cas où aucune question n'est disponible.
     *
     * Affiche un message d'erreur à l'utilisateur
     * puis ferme l'activité.
     */
    private void handleEmptyQuestions() {
        Toast.makeText(this, getString(R.string.error_no_questions, themeChoisi), Toast.LENGTH_SHORT).show();
        finish();
    }

    /**
     * Applique la langue enregistrée dans les préférences utilisateur.
     *
     * Met à jour la configuration des რესsources avant
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
     * Libère les ressources temporisées avant la destruction de l'activité.
     *
     * Annule le timer actif afin d'éviter toute exécution
     * résiduelle après la fermeture de l'écran.
     */
    @Override
    protected void onDestroy() {
        if (gameTimer != null) gameTimer.cancel();
        super.onDestroy();
    }
}