package com.example.projetmobilel3informatiqueleopereira.data.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entité représentant un utilisateur de l'application.
 *
 * Chaque instance correspond à un joueur enregistré dans
 * la base de données Room (table "users"). Elle contient
 * les informations d'authentification ainsi que les
 * statistiques globales du joueur.
 */
@Entity(tableName = "users")
public class User {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String pseudo;
    public String password;
    public float averageScore;
    public int gamesPlayed;

    /**
     * Constructeur permettant de créer un nouvel utilisateur.
     *
     * Initialise le pseudo et le mot de passe du joueur.
     * Les statistiques de jeu sont initialisées à zéro
     * lors de la création du compte.
     *
     * @param pseudo pseudo du joueur
     * @param password mot de passe associé au compte
     */
    public User(String pseudo, String password) {
        this.pseudo = pseudo;
        this.password = password;
        this.averageScore = 0.0f;
        this.gamesPlayed = 0;
    }
}