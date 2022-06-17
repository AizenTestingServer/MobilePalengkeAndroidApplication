package com.example.mobilepalengke.Activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mobilepalengke.Adapters.ProductAdapter;
import com.example.mobilepalengke.Adapters.ProductCategoryHorizontalAdapter;
import com.example.mobilepalengke.DataClasses.Cart;
import com.example.mobilepalengke.DataClasses.CartProduct;
import com.example.mobilepalengke.DataClasses.Product;
import com.example.mobilepalengke.DataClasses.ProductCategory;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ProductDetailsActivity extends AppCompatActivity {

    ImageView cartIconImage, imgProduct;
    ImageButton btnSubtractQty, btnAddQty;
    Button btnAddToCart;
    TextView tvCartCount, tvLabel, tvDescription, tvPrice, tvQty, tvProductCaption;
    RecyclerView recyclerView, recyclerView2;

    Context context;

    LoadingDialog loadingDialog;
    MessageDialog messageDialog;

    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;

    boolean isListening = true;

    Query productQuery, productCategoriesQuery, cartProductsQuery;

    List<ProductCategory> productCategories = new ArrayList<>();

    ProductCategoryHorizontalAdapter productCategoryHorizontalAdapter;

    Product currentProduct;

    String productId;

    int quantity;

    List<Product> relatedProducts = new ArrayList<>();
    List<CartProduct> cartProducts = new ArrayList<>();

    ProductAdapter productAdapter;

    String uid;
    int overallCartCount = 0;

    boolean isCartExisting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        cartIconImage = findViewById(R.id.cartIconImage);
        tvCartCount = findViewById(R.id.tvCartCount);

        imgProduct = findViewById(R.id.imgProduct);
        btnSubtractQty = findViewById(R.id.btnSubtractQty);
        btnAddQty = findViewById(R.id.btnAddQty);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        tvLabel = findViewById(R.id.tvLabel);
        tvPrice = findViewById(R.id.tvPrice);
        tvDescription = findViewById(R.id.tvDescription);
        tvQty = findViewById(R.id.tvQty);
        tvProductCaption = findViewById(R.id.tvProductCaption);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView2 = findViewById(R.id.recyclerView2);

        context = ProductDetailsActivity.this;

        loadingDialog = new LoadingDialog(context);
        messageDialog = new MessageDialog(context);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null)
            uid = firebaseUser.getUid();

        productId = getIntent().getStringExtra("productId");
        boolean isFromCart = getIntent().getBooleanExtra("isFromCart", false);

        firebaseDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_RTDB_url));
        productQuery = firebaseDatabase.getReference("products").orderByChild("name");
        productCategoriesQuery = firebaseDatabase.getReference("productCategories").orderByChild("name");
        cartProductsQuery = firebaseDatabase.getReference("cartList").child(uid);

        loadingDialog.showDialog();
        isListening = true;
        productQuery.addValueEventListener(getProdValueListener());

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,
                false);
        productCategoryHorizontalAdapter = new ProductCategoryHorizontalAdapter(context, productCategories);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(productCategoryHorizontalAdapter);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false);
        productAdapter = new ProductAdapter(context, relatedProducts);
        productAdapter.setProductAdapterListener(this::addToCart);
        recyclerView2.setLayoutManager(gridLayoutManager);
        recyclerView2.setAdapter(productAdapter);

        quantity = Integer.parseInt(tvQty.getText().toString());
        btnSubtractQty.setEnabled(quantity > 1);

        cartIconImage.setOnClickListener(view -> {
            if (isFromCart)
                onBackPressed();
            else {
                Intent intent = new Intent(context, CartActivity.class);
                intent.putExtra("productId", productId);
                startActivity(intent);
            }
        });

        btnSubtractQty.setOnClickListener(view -> {
            quantity = Integer.parseInt(tvQty.getText().toString());

            if (quantity != 1) {
                quantity -= 1;
                tvQty.setText(context.getString(R.string.qtyValue, quantity));
            }

            btnSubtractQty.setEnabled(quantity > 1);
        });

        btnAddQty.setOnClickListener(view -> {
            quantity = Integer.parseInt(tvQty.getText().toString());

            quantity += 1;
            tvQty.setText(context.getString(R.string.qtyValue, quantity));

            btnSubtractQty.setEnabled(quantity > 1);
        });

        btnAddToCart.setOnClickListener(view -> {
            quantity = Integer.parseInt(tvQty.getText().toString());

            addToCart(currentProduct, quantity);

            quantity = 1;
            tvQty.setText(context.getString(R.string.qtyValue, quantity));
        });
    }

    private ValueEventListener getProdValueListener() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    relatedProducts.clear();

                    if (snapshot.exists())
                        currentProduct = snapshot.child(productId).getValue(Product.class);

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Product product = dataSnapshot.getValue(Product.class);
                        if (product != null)
                            for (Map.Entry<String, String> mapProductCategories : product.getCategories().entrySet())
                                if (currentProduct != null && !currentProduct.getId().equals(product.getId()) &&
                                        currentProduct.getCategories().containsValue(mapProductCategories.getValue())) {
                                    relatedProducts.add(product);
                                    break;
                                }
                    }
                }

                if (currentProduct != null) {
                    tvLabel.setText(currentProduct.getName());
                    tvPrice.setText(context.getString(R.string.priceValue, currentProduct.getPrice()));
                    Picasso.get().load(currentProduct.getImg()).placeholder(R.drawable.ic_image_blue)
                            .error(R.drawable.ic_broken_image_red).into(imgProduct);

                    String description = "";
                    for (Map.Entry<String, String> mapDescription : currentProduct.getDescriptions().entrySet())
                        description += "• " + mapDescription.getValue() + "\n";

                    if (description.trim().length() > 0)
                        tvDescription.setVisibility(View.VISIBLE);
                    else tvDescription.setVisibility(View.GONE);

                    tvDescription.setText(description.trim());
                }

                Collections.shuffle(relatedProducts);

                List<Product> relatedProductsCopy = new ArrayList<>(relatedProducts);
                relatedProducts.clear();
                for (int i = 0; i < Math.min(4, relatedProductsCopy.size()); i++)
                    relatedProducts.add(relatedProductsCopy.get(i));

                if (relatedProducts.size() == 0)
                    tvProductCaption.setVisibility(View.VISIBLE);
                else tvProductCaption.setVisibility(View.GONE);
                tvProductCaption.bringToFront();

                productAdapter.notifyDataSetChanged();

                productCategoriesQuery.addValueEventListener(getProdCatValueListener());
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
                    productCategories.clear();

                    List<String> categoryIds = new ArrayList<>(currentProduct.getCategories().values());

                    if (snapshot.exists()) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            ProductCategory productCategory = dataSnapshot.getValue(ProductCategory.class);
                            if (productCategory != null && categoryIds.contains(productCategory.getId()))
                                productCategories.add(productCategory);
                        }
                    }

                    productCategoryHorizontalAdapter.notifyDataSetChanged();

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

    private void addToCart(Product product, int quantity) {
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
    }

    @Override
    public void onResume() {
        isListening = true;
        productQuery.addValueEventListener(getProdValueListener());

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