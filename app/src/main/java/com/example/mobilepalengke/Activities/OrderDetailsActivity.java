package com.example.mobilepalengke.Activities;

import android.content.Context;
import android.os.Bundle;

import com.example.mobilepalengke.DataClasses.Order;
import com.example.mobilepalengke.DialogClasses.LoadingDialog;
import com.example.mobilepalengke.DialogClasses.MessageDialog;
import com.example.mobilepalengke.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import androidx.appcompat.app.AppCompatActivity;

public class OrderDetailsActivity extends AppCompatActivity {

    Context context;

    LoadingDialog loadingDialog;
    MessageDialog messageDialog;

    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;

    boolean isListening = true;

    Query orderQuery, notificationsQuery;

    Order currentOrder;

    String orderId, notificationId;

    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        context = OrderDetailsActivity.this;

        loadingDialog = new LoadingDialog(context);
        messageDialog = new MessageDialog(context);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null)
            uid = firebaseUser.getUid();

        orderId = getIntent().getStringExtra("orderId");
        notificationId = getIntent().getStringExtra("notificationId");

        firebaseDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_RTDB_url));
        orderQuery = firebaseDatabase.getReference("orders").child(orderId);
        notificationsQuery = firebaseDatabase.getReference("notifications").child(uid);

        loadingDialog.showDialog();
        isListening = true;
        loadingDialog.dismissDialog();

        if (notificationId != null) {
            isListening = false;
            notificationsQuery.getRef().child(notificationId).child("read").setValue(true);
            isListening = true;
        }
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