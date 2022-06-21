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
import android.widget.TextView;
import android.widget.Toast;

import com.example.mobilepalengke.Adapters.IconOptionAdapter;
import com.example.mobilepalengke.Adapters.MealPlanAdapter;
import com.example.mobilepalengke.DataClasses.AppInfo;
import com.example.mobilepalengke.DataClasses.CheckableItem;
import com.example.mobilepalengke.DataClasses.IconOption;
import com.example.mobilepalengke.DataClasses.MealPlan;
import com.example.mobilepalengke.DataClasses.MealPlanCategory;
import com.example.mobilepalengke.DialogClasses.DownloadDialog;
import com.example.mobilepalengke.DialogClasses.LoadingDialog;
import com.example.mobilepalengke.DialogClasses.MealPlanPrimaryDetailsDialog;
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
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AdminMealPlansActivity extends AppCompatActivity {

    ConstraintLayout mealPlansLayout, mealPlanCategoriesLayout;
    EditText etSearchMealPlan;
    TextView tvSelectedCategory, btnChangeCategory, tvMealPlanCaption;
    Button btnAddMealPlan, btnBack;
    RecyclerView recyclerView, recyclerView2;

    Context context;

    LoadingDialog loadingDialog;
    MessageDialog messageDialog;
    MealPlanPrimaryDetailsDialog mealPlanPrimaryDetailsDialog;
    DownloadDialog downloadDialog;
    StatusDialog statusDialog;

    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;

    boolean isListening = true;

    int currentStep = 0, maxStep = 1;

    String searchValue;

    Query mealPlansQuery, mealPlanCategoriesQuery, appInfoQuery;

    List<MealPlan> mealPlans = new ArrayList<>(), mealPlansCopy = new ArrayList<>();
    List<String> mealPlansCategories = new ArrayList<>(), mealPlanCategoriesCopy = new ArrayList<>();

    MealPlanAdapter mealPlanAdapter;

    IconOptionAdapter iconOptionAdapter;

    List<IconOption> mealPlanCategories = new ArrayList<>();
    List<String> mealPlanCategoriesId = new ArrayList<>();

    int selectedCategoryIndex = 0;
    String selectedCategoryId;

    int overallMealPlanCount = 0;

    String uid;

    List<CheckableItem> mealPlanCategoriesCheckableItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_meal_plans);

        mealPlansLayout = findViewById(R.id.mealPlansLayout);
        mealPlanCategoriesLayout = findViewById(R.id.mealPlanCategoriesLayout);

        etSearchMealPlan = findViewById(R.id.etSearchMealPlan);
        tvSelectedCategory = findViewById(R.id.tvSelectedCategory);
        btnChangeCategory = findViewById(R.id.btnChangeCategory);
        recyclerView = findViewById(R.id.recyclerView);
        tvMealPlanCaption = findViewById(R.id.tvMealPlanCaption);

        recyclerView2 = findViewById(R.id.recyclerView2);
        btnAddMealPlan = findViewById(R.id.btnAddMealPlan);
        btnBack = findViewById(R.id.btnBack);

        context = AdminMealPlansActivity.this;

        loadingDialog = new LoadingDialog(context);
        messageDialog = new MessageDialog(context);
        mealPlanPrimaryDetailsDialog = new MealPlanPrimaryDetailsDialog(context);
        downloadDialog = new DownloadDialog(context);
        statusDialog = new StatusDialog(context);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null)
            uid = firebaseUser.getUid();

        selectedCategoryId = getIntent().getStringExtra("selectedCategoryId");

        firebaseDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_RTDB_url));
        mealPlansQuery = firebaseDatabase.getReference("mealPlans").orderByChild("name");
        mealPlanCategoriesQuery = firebaseDatabase.getReference("mealPlanCategories").orderByChild("name");
        appInfoQuery = firebaseDatabase.getReference("appInfo");

        loadingDialog.showDialog();
        isListening = true;
        mealPlansQuery.addValueEventListener(getMealPlanValueListener());
        appInfoQuery.addValueEventListener(getAppInfoValueListener());

        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false);
        mealPlanAdapter = new MealPlanAdapter(context, mealPlans);
        mealPlanAdapter.setMealPlanAdapterListener(mealPlan -> {
            Intent intent = new Intent(context, AdminMealPlanDetailsActivity.class);
            intent.putExtra("mealPlanId", mealPlan.getId());
            context.startActivity(intent);
        });
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(mealPlanAdapter);

        iconOptionAdapter = new IconOptionAdapter(context, mealPlanCategories);
        iconOptionAdapter.setIconOptionAdapterListener(new IconOptionAdapter.IconOptionAdapterListener() {
            @Override
            public void onClick(IconOption iconOption) {

            }

            @Override
            public void onClick(IconOption iconOption, int position) {
                currentStep--;

                tvSelectedCategory.setText(iconOption.getLabelName());

                selectedCategoryIndex = position;
                selectedCategoryId = mealPlanCategoriesId.get(position);

                mealPlanCategoriesLayout.setVisibility(View.GONE);
                mealPlansLayout.setVisibility(View.VISIBLE);

                filterMealPlans();
            }
        });
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL,
                false);
        recyclerView2.setLayoutManager(linearLayoutManager2);
        recyclerView2.setAdapter(iconOptionAdapter);

        etSearchMealPlan.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                searchValue = editable.toString();

                filterMealPlans();
            }
        });

        btnAddMealPlan.setOnClickListener(view -> mealPlanPrimaryDetailsDialog.showDialog(mealPlanCategoriesCheckableItems));

        mealPlanPrimaryDetailsDialog.setDialogListener(mealPlan -> {
            loadingDialog.showDialog();

            String mealPlanId = mealPlan.getId();

            if (mealPlanId == null) {
                StringBuilder idBuilder = new StringBuilder("mlp");

                for (int i = 0; i < 7 - String.valueOf(overallMealPlanCount + 1).length(); i++)
                    idBuilder.append("0");
                idBuilder.append(overallMealPlanCount + 1);

                mealPlanId = String.valueOf(idBuilder);
            }

            mealPlan.setId(mealPlanId);

            String toastMessage = "Successfully added the meal plan.";

            mealPlansQuery.getRef().child(mealPlanId).setValue(mealPlan).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(
                            context,
                            toastMessage,
                            Toast.LENGTH_SHORT
                    ).show();

                    mealPlanPrimaryDetailsDialog.dismissDialog();
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
        });

        btnChangeCategory.setOnClickListener(view1 -> {
            if (currentStep < maxStep)
                currentStep++;

            mealPlansLayout.setVisibility(View.GONE);
            mealPlanCategoriesLayout.setVisibility(View.VISIBLE);
        });

        btnBack.setOnClickListener(view1 -> {
            currentStep--;

            mealPlanCategoriesLayout.setVisibility(View.GONE);
            mealPlansLayout.setVisibility(View.VISIBLE);
        });
    }

    private ValueEventListener getMealPlanValueListener() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    overallMealPlanCount = 0;
                    mealPlans.clear();

                    if (snapshot.exists()) {
                        overallMealPlanCount = (int) snapshot.getChildrenCount();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            MealPlan mealPlan = dataSnapshot.getValue(MealPlan.class);
                            if (mealPlan != null)
                                mealPlans.add(mealPlan);
                        }
                    }

                    mealPlanCategoriesQuery.addValueEventListener(getMealPlanCatValueListener());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "mealPlansQuery:onCancelled", error.toException());
                loadingDialog.dismissDialog();

                messageDialog.setTextCaption("Failed to get the mealPlans.");
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
                    mealPlansCategories.clear();
                    mealPlanCategories.clear();
                    mealPlanCategoriesId.clear();
                    mealPlanCategoriesCheckableItems.clear();

                    if (snapshot.exists()) {
                        mealPlanCategories.add(new IconOption(getString(R.string.all), 0));
                        mealPlanCategoriesId.add("mlpCat00");

                        for (MealPlan mealPlan : mealPlans) {
                            List<String> categoryIds = mealPlan.getCategories() != null ?
                                    new ArrayList<>(mealPlan.getCategories().values()) :
                                    new ArrayList<>();
                            List<String> categories = new ArrayList<>();

                            for (String categoryId : categoryIds)
                                categories.add(snapshot.child(categoryId.trim())
                                        .child("name").getValue(String.class));

                            mealPlansCategories.add(TextUtils.join(", ", categories));
                        }

                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            mealPlanCategories.add(new IconOption(dataSnapshot.child("name").getValue(String.class), 0));
                            mealPlanCategoriesId.add(dataSnapshot.getKey());

                            if (selectedCategoryId != null && selectedCategoryId.equals(dataSnapshot.getKey())) {
                                selectedCategoryIndex = mealPlanCategoriesId.size() - 1;
                                tvSelectedCategory.setText(mealPlanCategories.get(selectedCategoryIndex).getLabelName());
                            }

                            MealPlanCategory mealPlanCategory = dataSnapshot.getValue(MealPlanCategory.class);
                            if (mealPlanCategory != null) {
                                CheckableItem checkableItem = new CheckableItem(mealPlanCategory.getId(), mealPlanCategory.getName());
                                mealPlanCategoriesCheckableItems.add(checkableItem);
                            }
                        }
                    }

                    mealPlansCopy.clear();
                    mealPlanCategoriesCopy.clear();

                    mealPlansCopy.addAll(mealPlans);
                    mealPlanCategoriesCopy.addAll(mealPlansCategories);

                    filterMealPlans();

                    iconOptionAdapter.notifyDataSetChanged();

                    loadingDialog.dismissDialog();
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
    private void filterMealPlans() {
        List<MealPlan> mealPlansTemp = new ArrayList<>(mealPlansCopy);
        List<String> mealPlanCategoriesTemp = new ArrayList<>(mealPlanCategoriesCopy);

        mealPlans.clear();
        mealPlansCategories.clear();

        for (int i = 0; i < mealPlansTemp.size(); i++) {
            List<String> categoriesId = mealPlansTemp.get(i).getCategories() != null ?
                    new ArrayList<>(mealPlansTemp.get(i).getCategories().values()) :
                    new ArrayList<>();

            boolean isSelectedCategory = selectedCategoryId == null || selectedCategoryIndex == 0 ||
                    categoriesId.contains(selectedCategoryId);

            boolean isSearchedValue = searchValue == null || searchValue.trim().length() == 0 ||
                    mealPlansTemp.get(i).getName().toLowerCase().contains(searchValue.trim().toLowerCase()) ||
                    mealPlanCategoriesTemp.get(i).toLowerCase().contains(searchValue.trim().toLowerCase());

            if (isSelectedCategory && isSearchedValue) {
                mealPlans.add(mealPlansTemp.get(i));
                mealPlansCategories.add(mealPlanCategoriesTemp.get(i));
            }
        }

        if (mealPlans.size() == 0)
            tvMealPlanCaption.setVisibility(View.VISIBLE);
        else tvMealPlanCaption.setVisibility(View.GONE);
        tvMealPlanCaption.bringToFront();

        mealPlanAdapter.notifyDataSetChanged();
    }

    public void onBackPressed() {
        if (currentStep > 0)
            currentStep--;
        else
            super.onBackPressed();

        if (currentStep == 0) {
            mealPlanCategoriesLayout.setVisibility(View.GONE);
            mealPlansLayout.setVisibility(View.VISIBLE);
        }
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