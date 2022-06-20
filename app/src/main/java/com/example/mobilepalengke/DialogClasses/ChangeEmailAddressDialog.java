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

import com.example.mobilepalengke.Classes.Credentials;
import com.example.mobilepalengke.R;

import androidx.core.content.ContextCompat;

public class ChangeEmailAddressDialog {

    private TextView tvErrorCaption;
    private EditText etEmail;

    private final Context context;
    private final Activity activity;
    private Dialog dialog;

    private String emailAddress;

    public ChangeEmailAddressDialog(Context context) {
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
        dialog.setContentView(R.layout.dialog_change_email_address_layout);
        dialog.setCanceledOnTouchOutside(false);

        tvErrorCaption = dialog.findViewById(R.id.tvErrorCaption);
        etEmail = dialog.findViewById(R.id.etEmail);
        Button btnConfirm = dialog.findViewById(R.id.btnConfirm);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                emailAddress = editable.toString().trim();

                if (Credentials.isValidEmailAddress(emailAddress)) {
                    etEmail.setBackgroundResource(R.drawable.et_bg_default);
                    etEmail.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(context, R.drawable.ic_email_focused),
                            null, null, null);

                    tvErrorCaption.setVisibility(View.GONE);
                } else {
                    etEmail.setBackgroundResource(R.drawable.et_bg_error);
                    etEmail.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(context, R.drawable.ic_email_red),
                            null, null, null);

                    tvErrorCaption.setText(context.getString(R.string.invalidEmail));
                    tvErrorCaption.setVisibility(View.VISIBLE);
                }
            }
        });

        btnConfirm.setOnClickListener(view -> {
            if (emailAddress == null || !Credentials.isValidEmailAddress(emailAddress)) {
                etEmail.setBackgroundResource(R.drawable.et_bg_error);
                etEmail.setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat.getDrawable(context, R.drawable.ic_email_red),
                        null, null, null);

                tvErrorCaption.setText(context.getString(R.string.invalidEmail));
                tvErrorCaption.setVisibility(View.VISIBLE);

                return;
            }

            if (dialogListener != null) dialogListener.onConfirm(emailAddress);
        });

        btnCancel.setOnClickListener(view -> dismissDialog());
    }

    private void setDialogWindow() {
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(activity, R.drawable.bg_corner));
    }

    public void showDialog() {
        dialog.show();

        etEmail.getText().clear();

        etEmail.setBackgroundResource(R.drawable.et_bg_default);
        etEmail.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(context, R.drawable.ic_email_focused),
                null, null, null);

        tvErrorCaption.setVisibility(View.GONE);
    }

    public void dismissDialog() {
        dialog.dismiss();
    }

    DialogListener dialogListener;

    public interface DialogListener {
        void onConfirm(String emailAddress);
    }

    public void setDialogListener(DialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }
}
