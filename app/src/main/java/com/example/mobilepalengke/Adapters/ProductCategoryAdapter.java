package com.example.mobilepalengke.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.mobilepalengke.Activities.ProductsActivity;
import com.example.mobilepalengke.DataClasses.ProductCategory;
import com.example.mobilepalengke.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class ProductCategoryAdapter extends RecyclerView.Adapter<ProductCategoryAdapter.ViewHolder> {

    LayoutInflater layoutInflater;

    Context context;

    List<ProductCategory> productCategories;

    public ProductCategoryAdapter(Context context, List<ProductCategory> productCategories) {
        this.productCategories = productCategories;
        this.layoutInflater = LayoutInflater.from(context);

        this.context = context;
    }

    @NonNull
    @Override
    public ProductCategoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.custom_product_category_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductCategoryAdapter.ViewHolder holder, int position) {
        ConstraintLayout backgroundLayout = holder.backgroundLayout;
        ImageView imgCategory = holder.imgCategory;

        ProductCategory productCategory = productCategories.get(position);

        Picasso.get().load(productCategory.getImg()).placeholder(R.drawable.ic_image_blue)
                .error(R.drawable.ic_broken_image_red).into(imgCategory);

        /*
         * int start = dpToPx(4), end = dpToPx(4), top = dpToPx(4), bottom = dpToPx(4);
         * 
         * boolean isFirstColumn = position % 3 == 0, isLastColumn = (position + 1) % 3
         * == 0;
         * boolean isFirstRow = position + 1 <= 3, isLastRow = position + 1 >=
         * productCategories.size();
         * 
         * if (isFirstColumn) start = dpToPx(8);
         * if (isLastColumn) end = dpToPx(8);
         * if (isFirstRow) top = dpToPx(8);
         * if (isLastRow) bottom = dpToPx(8);
         * 
         * ConstraintLayout.LayoutParams layoutParams =
         * (ConstraintLayout.LayoutParams) backgroundLayout.getLayoutParams();
         * layoutParams.setMarginStart(start);
         * layoutParams.setMarginEnd(end);
         * layoutParams.topMargin = top;
         * layoutParams.bottomMargin = bottom;
         * backgroundLayout.setLayoutParams(layoutParams);
         */

        backgroundLayout.setOnClickListener(view -> {
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
        ImageView imgCategory;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);
            imgCategory = itemView.findViewById(R.id.imgCategory);

            setIsRecyclable(false);
        }
    }

    private int dpToPx(int dp) {
        float px = dp * context.getResources().getDisplayMetrics().density;
        return (int) px;
    }
}
