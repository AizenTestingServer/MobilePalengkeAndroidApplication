package com.example.mobilepalengke.Adapters;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mobilepalengke.DataClasses.Message;
import com.example.mobilepalengke.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import static android.content.Context.CLIPBOARD_SERVICE;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.ViewHolder> {

    LayoutInflater layoutInflater;

    Context context;

    List<Message> messages;

    String uid;

    int selectedPosition = -1;

    public ChatMessageAdapter(Context context, List<Message> messages, String uid) {
        this.messages = messages;
        this.uid = uid;
        this.layoutInflater = LayoutInflater.from(context);

        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.custom_chat_message_layout, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull ChatMessageAdapter.ViewHolder holder, int position) {
        ConstraintLayout backgroundLayout = holder.backgroundLayout,
                startLayout = holder.startLayout,
                endLayout = holder.endLayout,
                constraintLayout = holder.constraintLayout,
                constraintLayout2 = holder.constraintLayout2;
        TextView tvStartMessage = holder.tvStartMessage,
                tvEndMessage = holder.tvEndMessage,
                tvStartTimestamp = holder.tvStartTimestamp,
                tvEndTimestamp = holder.tvEndTimestamp;

        tvStartTimestamp.setVisibility(View.GONE);
        tvEndTimestamp.setVisibility(View.GONE);

        Message message = messages.get(position);

        if (message.getSender().equals(uid)) {
            endLayout.setVisibility(View.GONE);
            startLayout.setVisibility(View.VISIBLE);

            tvStartMessage.setText(message.getValue());
            tvStartTimestamp.setText(message.getTimestamp());

            if (holder.getAdapterPosition() == selectedPosition)
                tvStartTimestamp.setVisibility(View.VISIBLE);
        } else {
            startLayout.setVisibility(View.GONE);
            endLayout.setVisibility(View.VISIBLE);

            tvEndMessage.setText(message.getValue());
            tvEndTimestamp.setText(message.getTimestamp());

            if (holder.getAdapterPosition() == selectedPosition)
                tvEndTimestamp.setVisibility(View.VISIBLE);
        }

        int top = dpToPx(0), bottom = dpToPx(0);

        boolean isFirstItem = position == 0, isLastItem = position == messages.size() - 1;

        if (isFirstItem)
            bottom = dpToPx(8);
        if (isLastItem)
            top = dpToPx(8);

        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) backgroundLayout.getLayoutParams();
        layoutParams.topMargin = top;
        layoutParams.bottomMargin = bottom;
        backgroundLayout.setLayoutParams(layoutParams);

        constraintLayout.setOnClickListener(view -> {
            if (tvStartTimestamp.getVisibility() == View.GONE) {
                tvStartTimestamp.setVisibility(View.VISIBLE);
                selectedPosition = holder.getAdapterPosition();
            } else {
                tvStartTimestamp.setVisibility(View.GONE);
                selectedPosition = -1;
            }

            notifyDataSetChanged();
        });

        constraintLayout2.setOnClickListener(view -> {
            if (tvEndTimestamp.getVisibility() == View.GONE) {
                tvEndTimestamp.setVisibility(View.VISIBLE);
                selectedPosition = holder.getAdapterPosition();
            } else {
                tvEndTimestamp.setVisibility(View.GONE);
                selectedPosition = -1;
            }

            notifyDataSetChanged();
        });

        constraintLayout.setOnLongClickListener(view -> {
            copyMessageToClipboard(message.getValue());
            return false;
        });

        constraintLayout2.setOnLongClickListener(view -> {
            copyMessageToClipboard(message.getValue());
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout backgroundLayout, startLayout, endLayout, constraintLayout, constraintLayout2;
        TextView tvStartMessage, tvEndMessage, tvStartTimestamp, tvEndTimestamp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);
            startLayout = itemView.findViewById(R.id.startLayout);
            endLayout = itemView.findViewById(R.id.endLayout);
            constraintLayout = itemView.findViewById(R.id.constraintLayout);
            constraintLayout2 = itemView.findViewById(R.id.constraintLayout2);

            tvStartMessage = itemView.findViewById(R.id.tvStartMessage);
            tvEndMessage = itemView.findViewById(R.id.tvEndMessage);
            tvStartTimestamp = itemView.findViewById(R.id.tvStartTimestamp);
            tvEndTimestamp = itemView.findViewById(R.id.tvEndTimestamp);

            setIsRecyclable(false);
        }
    }

    private void copyMessageToClipboard(String value) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("chatMessage", value);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(
                context,
                "The message was copied to clipboard",
                Toast.LENGTH_SHORT).show();
    }

    private int dpToPx(int dp) {
        float px = dp * context.getResources().getDisplayMetrics().density;
        return (int) px;
    }
}
