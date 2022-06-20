package com.example.mobilepalengke.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.mobilepalengke.Activities.ProductDetailsActivity;
import com.example.mobilepalengke.DataClasses.Product;
import com.example.mobilepalengke.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    LayoutInflater layoutInflater;

    Context context;

    List<Product> products;

    int quantity;

    public ProductAdapter(Context context, List<Product> products) {
        this.products = products;
        this.layoutInflater = LayoutInflater.from(context);

        this.context = context;
    }

    @NonNull
    @Override
    public ProductAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.custom_product_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductAdapter.ViewHolder holder, int position) {
        ConstraintLayout constraintLayout = holder.constraintLayout,
                backgroundLayout = holder.backgroundLayout;
        ImageView imgProduct = holder.imgProduct;
        ImageButton btnSubtractQty = holder.btnSubtractQty,
                btnAddQty = holder.btnAddQty;
        Button btnAddToCart = holder.btnAddToCart;
        TextView tvLabel = holder.tvLabel,
                tvPrice = holder.tvPrice,
                tvQty = holder.tvQty;

        Product product = products.get(position);

        quantity = Integer.parseInt(tvQty.getText().toString());
        btnSubtractQty.setEnabled(quantity > 1);

        tvLabel.setText(product.getName());
        tvPrice.setText(context.getString(R.string.priceValue, product.getPrice()));

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

        btnSubtractQty.setOnClickListener(view -> {
            quantity = Integer.parseInt(tvQty.getText().toString());

            if (quantity != 1) {
                quantity -= 1;
                tvQty.setText(context.getString(R.string.qtyValue, quantity));
            }

            btnSubtractQty.setEnabled(quantity > 1);
        });

        btnAddQty.setOnClickListener(view -> {
            quantity = Integer.parseInt(tvQty.getText().toString());

            quantity += 1;
            tvQty.setText(context.getString(R.string.qtyValue, quantity));

            btnSubtractQty.setEnabled(quantity > 1);
        });

        btnAddToCart.setOnClickListener(view -> {
            quantity = Integer.parseInt(tvQty.getText().toString());

            if (productAdapterListener != null)
                productAdapterListener.addToCart(product, quantity);

            quantity = 1;
            tvQty.setText(context.getString(R.string.qtyValue, quantity));

            btnSubtractQty.setEnabled(quantity > 1);
        });

        backgroundLayout.setOnClickListener(view -> {
            Intent intent = new Intent(context, ProductDetailsActivity.class);
            intent.putExtra("productId", product.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout constraintLayout, backgroundLayout;
        ImageView imgProduct;
        ImageButton btnSubtractQty, btnAddQty;
        Button btnAddToCart;
        TextView tvLabel, tvPrice, tvQty;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            constraintLayout = itemView.findViewById(R.id.constraintLayout);
            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            btnSubtractQty = itemView.findViewById(R.id.btnSubtractQty);
            btnAddQty = itemView.findViewById(R.id.btnAddQty);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
            tvLabel = itemView.findViewById(R.id.tvLabel);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQty = itemView.findViewById(R.id.tvQty);

            setIsRecyclable(false);
        }
    }

    private int dpToPx(int dp) {
        float px = dp * context.getResources().getDisplayMetrics().density;
        return (int) px;
    }

    ProductAdapterListener productAdapterListener;

    public interface ProductAdapterListener {
        void addToCart(Product product, int quantity);
    }

    public void setProductAdapterListener(ProductAdapterListener productAdapterListener) {
        this.productAdapterListener = productAdapterListener;
    }
}
