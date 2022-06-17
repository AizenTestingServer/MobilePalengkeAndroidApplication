package com.example.mobilepalengke.Adapters;

import com.example.mobilepalengke.Fragments.MealPlanCategoriesFragment;
import com.example.mobilepalengke.Fragments.ProductCategoriesFragment;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class CategoriesFragmentAdapter extends FragmentStateAdapter {

    public ProductCategoriesFragment productCategoriesFragment;
    public MealPlanCategoriesFragment mealPlanCategoriesFragment;

    public CategoriesFragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle,
            ProductCategoriesFragment productCategoriesFragment, MealPlanCategoriesFragment mealPlanCategoriesFragment) {
        super(fragmentManager, lifecycle);

        this.productCategoriesFragment = productCategoriesFragment;
        this.mealPlanCategoriesFragment = mealPlanCategoriesFragment;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 1)
            return mealPlanCategoriesFragment;
        return productCategoriesFragment;
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
