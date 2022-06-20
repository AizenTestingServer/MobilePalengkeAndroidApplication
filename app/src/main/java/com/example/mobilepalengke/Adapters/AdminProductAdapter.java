package com.example.mobilepalengke.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.mobilepalengke.DataClasses.Product;
import com.example.mobilepalengke.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class AdminProductAdapter extends RecyclerView.Adapter<AdminProductAdapter.ViewHolder> {

    LayoutInflater layoutInflater;

    Context context;

    List<Product> products;

    public AdminProductAdapter(Context context, List<Product> products) {
        this.products = products;
        this.layoutInflater = LayoutInflater.from(context);

        this.context = context;
    }

    @NonNull
    @Override
    public AdminProductAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.custom_admin_product_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminProductAdapter.ViewHolder holder, int position) {
        ConstraintLayout constraintLayout = holder.constraintLayout,
                backgroundLayout = holder.backgroundLayout;
        ImageView imgProduct = holder.imgProduct;
        TextView tvLabel = holder.tvLabel, tvPrice = holder.tvPrice;

        Product product = products.get(position);

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

        backgroundLayout.setOnClickListener(view -> {
            if (productAdapterListener != null) productAdapterListener.onClick(product);
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout constraintLayout, backgroundLayout;
        ImageView imgProduct;
        TextView tvLabel, tvPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            constraintLayout = itemView.findViewById(R.id.constraintLayout);
            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvLabel = itemView.findViewById(R.id.tvLabel);
            tvPrice = itemView.findViewById(R.id.tvPrice);

            setIsRecyclable(false);
        }
    }

    private int dpToPx(int dp) {
        float px = dp * context.getResources().getDisplayMetrics().density;
        return (int) px;
    }

    ProductAdapterListener productAdapterListener;

    public interface ProductAdapterListener {
        void onClick(Product product);
    }

    public void setProductAdapterListener(ProductAdapterListener productAdapterListener) {
        this.productAdapterListener = productAdapterListener;
    }
}
