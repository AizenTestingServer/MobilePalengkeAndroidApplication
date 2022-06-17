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

import com.example.mobilepalengke.DataClasses.Product;
import com.example.mobilepalengke.R;

import androidx.core.content.ContextCompat;

public class ProductDialog {

    TextView textView, tvErrorCaption, tvDescriptions, btnManage, tvCategories, btnManage2;
    EditText etName, etImageLink, etPrice;
    Button btnConfirm, btnCancel;

    String name, value, productId;

    Context context;
    Activity activity;
    Dialog dialog;

    public ProductDialog(Context context) {
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
        dialog.setContentView(R.layout.dialog_product_layout);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        textView = dialog.findViewById(R.id.textView);
        tvErrorCaption = dialog.findViewById(R.id.tvErrorCaption);
        etName = dialog.findViewById(R.id.etName);
        etImageLink = dialog.findViewById(R.id.etImageLink);
        etPrice = dialog.findViewById(R.id.etPrice);
        tvDescriptions = dialog.findViewById(R.id.tvDescriptions);
        btnManage = dialog.findViewById(R.id.btnManage);
        tvCategories = dialog.findViewById(R.id.tvCategories);
        btnManage2 = dialog.findViewById(R.id.btnManage2);
        btnConfirm = dialog.findViewById(R.id.btnConfirm);
        btnCancel = dialog.findViewById(R.id.btnCancel);

        etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                name = editable.toString().trim();

                if (name.length() > 1) {
                    etName.setBackgroundResource(R.drawable.et_bg_default);
                    etName.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(context, R.drawable.ic_label_focused),
                            null, null, null);

                    tvErrorCaption.setVisibility(View.GONE);
                } else {
                    etName.setBackgroundResource(R.drawable.et_bg_error);
                    etName.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(context, R.drawable.ic_label_red),
                            null, null, null);

                    tvErrorCaption.setText(context.getString(R.string.invalidLabel));
                    tvErrorCaption.setVisibility(View.VISIBLE);
                }
            }
        });

        btnConfirm.setOnClickListener(view -> {
            boolean isInvalidLabel = name == null ||
                    name.trim().length() < 2 || name.trim().length() > 16;
            boolean isInvalidAddress = value == null || value.trim().length() < 2;

            if (isInvalidLabel) {
                etName.setBackgroundResource(R.drawable.et_bg_error);
                etName.setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat.getDrawable(context, R.drawable.ic_label_red),
                        null, null, null);
            }
            if (isInvalidAddress) {
                etImageLink.setBackgroundResource(R.drawable.et_bg_error);
                etImageLink.setCompoundDrawablesWithIntrinsicBounds(
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

            if (dialogListener != null)
                dialogListener.onConfirm(productId, name, value);
        });

        btnCancel.setOnClickListener(view -> dismissDialog());
    }

    private void setDialogWindow() {
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(activity, R.drawable.bg_corner));
    }

    public void showDialog() {
        dialog.show();

        productId = null;

        etName.getText().clear();
        etImageLink.getText().clear();

        etName.setBackgroundResource(R.drawable.et_bg_default);
        etName.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(context, R.drawable.ic_label_focused),
                null, null, null);

        etImageLink.setBackgroundResource(R.drawable.et_bg_default);
        etImageLink.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(context, R.drawable.ic_share_location_focused),
                null, null, null);

        tvErrorCaption.setVisibility(View.GONE);

        textView.setText(context.getString(R.string.addAddress));
    }

    public void dismissDialog() {
        dialog.dismiss();
    }

    public void setData(Product product) {
        this.productId = product.getId();

        etName.setText(product.getName());
        etImageLink.setText(product.getImg());
        etPrice.setText(String.valueOf(product.getPrice()));

        textView.setText(context.getString(R.string.updateProduct));
    }

    DialogListener dialogListener;

    public interface DialogListener {
        void onConfirm(String productId, String name, String value);
    }

    public void setDialogListener(DialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }
}
