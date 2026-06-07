package com.example.projetmobilel3informatiqueleopereira.ui.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.projetmobilel3informatiqueleopereira.MainActivity;
import com.example.projetmobilel3informatiqueleopereira.R;
import com.example.projetmobilel3informatiqueleopereira.data.models.Score;
import com.example.projetmobilel3informatiqueleopereira.databinding.ActivityProfileBinding;
import com.example.projetmobilel3informatiqueleopereira.ui.viewmodels.ProfileViewModel;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Activité affichant le profil et les statistiques du joueur.
 *
 * Cette classe permet de visualiser l'évolution des scores
 * de l'utilisateur via un graphique, d'appliquer des filtres
 * par thème et période, et d'accéder aux QCM créés ainsi
 * qu'à la déconnexion.
 */
public class ProfileActivity extends AppCompatActivity {
    private ActivityProfileBinding binding;
    private ProfileViewModel viewModel;
    private String currentUser;

    /**
     * Initialise l'activité et prépare l'écran de profil.
     *
     * Configure la langue, initialise le View Binding,
     * récupère l'utilisateur courant, instancie le ViewModel
     * puis met en place l'interface, les observers et les listeners.
     *
     * @param savedInstanceState état précédent de l'activité si disponible
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyLocaleConfiguration();
        super.onCreate(savedInstanceState);

        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        currentUser = getIntent().getStringExtra("USER_PSEUDO");
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        initializeUI();
        setupObservers();
        setupListeners();

        viewModel.loadThemes(currentUser);
    }

    /**
     * Configure les composants visuels initiaux de l'écran.
     *
     * Initialise le graphique avec un message d'attente
     * et prépare le spinner de sélection de période
     * à partir des ressources localisées.
     */
    private void initializeUI() {
        binding.lineChart.setNoDataText(getString(R.string.loading_question));
        binding.lineChart.setNoDataTextColor(Color.WHITE);

        String[] periodes = getResources().getStringArray(R.array.periodes_array);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, periodes);
        binding.spinnerPeriode.setAdapter(adapter);
    }

    /**
     * Observe les données exposées par le ViewModel.
     *
     * Met à jour la liste des thèmes disponibles et
     * rafraîchit le graphique dès que les scores filtrés
     * changent.
     */
    private void setupObservers() {
        viewModel.themes.observe(this, themesReels -> {
            List<String> listeSpinner = new ArrayList<>();
            listeSpinner.add(getString(R.string.all_themes));
            if (themesReels != null) listeSpinner.addAll(themesReels);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_dropdown_item, listeSpinner);
            binding.spinnerTheme.setAdapter(adapter);
        });

        viewModel.filteredScores.observe(this, scores -> {
            if (scores == null || scores.isEmpty()) {
                showEmptyChart();
            } else {
                renderChartData(scores, binding.spinnerPeriode.getSelectedItemPosition());
            }
        });
    }

    /**
     * Configure les interactions utilisateur de l'écran.
     *
     * Gère les changements de filtres, l'ouverture de la liste
     * des QCM créés et la déconnexion de l'utilisateur.
     */
    private void setupListeners() {
        AdapterView.OnItemSelectedListener filterListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                triggerDataReload();
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        };

        binding.spinnerPeriode.setOnItemSelectedListener(filterListener);
        binding.spinnerTheme.setOnItemSelectedListener(filterListener);

        binding.btnShowAllQcm.setOnClickListener(v -> {
            Intent intent = new Intent(this, ListQcmActivity.class);
            intent.putExtra("USER_PSEUDO", currentUser);
            startActivity(intent);
        });

        binding.btnLogout.setOnClickListener(v -> performLogout());
    }

    /**
     * Déclenche le rechargement des données selon les filtres actifs.
     *
     * Transmet au ViewModel le thème sélectionné, la période choisie
     * et la valeur correspondant à l'option "tous les thèmes".
     */
    private void triggerDataReload() {
        if (binding.spinnerTheme.getSelectedItem() != null) {
            viewModel.loadAndFilterData(
                    currentUser,
                    binding.spinnerTheme.getSelectedItem().toString(),
                    binding.spinnerPeriode.getSelectedItemPosition(),
                    getString(R.string.all_themes)
            );
        }
    }

    /**
     * Construit et affiche les données du graphique de scores.
     *
     * Transforme la liste des scores en entrées exploitables
     * par MPAndroidChart puis configure les axes et l'affichage
     * du graphique.
     *
     * @param scores liste des scores à afficher
     * @param periodeIndex index de la période sélectionnée
     */
    private void renderChartData(List<Score> scores, int periodeIndex) {
        ArrayList<Entry> entries = new ArrayList<>();
        final SimpleDateFormat sdf = getDateFormatForPeriode(periodeIndex);

        for (int i = 0; i < scores.size(); i++) {
            entries.add(new Entry(i, scores.get(i).pourcentage));
        }

        LineDataSet dataSet = new LineDataSet(entries, getString(R.string.chart_label, currentUser));
        applyDataSetStyle(dataSet);

        XAxis xAxis = binding.lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                return (index >= 0 && index < scores.size())
                        ? sdf.format(new Date(scores.get(index).timestamp)) : "";
            }
        });

        YAxis leftAxis = binding.lineChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(100f);
        leftAxis.setTextColor(Color.WHITE);

        binding.lineChart.getAxisRight().setEnabled(false);
        binding.lineChart.getLegend().setTextColor(Color.WHITE);
        binding.lineChart.setData(new LineData(dataSet));
        binding.lineChart.getDescription().setEnabled(false);
        binding.lineChart.animateX(800);
        binding.lineChart.invalidate();
    }

    /**
     * Applique le style visuel au jeu de données du graphique.
     *
     * Configure les couleurs, l'épaisseur de ligne,
     * les points, le remplissage et le rendu des valeurs.
     *
     * @param dataSet jeu de données à styliser
     */
    private void applyDataSetStyle(LineDataSet dataSet) {
        dataSet.setColor(Color.parseColor("#4CAF50"));
        dataSet.setCircleColor(Color.parseColor("#2E7D32"));
        dataSet.setLineWidth(3f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#1976D2"));
        dataSet.setFillAlpha(60);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(10f);
    }

    /**
     * Affiche l'état vide du graphique.
     *
     * Nettoie les données actuelles et remplace le contenu
     * par un message indiquant l'absence de résultats.
     */
    private void showEmptyChart() {
        binding.lineChart.clear();
        binding.lineChart.setNoDataText(getString(R.string.no_data_chart));
        binding.lineChart.invalidate();
    }

    /**
     * Déconnecte l'utilisateur et réinitialise la session.
     *
     * Supprime les données de session enregistrées puis
     * redirige vers l'écran principal en vidant la pile
     * d'activités.
     */
    private void performLogout() {
        getSharedPreferences("USER_SESSION", MODE_PRIVATE).edit().clear().apply();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Retourne le format de date adapté à la période sélectionnée.
     *
     * Le format d'affichage varie selon le niveau de détail
     * attendu sur l'axe temporel du graphique.
     *
     * @param index index de la période choisie
     * @return formatteur de date correspondant
     */
    private SimpleDateFormat getDateFormatForPeriode(int index) {
        Locale currentLocale = Locale.getDefault();
        switch (index) {
            case 1: return new SimpleDateFormat("HH:mm", currentLocale);
            case 2: return new SimpleDateFormat("dd MMM", currentLocale);
            default: return new SimpleDateFormat("dd/MM/yy", currentLocale);
        }
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
}