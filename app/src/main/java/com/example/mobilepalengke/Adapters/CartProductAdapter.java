package com.example.mobilepalengke.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mobilepalengke.Activities.ProductDetailsActivity;
import com.example.mobilepalengke.DataClasses.CartProduct;
import com.example.mobilepalengke.DataClasses.CheckOutProduct;
import com.example.mobilepalengke.DataClasses.Product;
import com.example.mobilepalengke.DialogClasses.LoadingDialog;
import com.example.mobilepalengke.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class CartProductAdapter extends RecyclerView.Adapter<CartProductAdapter.ViewHolder> {

    List<CartProduct> cartProducts;
    List<Product> products;
    String productId;

    LayoutInflater layoutInflater;

    Context context;

    LoadingDialog loadingDialog;

    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;

    int quantity;
    String uid;

    List<CheckOutProduct> checkOutProducts = new ArrayList<>();

    boolean isAllSelected = false;

    public CartProductAdapter(Context context, List<CartProduct> cartProducts,
            List<Product> products, String productId) {
        this.cartProducts = cartProducts;
        this.products = products;
        this.productId = productId;
        this.layoutInflater = LayoutInflater.from(context);

        this.context = context;

        loadingDialog = new LoadingDialog(context);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null)
            uid = firebaseUser.getUid();

        firebaseDatabase = FirebaseDatabase.getInstance(context.getString(R.string.firebase_RTDB_url));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.custom_cart_product_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ConstraintLayout constraintLayout = holder.constraintLayout;
        TextView tvProductName = holder.tvProductName,
                tvPrice = holder.tvPrice,
                tvQty = holder.tvQty;
        ImageView imgProduct = holder.imgProduct;
        ImageButton btnSubtractQty = holder.btnSubtractQty,
                btnAddQty = holder.btnAddQty;
        Button btnRemove = holder.btnRemove;
        CheckBox checkBox = holder.checkBox;

        CartProduct cartProduct = cartProducts.get(position);
        Product product = products.get(position);

        quantity = cartProduct.getQuantity();
        btnSubtractQty.setEnabled(quantity > 1);

        try {
            Glide.with(context).load(product.getImg()).centerCrop().placeholder(R.drawable.ic_image_blue).
                    error(R.drawable.ic_broken_image_red).into(imgProduct);
        } catch (Exception ex) {}

        tvProductName.setText(product.getName());
        tvPrice.setText(context.getString(R.string.priceValue, product.getPrice()));
        tvQty.setText(context.getString(R.string.qtyValue, quantity));

        int top = dpToPx(0), bottom = dpToPx(0);

        boolean isFirstItem = position + 1 == 1, isLastItem = position + 1 == cartProducts.size();

        if (isFirstItem)
            top = dpToPx(4);
        if (isLastItem)
            bottom = dpToPx(4);

        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) constraintLayout.getLayoutParams();
        layoutParams.topMargin = top;
        layoutParams.bottomMargin = bottom;
        constraintLayout.setLayoutParams(layoutParams);

        if (adapterListener != null)
            adapterListener.updateCheckOutInfo(checkOutProducts);

        imgProduct.setOnClickListener(view -> {
            if (product.getId().equals(productId) && adapterListener != null)
                adapterListener.onBackPressed();
            else {
                Intent intent = new Intent(context, ProductDetailsActivity.class);
                intent.putExtra("productId", product.getId());
                intent.putExtra("isFromCart", true);
                context.startActivity(intent);
            }
        });

        tvProductName.setOnClickListener(view -> checkBox.performClick());

        btnSubtractQty.setOnClickListener(view -> {
            quantity = Integer.parseInt(tvQty.getText().toString());

            if (quantity != 1) {
                quantity -= 1;
                tvQty.setText(context.getString(R.string.qtyValue, quantity));
            }

            btnSubtractQty.setEnabled(quantity > 1);

            if (checkBox.isChecked()) {
                removeProductFromCheckOut(product.getId());
                addProductToCheckOut(new CheckOutProduct(product.getId(), quantity, quantity * product.getPrice()));
                if (adapterListener != null)
                    adapterListener.updateCheckOutInfo(checkOutProducts);
            }
        });

        btnAddQty.setOnClickListener(view -> {
            quantity = Integer.parseInt(tvQty.getText().toString());

            quantity += 1;
            tvQty.setText(context.getString(R.string.qtyValue, quantity));

            btnSubtractQty.setEnabled(quantity > 1);

            if (checkBox.isChecked()) {
                removeProductFromCheckOut(product.getId());
                addProductToCheckOut(new CheckOutProduct(product.getId(), quantity, quantity * product.getPrice()));
                if (adapterListener != null)
                    adapterListener.updateCheckOutInfo(checkOutProducts);
            } else checkBox.setChecked(true);
        });

        btnRemove.setOnClickListener(view -> removeFromCart(cartProduct.getId()));

        checkBox.setOnCheckedChangeListener((compoundButton, b) -> {
            quantity = Integer.parseInt(tvQty.getText().toString());

            if (b)
                addProductToCheckOut(new CheckOutProduct(product.getId(), quantity, quantity * product.getPrice()));
            else
                removeProductFromCheckOut(product.getId());

            adapterListener.updateCheckOutInfo(checkOutProducts);
        });

        checkBox.setChecked(isAllSelected);
    }

    @Override
    public int getItemCount() {
        return cartProducts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout constraintLayout, backgroundLayout;
        TextView tvProductName, tvPrice, tvQty;
        ImageView imgProduct;
        ImageButton btnSubtractQty, btnAddQty;
        Button btnRemove;
        CheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            
            constraintLayout = itemView.findViewById(R.id.constraintLayout);
            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQty = itemView.findViewById(R.id.tvQty);
            btnSubtractQty = itemView.findViewById(R.id.btnSubtractQty);
            btnAddQty = itemView.findViewById(R.id.btnAddQty);
            btnRemove = itemView.findViewById(R.id.btnRemove);
            checkBox = itemView.findViewById(R.id.checkBox);
        }
    }

    private void removeFromCart(String productId) {
        loadingDialog.showDialog();

        DatabaseReference cartRef = firebaseDatabase.getReference("cartList")
                .child(uid).child("cartProducts").child(productId);

        cartRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(
                        context,
                        "Successfully removed from cart.",
                        Toast.LENGTH_LONG).show();
                removeProductFromCheckOut(productId);
            } else
                Toast.makeText(
                        context,
                        "Failed to remove the product from cart.",
                        Toast.LENGTH_LONG).show();

            loadingDialog.dismissDialog();
        });
    }

    private void addProductToCheckOut(CheckOutProduct checkOutProduct) {
        checkOutProducts.add(checkOutProduct);
    }

    private void removeProductFromCheckOut(String productId) {
        checkOutProducts.removeIf(checkOutProduct -> checkOutProduct.getId().equals(productId));
    }

    private int dpToPx(int dp) {
        float px = dp * context.getResources().getDisplayMetrics().density;
        return (int) px;
    }

    public List<CheckOutProduct> getCheckOutProducts() {
        return checkOutProducts;
    }

    public void setCheckOutProducts(List<CheckOutProduct> checkOutProducts) {
        this.checkOutProducts = checkOutProducts;
    }

    AdapterListener adapterListener;

    public interface AdapterListener {
        void updateCheckOutInfo(List<CheckOutProduct> checkOutProducts);

        void onBackPressed();
    }

    public void setAdapterListener(AdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }
}
