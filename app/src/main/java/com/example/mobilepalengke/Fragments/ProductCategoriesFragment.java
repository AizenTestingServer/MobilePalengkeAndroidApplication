package com.example.mobilepalengke.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mobilepalengke.Adapters.ProductCategoryAdapter;
import com.example.mobilepalengke.DataClasses.ProductCategory;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ProductCategoriesFragment extends Fragment {

    RecyclerView recyclerView;
    TextView tvCategoryCaption;

    Context context;

    LoadingDialog loadingDialog;
    MessageDialog messageDialog;

    FirebaseDatabase firebaseDatabase;

    boolean isListening = true;

    Query productCategoriesQuery;

    List<ProductCategory> productCategories = new ArrayList<>();

    ProductCategoryAdapter productCategoryAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_categories, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        tvCategoryCaption = view.findViewById(R.id.tvCategoryCaption);

        context = getContext();

        loadingDialog = new LoadingDialog(context);
        messageDialog = new MessageDialog(context);

        firebaseDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_RTDB_url));
        productCategoriesQuery = firebaseDatabase.getReference("productCategories").orderByChild("name");

        loadingDialog.showDialog();
        isListening = true;
        productCategoriesQuery.addValueEventListener(getProdCatValueListener());

        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 3, GridLayoutManager.VERTICAL, false);
        productCategoryAdapter = new ProductCategoryAdapter(context, productCategories);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(productCategoryAdapter);

        return view;
    }

    private ValueEventListener getProdCatValueListener() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    productCategories.clear();

                    if (snapshot.exists()) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            ProductCategory productCategory = dataSnapshot.getValue(ProductCategory.class);
                            if (productCategory != null)
                                productCategories.add(productCategory);
                        }
                    }

                    if (productCategories.size() == 0)
                        tvCategoryCaption.setVisibility(View.VISIBLE);
                    else
                        tvCategoryCaption.setVisibility(View.GONE);
                    tvCategoryCaption.bringToFront();

                    productCategoryAdapter.notifyDataSetChanged();

                    loadingDialog.dismissDialog();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "productCategoriesQuery:onCancelled", error.toException());
                loadingDialog.dismissDialog();

                messageDialog.setTextCaption("Failed to get the product categories.");
                messageDialog.setTextType(2);
                messageDialog.showDialog();
            }
        };
    }

    @Override
    public void onResume() {
        isListening = true;
        productCategoriesQuery.addValueEventListener(getProdCatValueListener());

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