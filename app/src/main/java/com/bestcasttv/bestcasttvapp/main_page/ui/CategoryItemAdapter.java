package com.bestcasttv.bestcasttvapp.main_page.ui;

import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bestcasttv.bestcasttvapp.R;
import com.bestcasttv.bestcasttvapp.main_page.model.Genres;
import com.bestcasttv.bestcasttvapp.who_watching.KeyEventUtils;

import java.util.List;

public class CategoryItemAdapter  extends RecyclerView.Adapter<CategoryItemAdapter.MyItemViewHolder> {

    public List<Genres> genresList;
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

    public CategoryItemAdapter(List<Genres> genresList, Context context) {
        this.genresList = genresList;
        this.context = context;
    }

    @NonNull
    @Override
    public MyItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.selected_list_item_textview, null);
        MyItemViewHolder viewHoldler = new MyItemViewHolder(view, mlistener);
        return viewHoldler;

    }

    @Override
    public void onBindViewHolder(@NonNull MyItemViewHolder holder, int position) {

        holder.itemView.setSelected(selectedItem == position);
        Genres genres = genresList.get(position);

        holder.itemName.setText(genres.getTitle());
    }


    @Override
    public int getItemCount() {
        return (null != genresList ? genresList.size() : 0);
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



//    @Override
//    public void onBindViewHolder(MyItemViewHolder holder, @SuppressLint("RecyclerView") int position) {
//        holder.itemName.setText(data.get(position));
//
//        holder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (hasFocus) {
//                    // Item is selected/focused
//                    String item = data.get(position);
//
//                } else {
//                    // Item loses focus (optional)
//                }
//            }
//        });
//    }

    public class MyItemViewHolder extends RecyclerView.ViewHolder {

        TextView itemName;


        public MyItemViewHolder(@NonNull View itemView, final OnRecyclerViewItemClickListener listener) {

            super(itemView);

            itemName = itemView.findViewById(R.id.listItemTextview);

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
                    } else {

                    }
                }
            });

        }

    }
}


