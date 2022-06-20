package com.example.mobilepalengke.Activities;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mobilepalengke.Adapters.OrderProductGridAdapter;
import com.example.mobilepalengke.DataClasses.Address;
import com.example.mobilepalengke.DataClasses.CheckOutProduct;
import com.example.mobilepalengke.DataClasses.NotificationItem;
import com.example.mobilepalengke.DataClasses.Order;
import com.example.mobilepalengke.DataClasses.Product;
import com.example.mobilepalengke.DialogClasses.ConfirmationDialog;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class OrderDetailsActivity extends AppCompatActivity {

    ImageView imgExpand;
    ConstraintLayout constraintLayout2, constraintLayout4, buttonLayout;
    RecyclerView recyclerView;
    TextView tvTimestamp, tvTotalPrice, tvPaymentMethod, tvStatus, tvLabel, tvAddress, tvTotalQty;
    Button btnCancelOrder;

    Context context;

    LoadingDialog loadingDialog;
    MessageDialog messageDialog;
    ConfirmationDialog confirmationDialog;

    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;

    boolean isListening = true;

    Query orderQuery, productsQuery, notificationsQuery;

    Order currentOrder;

    String orderId, notificationId;

    String uid;

    OrderProductGridAdapter orderProductGridAdapter;

    List<CheckOutProduct> checkOutProducts = new ArrayList<>();
    List<Product> products = new ArrayList<>();

    boolean isDetailsShowed = true;

    int overallNotificationCount = 0;

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd-yy h:mm a", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        imgExpand = findViewById(R.id.imgExpand);
        recyclerView = findViewById(R.id.recyclerView);
        constraintLayout2 = findViewById(R.id.constraintLayout2);
        constraintLayout4 = findViewById(R.id.constraintLayout4);
        tvTimestamp = findViewById(R.id.tvTimestamp);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
        tvStatus = findViewById(R.id.tvStatus);
        tvLabel = findViewById(R.id.tvLabel);
        tvAddress = findViewById(R.id.tvAddress);
        tvTotalQty = findViewById(R.id.tvTotalQty);
        buttonLayout = findViewById(R.id.buttonLayout);
        btnCancelOrder = findViewById(R.id.btnCancelOrder);

        context = OrderDetailsActivity.this;

        loadingDialog = new LoadingDialog(context);
        messageDialog = new MessageDialog(context);
        confirmationDialog = new ConfirmationDialog(context);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null)
            uid = firebaseUser.getUid();

        orderId = getIntent().getStringExtra("orderId");
        notificationId = getIntent().getStringExtra("notificationId");

        firebaseDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_RTDB_url));
        orderQuery = firebaseDatabase.getReference("orders").child(orderId);
        productsQuery = firebaseDatabase.getReference("products").orderByChild("name");
        notificationsQuery = firebaseDatabase.getReference("notifications").child(uid);

        if (notificationId != null) {
            isListening = false;
            notificationsQuery.getRef().child(notificationId).child("read").setValue(true);
            isListening = true;
        }

        loadingDialog.showDialog();
        isListening = true;
        orderQuery.addValueEventListener(getOrderValueListener());

        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false);
        orderProductGridAdapter = new OrderProductGridAdapter(context, checkOutProducts, products);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(orderProductGridAdapter);

        imgExpand.setOnClickListener(view -> {
            if (isDetailsShowed) {
                constraintLayout2.setVisibility(View.GONE);
                constraintLayout4.setVisibility(View.GONE);
                tvStatus.setVisibility(View.GONE);
                imgExpand.setImageResource(R.drawable.ic_baseline_expand_more_24);
            } else {
                constraintLayout2.setVisibility(View.VISIBLE);
                constraintLayout4.setVisibility(View.VISIBLE);
                tvStatus.setVisibility(View.VISIBLE);
                imgExpand.setImageResource(R.drawable.ic_baseline_expand_less_24);
            }
            isDetailsShowed = !isDetailsShowed;
        });

        btnCancelOrder.setOnClickListener(view -> {
            confirmationDialog.setTextCaption("Do you want to cancel your order?");
            confirmationDialog.showDialog();
        });

        confirmationDialog.setDialogListener(() -> {
            loadingDialog.showDialog();

            String notificationId = "notif" + ((String.valueOf(overallNotificationCount + 1).length() < 2) ?
                    "0" + (overallNotificationCount + 1) : (int) (overallNotificationCount + 1));

            String notifDescription = "Order Cancelled",
                    notifTitle = "Mobile Palengke Order",
                    notifValue = "You have cancelled your order.",
                    activityText = "OrderDetailsActivity";
            Map<String, String> mapAttributes = new HashMap<>();
            mapAttributes.put("orderId", orderId);
            mapAttributes.put("notificationId", notificationId);

            String curDateAndTime = simpleDateFormat.format(new Date());

            NotificationItem notification = new NotificationItem(
                    notificationId, notifDescription, notifTitle, notifValue, curDateAndTime, activityText,
                    1, 1, 1, false, false, mapAttributes
            );

            orderQuery.getRef().child("status").setValue("Cancelled").
                    addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(
                            context,
                            "You have successfully cancelled your order.",
                            Toast.LENGTH_SHORT
                    ).show();

                    notificationsQuery.getRef().child(notificationId).setValue(notification);

                    showNotification(notification, 0);
                } else {
                    String error = "";
                    if (task.getException() != null)
                        error = task.getException().toString();

                    messageDialog.setTextCaption(error);
                    messageDialog.setTextType(2);
                    messageDialog.showDialog();
                }

                loadingDialog.dismissDialog();
                confirmationDialog.dismissDialog();
            });
        });
    }

    private ValueEventListener getOrderValueListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    if (snapshot.exists())
                        currentOrder = snapshot.getValue(Order.class);
                    
                    if (currentOrder != null) {
                        checkOutProducts.clear();
                        if (currentOrder.getProducts() != null)
                            checkOutProducts.addAll(currentOrder.getProducts().values());
                    }

                    productsQuery.addValueEventListener(getProductsValueListener());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "orderQuery:onCancelled", error.toException());
                loadingDialog.dismissDialog();

                messageDialog.setTextCaption("Failed to get the currentOrder.");
                messageDialog.setTextType(2);
                messageDialog.showDialog();
            }
        };
    }

    private ValueEventListener getProductsValueListener() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    products.clear();

                    if (snapshot.exists()) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Product product = dataSnapshot.getValue(Product.class);
                            if (product != null && !product.isDeactivated()) {
                                products.add(product);
                            }
                        }
                    }

                    orderProductGridAdapter.notifyDataSetChanged();

                    double totalPrice = 0;
                    int totalQuantity = 0;
                    for (CheckOutProduct checkOutProduct : checkOutProducts) {
                        totalPrice += checkOutProduct.getTotalPrice();
                        totalQuantity += checkOutProduct.getQuantity();
                    }

                    String status = "Status: " + currentOrder.getStatus();

                    Address address = new Address();

                    if (currentOrder.getAddress() != null)
                        for (Map.Entry<String, String> mapAddress : currentOrder.getAddress().entrySet()) {
                            if (mapAddress.getKey().equals("name")) address.setName(mapAddress.getValue());
                            if (mapAddress.getKey().equals("value")) address.setValue(mapAddress.getValue());
                        }

                    tvTimestamp.setText(currentOrder.getTimestamp());
                    tvTotalPrice.setText(getString(R.string.priceValue, totalPrice));
                    tvPaymentMethod.setText(currentOrder.getPaymentMethod());
                    tvStatus.setText(status);
                    tvLabel.setText(address.getName());
                    tvAddress.setText(address.getValue());
                    tvTotalQty.setText(getString(R.string.totalQuantityValue, totalQuantity, totalQuantity <= 1 ? "" : "s"));

                    buttonLayout.setVisibility(View.GONE);
                    switch (currentOrder.getStatus()) {
                        case "Processing":
                            tvStatus.setTextColor(context.getColor(R.color.mp_blue));
                            tvStatus.setBackground(AppCompatResources.getDrawable(context, R.drawable.bg_yellow_corner));
                            buttonLayout.setVisibility(View.VISIBLE);
                            break;
                        case "Shipping":
                            tvStatus.setTextColor(context.getColor(R.color.white));
                            tvStatus.setBackground(AppCompatResources.getDrawable(context, R.drawable.bg_green_corner));
                            break;
                        case "Delivered":
                            tvStatus.setTextColor(context.getColor(R.color.mp_yellow));
                            tvStatus.setBackground(AppCompatResources.getDrawable(context, R.drawable.bg_blue_corner));
                            break;
                        case "Cancelled":
                            tvStatus.setTextColor(context.getColor(R.color.white));
                            tvStatus.setBackground(AppCompatResources.getDrawable(context, R.drawable.bg_red_corner));
                            break;
                    }

                    notificationsQuery.addValueEventListener(getNotificationValueListener());
                }
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

    private ValueEventListener getNotificationValueListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    overallNotificationCount = 0;

                    if (snapshot.exists())
                        overallNotificationCount = (int) snapshot.getChildrenCount();

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

    @Override
    public void onResume() {
        isListening = true;

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