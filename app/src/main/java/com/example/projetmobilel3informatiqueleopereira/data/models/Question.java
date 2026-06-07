package com.example.projetmobilel3informatiqueleopereira.data.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entité représentant une question stockée dans la base de données Room.
 *
 * Chaque instance correspond à une entrée de la table "questions".
 * Elle contient l'énoncé de la question, les quatre choix possibles,
 * l'index de la bonne réponse ainsi que des informations de contexte
 * comme le thème et le créateur de la question.
 */
@Entity(tableName = "questions")
public class Question {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String owner;
    public String question;
    public String choice1;
    public String choice2;
    public String choice3;
    public String choice4;
    public int correctAnswer;

    public String theme;

    /**
     * Constructeur vide requis par Room.
     *
     * Room utilise ce constructeur pour instancier l'objet
     * lors de la lecture des données depuis la base SQLite.
     */
    public Question() {}

    /**
     * Constructeur utilitaire permettant de créer rapidement
     * une question avec toutes ses informations.
     *
     * @param question énoncé de la question
     * @param c1 premier choix de réponse
     * @param c2 deuxième choix de réponse
     * @param c3 troisième choix de réponse
     * @param c4 quatrième choix de réponse
     * @param correct index de la réponse correcte (1 à 4)
     * @param theme thème ou catégorie de la question
     * @param owner pseudo du créateur de la question
     */
    public Question(String question, String c1, String c2, String c3, String c4, int correct, String theme, String owner) {
        this.question = question;
        this.choice1 = c1;
        this.choice2 = c2;
        this.choice3 = c3;
        this.choice4 = c4;
        this.correctAnswer = correct;
        this.theme = theme;
        this.owner = owner;
    }
}

