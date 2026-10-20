package com.bestcasttv.bestcasttvapp.app_utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.bestcasttv.bestcasttvapp.R;

public class SignOutDialogFragment extends DialogFragment {

    public interface CustomDialogListener {
        void onCustomButtonClick();
    }

    private CustomDialogListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mListener = (CustomDialogListener) context;
        } catch (ClassCastException e) {
            Log.e("ListenerException", " must implement SignOutDialogListener"+e.toString());
        }
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_signout, null);

        TextView signoutText = view.findViewById(R.id.btn_signout);
        TextView cancelText = view.findViewById(R.id.btn_cancel);

        signoutText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onCustomButtonClick();
                dismiss();
            }
        });

        builder.setView(view);
        return builder.create();
    }
}

