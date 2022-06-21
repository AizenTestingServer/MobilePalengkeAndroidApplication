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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mobilepalengke.Adapters.CheckOutProductAdapter;
import com.example.mobilepalengke.Adapters.DeliveryAddressAdapter;
import com.example.mobilepalengke.DataClasses.Address;
import com.example.mobilepalengke.DataClasses.AppInfo;
import com.example.mobilepalengke.DataClasses.CartProduct;
import com.example.mobilepalengke.DataClasses.CheckOutProduct;
import com.example.mobilepalengke.DataClasses.NotificationItem;
import com.example.mobilepalengke.DataClasses.Order;
import com.example.mobilepalengke.DataClasses.Product;
import com.example.mobilepalengke.DataClasses.User;
import com.example.mobilepalengke.DialogClasses.AddressDialog;
import com.example.mobilepalengke.DialogClasses.ChangeEmailAddressDialog;
import com.example.mobilepalengke.DialogClasses.DownloadDialog;
import com.example.mobilepalengke.DialogClasses.LoadingDialog;
import com.example.mobilepalengke.DialogClasses.MessageDialog;
import com.example.mobilepalengke.DialogClasses.OrderDetailsDialog;
import com.example.mobilepalengke.DialogClasses.StatusDialog;
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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CheckOutActivity extends AppCompatActivity {

    ConstraintLayout captionLayout, firstConstraint, secondConstraint, thirdConstraint, fourthConstraint,
            footerLayout, footer1Layout;
    RecyclerView recyclerView, recyclerView2;
    TextView tvCaptionHeader, tvStep, tvErrorCaption, tvTotalPrice, tvTotalQty,
            tvAddressCaption, tvEmail2;
    Button btnNext, btnBack, btnNext1, btnAddAddress, btnChangeEmail, btnSaveMobileNumber;
    EditText etMobileNumber, etMobileNumber2;
    RadioGroup radioGroup;

    Context context;

    LoadingDialog loadingDialog;
    MessageDialog messageDialog;
    AddressDialog addressDialog;
    ChangeEmailAddressDialog changeEmailAddressDialog;
    OrderDetailsDialog orderDetailsDialog;
    DownloadDialog downloadDialog;
    StatusDialog statusDialog;

    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;

    boolean isListening = true;

    Query productsQuery, addressQuery, userQuery, ordersQuery, cartProductsQuery, notificationsQuery, appInfoQuery;

    ArrayList<String> productIdList;
    ArrayList<Integer> quantityList;
    ArrayList<Double> totalPriceList;

    List<CheckOutProduct> checkOutProducts = new ArrayList<>();
    List<Product> products = new ArrayList<>();
    List<CartProduct> cartProducts = new ArrayList<>();

    CheckOutProductAdapter checkOutProductAdapter;

    List<Address> addressList = new ArrayList<>();

    DeliveryAddressAdapter deliveryAddressAdapter;

    int overallAddressCount = 0, overallOrderCount = 0, overallNotificationCount = 0;

    Address deliveryAddress;

    User currentUser;

    String mobileNumber, mobileNumber2;

    int selectedPaymentMethod = 0;

    String uid;
    int currentStep = 1, maxStep = 4;

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd-yy h:mm a", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_out);

        captionLayout = findViewById(R.id.captionLayout);
        tvCaptionHeader = findViewById(R.id.tvCaptionHeader);
        tvStep = findViewById(R.id.tvStep);
        tvErrorCaption = findViewById(R.id.tvErrorCaption);

        footerLayout = findViewById(R.id.footerLayout);
        footer1Layout = findViewById(R.id.footer1Layout);

        firstConstraint = findViewById(R.id.firstConstraint);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        tvTotalQty = findViewById(R.id.tvTotalQty);
        btnNext = findViewById(R.id.btnNext);
        recyclerView = findViewById(R.id.recyclerView);

        secondConstraint = findViewById(R.id.secondConstraint);
        tvAddressCaption = findViewById(R.id.tvAddressCaption);
        recyclerView2 = findViewById(R.id.recyclerView2);
        btnAddAddress = findViewById(R.id.btnAddAddress);

        thirdConstraint = findViewById(R.id.thirdConstraint);
        tvEmail2 = findViewById(R.id.tvEmail2);
        btnChangeEmail = findViewById(R.id.btnChangeEmail);
        etMobileNumber = findViewById(R.id.etMobileNumber);
        etMobileNumber2 = findViewById(R.id.etMobileNumber2);
        btnSaveMobileNumber = findViewById(R.id.btnSaveMobileNumber);

        fourthConstraint = findViewById(R.id.fourthConstraint);
        radioGroup = findViewById(R.id.radioGroup);

        btnBack = findViewById(R.id.btnBack);
        btnNext1 = findViewById(R.id.btnNext1);

        context = CheckOutActivity.this;

        loadingDialog = new LoadingDialog(context);
        messageDialog = new MessageDialog(context);
        addressDialog = new AddressDialog(context);
        changeEmailAddressDialog = new ChangeEmailAddressDialog(context);
        orderDetailsDialog = new OrderDetailsDialog(context);
        downloadDialog = new DownloadDialog(context);
        statusDialog = new StatusDialog(context);

        Intent currentIntent = getIntent();
        productIdList = (ArrayList<String>) currentIntent.getSerializableExtra("productIdList");
        quantityList = (ArrayList<Integer>) currentIntent.getSerializableExtra("quantityList");
        totalPriceList = (ArrayList<Double>) currentIntent.getSerializableExtra("totalPriceList");

        double totalPrice = 0;
        int totalQuantity = 0;

        checkOutProducts.clear();
        for (int i = 0; i < productIdList.size(); i++) {
            checkOutProducts.add(
                    new CheckOutProduct(productIdList.get(i), quantityList.get(i), totalPriceList.get(i))
            );
            totalPrice += totalPriceList.get(i);
            totalQuantity += quantityList.get(i);
        }

        tvTotalPrice.setText(getString(R.string.priceValue, totalPrice));
        tvTotalQty.setText(getString(R.string.totalQuantityValue, totalQuantity, totalQuantity <= 1 ? "" : "s"));

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null)
            uid = firebaseUser.getUid();

        firebaseDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_RTDB_url));
        productsQuery = firebaseDatabase.getReference("products").orderByChild("name");
        addressQuery = firebaseDatabase.getReference("addressList").orderByChild("id");
        userQuery = firebaseDatabase.getReference("users").child(uid);
        ordersQuery = firebaseDatabase.getReference("orders");
        cartProductsQuery = firebaseDatabase.getReference("cartList").child(uid).child("cartProducts");
        notificationsQuery = firebaseDatabase.getReference("notifications").child(uid);
        appInfoQuery = firebaseDatabase.getReference("appInfo");

        loadingDialog.showDialog();
        isListening = true;
        productsQuery.addValueEventListener(getProductsValueListener());
        appInfoQuery.addValueEventListener(getAppInfoValueListener());

        firstConstraint.setVisibility(View.VISIBLE);
        footer1Layout.setVisibility(View.GONE);
        secondConstraint.setVisibility(View.GONE);
        thirdConstraint.setVisibility(View.GONE);
        fourthConstraint.setVisibility(View.GONE);

        GridLayoutManager gridLayoutManager =
                new GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(gridLayoutManager);
        checkOutProductAdapter = new CheckOutProductAdapter(context, checkOutProducts, products);
        recyclerView.setAdapter(checkOutProductAdapter);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        deliveryAddressAdapter = new DeliveryAddressAdapter(context, addressList);
        deliveryAddressAdapter.setAddressAdapterListener(address -> {
            deliveryAddress = address;
            tvErrorCaption.setVisibility(View.GONE);
        });
        recyclerView2.setLayoutManager(linearLayoutManager);
        recyclerView2.setAdapter(deliveryAddressAdapter);

        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            int paymentMethod = i;
            radioGroup.getChildAt(i).setOnClickListener(view -> selectedPaymentMethod = paymentMethod);
        }

        updateStep();

        btnNext.setOnClickListener(view -> {
            currentStep++;
            firstConstraint.setVisibility(View.GONE);
            footerLayout.setVisibility(View.GONE);

            secondConstraint.setVisibility(View.VISIBLE);
            footer1Layout.setVisibility(View.VISIBLE);

            tvCaptionHeader.setText(getString(R.string.deliveryAddress));
            updateStep();
        });

        btnBack.setOnClickListener(view -> {
            currentStep--;
            backLayout();
            updateStep();
        });

        btnNext1.setOnClickListener(view -> {
            if (currentStep < 4) {
                switch (currentStep) {
                    case 2:
                        if (deliveryAddress == null) {
                            tvErrorCaption.setText(getString(R.string.selectDeliveryAddress));
                            tvErrorCaption.setVisibility(View.VISIBLE);
                            return;
                        }
                        break;
                    case 3:
                        if (!isValidMobileNumber()) return;
                        break;
                    default:
                        break;
                }

                if (currentStep < maxStep) currentStep++;

                tvErrorCaption.setVisibility(View.GONE);

                switch (currentStep) {
                    case 3:
                        List<String> mobileNumbers = currentUser.getMobileNumbers() != null ?
                                new ArrayList<>(currentUser.getMobileNumbers().values()) :
                                new ArrayList<>();

                        etMobileNumber.getText().clear();
                        etMobileNumber2.getText().clear();

                        etMobileNumber.setBackgroundResource(R.drawable.et_bg_default);
                        etMobileNumber.setCompoundDrawablesWithIntrinsicBounds(
                                ContextCompat.getDrawable(context, R.drawable.ic_phone_focused),
                                null, null, null);

                        etMobileNumber2.setBackgroundResource(R.drawable.et_bg_default);
                        etMobileNumber2.setCompoundDrawablesWithIntrinsicBounds(
                                ContextCompat.getDrawable(context, R.drawable.ic_phone_focused),
                                null, null, null);

                        if (mobileNumbers.size() >= 1) etMobileNumber.setText(mobileNumbers.get(0));
                        if (mobileNumbers.size() >= 2) etMobileNumber2.setText(mobileNumbers.get(1));

                        secondConstraint.setVisibility(View.GONE);
                        thirdConstraint.setVisibility(View.VISIBLE);

                        tvCaptionHeader.setText(getString(R.string.contacts));
                        break;
                    case 4:
                        thirdConstraint.setVisibility(View.GONE);
                        fourthConstraint.setVisibility(View.VISIBLE);

                        tvCaptionHeader.setText(getString(R.string.paymentMethod));
                        break;
                    default:
                        break;
                }
            } else
                orderDetailsDialog.showDialog(checkOutProducts, products, deliveryAddress,
                        firebaseUser.getEmail(), mobileNumber, mobileNumber2, selectedPaymentMethod);
        });

        int finalTotalQuantity = totalQuantity;
        double finalTotalPrice = totalPrice;
        orderDetailsDialog.setDialogListener(() -> {
            loadingDialog.showDialog();

            String orderId;
            StringBuilder idBuilder = new StringBuilder("order");

            for (int i = 0; i < 7 - String.valueOf(overallOrderCount + 1).length(); i++)
                idBuilder.append("0");
            idBuilder.append(overallOrderCount + 1);

            orderId = String.valueOf(idBuilder);

            String paymentMethod;

            switch (selectedPaymentMethod) {
                case 1:
                    paymentMethod = "G-Cash";
                    break;
                case 2:
                    paymentMethod = "Paypal";
                    break;
                default:
                    paymentMethod = "Cash on Delivery";
                    break;
            }

            String curDateAndTime = simpleDateFormat.format(new Date());

            Map<String, String> mapAddress = new HashMap<>();
            mapAddress.put("name", deliveryAddress.getName());
            mapAddress.put("value", deliveryAddress.getValue());

            Map<String, CheckOutProduct> mapProducts = new HashMap<>();
            for (CheckOutProduct checkOutProduct : checkOutProducts)
                mapProducts.put(checkOutProduct.getId(), checkOutProduct);

            Map<String, String> mobileNumbers = new HashMap<>();
            mobileNumbers.put("mobileNo01", mobileNumber);
            mobileNumbers.put("mobileNo02", mobileNumber2);

            Order order = new Order(orderId, uid, paymentMethod, "Processing", curDateAndTime,
                    mapAddress, mobileNumbers, mapProducts);

            String notificationId;
            idBuilder = new StringBuilder("notif");

            for (int i = 0; i < 7 - String.valueOf(overallNotificationCount + 1).length(); i++)
                idBuilder.append("0");
            idBuilder.append(overallNotificationCount + 1);

            notificationId = String.valueOf(idBuilder);

            String notifDescription = "Order Placed",
                    notifTitle = "Mobile Palengke Order",
                    notifValue = "You ordered " + finalTotalQuantity + " product" +
                            (finalTotalQuantity > 1 ? "s" : "") + " with a cost of " +
                    getString(R.string.priceValue, finalTotalPrice),
                    activityText = "OrderDetailsActivity";
            Map<String, String> mapAttributes = new HashMap<>();
            mapAttributes.put("orderId", orderId);
            mapAttributes.put("notificationId", notificationId);

            NotificationItem notification = new NotificationItem(
                    notificationId, notifDescription, notifTitle, notifValue, curDateAndTime, activityText,
                    1, 1, 1, false, false, mapAttributes
            );

            isListening = false;
            ordersQuery.getRef().child(orderId).setValue(order).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(
                            context,
                            "You have successfully ordered the products.",
                            Toast.LENGTH_SHORT
                    ).show();

                    orderDetailsDialog.dismissDialog();

                    for (CheckOutProduct checkOutProduct : checkOutProducts) {
                        int remQty;
                        for (CartProduct cartProduct : cartProducts) {
                            if (cartProduct.getId().equals(checkOutProduct.getId())) {
                                remQty = cartProduct.getQuantity() - checkOutProduct.getQuantity();
                                if (remQty <= 0)
                                    cartProductsQuery.getRef().child(checkOutProduct.getId()).removeValue();
                                else
                                    cartProductsQuery.getRef().child(checkOutProduct.getId()).
                                            child("quantity").setValue(remQty);
                                break;
                            }
                        }
                    }

                    notificationsQuery.getRef().child(notificationId).setValue(notification);

                    showNotification(notification, 0);

                    Intent intent = new Intent(context, MainActivity.class);
                    startActivity(intent);
                    finishAffinity();

                    intent = new Intent(context, OrderDetailsActivity.class);
                    intent.putExtra("orderId", orderId);
                    intent.putExtra("notificationId", notificationId);
                    startActivity(intent);
                } else {
                    String error = "";
                    if (task.getException() != null)
                        error = task.getException().toString();

                    messageDialog.setTextCaption(error);
                    messageDialog.setTextType(2);
                    messageDialog.showDialog();
                }

                loadingDialog.dismissDialog();

                isListening = true;
            });
        });

        btnAddAddress.setOnClickListener(view -> addressDialog.showDialog());

        addressDialog.setDialogListener((address) -> {
            loadingDialog.showDialog();

            String addressId = address.getId();
            boolean isAddMode = false;

            if (addressId == null) {
                StringBuilder idBuilder = new StringBuilder("add");

                for (int i = 0; i < 7 - String.valueOf(overallAddressCount + 1).length(); i++)
                    idBuilder.append("0");
                idBuilder.append(overallAddressCount + 1);

                addressId = String.valueOf(idBuilder);

                isAddMode = true;
            }

            String toastMessage = "Successfully " + (isAddMode ? "added" : "updated") + " your address.";

            Address newAddress = new Address(addressId, uid, address.getName(), address.getValue());

            addressQuery.getRef().child(addressId).setValue(newAddress).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(
                            context,
                            toastMessage,
                            Toast.LENGTH_SHORT
                    ).show();

                    addressDialog.dismissDialog();
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

        btnChangeEmail.setOnClickListener(view -> changeEmailAddressDialog.showDialog());

        changeEmailAddressDialog.setDialogListener(emailAddress -> {
            loadingDialog.showDialog();

            firebaseUser.updateEmail(emailAddress).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(
                            context,
                            "Successfully changed your email address",
                            Toast.LENGTH_SHORT).show();

                    firebaseUser.reload();
                    tvEmail2.setText(firebaseUser.getEmail());

                    changeEmailAddressDialog.dismissDialog();
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

        etMobileNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                mobileNumber = editable.toString().trim();

                if (mobileNumber.length() == 11) {
                    etMobileNumber.setBackgroundResource(R.drawable.et_bg_default);
                    etMobileNumber.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(context, R.drawable.ic_phone_focused),
                            null, null, null);

                    tvErrorCaption.setVisibility(View.GONE);
                } else {
                    if (mobileNumber.length() == 0 && mobileNumber2 != null && mobileNumber2.length() != 0) {
                        etMobileNumber.setText(mobileNumber2);
                        etMobileNumber2.getText().clear();
                        return;
                    }

                    etMobileNumber.setBackgroundResource(R.drawable.et_bg_error);
                    etMobileNumber.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(context, R.drawable.ic_phone_red),
                            null, null, null);

                    if (mobileNumber.length() == 0)
                        tvErrorCaption.setText(context.getString(R.string.enterMobileNumber));
                    else
                        tvErrorCaption.setText(context.getString(R.string.invalidMobileNumber));

                    tvErrorCaption.setVisibility(View.VISIBLE);
                }
            }
        });

        etMobileNumber2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                mobileNumber2 = editable.toString().trim();

                if (mobileNumber2.length() == 11 || mobileNumber2.length() == 0) {
                    etMobileNumber2.setBackgroundResource(R.drawable.et_bg_default);
                    etMobileNumber2.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(context, R.drawable.ic_phone_focused),
                            null, null, null);

                    tvErrorCaption.setVisibility(View.GONE);
                } else {
                    etMobileNumber2.setBackgroundResource(R.drawable.et_bg_error);
                    etMobileNumber2.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(context, R.drawable.ic_phone_red),
                            null, null, null);

                    tvErrorCaption.setText(context.getString(R.string.invalidMobileNumber));
                    tvErrorCaption.setVisibility(View.VISIBLE);
                }
            }
        });

        btnSaveMobileNumber.setOnClickListener(view -> {
            if (isValidMobileNumber()) {
                Map<String, String> mobileNumbers = new HashMap<>();
                mobileNumbers.put("mobileNo01", mobileNumber);
                mobileNumbers.put("mobileNo02", mobileNumber2);

                userQuery.getRef().child("mobileNumbers").setValue(mobileNumbers).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(
                                context,
                                "Successfully saved the mobile number.",
                                Toast.LENGTH_SHORT
                        ).show();

                        addressDialog.dismissDialog();
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
        });
    }

    private ValueEventListener getProductsValueListener() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    products.clear();

                    if (snapshot.exists())
                        for (CheckOutProduct checkOutProduct : checkOutProducts) {
                            Product product = snapshot.child(checkOutProduct.getId()).getValue(Product.class);
                            if (product != null && !product.isDeactivated()) products.add(product);
                        }

                    checkOutProductAdapter.notifyDataSetChanged();

                    addressQuery.addValueEventListener(getAddressValueListener());
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

    private ValueEventListener getAddressValueListener() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    overallAddressCount = 0;
                    addressList.clear();

                    if (snapshot.exists()) {
                        overallAddressCount = (int) snapshot.getChildrenCount();

                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Address address = dataSnapshot.getValue(Address.class);
                            if (address != null && address.getOwnerId().equals(uid))
                                addressList.add(address);
                        }
                    }

                    if (addressList.size() == 0)
                        tvAddressCaption.setVisibility(View.VISIBLE);
                    else tvAddressCaption.setVisibility(View.GONE);
                    tvAddressCaption.bringToFront();

                    deliveryAddressAdapter.notifyDataSetChanged();

                    userQuery.addValueEventListener(getUserValueListener());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "addressQuery:onCancelled", error.toException());
                loadingDialog.dismissDialog();

                messageDialog.setTextCaption("Failed to get the address list.");
                messageDialog.setTextType(2);
                messageDialog.showDialog();
            }
        };
    }

    private ValueEventListener getUserValueListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    if (snapshot.exists())
                        currentUser = snapshot.getValue(User.class);

                    tvEmail2.setText(firebaseUser.getEmail());

                    ordersQuery.addValueEventListener(getOrdersValueListener());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "userQuery:onCancelled", error.toException());
                loadingDialog.dismissDialog();

                messageDialog.setTextCaption("Failed to get the user's data.");
                messageDialog.setTextType(2);
                messageDialog.showDialog();
            }
        };
    }

    private ValueEventListener getOrdersValueListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    overallOrderCount = 0;

                    if (snapshot.exists())
                        overallOrderCount = (int) snapshot.getChildrenCount();

                    cartProductsQuery.addValueEventListener(getCartValueListener());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "ordersQuery:onCancelled", error.toException());
                loadingDialog.dismissDialog();

                messageDialog.setTextCaption("Failed to get the orders.");
                messageDialog.setTextType(2);
                messageDialog.showDialog();
            }
        };
    }

    private ValueEventListener getCartValueListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    cartProducts.clear();

                    if (snapshot.exists())
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            CartProduct cartProduct = dataSnapshot.getValue(CartProduct.class);
                            if (cartProduct != null) cartProducts.add(cartProduct);
                        }

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

    private boolean isValidMobileNumber() {
        boolean isMobileNumberEmpty = mobileNumber == null || mobileNumber.length() == 0;
        boolean isInvalidMobileNumber = mobileNumber != null && mobileNumber.length() != 11;
        boolean isInvalidMobileNumber2 = mobileNumber2 != null &&
                mobileNumber2.length() != 0 && mobileNumber2.length() != 11;


        if (isMobileNumberEmpty || isInvalidMobileNumber || isInvalidMobileNumber2) {
            if (isMobileNumberEmpty) {
                etMobileNumber.setBackgroundResource(R.drawable.et_bg_error);
                etMobileNumber.setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat.getDrawable(context, R.drawable.ic_phone_red),
                        null, null, null);

                tvErrorCaption.setText(getString(R.string.enterMobileNumber));
            } else if (isInvalidMobileNumber) {
                etMobileNumber.setBackgroundResource(R.drawable.et_bg_error);
                etMobileNumber.setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat.getDrawable(context, R.drawable.ic_phone_red),
                        null, null, null);

                tvErrorCaption.setText(getString(R.string.invalidMobileNumber));
            } else {
                etMobileNumber2.setBackgroundResource(R.drawable.et_bg_error);
                etMobileNumber2.setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat.getDrawable(context, R.drawable.ic_phone_red),
                        null, null, null);

                tvErrorCaption.setText(getString(R.string.invalidMobileNumber));
            }

            tvErrorCaption.setVisibility(View.VISIBLE);

            return false;
        } else {
            etMobileNumber.setBackgroundResource(R.drawable.et_bg_default);
            etMobileNumber.setCompoundDrawablesWithIntrinsicBounds(
                    ContextCompat.getDrawable(context, R.drawable.ic_phone_focused),
                    null, null, null);

            etMobileNumber2.setBackgroundResource(R.drawable.et_bg_default);
            etMobileNumber2.setCompoundDrawablesWithIntrinsicBounds(
                    ContextCompat.getDrawable(context, R.drawable.ic_phone_focused),
                    null, null, null);
        }

        return true;
    }

    private void backLayout() {
        tvErrorCaption.setVisibility(View.GONE);

        switch (currentStep) {
            case 1:
                firstConstraint.setVisibility(View.VISIBLE);
                footerLayout.setVisibility(View.VISIBLE);

                secondConstraint.setVisibility(View.GONE);
                footer1Layout.setVisibility(View.GONE);

                tvCaptionHeader.setText(getString(R.string.orderSummary));
                break;
            case 2:
                secondConstraint.setVisibility(View.VISIBLE);
                thirdConstraint.setVisibility(View.GONE);

                tvCaptionHeader.setText(getString(R.string.deliveryAddress));
                break;
            case 3:
                thirdConstraint.setVisibility(View.VISIBLE);
                fourthConstraint.setVisibility(View.GONE);

                tvCaptionHeader.setText(getString(R.string.contacts));
                break;
            default:
                break;
        }
    }

    private void updateStep() {
        tvStep.setText(getString(R.string.stepValue, currentStep, maxStep));
    }

    @Override
    public void onBackPressed() {
        if (currentStep > 1)
            currentStep--;
        else {
            super.onBackPressed();
            return;
        }

        backLayout();
        updateStep();
    }

    @Override
    public void onResume() {
        isListening = true;
        productsQuery.addListenerForSingleValueEvent(getProductsValueListener());
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