package com.example.mobilepalengke.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mobilepalengke.Classes.Credentials;
import com.example.mobilepalengke.DataClasses.Role;
import com.example.mobilepalengke.DataClasses.Roles;
import com.example.mobilepalengke.DataClasses.User;
import com.example.mobilepalengke.DialogClasses.LoadingDialog;
import com.example.mobilepalengke.DialogClasses.MessageDialog;
import com.example.mobilepalengke.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

public class WelcomeScreenActivity extends AppCompatActivity {

    CardView welcomeCardView, signInCardView, forgotPasswordCardView, signUpCardView;
    ConstraintLayout constraintLayout1, constraintLayout2, constraintLayout3, btnNextLayout;
    EditText etEmail, etPassword, etLastName, etFirstName, etEmail2, etPassword2, etPassword3, etEmail3;
    TextView tvForgotPassword, tvErrorCaption, tvErrorCaption2, tvSignUp2, tvSignUp3;
    Button btnSignIn, btnSignUp, btnConfirmEmail, btnSignIn2, btnSignUp2, btnNext;

    Context context;

    LoadingDialog loadingDialog;
    MessageDialog messageDialog;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;

    int currentStep = 0, maxStep = 3;

    String emailAddress, password, lastName, firstName, signUpEmailAddress, signUpPassword,
            confirmPassword, forgotPasswordEmailAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);

        welcomeCardView = findViewById(R.id.welcomeCardView);
        signInCardView = findViewById(R.id.signInCardView);
        forgotPasswordCardView = findViewById(R.id.forgotPasswordCardView);
        signUpCardView = findViewById(R.id.signUpCardView);

        constraintLayout1 = findViewById(R.id.constraintLayout1);
        constraintLayout2 = findViewById(R.id.constraintLayout2);
        constraintLayout3 = findViewById(R.id.constraintLayout3);
        btnNextLayout = findViewById(R.id.btnNextLayout);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etLastName = findViewById(R.id.etLastName);
        etFirstName = findViewById(R.id.etFirstName);
        etEmail2 = findViewById(R.id.etEmail2);
        etPassword2 = findViewById(R.id.etPassword2);
        etPassword3 = findViewById(R.id.etPassword3);
        etEmail3 = findViewById(R.id.etEmail3);

        tvForgotPassword = findViewById(R.id.tvFPw);
        tvErrorCaption = findViewById(R.id.tvErrorCaption);
        tvErrorCaption2 = findViewById(R.id.tvErrorCaption2);
        tvSignUp2 = findViewById(R.id.tvSignUp2);
        tvSignUp3 = findViewById(R.id.tvSignUp3);

        btnSignIn = findViewById(R.id.btnSignIn);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnConfirmEmail = findViewById(R.id.btnConfirmEmail);
        btnSignIn2 = findViewById(R.id.btnSignIn2);
        btnSignUp2 = findViewById(R.id.btnSignUp2);
        btnNext = findViewById(R.id.btnNext);

        context = WelcomeScreenActivity.this;

        loadingDialog = new LoadingDialog(context);
        messageDialog = new MessageDialog(context);

        if (getIntent().getBooleanExtra("isForgotPasswordLinkSent", false)) {
            messageDialog.setTextCaption("The link to reset your password has been sent to your email.");
            messageDialog.setTextType(1);
            messageDialog.showDialog();
        }

        if (getIntent().getBooleanExtra("isForcedSignOut", false)) {
            messageDialog.setTextCaption("There are changes in the sign in process,\nplease sign in again.");
            messageDialog.setTextType(1);
            messageDialog.showDialog();
        }

        if (getIntent().getBooleanExtra("isOnPasswordChange", false)) {
            messageDialog.setTextCaption("Please sign in again before changing your password.");
            messageDialog.setTextType(1);
            messageDialog.showDialog();
        }

        if (getIntent().getBooleanExtra("isPasswordChanged", false)) {
            messageDialog.setTextCaption("Successfully changed the password,\nplease sign in again.");
            messageDialog.setTextType(1);
            messageDialog.showDialog();
        }

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_RTDB_url));

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
                } else {
                    etEmail.setBackgroundResource(R.drawable.et_bg_error);
                    etEmail.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(context, R.drawable.ic_email_red),
                            null, null, null);
                }
            }
        });

        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                password = editable.toString();

                if (password.length() > 5 && password.matches("[A-Za-z0-9]*")) {
                    etPassword.setBackgroundResource(R.drawable.et_bg_default);
                    etPassword.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(context, R.drawable.ic_lock_focused),
                            null, null, null);
                } else {
                    etPassword.setBackgroundResource(R.drawable.et_bg_error);
                    etPassword.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(context, R.drawable.ic_lock_red),
                            null, null, null);
                }
            }
        });

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

                    tvErrorCaption.setText(getString(R.string.invalidName));
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

                    tvErrorCaption.setText(getString(R.string.invalidName));
                    tvErrorCaption.setVisibility(View.VISIBLE);
                }
            }
        });

        etEmail2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                signUpEmailAddress = editable.toString().trim();

                if (Credentials.isValidEmailAddress(signUpEmailAddress)) {
                    etEmail2.setBackgroundResource(R.drawable.et_bg_default);
                    etEmail2.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(context, R.drawable.ic_email_focused),
                            null, null, null);

                    tvErrorCaption.setVisibility(View.GONE);
                } else {
                    etEmail2.setBackgroundResource(R.drawable.et_bg_error);
                    etEmail2.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(context, R.drawable.ic_email_red),
                            null, null, null);

                    tvErrorCaption.setText(getString(R.string.invalidEmail));
                    tvErrorCaption.setVisibility(View.VISIBLE);
                }
            }
        });

        etEmail3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                forgotPasswordEmailAddress = editable.toString().trim();

                if (Credentials.isValidEmailAddress(forgotPasswordEmailAddress)) {
                    etEmail3.setBackgroundResource(R.drawable.et_bg_default);
                    etEmail3.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(context, R.drawable.ic_email_focused),
                            null, null, null);

                    tvErrorCaption2.setVisibility(View.GONE);
                } else {
                    etEmail3.setBackgroundResource(R.drawable.et_bg_error);
                    etEmail3.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(context, R.drawable.ic_email_red),
                            null, null, null);

                    tvErrorCaption2.setText(getString(R.string.invalidEmail));
                    tvErrorCaption2.setVisibility(View.VISIBLE);
                }
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
                checkPasswordInput(1);
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
                checkPasswordInput(2);
            }
        });

        tvForgotPassword.setOnClickListener(view -> {
            currentStep++;
            signInCardView.setVisibility(View.GONE);
            forgotPasswordCardView.setVisibility(View.VISIBLE);
        });

        btnSignIn.setOnClickListener(view -> {
            currentStep++;
            welcomeCardView.setVisibility(View.GONE);
            signInCardView.setVisibility(View.VISIBLE);
        });

        btnSignUp.setOnClickListener(view -> {
            currentStep++;
            welcomeCardView.setVisibility(View.GONE);
            signUpCardView.setVisibility(View.VISIBLE);
        });

        btnConfirmEmail.setOnClickListener(view -> {
            if (forgotPasswordEmailAddress == null || !Credentials.isValidEmailAddress(forgotPasswordEmailAddress)) {
                etEmail3.setBackgroundResource(R.drawable.et_bg_error);
                etEmail3.setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat.getDrawable(context, R.drawable.ic_email_red),
                        null, null, null);

                tvErrorCaption2.setText(getString(R.string.invalidEmail));
                tvErrorCaption2.setVisibility(View.VISIBLE);

                return;
            }

            checkEmailAddressRegistration();
        });

        btnNext.setOnClickListener(view -> {
            if (currentStep == 1) {
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
                    tvErrorCaption.setText(getString(R.string.invalidName));
                    tvErrorCaption.setVisibility(View.VISIBLE);
                    return;
                }
            } else if (currentStep == 2) {
                if (signUpEmailAddress == null || !Credentials.isValidEmailAddress(signUpEmailAddress)) {
                    etEmail2.setBackgroundResource(R.drawable.et_bg_error);
                    etEmail2.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(context, R.drawable.ic_email_red),
                            null, null, null);

                    tvErrorCaption.setText(getString(R.string.invalidEmail));
                    tvErrorCaption.setVisibility(View.VISIBLE);

                    return;
                }
            }

            if (currentStep < maxStep)
                currentStep++;

            if (currentStep == 2) {
                constraintLayout1.setVisibility(View.GONE);
                constraintLayout2.setVisibility(View.VISIBLE);
            } else if (currentStep == 3) {
                constraintLayout2.setVisibility(View.GONE);
                btnNextLayout.setVisibility(View.GONE);
                constraintLayout3.setVisibility(View.VISIBLE);
            }
        });

        tvSignUp2.setOnClickListener(view -> {
            Intent intent = new Intent("android.intent.action.VIEW",
                    Uri.parse(getString(R.string.terms_and_conditions_url)));
            startActivity(intent);
        });

        tvSignUp3.setOnClickListener(view -> {
            Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(getString(R.string.privacy_policy_url)));
            startActivity(intent);
        });

        btnSignIn2.setOnClickListener(view -> {
            boolean isInvalidEmail = emailAddress == null || !Credentials.isValidEmailAddress(emailAddress);
            boolean isInvalidPassword = password == null || password.length() < 6 ||
                    !password.matches("[A-Za-z0-9]*");

            if (isInvalidEmail) {
                etEmail.setBackgroundResource(R.drawable.et_bg_error);
                etEmail.setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat.getDrawable(context, R.drawable.ic_email_red),
                        null, null, null);
            }

            if (isInvalidPassword) {
                etPassword.setBackgroundResource(R.drawable.et_bg_error);
                etPassword.setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat.getDrawable(context, R.drawable.ic_lock_red),
                        null, null, null);
            }

            if (isInvalidEmail || isInvalidPassword)
                return;

            loginUser();
        });

        btnSignUp2.setOnClickListener(view -> {
            boolean isPasswordInvalidLen = signUpPassword == null || signUpPassword.length() < 6;
            boolean isPasswordInvalidChar = signUpPassword == null || !signUpPassword.matches("[A-Za-z0-9]*");

            boolean isConPasswordInvalidLen = confirmPassword == null || confirmPassword.length() < 1;
            boolean isConPasswordNotEqual = confirmPassword == null || !confirmPassword.equals(signUpPassword);

            if (isPasswordInvalidLen || isPasswordInvalidChar) {
                etPassword2.setBackgroundResource(R.drawable.et_bg_error);
                etPassword2.setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat.getDrawable(context, R.drawable.ic_lock_red),
                        null, null, null);

                if (isPasswordInvalidLen)
                    tvErrorCaption.setText(getString(R.string.invalidPassword));
                else
                    tvErrorCaption.setText(getString(R.string.invalidPassword2));

                if (isConPasswordInvalidLen || isConPasswordNotEqual) {
                    etPassword3.setBackgroundResource(R.drawable.et_bg_error);
                    etPassword3.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(context, R.drawable.ic_lock_red),
                            null, null, null);
                }

                tvErrorCaption.setVisibility(View.VISIBLE);

                return;
            } else if (isConPasswordInvalidLen || isConPasswordNotEqual) {
                etPassword3.setBackgroundResource(R.drawable.et_bg_error);
                etPassword3.setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat.getDrawable(context, R.drawable.ic_lock_red),
                        null, null, null);

                if (isConPasswordInvalidLen)
                    tvErrorCaption.setText(getString(R.string.reenterPassword));
                else
                    tvErrorCaption.setText(getString(R.string.noMatchPassword));

                tvErrorCaption.setVisibility(View.VISIBLE);

                return;
            }

            registerUser();
        });
    }

    private void checkPasswordInput(int sender) {
        signUpPassword = etPassword2.getText().toString();
        confirmPassword = etPassword3.getText().toString();

        boolean isPasswordValidLen = signUpPassword != null && signUpPassword.length() > 5;
        boolean isPasswordValidChar = signUpPassword != null && signUpPassword.matches("[A-Za-z0-9]*");

        boolean isPasswordInvalidLen = signUpPassword == null || signUpPassword.length() < 6;

        boolean isConPasswordValidLen = confirmPassword != null && confirmPassword.length() > 0;
        boolean isConPasswordEqual = confirmPassword != null && confirmPassword.equals(signUpPassword);

        switch (sender) {
            case 1:
                if (isPasswordValidLen && isPasswordValidChar) {
                    if (isConPasswordValidLen) {
                        if (confirmPassword.equals(signUpPassword)) {
                            etPassword2.setBackgroundResource(R.drawable.et_bg_default);
                            etPassword2.setCompoundDrawablesWithIntrinsicBounds(
                                    ContextCompat.getDrawable(context, R.drawable.ic_lock_focused),
                                    null, null, null);

                            etPassword3.setBackgroundResource(R.drawable.et_bg_default);
                            etPassword3.setCompoundDrawablesWithIntrinsicBounds(
                                    ContextCompat.getDrawable(context, R.drawable.ic_lock_focused),
                                    null, null, null);

                            tvErrorCaption.setVisibility(View.GONE);
                        } else {
                            etPassword2.setBackgroundResource(R.drawable.et_bg_error);
                            etPassword2.setCompoundDrawablesWithIntrinsicBounds(
                                    ContextCompat.getDrawable(context, R.drawable.ic_lock_red),
                                    null, null, null);

                            etPassword3.setBackgroundResource(R.drawable.et_bg_error);
                            etPassword3.setCompoundDrawablesWithIntrinsicBounds(
                                    ContextCompat.getDrawable(context, R.drawable.ic_lock_red),
                                    null, null, null);

                            tvErrorCaption.setText(getString(R.string.noMatchPassword));
                            tvErrorCaption.setVisibility(View.VISIBLE);
                        }
                    } else {
                        etPassword2.setBackgroundResource(R.drawable.et_bg_default);
                        etPassword2.setCompoundDrawablesWithIntrinsicBounds(
                                ContextCompat.getDrawable(context, R.drawable.ic_lock_focused),
                                null, null, null);

                        tvErrorCaption.setVisibility(View.GONE);
                    }
                } else {
                    etPassword2.setBackgroundResource(R.drawable.et_bg_error);
                    etPassword2.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(context, R.drawable.ic_lock_red),
                            null, null, null);

                    if (isPasswordInvalidLen)
                        tvErrorCaption.setText(getString(R.string.invalidPassword));
                    else
                        tvErrorCaption.setText(getString(R.string.invalidPassword2));

                    tvErrorCaption.setVisibility(View.VISIBLE);
                }

                break;
            case 2:
                if (isConPasswordValidLen) {
                    if (isConPasswordEqual) {
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

                        tvErrorCaption.setText(getString(R.string.noMatchPassword));
                        tvErrorCaption.setVisibility(View.VISIBLE);
                    }
                } else {
                    etPassword3.setBackgroundResource(R.drawable.et_bg_error);
                    etPassword3.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(context, R.drawable.ic_lock_red),
                            null, null, null);

                    tvErrorCaption.setText(getString(R.string.reenterPassword));
                    tvErrorCaption.setVisibility(View.VISIBLE);
                }

                checkPasswordInput(1);

                break;
        }
    }

    private void loginUser() {
        loadingDialog.setTextCaption("Signing in, please wait");
        loadingDialog.showDialog();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signInWithEmailAndPassword(emailAddress, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        loadingDialog.dismissDialog();

                        firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null)
                            signInActivity(false);
                        else {
                            loadingDialog.dismissDialog();

                            firebaseAuth.signOut();
                            signInFailed("Failed to get the current user. Account logged out.");
                        }
                    } else {
                        if (task.getException() != null) {
                            loadingDialog.dismissDialog();
                            signInFailed(task.getException().toString());
                        }
                    }
                });
    }

    private void signInActivity(boolean isFromRegistration) {
        setSharedPreferences();

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("isFromRegistration", isFromRegistration);
        startActivity(intent);
        finish();

        Toast.makeText(
                context,
                "You are signed in using\n" + firebaseUser.getEmail(),
                Toast.LENGTH_LONG).show();
    }

    private void signInFailed(String error) {
        if (error.toLowerCase().contains("no user record") ||
                error.toLowerCase().contains("password is invalid"))
            error = getString(R.string.unregisteredAccountText);
        else if (error.toLowerCase().contains("network error"))
            error = "Network error. Please try again later.";
        else if (error.toLowerCase().contains("internal error"))
            error = "Internal error. Please try again later.";
        else if (error.toLowerCase().contains("blocked all requests"))
            error = "This device is blocked temporarily. Please try again next time.";
        else if (error.toLowerCase().contains("has been disabled"))
            error = "The account has been deactivated. Please contact the admin to activate it.";

        messageDialog.setTextCaption(error);
        messageDialog.setTextType(2);
        messageDialog.showDialog();
    }

    private void registerUser() {
        loadingDialog.setTextCaption("Signing up, please wait");
        loadingDialog.showDialog();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.createUserWithEmailAndPassword(signUpEmailAddress, signUpPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                        addToDatabase();
                    else {
                        String error = "Error";
                        if (task.getException() != null)
                            error = task.getException().toString();

                        if (error.contains("UserCollision"))
                            error = "The Email Address is already registered";

                        loadingDialog.dismissDialog();

                        messageDialog.setTextCaption(error);
                        messageDialog.setTextType(2);
                        messageDialog.showDialog();
                    }
                });
    }

    private void addToDatabase() {
        if (firebaseAuth != null) {
            firebaseUser = firebaseAuth.getCurrentUser();
            if (firebaseUser != null)
                firebaseDatabase.getReference().addListenerForSingleValueEvent(getValueListener());
        }
    }

    private ValueEventListener getValueListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = new User(firebaseUser.getUid(), lastName, firstName);

                Roles allRoles = snapshot.getValue(Roles.class);
                Map<String, String> mapRole = new HashMap<>();

                if (allRoles != null && allRoles.getRoles() != null)
                    for (Map.Entry<String, Map<String, Role>> rolesInCategory : allRoles.getRoles().entrySet())
                        if (rolesInCategory.getValue() != null)
                            for (Map.Entry<String, Role> roleInCategory : rolesInCategory.getValue().entrySet())
                                if (roleInCategory.getValue().isDefaultOnRegister()) {
                                    String roleIndex = "role" + ((String.valueOf(mapRole.size() + 1).length() < 2)
                                            ? "0" + (mapRole.size() + 1)
                                            : (int) (mapRole.size() + 1));

                                    mapRole.put(roleIndex, roleInCategory.getKey());
                                }

                user.setRoles(mapRole);

                firebaseDatabase.getReference("users").child(firebaseUser.getUid()).setValue(user)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                password = signUpPassword;
                                signInActivity(true);
                            } else
                                rollbackUser();
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "rolesQuery:onCancelled", error.toException());
                loadingDialog.dismissDialog();

                messageDialog.setTextCaption("Failed to get the roles.");
                messageDialog.setTextType(2);
                messageDialog.showDialog();

                rollbackUser();
            }
        };
    }

    /*
     * int sendEmailTryCount = 0;
     * private void sendEmailVerificationLink() {
     * sendEmailTryCount = 0;
     * 
     * firebaseUser.sendEmailVerification().addOnCompleteListener(task -> {
     * if (task.isSuccessful()) signInActivity(true, true);
     * else {
     * if (sendEmailTryCount == 5) signInActivity(true, false);
     * else {
     * sendEmailTryCount++;
     * sendEmailVerificationLink();
     * }
     * }
     * });
     * }
     */

    private void rollbackUser() {
        if (firebaseUser != null) {
            firebaseUser.delete().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String error = "Failed to register an account, please try again later.";

                    loadingDialog.dismissDialog();

                    messageDialog.setTextCaption(error);
                    messageDialog.setTextType(2);
                    messageDialog.showDialog();
                } else
                    addToDatabase();
            });
        }
    }

    private void checkEmailAddressRegistration() {
        loadingDialog.showDialog();

        firebaseAuth.fetchSignInMethodsForEmail(forgotPasswordEmailAddress)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().getSignInMethods() == null ||
                                task.getResult().getSignInMethods().isEmpty()) {
                            etEmail3.setBackgroundResource(R.drawable.et_bg_error);
                            etEmail3.setCompoundDrawablesWithIntrinsicBounds(
                                    ContextCompat.getDrawable(context, R.drawable.ic_email_red),
                                    null, null, null);

                            tvErrorCaption2.setText(getString(R.string.unregisteredAccountText));
                            tvErrorCaption2.setVisibility(View.VISIBLE);

                            loadingDialog.dismissDialog();

                            messageDialog.setTextCaption(getString(R.string.unregisteredAccountText));
                            messageDialog.setTextType(2);
                            messageDialog.showDialog();
                        } else
                            sendForgotPasswordEmailLink();
                    } else {
                        String error = "";
                        if (task.getException() != null)
                            error = task.getException().toString();

                        loadingDialog.dismissDialog();

                        messageDialog.setTextCaption(error);
                        messageDialog.setTextType(2);
                        messageDialog.showDialog();
                    }
                });
    }

    private void sendForgotPasswordEmailLink() {
        firebaseAuth.sendPasswordResetEmail(forgotPasswordEmailAddress)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Intent intent = new Intent(context, WelcomeScreenActivity.class);
                        intent.putExtra("isForgotPasswordLinkSent", true);
                        startActivity(intent);
                        finishAffinity();
                    } else {
                        String error = "";
                        if (task.getException() != null)
                            error = task.getException().toString();

                        messageDialog.setTextCaption(error);
                        messageDialog.setTextType(2);
                        messageDialog.showDialog();
                    }

                    loadingDialog.dismissDialog();
                });
    }

    private void setSharedPreferences() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("signedInData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("password", password);
        editor.apply();
    }

    @Override
    public void onBackPressed() {
        if (currentStep > 0)
            currentStep--;
        else {
            super.onBackPressed();
            return;
        }

        tvErrorCaption.setVisibility(View.GONE);

        if (currentStep == 0) {
            signInCardView.setVisibility(View.GONE);
            signUpCardView.setVisibility(View.GONE);
            welcomeCardView.setVisibility(View.VISIBLE);
        } else if (currentStep == 1) {
            if (forgotPasswordCardView.getVisibility() == View.VISIBLE) {
                forgotPasswordCardView.setVisibility(View.GONE);
                signInCardView.setVisibility(View.VISIBLE);
            } else if (signUpCardView.getVisibility() == View.VISIBLE) {
                constraintLayout2.setVisibility(View.GONE);
                constraintLayout1.setVisibility(View.VISIBLE);
            }
        } else if (currentStep == 2) {
            constraintLayout3.setVisibility(View.GONE);
            constraintLayout2.setVisibility(View.VISIBLE);
            btnNextLayout.setVisibility(View.VISIBLE);
        }
    }
}