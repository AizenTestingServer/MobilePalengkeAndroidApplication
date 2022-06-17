package com.example.mobilepalengke.DialogClasses;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mobilepalengke.R;

import androidx.core.content.ContextCompat;

public class MessageDialog {

    TextView messageTextView;
    ImageView imgSuccess, imgError, imgInfo;
    Button btnOK;

    Context context;
    Activity activity;
    Dialog dialog;

    public MessageDialog(Context context) {
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
        dialog.setContentView(R.layout.dialog_message_layout);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        messageTextView = dialog.findViewById(R.id.messageTextView);
        imgSuccess = dialog.findViewById(R.id.imgSuccess);
        imgError = dialog.findViewById(R.id.imgError);
        imgInfo = dialog.findViewById(R.id.imgInfo);
        btnOK = dialog.findViewById(R.id.btnOK);

        btnOK.setOnClickListener(view -> dismissDialog());
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

    public void setTextType(int textType) {
        imgSuccess.setVisibility(View.GONE);
        imgError.setVisibility(View.GONE);

        switch (textType) {
            case 1:
                imgSuccess.setVisibility(View.VISIBLE);
                break;
            case 2:
                imgError.setVisibility(View.VISIBLE);
                break;
            case 3:
                imgInfo.setVisibility(View.VISIBLE);
                break;
        }
    }
}
