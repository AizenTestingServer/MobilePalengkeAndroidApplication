package com.example.mobilepalengke.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.mobilepalengke.Adapters.ChatAdapter;
import com.example.mobilepalengke.Adapters.ChatUserAdapter;
import com.example.mobilepalengke.Adapters.IconOptionAdapter;
import com.example.mobilepalengke.DataClasses.Chat;
import com.example.mobilepalengke.DataClasses.IconOption;
import com.example.mobilepalengke.DataClasses.Message;
import com.example.mobilepalengke.DataClasses.Role;
import com.example.mobilepalengke.DataClasses.User;
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
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ChatListFragment extends Fragment {

    ConstraintLayout chatListLayout, chatCategoriesLayout, userListLayout, userRolesLayout;
    EditText etSearchChat, etSearchUser;
    TextView tvSelectedCategory, btnChangeCategory, tvChatListCaption, tvSelectedRole, btnChangeRole, tvUserListCaption;
    Button btnBack, btnComposeMessage, btnBack2, btnBack3;
    RecyclerView recyclerView, recyclerView2, recyclerView3, recyclerView4;

    Context context;

    LoadingDialog loadingDialog;
    MessageDialog messageDialog;

    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;

    boolean isListening = true;

    Query chatListQuery, usersQuery, adminRolesQuery;

    int currentStep = 0, maxStep = 2;

    String uid;
    String searchValue, searchValue2;

    List<Chat> chatList = new ArrayList<>(), chatListCopy = new ArrayList<>();
    List<Message> messages = new ArrayList<>(), messagesCopy = new ArrayList<>();
    List<User> chattedUsers = new ArrayList<>(), chattedUsersCopy = new ArrayList<>();
    List<String> chattedUsersRoles = new ArrayList<>(), chattedUsersRolesCopy = new ArrayList<>();

    ChatAdapter chatAdapter;

    List<IconOption> adminRoles = new ArrayList<>();
    List<String> adminRolesId = new ArrayList<>();

    IconOptionAdapter iconOptionAdapter;

    int selectedCategoryIndex = 0, selectedRoleIndex = 0;
    String selectedCategoryId, selectedRoleId;

    List<User> users = new ArrayList<>(), usersCopy = new ArrayList<>();
    List<String> usersRoles = new ArrayList<>(), usersRolesCopy = new ArrayList<>();

    ChatUserAdapter chatUserAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);

        chatListLayout = view.findViewById(R.id.chatListLayout);
        chatCategoriesLayout = view.findViewById(R.id.chatCategoriesLayout);
        userListLayout = view.findViewById(R.id.userListLayout);
        userRolesLayout = view.findViewById(R.id.userRolesLayout);

        etSearchChat = view.findViewById(R.id.etSearchChat);
        etSearchUser = view.findViewById(R.id.etSearchUser);

        tvSelectedCategory = view.findViewById(R.id.tvSelectedCategory);
        tvSelectedRole = view.findViewById(R.id.tvSelectedRole);

        btnChangeCategory = view.findViewById(R.id.btnChangeCategory);
        btnBack = view.findViewById(R.id.btnBack);
        btnComposeMessage = view.findViewById(R.id.btnComposeMessage);
        btnChangeRole = view.findViewById(R.id.btnChangeRole);
        btnBack2 = view.findViewById(R.id.btnBack2);
        btnBack3 = view.findViewById(R.id.btnBack3);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView2 = view.findViewById(R.id.recyclerView2);
        recyclerView3 = view.findViewById(R.id.recyclerView3);
        recyclerView4 = view.findViewById(R.id.recyclerView4);

        tvChatListCaption = view.findViewById(R.id.tvChatListCaption);
        tvUserListCaption = view.findViewById(R.id.tvUserListCaption);

        context = getContext();

        loadingDialog = new LoadingDialog(context);
        messageDialog = new MessageDialog(context);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null)
            uid = firebaseUser.getUid();

        firebaseDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_RTDB_url));
        usersQuery = firebaseDatabase.getReference("users");
        adminRolesQuery = firebaseDatabase.getReference("roles").child("adminRoles");
        chatListQuery = firebaseDatabase.getReference("chatList");

        loadingDialog.showDialog();
        isListening = true;
        chatListQuery.addValueEventListener(getChatListValueListener());

        chatAdapter = new ChatAdapter(context, chatList, messages, chattedUsers, chattedUsersRoles, uid);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(chatAdapter);

        iconOptionAdapter = new IconOptionAdapter(context, adminRoles);
        iconOptionAdapter.setIconOptionAdapterListener(new IconOptionAdapter.IconOptionAdapterListener() {
            @Override
            public void onClick(IconOption iconOption) {

            }

            @Override
            public void onClick(IconOption iconOption, int position) {
                currentStep--;

                if (chatCategoriesLayout.getVisibility() == View.VISIBLE) {
                    tvSelectedCategory.setText(iconOption.getLabelName());

                    selectedCategoryIndex = position;
                    selectedCategoryId = adminRolesId.get(position);

                    chatCategoriesLayout.setVisibility(View.GONE);
                    chatListLayout.setVisibility(View.VISIBLE);

                    filterChatList();
                } else if (userRolesLayout.getVisibility() == View.VISIBLE) {

                    Log.e("TAG: " + context.getClass(), adminRolesId.get(position));

                    tvSelectedRole.setText(iconOption.getLabelName());

                    selectedRoleIndex = position;
                    selectedRoleId = adminRolesId.get(position);

                    userRolesLayout.setVisibility(View.GONE);
                    userListLayout.setVisibility(View.VISIBLE);

                    filterUserList();
                }
            }
        });
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL,
                false);
        recyclerView2.setLayoutManager(linearLayoutManager2);
        recyclerView2.setAdapter(iconOptionAdapter);

        chatUserAdapter = new ChatUserAdapter(context, users, usersRoles);
        LinearLayoutManager linearLayoutManager3 = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL,
                false);
        recyclerView3.setLayoutManager(linearLayoutManager3);
        recyclerView3.setAdapter(chatUserAdapter);

        LinearLayoutManager linearLayoutManager4 = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL,
                false);
        recyclerView4.setLayoutManager(linearLayoutManager4);
        recyclerView4.setAdapter(iconOptionAdapter);

        etSearchChat.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                searchValue = editable.toString();

                filterChatList();
            }
        });

        etSearchUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                searchValue2 = editable.toString();

                filterUserList();
            }
        });

        btnChangeCategory.setOnClickListener(view1 -> {
            if (currentStep < maxStep)
                currentStep++;

            chatListLayout.setVisibility(View.GONE);
            chatCategoriesLayout.setVisibility(View.VISIBLE);
        });

        btnBack.setOnClickListener(view1 -> {
            currentStep--;

            chatCategoriesLayout.setVisibility(View.GONE);
            chatListLayout.setVisibility(View.VISIBLE);
        });

        btnComposeMessage.setOnClickListener(view12 -> {
            if (currentStep < maxStep)
                currentStep++;

            chatListLayout.setVisibility(View.GONE);
            userListLayout.setVisibility(View.VISIBLE);
        });

        btnChangeRole.setOnClickListener(view1 -> {
            if (currentStep < maxStep)
                currentStep++;

            userListLayout.setVisibility(View.GONE);
            userRolesLayout.setVisibility(View.VISIBLE);
        });

        btnBack2.setOnClickListener(view1 -> {
            currentStep--;

            userListLayout.setVisibility(View.GONE);
            chatListLayout.setVisibility(View.VISIBLE);
        });

        btnBack3.setOnClickListener(view1 -> {
            currentStep--;

            userRolesLayout.setVisibility(View.GONE);
            userListLayout.setVisibility(View.VISIBLE);
        });

        return view;
    }

    private ValueEventListener getChatListValueListener() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    chatList.clear();
                    messages.clear();

                    if (snapshot.exists()) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Chat chat = dataSnapshot.getValue(Chat.class);
                            if (chat != null) {
                                List<String> participantsId = chat.getParticipants() != null ?
                                        new ArrayList<>(chat.getParticipants().values()) :
                                        new ArrayList<>();

                                if (participantsId.contains(uid))
                                    chatList.add(chat);
                            }
                        }
                    }

                    chatList.sort((chat, t1) -> {
                        List<Message> messagesTemp = chat.getMessages() != null ?
                                new ArrayList<>(chat.getMessages().values()) :
                                new ArrayList<>();
                        messagesTemp.sort((message, t2) -> message.getId().compareToIgnoreCase(t2.getId()));
                        Collections.reverse(messagesTemp);
                        Message messageTemp = messagesTemp.get(0);

                        String timestamp = messageTemp.getTimestamp();
                        timestamp = timestamp.replaceAll("-", "");
                        String time = timestamp.split(" ")[1]; time = time.split(":")[0];
                        String timeType = timestamp.split(" ")[2];
                        int timeValue = Integer.parseInt(time);
                        if (timeType.equalsIgnoreCase("PM") && timeValue != 12)
                            timeValue += 12;
                        else if (timeValue == 12) timeValue = 0;

                        messagesTemp = t1.getMessages() != null ?
                                new ArrayList<>(t1.getMessages().values()) :
                                new ArrayList<>();
                        messagesTemp.sort((message, t2) -> message.getId().compareToIgnoreCase(t2.getId()));
                        Collections.reverse(messagesTemp);
                        messageTemp = messagesTemp.get(0);

                        String timestamp2 = messageTemp.getTimestamp();
                        timestamp2 = timestamp2.replaceAll("-", "");
                        String time2 = timestamp2.split(" ")[1]; time2 = time2.split(":")[0];
                        String timeType2 = timestamp2.split(" ")[2];
                        int timeValue2 = Integer.parseInt(time2);
                        if (timeType2.equalsIgnoreCase("PM") && timeValue2 != 12)
                            timeValue2 += 12;
                        else if (timeValue2 == 12) timeValue2 = 0;

                        time = String.valueOf(timeValue).length() < 2 ?
                                "0" + timeValue : String.valueOf(timeValue);
                        time += timestamp.split(" ")[1]; time = time.split(":")[1];
                        timestamp = timestamp.split(" ")[0] + time;

                        time2 = String.valueOf(timeValue2).length() < 2 ?
                                "0" + timeValue2 : String.valueOf(timeValue2);
                        time2 += timestamp2.split(" ")[1]; time2 = time2.split(":")[1];
                        timestamp2 = timestamp2.split(" ")[0] + time2;

                        return timestamp2.compareToIgnoreCase(timestamp);
                    });

                    for (Chat chat : chatList) {
                        List<Message> messagesTemp = chat.getMessages() != null ?
                                new ArrayList<>(chat.getMessages().values()) :
                                new ArrayList<>();
                        messagesTemp.sort((message, t1) -> message.getId().compareToIgnoreCase(t1.getId()));
                        Collections.reverse(messagesTemp);
                        messages.add(messagesTemp.get(0));
                    }

                    usersQuery.addValueEventListener(getUsersValueListener());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "chatListQuery:onCancelled", error.toException());
                loadingDialog.dismissDialog();

                messageDialog.setTextCaption("Failed to get the chat list.");
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
                    users.clear();
                    chattedUsers.clear();

                    if (snapshot.exists()) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            User user = dataSnapshot.getValue(User.class);
                            if (user != null && !user.getId().equals(uid) && user.getRoles() != null)
                                users.add(user);
                        }

                        for (Chat chat : chatList) {
                            String endPointUid = uid;

                            List<String> participantsId = chat.getParticipants() != null ?
                                    new ArrayList<>(chat.getParticipants().values()) :
                                    new ArrayList<>();
                            for (String participantId : participantsId)
                                if (!participantId.trim().equals(uid)) {
                                    endPointUid = participantId.trim();
                                    break;
                                }

                            User user = snapshot.child(endPointUid).getValue(User.class);
                            if (user != null)
                                chattedUsers.add(user);
                        }
                    }

                    adminRolesQuery.addValueEventListener(getARValueListener());
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

    private ValueEventListener getARValueListener() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    usersRoles.clear();
                    chattedUsersRoles.clear();
                    adminRoles.clear();
                    adminRolesId.clear();

                    if (snapshot.exists()) {
                        adminRoles.add(new IconOption(getString(R.string.all), 0));
                        adminRolesId.add("ar00");

                        for (User user : chattedUsers) {
                            List<String> roleIds = user.getRoles() != null ?
                                    new ArrayList<>(user.getRoles().values()) :
                                    new ArrayList<>();
                            List<String> roles = new ArrayList<>();

                            for (String roleId : roleIds) {
                                Role role = snapshot.child(roleId.trim()).getValue(Role.class);
                                if (role != null)
                                    roles.add(role.getName());
                            }

                            chattedUsersRoles.add(TextUtils.join(", ", roles));
                        }

                        List<User> usersTemp = new ArrayList<>();

                        for (User user : users) {
                            List<String> roleIds = user.getRoles() != null ?
                                    new ArrayList<>(user.getRoles().values()) :
                                    new ArrayList<>();
                            List<String> roles = new ArrayList<>();

                            for (String roleId : roleIds) {
                                Role role = snapshot.child(roleId.trim()).getValue(Role.class);
                                if (role != null)
                                    roles.add(role.getName());
                            }

                            if (TextUtils.join(", ", roles).length() != 0) {
                                usersTemp.add(user);
                                usersRoles.add(TextUtils.join(", ", roles));
                            }
                        }

                        users.clear();
                        users.addAll(usersTemp);

                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Role role = dataSnapshot.getValue(Role.class);
                            if (role != null) {
                                adminRoles.add(new IconOption(role.getName(), 0));
                                adminRolesId.add(dataSnapshot.getKey());
                            }
                        }
                    }

                    chatListCopy.clear();
                    messagesCopy.clear();
                    chattedUsersCopy.clear();
                    chattedUsersRolesCopy.clear();

                    chatListCopy.addAll(chatList);
                    messagesCopy.addAll(messages);
                    chattedUsersCopy.addAll(chattedUsers);
                    chattedUsersRolesCopy.addAll(chattedUsersRoles);

                    usersCopy.clear();
                    usersRolesCopy.clear();

                    usersCopy.addAll(users);
                    usersRolesCopy.addAll(usersRoles);

                    filterChatList();
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
    private void filterChatList() {
        List<Chat> chatListTemp = new ArrayList<>(chatListCopy);
        List<Message> messagesTemp = new ArrayList<>(messagesCopy);
        List<User> chattedUsersTemp = new ArrayList<>(chattedUsersCopy);
        List<String> chattedUsersRolesTemp = new ArrayList<>(chattedUsersRolesCopy);

        chatList.clear();
        messages.clear();
        chattedUsers.clear();
        chattedUsersRoles.clear();

        for (int i = 0; i < chatListTemp.size(); i++) {
            List<String> roleIds = chattedUsersTemp.get(i).getRoles() != null ?
                    new ArrayList<>(chattedUsersTemp.get(i).getRoles().values()) :
                    new ArrayList<>();

            boolean isSelectedCategory = selectedCategoryId == null || selectedCategoryIndex == 0 ||
                    roleIds.contains(selectedCategoryId);

            boolean isSearchedValue = searchValue == null || searchValue.trim().length() == 0 ||
                    (chattedUsersTemp.get(i).getFirstName() + " " + chattedUsersTemp.get(i).getLastName())
                            .toLowerCase().contains(searchValue.trim().toLowerCase()) ||
                    (chattedUsersTemp.get(i).getLastName() + " " + chattedUsersTemp.get(i).getFirstName())
                            .toLowerCase().contains(searchValue.trim().toLowerCase()) ||
                    chattedUsersRolesTemp.get(i).toLowerCase().contains(searchValue.trim().toLowerCase());

            if (isSelectedCategory && isSearchedValue) {
                chatList.add(chatListTemp.get(i));
                messages.add(messagesTemp.get(i));
                chattedUsers.add(chattedUsersTemp.get(i));
                chattedUsersRoles.add(chattedUsersRolesTemp.get(i));
            }
        }

        if (chatList.size() == 0)
            tvChatListCaption.setVisibility(View.VISIBLE);
        else
            tvChatListCaption.setVisibility(View.GONE);
        tvChatListCaption.bringToFront();

        chatAdapter.notifyDataSetChanged();
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

            boolean isSearchedValue = searchValue2 == null || searchValue2.trim().length() == 0 ||
                    (usersTemp.get(i).getFirstName() + " " + usersTemp.get(i).getLastName())
                            .toLowerCase().contains(searchValue2.trim().toLowerCase()) ||
                    (usersTemp.get(i).getLastName() + " " + usersTemp.get(i).getFirstName())
                            .toLowerCase().contains(searchValue2.trim().toLowerCase()) ||
                    usersRolesTemp.get(i).toLowerCase().contains(searchValue2.trim().toLowerCase());

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

        chatUserAdapter.notifyDataSetChanged();
    }

    public void onBackPressed() {
        if (currentStep > 0)
            currentStep--;

        if (currentStep == 0) {
            if (chatCategoriesLayout.getVisibility() == View.VISIBLE)
                chatCategoriesLayout.setVisibility(View.GONE);
            else if (userListLayout.getVisibility() == View.VISIBLE)
                userListLayout.setVisibility(View.GONE);

            chatListLayout.setVisibility(View.VISIBLE);
        } else if (currentStep == 1) {
            userRolesLayout.setVisibility(View.GONE);
            userListLayout.setVisibility(View.VISIBLE);
        }
    }

    public int getCurrentStep() {
        return currentStep;
    }

    @Override
    public void onResume() {
        isListening = true;
        chatListQuery.addListenerForSingleValueEvent(getChatListValueListener());

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