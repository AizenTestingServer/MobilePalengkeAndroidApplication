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

import com.example.mobilepalengke.DataClasses.Role;
import com.example.mobilepalengke.DataClasses.RoleItem;
import com.example.mobilepalengke.R;

import androidx.core.content.ContextCompat;

public class RoleDialog {

    private TextView textView, tvErrorCaption, tvRoleType ;
    private EditText etRoleName, etLevel;

    private final Context context;
    private final Activity activity;
    private Dialog dialog;

    private RoleTypesDialog roleTypesDialog;

    private String roleId, roleName;
    private int level = 0, roleType = 0;

    private int currentLevel, prevRoleType;

    public RoleDialog(Context context) {
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
        dialog.setContentView(R.layout.dialog_role_layout);
        dialog.setCanceledOnTouchOutside(false);

        textView = dialog.findViewById(R.id.textView);
        tvErrorCaption = dialog.findViewById(R.id.tvErrorCaption);
        etRoleName = dialog.findViewById(R.id.etRoleName);
        etLevel = dialog.findViewById(R.id.etLevel);
        tvRoleType = dialog.findViewById(R.id.tvRoleType);
        TextView btnChange = dialog.findViewById(R.id.btnChange);
        Button btnConfirm = dialog.findViewById(R.id.btnConfirm);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        roleTypesDialog = new RoleTypesDialog(context);

        etRoleName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                roleName = editable.toString().trim();

                if (roleName.length() > 1) {
                    etRoleName.setBackgroundResource(R.drawable.et_bg_default);
                    etRoleName.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(context, R.drawable.ic_label_focused),
                            null, null, null);

                    tvErrorCaption.setVisibility(View.GONE);
                } else {
                    etRoleName.setBackgroundResource(R.drawable.et_bg_error);
                    etRoleName.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(context, R.drawable.ic_label_red),
                            null, null, null);

                    tvErrorCaption.setText(context.getString(R.string.invalidProductName));
                    tvErrorCaption.setVisibility(View.VISIBLE);
                }
            }
        });

        etLevel.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                level = editable.toString().length() > 0 ? Integer.parseInt(editable.toString()) : 0;

                if (level > 0 && level < currentLevel) {
                    etLevel.setBackgroundResource(R.drawable.et_bg_default);
                    etLevel.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(context, R.drawable.ic_123_focused),
                            null, null, null);

                    tvErrorCaption.setVisibility(View.GONE);
                } else {
                    etLevel.setBackgroundResource(R.drawable.et_bg_error);
                    etLevel.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(context, R.drawable.ic_123_red),
                            null, null, null);

                    if (level >= currentLevel)
                        tvErrorCaption.setText(context.getString(R.string.invalidLevelValue, currentLevel));
                    else
                        tvErrorCaption.setText(context.getString(R.string.enterLevel));

                    tvErrorCaption.setVisibility(View.VISIBLE);
                }
            }
        });

        btnChange.setOnClickListener(view -> {
            roleTypesDialog.showDialog();
            roleTypesDialog.setSelectedRoleType(prevRoleType);
        });

        roleTypesDialog.setDialogListener(selectedRoleType -> {
            roleType = selectedRoleType;

            switch (roleType) {
                case 1:
                    tvRoleType.setText(context.getString(R.string.admin));
                    break;
                case 2:
                    tvRoleType.setText(context.getString(R.string.special));
                    break;
                default:
                    tvRoleType.setText(context.getString(R.string.normal));
                    break;
            }

            roleTypesDialog.dismissDialog();
        });

        btnConfirm.setOnClickListener(view -> {
            boolean isInvalidRoleName = roleName == null || roleName.trim().length() < 2;
            boolean isInvalidLevel = level >= currentLevel ;
            boolean isLevelEmpty = level <= 0;

            if (isInvalidRoleName) {
                etRoleName.setBackgroundResource(R.drawable.et_bg_error);
                etRoleName.setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat.getDrawable(context, R.drawable.ic_label_red),
                        null, null, null);
            }

            if (isInvalidRoleName || isInvalidLevel || isLevelEmpty) {
                if (isInvalidRoleName)
                    tvErrorCaption.setText(context.getString(R.string.enterRoleName));
                else if (isInvalidLevel)
                    tvErrorCaption.setText(context.getString(R.string.invalidLevelValue, currentLevel));
                else
                    tvErrorCaption.setText(context.getString(R.string.enterLevel));

                tvErrorCaption.setVisibility(View.VISIBLE);
                return;
            }

            Role role = new Role(roleId, roleName, level);

            if (dialogListener != null)
                dialogListener.onConfirm(role, roleType, prevRoleType);
        });

        btnCancel.setOnClickListener(view -> dismissDialog());
    }

    private void setDialogWindow() {
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(activity, R.drawable.bg_corner));
    }

    public void showDialog() {
        dialog.show();

        roleId = null;

        etRoleName.getText().clear();
        etLevel.getText().clear();

        roleType = 0;

        tvRoleType.setText(context.getString(R.string.normal));

        etRoleName.setBackgroundResource(R.drawable.et_bg_default);
        etRoleName.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(context, R.drawable.ic_label_focused),
                null, null, null);

        etLevel.setBackgroundResource(R.drawable.et_bg_default);
        etLevel.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(context, R.drawable.ic_123_focused),
                null, null, null);

        prevRoleType = 0;

        tvErrorCaption.setVisibility(View.GONE);

        textView.setText(context.getString(R.string.addRole));
    }

    public void dismissDialog() {
        dialog.dismiss();
    }

    public void setData(RoleItem roleItem) {
        roleId = roleItem.getId();

        etRoleName.setText(roleItem.getName());
        etLevel.setText(String.valueOf(roleItem.getLevel()));

        String roleType = roleItem.getType();

        tvRoleType.setText(roleType);

        if (roleType.equals(context.getString(R.string.normal)))
            this.roleType = 0;
        else if (roleType.equals(context.getString(R.string.admin)))
            this.roleType = 1;
        else if (roleType.equals(context.getString(R.string.special)))
            this.roleType = 2;

        prevRoleType = this.roleType;

        textView.setText(context.getString(R.string.updateRole));
    }

    public void setCurrentLevel(int currentLevel) {
        this.currentLevel = currentLevel;
    }

    DialogListener dialogListener;

    public interface DialogListener {
        void onConfirm(Role role, int roleType, int prevRoleType);
    }

    public void setDialogListener(DialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }
}
