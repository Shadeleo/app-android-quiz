package com.example.projetmobilel3informatiqueleopereira.logic.quiz;

import com.example.projetmobilel3informatiqueleopereira.data.models.Question;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.Collections;
import java.util.List;

/**
 * Gestionnaire de la logique métier liée aux quiz.
 *
 * Cette classe gère la validation des thèmes, l'analyse des réponses
 * JSON générées par l'IA, ainsi que la sauvegarde des questions via
 * le repository. Elle sert d'intermédiaire entre l'interface utilisateur
 * et la couche de données.
 */
public class QuizManager {
    private final QuizRepository repository;
    private final String pseudo;

    /**
     * Interface de communication avec l'interface utilisateur.
     *
     * Permet au QuizManager d'envoyer des messages ou retours
     * d'état vers la couche UI sans dépendance directe.
     */
    public interface QuizActionDelegate {
        void showMessage(String msg);
    }

    /**
     * Initialise le gestionnaire de quiz.
     *
     * @param repository repository responsable de la persistance des questions
     * @param pseudo pseudo de l'utilisateur créateur des quiz
     */
    public QuizManager(QuizRepository repository, String pseudo) {
        this.repository = repository;
        this.pseudo = pseudo;
    }

    /**
     * Vérifie si le nom du thème est valide.
     *
     * Le thème doit contenir uniquement des lettres ou des espaces
     * et ne pas dépasser 16 caractères.
     *
     * @param theme nom du thème à valider
     * @return true si le thème respecte les règles de validation
     */
    public boolean isThemeValid(String theme) {
        if (theme == null || theme.trim().isEmpty()) return false;
        return theme.matches("^[a-zA-Z\\s]{1,16}$");
    }

    /**
     * Analyse le JSON généré par l'IA et sauvegarde les questions.
     *
     * La méthode extrait le JSON brut, le convertit en liste de
     * questions puis valide leur contenu avant de les enregistrer
     * via le repository.
     *
     * @param rawJson réponse brute générée par l'IA
     * @param theme thème du quiz
     * @param delegate interface permettant d'afficher les messages à l'utilisateur
     */
    public void processJsonAndSave(String rawJson, String theme, QuizActionDelegate delegate) {
        if (!isThemeValid(theme)) {
            delegate.showMessage("Erreur : Titre invalide (Max 16 lettres).");
            return;
        }

        try {
            String cleanJson = extractJsonContent(rawJson);

            List<Question> list = new Gson().fromJson(
                    cleanJson,
                    new TypeToken<List<Question>>(){}.getType()
            );

            if (list != null && !list.isEmpty()) {
                if (validateQuestionList(list)) {
                    repository.saveQuestions(list, theme, pseudo, () ->
                            delegate.showMessage("Quiz '" + theme + "' ajouté avec succès !")
                    );
                } else {
                    delegate.showMessage("Certaines questions du JSON sont incomplètes.");
                }
            } else {
                delegate.showMessage("Le JSON reçu est vide.");
            }
        } catch (Exception e) {
            delegate.showMessage("Erreur de formatage : L'IA n'a pas renvoyé un JSON valide.");
        }
    }

    /**
     * Nettoie la réponse brute de l'IA pour extraire uniquement le JSON.
     *
     * Supprime les balises Markdown souvent ajoutées autour
     * du contenu JSON (ex : ```json ... ```).
     *
     * @param raw texte brut retourné par l'IA
     * @return chaîne JSON nettoyée
     */
    private String extractJsonContent(String raw) {
        String result = raw.trim();

        if (result.contains("```")) {
            result = result.replaceAll("(?s).*?```(?:json)?\\s*(.*?)\\s*```.*", "$1");
        }

        return result.trim();
    }

    /**
     * Vérifie la validité minimale des questions importées.
     *
     * Chaque question doit contenir un énoncé, au moins une
     * proposition de réponse et un index de bonne réponse valide.
     *
     * @param list liste des questions à vérifier
     * @return true si toutes les questions sont valides
     */
    private boolean validateQuestionList(List<Question> list) {
        for (Question q : list) {
            if (q.question == null || q.choice1 == null || q.correctAnswer < 1 || q.correctAnswer > 4) {
                return false;
            }
        }
        return true;
    }

    /**
     * Sauvegarde une question créée manuellement.
     *
     * La question est associée au thème et à l'utilisateur
     * puis enregistrée dans la base via le repository.
     *
     * @param q question à enregistrer
     * @param theme thème associé à la question
     * @param delegate interface permettant d'envoyer un message à l'utilisateur
     */
    public void saveSingleQuestion(Question q, String theme, QuizActionDelegate delegate) {
        if (!isThemeValid(theme)) {
            delegate.showMessage("Erreur : Titre de thème invalide.");
            return;
        }

        repository.saveQuestions(Collections.singletonList(q), theme, pseudo, () ->
                delegate.showMessage("Question ajoutée au thème : " + theme)
        );
    }

    /**
     * Construit le prompt utilisé pour générer des questions via l'IA Gemini.
     *
     * Le prompt impose un format JSON strict afin de faciliter
     * l'analyse automatique et l'import des questions dans l'application.
     *
     * @param fileName nom du document source utilisé pour générer les questions
     * @return prompt formaté envoyé à l'API Gemini
     */
    public String buildPrompt(String fileName) {
        return "Agis comme un expert pédagogique. " +
                "Génère exactement 5 questions de QCM en français basées sur le document suivant : '" + fileName + "'. " +
                "Réponds UNIQUEMENT avec un tableau JSON. " +
                "Structure attendue : [{\"question\":\"...\", \"choice1\":\"...\", \"choice2\":\"...\", \"choice3\":\"...\", \"choice4\":\"...\", \"correctAnswer\":1}]. " +
                "Ne mets aucune explication avant ou après le JSON.";
    }
}