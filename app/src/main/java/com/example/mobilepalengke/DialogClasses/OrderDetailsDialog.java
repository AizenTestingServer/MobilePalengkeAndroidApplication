package com.example.mobilepalengke.DialogClasses;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mobilepalengke.Adapters.OrderProductAdapter;
import com.example.mobilepalengke.DataClasses.Address;
import com.example.mobilepalengke.DataClasses.CheckOutProduct;
import com.example.mobilepalengke.DataClasses.Product;
import com.example.mobilepalengke.R;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class OrderDetailsDialog {

    private RecyclerView recyclerView;
    private TextView tvTotalPrice, tvTotalQty, tvLabel, tvAddress, tvEmail2,
            tvMobileNumber1, tvMobileNumber2, tvPaymentMethod2;
    private ImageView imgScrollRight;

    private final Context context;
    private final Activity activity;
    private Dialog dialog;

    private final List<CheckOutProduct> checkOutProducts = new ArrayList<>();
    private final List<Product> products = new ArrayList<>();

    OrderProductAdapter orderProductAdapter;

    public OrderDetailsDialog(Context context) {
        this.context = context;
        activity = (Activity) context;

        createDialog();
    }

    private void createDialog() {
        setDialog();
        setDialogWindow();
    }

    private void setDialog() {
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_order_details);
        dialog.setCanceledOnTouchOutside(false);

        ImageView imgClose = dialog.findViewById(R.id.imgClose);
        recyclerView = dialog.findViewById(R.id.recyclerView);
        tvTotalPrice = dialog.findViewById(R.id.tvTotalPrice);
        tvTotalQty = dialog.findViewById(R.id.tvTotalQty);
        imgScrollRight =dialog.findViewById(R.id.imgScrollRight);
        tvLabel = dialog.findViewById(R.id.tvLabel);
        tvAddress = dialog.findViewById(R.id.tvAddress);
        tvEmail2 = dialog.findViewById(R.id.tvEmail2);
        tvMobileNumber1 = dialog.findViewById(R.id.tvMobileNumber1);
        tvMobileNumber2 = dialog.findViewById(R.id.tvMobileNumber2);
        tvPaymentMethod2 = dialog.findViewById(R.id.tvPaymentMethod2);
        Button btnSubmit = dialog.findViewById(R.id.btnSubmit);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,
                false);
        orderProductAdapter = new OrderProductAdapter(context, checkOutProducts, products, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(orderProductAdapter);

        imgClose.setOnClickListener(view -> dismissDialog());

        imgScrollRight.setOnClickListener(view -> {
            recyclerView.smoothScrollToPosition(checkOutProducts.size() - 1);
            imgScrollRight.setVisibility(View.GONE);
        });

        int currentXScroll = 0;
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (checkOutProducts.size() > 2) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (dx > currentXScroll) imgScrollRight.setVisibility(View.GONE);
                    else imgScrollRight.setVisibility(View.VISIBLE);
                }
            }
        });

        btnSubmit.setOnClickListener(view -> {
            if (dialogListener != null) dialogListener.onSubmit();
        });

        btnCancel.setOnClickListener(view -> dismissDialog());
    }

    private void setDialogWindow() {
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(activity, R.drawable.bg_corner));
    }

    @SuppressLint("NotifyDataSetChanged")
    public void showDialog(List<CheckOutProduct> checkOutProducts,
                           List<Product> products, Address deliveryAddress, String emailAddress,
                           String mobileNumber, String mobileNumber2, int selectedPaymentMethod) {
        dialog.show();

        this.checkOutProducts.clear();
        this.checkOutProducts.addAll(checkOutProducts);

        this.products.clear();
        this.products.addAll(products);

        double totalPrice = 0;
        int totalQuantity = 0;

        for (CheckOutProduct checkOutProduct : checkOutProducts) {
            totalPrice += checkOutProduct.getTotalPrice();
            totalQuantity += checkOutProduct.getQuantity();
        }

        tvTotalPrice.setText(context.getString(R.string.priceValue, totalPrice));
        tvTotalQty.setText(context.getString(R.string.totalQuantityValue, totalQuantity, totalQuantity <= 1 ? "" : "s"));

        imgScrollRight.setVisibility(View.GONE);
        if (checkOutProducts.size() > 2) imgScrollRight.setVisibility(View.VISIBLE);
        imgScrollRight.bringToFront();

        String paymentMethod;

        switch (selectedPaymentMethod) {
            case 1:
                paymentMethod = "G-Cash";
                break;
            case 2:
                paymentMethod = "Paypal";
                break;
            default:
                paymentMethod = "Cash on Delivery";
                break;
        }

        tvLabel.setText(deliveryAddress.getName());
        tvAddress.setText(deliveryAddress.getValue());
        tvEmail2 .setText(emailAddress);

        tvMobileNumber1.setVisibility(View.VISIBLE);
        tvMobileNumber2.setVisibility(View.VISIBLE);

        if (mobileNumber != null && mobileNumber.length() != 0)
            tvMobileNumber1.setText(mobileNumber);
        else tvMobileNumber1.setVisibility(View.GONE);

        if (mobileNumber2 != null && mobileNumber2.length() != 0)
            tvMobileNumber2.setText(mobileNumber2);
        else tvMobileNumber2.setVisibility(View.GONE);

        tvPaymentMethod2.setText(paymentMethod);

        orderProductAdapter.notifyDataSetChanged();
    }

    public void dismissDialog() {
        dialog.dismiss();
    }

    DialogListener dialogListener;

    public interface DialogListener {
        void onSubmit();
    }

    public void setDialogListener(DialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }
}
