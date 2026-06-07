package com.example.projetmobilel3informatiqueleopereira.data.local;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.projetmobilel3informatiqueleopereira.data.models.Question;

import java.util.List;

/**
 * Fournit l'accès au DAO associé aux questions.
 *
 * Le DAO permet d'effectuer les opérations CRUD
 * sur les entités stockées dans la base de données.
 */
@Dao
public interface QuestionDao {
    @Query("SELECT * FROM questions WHERE theme = :nomTheme")
    List<Question> getQuestionsByTheme(String nomTheme);
    @Insert
    void insertAll(List<Question> questions);
    @Query("SELECT * FROM questions WHERE owner = :userPseudo")
    List<Question> getAllByUser(String userPseudo);
    @Query("SELECT DISTINCT theme FROM questions WHERE owner = :userPseudo")
    List<String> getAllThemesByUser(String userPseudo);
    @Query("SELECT * FROM questions WHERE theme = :nomTheme AND owner = :userPseudo")
    List<Question> getQuestionsByThemeAndUser(String nomTheme, String userPseudo);
    @Query("DELETE FROM questions WHERE theme = :nomTheme AND owner = :userPseudo")
    void deleteThemeByUser(String nomTheme, String userPseudo);
    @Query("DELETE FROM questions WHERE owner = :pseudo")
    void deleteAllByUser(String pseudo);
}