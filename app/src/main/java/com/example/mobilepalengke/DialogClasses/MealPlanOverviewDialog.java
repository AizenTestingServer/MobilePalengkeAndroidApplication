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

import com.example.mobilepalengke.DataClasses.MealPlan;
import com.example.mobilepalengke.R;

import androidx.core.content.ContextCompat;

public class MealPlanOverviewDialog {

    private TextView textView, tvErrorCaption;
    private EditText etPrepTime, etCookTime, etServings;

    private final Context context;
    private final Activity activity;
    private Dialog dialog;

    private String mealPlanId;
    private int prepTime, cookTime, servings;

    private MealPlan currentMealPlan;

    public MealPlanOverviewDialog(Context context) {
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
        dialog.setContentView(R.layout.dialog_meal_plan_overview_layout);
        dialog.setCanceledOnTouchOutside(false);

        textView = dialog.findViewById(R.id.textView);
        tvErrorCaption = dialog.findViewById(R.id.tvErrorCaption);
        etPrepTime = dialog.findViewById(R.id.etPrepTime);
        etCookTime = dialog.findViewById(R.id.etCookTime);
        etServings = dialog.findViewById(R.id.etServings);
        Button btnConfirm = dialog.findViewById(R.id.btnConfirm);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        etPrepTime.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                prepTime = editable.toString().length() > 0 ? Integer.parseInt(editable.toString()) : 0;

                if (prepTime > 0) {
                    etPrepTime.setBackgroundResource(R.drawable.et_bg_default);
                    etPrepTime.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(context, R.drawable.ic_access_time_focused),
                            null, null, null);

                    tvErrorCaption.setVisibility(View.GONE);
                } else {
                    etPrepTime.setBackgroundResource(R.drawable.et_bg_error);
                    etPrepTime.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(context, R.drawable.ic_access_time_red),
                            null, null, null);

                    tvErrorCaption.setText(context.getString(R.string.invalidOverview));

                    tvErrorCaption.setVisibility(View.VISIBLE);
                }
            }
        });

        etCookTime.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                cookTime = editable.toString().length() > 0 ? Integer.parseInt(editable.toString()) : 0;

                if (cookTime > 0) {
                    etCookTime.setBackgroundResource(R.drawable.et_bg_default);
                    etCookTime.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(context, R.drawable.ic_access_time_focused),
                            null, null, null);

                    tvErrorCaption.setVisibility(View.GONE);
                } else {
                    etCookTime.setBackgroundResource(R.drawable.et_bg_error);
                    etCookTime.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(context, R.drawable.ic_access_time_red),
                            null, null, null);

                    tvErrorCaption.setText(context.getString(R.string.invalidOverview));

                    tvErrorCaption.setVisibility(View.VISIBLE);
                }
            }
        });

        etServings.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                servings = editable.toString().length() > 0 ? Integer.parseInt(editable.toString()) : 0;

                if (servings > 0) {
                    etServings.setBackgroundResource(R.drawable.et_bg_default);
                    etServings.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(context, R.drawable.ic_groups_focused),
                            null, null, null);

                    tvErrorCaption.setVisibility(View.GONE);
                } else {
                    etServings.setBackgroundResource(R.drawable.et_bg_error);
                    etServings.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(context, R.drawable.ic_groups_red),
                            null, null, null);

                    tvErrorCaption.setText(context.getString(R.string.invalidOverview));

                    tvErrorCaption.setVisibility(View.VISIBLE);
                }
            }
        });

        btnConfirm.setOnClickListener(view -> {
            boolean isInvalidPrepTime = prepTime <= 0;
            boolean isInvalidCookTime = cookTime <= 0;
            boolean isInvalidServings = servings <= 0;

            if (isInvalidPrepTime) {
                etPrepTime.setBackgroundResource(R.drawable.et_bg_error);
                etPrepTime.setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat.getDrawable(context, R.drawable.ic_access_time_red),
                        null, null, null);
            }

            if (isInvalidCookTime) {
                etCookTime.setBackgroundResource(R.drawable.et_bg_error);
                etCookTime.setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat.getDrawable(context, R.drawable.ic_access_time_red),
                        null, null, null);
            }

            if (isInvalidServings) {
                etServings.setBackgroundResource(R.drawable.et_bg_error);
                etServings.setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat.getDrawable(context, R.drawable.ic_groups_red),
                        null, null, null);
            }

            if (isInvalidPrepTime || isInvalidCookTime || isInvalidServings) {
                tvErrorCaption.setText(context.getString(R.string.invalidOverview));
                return;
            }

            currentMealPlan.setPrepTime(prepTime);
            currentMealPlan.setCookTime(cookTime);
            currentMealPlan.setServings(servings);

            if (dialogListener != null)
                dialogListener.onConfirm(currentMealPlan);
        });

        btnCancel.setOnClickListener(view -> dismissDialog());
    }

    private void setDialogWindow() {
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(activity, R.drawable.bg_corner));
    }

    public void showDialog() {
        dialog.show();

        mealPlanId = null;

        etPrepTime.getText().clear();
        etCookTime.getText().clear();
        etServings.getText().clear();

        etPrepTime.setBackgroundResource(R.drawable.et_bg_default);
        etPrepTime.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(context, R.drawable.ic_label_focused),
                null, null, null);

        etServings.setBackgroundResource(R.drawable.et_bg_default);
        etServings.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(context, R.drawable.ic_chat_bubble_focused),
                null, null, null);

        etCookTime.setBackgroundResource(R.drawable.et_bg_default);
        etCookTime.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(context, R.drawable.ic_link_focused),
                null, null, null);

        tvErrorCaption.setVisibility(View.GONE);

        textView.setText(context.getString(R.string.addMealPlan));
    }

    public void dismissDialog() {
        dialog.dismiss();
    }

    public void setData(MealPlan mealPlan) {
        mealPlanId = mealPlan.getId();

        this.currentMealPlan = mealPlan;

        etPrepTime.setText(String.valueOf(mealPlan.getPrepTime()));
        etCookTime.setText(String.valueOf(mealPlan.getCookTime()));
        etServings.setText(String.valueOf(mealPlan.getServings()));

        textView.setText(context.getString(R.string.btnUpdateOverview));
    }

    DialogListener dialogListener;

    public interface DialogListener {
        void onConfirm(MealPlan mealPlan);
    }

    public void setDialogListener(DialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }
}
