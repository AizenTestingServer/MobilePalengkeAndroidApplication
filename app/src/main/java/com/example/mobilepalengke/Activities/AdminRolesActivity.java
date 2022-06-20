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
import android.widget.Toast;

import com.example.mobilepalengke.Adapters.IconOptionAdapter;
import com.example.mobilepalengke.Adapters.RoleItemAdapter;
import com.example.mobilepalengke.DataClasses.IconOption;
import com.example.mobilepalengke.DataClasses.Role;
import com.example.mobilepalengke.DataClasses.RoleItem;
import com.example.mobilepalengke.DataClasses.Roles;
import com.example.mobilepalengke.DataClasses.User;
import com.example.mobilepalengke.DialogClasses.LoadingDialog;
import com.example.mobilepalengke.DialogClasses.MessageDialog;
import com.example.mobilepalengke.DialogClasses.RoleDialog;
import com.example.mobilepalengke.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AdminRolesActivity extends AppCompatActivity {

    ConstraintLayout rolesLayout, typesLayout;
    EditText etSearchRole;
    TextView tvSelectedType, btnChangeType, tvRoleCaption;
    Button btnAddRole, btnBack;
    RecyclerView recyclerView, recyclerView2;

    Context context;

    LoadingDialog loadingDialog;
    MessageDialog messageDialog;
    RoleDialog roleDialog;

    FirebaseDatabase firebaseDatabase;

    Query userQuery, rolesQuery;

    String uid;

    User currentUser;
    int currentLevel;

    boolean isListening = true;

    int currentStep = 0, maxStep = 1;

    String searchValue;

    List<RoleItem> roleItems = new ArrayList<>(), roleItemsCopy = new ArrayList<>();

    RoleItemAdapter roleItemAdapter;

    List<IconOption> types = new ArrayList<>();

    IconOptionAdapter iconOptionAdapter;

    int selectedRoleIndex = 0;

    String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";

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

        btnAddRole = findViewById(R.id.btnAddRole);

        context = AdminRolesActivity.this;

        loadingDialog = new LoadingDialog(context);
        messageDialog = new MessageDialog(context);
        roleDialog = new RoleDialog(context);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) uid = firebaseUser.getUid();

        firebaseDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_RTDB_url));
        userQuery = firebaseDatabase.getReference("users").child(uid);
        rolesQuery = firebaseDatabase.getReference();

        loadingDialog.showDialog();
        isListening = true;
        userQuery.addValueEventListener(getUserValueListener());

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        roleItemAdapter = new RoleItemAdapter(context, roleItems);
        roleItemAdapter.setRoleAdapterListener(roleItem -> {
            if (roleItem.isFixed()) {
                messageDialog.setTextCaption(roleItem.getName() + " (Level " + roleItem.getLevel() + ")" +
                        "\nrole is fixed and cannot be modify.");
                messageDialog.setTextType(2);
                messageDialog.showDialog();
            } else {
                if (currentLevel > roleItem.getLevel()) {
                    roleDialog.showDialog();
                    roleDialog.setCurrentLevel(currentLevel);
                    roleDialog.setData(roleItem);
                } else {
                    messageDialog.setTextCaption("Your access level is too low to modify the " +
                            roleItem.getName() + " (Level " + roleItem.getLevel() + ") role.");
                    messageDialog.setTextType(2);
                    messageDialog.showDialog();
                }
            }
        });
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(roleItemAdapter);

        roleDialog.setDialogListener((role, roleType, prevRoleType) -> {
            loadingDialog.showDialog();

            String roleId = role.getId();
            boolean isAddMode = false;

            if (roleId == null) {
                StringBuilder roleIdBuilder = new StringBuilder();
                for (int i = 0; i < 28; i++) {
                    Random rnd = new Random();
                    roleIdBuilder.append(alphabet.charAt(rnd.nextInt(alphabet.length())));
                }
                roleId = roleIdBuilder.toString();
                isAddMode = true;
            }

            String toastMessage = "Successfully " + (isAddMode ? "added" : "updated") + " the role.";

            Role newRole = new Role(roleId, role.getName(), role.getLevel());

            DatabaseReference databaseReference  = rolesQuery.getRef().child("roles");
            DatabaseReference databaseReferenceForPrevRole  = databaseReference;

            switch (roleType) {
                case 1:
                    databaseReference = databaseReference.child("adminRoles");
                    break;
                case 2:
                    databaseReference = databaseReference.child("specialRoles");
                    break;
                default:
                    databaseReference = databaseReference.child("normalRoles");
                    break;
            }

            switch (prevRoleType) {
                case 1:
                    databaseReferenceForPrevRole = databaseReferenceForPrevRole.child("adminRoles");
                    break;
                case 2:
                    databaseReferenceForPrevRole = databaseReferenceForPrevRole.child("specialRoles");
                    break;
                default:
                    databaseReferenceForPrevRole = databaseReferenceForPrevRole.child("normalRoles");
                    break;
            }

            if (roleType != prevRoleType && !isAddMode)
                databaseReferenceForPrevRole.child(roleId).removeValue();

            databaseReference.child(roleId).setValue(newRole).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(
                            context,
                            toastMessage,
                            Toast.LENGTH_SHORT
                    ).show();

                    roleDialog.dismissDialog();
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

        btnAddRole.setOnClickListener(view -> {
            roleDialog.showDialog();
            roleDialog.setCurrentLevel(currentLevel);
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

    private ValueEventListener getUserValueListener() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    if (snapshot.exists()) {
                        User user = snapshot.getValue(User.class);
                        if (user != null  && user.getId().equals(uid))
                            currentUser = user;
                    }

                    rolesQuery.addValueEventListener(getRoleValueListener());
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

    private ValueEventListener getRoleValueListener() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    roleItems.clear();
                    types.clear();

                    if (snapshot.exists()) {
                        Roles allRoles = snapshot.getValue(Roles.class);

                        if (allRoles != null && allRoles.getRoles() != null)
                            for (Map.Entry<String, Map<String, Role>> rolesInCategory : allRoles.getRoles().entrySet())
                                if (rolesInCategory.getValue() != null) {
                                    String key = rolesInCategory.getKey();
                                    String value = "Value";

                                    if (key.equals(getString(R.string.adminRoles)))
                                        value = getString(R.string.admin);
                                    else if (key.equals(getString(R.string.normalRoles)))
                                        value = getString(R.string.normal);
                                    else if (key.equals(getString(R.string.specialRoles)))
                                        value = getString(R.string.special);

                                    for (Map.Entry<String, Role> roleInCategory : rolesInCategory.getValue().entrySet()) {
                                        Role role = roleInCategory.getValue();
                                        if (role != null) {
                                            RoleItem roleItem = new RoleItem(role, value);
                                            roleItems.add(roleItem);
                                        }
                                    }

                                    types.add(new IconOption(value, 0));
                                }
                    }

                    roleItems.sort((role, t1) -> role.getName().compareToIgnoreCase(t1.getName()));

                    currentLevel = 0;
                    for (RoleItem roleItem : roleItems) {
                        for (Map.Entry<String, String> mapRoles : currentUser.getRoles().entrySet())
                            if (mapRoles.getValue().equals(roleItem.getId())) {
                                currentLevel = Math.max(currentLevel, roleItem.getLevel());
                                break;
                            }
                    }

                    types.sort((type, t1) -> type.getLabelName().compareToIgnoreCase(t1.getLabelName()));

                    Collections.reverse(types);
                    types.add(new IconOption(getString(R.string.all), 0));
                    Collections.reverse(types);

                    roleItemsCopy.clear();

                    roleItemsCopy.addAll(roleItems);

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
        List<RoleItem> rolesTemp = new ArrayList<>(roleItemsCopy);

        roleItems.clear();

        for (int i = 0; i < rolesTemp.size(); i++) {
            boolean isSelectedRole = selectedRoleIndex == 0 ||
                    rolesTemp.get(i).getType().equals(tvSelectedType.getText().toString());

            boolean isSearchedValue = searchValue == null || searchValue.trim().length() == 0 ||
                    rolesTemp.get(i).getName() .toLowerCase().contains(searchValue.trim().toLowerCase()) ||
                    rolesTemp.get(i).getType().toLowerCase().contains(searchValue.trim().toLowerCase());

            if (isSelectedRole && isSearchedValue)
                roleItems.add(rolesTemp.get(i));
        }

        if (roleItems.size() == 0)
            tvRoleCaption.setVisibility(View.VISIBLE);
        else
            tvRoleCaption.setVisibility(View.GONE);
        tvRoleCaption.bringToFront();

        roleItemAdapter.setLevel(currentLevel);
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
        rolesQuery.addListenerForSingleValueEvent(getRoleValueListener());

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