package com.example.mobilepalengke.Activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mobilepalengke.Adapters.AddressAdapter;
import com.example.mobilepalengke.DataClasses.Address;
import com.example.mobilepalengke.DataClasses.AppInfo;
import com.example.mobilepalengke.DialogClasses.AddressDialog;
import com.example.mobilepalengke.DialogClasses.DownloadDialog;
import com.example.mobilepalengke.DialogClasses.LoadingDialog;
import com.example.mobilepalengke.DialogClasses.MessageDialog;
import com.example.mobilepalengke.DialogClasses.StatusDialog;
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
    DownloadDialog downloadDialog;
    StatusDialog statusDialog;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;

    boolean isListening = true;

    Query addressQuery, appInfoQuery;

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
        downloadDialog = new DownloadDialog(context);
        statusDialog = new StatusDialog(context);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null)
            uid = firebaseUser.getUid();

        firebaseDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_RTDB_url));
        addressQuery = firebaseDatabase.getReference("addressList").orderByChild("id");
        appInfoQuery = firebaseDatabase.getReference("appInfo");

        loadingDialog.showDialog();
        isListening = true;
        addressQuery.addValueEventListener(getAddressValueListener());
        appInfoQuery.addValueEventListener(getAppInfoValueListener());

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

                    addressAdapter.notifyDataSetChanged();

                    loadingDialog.dismissDialog();
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
    public void onResume() {
        isListening = true;
        addressQuery.addValueEventListener(getAddressValueListener());
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