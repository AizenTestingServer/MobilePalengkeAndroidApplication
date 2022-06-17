package com.example.mobilepalengke.Activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mobilepalengke.Adapters.ChatMessageAdapter;
import com.example.mobilepalengke.DataClasses.Chat;
import com.example.mobilepalengke.DataClasses.Message;
import com.example.mobilepalengke.DataClasses.NotificationItem;
import com.example.mobilepalengke.DataClasses.Role;
import com.example.mobilepalengke.DataClasses.User;
import com.example.mobilepalengke.DialogClasses.LoadingDialog;
import com.example.mobilepalengke.DialogClasses.MessageDialog;
import com.example.mobilepalengke.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ChatActivity extends AppCompatActivity {

    TextView tvFullName, tvRoles, tvChatMessageCaption;
    RecyclerView recyclerView;
    EditText etMessage;
    ImageView imgAlert, imgSend;

    Context context;

    LoadingDialog loadingDialog;
    MessageDialog messageDialog;

    String uid, endPointUid, chatId, notificationId, endPointNotificationId;

    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;

    boolean isListening = true;

    String messageText;

    Query userQuery, endPointUserQuery, adminRolesQuery, chatQuery, messagesQuery, notificationsQuery, endPointNotificationsQuery;

    User user, endPointUser;
    Chat currentChat;
    List<Message> messages = new ArrayList<>();

    ChatMessageAdapter chatMessageAdapter;

    String fullName, endPointUserFullName;
    int overallChatCount = 0, overallEndUserNotificationCount = 0;

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd-yy h:mm a", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        tvFullName = findViewById(R.id.tvFullName);
        tvRoles = findViewById(R.id.tvRoles);
        imgAlert = findViewById(R.id.imgAlert);
        tvChatMessageCaption = findViewById(R.id.tvChatMessageCaption);

        recyclerView = findViewById(R.id.recyclerView);
        etMessage = findViewById(R.id.etMessage);
        imgSend = findViewById(R.id.imgSend);

        context = ChatActivity.this;

        loadingDialog = new LoadingDialog(context);
        messageDialog = new MessageDialog(context);

        endPointUid = getIntent().getStringExtra("endPointUid");
        chatId = getIntent().getStringExtra("chatId");
        notificationId = getIntent().getStringExtra("notificationId");

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null)
            uid = firebaseUser.getUid();

        firebaseDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_RTDB_url));
        userQuery = firebaseDatabase.getReference("users").child(uid);
        endPointUserQuery = firebaseDatabase.getReference("users").child(endPointUid);
        adminRolesQuery = firebaseDatabase.getReference("roles").child("adminRoles");
        chatQuery = firebaseDatabase.getReference("chatList");
        notificationsQuery = firebaseDatabase.getReference("notifications").child(uid);
        endPointNotificationsQuery = firebaseDatabase.getReference("notifications").child(endPointUid);

        loadingDialog.showDialog();
        isListening = true;
        userQuery.addValueEventListener(getUserValueListener());

        chatMessageAdapter = new ChatMessageAdapter(context, messages, uid);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(chatMessageAdapter);

        imgAlert.setOnClickListener(view -> Toast.makeText(
                context,
                endPointUserFullName + " is reading the chat messages",
                Toast.LENGTH_SHORT).show());

        etMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                messageText = editable.toString();
            }
        });

        imgSend.setOnClickListener(view -> sendMessage());
    }

    private ValueEventListener getUserValueListener() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    if (snapshot.exists())
                        user = snapshot.getValue(User.class);

                    if (user != null)
                        fullName = user.getFirstName() + " " + user.getLastName();

                    endPointUserQuery.addValueEventListener(getEndPointUserValueListener());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "endPointUserQuery:onCancelled", error.toException());
                loadingDialog.dismissDialog();

                messageDialog.setTextCaption("Failed to get the end point user.");
                messageDialog.setTextType(2);
                messageDialog.showDialog();
            }
        };
    }

    private ValueEventListener getEndPointUserValueListener() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    if (snapshot.exists())
                        endPointUser = snapshot.getValue(User.class);

                    if (endPointUser != null) {
                        endPointUserFullName = endPointUser.getFirstName() + " " + endPointUser.getLastName();
                        tvFullName.setText(endPointUserFullName);
                    }

                    adminRolesQuery.addValueEventListener(getARValueListener());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "endPointUserQuery:onCancelled", error.toException());
                loadingDialog.dismissDialog();

                messageDialog.setTextCaption("Failed to get the end point user.");
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
                    if (snapshot.exists()) {
                        List<String> roleIds = new ArrayList<>(endPointUser.getRoles().values());
                        List<String> roles = new ArrayList<>();

                        for (String roleId : roleIds)
                            if (roleId.contains("ar")) {
                                Role role = snapshot.child(roleId.trim()).getValue(Role.class);
                                if (role != null)
                                    roles.add(role.getName());
                            }

                        tvRoles.setText(TextUtils.join(", ", roles));
                    }

                    chatQuery.addValueEventListener(getChatValueListener());
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

    private ValueEventListener getChatValueListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    imgAlert.setVisibility(View.GONE);

                    if (snapshot.exists()) {
                        overallChatCount = (int) snapshot.getChildrenCount();

                        if (chatId != null) {
                            currentChat = snapshot.child(chatId).getValue(Chat.class);
                            if (currentChat != null)
                                for (Map.Entry<String, Boolean> mapIsReading : currentChat.getIsReading().entrySet())
                                    if (mapIsReading.getKey().equals(endPointUid) && mapIsReading.getValue()) {
                                        imgAlert.setVisibility(View.VISIBLE);
                                        break;
                                    }
                        } else {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                Chat chat = dataSnapshot.getValue(Chat.class);
                                if (chat != null && chat.getParticipants().containsValue(uid) &&
                                        chat.getParticipants().containsValue(endPointUid)) {
                                    chatId = chat.getId();
                                    currentChat = chat;
                                    break;
                                }
                            }
                        }

                        if (chatId != null) {
                            snapshot.child(chatId).child("isRead").child(uid).getRef().setValue(true);
                            snapshot.child(chatId).child("isReading").child(uid).getRef().setValue(true);

                            messagesQuery = snapshot.child(chatId).child("messages").getRef().orderByChild("id");
                            messagesQuery.addValueEventListener(getMessagesValueListener());
                        } else
                            endPointNotificationsQuery.addValueEventListener(getEndUserNotificationValueListener());
                    } else
                        endPointNotificationsQuery.addValueEventListener(getEndUserNotificationValueListener());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "chatQuery:onCancelled", error.toException());
                loadingDialog.dismissDialog();

                messageDialog.setTextCaption("Failed to get the chat.");
                messageDialog.setTextType(2);
                messageDialog.showDialog();
            }
        };
    }

    private ValueEventListener getMessagesValueListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    messages.clear();

                    if (snapshot.exists())
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Message message = dataSnapshot.getValue(Message.class);
                            if (message != null)
                                messages.add(message);
                        }

                    Collections.reverse(messages);

                    endPointNotificationsQuery.addValueEventListener(getEndUserNotificationValueListener());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "messagesQuery:onCancelled", error.toException());
                loadingDialog.dismissDialog();

                messageDialog.setTextCaption("Failed to get the chat messages.");
                messageDialog.setTextType(2);
                messageDialog.showDialog();
            }
        };
    }

    private ValueEventListener getEndUserNotificationValueListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    overallEndUserNotificationCount = 0;

                    if (snapshot.exists()) {
                        overallEndUserNotificationCount = (int) snapshot.getChildrenCount();

                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            NotificationItem notification = dataSnapshot.getValue(NotificationItem.class);
                            if (notification != null && notification.getAttributes().containsValue(uid)) {
                                endPointNotificationId = notification.getId();
                                break;
                            }
                        }
                    }

                    if (notificationId != null) {
                        isListening = false;
                        notificationsQuery.getRef().child(notificationId).child("read").setValue(true);
                        isListening = true;
                    }

                    updateChatView();
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

    @SuppressLint("NotifyDataSetChanged")
    private void updateChatView() {
        if (messages.size() == 0)
            tvChatMessageCaption.setVisibility(View.VISIBLE);
        else
            tvChatMessageCaption.setVisibility(View.GONE);
        tvChatMessageCaption.bringToFront();

        chatMessageAdapter.notifyDataSetChanged();

        loadingDialog.dismissDialog();
    }

    private void sendMessage() {
        if (messageText != null && messageText.trim().length() > 0) {
            if (chatId == null)
                chatId = "chat" + ((String.valueOf(overallChatCount + 1).length() < 2) ? "0" + (overallChatCount + 1)
                        : (int) (overallChatCount + 1));

            String msgId = "msg" + ((String.valueOf(messages.size() + 1).length() < 2) ? "0" + (messages.size() + 1)
                    : (int) (messages.size() + 1));

            String curDateAndTime = simpleDateFormat.format(new Date());

            Message message = new Message(msgId, uid, curDateAndTime, messageText);

            if (endPointNotificationId == null)
                endPointNotificationId = "notif" + ((String.valueOf(overallEndUserNotificationCount + 1).length() < 2) ?
                        "0" + (overallEndUserNotificationCount + 1) : (int) (overallEndUserNotificationCount + 1));

            String notifDescription = "Chat with " + fullName,
                    notifTitle = "Mobile Palengke Chat",
                    notifValue = fullName + " messaged you: " + messageText,
                    activityText = "ChatActivity";
            Map<String, String> mapAttributes = new HashMap<>();
            mapAttributes.put("chatId", chatId);
            mapAttributes.put("endPointUid", uid);

            NotificationItem notification = new NotificationItem(
                    endPointNotificationId, notifDescription, notifTitle, notifValue, curDateAndTime, activityText,
                    2, 2, 3, mapAttributes
            );

            firebaseDatabase.getReference("chatList").child(chatId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                snapshot.child("isRead").child(endPointUid).getRef().setValue(false);
                                snapshot.child("messages").child(msgId).getRef().setValue(message)
                                        .addOnCompleteListener(getMSGSentOnCompleteListener());
                            } else {
                                Map<String, String> mapParticipant = new HashMap<>();
                                mapParticipant.put("parti01", uid);
                                mapParticipant.put("parti02", endPointUid);

                                Map<String, Message> mapMessage = new HashMap<>();
                                mapMessage.put("msg01", message);

                                Map<String, Boolean> mapIsRead = new HashMap<>();
                                mapIsRead.put(uid, true);
                                mapIsRead.put(endPointUid, false);

                                Map<String, Boolean> mapIsReading = new HashMap<>();
                                mapIsReading.put(uid, true);
                                mapIsReading.put(endPointUid, false);

                                Chat chat = new Chat(chatId, mapParticipant, mapMessage, mapIsRead, mapIsReading);
                                snapshot.getRef().setValue(chat).addOnCompleteListener(getMSGSentOnCompleteListener());
                            }
                            
                            endPointNotificationsQuery.getRef().child(endPointNotificationId).setValue(notification);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("TAG: " + context.getClass(), "chatReference:onCancelled", error.toException());
                            loadingDialog.dismissDialog();

                            messageDialog.setTextCaption("Failed to get the current chat.");
                            messageDialog.setTextType(2);
                            messageDialog.showDialog();
                        }
                    });
        }
    }

    private OnCompleteListener<Void> getMSGSentOnCompleteListener() {
        return task -> {
            if (task.isSuccessful())
                etMessage.getText().clear();
            else {
                String error = "Failed to send a message, please try again later.";

                loadingDialog.dismissDialog();

                messageDialog.setTextCaption(error);
                messageDialog.setTextType(2);
                messageDialog.showDialog();
            }
        };
    }

    @Override
    public void onResume() {
        isListening = true;
        endPointUserQuery.addValueEventListener(getEndPointUserValueListener());

        if (chatId != null)
            firebaseDatabase.getReference("chatList").child(chatId)
                    .child("isReading").child(uid).getRef().setValue(true);

        super.onResume();
    }

    @Override
    public void onStop() {
        isListening = false;

        if (chatId != null)
            firebaseDatabase.getReference("chatList").child(chatId)
                    .child("isReading").child(uid).getRef().setValue(false);

        super.onStop();
    }

    @Override
    public void onDestroy() {
        isListening = false;

        if (chatId != null)
            firebaseDatabase.getReference("chatList").child(chatId)
                    .child("isReading").child(uid).getRef().setValue(false);

        super.onDestroy();
    }
}