package com.example.projetmobilel3informatiqueleopereira.ui.viewmodels.factory;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.example.projetmobilel3informatiqueleopereira.ui.viewmodels.CreateQuizViewModel;

/**
 * Factory personnalisée utilisée pour créer des instances de CreateQuizViewModel.
 *
 * Cette classe permet d'injecter des paramètres supplémentaires dans le
 * constructeur du ViewModel, ce qui n'est pas possible avec le
 * ViewModelProvider standard.
 */
public class QuizViewModelFactory implements ViewModelProvider.Factory {
    private final Application application;
    private final String pseudo;

    /**
     * Initialise la factory de ViewModel.
     *
     * @param application instance de l'application utilisée par le ViewModel
     * @param pseudo pseudo de l'utilisateur à injecter dans le ViewModel
     */
    public QuizViewModelFactory(Application application, String pseudo) {
        this.application = application;
        this.pseudo = pseudo;
    }

    /**
     * Crée une instance du ViewModel demandé.
     *
     * Vérifie que la classe demandée correspond à CreateQuizViewModel
     * puis instancie le ViewModel en lui transmettant les paramètres
     * nécessaires.
     *
     * @param modelClass classe du ViewModel à créer
     * @param <T> type générique du ViewModel
     * @return instance du ViewModel configurée
     * @throws IllegalArgumentException si la classe demandée n'est pas supportée
     */
    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(CreateQuizViewModel.class)) {
            return (T) new CreateQuizViewModel(application, pseudo);
        }

        throw new IllegalArgumentException(
                String.format("La classe %s n'est pas supportée par QuizViewModelFactory", modelClass.getName())
        );
    }
}