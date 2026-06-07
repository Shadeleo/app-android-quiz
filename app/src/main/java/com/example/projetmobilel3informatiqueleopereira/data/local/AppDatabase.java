package com.example.projetmobilel3informatiqueleopereira.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.projetmobilel3informatiqueleopereira.data.models.Question;
import com.example.projetmobilel3informatiqueleopereira.data.models.Score;
import com.example.projetmobilel3informatiqueleopereira.data.models.User;

/**
 * Base de données locale de l'application utilisant Room.
 *
 * Cette classe définit l'accès centralisé à la base SQLite
 * et expose les DAO permettant de manipuler les entités
 * persistées (questions, scores, etc.).
 *
 * Elle implémente le pattern Singleton afin de garantir
 * une unique instance de base de données dans l'application.
 */
@Database(entities = {User.class, Question.class, Score.class}, version = 11, exportSchema = false) // Augmente à 11
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase instance;
    public abstract UserDao userDao();
    public abstract QuestionDao questionDao();
    public abstract ScoreDao scoreDao();

    /**
     * Retourne l'instance unique de la base de données.
     *
     * Si l'instance n'existe pas encore, elle est créée
     * via Room.databaseBuilder. Cette méthode garantit
     * qu'une seule base est utilisée pendant l'exécution
     * de l'application.
     */
    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "database-qcm")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}