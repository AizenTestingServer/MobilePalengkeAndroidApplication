package com.example.mobilepalengke.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.mobilepalengke.DataClasses.Product;
import com.example.mobilepalengke.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class AdminMealPlanProductAdapter extends RecyclerView.Adapter<AdminMealPlanProductAdapter.ViewHolder> {

    LayoutInflater layoutInflater;

    Context context;

    List<Product> products;

    public AdminMealPlanProductAdapter(Context context, List<Product> products) {
        this.products = products;
        this.layoutInflater = LayoutInflater.from(context);

        this.context = context;
    }

    @NonNull
    @Override
    public AdminMealPlanProductAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.custom_admin_meal_plan_product_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminMealPlanProductAdapter.ViewHolder holder, int position) {
        ConstraintLayout constraintLayout = holder.constraintLayout,
                backgroundLayout = holder.backgroundLayout;
        ImageView imgProduct = holder.imgProduct;
        TextView tvLabel = holder.tvLabel;
        Button btnRemove = holder.btnRemove;

        Product product = products.get(position);

        tvLabel.setText(product.getName());

        try {
            Glide.with(context).load(product.getImg()).centerCrop().placeholder(R.drawable.ic_image_blue).
                    error(R.drawable.ic_broken_image_red).into(imgProduct);
        } catch (Exception ex) {}

        int start = dpToPx(0), end = dpToPx(0), top = dpToPx(0), bottom = dpToPx(0);

        boolean isFirstColumn = (position + 1) % 2 == 1, isLastColumn = (position + 1) % 2 == 0;
        int lastRowFirstItem = products.size() % 2 == 0 ? products.size() - 1 : products.size();
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

        btnRemove.setOnClickListener(view -> {
            if (mealPlanProductAdapterListener != null)
                mealPlanProductAdapterListener.removeFromMealPlan(product);
        });

        backgroundLayout.setOnClickListener(view -> {
            if (mealPlanProductAdapterListener != null)
                mealPlanProductAdapterListener.onClick(product);
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout constraintLayout, backgroundLayout;
        ImageView imgProduct;
        Button btnRemove;
        TextView tvLabel;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            constraintLayout = itemView.findViewById(R.id.constraintLayout);
            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            btnRemove = itemView.findViewById(R.id.btnRemove);
            tvLabel = itemView.findViewById(R.id.tvLabel);

            setIsRecyclable(false);
        }
    }

    private int dpToPx(int dp) {
        float px = dp * context.getResources().getDisplayMetrics().density;
        return (int) px;
    }

    AdminMealPlanProductAdapterListener mealPlanProductAdapterListener;

    public interface AdminMealPlanProductAdapterListener {
        void onClick(Product product);
        void removeFromMealPlan(Product product);
    }

    public void setMealPlanProductAdapterListener(AdminMealPlanProductAdapterListener mealPlanProductAdapterListener) {
        this.mealPlanProductAdapterListener = mealPlanProductAdapterListener;
    }
}
