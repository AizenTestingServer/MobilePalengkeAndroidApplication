package com.example.mobilepalengke.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mobilepalengke.Activities.ChatActivity;
import com.example.mobilepalengke.Activities.OrderDetailsActivity;
import com.example.mobilepalengke.DataClasses.NotificationItem;
import com.example.mobilepalengke.DialogClasses.LoadingDialog;
import com.example.mobilepalengke.R;

import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    List<NotificationItem> notifications;

    LayoutInflater layoutInflater;

    Context context;

    LoadingDialog loadingDialog;

    public NotificationAdapter(Context context, List<NotificationItem> notifications) {
        this.notifications = notifications;
        this.layoutInflater = LayoutInflater.from(context);

        this.context = context;

        loadingDialog = new LoadingDialog(context);
    }

    @NonNull
    @Override
    public NotificationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.custom_notification_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationAdapter.ViewHolder holder, int position) {
        ConstraintLayout backgroundLayout = holder.backgroundLayout;
        ImageView imgAlert = holder.imgAlert;
        TextView tvNotificationTitle = holder.tvNotificationTitle,
                tvNotificationText = holder.tvNotificationText,
                tvTimestamp = holder.tvTimestamp;

        NotificationItem notification = notifications.get(position);

        tvNotificationTitle.setText(notification.getTitle());
        tvNotificationText.setText(notification.getValue());
        tvTimestamp.setText(notification.getTimestamp());

        imgAlert.setVisibility(View.GONE);
        tvNotificationText.setTextAppearance(R.style.TVFontStyle11);
        tvTimestamp.setTextAppearance(R.style.TVFontStyle11);

        if (!notification.isRead()) {
            imgAlert.setVisibility(View.VISIBLE);
            tvNotificationText.setTextAppearance(R.style.TVFontStyle3);
            tvTimestamp.setTextAppearance(R.style.TVFontStyle12);
        }

        int top = dpToPx(4), bottom = dpToPx(4);

        boolean isFirstItem = position == 0, isLastItem = position == notifications.size() - 1;

        if (isFirstItem) top = dpToPx(8);
        if (isLastItem) bottom = dpToPx(8);

        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) backgroundLayout.getLayoutParams();
        layoutParams.topMargin = top;
        layoutParams.bottomMargin = bottom;
        backgroundLayout.setLayoutParams(layoutParams);

        backgroundLayout.setOnClickListener(view -> {
            Intent intent = null;

            if (notification.getActivity().equals("ChatActivity"))
                intent = new Intent(context, ChatActivity.class);
            if (notification.getActivity().equals("OrderDetailsActivity"))
                intent = new Intent(context, OrderDetailsActivity.class);

            if (intent != null) {
                for (Map.Entry<String, String> mapAttributes : notification.getAttributes().entrySet())
                    intent.putExtra(mapAttributes.getKey(), mapAttributes.getValue());

                intent.putExtra("notificationId", notification.getId());

                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout backgroundLayout;
        ImageView imgAlert;
        TextView tvNotificationTitle, tvNotificationText, tvTimestamp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);
            imgAlert = itemView.findViewById(R.id.imgAlert);
            tvNotificationTitle = itemView.findViewById(R.id.tvNotificationTitle);
            tvNotificationText = itemView.findViewById(R.id.tvNotificationText);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);

            setIsRecyclable(false);
        }
    }

    private int dpToPx(int dp) {
        float px = dp * context.getResources().getDisplayMetrics().density;
        return (int) px;
    }
}
