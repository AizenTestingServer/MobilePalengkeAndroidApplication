package com.example.mobilepalengke.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mobilepalengke.Activities.OrderDetailsActivity;
import com.example.mobilepalengke.DataClasses.Address;
import com.example.mobilepalengke.DataClasses.CheckOutProduct;
import com.example.mobilepalengke.DataClasses.Order;
import com.example.mobilepalengke.DataClasses.Product;
import com.example.mobilepalengke.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    List<Order> orders;
    List<Product> products;

    LayoutInflater layoutInflater;

    Context context;

    public OrderAdapter(Context context, List<Order> orders, List<Product> products) {
        this.orders = orders;
        this.products = products;
        this.layoutInflater = LayoutInflater.from(context);

        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.custom_order_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ConstraintLayout constraintLayout = holder.constraintLayout,
                backgroundLayout = holder.backgroundLayout;
        RecyclerView recyclerView = holder.recyclerView;
        TextView tvTimestamp = holder.tvTimestamp,
                tvTotalPrice = holder.tvTotalPrice,
                tvPaymentMethod = holder.tvPaymentMethod,
                tvStatus = holder.tvStatus,
                tvLabel = holder.tvLabel,
                tvAddress = holder.tvAddress,
                tvTotalQty = holder.tvTotalQty;

        Order order = orders.get(position);

        List<CheckOutProduct> checkOutProducts = order.getProducts() != null ?
                new ArrayList<>(order.getProducts().values()) :
                new ArrayList<>();

        double totalPrice = 0;
        int totalQuantity = 0;
        for (CheckOutProduct checkOutProduct : checkOutProducts) {
            totalPrice += checkOutProduct.getTotalPrice();
            totalQuantity += checkOutProduct.getQuantity();
        }

        String status = "Status: " + order.getStatus();

        Address address = new Address();

        if (order.getAddress() != null)
            for (Map.Entry<String, String> mapAddress : order.getAddress().entrySet()) {
                if (mapAddress.getKey().equals("name")) address.setName(mapAddress.getValue());
                if (mapAddress.getKey().equals("value")) address.setValue(mapAddress.getValue());
            }

        tvTimestamp.setText(order.getTimestamp());
        tvTotalPrice.setText(context.getString(R.string.priceValue, totalPrice));
        tvPaymentMethod.setText(order.getPaymentMethod());
        tvStatus.setText(status);
        tvLabel.setText(address.getName());
        tvAddress.setText(address.getValue());
        tvTotalQty.setText(context.getString(R.string.totalQuantityValue, totalQuantity, totalQuantity <= 1 ? "" : "s"));

        switch (order.getStatus()) {
            case "Processing":
                tvStatus.setTextColor(context.getColor(R.color.mp_blue));
                tvStatus.setBackground(AppCompatResources.getDrawable(context, R.drawable.bg_yellow_corner));
                break;
            case "Shipping":
                tvStatus.setTextColor(context.getColor(R.color.white));
                tvStatus.setBackground(AppCompatResources.getDrawable(context, R.drawable.bg_green_corner));
                break;
            case "Delivered":
                tvStatus.setTextColor(context.getColor(R.color.mp_yellow));
                tvStatus.setBackground(AppCompatResources.getDrawable(context, R.drawable.bg_blue_corner));
                break;
            case "Cancelled":
                tvStatus.setTextColor(context.getColor(R.color.white));
                tvStatus.setBackground(AppCompatResources.getDrawable(context, R.drawable.bg_red_corner));
                break;
        }

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,
                false);
        OrderProductAdapter orderProductAdapter = new OrderProductAdapter(context, checkOutProducts, products, false);
        orderProductAdapter.setOrder(order);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(orderProductAdapter);

        int top = dpToPx(0), bottom = dpToPx(0);

        boolean isFirstItem = position == 0, isLastItem = position == orders.size() - 1;

        if (isFirstItem)
            top = dpToPx(4);
        if (isLastItem)
            bottom = dpToPx(4);

        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) constraintLayout.getLayoutParams();
        layoutParams.topMargin = top;
        layoutParams.bottomMargin = bottom;
        constraintLayout.setLayoutParams(layoutParams);

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
        ConstraintLayout constraintLayout, backgroundLayout;
        RecyclerView recyclerView;
        TextView tvTimestamp, tvTotalPrice, tvPaymentMethod, tvStatus, tvLabel, tvAddress, tvTotalQty;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            constraintLayout = itemView.findViewById(R.id.constraintLayout);
            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);
            recyclerView = itemView.findViewById(R.id.recyclerView);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
            tvPaymentMethod = itemView.findViewById(R.id.tvPaymentMethod);
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
