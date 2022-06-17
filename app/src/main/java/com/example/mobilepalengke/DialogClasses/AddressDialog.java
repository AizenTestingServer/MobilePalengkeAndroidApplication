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

import com.example.mobilepalengke.DataClasses.Address;
import com.example.mobilepalengke.R;

import androidx.core.content.ContextCompat;

public class AddressDialog {

    TextView textView, tvErrorCaption;
    EditText etLabel, etAddress;
    Button btnConfirm, btnCancel;

    String label, value, addressId;

    Context context;
    Activity activity;
    Dialog dialog;

    public AddressDialog(Context context) {
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
        dialog.setContentView(R.layout.dialog_address_layout);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        textView = dialog.findViewById(R.id.textView);
        tvErrorCaption = dialog.findViewById(R.id.tvErrorCaption);
        etLabel = dialog.findViewById(R.id.etLabel);
        etAddress = dialog.findViewById(R.id.etAddress);
        btnConfirm = dialog.findViewById(R.id.btnConfirm);
        btnCancel = dialog.findViewById(R.id.btnCancel);

        etLabel.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                label = editable.toString().trim();

                if (label.length() > 1 && label.trim().length() < 17) {
                    etLabel.setBackgroundResource(R.drawable.et_bg_default);
                    etLabel.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(context, R.drawable.ic_label_focused),
                            null, null, null);

                    tvErrorCaption.setVisibility(View.GONE);
                } else {
                    etLabel.setBackgroundResource(R.drawable.et_bg_error);
                    etLabel.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(context, R.drawable.ic_label_red),
                            null, null, null);

                    tvErrorCaption.setText(context.getString(R.string.invalidLabel));
                    tvErrorCaption.setVisibility(View.VISIBLE);
                }
            }
        });

        etAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                value = editable.toString().trim();

                if (value.length() > 1) {
                    etAddress.setBackgroundResource(R.drawable.et_bg_default);
                    etAddress.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(context, R.drawable.ic_share_location_focused),
                            null, null, null);

                    if (label != null && label.length() > 1 && label.trim().length() < 17)
                        tvErrorCaption.setVisibility(View.GONE);
                    else {
                        etLabel.setBackgroundResource(R.drawable.et_bg_error);
                        etLabel.setCompoundDrawablesWithIntrinsicBounds(
                                ContextCompat.getDrawable(context, R.drawable.ic_label_red),
                                null, null, null);

                        tvErrorCaption.setText(context.getString(R.string.invalidLabel));
                        tvErrorCaption.setVisibility(View.VISIBLE);
                    }
                } else {
                    etAddress.setBackgroundResource(R.drawable.et_bg_error);
                    etAddress.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(context, R.drawable.ic_share_location_red),
                            null, null, null);

                    tvErrorCaption.setText(context.getString(R.string.invalidName));
                    tvErrorCaption.setVisibility(View.VISIBLE);
                }
            }
        });

        btnConfirm.setOnClickListener(view -> {
            boolean isInvalidLabel = label == null ||
                    label.trim().length() < 2 || label.trim().length() > 16;
            boolean isInvalidAddress = value == null || value.trim().length() < 2;

            if (isInvalidLabel) {
                etLabel.setBackgroundResource(R.drawable.et_bg_error);
                etLabel.setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat.getDrawable(context, R.drawable.ic_label_red),
                        null, null, null);
            }
            if (isInvalidAddress) {
                etAddress.setBackgroundResource(R.drawable.et_bg_error);
                etAddress.setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat.getDrawable(context, R.drawable.ic_share_location_red),
                        null, null, null);
            }

            if (isInvalidLabel || isInvalidAddress) {
                if (isInvalidLabel)
                    tvErrorCaption.setText(context.getString(R.string.invalidLabel));
                if (isInvalidAddress)
                    tvErrorCaption.setText(context.getString(R.string.invalidAddress));
                tvErrorCaption.setVisibility(View.VISIBLE);
                return;
            }

            Address address = new Address(addressId, null, label, value);

            if (dialogListener != null)
                dialogListener.onConfirm(address);
        });

        btnCancel.setOnClickListener(view -> dismissDialog());
    }

    private void setDialogWindow() {
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(activity, R.drawable.bg_corner));
    }

    public void showDialog() {
        dialog.show();

        addressId = null;

        etLabel.getText().clear();
        etAddress.getText().clear();

        etLabel.setBackgroundResource(R.drawable.et_bg_default);
        etLabel.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(context, R.drawable.ic_label_focused),
                null, null, null);

        etAddress.setBackgroundResource(R.drawable.et_bg_default);
        etAddress.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(context, R.drawable.ic_share_location_focused),
                null, null, null);

        tvErrorCaption.setVisibility(View.GONE);

        textView.setText(context.getString(R.string.addAddress));
    }

    public void dismissDialog() {
        dialog.dismiss();
    }

    public void setData(Address address) {
        addressId = address.getId();

        etLabel.setText(address.getName());
        etAddress.setText(address.getValue());

        textView.setText(context.getString(R.string.updateAddress));
    }

    DialogListener dialogListener;

    public interface DialogListener {
        void onConfirm(Address address);
    }

    public void setDialogListener(DialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }
}
