package com.example.projetmobilel3informatiqueleopereira.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.projetmobilel3informatiqueleopereira.data.models.Score;

import java.util.List;

/**
 * Fournit l'accès au DAO associé aux scores.
 *
 * Le DAO permet d'effectuer les opérations CRUD
 * sur les entités stockées dans la base de données.
 */
@Dao
public interface ScoreDao {
    @Insert
    void insertScore(Score score);
    @Query("SELECT * FROM scores WHERE owner = :pseudo ORDER BY timestamp ASC")
    List<Score> getScoresByUser(String pseudo);
    @Query("SELECT * FROM scores WHERE owner = :pseudo AND theme = :nomTheme ORDER BY timestamp ASC")
    List<Score> getScoresByThemeAndUser(String pseudo, String nomTheme);
}