package com.example.mobilepalengke.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mobilepalengke.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class StringValueListAdapter extends RecyclerView.Adapter<StringValueListAdapter.ViewHolder> {

    LayoutInflater layoutInflater;

    Context context;

    List<String> list;

    public StringValueListAdapter(Context context, List<String> list) {
        this.list = list;
        this.layoutInflater = LayoutInflater.from(context);

        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.custom_string_value_list_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ConstraintLayout constraintLayout = holder.constraintLayout;
        TextView tvIconLabel = holder.tvIconLabel;
        ImageView imgRemove = holder.imgRemove;

        String value = list.get(position);

        tvIconLabel.setText(value);

        int top = dpToPx(0), bottom = dpToPx(0);

        boolean isFirstItem = position == 0, isLastItem = position == list.size() - 1;

        if (isFirstItem)
            top = dpToPx(4);
        if (isLastItem)
            bottom = dpToPx(4);

        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) constraintLayout.getLayoutParams();
        layoutParams.topMargin = top;
        layoutParams.bottomMargin = bottom;
        constraintLayout.setLayoutParams(layoutParams);

        tvIconLabel.setOnClickListener(view -> {
            if (orderedListAdapterListener != null)
                orderedListAdapterListener.onClick(value, holder.getAdapterPosition());
        });

        imgRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (orderedListAdapterListener != null)
                    orderedListAdapterListener.onRemove(value, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout constraintLayout, backgroundLayout;
        TextView tvIconLabel;
        ImageView imgRemove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            constraintLayout = itemView.findViewById(R.id.constraintLayout);
            tvIconLabel = itemView.findViewById(R.id.tvIconLabel);
            imgRemove = itemView.findViewById(R.id.imgRemove);

            setIsRecyclable(false);
        }
    }

    private int dpToPx(int dp) {
        float px = dp * context.getResources().getDisplayMetrics().density;
        return (int) px;
    }

    OrderedListAdapterListener orderedListAdapterListener;

    public interface OrderedListAdapterListener {
        void onClick(String value, int position);
        void onRemove(String value, int position);
    }

    public void setOrderedListAdapterListener(OrderedListAdapterListener orderedListAdapterListener) {
        this.orderedListAdapterListener = orderedListAdapterListener;
    }
}
