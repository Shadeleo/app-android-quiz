package com.example.projetmobilel3informatiqueleopereira.logic.utils;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.util.Log;

import com.example.projetmobilel3informatiqueleopereira.R;

/**
 * Gestionnaire centralisé des effets sonores de l'application.
 *
 * Implémente un système basé sur SoundPool afin de jouer
 * des sons courts avec une faible latence. Cette classe
 * suit un fonctionnement de type Singleton pour éviter
 * la création multiple de SoundPool dans l'application.
 */
public class SoundManager {
    private static final String TAG = "SoundManager";
    private static SoundPool soundPool;
    private static int clickSoundId;
    private static boolean isLoaded = false;

    /**
     * Initialise le système de lecture des sons.
     *
     * Configure les attributs audio et charge les effets
     * sonores présents dans les ressources de l'application.
     * Cette méthode doit être appelée une seule fois lors
     * du démarrage de l'application.
     *
     * @param context contexte de l'application
     */
    public static synchronized void init(Context context) {
        if (soundPool != null) return;

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(audioAttributes)
                .build();

        soundPool.setOnLoadCompleteListener((pool, sampleId, status) -> {
            if (status == 0) {
                isLoaded = true;
                Log.d(TAG, "Ressources sonores chargées avec succès.");
            }
        });

        clickSoundId = soundPool.load(context, R.raw.button_click, 1);
    }

    /**
     * Joue le son associé à l'action de clic sur un bouton.
     *
     * Utilisé pour fournir un retour sonore lors
     * des interactions utilisateur.
     */
    public static void playClick() {
        playSound(clickSoundId);
    }

    /**
     * Méthode interne permettant de jouer un son chargé.
     *
     * Vérifie que le SoundPool est initialisé et que
     * les ressources audio sont prêtes avant la lecture.
     *
     * @param soundId identifiant du son à jouer
     */
    private static void playSound(int soundId) {
        if (soundPool != null && isLoaded && soundId != 0) {
            soundPool.play(soundId, 0.8f, 0.8f, 1, 0, 1.0f);
        } else {
            Log.w(TAG, "Tentative de lecture d'un son non prêt.");
        }
    }
}