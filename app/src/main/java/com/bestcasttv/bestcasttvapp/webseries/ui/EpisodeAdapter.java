package com.bestcasttv.bestcasttvapp.webseries.ui;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bestcasttv.bestcasttvapp.R;
import com.bestcasttv.bestcasttvapp.api_services.RetrofitClient;
import com.bestcasttv.bestcasttvapp.webseries.model.EpisodeModel;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class EpisodeAdapter extends RecyclerView.Adapter<EpisodeAdapter.EpisodeViewHolder> {

    private List<EpisodeModel> episodes;
    private final OnEpisodeClickListener listener;

    public interface OnEpisodeClickListener {
        void onEpisodeClick(EpisodeModel episode, int position);
    }

    public EpisodeAdapter(List<EpisodeModel> episodes, OnEpisodeClickListener listener) {
        this.episodes = episodes != null ? episodes : new ArrayList<>();
        this.listener = listener;
    }

    public void updateEpisodes(List<EpisodeModel> newEpisodes) {
        this.episodes = newEpisodes != null ? newEpisodes : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EpisodeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_episode, parent, false);
        return new EpisodeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EpisodeViewHolder holder, int position) {
        if (episodes == null || position >= episodes.size()) return;
        EpisodeModel episode = episodes.get(position);
        if (episode == null) return;

        holder.txtTitle.setText(episode.getTitle());
        holder.txtDuration.setText(episode.getDurationText());

        String description = episode.getContentPlain();
        if (description == null || description.trim().isEmpty()) {
            description = episode.getContent();
        }
        if (description != null) {
            description = description.replaceAll("<[^>]*>", "").trim();
        }
        holder.txtDescription.setText(description != null ? description : "");

        int watchedPercent = 0;
        if (episode.getEpisodeUser() != null) {
            watchedPercent = episode.getEpisodeUser().getWatchedPercent();
        }

        if (watchedPercent > 0) {
            holder.progressBar.setVisibility(View.VISIBLE);
            holder.progressBar.setProgress(watchedPercent);
        } else {
            holder.progressBar.setVisibility(View.GONE);
        }

        String imageUrl = episode.getThumbnail();
        if (imageUrl == null || imageUrl.isEmpty()) {
            imageUrl = episode.getImage();
        }
        if (imageUrl != null && !imageUrl.startsWith("http")) {
            imageUrl = RetrofitClient.BASE_URL + "/" + imageUrl;
        }

        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .placeholder(R.drawable.default_landscape)
                .error(R.drawable.default_landscape)
                .into(holder.imgThumb);

        holder.itemView.setOnClickListener(v -> triggerClick(holder));

        holder.itemView.setOnKeyListener((v, keyCode, event) -> {
            if ((keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) && event.getAction() == KeyEvent.ACTION_DOWN) {
                triggerClick(holder);
                return true;
            }
            return false;
        });
    }

    private void triggerClick(EpisodeViewHolder holder) {
        if (listener != null) {
            int pos = holder.getBindingAdapterPosition();
            if (pos >= 0 && pos < getItemCount()) {
                listener.onEpisodeClick(episodes.get(pos), pos);
            }
        }
    }

    @Override
    public int getItemCount() {
        return episodes != null ? episodes.size() : 0;
    }

    static class EpisodeViewHolder extends RecyclerView.ViewHolder {
        ImageView imgThumb;
        ProgressBar progressBar;
        TextView txtTitle;
        TextView txtDuration;
        TextView txtDescription;

        EpisodeViewHolder(@NonNull View itemView) {
            super(itemView);
            imgThumb = itemView.findViewById(R.id.img_episode_thumb);
            progressBar = itemView.findViewById(R.id.progress_episode_watch);
            txtTitle = itemView.findViewById(R.id.txt_episode_title);
            txtDuration = itemView.findViewById(R.id.txt_episode_duration);
            txtDescription = itemView.findViewById(R.id.txt_episode_description);

            itemView.setFocusable(true);
            itemView.setFocusableInTouchMode(true);
            itemView.setClickable(true);
        }
    }
}
