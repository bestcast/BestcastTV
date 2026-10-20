package com.bestcasttv.bestcasttvapp.app_utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import android.widget.ProgressBar;

import com.bestcasttv.bestcasttvapp.R;

public class LoadingDialog extends Dialog {

    public LoadingDialog(Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_loading);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // Set background to transparent
        setCancelable(false);

        // Get the ProgressBar from the layout
        ProgressBar progressBar = findViewById(R.id.progressBar);

        // Set the indeterminate tint color
        progressBar.getIndeterminateDrawable().setColorFilter(
                context.getResources().getColor(R.color.app_red),
                PorterDuff.Mode.SRC_IN);
    }
}
