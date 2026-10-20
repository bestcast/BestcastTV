package com.bestcasttv.bestcasttvapp.who_watching;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.bestcasttv.bestcasttvapp.R;

import java.util.ArrayList;
import java.util.List;

public class WhoWatchingAdapter extends RecyclerView.Adapter<WhoWatchingAdapter.CustomViewHolder> implements Filterable {
    public List<WhoWatchingItem> whoWatchingItemList, filterlist;
    public Context context;
    OnRecyclerViewItemClickListener mlistener;

    private RecyclerView mRecyclerView;
    private int selectedItem = RecyclerView.NO_POSITION;


    public interface OnRecyclerViewItemClickListener {

        void onListItemClicked(int position, View v);

    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        mlistener = listener;
    }

    public WhoWatchingAdapter(List<WhoWatchingItem> whoWatchingItemList, Context context) {
        this.whoWatchingItemList = whoWatchingItemList;
        this.filterlist = whoWatchingItemList;
        this.context = context;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.who_watching_item, null);
        CustomViewHolder viewHoldler = new CustomViewHolder(view, mlistener);
        return viewHoldler;

    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {

        holder.itemView.setSelected(selectedItem == position);

        WhoWatchingItem whoWatchingItemListItem = filterlist.get(position);

        holder.profileName.setText(whoWatchingItemListItem.getProfileName());

        Glide.with(context)
                .load(whoWatchingItemListItem.getProfilePicture())
                .placeholder(R.drawable.default_profile)
                .error(R.drawable.default_profile)
                .into(holder.profileImage);
    }

    @Override
    public int getItemCount() {
        return (null != filterlist ? filterlist.size() : 0);
    }


    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charString = constraint.toString();
                if (charString.isEmpty()) {
                    filterlist = whoWatchingItemList;
                } else {
                    List<WhoWatchingItem> filterItem = new ArrayList<>();
                    for (WhoWatchingItem item : whoWatchingItemList) {
                        if (item.getProfileName().toLowerCase().contains(charString.toLowerCase())) {
                            filterItem.add(item);
                        }
                    }
                    filterlist = filterItem;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = filterlist;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filterlist = (ArrayList<WhoWatchingItem>) results.values;
                notifyDataSetChanged();
            }

        };
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        mRecyclerView=recyclerView;
        mRecyclerView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                RecyclerView.LayoutManager lm = mRecyclerView.getLayoutManager();

                // Return false if scrolled to the bounds and allow focus to move off the list
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        return tryMoveSelection(lm, 1);
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        return tryMoveSelection(lm, -1);
                    } else if(KeyEventUtils.isConfirmButton(event)){
                        View view = mRecyclerView.findViewHolderForAdapterPosition(selectedItem).itemView;
                        if((event.getFlags() & KeyEvent.FLAG_LONG_PRESS)==KeyEvent.FLAG_LONG_PRESS) {
                            view.performLongClick();
                        }
                        else{
                            view.performClick();
                        }
                        return true;
                    }
                }

                return false;
            }
        });
    }

    private void tryMoveSelection(int position) {
        int itemCount = getItemCount();
        if (position >= 0 && position < itemCount) {
            notifyItemChanged(selectedItem);
            selectedItem = position;
            notifyItemChanged(selectedItem);
        }
    }

    private boolean tryMoveSelection(RecyclerView.LayoutManager lm, int direction) {
        int tryFocusItem = selectedItem + direction;

        // If still within valid bounds, move the selection, notify to redraw, and scroll
        if (tryFocusItem >= 0 && tryFocusItem < getItemCount()) {
            notifyItemChanged(selectedItem);
            selectedItem = tryFocusItem;
            notifyItemChanged(selectedItem);
            lm.scrollToPosition(selectedItem);
            return true;
        }

        return false;
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {

        TextView profileName;
        ImageView profileImage;
        private AnimatorSet zoomInAnim;
        private AnimatorSet zoomOutAnim;


        public CustomViewHolder(@NonNull View itemView, final OnRecyclerViewItemClickListener listener) {

            super(itemView);

            profileImage = itemView.findViewById(R.id.profileImage);
            profileName = itemView.findViewById(R.id.profileName);

            zoomInAnim = createZoomInAnimator();
            zoomOutAnim = createZoomOutAnimator();

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    notifyItemChanged(selectedItem);
                    selectedItem = getLayoutPosition();
                    notifyItemChanged(selectedItem);

                    if (listener != null) {
                        int positions = getAdapterPosition();
                        if (positions != RecyclerView.NO_POSITION) {
                            listener.onListItemClicked(positions, view);
                        }
                    }
                }
            });

            itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        selectedItem = getAdapterPosition();
                        zoomInAnim.start();
                    } else {
                        zoomOutAnim.start();
                    }
                }
            });

        }

        private AnimatorSet createZoomInAnimator() {
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(itemView, View.SCALE_X, 1f, 1.2f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(itemView, View.SCALE_Y, 1f, 1.2f);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(scaleX, scaleY);
            animatorSet.setDuration(200);
            return animatorSet;
        }

        private AnimatorSet createZoomOutAnimator() {
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(itemView, View.SCALE_X, 1.2f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(itemView, View.SCALE_Y, 1.2f, 1f);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(scaleX, scaleY);
            animatorSet.setDuration(200);
            return animatorSet;
        }
    }
}


