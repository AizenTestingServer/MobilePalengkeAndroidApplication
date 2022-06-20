package com.example.mobilepalengke.DialogClasses;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.mobilepalengke.R;

import androidx.core.content.ContextCompat;

public class StringValueDialog {

    private TextView textView, tvErrorCaption;
    private EditText etValue;

    private final Context context;
    private final Activity activity;
    private Dialog dialog;

    private String value;

    public StringValueDialog(Context context) {
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
        dialog.setContentView(R.layout.dialog_string_value_layout);
        dialog.setCanceledOnTouchOutside(false);

        textView = dialog.findViewById(R.id.textView);
        tvErrorCaption = dialog.findViewById(R.id.tvErrorCaption);
        etValue = dialog.findViewById(R.id.etValue);
        Button btnConfirm = dialog.findViewById(R.id.btnConfirm);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        etValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                value = editable.toString().trim();
            }
        });

        btnConfirm.setOnClickListener(view -> {
            if (dialogListener != null) dialogListener.onConfirm(value);
            dismissDialog();
        });

        btnCancel.setOnClickListener(view -> dismissDialog());
    }

    private void setDialogWindow() {
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(activity, R.drawable.bg_corner));
    }

    public void showDialog(String title) {
        dialog.show();

        etValue.getText().clear();

        tvErrorCaption.setVisibility(View.GONE);

        textView.setText(title);
    }

    public void dismissDialog() {
        dialog.dismiss();
    }

    public void setData(String value) {
        etValue.setText(value);
    }

    DialogListener dialogListener;

    public interface DialogListener {
        void onConfirm(String value);
    }

    public void setDialogListener(DialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }
}
