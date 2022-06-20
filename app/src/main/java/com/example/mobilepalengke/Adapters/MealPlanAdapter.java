package com.example.mobilepalengke.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.mobilepalengke.DataClasses.MealPlan;
import com.example.mobilepalengke.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class MealPlanAdapter extends RecyclerView.Adapter<MealPlanAdapter.ViewHolder> {

    LayoutInflater layoutInflater;

    Context context;

    List<MealPlan> mealPlans;

    public MealPlanAdapter(Context context, List<MealPlan> mealPlans) {
        this.mealPlans = mealPlans;
        this.layoutInflater = LayoutInflater.from(context);

        this.context = context;
    }

    @NonNull
    @Override
    public MealPlanAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.custom_meal_plan_category_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MealPlanAdapter.ViewHolder holder, int position) {
        ConstraintLayout constraintLayout = holder.constraintLayout,
                backgroundLayout = holder.backgroundLayout;
        TextView tvLabel = holder.tvLabel;
        ImageView imgMealPlan = holder.imgMealPlan;

        MealPlan mealPlan = mealPlans.get(position);

        tvLabel.setText(mealPlan.getName());

        try {
            Glide.with(context).load(mealPlan.getImg()).centerCrop().placeholder(R.drawable.ic_image_blue).
                    error(R.drawable.ic_broken_image_red).into(imgMealPlan);
        } catch (Exception ex) {}

        int start = dpToPx(0), end = dpToPx(0), top = dpToPx(0), bottom = dpToPx(0);

        boolean isFirstColumn = (position + 1) % 2 == 1, isLastColumn = (position + 1) % 2 == 0;
        int lastRowFirstItem = mealPlans.size() % 2 == 0 ? mealPlans.size() - 1 : mealPlans.size();
        boolean isFirstRow = position + 1 <= 2, isLastRow = position + 1 >= lastRowFirstItem;

        if (isFirstColumn) start = dpToPx(4);
        if (isLastColumn) end = dpToPx(4);
        if (isFirstRow) top = dpToPx(4);
        if (isLastRow) bottom = dpToPx(4);

        ConstraintLayout.LayoutParams layoutParams =
                (ConstraintLayout.LayoutParams) constraintLayout.getLayoutParams();
        layoutParams.setMarginStart(start);
        layoutParams.setMarginEnd(end);
        layoutParams.topMargin = top;
        layoutParams.bottomMargin = bottom;
        constraintLayout.setLayoutParams(layoutParams);

        backgroundLayout.setOnClickListener(view -> {
            if (mealPlanAdapterListener != null) mealPlanAdapterListener.onClick(mealPlan);
        });
    }

    @Override
    public int getItemCount() {
        return mealPlans.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout constraintLayout, backgroundLayout;
        TextView tvLabel;
        ImageView imgMealPlan;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            constraintLayout = itemView.findViewById(R.id.constraintLayout);
            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);
            tvLabel = itemView.findViewById(R.id.tvLabel);
            imgMealPlan = itemView.findViewById(R.id.imgMealPlan);

            setIsRecyclable(false);
        }
    }

    private int dpToPx(int dp) {
        float px = dp * context.getResources().getDisplayMetrics().density;
        return (int) px;
    }

    MealPlanAdapterListener mealPlanAdapterListener;

    public interface MealPlanAdapterListener {
        void onClick(MealPlan mealPlan);
    }

    public void setMealPlanAdapterListener(MealPlanAdapterListener mealPlanAdapterListener) {
        this.mealPlanAdapterListener = mealPlanAdapterListener;
    }
}
