package com.example.mobilepalengke.Activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mobilepalengke.DataClasses.AppInfo;
import com.example.mobilepalengke.DataClasses.Role;
import com.example.mobilepalengke.DataClasses.User;
import com.example.mobilepalengke.DialogClasses.ChangeEmailAddressDialog;
import com.example.mobilepalengke.DialogClasses.ChangeNameDialog;
import com.example.mobilepalengke.DialogClasses.ChangePasswordDialog;
import com.example.mobilepalengke.DialogClasses.DownloadDialog;
import com.example.mobilepalengke.DialogClasses.LoadingDialog;
import com.example.mobilepalengke.DialogClasses.MessageDialog;
import com.example.mobilepalengke.DialogClasses.StatusDialog;
import com.example.mobilepalengke.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class AccountDetailsActivity extends AppCompatActivity {

    TextView tvLastName2, tvFirstName2, tvEmail2, tvRoles2, tvResendLink;
    ImageView imgVerification;
    Button btnChangeName, btnChangeEmail, btnChangePassword;

    Context context;

    LoadingDialog loadingDialog;
    MessageDialog messageDialog;
    ChangeNameDialog changeNameDialog;
    ChangeEmailAddressDialog changeEmailAddressDialog;
    ChangePasswordDialog changePasswordDialog;
    DownloadDialog downloadDialog;
    StatusDialog statusDialog;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;

    boolean isListening = true;

    Query userQuery, rolesQuery, appInfoQuery;

    String uid, currentPassword;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_details);

        tvLastName2 = findViewById(R.id.tvLastName2);
        tvFirstName2 = findViewById(R.id.tvFirstName2);
        tvEmail2 = findViewById(R.id.tvEmail2);
        tvResendLink = findViewById(R.id.tvResendLink);
        imgVerification = findViewById(R.id.imgVerification);
        tvRoles2 = findViewById(R.id.tvRoles2);
        btnChangeName = findViewById(R.id.btnChangeName);
        btnChangeEmail = findViewById(R.id.btnChangeEmail);
        btnChangePassword = findViewById(R.id.btnChangePassword);

        context = AccountDetailsActivity.this;

        loadingDialog = new LoadingDialog(context);
        messageDialog = new MessageDialog(context);
        changeNameDialog = new ChangeNameDialog(context);
        changeEmailAddressDialog = new ChangeEmailAddressDialog(context);
        changePasswordDialog = new ChangePasswordDialog(context);
        downloadDialog = new DownloadDialog(context);
        statusDialog = new StatusDialog(context);

        getSharedPreference();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null)
            uid = firebaseUser.getUid();

        firebaseDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_RTDB_url));
        userQuery = firebaseDatabase.getReference("users").child(uid);
        rolesQuery = firebaseDatabase.getReference("roles");
        appInfoQuery = firebaseDatabase.getReference("appInfo");

        loadingDialog.showDialog();
        isListening = true;
        userQuery.addValueEventListener(getUserValueListener());
        appInfoQuery.addValueEventListener(getAppInfoValueListener());

        tvResendLink.setOnClickListener(view -> sendEmailVerificationLink());

        btnChangeName.setOnClickListener(view -> {
            changeNameDialog.showDialog();
            changeNameDialog.setData(user.getLastName(), user.getFirstName());
        });

        btnChangeEmail.setOnClickListener(view -> changeEmailAddressDialog.showDialog());

        btnChangePassword.setOnClickListener(view -> changePasswordDialog.showDialog());

        changePasswordDialog.setCurrentPassword2(currentPassword);

        changeNameDialog.setDialogListener((lastName, firstName) -> {
            loadingDialog.showDialog();

            userQuery.getRef().child("lastName").setValue(lastName);
            userQuery.getRef().child("firstName").setValue(firstName)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(
                                    context,
                                    "Successfully changed your name",
                                    Toast.LENGTH_SHORT).show();

                            changeNameDialog.dismissDialog();
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
        });

        changeEmailAddressDialog.setDialogListener(emailAddress -> {
            loadingDialog.showDialog();

            firebaseUser.updateEmail(emailAddress).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(
                            context,
                            "Successfully changed your email address",
                            Toast.LENGTH_SHORT).show();

                    firebaseUser.reload();
                    tvEmail2.setText(firebaseUser.getEmail());

                    changeEmailAddressDialog.dismissDialog();
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
        });

        changePasswordDialog.setDialogListener(password -> {
            loadingDialog.showDialog();

            firebaseUser.updatePassword(password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(
                            context,
                            "Successfully changed your password",
                            Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(context, WelcomeScreenActivity.class);
                    intent.putExtra("isPasswordChanged", true);
                    startActivity(intent);
                    finishAffinity();

                } else {
                    String error = "";
                    if (task.getException() != null)
                        error = task.getException().toString();

                    if (error.contains("RecentLoginRequired")) {
                        Intent intent = new Intent(context, WelcomeScreenActivity.class);
                        intent.putExtra("isOnPasswordChange", true);
                        startActivity(intent);
                        finishAffinity();
                    }

                    messageDialog.setTextCaption(error);
                    messageDialog.setTextType(2);
                    messageDialog.showDialog();
                }

                loadingDialog.dismissDialog();
            });
        });
    }

    private ValueEventListener getUserValueListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    if (snapshot.exists()) {
                        user = snapshot.getValue(User.class);
                        if (user != null) {
                            tvLastName2.setText(user.getLastName());
                            tvFirstName2.setText(user.getFirstName());
                            tvEmail2.setText(firebaseUser.getEmail());
                        }
                    }

                    rolesQuery.addValueEventListener(getARValueListener());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "userQuery:onCancelled", error.toException());
                loadingDialog.dismissDialog();

                messageDialog.setTextCaption("Failed to get the user's data.");
                messageDialog.setTextType(2);
                messageDialog.showDialog();
            }
        };
    }

    private ValueEventListener getARValueListener() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    if (snapshot.exists()) {
                        List<String> roleIds = user.getRoles() != null ?
                                new ArrayList<>(user.getRoles().values()) :
                                new ArrayList<>();

                        String roles = "";
                        for (DataSnapshot dataSnapshot : snapshot.getChildren())
                            for (String roleId : roleIds) {
                                Role role = dataSnapshot.child(roleId.trim()).getValue(Role.class);
                                if (role != null)
                                    roles += "• " + role.getName() + "\n";
                            }

                        if (roles.trim().length() > 0)
                            tvRoles2.setText(roles.trim());
                        else
                            tvRoles2.setText(getString(R.string.none));
                    }

                    loadingDialog.dismissDialog();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "rolesQuery:onCancelled", error.toException());
                loadingDialog.dismissDialog();

                messageDialog.setTextCaption("Failed to get the admin roles.");
                messageDialog.setTextType(2);
                messageDialog.showDialog();
            }
        };
    }

    private ValueEventListener getAppInfoValueListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    if (snapshot.exists()) {
                        AppInfo appInfo = snapshot.getValue(AppInfo.class);

                        if (appInfo != null) {
                            if (appInfo.getStatus().equals("Live") || appInfo.isDeveloper()) {
                                statusDialog.dismissDialog();

                                if (appInfo.getCurrentVersion() < appInfo.getLatestVersion() && !appInfo.isDeveloper()) {
                                    downloadDialog.setTextCaption(getString(R.string.newVersionPrompt, appInfo.getLatestVersion()));
                                    downloadDialog.showDialog();

                                    downloadDialog.setDialogListener(new DownloadDialog.DialogListener() {
                                        @Override
                                        public void onDownload() {
                                            Intent intent = new Intent("android.intent.action.VIEW",
                                                    Uri.parse(appInfo.getDownloadLink()));
                                            startActivity(intent);

                                            downloadDialog.dismissDialog();
                                            finishAffinity();
                                        }

                                        @Override
                                        public void onCancel() {
                                            downloadDialog.dismissDialog();
                                            finishAffinity();
                                        }
                                    });
                                } else downloadDialog.dismissDialog();
                            } else {
                                statusDialog.setTextCaption(getString(R.string.statusPrompt, appInfo.getStatus()));
                                statusDialog.showDialog();

                                statusDialog.setDialogListener(() -> {
                                    statusDialog.dismissDialog();
                                    finishAffinity();
                                });
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "appInfoQuery:onCancelled", error.toException());
            }
        };
    }

    int sendEmailTryCount = 0;

    private void sendEmailVerificationLink() {
        loadingDialog.showDialog();

        sendEmailTryCount = 0;

        firebaseUser.sendEmailVerification().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                messageDialog.setTextCaption("Please check your email for verification link.");
                messageDialog.setTextType(1);
                messageDialog.showDialog();

                loadingDialog.dismissDialog();
            } else {
                if (sendEmailTryCount == 5) {
                    messageDialog.setTextCaption("Failed to send the email verification link.\n" +
                            "Please try again later by going to " + getString(R.string.more) + ">" +
                            getString(R.string.moreOption1) + ">" + getString(R.string.resendLink) + ".");
                    messageDialog.setTextType(2);
                    messageDialog.showDialog();

                    loadingDialog.dismissDialog();
                } else {
                    sendEmailTryCount++;
                    sendEmailVerificationLink();
                }
            }
        });
    }

    /*
     * private void checkIfEmailAddressIsVerified() {
     * firebaseUser.reload();
     * if (firebaseUser.isEmailVerified()) {
     * imgVerification.setImageResource(R.drawable.ic_check_circle_green);
     * tvResendLink.setVisibility(View.GONE);
     * }
     * else {
     * imgVerification.setImageResource(R.drawable.ic_error_red);
     * tvResendLink.setVisibility(View.VISIBLE);
     * }
     * 
     * imgVerification.setOnLongClickListener(view -> {
     * Toast.makeText(
     * context,
     * "Your email address is " +
     * (firebaseUser.isEmailVerified() ? "verified" : "not verified"),
     * Toast.LENGTH_SHORT
     * ).show();
     * return false;
     * });
     * }
     */

    private void getSharedPreference() {
        SharedPreferences sharedPreferences = getSharedPreferences("signedInData", Context.MODE_PRIVATE);
        currentPassword = sharedPreferences.getString("password", null);
    }

    @Override
    public void onResume() {
        isListening = true;
        userQuery.addListenerForSingleValueEvent(getUserValueListener());
        appInfoQuery.addListenerForSingleValueEvent(getAppInfoValueListener());

        super.onResume();
    }

    @Override
    public void onStop() {
        isListening = false;

        super.onStop();
    }

    @Override
    public void onDestroy() {
        isListening = false;

        super.onDestroy();
    }
}