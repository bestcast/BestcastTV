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
    private final OnEpisodeFocusListener focusListener;

    public interface OnEpisodeClickListener {
        void onEpisodeClick(EpisodeModel episode, int position);
    }

    public interface OnEpisodeFocusListener {
        void onEpisodeFocus(EpisodeModel episode, boolean hasFocus);
    }

    public EpisodeAdapter(List<EpisodeModel> episodes, OnEpisodeClickListener listener) {
        this(episodes, listener, null);
    }

    public EpisodeAdapter(List<EpisodeModel> episodes, OnEpisodeClickListener listener, OnEpisodeFocusListener focusListener) {
        this.episodes = episodes != null ? episodes : new ArrayList<>();
        this.listener = listener;
        this.focusListener = focusListener;
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

        // Dynamic Episode Title with number prefix
        String rawTitle = episode.getTitle() != null ? episode.getTitle().trim() : "";
        String displayTitle;
        if (rawTitle.toLowerCase().startsWith("episode") || rawTitle.toLowerCase().startsWith("ep ") || rawTitle.toLowerCase().startsWith("ep.")) {
            displayTitle = rawTitle;
        } else if (!rawTitle.isEmpty()) {
            displayTitle = (position + 1) + ". " + rawTitle;
        } else {
            displayTitle = "Episode " + (position + 1);
        }
        holder.txtTitle.setText(displayTitle);

        // Dynamic Duration Formatting
        String dur = episode.getDurationText();
        if (dur != null && !dur.trim().isEmpty()) {
            dur = dur.trim();
            try {
                if (dur.matches("\\d+")) {
                    long secs = Long.parseLong(dur);
                    long hours = secs / 3600;
                    long mins = (secs % 3600) / 60;
                    if (hours > 0) {
                        dur = hours + "h " + mins + "m";
                    } else if (mins > 0) {
                        dur = mins + "m";
                    } else {
                        dur = secs + "s";
                    }
                }
            } catch (Exception ignored) {}
            holder.txtDuration.setVisibility(View.VISIBLE);
            holder.txtDuration.setText(dur);
        } else {
            holder.txtDuration.setVisibility(View.GONE);
        }

        // Clean plain-text description
        String description = episode.getContentPlain();
        if (description == null || description.trim().isEmpty()) {
            description = episode.getContent();
        }
        if (description != null) {
            description = description.replaceAll("<[^>]*>", "").trim();
        }
        holder.txtDescription.setText(description != null ? description : "");

        // Watch Progress & Dynamic Badge
        int watchedPercent = 0;
        if (episode.getEpisodeUser() != null) {
            watchedPercent = episode.getEpisodeUser().getWatchedPercent();
        }

        if (watchedPercent > 0) {
            holder.progressBar.setVisibility(View.VISIBLE);
            holder.progressBar.setProgress(watchedPercent);
            if (holder.txtBadge != null) {
                holder.txtBadge.setVisibility(View.VISIBLE);
                if (watchedPercent >= 90) {
                    holder.txtBadge.setText("Watched");
                    holder.txtBadge.setTextColor(0xFF888888);
                } else {
                    holder.txtBadge.setText(watchedPercent + "% watched");
                    holder.txtBadge.setTextColor(0xFFE50914);
                }
            }
        } else {
            holder.progressBar.setVisibility(View.GONE);
            if (holder.txtBadge != null) {
                holder.txtBadge.setVisibility(View.GONE);
            }
        }

        // Thumbnail with Glide
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

        // Click listeners for remote OK and touch
        holder.itemView.setOnClickListener(v -> triggerClick(holder));

        holder.itemView.setOnKeyListener((v, keyCode, event) -> {
            if ((keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER)
                    && event.getAction() == KeyEvent.ACTION_DOWN) {
                triggerClick(holder);
                return true;
            }
            return false;
        });

        // Dynamic TV Focus Animation & Hover Preview Callback
        holder.itemView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                v.animate().scaleX(1.03f).scaleY(1.03f).translationZ(6f).setDuration(150).start();
                if (focusListener != null) {
                    int pos = holder.getBindingAdapterPosition();
                    if (pos >= 0 && pos < getItemCount()) {
                        focusListener.onEpisodeFocus(episodes.get(pos), true);
                    }
                }
            } else {
                v.animate().scaleX(1.0f).scaleY(1.0f).translationZ(0f).setDuration(150).start();
            }
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
        TextView txtBadge;
        TextView txtDescription;

        EpisodeViewHolder(@NonNull View itemView) {
            super(itemView);
            imgThumb = itemView.findViewById(R.id.img_episode_thumb);
            progressBar = itemView.findViewById(R.id.progress_episode_watch);
            txtTitle = itemView.findViewById(R.id.txt_episode_title);
            txtDuration = itemView.findViewById(R.id.txt_episode_duration);
            txtBadge = itemView.findViewById(R.id.txt_episode_badge);
            txtDescription = itemView.findViewById(R.id.txt_episode_description);

            itemView.setFocusable(true);
            itemView.setFocusableInTouchMode(true);
            itemView.setClickable(true);
        }
    }
}
