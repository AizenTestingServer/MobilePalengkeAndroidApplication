package com.example.mobilepalengke.Activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.example.mobilepalengke.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.appcompat.app.AppCompatActivity;

@SuppressLint("CustomSplashScreen")
public class SplashScreen extends AppCompatActivity {

    Context context;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        context = SplashScreen.this;

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

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
}