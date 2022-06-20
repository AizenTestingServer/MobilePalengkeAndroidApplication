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

public class ChangePasswordDialog {

    private TextView tvErrorCaption;
    private EditText etPassword, etPassword2, etPassword3;

    private final Context context;
    private final Activity activity;
    private Dialog dialog;

    private String password, confirmPassword, currentPassword, currentPassword2;

    public ChangePasswordDialog(Context context) {
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
        dialog.setContentView(R.layout.dialog_change_password_layout);
        dialog.setCanceledOnTouchOutside(false);

        tvErrorCaption = dialog.findViewById(R.id.tvErrorCaption);
        etPassword = dialog.findViewById(R.id.etPassword);
        etPassword2 = dialog.findViewById(R.id.etPassword2);
        etPassword3 = dialog.findViewById(R.id.etPassword3);
        Button btnConfirm = dialog.findViewById(R.id.btnConfirm);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                checkPasswordInput(1);
            }
        });

        etPassword2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                checkPasswordInput(2);
            }
        });

        etPassword3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                checkPasswordInput(3);
            }
        });

        btnConfirm.setOnClickListener(view -> {
            boolean isPasswordInvalidLen = password == null || password.length() < 6;
            boolean isPasswordInvalidChar = password == null || !password.matches("[A-Za-z0-9]*");

            boolean isConPasswordInvalidLen = confirmPassword == null ||  confirmPassword.length() < 1;
            boolean isConPasswordNotEqual = confirmPassword == null || !confirmPassword.equals(password);

            boolean isCurPasswordInvalidLen = currentPassword == null || currentPassword.length() < 1;
            boolean isCurPasswordNotEqual = currentPassword == null || !currentPassword.equals(currentPassword2);

            if (isPasswordInvalidLen || isPasswordInvalidChar) {
                etPassword.setBackgroundResource(R.drawable.et_bg_error);
                etPassword.setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat.getDrawable(context, R.drawable.ic_lock_red),
                        null, null, null);

                if (isPasswordInvalidLen)
                    tvErrorCaption.setText(context.getString(R.string.invalidPassword));
                else tvErrorCaption.setText(context.getString(R.string.invalidPassword2));

                if (isConPasswordInvalidLen || isConPasswordNotEqual) {
                    etPassword2.setBackgroundResource(R.drawable.et_bg_error);
                    etPassword2.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(context, R.drawable.ic_lock_red),
                            null, null, null);
                }

                if (isCurPasswordInvalidLen || isCurPasswordNotEqual) {
                    etPassword3.setBackgroundResource(R.drawable.et_bg_error);
                    etPassword3.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(context, R.drawable.ic_lock_red),
                            null, null, null);
                }

                tvErrorCaption.setVisibility(View.VISIBLE);

                return;
            } else if (isConPasswordInvalidLen || isConPasswordNotEqual) {
                etPassword2.setBackgroundResource(R.drawable.et_bg_error);
                etPassword2.setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat.getDrawable(context, R.drawable.ic_lock_red),
                        null, null, null);

                if (isConPasswordInvalidLen)
                    tvErrorCaption.setText(context.getString(R.string.reenterPassword));
                else tvErrorCaption.setText(context.getString(R.string.noMatchPassword));

                if (isCurPasswordInvalidLen || isCurPasswordNotEqual) {
                    etPassword3.setBackgroundResource(R.drawable.et_bg_error);
                    etPassword3.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(context, R.drawable.ic_lock_red),
                            null, null, null);
                }

                tvErrorCaption.setVisibility(View.VISIBLE);

                return;
            } else if (isCurPasswordInvalidLen || isCurPasswordNotEqual) {
                etPassword3.setBackgroundResource(R.drawable.et_bg_error);
                etPassword3.setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat.getDrawable(context, R.drawable.ic_lock_red),
                        null, null, null);

                if (isCurPasswordInvalidLen)
                    tvErrorCaption.setText(context.getString(R.string.enterCurrentPassword));
                else tvErrorCaption.setText(context.getString(R.string.noMatchPassword2));

                tvErrorCaption.setVisibility(View.VISIBLE);

                return;
            }

            if (dialogListener != null) dialogListener.onConfirm(password);
        });

        btnCancel.setOnClickListener(view -> dismissDialog());
    }

    private void setDialogWindow() {
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(activity, R.drawable.bg_corner));
    }

    public void showDialog() {
        dialog.show();

        etPassword.getText().clear();
        etPassword2.getText().clear();
        etPassword3.getText().clear();

        etPassword.setBackgroundResource(R.drawable.et_bg_default);
        etPassword.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(context, R.drawable.ic_lock_focused),
                null, null, null);

        etPassword2.setBackgroundResource(R.drawable.et_bg_default);
        etPassword2.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(context, R.drawable.ic_lock_focused),
                null, null, null);

        etPassword3.setBackgroundResource(R.drawable.et_bg_default);
        etPassword3.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(context, R.drawable.ic_lock_focused),
                null, null, null);

        tvErrorCaption.setVisibility(View.GONE);
    }

    public void dismissDialog() {
        dialog.dismiss();
    }

    private void checkPasswordInput(int sender) {
        password = etPassword.getText().toString();
        confirmPassword = etPassword2.getText().toString();
        currentPassword = etPassword3.getText().toString();

        boolean isPasswordValidLen = password != null && password.length() > 5;
        boolean isPasswordValidChar = password != null && password.matches("[A-Za-z0-9]*");

        boolean isPasswordInvalidLen = password == null || password.length() < 6;

        boolean isConPasswordValidLen = confirmPassword != null && confirmPassword.length() > 0;
        boolean isConPasswordEqual = confirmPassword != null && confirmPassword.equals(password);

        boolean isCurPasswordValidLen = currentPassword != null && currentPassword.length() > 0;
        boolean isCurPasswordEqual = currentPassword != null && currentPassword.equals(currentPassword2);

        switch(sender) {
            case 1:
                if (isPasswordValidLen && isPasswordValidChar) {
                    if (isConPasswordValidLen) {
                        if(confirmPassword.equals(password)) {
                            etPassword.setBackgroundResource(R.drawable.et_bg_default);
                            etPassword.setCompoundDrawablesWithIntrinsicBounds(
                                    ContextCompat.getDrawable(context, R.drawable.ic_lock_focused),
                                    null, null, null);

                            etPassword2.setBackgroundResource(R.drawable.et_bg_default);
                            etPassword2.setCompoundDrawablesWithIntrinsicBounds(
                                    ContextCompat.getDrawable(context, R.drawable.ic_lock_focused),
                                    null, null, null);

                            tvErrorCaption.setVisibility(View.GONE);
                        } else {
                            etPassword.setBackgroundResource(R.drawable.et_bg_error);
                            etPassword.setCompoundDrawablesWithIntrinsicBounds(
                                    ContextCompat.getDrawable(context, R.drawable.ic_lock_red),
                                    null, null, null);

                            etPassword2.setBackgroundResource(R.drawable.et_bg_error);
                            etPassword2.setCompoundDrawablesWithIntrinsicBounds(
                                    ContextCompat.getDrawable(context, R.drawable.ic_lock_red),
                                    null, null, null);

                            tvErrorCaption.setText(context.getString(R.string.noMatchPassword));
                            tvErrorCaption.setVisibility(View.VISIBLE);
                        }
                    } else {
                        etPassword.setBackgroundResource(R.drawable.et_bg_default);
                        etPassword.setCompoundDrawablesWithIntrinsicBounds(
                                ContextCompat.getDrawable(context, R.drawable.ic_lock_focused),
                                null, null, null);

                        tvErrorCaption.setVisibility(View.GONE);
                    }
                } else {
                    etPassword.setBackgroundResource(R.drawable.et_bg_error);
                    etPassword.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(context, R.drawable.ic_lock_red),
                            null, null, null);

                    if (isPasswordInvalidLen)
                        tvErrorCaption.setText(context.getString(R.string.invalidPassword));
                    else tvErrorCaption.setText(context.getString(R.string.invalidPassword2));

                    tvErrorCaption.setVisibility(View.VISIBLE);
                }

                break;
            case 2:
                if (isConPasswordValidLen) {
                    if (isConPasswordEqual) {
                        etPassword2.setBackgroundResource(R.drawable.et_bg_default);
                        etPassword2.setCompoundDrawablesWithIntrinsicBounds(
                                ContextCompat.getDrawable(context, R.drawable.ic_lock_focused),
                                null, null, null);

                        tvErrorCaption.setVisibility(View.GONE);
                    } else {
                        etPassword2.setBackgroundResource(R.drawable.et_bg_error);
                        etPassword2.setCompoundDrawablesWithIntrinsicBounds(
                                ContextCompat.getDrawable(context, R.drawable.ic_lock_red),
                                null, null, null);

                        tvErrorCaption.setText(context.getString(R.string.noMatchPassword));
                        tvErrorCaption.setVisibility(View.VISIBLE);
                    }
                } else {
                    etPassword2.setBackgroundResource(R.drawable.et_bg_error);
                    etPassword2.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(context, R.drawable.ic_lock_red),
                            null, null, null);

                    tvErrorCaption.setText(context.getString(R.string.reenterPassword));
                    tvErrorCaption.setVisibility(View.VISIBLE);
                }

                checkPasswordInput(1);

                break;
            case 3:
                if (isCurPasswordValidLen) {
                    if (isCurPasswordEqual) {
                        etPassword3.setBackgroundResource(R.drawable.et_bg_default);
                        etPassword3.setCompoundDrawablesWithIntrinsicBounds(
                                ContextCompat.getDrawable(context, R.drawable.ic_lock_focused),
                                null, null, null);

                        tvErrorCaption.setVisibility(View.GONE);
                    } else {
                        etPassword3.setBackgroundResource(R.drawable.et_bg_error);
                        etPassword3.setCompoundDrawablesWithIntrinsicBounds(
                                ContextCompat.getDrawable(context, R.drawable.ic_lock_red),
                                null, null, null);

                        tvErrorCaption.setText(context.getString(R.string.noMatchPassword2));
                        tvErrorCaption.setVisibility(View.VISIBLE);
                    }
                } else {
                    etPassword3.setBackgroundResource(R.drawable.et_bg_error);
                    etPassword3.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(context, R.drawable.ic_lock_red),
                            null, null, null);

                    tvErrorCaption.setText(context.getString(R.string.enterCurrentPassword));
                    tvErrorCaption.setVisibility(View.VISIBLE);
                }

                break;
        }
    }

    public void setCurrentPassword2(String currentPassword2) {
        this.currentPassword2 = currentPassword2;
    }

    DialogListener dialogListener;

    public interface DialogListener {
        void onConfirm(String password);
    }

    public void setDialogListener(DialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }
}
