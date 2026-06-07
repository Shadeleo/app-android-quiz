package com.example.projetmobilel3informatiqueleopereira.logic.game;

import com.example.projetmobilel3informatiqueleopereira.data.models.Question;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Moteur de gestion du mode Duel (jeu à deux joueurs).
 *
 * Cette classe gère la progression d'une partie en face-à-face
 * sur un même appareil : ordre des questions, validation des
 * réponses et calcul des scores pour chaque joueur.
 */
public class DuelGameManager {
    private final List<Question> questions;
    private int currentIndex = 0;
    private int scoreJ1 = 0;
    private int scoreJ2 = 0;

    /**
     * Initialise une nouvelle partie Duel.
     *
     * Les questions sont copiées puis mélangées afin de garantir
     * un ordre aléatoire à chaque nouvelle partie.
     *
     * @param questions liste des questions utilisées pour la partie
     */
    public DuelGameManager(List<Question> questions) {
        this.questions = new ArrayList<>(questions);
        Collections.shuffle(this.questions);
    }

    /**
     * Retourne la question actuellement posée aux joueurs.
     *
     * Si toutes les questions ont été traitées, la méthode
     * retourne null pour indiquer la fin de la partie.
     *
     * @return question en cours ou null si la partie est terminée
     */
    public Question getCurrentQuestion() {
        if (isGameOver()) return null;
        return questions.get(currentIndex);
    }

    /**
     * Enregistre les réponses des deux joueurs et met à jour les scores.
     *
     * Chaque réponse est comparée à la bonne réponse de la question
     * actuelle. Si la réponse est correcte, le score du joueur est
     * incrémenté. La partie passe ensuite à la question suivante.
     *
     * @param j1Answer réponse choisie par le joueur 1 (index 1 à 4)
     * @param j2Answer réponse choisie par le joueur 2 (index 1 à 4)
     */
    public void submitAnswers(int j1Answer, int j2Answer) {
        if (isGameOver()) return;

        int correct = questions.get(currentIndex).correctAnswer;

        if (j1Answer == correct) scoreJ1++;
        if (j2Answer == correct) scoreJ2++;

        currentIndex++;
    }

    /**
     * Vérifie si toutes les questions ont été posées.
     *
     * @return true si la partie est terminée
     */
    public boolean isGameOver() {
        return currentIndex >= questions.size();
    }

    /**
     * Retourne le score actuel du joueur 1.
     *
     * @return nombre de réponses correctes du joueur 1
     */
    public int getScoreJ1() { return scoreJ1; }

    /**
     * Retourne le score actuel du joueur 2.
     *
     * @return nombre de réponses correctes du joueur 2
     */
    public int getScoreJ2() { return scoreJ2; }
}