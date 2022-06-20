package com.example.mobilepalengke.Fragments;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mobilepalengke.Activities.AccountDetailsActivity;
import com.example.mobilepalengke.Activities.AddressActivity;
import com.example.mobilepalengke.Activities.SettingsActivity;
import com.example.mobilepalengke.Activities.WelcomeScreenActivity;
import com.example.mobilepalengke.Adapters.IconOptionAdapter;
import com.example.mobilepalengke.DataClasses.IconOption;
import com.example.mobilepalengke.DataClasses.User;
import com.example.mobilepalengke.DialogClasses.LoadingDialog;
import com.example.mobilepalengke.DialogClasses.MessageDialog;
import com.example.mobilepalengke.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MoreFragment extends Fragment {

    TextView tvFullName;
    RecyclerView recyclerView;
    Button btnSignOut;

    Context context;

    LoadingDialog loadingDialog;
    MessageDialog messageDialog;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;

    boolean isListening = true;

    Query userQuery;

    String uid, fullName;
    User currentUser;

    List<IconOption> moreIconOptions;

    IconOptionAdapter iconOptionAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_more, container, false);

        tvFullName = view.findViewById(R.id.tvFullName);
        recyclerView = view.findViewById(R.id.recyclerView);
        btnSignOut = view.findViewById(R.id.btnSignOut);

        context = getContext();

        loadingDialog = new LoadingDialog(context);
        messageDialog = new MessageDialog(context);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null)
            uid = firebaseUser.getUid();

        firebaseDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_RTDB_url));
        userQuery = firebaseDatabase.getReference("users").child(uid);

        loadingDialog.showDialog();
        isListening = true;
        userQuery.addValueEventListener(getUserValueListener());

        moreIconOptions = Arrays.asList(
                new IconOption(getString(R.string.moreOption1), R.drawable.ic_baseline_person_24),
                new IconOption(getString(R.string.moreOption2), R.drawable.ic_baseline_share_location_24),
                new IconOption(getString(R.string.moreOption4), R.drawable.ic_baseline_info_24),
                new IconOption(getString(R.string.moreOption5), R.drawable.ic_baseline_contacts_24),
                new IconOption(getString(R.string.moreOption6), R.drawable.ic_baseline_question_answer_24),
                new IconOption(getString(R.string.moreOption7), R.drawable.ic_baseline_reviews_24),
                new IconOption(getString(R.string.moreOption8), R.drawable.ic_baseline_checklist_rtl_24),
                new IconOption(getString(R.string.moreOption9), R.drawable.ic_baseline_privacy_tip_24));

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        iconOptionAdapter = new IconOptionAdapter(context, moreIconOptions);
        iconOptionAdapter.setIconOptionAdapterListener(new IconOptionAdapter.IconOptionAdapterListener() {
            @Override
            public void onClick(IconOption iconOption) {
                if (iconOption.getLabelName().equals(getString(R.string.moreOption1))) {
                    Intent intent = new Intent(context, AccountDetailsActivity.class);
                    context.startActivity(intent);
                } else if (iconOption.getLabelName().equals(getString(R.string.moreOption2))) {
                    Intent intent = new Intent(context, AddressActivity.class);
                    context.startActivity(intent);
                } else if (iconOption.getLabelName().equals(getString(R.string.moreOption3))) {
                    Intent intent = new Intent(context, SettingsActivity.class);
                    context.startActivity(intent);
                } else if (iconOption.getLabelName().equals(getString(R.string.moreOption4))) {
                    Intent intent = new Intent("android.intent.action.VIEW",
                            Uri.parse(getString(R.string.about_us_url)));
                    context.startActivity(intent);
                } else if (iconOption.getLabelName().equals(getString(R.string.moreOption5))) {
                    Intent intent = new Intent("android.intent.action.VIEW",
                            Uri.parse(getString(R.string.contact_us_url)));
                    context.startActivity(intent);
                } else if (iconOption.getLabelName().equals(getString(R.string.moreOption6))) {
                    Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(getString(R.string.faq_url)));
                    context.startActivity(intent);
                } else if (iconOption.getLabelName().equals(getString(R.string.moreOption7))) {
                    Intent intent = new Intent("android.intent.action.VIEW",
                            Uri.parse(getString(R.string.reviews_url)));
                    context.startActivity(intent);
                } else if (iconOption.getLabelName().equals(getString(R.string.moreOption8))) {
                    Intent intent = new Intent("android.intent.action.VIEW",
                            Uri.parse(getString(R.string.terms_and_conditions_url)));
                    context.startActivity(intent);
                } else if (iconOption.getLabelName().equals(getString(R.string.moreOption9))) {
                    Intent intent = new Intent("android.intent.action.VIEW",
                            Uri.parse(getString(R.string.privacy_policy_url)));
                    context.startActivity(intent);
                }
            }

            @Override
            public void onClick(IconOption iconOption, int position) {

            }
        });
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(iconOptionAdapter);

        btnSignOut.setOnClickListener(view1 -> {
            if (firebaseUser != null) {
                loadingDialog.showDialog();
                firebaseAuth.signOut();

                Intent intent = new Intent(context, WelcomeScreenActivity.class);
                startActivity(intent);
                ((Activity) context).finishAffinity();

                NotificationManager notificationManager = (NotificationManager)
                        context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancelAll();

                loadingDialog.dismissDialog();

                Toast.makeText(
                        context,
                        "You have signed out your account",
                        Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private ValueEventListener getUserValueListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    if (snapshot.exists()) {
                        currentUser = snapshot.getValue(User.class);
                        if (currentUser != null) {
                            fullName = currentUser.getFirstName() + " " + currentUser.getLastName();
                            tvFullName.setText(fullName);
                        }
                    }

                    loadingDialog.dismissDialog();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "userQuery:onCancelled", error.toException());
                loadingDialog.dismissDialog();

                messageDialog.setTextCaption("Failed to get the currentUser's data.");
                messageDialog.setTextType(2);
                messageDialog.showDialog();
            }
        };
    }

    @Override
    public void onResume() {
        isListening = true;
        userQuery.addListenerForSingleValueEvent(getUserValueListener());

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