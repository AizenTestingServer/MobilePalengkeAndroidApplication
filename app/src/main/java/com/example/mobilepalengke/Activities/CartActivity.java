package com.example.mobilepalengke.Activities;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mobilepalengke.Adapters.CartFragmentAdapter;
import com.example.mobilepalengke.Fragments.CartFragment;
import com.example.mobilepalengke.Fragments.CheckOutFragment;
import com.example.mobilepalengke.R;
import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

public class CartActivity extends AppCompatActivity {

    TabLayout tabLayout;
    ViewPager2 viewPager2;

    Context context;

    FragmentActivity fragmentActivity;

    CartFragment cartFragment = new CartFragment();
    CheckOutFragment checkOutFragment = new CheckOutFragment();

    CartFragmentAdapter cartFragmentAdapter;

    int selectedTab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager2 = findViewById(R.id.viewPager2);

        fragmentActivity = CartActivity.this;
        context = fragmentActivity;

        String productId = getIntent().getStringExtra("productId");

        if (fragmentActivity != null) {
            FragmentManager fragmentManager = fragmentActivity.getSupportFragmentManager();

            cartFragmentAdapter = new CartFragmentAdapter(fragmentManager, getLifecycle(),
                    cartFragment, checkOutFragment, productId);
            viewPager2.setAdapter(cartFragmentAdapter);

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
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}