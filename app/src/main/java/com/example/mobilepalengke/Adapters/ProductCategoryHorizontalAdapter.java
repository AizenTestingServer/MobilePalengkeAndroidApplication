package com.example.mobilepalengke.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.mobilepalengke.Activities.ProductsActivity;
import com.example.mobilepalengke.DataClasses.ProductCategory;
import com.example.mobilepalengke.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class ProductCategoryHorizontalAdapter
        extends RecyclerView.Adapter<ProductCategoryHorizontalAdapter.ViewHolder> {

    LayoutInflater layoutInflater;

    Context context;

    List<ProductCategory> productCategories;

    public ProductCategoryHorizontalAdapter(Context context, List<ProductCategory> productCategories) {
        this.productCategories = productCategories;
        this.layoutInflater = LayoutInflater.from(context);

        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.custom_product_category_horizontal_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ConstraintLayout backgroundLayout = holder.backgroundLayout;
        Button btnCategory = holder.btnCategory;

        ProductCategory productCategory = productCategories.get(position);

        btnCategory.setText(productCategory.getName());

        int start = dpToPx(4), end = dpToPx(4);

        boolean isFirstItem = position == 0, isLastItem = position == productCategories.size() - 1;

        if (isFirstItem)
            start = dpToPx(16);
        if (isLastItem)
            end = dpToPx(16);

        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) backgroundLayout.getLayoutParams();
        layoutParams.setMarginStart(start);
        layoutParams.setMarginEnd(end);
        backgroundLayout.setLayoutParams(layoutParams);

        btnCategory.setOnClickListener(view -> {
            Intent intent = new Intent(context, ProductsActivity.class);
            intent.putExtra("selectedCategoryId", productCategory.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return productCategories.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout backgroundLayout;
        Button btnCategory;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);
            btnCategory = itemView.findViewById(R.id.btnCategory);

            setIsRecyclable(false);
        }
    }

    private int dpToPx(int dp) {
        float px = dp * context.getResources().getDisplayMetrics().density;
        return (int) px;
    }
}
