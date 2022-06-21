package com.example.mobilepalengke.Activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.example.mobilepalengke.DataClasses.AppInfo;
import com.example.mobilepalengke.DialogClasses.DownloadDialog;
import com.example.mobilepalengke.DialogClasses.StatusDialog;
import com.example.mobilepalengke.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

@SuppressLint("CustomSplashScreen")
public class SplashScreen extends AppCompatActivity {

    Context context;

    DownloadDialog downloadDialog;
    StatusDialog statusDialog;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;

    boolean isListening = true;

    Query appInfoQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        context = SplashScreen.this;

        downloadDialog = new DownloadDialog(context);
        statusDialog = new StatusDialog(context);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_RTDB_url));

        appInfoQuery = firebaseDatabase.getReference("appInfo");

        isListening = true;
        appInfoQuery.addValueEventListener(getAppInfoValueListener());
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
                                            finish();
                                        }

                                        @Override
                                        public void onCancel() {
                                            downloadDialog.dismissDialog();
                                            finish();
                                        }
                                    });
                                } else {
                                    downloadDialog.dismissDialog();

                                    new Handler().postDelayed(() -> {
                                        Intent intent;

                                        if (firebaseUser != null) {
                                            intent = new Intent(context, MainActivity.class);

                                            Toast.makeText(
                                                    context,
                                                    "You are signed in using\n" + firebaseUser.getEmail(),
                                                    Toast.LENGTH_LONG).show();
                                        } else
                                            intent = new Intent(context, WelcomeScreenActivity.class);

                                        startActivity(intent);
                                        finish();
                                    }, 2000);
                                }
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

    @Override
    public void onResume() {
        isListening = true;
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