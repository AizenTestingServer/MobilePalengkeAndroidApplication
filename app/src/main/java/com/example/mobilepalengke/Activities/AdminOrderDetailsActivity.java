package com.example.mobilepalengke.Activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.example.mobilepalengke.DataClasses.User;
import com.example.mobilepalengke.DialogClasses.LoadingDialog;
import com.example.mobilepalengke.DialogClasses.MessageDialog;
import com.example.mobilepalengke.DialogClasses.OrderStatusDialog;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AdminOrderDetailsActivity extends AppCompatActivity {

    ImageView imgExpand;
    ConstraintLayout constraintLayout2, constraintLayout4, buttonLayout;
    RecyclerView recyclerView;
    TextView tvFullName, tvMobileNumbers, tvTimestamp, tvTotalPrice, tvPaymentMethod,
            tvStatus, tvLabel, tvAddress, tvTotalQty;
    Button btnUpdateStatus;

    Context context;

    LoadingDialog loadingDialog;
    MessageDialog messageDialog;
    OrderStatusDialog orderStatusDialog;

    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;

    boolean isListening = true;

    Query orderQuery, productsQuery, userQuery, rolesQuery, notificationsQuery, ownerNotificationsQuery;

    Order currentOrder;

    String orderId, ownerId, notificationId;

    String uid;

    User owner;

    OrderProductGridAdapter orderProductGridAdapter;

    List<CheckOutProduct> checkOutProducts = new ArrayList<>();
    List<Product> products = new ArrayList<>();

    boolean isDetailsShowed = true;

    int overallOwnerNotificationCount = 0;

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd-yy h:mm a", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_order_details);

        imgExpand = findViewById(R.id.imgExpand);
        tvFullName = findViewById(R.id.tvFullName);
        tvMobileNumbers = findViewById(R.id.tvMobileNumbers);
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
        btnUpdateStatus = findViewById(R.id.btnUpdateStatus);
        buttonLayout = findViewById(R.id.buttonLayout);

        context = AdminOrderDetailsActivity.this;

        loadingDialog = new LoadingDialog(context);
        messageDialog = new MessageDialog(context);
        orderStatusDialog = new OrderStatusDialog(context);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null)
            uid = firebaseUser.getUid();

        orderId = getIntent().getStringExtra("orderId");
        ownerId = getIntent().getStringExtra("ownerId");
        notificationId = getIntent().getStringExtra("notificationId");

        firebaseDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_RTDB_url));
        orderQuery = firebaseDatabase.getReference("orders").child(orderId);
        productsQuery = firebaseDatabase.getReference("products").orderByChild("name");
        userQuery = firebaseDatabase.getReference("users").child(ownerId);
        rolesQuery = firebaseDatabase.getReference();
        notificationsQuery = firebaseDatabase.getReference("notifications").child(uid);
        ownerNotificationsQuery = firebaseDatabase.getReference("notifications").child(ownerId);

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

        btnUpdateStatus.setOnClickListener(view -> {
            int status = 0;

            if (currentOrder.getStatus().equals(context.getString(R.string.shipping)))
                status = 1;
            else if (currentOrder.getStatus().equals(context.getString(R.string.delivered)))
                status = 2;

            orderStatusDialog.showDialog();
            orderStatusDialog.setSelectedStatus(status);
        });

        orderStatusDialog.setDialogListener(selectedStatus -> {
            String status = getString(R.string.processing);

            if (selectedStatus == 1)
                status = getString(R.string.shipping);
            else if (selectedStatus == 2)
                status =getString(R.string.delivered);

            currentOrder.setStatus(status);

            String[] notifData = new String[2];

            switch (status) {
                case "Processing":
                    notifData[0] = "Order Processing";
                    notifData[1]  = "Your order is still processing.";
                    break;
                case "Shipping":
                    notifData[0] = "Order Shipping";
                    notifData[1]  = "Your order is now shipping.";
                    break;
                case "Delivered":
                    notifData[0] = "Order Delivered";
                    notifData[1]  = "Your order has been delivered.";
                    break;
                default:
                    notifData[0] = "Order Cancelled";
                    notifData[1]  = "Your order has been cancelled.";
                    break;
            }

            orderQuery.getRef().child("status").setValue(status).
                    addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(
                                    context,
                                    "You have successfully updated the order status.",
                                    Toast.LENGTH_SHORT
                            ).show();

                            sendNotification(notifData);
                        } else {
                            String error = "";
                            if (task.getException() != null)
                                error = task.getException().toString();

                            messageDialog.setTextCaption(error);
                            messageDialog.setTextType(2);
                            messageDialog.showDialog();
                        }

                        loadingDialog.dismissDialog();
                        orderStatusDialog.dismissDialog();
                    });
        });
    }

    private void sendNotification(String[] notifData) {
        loadingDialog.showDialog();

        String notificationId = "notif" + ((String.valueOf(overallOwnerNotificationCount + 1).length() < 2) ?
                "0" + (overallOwnerNotificationCount + 1) : (int) (overallOwnerNotificationCount + 1));

        String notifDescription = notifData[0],
                notifTitle = "Mobile Palengke Order",
                notifValue = notifData[1],
                activityText = "OrderDetailsActivity";
        Map<String, String> mapAttributes = new HashMap<>();
        mapAttributes.put("orderId", orderId);
        mapAttributes.put("notificationId", notificationId);

        String curDateAndTime = simpleDateFormat.format(new Date());

        NotificationItem notification = new NotificationItem(
                notificationId, notifDescription, notifTitle, notifValue, curDateAndTime, activityText,
                1, 1, 1, false, false, mapAttributes
        );

        ownerNotificationsQuery.getRef().child(notificationId).setValue(notification);
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
                            if (product != null) {
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

                    buttonLayout.setVisibility(View.VISIBLE);
                    switch (currentOrder.getStatus()) {
                        case "Processing":
                            tvStatus.setTextColor(context.getColor(R.color.mp_blue));
                            tvStatus.setBackground(AppCompatResources.getDrawable(context, R.drawable.bg_yellow_corner));
                            break;
                        case "Shipping":
                            tvStatus.setTextColor(context.getColor(R.color.white));
                            tvStatus.setBackground(AppCompatResources.getDrawable(context, R.drawable.bg_green_corner));
                            break;
                        case "Delivered":
                            tvStatus.setTextColor(context.getColor(R.color.mp_yellow));
                            tvStatus.setBackground(AppCompatResources.getDrawable(context, R.drawable.bg_blue_corner));
                            buttonLayout.setVisibility(View.GONE);
                            break;
                        case "Cancelled":
                            tvStatus.setTextColor(context.getColor(R.color.white));
                            tvStatus.setBackground(AppCompatResources.getDrawable(context, R.drawable.bg_red_corner));
                            buttonLayout.setVisibility(View.GONE);
                            break;
                    }

                    userQuery.addValueEventListener(getUsersValueListener());
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

    private ValueEventListener getUsersValueListener() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {

                    if (snapshot.exists()) {
                        User user = snapshot.getValue(User.class);
                        if (user != null)
                            owner = user;
                    }

                    String fullName = owner.getFirstName() + " " + owner.getLastName();

                    tvFullName.setText(fullName);

                    List<String> mobileNumbers = currentOrder.getMobileNumbers() != null ?
                            new ArrayList<>(currentOrder.getMobileNumbers().values()) : new ArrayList<>();

                    tvMobileNumbers.setText(TextUtils.join(", ", mobileNumbers));

                    ownerNotificationsQuery.addValueEventListener(getOwnerNotificationValueListener());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "userQuery:onCancelled", error.toException());
                loadingDialog.dismissDialog();

                messageDialog.setTextCaption("Failed to get the users.");
                messageDialog.setTextType(2);
                messageDialog.showDialog();
            }
        };
    }

    private ValueEventListener getOwnerNotificationValueListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    overallOwnerNotificationCount = 0;

                    if (snapshot.exists())
                        overallOwnerNotificationCount = (int) snapshot.getChildrenCount();

                    loadingDialog.dismissDialog();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "endPointNotificationsQuery:onCancelled", error.toException());
                loadingDialog.dismissDialog();

                messageDialog.setTextCaption("Failed to get the end point notifications.");
                messageDialog.setTextType(2);
                messageDialog.showDialog();
            }
        };
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