package com.example.mobilepalengke.Adapters;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mobilepalengke.Activities.AdminOrderDetailsActivity;
import com.example.mobilepalengke.DataClasses.Address;
import com.example.mobilepalengke.DataClasses.CheckOutProduct;
import com.example.mobilepalengke.DataClasses.Order;
import com.example.mobilepalengke.DataClasses.Product;
import com.example.mobilepalengke.DataClasses.User;
import com.example.mobilepalengke.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.ViewHolder> {

    List<Order> orders;
    List<Product> products;
    List<User> users;

    LayoutInflater layoutInflater;

    Context context;

    public AdminOrderAdapter(Context context, List<Order> orders, List<Product> products,
                             List<User> users) {
        this.orders = orders;
        this.products = products;
        this.users = users;
        this.layoutInflater = LayoutInflater.from(context);

        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.custom_admin_order_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ConstraintLayout constraintLayout = holder.constraintLayout,
                backgroundLayout = holder.backgroundLayout;
        RecyclerView recyclerView = holder.recyclerView;
        TextView tvFullName = holder.tvFullName,
                tvMobileNumbers = holder.tvMobileNumbers,
                tvTimestamp = holder.tvTimestamp,
                tvTotalPrice = holder.tvTotalPrice,
                tvPaymentMethod = holder.tvPaymentMethod,
                tvStatus = holder.tvStatus,
                tvLabel = holder.tvLabel,
                tvAddress = holder.tvAddress,
                tvTotalQty = holder.tvTotalQty;

        Order order = orders.get(position);
        User user = users.get(0);

        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId().equals(order.getOwnerId())) {
                user = users.get(i);
                break;
            }
        }

        String fullName = user.getFirstName() + " " + user.getLastName();

        List<String> mobileNumbers = order.getMobileNumbers() != null ?
                new ArrayList<>(order.getMobileNumbers().values()) : new ArrayList<>();

        String mobileNumber = TextUtils.join(", ", mobileNumbers).length() != 0 ?
                TextUtils.join(", ", mobileNumbers) : "No Mobile Number";

        tvFullName.setText(fullName);
        tvMobileNumbers.setText(mobileNumber);

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
        OrderProductAdapter orderProductAdapter = new OrderProductAdapter(context, checkOutProducts, products, true);
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
            Intent intent = new Intent(context, AdminOrderDetailsActivity.class);
            intent.putExtra("orderId", order.getId());
            intent.putExtra("ownerId", order.getOwnerId());
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
        TextView tvFullName, tvMobileNumbers, tvTimestamp, tvTotalPrice, tvPaymentMethod,
                tvStatus, tvLabel, tvAddress, tvTotalQty;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            constraintLayout = itemView.findViewById(R.id.constraintLayout);
            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);
            tvFullName = itemView.findViewById(R.id.tvFullName);
            tvMobileNumbers = itemView.findViewById(R.id.tvMobileNumbers);
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
