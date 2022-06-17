package com.example.mobilepalengke.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mobilepalengke.Activities.AdminMealPlansActivity;
import com.example.mobilepalengke.Activities.AdminOrderActivity;
import com.example.mobilepalengke.Activities.AdminProductActivity;
import com.example.mobilepalengke.Activities.AdminRolesActivity;
import com.example.mobilepalengke.Activities.AdminUsersActivity;
import com.example.mobilepalengke.DialogClasses.LoadingDialog;
import com.example.mobilepalengke.DialogClasses.MessageDialog;
import com.example.mobilepalengke.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

public class DashboardFragment extends Fragment {

    CardView ordersCardView, productsCardView, mealPlansCardView, usersCardView, rolesCardView;
    TextView tvCount, tvCount2, tvCount3, tvCount4, tvCount5;

    Context context;

    LoadingDialog loadingDialog;
    MessageDialog messageDialog;

    FirebaseDatabase firebaseDatabase;

    boolean isListening = true;

    int orderCount = 0, productCount = 0, mealPlanCount = 0, userCount = 0, roleCount = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        ordersCardView = view.findViewById(R.id.ordersCardView);
        productsCardView = view.findViewById(R.id.productsCardView);
        mealPlansCardView = view.findViewById(R.id.mealPlansCardView);
        usersCardView = view.findViewById(R.id.usersCardView);
        rolesCardView = view.findViewById(R.id.rolesCardView);

        tvCount = view.findViewById(R.id.tvCount);
        tvCount2 = view.findViewById(R.id.tvCount2);
        tvCount3 = view.findViewById(R.id.tvCount3);
        tvCount4 = view.findViewById(R.id.tvCount4);
        tvCount5 = view.findViewById(R.id.tvCount5);

        context = getContext();

        loadingDialog = new LoadingDialog(context);
        messageDialog = new MessageDialog(context);

        firebaseDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_RTDB_url));

        loadingDialog.showDialog();
        isListening = true;
        firebaseDatabase.getReference().addValueEventListener(getGeneralReference());

        rolesCardView.setOnClickListener(view1 -> {
            Intent intent = new Intent(context, AdminRolesActivity.class);
            startActivity(intent);
        });

        usersCardView.setOnClickListener(view1 -> {
            Intent intent = new Intent(context, AdminUsersActivity.class);
            startActivity(intent);
        });

        mealPlansCardView.setOnClickListener(view1 -> {
            Intent intent = new Intent(context, AdminMealPlansActivity.class);
            startActivity(intent);
        });

        productsCardView.setOnClickListener(view1 -> {
            Intent intent = new Intent(context, AdminProductActivity.class);
            startActivity(intent);
        });

        ordersCardView.setOnClickListener(view1 -> {
            Intent intent = new Intent(context, AdminOrderActivity.class);
            startActivity(intent);
        });

        return view;
    }

    private ValueEventListener getGeneralReference() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    if (snapshot.exists()) {
                        orderCount = (int) snapshot.child("orders").getChildrenCount();
                        productCount = (int) snapshot.child("products").getChildrenCount();
                        mealPlanCount = (int) snapshot.child("mealPlans").getChildrenCount();
                        userCount = (int) snapshot.child("users").getChildrenCount();
                        roleCount = 0;

                        for (DataSnapshot dataSnapshot : snapshot.child("roles").getChildren())
                            roleCount += dataSnapshot.getChildrenCount();
                    }

                    tvCount.setText(String.valueOf(orderCount));
                    tvCount2.setText(String.valueOf(productCount));
                    tvCount3.setText(String.valueOf(mealPlanCount));
                    tvCount4.setText(String.valueOf(userCount));
                    tvCount5.setText(String.valueOf(roleCount));

                    loadingDialog.dismissDialog();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "homeSliderImagesQuery:onCancelled", error.toException());
                loadingDialog.dismissDialog();

                messageDialog.setTextCaption("Failed to get the images.");
                messageDialog.setTextType(2);
                messageDialog.showDialog();
            }
        };
    }

    @Override
    public void onResume() {
        isListening = true;
        firebaseDatabase.getReference().addValueEventListener(getGeneralReference());

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