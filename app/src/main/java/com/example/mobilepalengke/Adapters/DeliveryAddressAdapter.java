package com.example.mobilepalengke.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mobilepalengke.DataClasses.Address;
import com.example.mobilepalengke.DialogClasses.LoadingDialog;
import com.example.mobilepalengke.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class DeliveryAddressAdapter extends RecyclerView.Adapter<DeliveryAddressAdapter.ViewHolder> {

    List<Address> addressList;

    LayoutInflater layoutInflater;

    Context context;

    LoadingDialog loadingDialog;

    int selectedPosition = -1;

    public DeliveryAddressAdapter(Context context, List<Address> addressList) {
        this.addressList = addressList;
        this.layoutInflater = LayoutInflater.from(context);

        this.context = context;

        loadingDialog = new LoadingDialog(context);
    }

    @NonNull
    @Override
    public DeliveryAddressAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.custom_address_layout, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull DeliveryAddressAdapter.ViewHolder holder, int position) {
        ConstraintLayout constraintLayout = holder.constraintLayout,
                backgroundLayout = holder.backgroundLayout;
        TextView tvLabel = holder.tvLabel,
                tvAddress = holder.tvAddress;

        Address address = addressList.get(position);

        tvLabel.setText(address.getName());
        tvAddress.setText(address.getValue());

        tvLabel.setTextAppearance(R.style.TVFontStyle1);
        tvAddress.setTextAppearance(R.style.TVFontStyle11);

        backgroundLayout.setBackgroundColor(context.getColor(R.color.white));

        if (holder.getAdapterPosition() == selectedPosition) {
            backgroundLayout.setBackgroundColor(context.getColor(R.color.mp_blue));
            tvLabel.setTextAppearance(R.style.TVFontStyle13);
            tvAddress.setTextAppearance(R.style.TVFontStyle13);
        }

        int top = dpToPx(0), bottom = dpToPx(0);

        boolean isFirstItem = position == 0, isLastItem = position == addressList.size() - 1;

        if (isFirstItem)
            top = dpToPx(4);
        if (isLastItem)
            bottom = dpToPx(4);

        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) constraintLayout.getLayoutParams();
        layoutParams.topMargin = top;
        layoutParams.bottomMargin = bottom;
        constraintLayout.setLayoutParams(layoutParams);

        backgroundLayout.setOnClickListener(view -> {
            if (addressAdapterListener != null)
                addressAdapterListener.onClick(address);

            selectedPosition = holder.getAdapterPosition();
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return addressList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout constraintLayout, backgroundLayout;
        TextView tvLabel, tvAddress;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            constraintLayout = itemView.findViewById(R.id.constraintLayout);
            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvLabel = itemView.findViewById(R.id.tvLabel);

            setIsRecyclable(false);
        }
    }

    private int dpToPx(int dp) {
        float px = dp * context.getResources().getDisplayMetrics().density;
        return (int) px;
    }

    AddressAdapterListener addressAdapterListener;

    public interface AddressAdapterListener {
        void onClick(Address address);
    }

    public void setAddressAdapterListener(AddressAdapterListener addressAdapterListener) {
        this.addressAdapterListener = addressAdapterListener;
    }
}
