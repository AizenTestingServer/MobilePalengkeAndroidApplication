package com.example.mobilepalengke.Activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mobilepalengke.Adapters.IconOptionAdapter;
import com.example.mobilepalengke.Adapters.ProductAdapter;
import com.example.mobilepalengke.DataClasses.AppInfo;
import com.example.mobilepalengke.DataClasses.Cart;
import com.example.mobilepalengke.DataClasses.CartProduct;
import com.example.mobilepalengke.DataClasses.IconOption;
import com.example.mobilepalengke.DataClasses.Product;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ProductsActivity extends AppCompatActivity {

    ImageView cartIconImage;
    ConstraintLayout productsLayout, productCategoriesLayout;
    EditText etSearchProduct;
    TextView tvCartCount, tvSelectedCategory, btnChangeCategory, tvProductCaption;
    Button btnBack;
    RecyclerView recyclerView, recyclerView2;

    Context context;

    LoadingDialog loadingDialog;
    MessageDialog messageDialog;
    DownloadDialog downloadDialog;
    StatusDialog statusDialog;

    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;

    boolean isListening = true;

    int currentStep = 0, maxStep = 1;

    String searchValue;

    Query productsQuery, productCategoriesQuery, cartProductsQuery, appInfoQuery;

    List<Product> products = new ArrayList<>(), productsCopy = new ArrayList<>();
    List<String> productsCategories = new ArrayList<>(), productsCategoriesCopy = new ArrayList<>();

    ProductAdapter productAdapter;

    IconOptionAdapter iconOptionAdapter;

    List<IconOption> productCategories = new ArrayList<>();
    List<String> productCategoriesId = new ArrayList<>();
    List<CartProduct> cartProducts = new ArrayList<>();

    int selectedCategoryIndex = 0;
    String selectedCategoryId;

    String uid;
    int overallCartCount = 0;

    boolean isCartExisting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);

        cartIconImage = findViewById(R.id.cartIconImage);
        tvCartCount = findViewById(R.id.tvCartCount);

        productsLayout = findViewById(R.id.productsLayout);
        productCategoriesLayout = findViewById(R.id.productCategoriesLayout);

        etSearchProduct = findViewById(R.id.etSearchProduct);
        tvSelectedCategory = findViewById(R.id.tvSelectedCategory);
        btnChangeCategory = findViewById(R.id.btnChangeCategory);
        recyclerView = findViewById(R.id.recyclerView);
        tvProductCaption = findViewById(R.id.tvProductCaption);

        recyclerView2 = findViewById(R.id.recyclerView2);
        btnBack = findViewById(R.id.btnBack);

        context = ProductsActivity.this;

        loadingDialog = new LoadingDialog(context);
        messageDialog = new MessageDialog(context);
        downloadDialog = new DownloadDialog(context);
        statusDialog = new StatusDialog(context);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null)
            uid = firebaseUser.getUid();

        selectedCategoryId = getIntent().getStringExtra("selectedCategoryId");

        firebaseDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_RTDB_url));
        productsQuery = firebaseDatabase.getReference("products").orderByChild("name");
        productCategoriesQuery = firebaseDatabase.getReference("productCategories").orderByChild("name");
        cartProductsQuery = firebaseDatabase.getReference("cartList").child(uid);
        appInfoQuery = firebaseDatabase.getReference("appInfo");

        loadingDialog.showDialog();
        isListening = true;
        productsQuery.addValueEventListener(getProdValueListener());
        appInfoQuery.addValueEventListener(getAppInfoValueListener());

        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false);
        productAdapter = new ProductAdapter(context, products);
        productAdapter.setProductAdapterListener((product, quantity) -> {
            loadingDialog.showDialog();

            int initialQty = 0;

            for (CartProduct cartProduct : cartProducts)
                if (cartProduct.getId().equals(product.getId())) {
                    initialQty = cartProduct.getQuantity();
                    break;
                }

            CartProduct cartProduct = new CartProduct(product.getId(), initialQty + quantity);

            if (isCartExisting)
                cartProductsQuery.getRef().child("cartProducts").child(product.getId())
                        .setValue(cartProduct).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(
                                        context,
                                        product.getName() + " (" + quantity + ") was added to cart",
                                        Toast.LENGTH_SHORT).show();

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
            else {
                Map<String, CartProduct> mapCartProduct = new HashMap<>();
                mapCartProduct.put(product.getId(), cartProduct);

                Cart cart = new Cart(uid, mapCartProduct);
                cartProductsQuery.getRef().setValue(cart);
            }
        });
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(productAdapter);

        iconOptionAdapter = new IconOptionAdapter(context, productCategories);
        iconOptionAdapter.setIconOptionAdapterListener(new IconOptionAdapter.IconOptionAdapterListener() {
            @Override
            public void onClick(IconOption iconOption) {

            }

            @Override
            public void onClick(IconOption iconOption, int position) {
                currentStep--;

                tvSelectedCategory.setText(iconOption.getLabelName());

                selectedCategoryIndex = position;
                selectedCategoryId = productCategoriesId.get(position);

                productCategoriesLayout.setVisibility(View.GONE);
                productsLayout.setVisibility(View.VISIBLE);

                filterProducts();
            }
        });
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL,
                false);
        recyclerView2.setLayoutManager(linearLayoutManager2);
        recyclerView2.setAdapter(iconOptionAdapter);

        cartIconImage.setOnClickListener(view -> {
            Intent intent = new Intent(context, CartActivity.class);
            startActivity(intent);
        });

        etSearchProduct.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                searchValue = editable.toString();

                filterProducts();
            }
        });

        btnChangeCategory.setOnClickListener(view1 -> {
            if (currentStep < maxStep)
                currentStep++;

            productsLayout.setVisibility(View.GONE);
            productCategoriesLayout.setVisibility(View.VISIBLE);
        });

        btnBack.setOnClickListener(view1 -> {
            currentStep--;

            productCategoriesLayout.setVisibility(View.GONE);
            productsLayout.setVisibility(View.VISIBLE);
        });
    }

    private ValueEventListener getProdValueListener() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    products.clear();

                    if (snapshot.exists()) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Product product = dataSnapshot.getValue(Product.class);
                            if (product != null && !product.isDeactivated())
                                products.add(product);
                        }
                    }

                    productCategoriesQuery.addValueEventListener(getProdCatValueListener());
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

    private ValueEventListener getProdCatValueListener() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    productsCategories.clear();
                    productCategories.clear();
                    productCategoriesId.clear();

                    if (snapshot.exists()) {
                        productCategories.add(new IconOption(getString(R.string.all), 0));
                        productCategoriesId.add("prodCat00");

                        for (Product product : products) {
                            List<String> categoryIds = product.getCategories() != null ?
                                    new ArrayList<>(product.getCategories().values()) :
                                    new ArrayList<>();
                            List<String> categories = new ArrayList<>();

                            for (String categoryId : categoryIds)
                                categories.add(snapshot.child(categoryId.trim())
                                        .child("name").getValue(String.class));

                            productsCategories.add(TextUtils.join(", ", categories));
                        }

                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            productCategories.add(new IconOption(dataSnapshot.child("name").getValue(String.class), 0));
                            productCategoriesId.add(dataSnapshot.getKey());

                            if (selectedCategoryId != null && selectedCategoryId.equals(dataSnapshot.getKey())) {
                                selectedCategoryIndex = productCategoriesId.size() - 1;
                                tvSelectedCategory.setText(productCategories.get(selectedCategoryIndex).getLabelName());
                            }
                        }
                    }

                    productsCopy.clear();
                    productsCategoriesCopy.clear();

                    productsCopy.addAll(products);
                    productsCategoriesCopy.addAll(productsCategories);

                    filterProducts();

                    iconOptionAdapter.notifyDataSetChanged();

                    cartProductsQuery.addValueEventListener(getCartValueListener());
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

    private ValueEventListener getCartValueListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    overallCartCount = 0;
                    cartProducts.clear();

                    if (snapshot.child("cartProducts").exists()) {
                        isCartExisting = true;

                        overallCartCount = (int) snapshot.child("cartProducts").getChildrenCount();

                        for (DataSnapshot dataSnapshot : snapshot.child("cartProducts").getChildren()) {
                            CartProduct cartProduct = dataSnapshot.getValue(CartProduct.class);
                            if (cartProduct != null)
                                cartProducts.add(cartProduct);
                        }
                    } else
                        isCartExisting = false;

                    if (overallCartCount == 0)
                        tvCartCount.setVisibility(View.GONE);
                    else tvCartCount.setVisibility(View.VISIBLE);
                    tvCartCount.bringToFront();
                    tvCartCount.setText(String.valueOf(overallCartCount));

                    loadingDialog.dismissDialog();
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

    @SuppressLint("NotifyDataSetChanged")
    private void filterProducts() {
        List<Product> productsTemp = new ArrayList<>(productsCopy);
        List<String> productsCategoriesTemp = new ArrayList<>(productsCategoriesCopy);

        products.clear();
        productsCategories.clear();

        for (int i = 0; i < productsTemp.size(); i++) {
            List<String> categoriesId = productsTemp.get(i).getCategories() != null ?
                    new ArrayList<>(productsTemp.get(i).getCategories().values()) :
                    new ArrayList<>();

            boolean isSelectedCategory = selectedCategoryId == null || selectedCategoryIndex == 0 ||
                    categoriesId.contains(selectedCategoryId);

            boolean isSearchedValue = searchValue == null || searchValue.trim().length() == 0 ||
                    productsTemp.get(i).getName().toLowerCase().contains(searchValue.trim().toLowerCase()) ||
                    productsCategoriesTemp.get(i).toLowerCase().contains(searchValue.trim().toLowerCase());

            if (isSelectedCategory && isSearchedValue) {
                products.add(productsTemp.get(i));
                productsCategories.add(productsCategoriesTemp.get(i));
            }
        }

        if (products.size() == 0)
            tvProductCaption.setVisibility(View.VISIBLE);
        else tvProductCaption.setVisibility(View.GONE);
        tvProductCaption.bringToFront();

        productAdapter.notifyDataSetChanged();
    }

    public void onBackPressed() {
        if (currentStep > 0)
            currentStep--;
        else
            super.onBackPressed();

        if (currentStep == 0) {
            productCategoriesLayout.setVisibility(View.GONE);
            productsLayout.setVisibility(View.VISIBLE);
        }
    }

    boolean isStopped = false;

    @Override
    public void onResume() {
        if (isStopped) {
            isListening = true;
            productsQuery.addListenerForSingleValueEvent(getProdValueListener());
            appInfoQuery.addListenerForSingleValueEvent(getAppInfoValueListener());
            isStopped = false;
        }

        super.onResume();
    }

    @Override
    public void onStop() {
        isListening = false;
        isStopped = true;

        super.onStop();
    }

    @Override
    public void onDestroy() {
        isListening = false;
        isStopped = true;

        super.onDestroy();
    }
}