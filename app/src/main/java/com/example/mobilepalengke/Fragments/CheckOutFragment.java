package com.example.mobilepalengke.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mobilepalengke.Adapters.CheckOutAdapter;
import com.example.mobilepalengke.DataClasses.Order;
import com.example.mobilepalengke.DataClasses.Product;
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

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CheckOutFragment extends Fragment {

    RecyclerView recyclerView;
    TextView tvCheckOutCaption;

    Context context;

    LoadingDialog loadingDialog;
    MessageDialog messageDialog;

    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;

    boolean isListening = true;

    Query ordersQuery, productsQuery;

    List<Order> orders = new ArrayList<>();
    List<Product> products = new ArrayList<>();

    CheckOutAdapter checkOutAdapter;

    String uid;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_check_out, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        tvCheckOutCaption = view.findViewById(R.id.tvCheckOutCaption);

        context = getContext();

        loadingDialog = new LoadingDialog(context);
        messageDialog = new MessageDialog(context);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null)
            uid = firebaseUser.getUid();

        firebaseDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_RTDB_url));
        ordersQuery = firebaseDatabase.getReference("orders");
        productsQuery = firebaseDatabase.getReference("products").orderByChild("name");

        loadingDialog.showDialog();
        isListening = true;
        ordersQuery.addValueEventListener(getOrdersValueListener());

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        checkOutAdapter = new CheckOutAdapter(context, orders, products);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(checkOutAdapter);

        return view;
    }

    private ValueEventListener getOrdersValueListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    orders.clear();

                    if (snapshot.exists())
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Order order = dataSnapshot.getValue(Order.class);
                            if (order != null && order.getOwnerId().equals(uid))
                                orders.add(order);
                        }

                    loadingDialog.dismissDialog();

                    productsQuery.addValueEventListener(getProductsValueListener());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "ordersQuery:onCancelled", error.toException());
                loadingDialog.dismissDialog();

                messageDialog.setTextCaption("Failed to get the orders.");
                messageDialog.setTextType(2);
                messageDialog.showDialog();
            }
        };
    }

    private ValueEventListener getProductsValueListener() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    products.clear();

                    if (snapshot.exists())
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Product product = dataSnapshot.getValue(Product.class);
                            if (product != null)
                                products.add(product);
                        }

                    if (orders.size() == 0)
                        tvCheckOutCaption.setVisibility(View.VISIBLE);
                    else
                        tvCheckOutCaption.setVisibility(View.GONE);
                    tvCheckOutCaption.bringToFront();

                    checkOutAdapter.notifyDataSetChanged();

                    loadingDialog.dismissDialog();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "productsQuery:onCancelled", error.toException());
                loadingDialog.dismissDialog();

                messageDialog.setTextCaption("Failed to get the products.");
                messageDialog.setTextType(2);
                messageDialog.showDialog();
            }
        };
    }

    @Override
    public void onResume() {
        isListening = true;
        ordersQuery.addValueEventListener(getOrdersValueListener());

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