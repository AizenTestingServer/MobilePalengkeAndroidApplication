package com.example.mobilepalengke.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mobilepalengke.Activities.ChatActivity;
import com.example.mobilepalengke.DataClasses.User;
import com.example.mobilepalengke.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class ChatUserAdapter extends RecyclerView.Adapter<ChatUserAdapter.ViewHolder> {

    LayoutInflater layoutInflater;

    Context context;

    List<User> users;
    List<String> usersRoles;

    public ChatUserAdapter(Context context, List<User> users, List<String> usersRoles) {
        this.users = users;
        this.usersRoles = usersRoles;
        this.layoutInflater = LayoutInflater.from(context);

        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.custom_chat_user_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ConstraintLayout backgroundLayout = holder.backgroundLayout;
        TextView tvFullName = holder.tvFullName,
                tvRoles = holder.tvRoles;

        User user = users.get(position);
        String roles = usersRoles.get(position);

        String fullName = user.getFirstName() + " " + user.getLastName();

        tvFullName.setText(fullName);
        tvRoles.setText(roles);

        int top = dpToPx(4), bottom = dpToPx(4);

        boolean isFirstItem = position == 0, isLastItem = position == users.size() - 1;

        if (isFirstItem)
            top = dpToPx(8);
        if (isLastItem)
            bottom = dpToPx(8);

        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) backgroundLayout.getLayoutParams();
        layoutParams.topMargin = top;
        layoutParams.bottomMargin = bottom;
        backgroundLayout.setLayoutParams(layoutParams);

        backgroundLayout.setOnClickListener(view -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("endPointUid", user.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout backgroundLayout;
        TextView tvFullName, tvRoles;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);
            tvFullName = itemView.findViewById(R.id.tvFullName);
            tvRoles = itemView.findViewById(R.id.tvRoles);

            setIsRecyclable(false);
        }
    }

    private int dpToPx(int dp) {
        float px = dp * context.getResources().getDisplayMetrics().density;
        return (int) px;
    }
}
