package com.example.mobilepalengke.Activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mobilepalengke.Adapters.AdminMealPlanProductAdapter;
import com.example.mobilepalengke.DataClasses.AppInfo;
import com.example.mobilepalengke.DataClasses.CheckableItem;
import com.example.mobilepalengke.DataClasses.MealPlan;
import com.example.mobilepalengke.DataClasses.MealPlanCategory;
import com.example.mobilepalengke.DataClasses.Product;
import com.example.mobilepalengke.DialogClasses.AddProductToMealPlanDialog;
import com.example.mobilepalengke.DialogClasses.ConfirmationDialog;
import com.example.mobilepalengke.DialogClasses.DownloadDialog;
import com.example.mobilepalengke.DialogClasses.LoadingDialog;
import com.example.mobilepalengke.DialogClasses.MealPlanOverviewDialog;
import com.example.mobilepalengke.DialogClasses.MealPlanPrimaryDetailsDialog;
import com.example.mobilepalengke.DialogClasses.MessageDialog;
import com.example.mobilepalengke.DialogClasses.StatusDialog;
import com.example.mobilepalengke.DialogClasses.StringValuesDialog;
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

public class AdminMealPlanDetailsActivity extends AppCompatActivity {

    ImageView imgMealPlan;
    TextView tvLabel, tvDescription, textView6, textView7, textView8, textView9,
            tvIngredients, tvInstructions, tvProductCaption;
    RecyclerView recyclerView;
    Button btnUpdatePrimaryDetails, btnUpdateOverview, btnUpdateIngredients,
            btnUpdateInstructions, btnAddProduct;

    Context context;

    LoadingDialog loadingDialog;
    MessageDialog messageDialog;
    ConfirmationDialog confirmationDialog;
    MealPlanPrimaryDetailsDialog mealPlanPrimaryDetailsDialog;
    MealPlanOverviewDialog mealPlanOverviewDialog;
    AddProductToMealPlanDialog addProductToMealPlanDialog;
    StringValuesDialog stringValuesDialog;
    DownloadDialog downloadDialog;
    StatusDialog statusDialog;

    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;

    boolean isListening = true;

    Query mealPlansQuery, mealPlanCategoriesQuery, productsQuery, cartProductsQuery, appInfoQuery;

    List<MealPlanCategory> mealPlanCategories = new ArrayList<>();

    MealPlan currentMealPlan;

    List<MealPlan> relatedMealPlans = new ArrayList<>();
    List<Product> products = new ArrayList<>();

    int overallMealPlanCount = 0;

    AdminMealPlanProductAdapter adminMealPlanProductAdapter;

    String mealPlanId;

    String uid;

    List<CheckableItem> mealPlanCategoriesCheckableItems = new ArrayList<>();

    Product selectedProduct;

    Map<String, String> mapIngredients = new HashMap<>(), mapInstructions = new HashMap<>();

