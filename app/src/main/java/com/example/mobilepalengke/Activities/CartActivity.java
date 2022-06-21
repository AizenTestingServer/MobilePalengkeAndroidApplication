package com.example.mobilepalengke.Activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mobilepalengke.Adapters.CartFragmentAdapter;
import com.example.mobilepalengke.DataClasses.AppInfo;
import com.example.mobilepalengke.DialogClasses.DownloadDialog;
import com.example.mobilepalengke.DialogClasses.StatusDialog;
import com.example.mobilepalengke.Fragments.CartFragment;
import com.example.mobilepalengke.Fragments.OrdersFragment;
import com.example.mobilepalengke.R;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

public class CartActivity extends AppCompatActivity {

    TabLayout tabLayout;
    ViewPager2 viewPager2;

    Context context;

    DownloadDialog downloadDialog;
    StatusDialog statusDialog;

    FragmentActivity fragmentActivity;

    CartFragment cartFragment = new CartFragment();
    OrdersFragment ordersFragment = new OrdersFragment();

    CartFragmentAdapter cartFragmentAdapter;

    boolean isListening = true;

    Query appInfoQuery;

    int selectedTab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager2 = findViewById(R.id.viewPager2);

        fragmentActivity = CartActivity.this;

        context = fragmentActivity;

        downloadDialog = new DownloadDialog(context);
        statusDialog = new StatusDialog(context);

        String productId = getIntent().getStringExtra("productId");

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_RTDB_url));

        appInfoQuery = firebaseDatabase.getReference("appInfo");

        appInfoQuery.addValueEventListener(getAppInfoValueListener());

        if (fragmentActivity != null) {
            FragmentManager fragmentManager = fragmentActivity.getSupportFragmentManager();

            Bundle bundle = new Bundle();
            bundle.putString("productId", productId);
            cartFragment.setArguments(bundle);

            cartFragment.setFragmentListener(CartActivity.this::onBackPressed);

            cartFragmentAdapter = new CartFragmentAdapter(fragmentManager, getLifecycle(),
                    cartFragment, ordersFragment, productId);
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
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onResume() {
        isListening = true;
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