package com.example.projetmobilel3informatiqueleopereira.ui.adaptater;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projetmobilel3informatiqueleopereira.R;
import com.example.projetmobilel3informatiqueleopereira.data.models.Question;

import java.util.List;
import java.util.Map;

/**
 * Adaptateur RecyclerView chargé d'afficher les thèmes et leurs questions associées.
 *
 * Cette classe organise l'affichage des groupes de questions par thème,
 * gère l'ajout dynamique des vues enfants et délègue les actions
 * utilisateur, comme la suppression d'un thème, à la couche appelante.
 */
public class ThemeAdapter extends RecyclerView.Adapter<ThemeAdapter.ThemeViewHolder> {
    private final List<String> themes;
    private final Map<String, List<Question>> data;
    private final OnThemeAction listener;

    /**
     * Interface de callback utilisée pour déléguer les actions utilisateur.
     *
     * Permet notamment de notifier l'écran appelant lorsqu'un thème
     * doit être supprimé.
     */
    public interface OnThemeAction {
        void onDeleteTheme(String themeName);
    }

    /**
     * Initialise l'adaptateur des thèmes.
     *
     * @param themes liste des thèmes à afficher
     * @param data association entre chaque thème et ses questions
     * @param listener callback recevant les actions sur un thème
     */
    public ThemeAdapter(List<String> themes, Map<String, List<Question>> data, OnThemeAction listener) {
        this.themes = themes;
        this.data = data;
        this.listener = listener;
    }

    /**
     * Crée un nouveau ViewHolder pour un thème.
     *
     * Inflate le layout correspondant à un groupe de questions
     * puis encapsule ses vues dans un ThemeViewHolder.
     *
     * @param parent conteneur parent du RecyclerView
     * @param viewType type de vue demandé
     * @return nouveau ViewHolder prêt à être utilisé
     */
    @NonNull
    @Override
    public ThemeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_theme_group, parent, false);
        return new ThemeViewHolder(v);
    }

    /**
     * Associe les données d'un thème à son ViewHolder.
     *
     * Met à jour le titre du thème, reconstruit dynamiquement
     * la liste des questions affichées et configure l'action
     * de suppression.
     *
     * @param holder ViewHolder à alimenter
     * @param position position de l'élément dans la liste
     */
    @Override
    public void onBindViewHolder(@NonNull ThemeViewHolder holder, int position) {
        String theme = themes.get(position);
        List<Question> questions = data.get(theme);

        holder.tvTitle.setText(theme.toUpperCase());
        holder.container.removeAllViews();

        if (questions != null && !questions.isEmpty()) {
            populateQuestions(holder, questions);
        }

        holder.btnDelete.setOnClickListener(v -> listener.onDeleteTheme(theme));
    }

    /**
     * Ajoute dynamiquement les questions d'un thème dans le conteneur visuel.
     *
     * Pour chaque question, une vue dédiée est générée puis remplie
     * avec l'énoncé et la bonne réponse associée.
     *
     * @param holder ViewHolder contenant le conteneur à remplir
     * @param questions liste des questions du thème
     */
    private void populateQuestions(ThemeViewHolder holder, List<Question> questions) {
        Context context = holder.itemView.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        for (Question q : questions) {
            View qView = inflater.inflate(android.R.layout.simple_list_item_2, holder.container, false);

            TextView t1 = qView.findViewById(android.R.id.text1);
            TextView t2 = qView.findViewById(android.R.id.text2);

            t1.setText(String.format("❓ %s", q.question));
            t1.setTextColor(Color.WHITE);
            t1.setPadding(0, 10, 0, 0);

            String correctText = getCorrectAnswerText(q);
            t2.setText(String.format("✔ %s", correctText));
            t2.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
            t2.setPadding(0, 0, 0, 10);

            holder.container.addView(qView);
        }
    }

    /**
     * Retourne le texte correspondant à la bonne réponse d'une question.
     *
     * La méthode utilise l'index de réponse correcte pour sélectionner
     * le champ approprié parmi les quatre propositions.
     *
     * @param q question à analyser
     * @return texte de la bonne réponse ou valeur par défaut si invalide
     */
    private String getCorrectAnswerText(Question q) {
        if (q == null) return "";
        switch (q.correctAnswer) {
            case 1: return q.choice1;
            case 2: return q.choice2;
            case 3: return q.choice3;
            case 4: return q.choice4;
            default: return "Inconnue";
        }
    }

    /**
     * Retourne le nombre total de thèmes affichés par l'adaptateur.
     *
     * @return nombre d'éléments à afficher dans le RecyclerView
     */
    @Override
    public int getItemCount() {
        return themes != null ? themes.size() : 0;
    }

    /**
     * ViewHolder contenant les références des vues d'un thème.
     *
     * Centralise les composants d'affichage nécessaires
     * pour limiter les recherches répétées dans le layout
     * et améliorer les performances du RecyclerView.
     */
    static class ThemeViewHolder extends RecyclerView.ViewHolder {
        final TextView tvTitle;
        final Button btnDelete;
        final LinearLayout container;

        ThemeViewHolder(@NonNull View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvThemeTitle);
            btnDelete = v.findViewById(R.id.btnDeleteTheme);
            container = v.findViewById(R.id.containerQuestions);
        }
    }
}