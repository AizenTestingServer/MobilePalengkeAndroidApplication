package com.example.mobilepalengke.Activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.mobilepalengke.Adapters.CheckOutAdapter;
import com.example.mobilepalengke.DataClasses.Order;
import com.example.mobilepalengke.DataClasses.Product;
import com.example.mobilepalengke.DialogClasses.LoadingDialog;
import com.example.mobilepalengke.DialogClasses.MessageDialog;
import com.example.mobilepalengke.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AdminOrderActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    TextView tvCheckOutCaption;

    Context context;

    LoadingDialog loadingDialog;
    MessageDialog messageDialog;

    FirebaseDatabase firebaseDatabase;

    boolean isListening = true;

    Query ordersQuery, productsQuery;

    List<Order> orders = new ArrayList<>();
    List<Product> products = new ArrayList<>();

    CheckOutAdapter checkOutAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_order);

        recyclerView = findViewById(R.id.recyclerView);
        tvCheckOutCaption = findViewById(R.id.tvCheckOutCaption);

        context = AdminOrderActivity.this;

        loadingDialog = new LoadingDialog(context);
        messageDialog = new MessageDialog(context);

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
                            if (order != null)
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
}