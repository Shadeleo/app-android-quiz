package com.example.projetmobilel3informatiqueleopereira.logic.sensor;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Détecteur de mouvement de type "Shake" basé sur l'accéléromètre.
 *
 * Cette classe analyse les variations d'accélération du capteur afin
 * de détecter lorsqu'un utilisateur secoue l'appareil. Lorsqu'un
 * mouvement dépassant un certain seuil est détecté, un callback est
 * déclenché via l'interface OnShakeListener.
 */
public class ShakeDetector implements SensorEventListener {
    private static final int SHAKE_THRESHOLD = 12;
    private static final int SHAKE_SKIP_TIME = 1000;
    private float mAcceleration = 0f;
    private float mCurrentAcceleration = SensorManager.GRAVITY_EARTH;
    private float mLastAcceleration = SensorManager.GRAVITY_EARTH;
    private long lastShakeTimestamp = 0;
    private final OnShakeListener listener;

    /**
     * Interface de callback déclenchée lorsqu'un mouvement de secousse
     * est détecté par le capteur.
     */
    public interface OnShakeListener {
        void onShake();
    }

    /**
     * Initialise le détecteur de secousse.
     *
     * @param listener callback appelé lorsqu'un shake est détecté
     */
    public ShakeDetector(OnShakeListener listener) {
        this.listener = listener;
    }

    /**
     * Méthode appelée automatiquement lors d'un changement
     * de valeur du capteur (accéléromètre).
     *
     * Elle calcule l'accélération totale à partir des axes X, Y et Z,
     * applique un filtre pour lisser les variations puis vérifie si
     * le seuil de détection de secousse est dépassé.
     *
     * Si une secousse est détectée et que le délai minimum est respecté,
     * le callback onShake() est déclenché.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        mLastAcceleration = mCurrentAcceleration;
        mCurrentAcceleration = (float) Math.sqrt(x * x + y * y + z * z);

        float delta = mCurrentAcceleration - mLastAcceleration;

        mAcceleration = mAcceleration * 0.9f + delta;

        if (mAcceleration > SHAKE_THRESHOLD) {
            long now = System.currentTimeMillis();
            if (lastShakeTimestamp + SHAKE_SKIP_TIME > now) {
                return;
            }
            lastShakeTimestamp = now;
            listener.onShake();
        }
    }

    /**
     * Méthode appelée lorsque la précision du capteur change.
     *
     * Aucun traitement particulier n'est nécessaire dans ce cas
     * pour la détection de secousse.
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}