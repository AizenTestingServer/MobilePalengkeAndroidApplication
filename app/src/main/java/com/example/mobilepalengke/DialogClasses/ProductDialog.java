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

import java.util.Map;

import androidx.core.content.ContextCompat;

public class ProductDialog {

    TextView textView, tvErrorCaption, tvDescriptions, btnManage, tvCategories, btnManage2;
    EditText etName, etImageLink, etPrice;
    Button btnConfirm, btnCancel;

    String productId, name, img;
    double price = 0;
    Map<String, String> categories, descriptions;

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

                    tvErrorCaption.setText(context.getString(R.string.invalidProductName));
                    tvErrorCaption.setVisibility(View.VISIBLE);
                }
            }
        });

        etImageLink.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                img = editable.toString();
            }
        });

        etPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                price = editable.toString().length() > 0 ? Double.parseDouble(editable.toString()) : 0;

                if (price >= 10) {
                    etPrice.setBackgroundResource(R.drawable.et_bg_default);
                    etPrice.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(context, R.drawable.ic_price_change_focused),
                            null, null, null);

                    tvErrorCaption.setVisibility(View.GONE);
                } else {
                    etPrice.setBackgroundResource(R.drawable.et_bg_error);
                    etPrice.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(context, R.drawable.ic_price_change_red),
                            null, null, null);

                    tvErrorCaption.setText(context.getString(R.string.invalidPrice));
                    tvErrorCaption.setVisibility(View.VISIBLE);
                }
            }
        });

        btnConfirm.setOnClickListener(view -> {
            boolean isInvalidProductName = name == null || name.trim().length() < 2;
            boolean isInvalidPrice = price == 0 || price < 10;

            if (isInvalidProductName) {
                etName.setBackgroundResource(R.drawable.et_bg_error);
                etName.setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat.getDrawable(context, R.drawable.ic_label_red),
                        null, null, null);
            }

            if (isInvalidProductName || isInvalidPrice) {
                if (isInvalidProductName)
                    tvErrorCaption.setText(context.getString(R.string.invalidProductName));
                if (isInvalidPrice)
                    tvErrorCaption.setText(context.getString(R.string.invalidPrice));

                tvErrorCaption.setVisibility(View.VISIBLE);
                return;
            }

            Product product = new Product(productId, name, img, categories, descriptions, price);

            if (dialogListener != null)
                dialogListener.onConfirm(product);
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
        etPrice.getText().clear();

        etName.setBackgroundResource(R.drawable.et_bg_default);
        etName.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(context, R.drawable.ic_label_focused),
                null, null, null);

        etImageLink.setBackgroundResource(R.drawable.et_bg_default);
        etImageLink.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(context, R.drawable.ic_link_focused),
                null, null, null);

        etPrice.setBackgroundResource(R.drawable.et_bg_default);
        etPrice.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(context, R.drawable.ic_price_change_focused),
                null, null, null);

        categories = null;
        descriptions = null;

        tvErrorCaption.setVisibility(View.GONE);

        textView.setText(context.getString(R.string.addProduct));
    }

    public void dismissDialog() {
        dialog.dismiss();
    }

    public void setData(Product product) {
        productId = product.getId();

        etName.setText(product.getName());
        etImageLink.setText(product.getImg());
        etPrice.setText(String.valueOf(product.getPrice()));
        tvDescriptions.setText(context.getString(R.string.qtyProductDescriptionValue,
                product.getDescriptions().size(),
                product.getDescriptions().size() > 1 ? "s" : ""));
        tvCategories.setText(context.getString(R.string.qtyProductCategoriesValue,
                product.getCategories().size(),
                product.getCategories().size() > 1 ? "ies" : "y"));

        categories = product.getCategories();
        descriptions = product.getDescriptions();

        textView.setText(context.getString(R.string.updateProduct));
    }

    DialogListener dialogListener;

    public interface DialogListener {
        void onConfirm(Product product);
    }

    public void setDialogListener(DialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }
}
