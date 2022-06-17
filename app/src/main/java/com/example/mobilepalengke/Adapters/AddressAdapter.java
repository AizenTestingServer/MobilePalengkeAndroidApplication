package com.example.mobilepalengke.Adapters;

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

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.ViewHolder> {

    List<Address> addressList;

    LayoutInflater layoutInflater;

    Context context;

    LoadingDialog loadingDialog;

    public AddressAdapter(Context context, List<Address> addressList) {
        this.addressList = addressList;
        this.layoutInflater = LayoutInflater.from(context);

        this.context = context;

        loadingDialog = new LoadingDialog(context);
    }

    @NonNull
    @Override
    public AddressAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.custom_address_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressAdapter.ViewHolder holder, int position) {
        ConstraintLayout backgroundLayout = holder.backgroundLayout;
        TextView tvLabel = holder.tvLabel,
                tvAddress = holder.tvAddress;

        Address address = addressList.get(position);

        tvLabel.setText(address.getName());
        tvAddress.setText(address.getValue());

        int top = dpToPx(4), bottom = dpToPx(4);

        boolean isFirstItem = position == 0, isLastItem = position == addressList.size() - 1;

        if (isFirstItem)
            top = dpToPx(8);
        if (isLastItem)
            bottom = dpToPx(8);

        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) backgroundLayout.getLayoutParams();
        layoutParams.topMargin = top;
        layoutParams.bottomMargin = bottom;
        backgroundLayout.setLayoutParams(layoutParams);

        backgroundLayout.setOnClickListener(view -> {
            if (addressAdapterListener != null)
                addressAdapterListener.onClick(address);
        });
    }

    @Override
    public int getItemCount() {
        return addressList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout backgroundLayout;
        TextView tvLabel, tvAddress;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

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
