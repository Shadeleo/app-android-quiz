package com.example.projetmobilel3informatiqueleopereira;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.projetmobilel3informatiqueleopereira.logic.auth.AuthManager;
import com.example.projetmobilel3informatiqueleopereira.logic.utils.SoundManager;
import com.example.projetmobilel3informatiqueleopereira.ui.activities.LoginActivity;
import com.example.projetmobilel3informatiqueleopereira.ui.activities.MenuActivity;

/**
 * Activité principale de démarrage de l'application (Splash Screen).
 *
 * Cette activité est le point d'entrée de l'application Android.
 * Elle affiche un écran de chargement avec animation, initialise
 * certains services globaux et décide vers quel écran rediriger
 * l'utilisateur :
 *
 * - Acceptation des conditions d'utilisation
 * - Menu principal si l'utilisateur est déjà connecté
 * - Écran de connexion sinon
 */
public class MainActivity extends AppCompatActivity {
    private AuthManager authManager;
    private final Handler routingHandler = new Handler(Looper.getMainLooper());
    private static final int SPLASH_DELAY = 2000;

    /**
     * Méthode appelée lors de la création de l'activité.
     *
     * Elle initialise les services nécessaires au démarrage,
     * configure l'animation du splash screen et déclenche
     * la logique de routage après un court délai.
     *
     * @param savedInstanceState état sauvegardé de l'activité
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        authManager = new AuthManager(this);
        SoundManager.init(this);

        setupSplashAnimation();

        routingHandler.postDelayed(this::checkRoutage, SPLASH_DELAY);
    }

    /**
     * Configure et démarre l'animation du loader affiché
     * sur l'écran de démarrage.
     *
     * Cette animation améliore l'expérience utilisateur
     * en donnant un retour visuel pendant le chargement.
     */
    private void setupSplashAnimation() {
        View loader = findViewById(R.id.loaderIcon);
        if (loader != null) {
            loader.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate_loading));
        }
    }

    /**
     * Détermine vers quelle activité rediriger l'utilisateur.
     *
     * La logique de décision est la suivante :
     * 1. Si les conditions d'utilisation ne sont pas acceptées,
     *    afficher le dialogue de validation.
     * 2. Si l'utilisateur est déjà connecté, ouvrir le menu principal.
     * 3. Sinon, rediriger vers l'écran de connexion.
     */
    private void checkRoutage() {
        if (isFinishing()) return;

        if (!authManager.areTermsAccepted()) {
            showTermsDialog();
        }
        else if (authManager.isUserLoggedIn()) {
            navigateTo(MenuActivity.class, authManager.getCurrentUser());
        }
        else {
            navigateTo(LoginActivity.class, null);
        }
    }

    /**
     * Lance une nouvelle activité et termine l'écran de splash.
     *
     * Cette méthode centralise la logique de navigation afin
     * d'éviter la duplication de code dans la classe.
     *
     * @param destination activité cible à lancer
     * @param pseudo pseudonyme de l'utilisateur (optionnel)
     */
    private void navigateTo(Class<?> destination, String pseudo) {
        Intent intent = new Intent(this, destination);
        if (pseudo != null) {
            intent.putExtra("USER_PSEUDO", pseudo);
        }
        startActivity(intent);
        finish();
    }

    /**
     * Affiche une boîte de dialogue demandant à l'utilisateur
     * d'accepter les conditions générales d'utilisation (CGU).
     *
     * L'utilisateur peut :
     * - accepter les conditions et continuer l'application
     * - refuser et quitter l'application.
     */
    private void showTermsDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.terms_title)
                .setMessage(R.string.terms_message)
                .setCancelable(false)
                .setPositiveButton(R.string.btn_accept, (dialog, which) -> {
                    authManager.acceptTerms();
                    checkRoutage();
                })
                .setNegativeButton(R.string.btn_quit, (dialog, which) -> finish())
                .show();
    }

    /**
     * Méthode appelée lors de la destruction de l'activité.
     *
     * Elle supprime les callbacks du Handler afin d'éviter
     * toute fuite mémoire liée au délai du splash screen.
     */
    @Override
    protected void onDestroy() {
        routingHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}