package com.example.mobilepalengke.Fragments;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mobilepalengke.Adapters.CategoriesFragmentAdapter;
import com.example.mobilepalengke.R;
import com.google.android.material.tabs.TabLayout;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

public class CategoriesFragment extends Fragment {

    TabLayout tabLayout;
    ViewPager2 viewPager2;

    Context context;

    FragmentActivity fragmentActivity;

    ProductCategoriesFragment productCategoriesFragment = new ProductCategoriesFragment();
    MealPlanCategoriesFragment mealPlanCategoriesFragment = new MealPlanCategoriesFragment();

    CategoriesFragmentAdapter categoriesFragmentAdapter;

    int selectedTab;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_categories, container, false);

        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager2 = view.findViewById(R.id.viewPager2);

        fragmentActivity = getActivity();
        context = getContext();

        if (fragmentActivity != null) {
            FragmentManager fragmentManager = fragmentActivity.getSupportFragmentManager();

            categoriesFragmentAdapter = new CategoriesFragmentAdapter(fragmentManager, getLifecycle(),
                    productCategoriesFragment, mealPlanCategoriesFragment);
            viewPager2.setAdapter(categoriesFragmentAdapter);

            Typeface typeface = ResourcesCompat.getFont(context, R.font.poppins_regular);

            ViewGroup viewGroup = (ViewGroup) tabLayout.getChildAt(0);
            int tabCount = viewGroup.getChildCount();

            for (int i = 0; i < tabCount; i++) {
                ViewGroup viewGroup1 = (ViewGroup) viewGroup.getChildAt(i);
                int tabChildrenCount = viewGroup1.getChildCount();

                for (int j = 0; j < tabChildrenCount; j++) {
                    View view1 = viewGroup1.getChildAt(j);
                    if (view1 instanceof TextView)
                        ((TextView) view1).setTypeface(typeface);
                }
            }

            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    selectedTab = tab.getPosition();
                    viewPager2.setCurrentItem(selectedTab);
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {

                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {

                }
            });

            viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    tabLayout.selectTab(tabLayout.getTabAt(position));
                }
            });
        } else {
            tabLayout.setVisibility(View.GONE);
            viewPager2.setVisibility(View.GONE);
            Log.e("TAG: " + context.getClass(), "fragmentActivity:null");
        }

        return view;
    }
}