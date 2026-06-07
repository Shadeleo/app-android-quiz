package com.example.projetmobilel3informatiqueleopereira.ui.activities;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.projetmobilel3informatiqueleopereira.R;
import com.example.projetmobilel3informatiqueleopereira.databinding.ActivityMenuBinding;
import com.example.projetmobilel3informatiqueleopereira.logic.utils.PdfHelper;
import com.example.projetmobilel3informatiqueleopereira.logic.utils.SoundManager;
import com.example.projetmobilel3informatiqueleopereira.ui.viewmodels.MenuViewModel;

import java.util.Locale;

/**
 * Activité représentant le menu principal de l'application.
 *
 * Cette classe centralise la navigation vers les différents
 * écrans du projet, affiche les informations du profil utilisateur
 * et gère les fonctionnalités d'import de quiz via PDF ou JSON
 * généré par Gemini.
 */
public class MenuActivity extends AppCompatActivity {
    private ActivityMenuBinding binding;
    private MenuViewModel viewModel;
    private PdfHelper pdfHelper;
    private ActivityResultLauncher<Intent> filePickerLauncher;

    /**
     * Initialise l'activité et prépare le menu principal.
     *
     * Configure la langue, initialise le View Binding,
     * instancie le ViewModel et les utilitaires nécessaires,
     * puis met en place les listeners, observers et animations.
     *
     * @param savedInstanceState état précédent de l'activité si disponible
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyLocaleConfiguration();
        super.onCreate(savedInstanceState);

        binding = ActivityMenuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(MenuViewModel.class);
        pdfHelper = new PdfHelper(this);

        setupListeners();
        setupObservers();
        setupFilePicker();
        startAvatarFloatingAnimation();
    }

    /**
     * Configure les actions associées aux éléments du menu.
     *
     * Gère la navigation entre les différentes fonctionnalités,
     * l'ouverture des paramètres de langue ainsi que les actions
     * d'importation de quiz via PDF ou JSON.
     */
    private void setupListeners() {
        binding.cardProfile.setOnClickListener(v -> navigateWithCheck(ProfileActivity.class));
        binding.btnPlay.setOnClickListener(v -> navigateWithCheck(SelectionThemeActivity.class));
        binding.btnStory.setOnClickListener(v -> navigateWithCheck(StoryActivity.class));
        binding.btnDuel.setOnClickListener(v -> navigateWithCheck(DuelChoiceActivity.class));
        binding.btnDressing.setOnClickListener(v -> navigateWithCheck(AvatarActivity.class));
        binding.btnCreateQuiz.setOnClickListener(v -> navigateWithCheck(CreateQuizActivity.class));
        binding.btnLanguage.setOnClickListener(v -> showLanguageDialog());
        binding.btnAddPdf.setOnClickListener(v -> {
            SoundManager.playClick();
            if (isTutoCompleted()) openFilePicker();
            else navigateToStory();
        });
        binding.btnPasteGemini.setOnClickListener(v -> {
            SoundManager.playClick();
            if (isTutoCompleted()) showImportDialog();
            else navigateToStory();
        });
    }

    /**
     * Observe les états exposés par le ViewModel.
     *
     * Met à jour les informations du profil affiché,
     * gère le verrouillage des fonctionnalités selon
     * la progression du tutoriel et affiche les messages
     * utilisateur remontés par le ViewModel.
     */
    private void setupObservers() {
        viewModel.profileState.observe(this, state -> {
            if (state == null) return;

            binding.txtProfileName.setText(state.pseudo);
            binding.progressSuccess.setProgress(state.successRate);
            binding.txtProgressPercent.setText(state.successRate + "%");

            float fraction = state.successRate / 100f;
            int colorProgress = (Integer) new android.animation.ArgbEvaluator()
                    .evaluate(fraction, Color.RED, Color.GREEN);

            binding.progressSuccess.setProgressTintList(
                    android.content.res.ColorStateList.valueOf(colorProgress)
            );

            binding.imgAvatar.setImageResource(state.avatarRes);
            binding.imgAvatar.setColorFilter(state.avatarColor, PorterDuff.Mode.SRC_IN);
        });

        viewModel.isTutoCompleted.observe(this, completed -> {
            int visibility = (completed != null && completed) ? View.GONE : View.VISIBLE;

            binding.overlayProfile.setVisibility(visibility);
            binding.overlayGemini.setVisibility(visibility);
            binding.overlayPdf.setVisibility(visibility);
            binding.overlayCreate.setVisibility(visibility);
            binding.overlayBattle.setVisibility(visibility);
            binding.overlayAvatar.setVisibility(visibility);
            binding.lockCardProfile.setVisibility(visibility);
            binding.lockCardGemini.setVisibility(visibility);
            binding.lockCardPdf.setVisibility(visibility);
            binding.lockCardCreate.setVisibility(visibility);
            binding.lockCardBattle.setVisibility(visibility);
            binding.lockCardAvatar.setVisibility(visibility);
        });

        viewModel.toastMessageRes.observe(this, resId -> {
            if (resId != null) {
                Toast.makeText(this, getString(resId), Toast.LENGTH_SHORT).show();
                viewModel.onToastShown();
            }
        });
    }

