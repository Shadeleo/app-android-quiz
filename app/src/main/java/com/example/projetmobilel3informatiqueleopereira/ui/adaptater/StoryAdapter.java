package com.example.projetmobilel3informatiqueleopereira.ui.adaptater;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.projetmobilel3informatiqueleopereira.R;

import java.util.List;

/**
 * Adaptateur personnalisé utilisé pour afficher la liste des niveaux du mode Histoire.
 *
 * Cette classe gère l'affichage des niveaux ainsi que leur état visuel
 * (réussi, disponible ou verrouillé) en fonction de la progression
 * actuelle de l'utilisateur.
 */
public class StoryAdapter extends ArrayAdapter<String> {
    private final int currentProgress;
    private final LayoutInflater inflater;

    /**
     * Initialise l'adaptateur des niveaux du mode Histoire.
     *
     * @param context contexte utilisé pour accéder aux ressources
     * @param levels liste des noms de niveaux à afficher
     * @param currentProgress niveau actuellement atteint par le joueur
     */
    public StoryAdapter(Context context, List<String> levels, int currentProgress) {
        super(context, 0, levels);
        this.currentProgress = currentProgress;
        this.inflater = LayoutInflater.from(context);
    }

    /**
     * Classe interne utilisée pour stocker les références des vues.
     *
     * Le pattern ViewHolder permet d'éviter les appels répétés
     * à findViewById et améliore les performances lors du
     * défilement de la liste.
     */
    private static class ViewHolder {
        final TextView tvName;
        final ImageView imgStatus;

        ViewHolder(View view) {
            tvName = view.findViewById(R.id.tvLevelName);
            imgStatus = view.findViewById(R.id.imgStatus);
        }
    }

    /**
     * Fournit la vue correspondant à un élément de la liste.
     *
     * Gère le recyclage des vues pour optimiser les performances,
     * puis applique les données et le style correspondant
     * au niveau affiché.
     *
     * @param position position de l'élément dans la liste
     * @param convertView vue recyclable si disponible
     * @param parent conteneur de la vue
     * @return vue configurée pour l'élément courant
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_level_story, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String levelName = getItem(position);
        int levelNum = position + 1;

        if (levelName != null) {
            holder.tvName.setText(levelName);
            updateLevelStatus(convertView, holder, levelNum);
        }

        return convertView;
    }

    /**
     * Met à jour l'apparence visuelle d'un niveau.
     *
     * Le style dépend de la progression du joueur :
     * - Niveau réussi : icône validée et opacité réduite
     * - Niveau actuel : icône de lecture et mise en avant
     * - Niveau verrouillé : icône de cadenas et opacité faible
     *
     * @param itemView vue de l'élément dans la liste
     * @param holder ViewHolder associé
     * @param levelNum numéro du niveau traité
     */
    private void updateLevelStatus(View itemView, ViewHolder holder, int levelNum) {
        Context context = getContext();

        if (levelNum < currentProgress) {
            holder.imgStatus.setImageResource(android.R.drawable.checkbox_on_background);
            holder.imgStatus.setColorFilter(ContextCompat.getColor(context, android.R.color.holo_green_light));
            itemView.setAlpha(0.6f);

        } else if (levelNum == currentProgress) {
            holder.imgStatus.setImageResource(android.R.drawable.ic_media_play);
            holder.imgStatus.setColorFilter(ContextCompat.getColor(context, R.color.accent_pro));
            itemView.setAlpha(1.0f);

        } else {
            holder.imgStatus.setImageResource(android.R.drawable.ic_lock_idle_lock);
            holder.imgStatus.setColorFilter(ContextCompat.getColor(context, R.color.gray_text));
            itemView.setAlpha(0.4f);
        }
    }
}