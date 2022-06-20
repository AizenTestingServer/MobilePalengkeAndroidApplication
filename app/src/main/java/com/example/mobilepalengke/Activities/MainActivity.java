package com.example.mobilepalengke.Activities;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mobilepalengke.DataClasses.NotificationItem;
import com.example.mobilepalengke.DialogClasses.LoadingDialog;
import com.example.mobilepalengke.DialogClasses.MessageDialog;
import com.example.mobilepalengke.Fragments.ChatListFragment;
import com.example.mobilepalengke.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {

    TextView tvAppNav, tvCartCount;
    ImageView cartIconImage;

    BottomNavigationView bottomNavigationView;
    NavHostFragment navHostFragment;
    NavController navController;

    Context context;

    LoadingDialog loadingDialog;
    MessageDialog messageDialog;

    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;

    boolean isListening = true;

    Query cartProductsQuery, notificationsQuery;

    String uid, currentPassword;
    int overallCartCount = 0, overallNotificationCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvAppNav = findViewById(R.id.tvAppNav);
        cartIconImage = findViewById(R.id.cartIconImage);
        tvCartCount = findViewById(R.id.tvCartCount);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainerView);

        context = MainActivity.this;

        loadingDialog = new LoadingDialog(context);
        messageDialog = new MessageDialog(context);

        if (getIntent().getBooleanExtra("isFromRegistration", false)) {
            messageDialog.setTextCaption("Successfully registered an account.");
            messageDialog.setTextType(1);

            /*
             * MessageDialog messageDialog2 = new MessageDialog(context);
             * 
             * if (getIntent().getBooleanExtra("isSuccess", false)) {
             * messageDialog2.
             * setTextCaption("Please check your email for verification link.");
             * messageDialog2.setTextType(1);
             * } else {
             * messageDialog2.setTextCaption("Failed to send the email verification link.\n"
             * +
             * "Please try again later by going to " + getString(R.string.more) + ">" +
             * getString(R.string.moreOption1) + ">" + getString(R.string.resendLink) +".");
             * messageDialog2.setTextType(2);
             * }
             * 
             * messageDialog2.showDialog();
             */

            messageDialog.showDialog();
        }

        getSharedPreference();

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_RTDB_url));

        if (firebaseUser == null || currentPassword == null) {
            firebaseAuth.signOut();

            Intent intent = new Intent(context, WelcomeScreenActivity.class);
            intent.putExtra("isForcedSignOut", true);
            startActivity(intent);
            finishAffinity();
        } else
            uid = firebaseUser.getUid();

        cartProductsQuery = firebaseDatabase.getReference("cartList").child(uid);
        notificationsQuery = firebaseDatabase.getReference("notifications").child(uid);

        loadingDialog.showDialog();
        isListening = true;
        cartProductsQuery.addValueEventListener(getCartValueListener());

        bottomNavigationView.setBackground(null);
        if (navHostFragment != null)
            navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            /*
             * FragmentManager fragmentManager = navHostFragment.getChildFragmentManager();
             * 
             * List<Fragment> fragments = fragmentManager.getFragments();
             * Fragment fragment = fragments.get(0);
             */

            if (bottomNavigationView.getSelectedItemId() == R.id.homeFragment) {
                tvAppNav.setText(getString(R.string.home1));
            } else if (bottomNavigationView.getSelectedItemId() == R.id.categoriesFragment) {
                tvAppNav.setText(getString(R.string.categories));
            } else if (bottomNavigationView.getSelectedItemId() == R.id.chatListFragment) {
                tvAppNav.setText(getString(R.string.chatList));
            } else if (bottomNavigationView.getSelectedItemId() == R.id.notificationFragment) {
                tvAppNav.setText(getString(R.string.notifications));
            } else {
                tvAppNav.setText(getString(R.string.more));
            }
        });

        cartIconImage.setOnClickListener(view -> {
            Intent intent = new Intent(context, CartActivity.class);
            startActivity(intent);
        });
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

                    notificationsQuery.addValueEventListener(getNotificationValueListener());
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

    private ValueEventListener getNotificationValueListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    overallNotificationCount = 0;

                    if (snapshot.exists()) {
                        overallNotificationCount = (int) snapshot.getChildrenCount();

                        int index = 0;
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            NotificationItem notification = dataSnapshot.getValue(NotificationItem.class);
                            if (notification != null && !notification.isNotified())
                                showNotification(notification, index);

                            index++;
                        }
                    }

                    loadingDialog.dismissDialog();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "notificationsQuery:onCancelled", error.toException());
                loadingDialog.dismissDialog();

                messageDialog.setTextCaption("Failed to get the notifications.");
                messageDialog.setTextType(2);
                messageDialog.showDialog();
            }
        };
    }

    private void showNotification(NotificationItem notification, int index) {
        NotificationManager notificationManager = getNotificationManager(notification);
        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_head);

        String category;
        switch (notification.getCategory()) {
            case 2:
                category = NotificationCompat.CATEGORY_MESSAGE;
                break;
            case 3:
                category = NotificationCompat.CATEGORY_STATUS;
                break;
            default:
                category = NotificationCompat.CATEGORY_REMINDER;
                break;
        }

        int visibility;
        switch (notification.getVisibility()) {
            case 2:
                visibility = NotificationCompat.VISIBILITY_PRIVATE;
                break;
            case 3:
                visibility = NotificationCompat.VISIBILITY_SECRET;
                break;
            default:
                visibility = NotificationCompat.VISIBILITY_PUBLIC;
                break;
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, notification.getId())
                        .setSmallIcon(R.drawable.ic_head).setLargeIcon(icon)
                        .setContentTitle(notification.getTitle())
                        .setContentText(notification.getValue())
                        .setCategory(category)
                        .setVisibility(visibility)
                        .setDefaults(NotificationCompat.DEFAULT_ALL);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            builder.setPriority(Notification.PRIORITY_HIGH);

        Intent notificationIntent = null;

        if (notification.getActivity().equals("ChatActivity"))
            notificationIntent = new Intent(context, ChatActivity.class);
        if (notification.getActivity().equals("OrderDetailsActivity"))
            notificationIntent = new Intent(context, OrderDetailsActivity.class);

        if (notificationIntent != null) {
            if (notification.getAttributes() != null)
                for (Map.Entry<String, String> mapAttributes : notification.getAttributes().entrySet())
                    notificationIntent.putExtra(mapAttributes.getKey(), mapAttributes.getValue());

            notificationIntent.putExtra("notificationId", notification.getId());

            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT
            );
            builder.setContentIntent(pendingIntent);
            builder.setFullScreenIntent(pendingIntent, true);
        }
        notificationManager.notify(index, builder.build());

        isListening = false;
        notificationsQuery.getRef().child(notification.getId()).child("notified").setValue(true);
        isListening = true;
    }

    private NotificationManager getNotificationManager(NotificationItem notification) {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        int importance;
        switch (notification.getCategory()) {
            case 2:
                importance = NotificationManager.IMPORTANCE_LOW;
                break;
            case 3:
                importance = NotificationManager.IMPORTANCE_HIGH;
                break;
            default:
                importance = NotificationManager.IMPORTANCE_DEFAULT;
                break;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel =
                    new NotificationChannel(
                            notification.getId(),
                            notification.getDescription(),
                            importance
                    );
            notificationChannel.setDescription(notification.getDescription());
            notificationManager.createNotificationChannel(notificationChannel);
        }

        return notificationManager;
    }

    private void getSharedPreference() {
        SharedPreferences sharedPreferences = getSharedPreferences("signedInData", Context.MODE_PRIVATE);
        currentPassword = sharedPreferences.getString("password", null);
    }

    @Override
    public void onBackPressed() {
        if (bottomNavigationView.getSelectedItemId() == R.id.chatListFragment) {
            FragmentManager fragmentManager = navHostFragment.getChildFragmentManager();

            List<Fragment> fragments = fragmentManager.getFragments();
            Fragment fragment = fragments.get(0);

            try {
                ChatListFragment chatListFragment = (ChatListFragment) fragment;

                if (chatListFragment.getCurrentStep() > 0)
                    chatListFragment.onBackPressed();
                else
                    super.onBackPressed();
            } catch (Exception exception) {
                Log.w("TAG: " + context.getClass(), exception.toString());
            }
        } else super.onBackPressed();
    }

    @Override
    public void onResume() {
        isListening = true;
        cartProductsQuery.addListenerForSingleValueEvent(getCartValueListener());

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