package com.example.projetmobilel3informatiqueleopereira.data.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entité représentant le score d'une partie enregistrée
 * dans la base de données Room.
 *
 * Chaque entrée de la table "scores" correspond au résultat
 * d'un joueur pour un thème donné, avec son pourcentage de
 * réussite et la date à laquelle la partie a été jouée.
 */
@Entity(tableName = "scores")
public class Score {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String owner;
    public String theme;
    public float pourcentage;
    public long timestamp;
}
