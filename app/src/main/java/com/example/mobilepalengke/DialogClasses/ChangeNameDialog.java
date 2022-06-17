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

public class ChangeNameDialog {

    TextView tvErrorCaption;
    EditText etLastName, etFirstName;
    Button btnConfirm, btnCancel;

    String lastName, firstName;

    Context context;
    Activity activity;
    Dialog dialog;

    public ChangeNameDialog(Context context) {
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
        dialog.setContentView(R.layout.dialog_change_name_layout);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        tvErrorCaption = dialog.findViewById(R.id.tvErrorCaption);
        etLastName = dialog.findViewById(R.id.etLastName);
        etFirstName = dialog.findViewById(R.id.etFirstName);
        btnConfirm = dialog.findViewById(R.id.btnConfirm);
        btnCancel = dialog.findViewById(R.id.btnCancel);

        etLastName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                lastName = editable.toString().trim();

                if (lastName.length() > 1 && lastName.trim().length() < 17) {
                    etLastName.setBackgroundResource(R.drawable.et_bg_default);
                    etLastName.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(context, R.drawable.ic_person_focused),
                            null, null, null);

                    if (firstName != null && firstName.length() > 1 && firstName.trim().length() < 17)
                        tvErrorCaption.setVisibility(View.GONE);
                } else {
                    etLastName.setBackgroundResource(R.drawable.et_bg_error);
                    etLastName.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(context, R.drawable.ic_person_red),
                            null, null, null);

                    tvErrorCaption.setText(context.getString(R.string.invalidName));
                    tvErrorCaption.setVisibility(View.VISIBLE);
                }
            }
        });

        etFirstName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                firstName = editable.toString().trim();

                if (firstName.length() > 1 && firstName.trim().length() < 17) {
                    etFirstName.setBackgroundResource(R.drawable.et_bg_default);
                    etFirstName.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(context, R.drawable.ic_person_focused),
                            null, null, null);

                    if (lastName != null && lastName.length() > 1 && lastName.trim().length() < 17)
                        tvErrorCaption.setVisibility(View.GONE);
                } else {
                    etFirstName.setBackgroundResource(R.drawable.et_bg_error);
                    etFirstName.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(context, R.drawable.ic_person_red),
                            null, null, null);

                    tvErrorCaption.setText(context.getString(R.string.invalidName));
                    tvErrorCaption.setVisibility(View.VISIBLE);
                }
            }
        });

        btnConfirm.setOnClickListener(view -> {
            boolean isInvalidLastName = lastName == null ||
                    lastName.trim().length() < 2 || lastName.trim().length() > 16;
            boolean isInvalidFirstName = firstName == null ||
                    firstName.trim().length() < 2 || firstName.trim().length() > 16;

            if (isInvalidLastName) {
                etLastName.setBackgroundResource(R.drawable.et_bg_error);
                etLastName.setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat.getDrawable(context, R.drawable.ic_person_red),
                        null, null, null);
            }
            if (isInvalidFirstName) {
                etFirstName.setBackgroundResource(R.drawable.et_bg_error);
                etFirstName.setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat.getDrawable(context, R.drawable.ic_person_red),
                        null, null, null);
            }

            if (isInvalidLastName || isInvalidFirstName) {
                tvErrorCaption.setText(context.getString(R.string.invalidName));
                tvErrorCaption.setVisibility(View.VISIBLE);
                return;
            }

            if (dialogListener != null) dialogListener.onConfirm(lastName, firstName);
        });

        btnCancel.setOnClickListener(view -> dismissDialog());
    }

    private void setDialogWindow() {
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(activity, R.drawable.bg_corner));
    }

    public void showDialog() {
        dialog.show();

        etLastName.getText().clear();
        etFirstName.getText().clear();

        etLastName.setBackgroundResource(R.drawable.et_bg_default);
        etLastName.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(context, R.drawable.ic_person_focused),
                null, null, null);

        etFirstName.setBackgroundResource(R.drawable.et_bg_default);
        etFirstName.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(context, R.drawable.ic_person_focused),
                null, null, null);

        tvErrorCaption.setVisibility(View.GONE);
    }

    public void dismissDialog() {
        dialog.dismiss();
    }

    public void setData(String lastName, String firstName) {
        etLastName.setText(lastName);
        etFirstName.setText(firstName);
    }

    DialogListener dialogListener;

    public interface DialogListener {
        void onConfirm(String lastName, String firstName);
    }

    public void setDialogListener(DialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }
}
