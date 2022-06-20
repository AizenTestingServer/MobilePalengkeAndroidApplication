package com.example.mobilepalengke.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mobilepalengke.DataClasses.IconOption;
import com.example.mobilepalengke.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class IconOptionAdapter extends RecyclerView.Adapter<IconOptionAdapter.ViewHolder> {

    LayoutInflater layoutInflater;

    Context context;

    List<IconOption> iconOptions;

    public IconOptionAdapter(Context context, List<IconOption> iconOptions) {
        this.iconOptions = iconOptions;
        this.layoutInflater = LayoutInflater.from(context);

        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.custom_listed_icon_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ConstraintLayout constraintLayout = holder.constraintLayout;
        TextView tvIconLabel = holder.tvIconLabel;

        IconOption iconOption = iconOptions.get(position);

        tvIconLabel.setText(iconOption.getLabelName());
        if (iconOption.getIcon() != 0)
            tvIconLabel.setCompoundDrawablesWithIntrinsicBounds(
                    AppCompatResources.getDrawable(context, iconOption.getIcon()),
                    null, null, null);
        else
            tvIconLabel.setCompoundDrawablesWithIntrinsicBounds(
                    null, null, null, null);

        int top = dpToPx(0), bottom = dpToPx(0);

        boolean isFirstItem = position == 0, isLastItem = position == iconOptions.size() - 1;

        if (isFirstItem)
            top = dpToPx(4);
        if (isLastItem)
            bottom = dpToPx(4);

        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) constraintLayout.getLayoutParams();
        layoutParams.topMargin = top;
        layoutParams.bottomMargin = bottom;
        constraintLayout.setLayoutParams(layoutParams);

        tvIconLabel.setOnClickListener(view -> {
            if (iconOptionAdapterListener != null) {
                iconOptionAdapterListener.onClick(iconOption);
                iconOptionAdapterListener.onClick(iconOption, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return iconOptions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout constraintLayout, backgroundLayout;
        TextView tvIconLabel;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            constraintLayout = itemView.findViewById(R.id.constraintLayout);
            tvIconLabel = itemView.findViewById(R.id.tvIconLabel);

            setIsRecyclable(false);
        }
    }

    private int dpToPx(int dp) {
        float px = dp * context.getResources().getDisplayMetrics().density;
        return (int) px;
    }

    IconOptionAdapterListener iconOptionAdapterListener;

    public interface IconOptionAdapterListener {
        void onClick(IconOption iconOption);
        void onClick(IconOption iconOption, int position);
    }

    public void setIconOptionAdapterListener(IconOptionAdapterListener iconOptionAdapterListener) {
        this.iconOptionAdapterListener = iconOptionAdapterListener;
    }
}
