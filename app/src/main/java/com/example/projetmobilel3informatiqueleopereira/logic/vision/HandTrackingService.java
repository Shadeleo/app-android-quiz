package com.example.projetmobilel3informatiqueleopereira.logic.vision;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mediapipe.framework.image.BitmapImageBuilder;
import com.google.mediapipe.framework.image.MPImage;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker;
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service de vision chargé du suivi des mains en temps réel.
 *
 * Cette classe combine CameraX et MediaPipe afin de détecter
 * les mains visibles dans le flux caméra, d'estimer leur position
 * à l'écran et d'interpréter certains gestes simples comme le
 * nombre de doigts levés pour chaque joueur.
 */
public class HandTrackingService {
    private final Context context;
    private final HandResultListener listener;
    private HandLandmarker handLandmarker;
    private final ExecutorService cameraExecutor;

    /**
     * Interface de callback utilisée pour transmettre les résultats
     * de détection des mains à la couche UI.
     *
     * Elle retourne pour chaque joueur la valeur détectée ainsi que
     * les coordonnées normalisées de la main dans l'image.
     */
    public interface HandResultListener {
        void onResult(String resJ1, String resJ2, float x1, float y1, float x2, float y2);
    }

    /**
     * Initialise le service de suivi des mains.
     *
     * Prépare les dépendances nécessaires à l'analyse vidéo
     * et configure MediaPipe pour la détection en temps réel.
     *
     * @param context contexte de l'application
     * @param listener callback recevant les résultats de détection
     */
    public HandTrackingService(Context context, HandResultListener listener) {
        this.context = context;
        this.listener = listener;
        this.cameraExecutor = Executors.newSingleThreadExecutor();
        setupMediaPipe();
    }

    /**
     * Configure et initialise le modèle MediaPipe de détection des mains.
     *
     * Définit les options du modèle, le mode d'exécution temps réel,
     * le nombre maximal de mains à détecter ainsi que le callback
     * de traitement des résultats.
     */
    private void setupMediaPipe() {
        try {
            var baseOptions = com.google.mediapipe.tasks.core.BaseOptions.builder()
                    .setModelAssetPath("hand_landmarker.task").build();

            var options = HandLandmarker.HandLandmarkerOptions.builder()
                    .setBaseOptions(baseOptions)
                    .setRunningMode(RunningMode.LIVE_STREAM)
                    .setNumHands(2)
                    .setMinHandDetectionConfidence(0.5f)
                    .setResultListener(this::processResult)
                    .build();

            handLandmarker = HandLandmarker.createFromOptions(context, options);
        } catch (Exception e) {
            Log.e("VisionService", "MediaPipe Init Error", e);
        }
    }

    /**
     * Démarre la caméra frontale et lance l'analyse des images.
     *
     * Le flux caméra est affiché dans le PreviewView puis analysé
     * image par image afin d'envoyer les frames à MediaPipe pour
     * la détection des mains.
     *
     * @param lifecycleOwner propriétaire du cycle de vie utilisé par CameraX
     * @param viewFinder vue affichant l'aperçu caméra
     */
    public void startCamera(LifecycleOwner lifecycleOwner, PreviewView viewFinder) {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(context);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

                ImageAnalysis analysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                analysis.setAnalyzer(cameraExecutor, imageProxy -> {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        Bitmap bitmap = viewFinder.getBitmap();

                        if (bitmap != null && handLandmarker != null) {
                            MPImage mpImage = new BitmapImageBuilder(bitmap).build();

                            handLandmarker.detectAsync(mpImage, System.currentTimeMillis());
                        }

                        imageProxy.close();
                    });
                });

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_FRONT_CAMERA, preview, analysis);

            } catch (Exception e) {
                Log.e("VisionService", "CameraX Error", e);
            }
        }, androidx.core.content.ContextCompat.getMainExecutor(context));
    }

    /**
     * Estime le nombre de doigts levés à partir des landmarks de la main.
     *
     * La détection repose sur la comparaison entre la position verticale
     * des extrémités des doigts et celle de leurs articulations intermédiaires.
     *
     * @param landmarks liste des points normalisés détectés sur la main
     * @return nombre de doigts considérés comme levés
     */
    private void processResult(HandLandmarkerResult result, MPImage mpImage) {
        String resJ1 = "", resJ2 = "";
        float x1 = -1, y1 = -1, x2 = -1, y2 = -1;

        if (result.landmarks() != null && !result.landmarks().isEmpty()) {
            for (int i = 0; i < result.landmarks().size(); i++) {
                var landmarks = result.landmarks().get(i);
                int fingers = countFingers(landmarks);
                float rawX = landmarks.get(0).x();
                float rawY = landmarks.get(0).y();

                if (rawX < 0.5f) {
                    x1 = rawX; y1 = rawY; resJ1 = String.valueOf(fingers);
                } else {
                    x2 = rawX; y2 = rawY; resJ2 = String.valueOf(fingers);
                }
            }
        }

        listener.onResult(resJ1, resJ2, x1, y1, x2, y2);
    }

    /**
     * Estime le nombre de doigts levés à partir des landmarks de la main.
     *
     * La détection repose sur la comparaison entre la position verticale
     * des extrémités des doigts et celle de leurs articulations intermédiaires.
     *
     * @param landmarks liste des points normalisés détectés sur la main
     * @return nombre de doigts considérés comme levés
     */
    private int countFingers(List<com.google.mediapipe.tasks.components.containers.NormalizedLandmark> landmarks) {
        int count = 0;
        int[] tips = {8, 12, 16, 20};
        int[] pips = {6, 10, 14, 18};
        for (int i = 0; i < tips.length; i++) {
            if (landmarks.get(tips[i]).y() < landmarks.get(pips[i]).y()) count++;
        }
        return count;
    }
}