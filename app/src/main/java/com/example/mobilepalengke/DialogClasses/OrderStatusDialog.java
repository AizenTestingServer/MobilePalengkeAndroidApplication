package com.example.mobilepalengke.DialogClasses;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioGroup;

import com.example.mobilepalengke.R;

import androidx.core.content.ContextCompat;

public class OrderStatusDialog {

    RadioGroup radioGroup;

    private final Context context;
    private final Activity activity;
    private Dialog dialog;

    private int selectedStatus;

    public OrderStatusDialog(Context context) {
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
        dialog.setContentView(R.layout.dialog_order_status_layout);
        dialog.setCanceledOnTouchOutside(false);

        radioGroup = dialog.findViewById(R.id.radioGroup);
        Button btnConfirm = dialog.findViewById(R.id.btnConfirm);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        
        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            int status = i;
            radioGroup.getChildAt(i).setOnClickListener(view -> selectedStatus = status);
        }

        btnConfirm.setOnClickListener(view -> {
            if (dialogListener != null)
                dialogListener.onConfirm(selectedStatus);
        });

        btnCancel.setOnClickListener(view -> dismissDialog());
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

    public void setSelectedStatus(int selectedStatus) {
        this.selectedStatus = selectedStatus;
        radioGroup.getChildAt(selectedStatus).performClick();
    }

    DialogListener dialogListener;

    public interface DialogListener {
        void onConfirm(int selectedStatus);
    }

    public void setDialogListener(DialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }
}