    /**
     * Initialise le sélecteur de fichiers pour l'import PDF.
     *
     * Récupère l'URI du document choisi puis construit
     * le prompt à transmettre à Gemini à partir du nom du fichier.
     */
    private void setupFilePicker() {
        filePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                Uri uri = result.getData().getData();
                String prompt = viewModel.buildGeminiPrompt(pdfHelper.getFileName(uri));
                showGeminiRedirectDialog(prompt);
            }
        });
    }

    /**
     * Affiche une boîte de dialogue permettant d'importer un quiz au format JSON.
     *
     * L'utilisateur saisit un nom de thème et colle le contenu JSON
     * généré par l'IA avant de lancer l'import via le ViewModel.
     */
    private void showImportDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, padding);

        final EditText themeInput = new EditText(this);
        themeInput.setHint(R.string.dialog_theme_name);
        themeInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});

        final EditText jsonInput = new EditText(this);
        jsonInput.setHint("Collez ici le JSON");
        jsonInput.setMinLines(6);
        jsonInput.setGravity(Gravity.TOP | Gravity.START);
        jsonInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);

        layout.addView(themeInput);
        layout.addView(jsonInput);

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_theme_name)
                .setView(layout)
                .setPositiveButton(R.string.btn_import, (d, w) -> {
                    String theme = themeInput.getText().toString().trim();
                    String json = jsonInput.getText().toString().trim();

                    if (theme.isEmpty()) {
                        Toast.makeText(this, getString(R.string.import_theme_required), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!theme.matches("^[a-zA-Z0-9]{1,10}$")) {
                        Toast.makeText(this, "Le nom du thème doit contenir uniquement des lettres et des chiffres (max 10).", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (json.isEmpty()) {
                        Toast.makeText(this, "Veuillez coller un JSON avant d’importer.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    viewModel.importJson(json, theme);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /**
     * Affiche une boîte de dialogue de redirection vers Gemini.
     *
     * Le prompt généré est envoyé à l'application Gemini si elle est
     * disponible. En cas d'absence, le prompt est copié dans le presse-papiers
     * puis une tentative d'ouverture de Gemini dans le navigateur est effectuée.
     *
     * @param prompt texte à transmettre à Gemini
     */
    private void showGeminiRedirectDialog(String prompt) {
        new AlertDialog.Builder(this)
                .setTitle("Ouvrir Gemini")
                .setMessage("Le prompt va être envoyé à Gemini.")
                .setPositiveButton(R.string.btn_go_to_gemini, (d, w) -> {
                    if (!isInternetAvailable()) {
                        Toast.makeText(this, "Aucune connexion Internet détectée.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                        return;
                    }

                    Intent geminiShareIntent = new Intent(Intent.ACTION_SEND);
                    geminiShareIntent.setType("text/plain");
                    geminiShareIntent.putExtra(Intent.EXTRA_TEXT, prompt);
                    geminiShareIntent.setPackage("com.google.android.apps.bard");

                    try {
                        startActivity(geminiShareIntent);
                    } catch (ActivityNotFoundException e) {
                        ClipboardManager cb = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        if (cb != null) {
                            cb.setPrimaryClip(ClipData.newPlainText("Prompt", prompt));
                        }

                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://gemini.google.com/")));
                        } catch (ActivityNotFoundException ex) {
                            Toast.makeText(this, "Impossible d’ouvrir Gemini.", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /**
     * Lance une activité après vérification de l'état du tutoriel.
     *
     * Certaines fonctionnalités restent verrouillées tant que
     * le tutoriel n'est pas terminé. Le pseudo utilisateur est
     * également transmis à l'activité cible si disponible.
     *
     * @param cls classe de l'activité à ouvrir
     */
    private void navigateWithCheck(Class<?> cls) {
        SoundManager.playClick();
        if (!isTutoCompleted() && !cls.equals(StoryActivity.class)) {
            Toast.makeText(this, R.string.tuto_required, Toast.LENGTH_SHORT).show();
            navigateToStory();
            return;
        }

        Intent intent = new Intent(this, cls);
        if (viewModel.profileState.getValue() != null) {
            intent.putExtra("USER_PSEUDO", viewModel.profileState.getValue().pseudo);
        }
        startActivity(intent);
    }

    /**
     * Redirige l'utilisateur vers le mode Story.
     *
     * Utilisé notamment lorsque certaines fonctionnalités
     * nécessitent d'avoir terminé le tutoriel.
     */
    private void navigateToStory() {
        startActivity(new Intent(this, StoryActivity.class));
    }

    /**
     * Vérifie si le tutoriel a été complété.
     *
     * @return true si le tutoriel est terminé, sinon false
     */
    private boolean isTutoCompleted() {
        return Boolean.TRUE.equals(viewModel.isTutoCompleted.getValue());
    }

    /**
     * Affiche une boîte de dialogue de sélection de langue.
     *
     * La langue choisie est enregistrée dans les préférences
     * puis l'activité est recréée pour appliquer la nouvelle configuration.
     */
    private void showLanguageDialog() {
        String[] codes = {"fr", "en", "es", "zh"};
        String[] names = {
                getString(R.string.lang_fr), getString(R.string.lang_en),
                getString(R.string.lang_es), getString(R.string.lang_zh)
        };

        new AlertDialog.Builder(this).setItems(names, (d, w) -> {
            getSharedPreferences("Settings", MODE_PRIVATE).edit().putString("My_Lang", codes[w]).apply();
            recreate();
        }).show();
    }

    /**
     * Ouvre le sélecteur système pour choisir un fichier PDF.
     *
     * Le document sélectionné pourra ensuite être utilisé
     * pour générer un prompt destiné à Gemini.
     */
    private void openFilePicker() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT).setType("application/pdf");
        filePickerLauncher.launch(i);
    }

    /**
     * Lance une animation flottante en boucle sur l'avatar du menu.
     *
     * Cette animation améliore le rendu visuel du menu principal
     * en donnant un effet de mouvement léger à l'avatar.
     */
    private void startAvatarFloatingAnimation() {
        binding.imgAvatar.animate().translationY(40f).setDuration(2000)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    if (!isFinishing()) {
                        binding.imgAvatar.animate().translationY(0f).setDuration(2000)
                                .withEndAction(this::startAvatarFloatingAnimation).start();
                    }
                }).start();
    }

    /**
     * Applique la langue enregistrée dans les préférences utilisateur.
     *
     * Met à jour la configuration des ressources avant
     * l'affichage de l'interface.
     */
    private void applyLocaleConfiguration() {
        String lang = getSharedPreferences("Settings", MODE_PRIVATE).getString("My_Lang", "fr");
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    /**
     * Rafraîchit les données affichées au retour sur l'activité.
     *
     * Permet de recharger l'état du profil et de la progression
     * après un retour depuis un autre écran.
     */
    @Override
    protected void onResume() {
        super.onResume();
        viewModel.refreshState();
    }

    /**
     * Vérifie la disponibilité d'une connexion réseau active.
     *
     * La méthode prend en charge les API Android récentes
     * et anciennes afin de déterminer si l'appareil dispose
     * d'un accès Internet exploitable.
     *
     * @return true si une connexion réseau est disponible
     */
    private boolean isInternetAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        if (connectivityManager == null) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = connectivityManager.getActiveNetwork();
            if (network == null) {
                return false;
            }

            NetworkCapabilities capabilities =
                    connectivityManager.getNetworkCapabilities(network);

            return capabilities != null &&
                    (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                            || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                            || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
        } else {
            android.net.NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
    }
}