package com.example.projetmobilel3informatiqueleopereira.ui.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.ArrayRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.projetmobilel3informatiqueleopereira.R;
import com.example.projetmobilel3informatiqueleopereira.databinding.ActivityAvatarBinding;
import com.example.projetmobilel3informatiqueleopereira.logic.utils.SoundManager;
import com.example.projetmobilel3informatiqueleopereira.ui.viewmodels.AvatarViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Activité dédiée à la personnalisation de l'avatar utilisateur.
 *
 * Cette classe gère l'affichage des formes et couleurs disponibles,
 * l'aperçu en temps réel de l'avatar et la sauvegarde de la sélection.
 * Elle s'appuie sur l'architecture MVVM avec un AvatarViewModel
 * et utilise le View Binding pour sécuriser l'accès aux vues.
 */
public class AvatarActivity extends AppCompatActivity {
    private AvatarViewModel viewModel;
    private ActivityAvatarBinding binding;

    /**
     * Initialise l'activité et prépare l'interface utilisateur.
     *
     * Configure la langue, initialise le View Binding, instancie
     * le ViewModel puis met en place l'interface et les observers.
     *
     * @param savedInstanceState état précédent de l'activité si disponible
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        initLanguageConfiguration();

        super.onCreate(savedInstanceState);

        binding = ActivityAvatarBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(AvatarViewModel.class);

        setupUI();
        setupObservers();
    }

    /**
     * Configure les composants interactifs de l'interface.
     *
     * Associe les écouteurs aux listes de formes et de couleurs
     * ainsi qu'au bouton de sauvegarde de l'avatar.
     */
    private void setupUI() {
        binding.listShapes.setOnItemClickListener((parent, view, position, id) -> {
            handleSelection(viewModel.unlockedShapes.getValue(), position, true);
        });

        binding.listColors.setOnItemClickListener((parent, view, position, id) -> {
            handleSelection(viewModel.unlockedColors.getValue(), position, false);
        });

        binding.btnSaveAvatar.setOnClickListener(v -> {
            SoundManager.playClick();
            viewModel.saveAvatar();
            Toast.makeText(this, R.string.avatar_saved_success, Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    /**
     * Initialise les observations des données exposées par le ViewModel.
     *
     * Met à jour dynamiquement les listes disponibles et l'aperçu
     * de l'avatar lorsque les données changent.
     */
    private void setupObservers() {
        viewModel.unlockedShapes.observe(this, items ->
                populateList(binding.listShapes, items, R.array.shape_names, "Style "));

        viewModel.unlockedColors.observe(this, items ->
                populateList(binding.listColors, items, R.array.color_names, "Color "));

        viewModel.selectedShape.observe(this, shape -> updatePreview());
        viewModel.selectedColor.observe(this, color -> updatePreview());
    }

    /**
     * Remplit une ListView avec les libellés correspondant aux éléments disponibles.
     *
     * Les noms affichés sont récupérés depuis les ressources traduites
     * de l'application. Un libellé par défaut est utilisé si aucune
     * traduction n'est disponible pour un élément.
     *
     * @param listView liste à alimenter
     * @param items liste des identifiants des éléments disponibles
     * @param arrayResId ressource contenant les libellés traduits
     * @param fallback préfixe utilisé si aucun libellé n'est trouvé
     */
    private void populateList(@NonNull ListView listView, List<Integer> items, @ArrayRes int arrayResId, String fallback) {
        if (items == null) return;

        String[] translatedNames = getResources().getStringArray(arrayResId);
        List<String> displayNames = new ArrayList<>();

        for (int i = 0; i < items.size(); i++) {
            String label = (i < translatedNames.length) ? translatedNames[i] : fallback + (i + 1);
            displayNames.add(label);
        }

        listView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displayNames));
    }

    /**
     * Gère la sélection d'un élément dans la liste des formes ou des couleurs.
     *
     * Met à jour la sélection courante dans le ViewModel puis déclenche
     * une animation de rafraîchissement de l'aperçu.
     *
     * @param list liste des valeurs disponibles
     * @param position position de l'élément sélectionné
     * @param isShape indique si la sélection concerne une forme ou une couleur
     */
    private void handleSelection(List<Integer> list, int position, boolean isShape) {
        if (list != null && position < list.size()) {
            if (isShape) viewModel.selectShape(list.get(position));
            else viewModel.selectColor(list.get(position));

            animatePreviewChange();
        }
    }

    /**
     * Met à jour l'aperçu visuel de l'avatar.
     *
     * Applique la forme sélectionnée et la couleur courante
     * sur l'image d'aperçu affichée dans l'interface.
     */
    private void updatePreview() {
        Integer shape = viewModel.selectedShape.getValue();
        Integer color = viewModel.selectedColor.getValue();

        if (shape != null) {
            binding.idImgPreview.setImageResource(shape);
        }
        if (color != null) {
            binding.idImgPreview.setColorFilter(color);
        }
    }

    /**
     * Animation de retour visuel lors d'un changement de style.
     */
    private void animatePreviewChange() {
        binding.idImgPreview.setScaleX(0.8f);
        binding.idImgPreview.setScaleY(0.8f);
        binding.idImgPreview.animate()
                .scaleX(1.0f)
                .scaleY(1.0f)
                .setDuration(200)
                .start();
    }

    /**
     * Charge et applique la langue enregistrée dans les préférences.
     *
     * Cette configuration est effectuée avant l'initialisation
     * des vues afin d'afficher l'interface dans la langue choisie.
     */
    private void initLanguageConfiguration() {
        String lang = getSharedPreferences("Settings", MODE_PRIVATE).getString("My_Lang", "fr");
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);

        android.content.res.Configuration config = new android.content.res.Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }
}