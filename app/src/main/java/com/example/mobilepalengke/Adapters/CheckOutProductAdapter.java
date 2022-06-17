package com.example.mobilepalengke.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mobilepalengke.DataClasses.CheckOutProduct;
import com.example.mobilepalengke.DataClasses.Product;
import com.example.mobilepalengke.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class CheckOutProductAdapter extends RecyclerView.Adapter<CheckOutProductAdapter.ViewHolder> {

    LayoutInflater layoutInflater;

    Context context;

    List<CheckOutProduct> checkOutProducts;
    List<Product> products;

    public CheckOutProductAdapter(Context context, List<CheckOutProduct> checkOutProducts,
                                  List<Product> products) {
        this.checkOutProducts = checkOutProducts;
        this.products = products;
        this.layoutInflater = LayoutInflater.from(context);

        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.custom_check_out_product_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ConstraintLayout backgroundLayout = holder.backgroundLayout;
        ImageView imgProduct = holder.imgProduct;
        TextView tvLabel = holder.tvLabel,
                tvQty = holder.tvQty;

        CheckOutProduct checkOutProduct = checkOutProducts.get(position);
        Product product = products.get(position);

        for (Product product1 : products) {
            if (product1.getId().equals(checkOutProduct.getId())) {
                product = product1;
                break;
            }
        }

        tvLabel.setText(product.getName());
        tvQty.setText(context.getString(R.string.ctrQtyValue, checkOutProduct.getQuantity()));
        Picasso.get().load(product.getImg()).placeholder(R.drawable.ic_image_blue)
                .error(R.drawable.ic_broken_image_red).into(imgProduct);

        int start = dpToPx(4), end = dpToPx(4);

        boolean isFirstItem = position == 0, isLastItem = position == checkOutProducts.size() - 1;

        if (isFirstItem)
            start = dpToPx(8);
        if (isLastItem)
            end = dpToPx(8);

        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) backgroundLayout.getLayoutParams();
        layoutParams.setMarginStart(start);
        layoutParams.setMarginEnd(end);
        backgroundLayout.setLayoutParams(layoutParams);
    }

    @Override
    public int getItemCount() {
        return checkOutProducts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout backgroundLayout;
        ImageView imgProduct;
        TextView tvLabel, tvQty;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvLabel = itemView.findViewById(R.id.tvLabel);
            tvQty = itemView.findViewById(R.id.tvQty);

            setIsRecyclable(false);
        }
    }

    private int dpToPx(int dp) {
        float px = dp * context.getResources().getDisplayMetrics().density;
        return (int) px;
    }
}
