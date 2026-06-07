package com.example.projetmobilel3informatiqueleopereira.logic.quiz;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.example.projetmobilel3informatiqueleopereira.data.local.AppDatabase;
import com.example.projetmobilel3informatiqueleopereira.data.models.Question;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository chargé de l'accès aux questions de quiz dans la base de données.
 *
 * Cette classe gère la communication avec Room (AppDatabase) pour
 * enregistrer et récupérer les questions. Les opérations sont exécutées
 * sur des threads secondaires afin d'éviter de bloquer le thread UI.
 */
public class QuizRepository {
    private final AppDatabase db;
    private final Context context;

    /**
     * Interface de callback utilisée pour retourner les questions
     * chargées de manière asynchrone vers l'interface utilisateur.
     */
    public interface OnQuestionsLoadedListener {
        void onLoaded(List<Question> questions);
    }

    /**
     * Initialise le repository et récupère l'instance de la base de données.
     *
     * @param context contexte de l'application utilisé pour accéder
     * à AppDatabase et aux ressources système.
     */
    public QuizRepository(Context context) {
        this.context = context;
        this.db = AppDatabase.getInstance(context);
    }

    /**
     * Enregistre une liste de questions dans la base de données.
     *
     * Chaque question est associée au thème et au propriétaire
     * avant d'être insérée. L'opération est exécutée dans un
     * thread secondaire puis un callback est déclenché sur
     * le thread principal lorsque la sauvegarde est terminée.
     *
     * @param questions liste des questions à sauvegarder
     * @param theme thème associé aux questions
     * @param owner pseudo du créateur des questions
     * @param onDone action exécutée après la sauvegarde
     */
    public void saveQuestions(List<Question> questions, String theme, String owner, Runnable onDone) {
        new Thread(() -> {
            for (Question q : questions) {
                q.theme = theme;
                q.owner = owner;
            }
            db.questionDao().insertAll(questions);
            if (onDone != null) {
                new Handler(Looper.getMainLooper()).post(onDone);
            }
        }).start();
    }

    /**
     * Charge les questions d'un utilisateur de manière asynchrone.
     *
     * Les questions peuvent être filtrées par thème ou récupérer
     * l'ensemble des thèmes. Un filtrage supplémentaire garantit
     * que seules les questions appartenant à l'utilisateur courant
     * sont retournées.
     *
     * @param pseudo pseudo du propriétaire des questions
     * @param theme thème sélectionné (ou tous les thèmes)
     * @param listener callback recevant la liste des questions
     */
    public void getQuestionsForUser(String pseudo, String theme, OnQuestionsLoadedListener listener) {
        new Thread(() -> {
            List<Question> rawList;

            if (theme == null || theme.isEmpty() || theme.equalsIgnoreCase("Tous les thèmes") || theme.equalsIgnoreCase("All Themes")) {
                rawList = db.questionDao().getAllByUser(pseudo);
            } else {
                rawList = db.questionDao().getQuestionsByTheme(theme);
            }

            List<Question> filteredList = new ArrayList<>();
            for (Question q : rawList) {
                if (q.owner != null && q.owner.equals(pseudo)) {
                    filteredList.add(q);
                }
            }

            new Handler(Looper.getMainLooper()).post(() -> {
                if (listener != null) {
                    listener.onLoaded(filteredList);
                }
            });
        }).start();
    }
}