package com.example.mobilepalengke.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.mobilepalengke.Activities.CheckOutActivity;
import com.example.mobilepalengke.Adapters.CartProductAdapter;
import com.example.mobilepalengke.DataClasses.CartProduct;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CartFragment extends Fragment {

    CheckBox cbSelectAll;
    RecyclerView recyclerView;
    TextView tvCartProductCaption, tvTotalPrice, tvTotalQty;
    Button btnCheckOut;

    Context context;

    LoadingDialog loadingDialog;
    MessageDialog messageDialog;

    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;

    boolean isListening = true;

    Query cartProductsQuery, productsQuery;

    List<CartProduct> cartProducts = new ArrayList<>();
    List<Product> products = new ArrayList<>();

    CartProductAdapter cartProductAdapter;

    String uid;
    boolean isListeningCheck = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        cbSelectAll = view.findViewById(R.id.cbSelectAll);
        recyclerView = view.findViewById(R.id.recyclerView);
        tvCartProductCaption = view.findViewById(R.id.tvCartProductCaption);
        tvTotalPrice = view.findViewById(R.id.tvTotalPrice);
        tvTotalQty = view.findViewById(R.id.tvTotalQty);
        btnCheckOut = view.findViewById(R.id.btnCheckOut);

        context = getContext();

        loadingDialog = new LoadingDialog(context);
        messageDialog = new MessageDialog(context);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null)
            uid = firebaseUser.getUid();

        firebaseDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_RTDB_url));
        cartProductsQuery = firebaseDatabase.getReference("cartList").child(uid).child("cartProducts");
        productsQuery = firebaseDatabase.getReference("products").orderByChild("name");

        loadingDialog.showDialog();
        isListening = true;
        cartProductsQuery.addValueEventListener(getCartValueListener());

        String productId = null;
        if (getArguments() != null)
            productId = getArguments().getString("productId");

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        cartProductAdapter = new CartProductAdapter(context, cartProducts, products, productId);
        cartProductAdapter.setAdapterListener(new CartProductAdapter.AdapterListener() {
            @Override
            public void updateCheckOutInfo(List<CheckOutProduct> checkOutProducts) {
                double totalPrice = 0;
                int totalQuantity = 0;

                for (CheckOutProduct checkOutProduct : checkOutProducts) {
                    totalPrice += checkOutProduct.getTotalPrice();
                    totalQuantity += checkOutProduct.getQuantity();
                }

                tvTotalPrice.setText(getString(R.string.priceValue, totalPrice));
                tvTotalQty.setText(getString(R.string.totalQuantityValue, totalQuantity, totalQuantity <= 1 ? "" : "s"));
            }

            @Override
            public void onBackPressed() {
                if (fragmentListener != null)
                    fragmentListener.onBackPressed();
            }
        });
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(cartProductAdapter);

        cbSelectAll.setOnCheckedChangeListener((compoundButton, b) -> {
            if (isListeningCheck) {
                recyclerView.smoothScrollToPosition(0);
                recyclerView.smoothScrollToPosition(cartProducts.size() - 1);
            }
        });

        tvTotalPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().equals(getString(R.string.totalPriceText)))
                    btnCheckOut.setVisibility(View.GONE);
                else
                    btnCheckOut.setVisibility(View.VISIBLE);
            }
        });

        btnCheckOut.setOnClickListener(view1 -> {
            ArrayList<String> productIdList = new ArrayList<>();
            ArrayList<Integer> quantityList = new ArrayList<>();
            ArrayList<Double> totalPriceList = new ArrayList<>();

            for (CheckOutProduct checkOutProduct : cartProductAdapter.getCheckOutProducts()) {
                productIdList.add(checkOutProduct.getId());
                quantityList.add(checkOutProduct.getQuantity());
                totalPriceList.add(checkOutProduct.getTotalPrice());
            }

            Intent intent = new Intent(context, CheckOutActivity.class);
            intent.putExtra("productIdList", productIdList);
            intent.putExtra("quantityList", quantityList);
            intent.putExtra("totalPriceList", totalPriceList);
            startActivity(intent);
        });

        return view;
    }

    private ValueEventListener getCartValueListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    cartProducts.clear();

                    if (snapshot.exists())
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            CartProduct cartProduct = dataSnapshot.getValue(CartProduct.class);
                            if (cartProduct != null) cartProducts.add(cartProduct);
                        }

                    loadingDialog.dismissDialog();

                    productsQuery.addValueEventListener(getProductsValueListener());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "cartProductsQuery:onCancelled", error.toException());
                loadingDialog.dismissDialog();

                messageDialog.setTextCaption("Failed to get the cart products.");
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
                        for (CartProduct cartProduct : cartProducts) {
                            Product product = snapshot.child(cartProduct.getId()).getValue(Product.class);
                            if (product != null) products.add(product);
                        }

                    if (cartProducts.size() == 0)
                        tvCartProductCaption.setVisibility(View.VISIBLE);
                    else
                        tvCartProductCaption.setVisibility(View.GONE);
                    tvCartProductCaption.bringToFront();

                    cartProductAdapter.notifyDataSetChanged();

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

    FragmentListener fragmentListener;

    public interface FragmentListener {
        void onBackPressed();
    }

    public void setFragmentListener(FragmentListener fragmentListener) {
        this.fragmentListener = fragmentListener;
    }

    @Override
    public void onResume() {
        isListening = true;
        cartProductsQuery.addValueEventListener(getCartValueListener());

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