package com.example.mobilepalengke.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mobilepalengke.Activities.ChatActivity;
import com.example.mobilepalengke.DataClasses.Chat;
import com.example.mobilepalengke.DataClasses.Message;
import com.example.mobilepalengke.DataClasses.User;
import com.example.mobilepalengke.R;

import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    LayoutInflater layoutInflater;

    Context context;

    List<Chat> chatList;
    List<Message> messages;
    List<User> users;
    List<String> usersRoles;

    String uid;

    public ChatAdapter(Context context, List<Chat> chatList, List<Message> messages,
            List<User> users, List<String> usersRoles, String uid) {
        this.chatList = chatList;
        this.messages = messages;
        this.users = users;
        this.usersRoles = usersRoles;
        this.uid = uid;
        this.layoutInflater = LayoutInflater.from(context);

        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.custom_chat_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ConstraintLayout constraintLayout = holder.constraintLayout,
                backgroundLayout = holder.backgroundLayout;
        TextView tvFullName = holder.tvFullName,
                tvRoles = holder.tvRoles,
                tvLatestMessage = holder.tvLatestMessage,
                tvTimestamp = holder.tvTimestamp;
        ImageView imgAlert = holder.imgAlert;

        Chat chat = chatList.get(position);
        Message message = messages.get(position);
        User user = users.get(position);
        String roles = usersRoles.get(position);

        String fullName = user.getFirstName() + " " + user.getLastName();

        tvFullName.setText(fullName);
        tvRoles.setText(roles);

        String messageValue = message.getSender().equals(uid) ? "You: " + message.getValue() : message.getValue();
        tvLatestMessage.setText(messageValue);
        tvTimestamp.setText(message.getTimestamp());

        imgAlert.setVisibility(View.GONE);
        tvRoles.setTextAppearance(R.style.TVFontStyle11);
        tvLatestMessage.setTextAppearance(R.style.TVFontStyle11);
        tvTimestamp.setTextAppearance(R.style.TVFontStyle11);

        if (chat.getIsRead() != null) {
            for (Map.Entry<String, Boolean> mapIsRead : chat.getIsRead().entrySet())
                if (mapIsRead.getKey().equals(uid) && !mapIsRead.getValue()) {
                    imgAlert.setVisibility(View.VISIBLE);
                    tvRoles.setTextAppearance(R.style.TVFontStyle12);
                    tvLatestMessage.setTextAppearance(R.style.TVFontStyle3);
                    tvTimestamp.setTextAppearance(R.style.TVFontStyle12);
                    break;
                }
        }

        int top = dpToPx(0), bottom = dpToPx(0);

        boolean isFirstItem = position == 0, isLastItem = position == messages.size() - 1;

        if (isFirstItem)
            top = dpToPx(4);
        if (isLastItem)
            bottom = dpToPx(4);

        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) constraintLayout.getLayoutParams();
        layoutParams.topMargin = top;
        layoutParams.bottomMargin = bottom;
        constraintLayout.setLayoutParams(layoutParams);

        backgroundLayout.setOnClickListener(view -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("endPointUid", user.getId());
            intent.putExtra("chatId", chat.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout constraintLayout, backgroundLayout;
        TextView tvFullName, tvRoles, tvLatestMessage, tvTimestamp;
        ImageView imgAlert;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            constraintLayout = itemView.findViewById(R.id.constraintLayout);
            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);
            tvFullName = itemView.findViewById(R.id.tvFullName);
            tvRoles = itemView.findViewById(R.id.tvRoles);
            tvLatestMessage = itemView.findViewById(R.id.tvLatestMessage);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            imgAlert = itemView.findViewById(R.id.imgAlert);

            setIsRecyclable(false);
        }
    }

    private int dpToPx(int dp) {
        float px = dp * context.getResources().getDisplayMetrics().density;
        return (int) px;
    }
}
