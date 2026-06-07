package com.example.projetmobilel3informatiqueleopereira.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * Custom View utilisée comme couche d'affichage au-dessus du flux caméra.
 *
 * Responsabilités :
 * - Afficher la position des mains détectées pour chaque joueur.
 * - Dessiner les curseurs et les réponses sélectionnées.
 * - Fournir un feedback visuel (correct / incorrect) pendant le duel.
 *
 * Les coordonnées des mains sont normalisées (0 → 1) puis converties
 * en coordonnées écran lors du rendu dans onDraw().
 */
public class OverlayView extends View {
    private Paint paintJ1, paintJ2, linePaint, feedbackPaint;

    public float j1HandX = -1, j1HandY = -1;
    public float j2HandX = -1, j2HandY = -1;
    public String j1Reponse = "", j2Reponse = "";

    public int statusJ1 = 0;
    public int statusJ2 = 0;

    public OverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initPaints();
    }

    private void initPaints() {
        paintJ1 = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintJ1.setColor(Color.CYAN);
        paintJ1.setTextSize(80f);
        paintJ1.setStrokeWidth(5f);
        paintJ1.setFakeBoldText(true);

        paintJ2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintJ2.setColor(Color.RED);
        paintJ2.setTextSize(80f);
        paintJ2.setStrokeWidth(5f);
        paintJ2.setFakeBoldText(true);

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.WHITE);
        linePaint.setStrokeWidth(8f);
        linePaint.setAlpha(150);

        feedbackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        feedbackPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float w = getWidth();
        float h = getHeight();
        float midX = w / 2f;

        drawFeedbackBackgrounds(canvas, w, h, midX);
        canvas.drawLine(midX, 0, midX, h, linePaint);
        if (j1HandX != -1) {
            drawPlayerMarker(canvas, j1HandX * w, j1HandY * h, j1Reponse, paintJ1);
        }

        if (j2HandX != -1) {
            drawPlayerMarker(canvas, j2HandX * w, j2HandY * h, j2Reponse, paintJ2);
        }
    }

    private void drawFeedbackBackgrounds(Canvas canvas, float w, float h, float midX) {
        if (statusJ1 != 0) {
            feedbackPaint.setColor(statusJ1 == 1 ? Color.GREEN : Color.RED);
            feedbackPaint.setAlpha(60);
            canvas.drawRect(0, 0, midX, h, feedbackPaint);
        }

        if (statusJ2 != 0) {
            feedbackPaint.setColor(statusJ2 == 1 ? Color.GREEN : Color.RED);
            feedbackPaint.setAlpha(60);
            canvas.drawRect(midX, 0, w, h, feedbackPaint);
        }
    }

    private void drawPlayerMarker(Canvas canvas, float x, float y, String text, Paint paint) {
        canvas.drawCircle(x, y, 35f, paint);
        canvas.drawText(text, x - 30f, y - 70f, paint);
    }
}