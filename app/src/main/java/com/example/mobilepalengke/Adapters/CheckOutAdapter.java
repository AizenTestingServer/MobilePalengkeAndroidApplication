package com.example.mobilepalengke.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mobilepalengke.Activities.OrderDetailsActivity;
import com.example.mobilepalengke.DataClasses.CheckOutProduct;
import com.example.mobilepalengke.DataClasses.Order;
import com.example.mobilepalengke.DataClasses.Product;
import com.example.mobilepalengke.R;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CheckOutAdapter extends RecyclerView.Adapter<CheckOutAdapter.ViewHolder> {

    LayoutInflater layoutInflater;

    Context context;

    List<Order> orders;
    List<Product> products;

    public CheckOutAdapter(Context context, List<Order> orders, List<Product> products) {
        this.orders = orders;
        this.products = products;
        this.layoutInflater = LayoutInflater.from(context);

        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.custom_check_out_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ConstraintLayout backgroundLayout = holder.backgroundLayout;
        RecyclerView recyclerView = holder.recyclerView;
        TextView tvTimestamp = holder.tvTimestamp,
                tvTotalPrice = holder.tvTotalPrice,
                tvStatus = holder.tvStatus,
                tvLabel = holder.tvLabel,
                tvAddress = holder.tvAddress,
                tvTotalQty = holder.tvTotalQty;

        Order order = orders.get(position);

        List<CheckOutProduct> checkOutProducts = new ArrayList<>(order.getProducts().values());

        double totalPrice = 0;
        int totalQuantity = 0;
        for (CheckOutProduct checkOutProduct : checkOutProducts) {
            totalPrice += checkOutProduct.getTotalPrice();
            totalQuantity += checkOutProduct.getQuantity();
        }

        tvTimestamp.setText(order.getTimestamp());
        tvTotalPrice.setText(context.getString(R.string.priceValue, totalPrice));
        tvStatus.setText(order.getStatus());
        tvLabel.setText(order.getAddress().getName());
        tvAddress.setText(order.getAddress().getValue());
        tvTotalQty
                .setText(context.getString(R.string.totalQuantityValue, totalQuantity, totalQuantity <= 1 ? "" : "s"));

        switch (order.getStatus()) {
            case "Processing":
            case "Shipping":
                tvStatus.setTextColor(context.getColor(R.color.mp_blue));
                tvStatus.setBackgroundColor(context.getColor(R.color.mp_yellow));
                break;
            case "Delivered":
                tvStatus.setTextColor(context.getColor(R.color.mp_yellow));
                tvStatus.setBackgroundColor(context.getColor(R.color.mp_blue));
                break;
            case "Cancelled":
                tvStatus.setTextColor(context.getColor(R.color.white));
                tvStatus.setBackgroundColor(context.getColor(R.color.mp_red));
                break;
        }

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,
                false);
        CheckOutProductAdapter checkOutProductAdapter = new CheckOutProductAdapter(context, checkOutProducts, products);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(checkOutProductAdapter);

        int top = dpToPx(4), bottom = dpToPx(4);

        boolean isFirstItem = position == 0, isLastItem = position == orders.size() - 1;

        if (isFirstItem)
            top = dpToPx(8);
        if (isLastItem)
            bottom = dpToPx(8);

        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) backgroundLayout.getLayoutParams();
        layoutParams.topMargin = top;
        layoutParams.bottomMargin = bottom;
        backgroundLayout.setLayoutParams(layoutParams);

        backgroundLayout.setOnClickListener(view -> {
            Intent intent = new Intent(context, OrderDetailsActivity.class);
            intent.putExtra("orderId", order.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout backgroundLayout;
        RecyclerView recyclerView;
        TextView tvTimestamp, tvTotalPrice, tvStatus, tvLabel, tvAddress, tvTotalQty;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);
            recyclerView = itemView.findViewById(R.id.recyclerView);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvLabel = itemView.findViewById(R.id.tvLabel);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvTotalQty = itemView.findViewById(R.id.tvTotalQty);

            setIsRecyclable(false);
        }
    }

    private int dpToPx(int dp) {
        float px = dp * context.getResources().getDisplayMetrics().density;
        return (int) px;
    }
}
