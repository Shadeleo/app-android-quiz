package com.example.projetmobilel3informatiqueleopereira.ui.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.projetmobilel3informatiqueleopereira.logic.avatar.AvatarManager;

import java.util.List;

/**
 * ViewModel responsable de la gestion de la personnalisation de l'avatar.
 *
 * Cette classe centralise l'état de l'avatar (forme et couleur sélectionnées)
 * ainsi que la liste des éléments débloqués. Elle communique avec
 * l'AvatarManager pour récupérer et sauvegarder les préférences utilisateur.
 */
public class AvatarViewModel extends AndroidViewModel {
    private final AvatarManager avatarManager;
    private final MutableLiveData<Integer> _selectedShape = new MutableLiveData<>();
    private final MutableLiveData<Integer> _selectedColor = new MutableLiveData<>();
    private final MutableLiveData<List<Integer>> _unlockedShapes = new MutableLiveData<>();
    private final MutableLiveData<List<Integer>> _unlockedColors = new MutableLiveData<>();
    public final LiveData<Integer> selectedShape = _selectedShape;
    public final LiveData<Integer> selectedColor = _selectedColor;
    public final LiveData<List<Integer>> unlockedShapes = _unlockedShapes;
    public final LiveData<List<Integer>> unlockedColors = _unlockedColors;

    /**
     * Initialise le ViewModel et le gestionnaire d'avatar.
     *
     * Charge les données sauvegardées (forme, couleur et éléments
     * débloqués) afin de synchroniser l'interface avec l'état actuel
     * de l'utilisateur.
     *
     * @param application instance de l'application
     */
    public AvatarViewModel(@NonNull Application application) {
        super(application);
        this.avatarManager = new AvatarManager(application);
        loadAvatarData();
    }

    /**
     * Charge les informations de personnalisation de l'avatar.
     *
     * Récupère les préférences sauvegardées et les éléments
     * débloqués via l'AvatarManager puis met à jour les LiveData
     * observées par l'interface.
     */
    private void loadAvatarData() {
        _selectedShape.setValue(avatarManager.getCurrentShape());
        _selectedColor.setValue(avatarManager.getCurrentColor());
        _unlockedShapes.setValue(avatarManager.getUnlockedShapes());
        _unlockedColors.setValue(avatarManager.getUnlockedColors());
    }

    /**
     * Met à jour la forme sélectionnée par l'utilisateur.
     *
     * La valeur est publiée via LiveData afin de déclencher
     * la mise à jour de l'interface.
     *
     * @param shapeRes identifiant de la ressource Drawable sélectionnée
     */
    public void selectShape(int shapeRes) {
        if (_selectedShape.getValue() == null || _selectedShape.getValue() != shapeRes) {
            _selectedShape.setValue(shapeRes);
        }
    }

    /**
     * Met à jour la couleur sélectionnée pour l'avatar.
     *
     * La modification est propagée aux observateurs afin
     * d'actualiser l'aperçu visuel.
     *
     * @param colorInt valeur entière représentant la couleur
     */
    public void selectColor(int colorInt) {
        if (_selectedColor.getValue() == null || _selectedColor.getValue() != colorInt) {
            _selectedColor.setValue(colorInt);
        }
    }

    /**
     * Sauvegarde les paramètres actuels de l'avatar.
     *
     * Enregistre la forme et la couleur sélectionnées
     * via l'AvatarManager afin de persister la configuration
     * dans les préférences utilisateur.
     */
    public void saveAvatar() {
        Integer shape = _selectedShape.getValue();
        Integer color = _selectedColor.getValue();

        if (shape != null && color != null) {
            avatarManager.saveAvatar(shape, color);
        }
    }
}