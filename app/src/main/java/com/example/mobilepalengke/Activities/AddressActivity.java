package com.example.mobilepalengke.Activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mobilepalengke.Adapters.AddressAdapter;
import com.example.mobilepalengke.DataClasses.Address;
import com.example.mobilepalengke.DialogClasses.AddressDialog;
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

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AddressActivity extends AppCompatActivity {

    TextView tvAddressCaption;
    RecyclerView recyclerView;
    Button btnAddAddress;

    Context context;

    LoadingDialog loadingDialog;
    MessageDialog messageDialog;
    AddressDialog addressDialog;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;

    boolean isListening = true;

    Query addressQuery;

    List<Address> addressList = new ArrayList<>();

    AddressAdapter addressAdapter;

    String uid;
    int overallAddressCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);

        tvAddressCaption = findViewById(R.id.tvAddressCaption);
        recyclerView = findViewById(R.id.recyclerView);
        btnAddAddress = findViewById(R.id.btnAddAddress);

        context = AddressActivity.this;

        loadingDialog = new LoadingDialog(context);
        messageDialog = new MessageDialog(context);
        addressDialog = new AddressDialog(context);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null)
            uid = firebaseUser.getUid();

        firebaseDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_RTDB_url));
        addressQuery = firebaseDatabase.getReference("addressList").orderByChild("id");

        loadingDialog.showDialog();
        isListening = true;
        addressQuery.addValueEventListener(getAddressValueListener());

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        addressAdapter = new AddressAdapter(context, addressList);
        addressAdapter.setAddressAdapterListener(address -> {
            addressDialog.showDialog();
            addressDialog.setData(address);
        });
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(addressAdapter);

        btnAddAddress.setOnClickListener(view -> addressDialog.showDialog());

        addressDialog.setDialogListener((address) -> {
            loadingDialog.showDialog();

            String addressId = address.getId();
            boolean isAddMode = false;

            if (addressId == null) {
                addressId = "add"
                        + ((String.valueOf(overallAddressCount + 1).length() < 2) ?
                        "0" + (overallAddressCount + 1) :
                        (int) (overallAddressCount + 1));
                isAddMode = true;
            }

            String toastMessage = "Successfully " + (isAddMode ? "added" : "updated") + " your address.";

            Address newAddress = new Address(addressId, uid, address.getName(), address.getValue());

            firebaseDatabase.getReference("addressList").child(addressId)
                    .setValue(newAddress).addOnCompleteListener(task -> {
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
    }

    private ValueEventListener getAddressValueListener() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
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

                    addressAdapter.notifyDataSetChanged();
                }

                loadingDialog.dismissDialog();
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
}