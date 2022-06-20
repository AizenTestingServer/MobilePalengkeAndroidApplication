package com.example.mobilepalengke.Activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mobilepalengke.Adapters.ProductAdapter;
import com.example.mobilepalengke.DataClasses.Cart;
import com.example.mobilepalengke.DataClasses.CartProduct;
import com.example.mobilepalengke.DataClasses.MealPlan;
import com.example.mobilepalengke.DataClasses.MealPlanCategory;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MealPlanDetailsActivity extends AppCompatActivity {

    ImageView cartIconImage, imgMealPlan;
    TextView tvCartCount, tvLabel, tvDescription, textView6, textView7, textView8, textView9,
            tvIngredients, tvInstructions, tvProductCaption;
    RecyclerView recyclerView;

    Context context;

    LoadingDialog loadingDialog;
    MessageDialog messageDialog;

    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;

    boolean isListening = true;

    Query mealPlansQuery, mealPlanCategoriesQuery, productsQuery, cartProductsQuery;

    List<MealPlanCategory> mealPlanCategories = new ArrayList<>();

    MealPlan currentMealPlan;

    List<MealPlan> relatedMealPlans = new ArrayList<>();
    List<Product> products = new ArrayList<>();
    List<CartProduct> cartProducts = new ArrayList<>();

    ProductAdapter productAdapter;

    String mealPlanId;

    String uid;
    int overallCartCount = 0;

    boolean isCartExisting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_plan_details);

        cartIconImage = findViewById(R.id.cartIconImage);
        tvCartCount = findViewById(R.id.tvCartCount);
        imgMealPlan = findViewById(R.id.imgMealPlan);
        tvLabel = findViewById(R.id.tvLabel);
        tvDescription = findViewById(R.id.tvDescription);
        textView6 = findViewById(R.id.textView6);
        textView7 = findViewById(R.id.textView7);
        textView8 = findViewById(R.id.textView8);
        textView9 = findViewById(R.id.textView9);
        tvIngredients = findViewById(R.id.tvIngredients);
        tvInstructions = findViewById(R.id.tvInstructions);
        recyclerView = findViewById(R.id.recyclerView);
        tvProductCaption = findViewById(R.id.tvProductCaption);

        context = MealPlanDetailsActivity.this;

        loadingDialog = new LoadingDialog(context);
        messageDialog = new MessageDialog(context);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null)
            uid = firebaseUser.getUid();

        mealPlanId = getIntent().getStringExtra("mealPlanId");

        firebaseDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_RTDB_url));
        mealPlansQuery = firebaseDatabase.getReference("mealPlans").orderByChild("name");
        mealPlanCategoriesQuery = firebaseDatabase.getReference("mealPlanCategories").orderByChild("name");
        productsQuery = firebaseDatabase.getReference("products").orderByChild("name");
        cartProductsQuery = firebaseDatabase.getReference("cartList").child(uid);

        loadingDialog.showDialog();
        isListening = true;
        mealPlansQuery.addValueEventListener(getMealPlanValueListener());

        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false);
        productAdapter = new ProductAdapter(context, products);
        productAdapter.setProductAdapterListener(this::addToCart);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(productAdapter);

        cartIconImage.setOnClickListener(view -> {
            Intent intent = new Intent(context, CartActivity.class);
            startActivity(intent);
        });
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

    private ValueEventListener getMealPlanValueListener() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    if (snapshot.exists()) {
                        currentMealPlan = snapshot.child(mealPlanId).getValue(MealPlan.class);

                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            MealPlan mealPlan = dataSnapshot.getValue(MealPlan.class);
                            if (mealPlan != null && mealPlan.getCategories() != null)
                                for (Map.Entry<String, String> mapMealPlanCategories : mealPlan.getCategories().entrySet())
                                    if (currentMealPlan != null && !currentMealPlan.getId().equals(mealPlan.getId()) &&
                                            currentMealPlan.getCategories() != null &&
                                            currentMealPlan.getCategories().containsValue(mapMealPlanCategories.getValue())) {
                                        relatedMealPlans.add(mealPlan);
                                        break;
                                    }
                        }
                    }
                }

                if (currentMealPlan != null) {
                    try {
                        Glide.with(context).load(currentMealPlan.getImg()).centerCrop().placeholder(R.drawable.ic_image_blue).
                                error(R.drawable.ic_broken_image_red).into(imgMealPlan);
                    } catch (Exception ex) {}

                    tvLabel.setText(currentMealPlan.getName());

                    String description = currentMealPlan.getDescription() != null ?
                            currentMealPlan.getDescription() : "";

                    if (description.trim().length() > 0)
                        tvDescription.setVisibility(View.VISIBLE);
                    else tvDescription.setVisibility(View.GONE);

                    tvDescription.setText(description.trim());

                    int prepTime = currentMealPlan.getPrepTime();
                    int cookTime = currentMealPlan.getCookTime();
                    int totalTime = prepTime + cookTime;
                    int servings = currentMealPlan.getServings();

                    textView6.setText(getString(R.string.minutesValue, prepTime,
                            prepTime > 1 ? "s" : ""));
                    textView7.setText(getString(R.string.minutesValue, cookTime,
                            cookTime > 1 ? "s" : ""));
                    textView8.setText(getString(R.string.minutesValue, totalTime,
                            totalTime > 1 ? "s" : ""));
                    textView9.setText(getString(R.string.servingsValue, servings,
                            servings > 1 ? "people" : "person"));

                    List<String> ingredrientList = new ArrayList<>();
                    String ingredients = "";

                    if (currentMealPlan.getIngredients() != null)
                        ingredrientList = new ArrayList<>(currentMealPlan.getIngredients().values());

                    ingredrientList.sort(String::compareToIgnoreCase);

                    for (String ingredient : ingredrientList)
                        ingredients += "• " + ingredient + "\n";

                    if (ingredients.trim().length() > 0)
                        tvIngredients.setText(ingredients.trim());
                    else tvIngredients.setText(getString(R.string.defaultRecordCaption));

                    List<String> instructionList = new ArrayList<>();
                    String instructions = "";

                    if (currentMealPlan.getInstructions() != null)
                        instructionList = new ArrayList<>(currentMealPlan.getInstructions().values());

                    instructionList.sort(String::compareToIgnoreCase);

                    for (String instruction : instructionList)
                        instructions += "• " + instruction + "\n";

                    if (instructions.trim().length() > 0)
                        tvInstructions.setText(instructions.trim());
                    else tvInstructions.setText(getString(R.string.defaultRecordCaption));
                }

                Collections.shuffle(relatedMealPlans);

                List<MealPlan> relatedMealPlansCopy = new ArrayList<>(relatedMealPlans);
                relatedMealPlans.clear();
                for (int i = 0; i < Math.min(4, relatedMealPlansCopy.size()); i++)
                    relatedMealPlans.add(relatedMealPlansCopy.get(i));

                mealPlanCategoriesQuery.addValueEventListener(getMealPlanCatValueListener());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "mealPlansQuery:onCancelled", error.toException());
                loadingDialog.dismissDialog();

                messageDialog.setTextCaption("Failed to get the products.");
                messageDialog.setTextType(2);
                messageDialog.showDialog();
            }
        };
    }

    private ValueEventListener getMealPlanCatValueListener() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    mealPlanCategories.clear();

                    List<String> categoryIds = currentMealPlan.getCategories() != null ?
                            new ArrayList<>(currentMealPlan.getCategories().values()) :
                            new ArrayList<>();

                    if (snapshot.exists()) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            MealPlanCategory mealPlanCategory = dataSnapshot.getValue(MealPlanCategory.class);
                            if (mealPlanCategory != null && !mealPlanCategory.isDeactivated() &&
                                    categoryIds.contains(mealPlanCategory.getId()))
                                mealPlanCategories.add(mealPlanCategory);
                        }
                    }

                    productsQuery.addValueEventListener(getProdValueListener());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "mealPlanCategoriesQuery:onCancelled", error.toException());
                loadingDialog.dismissDialog();

                messageDialog.setTextCaption("Failed to get the product categories.");
                messageDialog.setTextType(2);
                messageDialog.showDialog();
            }
        };
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
                            if (product != null && product.getCategories() != null && !product.isDeactivated())
                                if (currentMealPlan.getProducts() != null) {
                                    for (Map.Entry<String, String> mapProduct : currentMealPlan.getProducts().entrySet()) {
                                        if (mapProduct.getValue().equals(product.getId())) {
                                            products.add(product);
                                            break;
                                        }
                                    }
                                }
                        }
                    }
                }

                if (products.size() == 0)
                    tvProductCaption.setVisibility(View.VISIBLE);
                else tvProductCaption.setVisibility(View.GONE);
                tvProductCaption.bringToFront();

                productAdapter.notifyDataSetChanged();

                cartProductsQuery.addValueEventListener(getCartValueListener());
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

    @Override
    public void onResume() {
        isListening = true;
        mealPlansQuery.addListenerForSingleValueEvent(getMealPlanValueListener());

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