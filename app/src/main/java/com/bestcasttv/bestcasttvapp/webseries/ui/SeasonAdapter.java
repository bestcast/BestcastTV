package com.bestcasttv.bestcasttvapp.webseries.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bestcasttv.bestcasttvapp.R;
import com.bestcasttv.bestcasttvapp.webseries.model.SeasonModel;

import java.util.List;

public class SeasonAdapter extends RecyclerView.Adapter<SeasonAdapter.SeasonViewHolder> {

    private final List<SeasonModel> seasons;
    private int selectedPosition = 0;
    private final OnSeasonClickListener listener;

    public interface OnSeasonClickListener {
        void onSeasonClick(SeasonModel season, int position);
    }

    public SeasonAdapter(List<SeasonModel> seasons, OnSeasonClickListener listener) {
        this.seasons = seasons;
        this.listener = listener;
    }

    public void setSelectedPosition(int position) {
        if (this.selectedPosition != position) {
            int previousPosition = this.selectedPosition;
            this.selectedPosition = position;
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);
        }
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    @NonNull
    @Override
    public SeasonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_season_pill, parent, false);
        return new SeasonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SeasonViewHolder holder, int position) {
        SeasonModel season = seasons.get(position);
        holder.txtSeason.setText(season.getTitle() != null ? season.getTitle() : "Season " + (position + 1));

        boolean isSelected = (position == selectedPosition);
        holder.txtSeason.setActivated(isSelected);
        holder.txtSeason.setSelected(isSelected);

        holder.txtSeason.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                int newPos = holder.getBindingAdapterPosition();
                if (newPos != RecyclerView.NO_POSITION && newPos != selectedPosition) {
                    int previous = selectedPosition;
                    selectedPosition = newPos;
                    notifyItemChanged(previous);
                    notifyItemChanged(selectedPosition);
                    if (listener != null && newPos < seasons.size()) {
                        listener.onSeasonClick(seasons.get(newPos), newPos);
                    }
                }
            }
        });

        holder.txtSeason.setOnClickListener(v -> {
            int newPos = holder.getBindingAdapterPosition();
            if (newPos != RecyclerView.NO_POSITION && newPos != selectedPosition) {
                int previous = selectedPosition;
                selectedPosition = newPos;
                notifyItemChanged(previous);
                notifyItemChanged(selectedPosition);
                if (listener != null && newPos < seasons.size()) {
                    listener.onSeasonClick(seasons.get(newPos), newPos);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return seasons != null ? seasons.size() : 0;
    }

    static class SeasonViewHolder extends RecyclerView.ViewHolder {
        TextView txtSeason;

        SeasonViewHolder(@NonNull View itemView) {
            super(itemView);
            txtSeason = (TextView) itemView;
            txtSeason.setFocusable(true);
            txtSeason.setClickable(true);
        }
    }
}
