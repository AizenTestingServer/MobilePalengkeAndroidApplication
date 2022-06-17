package com.example.mobilepalengke.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mobilepalengke.DataClasses.Product;
import com.example.mobilepalengke.R;
import com.squareup.picasso.Picasso;

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
        View view = layoutInflater.inflate(R.layout.custom_meal_plan_category_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminProductAdapter.ViewHolder holder, int position) {
        ConstraintLayout backgroundLayout = holder.backgroundLayout;
        ImageView imgMealPlan = holder.imgMealPlan;
        TextView tvLabel = holder.tvLabel;

        Product product = products.get(position);

        tvLabel.setText(product.getName());
        Picasso.get().load(product.getImg()).placeholder(R.drawable.ic_image_blue)
                .error(R.drawable.ic_broken_image_red).into(imgMealPlan);

        backgroundLayout.setOnClickListener(view -> {
            if (productAdapterListener != null) productAdapterListener.onClick(product);
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout backgroundLayout;
        ImageView imgMealPlan;
        TextView tvLabel;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);
            imgMealPlan = itemView.findViewById(R.id.imgMealPlan);
            tvLabel = itemView.findViewById(R.id.tvLabel);
        }
    }

    ProductAdapterListener productAdapterListener;

    public interface ProductAdapterListener {
        void onClick(Product product);
    }

    public void setProductAdapterListener(ProductAdapterListener productAdapterListener) {
        this.productAdapterListener = productAdapterListener;
    }
}
