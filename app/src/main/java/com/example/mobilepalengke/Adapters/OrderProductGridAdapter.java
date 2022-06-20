package com.example.mobilepalengke.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.mobilepalengke.DataClasses.CheckOutProduct;
import com.example.mobilepalengke.DataClasses.Product;
import com.example.mobilepalengke.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class OrderProductGridAdapter extends RecyclerView.Adapter<OrderProductGridAdapter.ViewHolder> {

    List<CheckOutProduct> checkOutProducts;
    List<Product> products;

    LayoutInflater layoutInflater;

    Context context;

    public OrderProductGridAdapter(Context context, List<CheckOutProduct> checkOutProducts,
                                   List<Product> products) {
        this.checkOutProducts = checkOutProducts;
        this.products = products;
        this.layoutInflater = LayoutInflater.from(context);

        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.custom_order_product_grid_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ConstraintLayout constraintLayout = holder.constraintLayout,
                backgroundLayout = holder.backgroundLayout;
        ImageView imgProduct = holder.imgProduct;
        TextView tvLabel = holder.tvLabel,
                tvQty = holder.tvQty,
                tvPrice = holder.tvPrice;

        CheckOutProduct checkOutProduct = checkOutProducts.get(position);
        Product product = products.get(0);

        for (Product product1 : products) {
            if (product1.getId().equals(checkOutProduct.getId())) {
                product = product1;
                break;
            }
        }

        tvLabel.setText(product.getName());
        tvQty.setText(context.getString(R.string.ctrQtyValue, checkOutProduct.getQuantity()));
        tvPrice.setText(context.getString(R.string.priceValue, checkOutProduct.getTotalPrice()));

        try {
            Glide.with(context).load(product.getImg()).centerCrop().placeholder(R.drawable.ic_image_blue).
                    error(R.drawable.ic_broken_image_red).into(imgProduct);
        } catch (Exception ex) {}

        int start = dpToPx(0), end = dpToPx(0);

        boolean isFirstItem = position == 0, isLastItem = position == checkOutProducts.size() - 1;

        if (isFirstItem)
            start = dpToPx(4);
        if (isLastItem)
            end = dpToPx(4);

        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) constraintLayout.getLayoutParams();
        layoutParams.setMarginStart(start);
        layoutParams.setMarginEnd(end);
        constraintLayout.setLayoutParams(layoutParams);
    }

    @Override
    public int getItemCount() {
        return checkOutProducts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout constraintLayout, backgroundLayout;
        ImageView imgProduct;
        TextView tvLabel, tvQty, tvPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            constraintLayout = itemView.findViewById(R.id.constraintLayout);
            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvLabel = itemView.findViewById(R.id.tvLabel);
            tvQty = itemView.findViewById(R.id.tvQty);
            tvPrice = itemView.findViewById(R.id.tvPrice);

            setIsRecyclable(false);
        }
    }

    private int dpToPx(int dp) {
        float px = dp * context.getResources().getDisplayMetrics().density;
        return (int) px;
    }
}
