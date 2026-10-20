package com.bestcasttv.bestcasttvapp.app_utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.bestcasttv.bestcasttvapp.R;

public class SignoutAlertDialog extends Dialog {

    public interface SignOutOnclickListener{
        void setOnClickSignOut();
    }

    private SignOutOnclickListener mListener;

    public void setOnDialogListener(SignOutOnclickListener listener) {
        this.mListener = listener;
    }

    public SignoutAlertDialog(Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_signout);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setCancelable(false);

        TextView signoutText = findViewById(R.id.btn_signout);
        TextView cancelText = findViewById(R.id.btn_cancel);

        signoutText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.setOnClickSignOut();
                }
            }
        });

        cancelText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}
