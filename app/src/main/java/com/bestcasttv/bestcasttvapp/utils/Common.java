package com.bestcasttv.bestcasttvapp.utils;
import android.content.Context;
import android.widget.TextView;

import com.bestcasttv.bestcasttvapp.R;

public class Common {

    public static int getWidthInPercent(Context context, int percent) {
        int width = context.getResources().getDisplayMetrics().widthPixels;
        return (width * percent) / 100;
    }

    public static int getHeightInPercent(Context context, int percent) {
        int height = context.getResources().getDisplayMetrics().heightPixels;
        return (height * percent) / 100;
    }

    public static void isEllipsized(TextView textView, OnEllipsizedListener listener) {
        if (textView.getLayout() != null) {
            int lineCount = textView.getLayout().getLineCount();
            if (lineCount > 0) {
                int ellipsisCount = textView.getLayout().getEllipsisCount(lineCount - 1);
                listener.onEllipsized(ellipsisCount > 0);
            }
        }
    }

//    public static void descriptionDialog(Context context, String title, String subtext, String description) {
//        AppCompatDialog dialog = new AppCompatDialog(context, R.style.Theme_TvApp);
//        dialog.getWindow().setBackgroundDrawableResource(R.color.transparent);
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        dialog.setContentView(R.layout.dialog_description);
//
//        TextView tvTitle = dialog.findViewById(R.id.tvTitle);
//        tvTitle.setText(title);
//        TextView tvSubTitle = dialog.findViewById(R.id.tvSubTitle);
//        tvSubTitle.setText(subtext);
//        TextView descriptionView = dialog.findViewById(R.id.description);
//        descriptionView.setText(description);
//
//        TextView closeBtn = dialog.findViewById(R.id.closeBtn);
//        closeBtn.setOnClickListener(v -> dialog.dismiss());
//
//        dialog.show();
//    }

    public interface OnEllipsizedListener {
        void onEllipsized(boolean isEllipsized);
    }
}

