package com.example.mobilepalengke.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mobilepalengke.Adapters.NotificationAdapter;
import com.example.mobilepalengke.DataClasses.NotificationItem;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class NotificationFragment extends Fragment {

    TextView tvNotificationCaption;
    RecyclerView recyclerView;

    Context context;

    LoadingDialog loadingDialog;
    MessageDialog messageDialog;

    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;

    boolean isListening = true;

    Query notificationsQuery;

    List<NotificationItem> notifications = new ArrayList<>();

    NotificationAdapter notificationAdapter;

    String uid;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);
        
        tvNotificationCaption = view.findViewById(R.id.tvNotificationCaption);
        recyclerView = view.findViewById(R.id.recyclerView);

        context = getContext();

        loadingDialog = new LoadingDialog(context);
        messageDialog = new MessageDialog(context);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null)
            uid = firebaseUser.getUid();

        firebaseDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_RTDB_url));
        notificationsQuery = firebaseDatabase.getReference("notifications").child(uid).orderByChild("timestamp");

        loadingDialog.showDialog();
        isListening = true;
        notificationsQuery.addValueEventListener(getNotificationValueListener());

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        notificationAdapter = new NotificationAdapter(context, notifications);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(notificationAdapter);

        return view;
    }

    private ValueEventListener getNotificationValueListener() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    notifications.clear();

                    if (snapshot.exists()) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            NotificationItem notification = dataSnapshot.getValue(NotificationItem.class);
                            if (notification != null) notifications.add(notification);
                        }
                    }

                    notifications.sort((notificationItem, t1) -> {
                        String timestamp = notificationItem.getTimestamp();
                        timestamp = timestamp.replaceAll("-", "");
                        String time = timestamp.split(" ")[1]; time = time.split(":")[0];
                        String timeType = timestamp.split(" ")[2];
                        int timeValue = Integer.parseInt(time);
                        if (timeType.equalsIgnoreCase("PM") && timeValue != 12)
                            timeValue += 12;
                        else if (timeValue == 12) timeValue = 0;

                        String timestamp2 = t1.getTimestamp();
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

                    if (notifications.size() == 0)
                        tvNotificationCaption.setVisibility(View.VISIBLE);
                    else tvNotificationCaption.setVisibility(View.GONE);
                    tvNotificationCaption.bringToFront();

                    loadingDialog.dismissDialog();

                    notificationAdapter.notifyDataSetChanged();
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
}