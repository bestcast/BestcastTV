package com.bestcasttv.bestcasttvapp.main_page;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bestcasttv.bestcasttvapp.R;
import com.bestcasttv.bestcasttvapp.main_page.model.Genres;

import java.util.List;

public class CategoryCustomAdapter extends ArrayAdapter<Genres> {

    private LayoutInflater inflater;

    OnViewItemClickListener mlistener;
    public interface OnViewItemClickListener {
        void onListItemClicked(int position, View v);
    }

    public void setOnItemClickListener(OnViewItemClickListener listener) {
        this.mlistener = listener;
    }

    public CategoryCustomAdapter(Context context, List<Genres> items) {
        super(context, 0, items);
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.selected_list_item_textview, parent, false);
            holder = new ViewHolder();
            holder.titleTextView = convertView.findViewById(R.id.listItemTextview);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Genres item = getItem(position);
        if (item != null) {
            holder.titleTextView.setText(item.getTitle());
        }

        holder.titleTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mlistener != null) {
                    if (position != RecyclerView.NO_POSITION) {
                        mlistener.onListItemClicked(position, v);
                    }
                }
            }
        });

        return convertView;
    }

    static class ViewHolder {
        TextView titleTextView;
    }
}