    int selectedValue = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_meal_plan_details);

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

        btnUpdatePrimaryDetails = findViewById(R.id.btnUpdatePrimaryDetails);
        btnUpdateOverview = findViewById(R.id.btnUpdateOverview);
        btnUpdateIngredients = findViewById(R.id.btnUpdateIngredients);
        btnUpdateInstructions = findViewById(R.id.btnUpdateInstructions);
        btnAddProduct = findViewById(R.id.btnAddProduct);

        context = AdminMealPlanDetailsActivity.this;

        loadingDialog = new LoadingDialog(context);
        messageDialog = new MessageDialog(context);
        confirmationDialog = new ConfirmationDialog(context);
        mealPlanPrimaryDetailsDialog = new MealPlanPrimaryDetailsDialog(context);
        mealPlanOverviewDialog = new MealPlanOverviewDialog(context);
        addProductToMealPlanDialog = new AddProductToMealPlanDialog(context);
        stringValuesDialog = new StringValuesDialog(context);
        downloadDialog = new DownloadDialog(context);
        statusDialog = new StatusDialog(context);

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
        appInfoQuery = firebaseDatabase.getReference("appInfo");

        loadingDialog.showDialog();
        isListening = true;
        mealPlansQuery.addValueEventListener(getMealPlanValueListener());
        appInfoQuery.addValueEventListener(getAppInfoValueListener());

        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false);
        adminMealPlanProductAdapter = new AdminMealPlanProductAdapter(context, products);
        adminMealPlanProductAdapter.setMealPlanProductAdapterListener(new AdminMealPlanProductAdapter.AdminMealPlanProductAdapterListener() {
            @Override
            public void onClick(Product product) {

            }

            @Override
            public void removeFromMealPlan(Product product) {
                selectedProduct = product;
                confirmationDialog.setTextCaption("Do you want to remove the product from meal plan?");
                confirmationDialog.showDialog();
            }
        });
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adminMealPlanProductAdapter);

        confirmationDialog.setDialogListener(() -> {
            loadingDialog.showDialog();

            mealPlansQuery.getRef().child(currentMealPlan.getId()).child("products").
                    child(selectedProduct.getId()).removeValue().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(
                                    context,
                                    "Successfully removed from meal plan.",
                                    Toast.LENGTH_LONG).show();
                        } else
                            Toast.makeText(
                                    context,
                                    "Failed to remove the product from meal plan.",
                                    Toast.LENGTH_LONG).show();

                        loadingDialog.dismissDialog();

                        confirmationDialog.dismissDialog();
                    });
        });

        btnUpdatePrimaryDetails.setOnClickListener(view -> {
            mealPlanPrimaryDetailsDialog.showDialog(mealPlanCategoriesCheckableItems);
            mealPlanPrimaryDetailsDialog.setData(currentMealPlan);
        });

        mealPlanPrimaryDetailsDialog.setDialogListener(this::updateMealPlan);

        btnUpdateOverview.setOnClickListener(view -> {
            mealPlanOverviewDialog.showDialog();
            mealPlanOverviewDialog.setData(currentMealPlan);
        });

        mealPlanOverviewDialog.setDialogListener(this::updateMealPlan);

        btnUpdateIngredients.setOnClickListener(view -> {
            selectedValue = 0;
            stringValuesDialog.showDialog(getString(R.string.ingredients));
            stringValuesDialog.setData(mapIngredients);
        });

        btnUpdateInstructions.setOnClickListener(view ->{
            selectedValue = 1;
            stringValuesDialog.showDialog(getString(R.string.descriptions));
            stringValuesDialog.setData(mapInstructions);
        });

        stringValuesDialog.setDialogListener(mapValues -> {
            if (selectedValue == 1) {
                mapInstructions.clear();
                mapValues.forEach((s, s2) -> mapInstructions.put("ins" + s, s2));
                currentMealPlan.setInstructions(mapInstructions);
            } else {
                mapIngredients.clear();
                mapValues.forEach((s, s2) -> mapIngredients.put("ingr" + s, s2));
                currentMealPlan.setIngredients(mapIngredients);
            }

            updateMealPlan(currentMealPlan);
            stringValuesDialog.dismissDialog();
        });

        btnAddProduct.setOnClickListener(view -> addProductToMealPlanDialog.showDialog());

        addProductToMealPlanDialog.setDialogListener(product -> {
            loadingDialog.showDialog();

            mealPlansQuery.getRef().child(currentMealPlan.getId()).child("products").
                    child(product.getId()).setValue(product.getId()).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(
                                    context,
                                    "Successfully added to meal plan.",
                                    Toast.LENGTH_LONG).show();
                        } else
                            Toast.makeText(
                                    context,
                                    "Failed to add the product to meal plan.",
                                    Toast.LENGTH_LONG).show();

                        loadingDialog.dismissDialog();

                        addProductToMealPlanDialog.dismissDialog();
                    });
        });
    }

    private void updateMealPlan(MealPlan mealPlan) {
        loadingDialog.showDialog();

        String mealPlanId = mealPlan.getId();

        String toastMessage = "Successfully updated the meal plan.";

        mealPlansQuery.getRef().child(mealPlanId).setValue(mealPlan).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(
                        context,
                        toastMessage,
                        Toast.LENGTH_SHORT
                ).show();

                mealPlanPrimaryDetailsDialog.dismissDialog();
                mealPlanOverviewDialog.dismissDialog();
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
    }

    private ValueEventListener getMealPlanValueListener() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    overallMealPlanCount = 0;
                    if (snapshot.exists()) {
                        overallMealPlanCount = (int) snapshot.getChildrenCount();
                        
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

                if (currentMealPlan != null) {try {
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

                    mapIngredients.clear();
                    for (String ingredient : ingredrientList) {
                        ingredients += "• " + ingredient + "\n";

                        String keyName = String.valueOf(mapIngredients.size() + 1).length() < 2
                                ? "0" + (mapIngredients.size() + 1)
                                : String.valueOf((mapIngredients.size() + 1));

                        mapIngredients.put(keyName, ingredient);
                    }

                    if (ingredients.trim().length() > 0)
                        tvIngredients.setText(ingredients.trim());
                    else tvIngredients.setText(getString(R.string.defaultRecordCaption));

                    List<String> instructionList = new ArrayList<>();
                    String instructions = "";

                    if (currentMealPlan.getInstructions() != null)
                        instructionList = new ArrayList<>(currentMealPlan.getInstructions().values());

                    instructionList.sort(String::compareToIgnoreCase);

                    mapInstructions.clear();
                    for (String instruction : instructionList) {
                        instructions += "• " + instruction + "\n";

                        String keyName = String.valueOf(mapInstructions.size() + 1).length() < 2
                                ? "0" + (mapInstructions.size() + 1)
                                : String.valueOf((mapInstructions.size() + 1));

                        mapInstructions.put(keyName, instruction);
                    }

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
                    mealPlanCategoriesCheckableItems.clear();

                    List<String> categoryIds = currentMealPlan.getCategories() != null ?
                            new ArrayList<>(currentMealPlan.getCategories().values()) :
                            new ArrayList<>();

                    if (snapshot.exists()) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            MealPlanCategory mealPlanCategory = dataSnapshot.getValue(MealPlanCategory.class);
                            if (mealPlanCategory != null  && categoryIds.contains(mealPlanCategory.getId()))
                                mealPlanCategories.add(mealPlanCategory);

                            if (mealPlanCategory != null) {
                                CheckableItem checkableItem = new CheckableItem(mealPlanCategory.getId(), mealPlanCategory.getName());
                                mealPlanCategoriesCheckableItems.add(checkableItem);
                            }
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
                            if (product != null && product.getCategories() != null)
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

                adminMealPlanProductAdapter.notifyDataSetChanged();

                loadingDialog.dismissDialog();
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

    @Override
    public void onResume() {
        isListening = true;
        mealPlansQuery.addListenerForSingleValueEvent(getMealPlanValueListener());
        appInfoQuery.addListenerForSingleValueEvent(getAppInfoValueListener());

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