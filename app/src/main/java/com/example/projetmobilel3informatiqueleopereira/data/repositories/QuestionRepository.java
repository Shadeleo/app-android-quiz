package com.example.projetmobilel3informatiqueleopereira.data.repositories;

import android.content.Context;
import com.example.projetmobilel3informatiqueleopereira.data.local.AppDatabase;
import com.example.projetmobilel3informatiqueleopereira.data.models.Question;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository responsable de l'accès et de la gestion des questions.
 *
 * Cette classe agit comme couche intermédiaire entre l'application
 * et la base de données Room. Elle centralise les opérations liées
 * aux questions (lecture, suppression, récupération par thème, etc.)
 * via le QuestionDao.
 */
public class QuestionRepository {
    private final AppDatabase db;

    /**
     * Initialise le repository et récupère l'instance de la base de données.
     *
     * @param context contexte de l'application utilisé pour accéder
     * à l'instance singleton de AppDatabase.
     */
    public QuestionRepository(Context context) {
        this.db = AppDatabase.getInstance(context.getApplicationContext());
    }

    /**
     * Récupère la liste des thèmes créés par un utilisateur.
     *
     * Si aucun thème n'est trouvé, une liste vide est retournée
     * afin d'éviter les valeurs null dans l'application.
     *
     * @param user pseudo de l'utilisateur
     * @return liste des thèmes associés à l'utilisateur
     */
    public List<String> getAllThemesByUser(String user) {
        List<String> themes = db.questionDao().getAllThemesByUser(user);
        return (themes != null) ? themes : new ArrayList<>();
    }

    /**
     * Récupère toutes les questions associées à un thème donné.
     *
     * @param theme thème des questions à récupérer
     * @return liste des questions correspondantes
     */
    public List<Question> getQuestionsByTheme(String theme) {
        return db.questionDao().getQuestionsByTheme(theme);
    }

    /**
     * Récupère toutes les questions créées par un utilisateur.
     *
     * @param pseudo pseudo du créateur des questions
     * @return liste des questions associées à cet utilisateur
     */
    public List<Question> getQuestions(String pseudo) {
        return db.questionDao().getAllByUser(pseudo);
    }

    /**
     * Supprime toutes les questions appartenant à un utilisateur.
     *
     * @param pseudo pseudo de l'utilisateur
     */
    public void deleteAll(String pseudo) {
        db.questionDao().deleteAllByUser(pseudo);
    }

    /**
     * Supprime toutes les questions d'un thème spécifique
     * appartenant à un utilisateur donné.
     *
     * @param theme thème à supprimer
     * @param pseudo pseudo de l'utilisateur
     */
    public void deleteTheme(String theme, String pseudo) {
        db.questionDao().deleteThemeByUser(theme, pseudo);
    }

    /**
     * Vérifie si un thème contient déjà des questions dans la base.
     *
     * Si aucune question n'existe pour ce thème, la méthode injecte
     * automatiquement une liste de questions système par défaut.
     * Cela permet de garantir que certains thèmes disposent toujours
     * de contenu initial.
     *
     * @param theme thème pour lequel vérifier et éventuellement
     * injecter des questions système
     */
    public void checkAndInjectSystemQuestions(String theme) {
        List<Question> existing = db.questionDao().getQuestionsByTheme(theme);
        if (existing.isEmpty()) {
            List<Question> list = new ArrayList<>();
            switch (theme) {
                case "TUTO":
                    list.add(new Question("Comment se déplacer ?", "Cliquer sur une case", "Secouer", "Attendre", "Sauter", 1, "TUTO", "SYSTEM"));
                    list.add(new Question("Fin du chrono ?", "On perd", "Réponse validée", "Rien", "Pause", 2, "TUTO", "SYSTEM"));
                    break;
                case "COULEURS":
                    list.add(new Question("Bleu + Jaune = ?", "Orange", "Vert", "Violet", "Gris", 2, "COULEURS", "SYSTEM"));
                    list.add(new Question("Couleur du ciel ?", "Rouge", "Bleu", "Noir", "Vert", 2, "COULEURS", "SYSTEM"));
                    break;
                case "CHIFFRES":
                    list.add(new Question("2 + 2 x 2 = ?", "8", "6", "4", "0", 2, "CHIFFRES", "SYSTEM"));
                    list.add(new Question("Combien de côtés pour un triangle ?", "4", "2", "3", "5", 3, "CHIFFRES", "SYSTEM"));
                    break;
                case "FRANCAIS":
                    list.add(new Question("Synonyme de Joyeux ?", "Triste", "Heureux", "Colère", "Fatigué", 2, "FRANCAIS", "SYSTEM"));
                    list.add(new Question("Pluriel de Cheval ?", "Chevals", "Chevaux", "Cheval", "Chevau", 2, "FRANCAIS", "SYSTEM"));
                    break;
            }
            if (!list.isEmpty()) db.questionDao().insertAll(list);
        }
    }
}