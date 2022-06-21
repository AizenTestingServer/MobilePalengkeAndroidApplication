package com.example.mobilepalengke.DialogClasses;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.example.mobilepalengke.R;

import androidx.core.content.ContextCompat;

public class StatusDialog {

    private TextView messageTextView;

    private final Context context;
    private final Activity activity;
    private Dialog dialog;

    public StatusDialog(Context context) {
        this.context = context;
        activity = (Activity) context;

        createDialog();
    }

    private void createDialog() {
        setDialog();
        setDialogWindow();
    }

    private void setDialog() {
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_status_layout);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        messageTextView = dialog.findViewById(R.id.messageTextView);
        Button btnOK = dialog.findViewById(R.id.btnOK);

        btnOK.setOnClickListener(view -> {
            if (dialogListener != null) dialogListener.onOK();
        });
    }

    private void setDialogWindow() {
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(activity, R.drawable.bg_corner));
    }

    public void showDialog() {
        dialog.show();
    }

    public void dismissDialog() {
        dialog.dismiss();
    }

    public void setTextCaption(String textCaption) {
        messageTextView.setText(textCaption);
    }

    DialogListener dialogListener;

    public interface DialogListener {
        void onOK();
    }

    public void setDialogListener(DialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }
}
