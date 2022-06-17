package com.example.mobilepalengke.Activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.mobilepalengke.Adapters.IconOptionAdapter;
import com.example.mobilepalengke.Adapters.UserAdapter;
import com.example.mobilepalengke.DataClasses.IconOption;
import com.example.mobilepalengke.DataClasses.Role;
import com.example.mobilepalengke.DataClasses.Roles;
import com.example.mobilepalengke.DataClasses.User;
import com.example.mobilepalengke.DialogClasses.LoadingDialog;
import com.example.mobilepalengke.DialogClasses.MessageDialog;
import com.example.mobilepalengke.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AdminUsersActivity extends AppCompatActivity {

    ConstraintLayout userListLayout, userRolesLayout;
    EditText etSearchUser;
    TextView tvSelectedRole, btnChangeRole, tvUserListCaption;
    Button btnBack;
    RecyclerView recyclerView, recyclerView2;

    Context context;

    LoadingDialog loadingDialog;
    MessageDialog messageDialog;

    FirebaseDatabase firebaseDatabase;

    Query usersQuery, rolesQuery;

    boolean isListening = true;

    int currentStep = 0, maxStep = 1;

    String searchValue;

    List<User> users = new ArrayList<>(), usersCopy = new ArrayList<>();
    List<String> usersRoles = new ArrayList<>(), usersRolesCopy = new ArrayList<>();

    UserAdapter userAdapter;

    List<IconOption> roles = new ArrayList<>();
    List<String> rolesId = new ArrayList<>();

    IconOptionAdapter iconOptionAdapter;

    int selectedRoleIndex = 0;
    String selectedRoleId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_users);

        userListLayout = findViewById(R.id.userListLayout);
        userRolesLayout = findViewById(R.id.userRolesLayout);

        etSearchUser = findViewById(R.id.etSearchUser);

        tvSelectedRole = findViewById(R.id.tvSelectedRole);

        btnBack = findViewById(R.id.btnBack);
        btnChangeRole = findViewById(R.id.btnChangeRole);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView2 = findViewById(R.id.recyclerView2);

        tvUserListCaption = findViewById(R.id.tvUserListCaption);

        context = AdminUsersActivity.this;

        loadingDialog = new LoadingDialog(context);
        messageDialog = new MessageDialog(context);

        firebaseDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_RTDB_url));
        usersQuery = firebaseDatabase.getReference("users");
        rolesQuery = firebaseDatabase.getReference();

        loadingDialog.showDialog();
        isListening = true;
        usersQuery.addValueEventListener(getUsersValueListener());

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        userAdapter = new UserAdapter(context, users, usersRoles);
        userAdapter.setUserAdapterListener(new UserAdapter.UserAdapterListener() {
            @Override
            public void onClick(User user) {

            }
        });
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(userAdapter);

        iconOptionAdapter = new IconOptionAdapter(context, roles);
        iconOptionAdapter.setIconOptionAdapterListener(new IconOptionAdapter.IconOptionAdapterListener() {
            @Override
            public void onClick(IconOption iconOption) {

            }

            @Override
            public void onClick(IconOption iconOption, int position) {
                currentStep--;

                tvSelectedRole.setText(iconOption.getLabelName());

                selectedRoleIndex = position;
                selectedRoleId = rolesId.get(position);

                userRolesLayout.setVisibility(View.GONE);
                userListLayout.setVisibility(View.VISIBLE);

                filterUserList();
            }
        });
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL,
                false);
        recyclerView2.setLayoutManager(linearLayoutManager2);
        recyclerView2.setAdapter(iconOptionAdapter);

        etSearchUser.addTextChangedListener(new TextWatcher() {
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

        btnChangeRole.setOnClickListener(view1 -> {
            if (currentStep < maxStep)
                currentStep++;

            userListLayout.setVisibility(View.GONE);
            userRolesLayout.setVisibility(View.VISIBLE);
        });

        btnBack.setOnClickListener(view1 -> {
            currentStep--;

            userRolesLayout.setVisibility(View.GONE);
            userListLayout.setVisibility(View.VISIBLE);
        });
    }

    private ValueEventListener getUsersValueListener() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    users.clear();

                    if (snapshot.exists()) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            User user = dataSnapshot.getValue(User.class);
                            if (user != null)
                                users.add(user);
                        }
                    }

                    rolesQuery.addValueEventListener(getRoleValueListener());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "usersQuery:onCancelled", error.toException());
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
                    usersRoles.clear();
                    roles.clear();
                    rolesId.clear();

                    if (snapshot.exists()) {
                        roles.add(new IconOption(getString(R.string.all), 0));
                        rolesId.add("r00");

                        for (User user : users) {
                            List<String> roleIds = user.getRoles() != null ?
                                    new ArrayList<>(user.getRoles().values()) :
                                    new ArrayList<>();
                            List<String> roles = new ArrayList<>();

                            for (String roleId : roleIds) {
                                for (DataSnapshot dataSnapshot : snapshot.child("roles").getChildren()) {
                                    Role role = dataSnapshot.child(roleId.trim()).getValue(Role.class);
                                    if (role != null) {
                                        roles.add(role.getName());
                                        break;
                                    }
                                }
                            }

                            usersRoles.add(TextUtils.join(", ", roles));
                        }

                        Roles allRoles = snapshot.getValue(Roles.class);

                        if (allRoles != null && allRoles.getRoles() != null)
                            for (Map.Entry<String, Map<String, Role>> rolesInCategory : allRoles.getRoles().entrySet())
                                if (rolesInCategory.getValue() != null)
                                    for (Map.Entry<String, Role> roleInCategory : rolesInCategory.getValue().entrySet()) {
                                        Role role = roleInCategory.getValue();
                                        if (role != null) {
                                            roles.add(new IconOption(role.getName(), 0));
                                            rolesId.add(roleInCategory.getKey());
                                        }
                                    }
                    }

                    usersCopy.clear();
                    usersRolesCopy.clear();

                    usersCopy.addAll(users);
                    usersRolesCopy.addAll(usersRoles);

                    filterUserList();

                    iconOptionAdapter.notifyDataSetChanged();

                    loadingDialog.dismissDialog();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "adminRolesQuery:onCancelled", error.toException());
                loadingDialog.dismissDialog();

                messageDialog.setTextCaption("Failed to get the admin roles.");
                messageDialog.setTextType(2);
                messageDialog.showDialog();
            }
        };
    }

    @SuppressLint("NotifyDataSetChanged")
    private void filterUserList() {
        List<User> usersTemp = new ArrayList<>(usersCopy);
        List<String> usersRolesTemp = new ArrayList<>(usersRolesCopy);

        users.clear();
        usersRoles.clear();

        for (int i = 0; i < usersTemp.size(); i++) {
            List<String> roleIds = usersTemp.get(i).getRoles() != null ?
                    new ArrayList<>(usersTemp.get(i).getRoles().values()) :
                    new ArrayList<>();

            boolean isSelectedRole = selectedRoleId == null || selectedRoleIndex == 0 ||
                    roleIds.contains(selectedRoleId);

            boolean isSearchedValue = searchValue == null || searchValue.trim().length() == 0 ||
                    (usersTemp.get(i).getFirstName() + " " + usersTemp.get(i).getLastName())
                            .toLowerCase().contains(searchValue.trim().toLowerCase()) ||
                    (usersTemp.get(i).getLastName() + " " + usersTemp.get(i).getFirstName())
                            .toLowerCase().contains(searchValue.trim().toLowerCase()) ||
                    usersRolesTemp.get(i).toLowerCase().contains(searchValue.trim().toLowerCase());

            if (isSelectedRole && isSearchedValue) {
                users.add(usersTemp.get(i));
                usersRoles.add(usersRolesTemp.get(i));
            }
        }

        if (users.size() == 0)
            tvUserListCaption.setVisibility(View.VISIBLE);
        else
            tvUserListCaption.setVisibility(View.GONE);
        tvUserListCaption.bringToFront();

        userAdapter.notifyDataSetChanged();
    }

    public void onBackPressed() {
        if (currentStep > 0)
            currentStep--;
        else
            super.onBackPressed();

        if (currentStep == 0) {
            userRolesLayout.setVisibility(View.GONE);
            userListLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        isListening = true;
        usersQuery.addValueEventListener(getUsersValueListener());

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