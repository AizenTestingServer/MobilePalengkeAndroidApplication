package com.example.mobilepalengke.Activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.mobilepalengke.Adapters.IconOptionAdapter;
import com.example.mobilepalengke.Adapters.RoleItemAdapter;
import com.example.mobilepalengke.DataClasses.IconOption;
import com.example.mobilepalengke.DataClasses.Role;
import com.example.mobilepalengke.DataClasses.RoleItem;
import com.example.mobilepalengke.DataClasses.Roles;
import com.example.mobilepalengke.DialogClasses.LoadingDialog;
import com.example.mobilepalengke.DialogClasses.MessageDialog;
import com.example.mobilepalengke.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AdminRolesActivity extends AppCompatActivity {

    ConstraintLayout rolesLayout, typesLayout;
    EditText etSearchRole;
    TextView tvSelectedType, btnChangeType, tvRoleCaption;
    Button btnBack;
    RecyclerView recyclerView, recyclerView2;

    Context context;

    LoadingDialog loadingDialog;
    MessageDialog messageDialog;

    FirebaseDatabase firebaseDatabase;

    Query rolesQuery;

    boolean isListening = true;

    int currentStep = 0, maxStep = 1;

    String searchValue;

    List<RoleItem> roles = new ArrayList<>(), rolesCopy = new ArrayList<>();

    RoleItemAdapter roleItemAdapter;

    List<IconOption> types = new ArrayList<>();

    IconOptionAdapter iconOptionAdapter;

    int selectedRoleIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_roles);

        rolesLayout = findViewById(R.id.rolesLayout);
        typesLayout = findViewById(R.id.typesLayout);

        etSearchRole = findViewById(R.id.etSearchRole);

        tvSelectedType = findViewById(R.id.tvSelectedType);

        btnBack = findViewById(R.id.btnBack);
        btnChangeType = findViewById(R.id.btnChangeType);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView2 = findViewById(R.id.recyclerView2);

        tvRoleCaption = findViewById(R.id.tvRoleCaption);

        context = AdminRolesActivity.this;

        loadingDialog = new LoadingDialog(context);
        messageDialog = new MessageDialog(context);

        firebaseDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_RTDB_url));
        rolesQuery = firebaseDatabase.getReference();

        loadingDialog.showDialog();
        isListening = true;
        rolesQuery.addValueEventListener(getRoleValueListener());

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        roleItemAdapter = new RoleItemAdapter(context, roles);
        roleItemAdapter.setRoleAdapterListener(new RoleItemAdapter.RoleAdapterListener() {
            @Override
            public void onClick(RoleItem roleItem) {

            }
        });
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(roleItemAdapter);

        iconOptionAdapter = new IconOptionAdapter(context, types);
        iconOptionAdapter.setIconOptionAdapterListener(new IconOptionAdapter.IconOptionAdapterListener() {
            @Override
            public void onClick(IconOption iconOption) {

            }

            @Override
            public void onClick(IconOption iconOption, int position) {
                currentStep--;

                tvSelectedType.setText(iconOption.getLabelName());

                selectedRoleIndex = position;

                typesLayout.setVisibility(View.GONE);
                rolesLayout.setVisibility(View.VISIBLE);

                filterUserList();
            }
        });
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL,
                false);
        recyclerView2.setLayoutManager(linearLayoutManager2);
        recyclerView2.setAdapter(iconOptionAdapter);

        etSearchRole.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                searchValue = editable.toString();

                filterUserList();
            }
        });

        btnChangeType.setOnClickListener(view1 -> {
            if (currentStep < maxStep)
                currentStep++;

            rolesLayout.setVisibility(View.GONE);
            typesLayout.setVisibility(View.VISIBLE);
        });

        btnBack.setOnClickListener(view1 -> {
            currentStep--;

            typesLayout.setVisibility(View.GONE);
            rolesLayout.setVisibility(View.VISIBLE);
        });
    }

    private ValueEventListener getRoleValueListener() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    roles.clear();
                    types.clear();

                    if (snapshot.exists()) {
                        Roles allRoles = snapshot.getValue(Roles.class);

                        if (allRoles != null && allRoles.getRoles() != null)
                            for (Map.Entry<String, Map<String, Role>> rolesInCategory : allRoles.getRoles().entrySet())
                                if (rolesInCategory.getValue() != null) {
                                    String key = rolesInCategory.getKey();
                                    String value = "Value";

                                    switch (key) {
                                        case "adminRoles":
                                            value = "Admin";
                                            break;
                                        case "normalRoles":
                                            value = "Normal";
                                            break;
                                        case "specialRoles":
                                            value = "Special";
                                            break;
                                    }

                                    for (Map.Entry<String, Role> roleInCategory : rolesInCategory.getValue().entrySet()) {
                                        Role role = roleInCategory.getValue();
                                        if (role != null) {
                                            RoleItem roleItem = new RoleItem(role, value);
                                            roles.add(roleItem);
                                        }
                                    }

                                    types.add(new IconOption(value, 0));
                                }

                        roles.sort((role, t1) -> role.getName().compareToIgnoreCase(t1.getName()));

                        types.sort((type, t1) -> type.getLabelName().compareToIgnoreCase(t1.getLabelName()));

                        Collections.reverse(types);
                        types.add(new IconOption(getString(R.string.all), 0));
                        Collections.reverse(types);
                    }

                    rolesCopy.clear();

                    rolesCopy.addAll(roles);

                    filterUserList();

                    iconOptionAdapter.notifyDataSetChanged();

                    loadingDialog.dismissDialog();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "adminRolesQuery:onCancelled", error.toException());
                loadingDialog.dismissDialog();

                messageDialog.setTextCaption("Failed to get the admin types.");
                messageDialog.setTextType(2);
                messageDialog.showDialog();
            }
        };
    }

    @SuppressLint("NotifyDataSetChanged")
    private void filterUserList() {
        List<RoleItem> rolesTemp = new ArrayList<>(rolesCopy);

        roles.clear();

        for (int i = 0; i < rolesTemp.size(); i++) {
            boolean isSelectedRole = selectedRoleIndex == 0 ||
                    rolesTemp.get(i).getType().equals(tvSelectedType.getText().toString());

            boolean isSearchedValue = searchValue == null || searchValue.trim().length() == 0 ||
                    rolesTemp.get(i).getName() .toLowerCase().contains(searchValue.trim().toLowerCase()) ||
                    rolesTemp.get(i).getType().toLowerCase().contains(searchValue.trim().toLowerCase());

            if (isSelectedRole && isSearchedValue)
                roles.add(rolesTemp.get(i));
        }

        if (roles.size() == 0)
            tvRoleCaption.setVisibility(View.VISIBLE);
        else
            tvRoleCaption.setVisibility(View.GONE);
        tvRoleCaption.bringToFront();

        roleItemAdapter.notifyDataSetChanged();
    }

    public void onBackPressed() {
        if (currentStep > 0)
            currentStep--;
        else
            super.onBackPressed();

        if (currentStep == 0) {
            typesLayout.setVisibility(View.GONE);
            rolesLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        isListening = true;
        rolesQuery.addValueEventListener(getRoleValueListener());

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