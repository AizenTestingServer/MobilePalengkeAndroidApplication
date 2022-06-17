package com.example.mobilepalengke.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mobilepalengke.Adapters.MealPlanCategoryAdapter;
import com.example.mobilepalengke.DataClasses.MealPlanCategory;
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

public class MealPlanCategoriesFragment extends Fragment {

    RecyclerView recyclerView;
    TextView tvMealPlanCategoryCaption;

    Context context;

    LoadingDialog loadingDialog;
    MessageDialog messageDialog;

    FirebaseDatabase firebaseDatabase;

    boolean isListening = true;

    Query mealPlanCategoriesQuery;

    List<MealPlanCategory> mealPlanCategories = new ArrayList<>();

    MealPlanCategoryAdapter mealPlanCategoryAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_meal_plan_categories, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        tvMealPlanCategoryCaption = view.findViewById(R.id.tvMealPlanCategoryCaption);

        context = getContext();

        loadingDialog = new LoadingDialog(context);
        messageDialog = new MessageDialog(context);

        firebaseDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_RTDB_url));
        mealPlanCategoriesQuery = firebaseDatabase.getReference("mealPlanCategories").orderByChild("name");

        loadingDialog.showDialog();
        isListening = true;
        mealPlanCategoriesQuery.addValueEventListener(getMealPlanCatValueListener());

        GridLayoutManager gridLayoutManager2 = new GridLayoutManager(context, 3, GridLayoutManager.VERTICAL, false);
        mealPlanCategoryAdapter = new MealPlanCategoryAdapter(context, mealPlanCategories);
        recyclerView.setLayoutManager(gridLayoutManager2);
        recyclerView.setAdapter(mealPlanCategoryAdapter);

        return view;
    }

    private ValueEventListener getMealPlanCatValueListener() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    mealPlanCategories.clear();

                    if (snapshot.exists()) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            MealPlanCategory mealPlanCategory = dataSnapshot.getValue(MealPlanCategory.class);
                            if (mealPlanCategory != null)
                                mealPlanCategories.add(mealPlanCategory);
                        }
                    }

                    if (mealPlanCategories.size() == 0)
                        tvMealPlanCategoryCaption.setVisibility(View.VISIBLE);
                    else
                        tvMealPlanCategoryCaption.setVisibility(View.GONE);

                    mealPlanCategoryAdapter.notifyDataSetChanged();

                    loadingDialog.dismissDialog();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "mealPlanCategoriesQuery:onCancelled", error.toException());
                loadingDialog.dismissDialog();

                messageDialog.setTextCaption("Failed to get the meal plan categories.");
                messageDialog.setTextType(2);
                messageDialog.showDialog();
            }
        };
    }

    @Override
    public void onResume() {
        isListening = true;
        mealPlanCategoriesQuery.addValueEventListener(getMealPlanCatValueListener());

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