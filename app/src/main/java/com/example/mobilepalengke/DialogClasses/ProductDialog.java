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

import com.example.mobilepalengke.DataClasses.CheckableItem;
import com.example.mobilepalengke.DataClasses.Product;
import com.example.mobilepalengke.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

public class ProductDialog {

    private TextView textView, tvErrorCaption, tvDescriptions, tvCategories;
    private EditText etName, etImageLink, etPrice;
    SwitchCompat switch1;

    private final Context context;
    private final Activity activity;
    private Dialog dialog;

    private StringValuesDialog stringValuesDialog;
    private ProductCategoriesDialog productCategoriesDialog;

    private String productId, name, img;
    private double price = 0;
    private Map<String, String> categories = new HashMap<>(), descriptions = new HashMap<>();
    private boolean isDeactivated;

    List<CheckableItem> productCategoriesCheckableItems = new ArrayList<>(), selectedProductCategoriesCheckableItems = new ArrayList<>();

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

        textView = dialog.findViewById(R.id.textView);
        tvErrorCaption = dialog.findViewById(R.id.tvErrorCaption);
        etName = dialog.findViewById(R.id.etName);
        etImageLink = dialog.findViewById(R.id.etImageLink);
        etPrice = dialog.findViewById(R.id.etPrice);
        tvDescriptions = dialog.findViewById(R.id.tvDescriptions);
        TextView btnManage = dialog.findViewById(R.id.btnManage);
        tvCategories = dialog.findViewById(R.id.tvCategories);
        TextView btnManage2 = dialog.findViewById(R.id.btnManage2);
        switch1 = dialog.findViewById(R.id.switch1);
        Button btnConfirm = dialog.findViewById(R.id.btnConfirm);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        stringValuesDialog = new StringValuesDialog(context);
        productCategoriesDialog = new ProductCategoriesDialog(context);

        btnManage.setOnClickListener(view -> {
            stringValuesDialog.showDialog(context.getString(R.string.descriptions));
            stringValuesDialog.setData(descriptions);
        });

        stringValuesDialog.setDialogListener(mapValues -> {
            descriptions.clear();
            mapValues.forEach((s, s2) -> descriptions.put("desc" + s, s2));

            checkData();

            stringValuesDialog.dismissDialog();
        });

        btnManage2.setOnClickListener(view -> {
            productCategoriesDialog.showDialog();
            productCategoriesDialog.setCheckableItems(productCategoriesCheckableItems, selectedProductCategoriesCheckableItems);
        });

        productCategoriesDialog.setDialogListener((mapSelectedProductCategories, selectedCheckableItems) -> {
            categories.clear();
            categories.putAll(mapSelectedProductCategories);

            selectedProductCategoriesCheckableItems.clear();
            selectedProductCategoriesCheckableItems.addAll(selectedCheckableItems);

            checkData();

            productCategoriesDialog.dismissDialog();
        });

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

        switch1.setOnClickListener(view -> isDeactivated = switch1.isChecked());

        btnConfirm.setOnClickListener(view -> {
            boolean isInvalidProductName = name == null || name.trim().length() < 2;
            boolean isInvalidImgLink = img == null || img.trim().length() < 1;
            boolean isInvalidPrice = price < 10;

            if (isInvalidProductName) {
                etName.setBackgroundResource(R.drawable.et_bg_error);
                etName.setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat.getDrawable(context, R.drawable.ic_label_red),
                        null, null, null);
            }

            if (isInvalidImgLink) img = "None";

            if (isInvalidPrice) {
                etPrice.setBackgroundResource(R.drawable.et_bg_error);
                etPrice.setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat.getDrawable(context, R.drawable.ic_price_change_red),
                        null, null, null);
            }

            if (isInvalidProductName || isInvalidPrice) {
                if (isInvalidProductName)
                    tvErrorCaption.setText(context.getString(R.string.invalidProductName));
                else tvErrorCaption.setText(context.getString(R.string.invalidPrice));

                tvErrorCaption.setVisibility(View.VISIBLE);
                return;
            }

            Product product = new Product(productId, name, img, categories, descriptions, price);
            product.setDeactivated(isDeactivated);

            if (dialogListener != null)
                dialogListener.onConfirm(product);
        });

        btnCancel.setOnClickListener(view -> dismissDialog());
    }

    private void setDialogWindow() {
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(activity, R.drawable.bg_corner));
    }

    public void showDialog(List<CheckableItem> productCategoriesCheckableItems) {
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

        categories = new HashMap<>();
        descriptions = new HashMap<>();

        checkData();

        this.productCategoriesCheckableItems.clear();
        this.productCategoriesCheckableItems.addAll(productCategoriesCheckableItems);

        selectedProductCategoriesCheckableItems.clear();

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

        descriptions = product.getDescriptions();
        categories = product.getCategories();

        if (descriptions == null) descriptions = new HashMap<>();
        if (categories == null) categories = new HashMap<>();

        for (Map.Entry<String, String> mapCategories : categories.entrySet())
            selectedProductCategoriesCheckableItems.add(new CheckableItem(mapCategories.getValue(), null));

        checkData();

        switch1.setChecked(product.isDeactivated());

        textView.setText(context.getString(R.string.updateProduct));
    }

    private void checkData() {
        if (descriptions.size() == 0)
            tvDescriptions.setText(context.getString(R.string.noDescription));
        else
            tvDescriptions.setText(context.getString(R.string.qtyProdDescValue,
                    descriptions.size(), descriptions.size() > 1 ? "s" : ""));

        if (categories.size() == 0)
            tvCategories.setText(context.getString(R.string.noCategory));
        else
            tvCategories.setText(context.getString(R.string.qtyProdCatValue,
                    categories.size(), categories.size() > 1 ? "ies" : "y"));
    }

    DialogListener dialogListener;

    public interface DialogListener {
        void onConfirm(Product product);
    }

    public void setDialogListener(DialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }
}
