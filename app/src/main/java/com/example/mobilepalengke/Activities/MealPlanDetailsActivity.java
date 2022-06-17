package com.example.mobilepalengke.Activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mobilepalengke.DataClasses.MealPlan;
import com.example.mobilepalengke.DataClasses.MealPlanCategory;
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
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MealPlanDetailsActivity extends AppCompatActivity {

    ImageView cartIconImage;
    Button btnAddToCart;
    TextView tvCartCount;

    Context context;

    LoadingDialog loadingDialog;
    MessageDialog messageDialog;

    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;

    boolean isListening = true;

    Query mealPlansQuery, mealPlanCategoriesQuery, cartProductsQuery;

    List<MealPlanCategory> mealPlanCategories = new ArrayList<>();

    MealPlan currentMealPlan;

    List<MealPlan> relatedMealPlans = new ArrayList<>();

    String mealPlanId;

    String uid;
    int overallCartCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_plan_details);

        cartIconImage = findViewById(R.id.cartIconImage);
        tvCartCount = findViewById(R.id.tvCartCount);

        btnAddToCart = findViewById(R.id.btnAddToCart);

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
        cartProductsQuery = firebaseDatabase.getReference("cartList").child(uid);

        loadingDialog.showDialog();
        isListening = true;
        mealPlansQuery.addValueEventListener(getMealPlanValueListener());

        cartIconImage.setOnClickListener(view -> {
            Intent intent = new Intent(context, CartActivity.class);
            startActivity(intent);
        });
    }

    private ValueEventListener getMealPlanValueListener() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {

                    if (snapshot.exists())
                        currentMealPlan = snapshot.child(mealPlanId).getValue(MealPlan.class);

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        MealPlan mealPlan = dataSnapshot.getValue(MealPlan.class);
                        if (mealPlan != null && mealPlan.getCategories() != null)
                            for (Map.Entry<String, String> mapMealPlanCategories : mealPlan.getCategories().entrySet())
                                if (currentMealPlan != null && !currentMealPlan.getId().equals(mealPlan.getId()) &&
                                        currentMealPlan.getCategories().containsValue(mapMealPlanCategories.getValue())) {
                                    relatedMealPlans.add(mealPlan);
                                    break;
                                }
                    }
                }

                if (currentMealPlan != null) {
                    //Selected Meal Plan Details here
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
                Log.e("TAG: " + context.getClass(), "productsQuery:onCancelled", error.toException());
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
                            if (mealPlanCategory != null && categoryIds.contains(mealPlanCategory.getId()))
                                mealPlanCategories.add(mealPlanCategory);
                        }
                    }

                    cartProductsQuery.addValueEventListener(getCartValueListener());
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

    private ValueEventListener getCartValueListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    overallCartCount = 0;

                    if (snapshot.child("cartProducts").exists())
                        overallCartCount = (int) snapshot.child("cartProducts").getChildrenCount();

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
        mealPlansQuery.addValueEventListener(getMealPlanValueListener());

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