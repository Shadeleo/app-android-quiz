package com.example.projetmobilel3informatiqueleopereira.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.projetmobilel3informatiqueleopereira.data.models.User;

/**
 * Fournit l'accès au DAO associé aux users.
 *
 * Le DAO permet d'effectuer les opérations CRUD
 * sur les entités stockées dans la base de données.
 */
@Dao
public interface UserDao {
    @Insert
    void insertUser(User user);
    @Query("SELECT * FROM users WHERE pseudo = :pseudo AND password = :password LIMIT 1")
    User login(String pseudo, String password);
    @Query("SELECT * FROM users WHERE pseudo = :pseudo LIMIT 1")
    User checkUserExists(String pseudo);
}