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

public class DownloadDialog {

    private TextView messageTextView;

    private final Context context;
    private final Activity activity;
    private Dialog dialog;

    public DownloadDialog(Context context) {
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
        dialog.setContentView(R.layout.dialog_download_layout);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        messageTextView = dialog.findViewById(R.id.messageTextView);
        Button btnDownload = dialog.findViewById(R.id.btnDownload);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        btnDownload.setOnClickListener(view -> {
            if (dialogListener != null) dialogListener.onDownload();
        });

        btnCancel.setOnClickListener(view -> {
            if (dialogListener != null) dialogListener.onCancel();
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
        void onDownload();
        void onCancel();
    }

    public void setDialogListener(DialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }
}
