package com.example.projetmobilel3informatiqueleopereira.logic.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import java.io.InputStream;

/**
 * Classe utilitaire pour la gestion des fichiers PDF.
 *
 * Fournit des méthodes permettant d'extraire du texte depuis
 * un document PDF et de récupérer son nom à partir d'un URI.
 * Utilisée notamment pour analyser un document et générer
 * des quiz à partir de son contenu.
 */
public class PdfHelper {
    private static final String TAG = "PdfHelper";
    private final Context context;

    /**
     * Initialise l'utilitaire PDF.
     *
     * Le contexte de l'application est utilisé pour accéder
     * au ContentResolver afin de lire les fichiers via leur URI.
     *
     * @param context contexte de l'application
     */
    public PdfHelper(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * Récupère le nom réel d'un fichier à partir de son URI.
     *
     * La méthode tente d'abord d'obtenir le nom via le ContentResolver.
     * Si cela échoue, elle extrait le nom directement depuis le chemin
     * du fichier contenu dans l'URI.
     *
     * @param uri URI du fichier
     * @return nom du fichier ou "Sujet Inconnu" si non disponible
     */
    public String getFileName(Uri uri) {
        if (uri == null) return "Sujet Inconnu";

        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Erreur récupération nom fichier : " + e.getMessage());
            }
        }

        if (result == null) {
            result = uri.getPath();
            int cut = result != null ? result.lastIndexOf('/') : -1;
            if (cut != -1) result = result.substring(cut + 1);
        }

        return (result == null || result.isEmpty()) ? "Sujet Inconnu" : result;
    }
}