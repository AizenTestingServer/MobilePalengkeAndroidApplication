package com.example.mobilepalengke.DialogClasses;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.example.mobilepalengke.R;

import androidx.core.content.ContextCompat;

public class LoadingDialog {

    TextView loadingTextView;

    Context context;
    Activity activity;
    Dialog dialog;

    public LoadingDialog(Context context) {
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
        dialog.setContentView(R.layout.dialog_loading_layout);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        loadingTextView = dialog.findViewById(R.id.loadingTextView);
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
        loadingTextView.setText(textCaption);
    }
}
