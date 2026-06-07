package com.example.projetmobilel3informatiqueleopereira.data.repositories;

import android.content.Context;
import com.example.projetmobilel3informatiqueleopereira.data.local.AppDatabase;
import com.example.projetmobilel3informatiqueleopereira.data.models.User;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository gérant les opérations sur les utilisateurs.
 * Centralise l'accès à la base de données Room.
 */
public class UserRepository {
    private final AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public UserRepository(Context context) {
        this.db = AppDatabase.getInstance(context.getApplicationContext());
    }

    /**
     * Tente de connecter un utilisateur.
     * Note : À appeler idéalement dans un thread séparé ou via un Callback.
     */
    public User login(String pseudo, String password) {
        return db.userDao().login(pseudo, password);
    }

    /**
     * Vérifie si un pseudo est déjà pris.
     */
    public User checkExists(String pseudo) {
        return db.userDao().checkUserExists(pseudo);
    }

    /**
     * Insère un nouvel utilisateur de manière asynchrone.
     */
    public void insert(User user) {
        executor.execute(() -> {
            db.userDao().insertUser(user);
        });
    }


}