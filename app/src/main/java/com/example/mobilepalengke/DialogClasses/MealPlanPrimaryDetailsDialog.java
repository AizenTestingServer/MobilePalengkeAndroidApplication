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
import com.example.mobilepalengke.DataClasses.MealPlan;
import com.example.mobilepalengke.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

public class MealPlanPrimaryDetailsDialog {

    private TextView textView, textView2, tvErrorCaption, tvCategories;
    private EditText etName, etImageLink, etDescription;
    SwitchCompat switch1;

    private final Context context;
    private final Activity activity;
    private Dialog dialog;

    private MealPlanCategoriesDialog mealPlanCategoriesDialog;

    private String name, description, img;
    private Map<String, String> categories = new HashMap<>();
    private boolean isDeactivated;

    private MealPlan currentMealPlan = new MealPlan();

    List<CheckableItem> mealPlanCategoriesCheckableItems = new ArrayList<>(), selectedMealPlanCategoriesCheckableItems = new ArrayList<>();

    public MealPlanPrimaryDetailsDialog(Context context) {
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
        dialog.setContentView(R.layout.dialog_meal_plan_primary_details_layout);
        dialog.setCanceledOnTouchOutside(false);

        textView = dialog.findViewById(R.id.textView);
        textView2 = dialog.findViewById(R.id.textView2);
        tvErrorCaption = dialog.findViewById(R.id.tvErrorCaption);
        etName = dialog.findViewById(R.id.etName);
        etImageLink = dialog.findViewById(R.id.etImageLink);
        etDescription = dialog.findViewById(R.id.etDescription);
        switch1 = dialog.findViewById(R.id.switch1);
        tvCategories = dialog.findViewById(R.id.tvCategories);
        TextView btnManage = dialog.findViewById(R.id.btnManage);
        Button btnConfirm = dialog.findViewById(R.id.btnConfirm);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        mealPlanCategoriesDialog = new MealPlanCategoriesDialog(context);

        btnManage.setOnClickListener(view -> {
            mealPlanCategoriesDialog.showDialog();
            mealPlanCategoriesDialog.setCheckableItems(mealPlanCategoriesCheckableItems, selectedMealPlanCategoriesCheckableItems);
        });

        mealPlanCategoriesDialog.setDialogListener((mapSelectedProductCategories, selectedCheckableItems) -> {
            categories.clear();
            categories.putAll(mapSelectedProductCategories);

            selectedMealPlanCategoriesCheckableItems.clear();
            selectedMealPlanCategoriesCheckableItems.addAll(selectedCheckableItems);

            checkData();

            mealPlanCategoriesDialog.dismissDialog();
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

        etDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                description = editable.toString();
            }
        });

        switch1.setOnClickListener(view -> isDeactivated = switch1.isChecked());

        btnConfirm.setOnClickListener(view -> {
            boolean isInvalidProductName = name == null || name.trim().length() < 2;
            boolean isDecsriptionEmpty = description == null;
            boolean isInvalidImgLink = img == null || img.trim().length() < 1;

            if (isInvalidProductName) {
                etName.setBackgroundResource(R.drawable.et_bg_error);
                etName.setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat.getDrawable(context, R.drawable.ic_label_red),
                        null, null, null);
            }

            if (isDecsriptionEmpty) description = "";

            if (isInvalidImgLink) img = "None";

            if (isInvalidProductName || isDecsriptionEmpty) {
                if (isInvalidProductName)
                    tvErrorCaption.setText(context.getString(R.string.invalidProductName));
                else tvErrorCaption.setText(context.getString(R.string.invalidPrice));

                tvErrorCaption.setVisibility(View.VISIBLE);
                return;
            }

            currentMealPlan.setName(name);
            currentMealPlan.setDescription(description);
            currentMealPlan.setImg(img);
            currentMealPlan.setDeactivated(isDeactivated);
            currentMealPlan.setCategories(categories);

            if (dialogListener != null)
                dialogListener.onConfirm(currentMealPlan);
        });

        btnCancel.setOnClickListener(view -> dismissDialog());
    }

    private void setDialogWindow() {
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(activity, R.drawable.bg_corner));
    }

    public void showDialog(List<CheckableItem> mealPlanCategoriesCheckableItems) {
        dialog.show();

        etName.getText().clear();
        etImageLink.getText().clear();
        etDescription.getText().clear();

        etName.setBackgroundResource(R.drawable.et_bg_default);
        etName.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(context, R.drawable.ic_label_focused),
                null, null, null);

        etDescription.setBackgroundResource(R.drawable.et_bg_default);
        etDescription.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(context, R.drawable.ic_chat_bubble_focused),
                null, null, null);

        etImageLink.setBackgroundResource(R.drawable.et_bg_default);
        etImageLink.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(context, R.drawable.ic_link_focused),
                null, null, null);

        categories = new HashMap<>();

        checkData();

        this.mealPlanCategoriesCheckableItems.clear();
        this.mealPlanCategoriesCheckableItems.addAll(mealPlanCategoriesCheckableItems);

        tvErrorCaption.setVisibility(View.GONE);

        textView.setText(context.getString(R.string.addMealPlan));
        textView2.setText(context.getString(R.string.enterMealPlan));
    }

    public void dismissDialog() {
        dialog.dismiss();
    }

    public void setData(MealPlan mealPlan) {
        this.currentMealPlan = mealPlan;

        etName.setText(mealPlan.getName());
        etDescription.setText(String.valueOf(mealPlan.getDescription()));
        etImageLink.setText(mealPlan.getImg());

        categories = mealPlan.getCategories();

        if (categories == null) categories = new HashMap<>();

        for (Map.Entry<String, String> mapCategories : categories.entrySet())
            selectedMealPlanCategoriesCheckableItems.add(new CheckableItem(mapCategories.getValue(), null));

        checkData();

        switch1.setChecked(mealPlan.isDeactivated());

        textView.setText(context.getString(R.string.btnUpdatePrimaryDetails));
        textView2.setText(context.getString(R.string.btnUpdatePrimaryDetails));
    }

    private void checkData() {
        if (categories.size() == 0)
            tvCategories.setText(context.getString(R.string.noCategory));
        else
            tvCategories.setText(context.getString(R.string.qtyProdCatValue,
                    categories.size(), categories.size() > 1 ? "ies" : "y"));
    }

    DialogListener dialogListener;

    public interface DialogListener {
        void onConfirm(MealPlan mealPlan);
    }

    public void setDialogListener(DialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }
}
