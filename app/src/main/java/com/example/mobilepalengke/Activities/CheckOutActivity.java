package com.example.mobilepalengke.Activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.mobilepalengke.Adapters.CheckOutSummaryProductAdapter;
import com.example.mobilepalengke.DataClasses.CheckOutProduct;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CheckOutActivity extends AppCompatActivity {

    ConstraintLayout captionLayout, firstConstraint, footerLayout, footer1Layout;
    RecyclerView recyclerView;
    TextView tvCaptionHeader, tvStep, tvTotalPrice, tvTotalQty;
    Button btnNext, btnBack, btnNext1;

    Context context;

    LoadingDialog loadingDialog;
    MessageDialog messageDialog;

    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;

    boolean isListening = true;

    Query productsQuery;

    ArrayList<String> productIdList;
    ArrayList<Integer> quantityList;
    ArrayList<Double> totalPriceList;

    List<CheckOutProduct> checkOutProducts = new ArrayList<>();
    List<Product> products = new ArrayList<>();

    CheckOutSummaryProductAdapter checkOutSummaryProductAdapter;

    String uid;
    int currentStep = 1, maxStep = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_out);

        captionLayout = findViewById(R.id.captionLayout);
        tvCaptionHeader = findViewById(R.id.tvCaptionHeader);
        tvStep = findViewById(R.id.tvStep);

        footerLayout = findViewById(R.id.footerLayout);
        footer1Layout = findViewById(R.id.footer1Layout);

        firstConstraint = findViewById(R.id.firstConstraint);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        tvTotalQty = findViewById(R.id.tvTotalQty);
        btnNext = findViewById(R.id.btnNext);
        recyclerView = findViewById(R.id.recyclerView);

        btnBack = findViewById(R.id.btnBack);
        btnNext1 = findViewById(R.id.btnNext1);

        context = CheckOutActivity.this;

        loadingDialog = new LoadingDialog(context);
        messageDialog = new MessageDialog(context);

        Intent currentIntent = getIntent();
        productIdList = (ArrayList<String>) currentIntent.getSerializableExtra("productIdList");
        quantityList = (ArrayList<Integer>) currentIntent.getSerializableExtra("quantityList");
        totalPriceList = (ArrayList<Double>) currentIntent.getSerializableExtra("totalPriceList");

        double totalPrice = 0;
        int totalQuantity = 0;

        checkOutProducts.clear();
        for (int i = 0; i < productIdList.size(); i++) {
            checkOutProducts.add(
                    new CheckOutProduct(productIdList.get(i), quantityList.get(i), totalPriceList.get(i))
            );
            totalPrice += totalPriceList.get(i);
            totalQuantity += quantityList.get(i);
        }

        tvTotalPrice.setText(getString(R.string.priceValue, totalPrice));
        tvTotalQty.setText(getString(R.string.totalQuantityValue, totalQuantity, totalQuantity <= 1 ? "" : "s"));

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null)
            uid = firebaseUser.getUid();

        firebaseDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_RTDB_url));
        productsQuery = firebaseDatabase.getReference("products").orderByChild("name");

        loadingDialog.showDialog();
        isListening = true;
        productsQuery.addValueEventListener(getProductsValueListener());

        GridLayoutManager gridLayoutManager =
                new GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(gridLayoutManager);
        checkOutSummaryProductAdapter = new CheckOutSummaryProductAdapter(context, checkOutProducts, products);
        recyclerView.setAdapter(checkOutSummaryProductAdapter);

        updateStep();

        btnNext.setOnClickListener(view -> {
            currentStep++;
            firstConstraint.setVisibility(View.GONE);
            footerLayout.setVisibility(View.GONE);
            footer1Layout.setVisibility(View.VISIBLE);

            //showStep2LayoutAndHideStep1Layout
            tvCaptionHeader.setText("Step 2 Title");
            updateStep();
        });

        btnBack.setOnClickListener(view -> {
            currentStep--;
            backLayout();
            updateStep();
        });

        btnNext1.setOnClickListener(view -> {
            if (currentStep < maxStep) currentStep++;

            switch (currentStep) {
                case 3:
                    //showStep3LayoutAndHideStep2Layout
                    tvCaptionHeader.setText("Step 3 Title");
                    break;
                default:
                    break;
            }

            updateStep();
        });
    }

    private ValueEventListener getProductsValueListener() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    products.clear();

                    if (snapshot.exists())
                        for (CheckOutProduct checkOutProduct : checkOutProducts) {
                            Product product = snapshot.child(checkOutProduct.getId()).getValue(Product.class);
                            if (product != null) products.add(product);
                        }

                    checkOutSummaryProductAdapter.notifyDataSetChanged();

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

    private void backLayout() {
        switch (currentStep) {
            case 1:
                firstConstraint.setVisibility(View.VISIBLE);
                footerLayout.setVisibility(View.VISIBLE);
                footer1Layout.setVisibility(View.GONE);

                tvCaptionHeader.setText(getString(R.string.orderSummary));
                //showStep1LayoutAndHideStep2Layout
                break;
            case 2:
                tvCaptionHeader.setText("Step 2 Title");
                //showStep2LayoutAndHideStep3Layout
                break;
            case 3:
                tvCaptionHeader.setText("Step 3 Title");
                //showStep3LayoutAndHideStep4Layout
                break;
            default:
                break;
        }
    }

    private void updateStep() {
        tvStep.setText(getString(R.string.stepValue, currentStep, maxStep));
    }

    @Override
    public void onBackPressed() {
        if (currentStep > 1)
            currentStep--;
        else {
            super.onBackPressed();
            return;
        }

        backLayout();
        updateStep();
    }

    @Override
    public void onResume() {
        isListening = true;
        productsQuery.addValueEventListener(getProductsValueListener());

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