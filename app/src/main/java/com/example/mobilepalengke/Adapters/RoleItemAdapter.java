package com.example.mobilepalengke.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mobilepalengke.DataClasses.RoleItem;
import com.example.mobilepalengke.DialogClasses.LoadingDialog;
import com.example.mobilepalengke.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class RoleItemAdapter extends RecyclerView.Adapter<RoleItemAdapter.ViewHolder> {

    List<RoleItem> roles;

    LayoutInflater layoutInflater;

    Context context;

    LoadingDialog loadingDialog;

    public RoleItemAdapter(Context context, List<RoleItem> roles) {
        this.roles = roles;
        this.layoutInflater = LayoutInflater.from(context);

        this.context = context;

        loadingDialog = new LoadingDialog(context);
    }

    @NonNull
    @Override
    public RoleItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.custom_role_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoleItemAdapter.ViewHolder holder, int position) {
        ConstraintLayout backgroundLayout = holder.backgroundLayout;
        TextView tvRole = holder.tvRole,
                tvRoleType = holder.tvRoleType;

        RoleItem roleItem = roles.get(position);

        tvRole.setText(roleItem.getName());
        tvRoleType.setText(roleItem.getType());

        int top = dpToPx(4), bottom = dpToPx(4);

        boolean isFirstItem = position == 0, isLastItem = position == roles.size() - 1;

        if (isFirstItem)
            top = dpToPx(8);
        if (isLastItem)
            bottom = dpToPx(8);

        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) backgroundLayout.getLayoutParams();
        layoutParams.topMargin = top;
        layoutParams.bottomMargin = bottom;
        backgroundLayout.setLayoutParams(layoutParams);

        backgroundLayout.setOnClickListener(view -> {
            if (roleAdapterListener != null)
                roleAdapterListener.onClick(roleItem);
        });
    }

    @Override
    public int getItemCount() {
        return roles.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout backgroundLayout;
        TextView tvRole, tvRoleType;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);
            tvRoleType = itemView.findViewById(R.id.tvRoleType);
            tvRole = itemView.findViewById(R.id.tvRole);

            setIsRecyclable(false);
        }
    }

    private int dpToPx(int dp) {
        float px = dp * context.getResources().getDisplayMetrics().density;
        return (int) px;
    }

    RoleAdapterListener roleAdapterListener;

    public interface RoleAdapterListener {
        void onClick(RoleItem roleItem);
    }

    public void setRoleAdapterListener(RoleAdapterListener roleAdapterListener) {
        this.roleAdapterListener = roleAdapterListener;
    }
}
