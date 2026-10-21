package com.bestcasttv.bestcasttvapp.webseries.presenter;

import android.graphics.Color;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.RowHeaderPresenter;

public class CustomRowHeaderPresenter extends RowHeaderPresenter {

    public CustomRowHeaderPresenter() {
        super();
    }

    @NonNull
    @Override
    public Presenter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
        Presenter.ViewHolder vh = super.onCreateViewHolder(parent);
        parent.setClipChildren(false);
        parent.setClipToPadding(false);

        if (vh.view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) vh.view;
            vg.setClipChildren(false);
            vg.setClipToPadding(false);
        }

        TextView headerView = vh.view.findViewById(androidx.leanback.R.id.row_header);
        if (headerView == null && vh.view instanceof TextView) {
            headerView = (TextView) vh.view;
        }

        if (headerView != null) {
            headerView.setIncludeFontPadding(false);
            headerView.setPadding(0, 0, 0, 0);
            headerView.setTextColor(Color.WHITE);
            headerView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            headerView.setSingleLine(true);
        }

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull Presenter.ViewHolder viewHolder, Object item) {
        super.onBindViewHolder(viewHolder, item);
        TextView headerView = viewHolder.view.findViewById(androidx.leanback.R.id.row_header);
        if (headerView == null && viewHolder.view instanceof TextView) {
            headerView = (TextView) viewHolder.view;
        }

        if (headerView != null) {
            headerView.setIncludeFontPadding(false);
            headerView.setPadding(0, 0, 0, 0);
            headerView.setTextColor(Color.WHITE);
            headerView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            headerView.setSingleLine(true);
        }
    }
}
