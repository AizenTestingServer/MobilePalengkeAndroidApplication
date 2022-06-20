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

    int level;

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
        ConstraintLayout constraintLayout = holder.constraintLayout,
                backgroundLayout = holder.backgroundLayout;
        TextView tvRole = holder.tvRole,
                tvDescription = holder.tvDescription;

        RoleItem roleItem = roles.get(position);

        String description = "Level " + roleItem.getLevel() + " • " + roleItem.getType() +
                (roleItem.isFixed() ? " • Fixed" : "");

        tvRole.setText(roleItem.getName());
        tvDescription.setText(description);

        tvRole.setTextAppearance(R.style.FlatButtonStyle4);
        tvDescription.setTextAppearance(R.style.TVFontStyle11);

        if (level <= roleItem.getLevel() || roleItem.isFixed()) {
            tvRole.setTextAppearance(R.style.FlatButtonStyle5);
            tvDescription.setTextAppearance(R.style.TVFontStyle5);
        }

        int top = dpToPx(0), bottom = dpToPx(0);

        boolean isFirstItem = position == 0, isLastItem = position == roles.size() - 1;

        if (isFirstItem)
            top = dpToPx(4);
        if (isLastItem)
            bottom = dpToPx(4);

        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) constraintLayout.getLayoutParams();
        layoutParams.topMargin = top;
        layoutParams.bottomMargin = bottom;
        constraintLayout.setLayoutParams(layoutParams);

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
        ConstraintLayout constraintLayout, backgroundLayout;
        TextView tvRole, tvDescription;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            constraintLayout = itemView.findViewById(R.id.constraintLayout);
            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvRole = itemView.findViewById(R.id.tvRole);

            setIsRecyclable(false);
        }
    }

    private int dpToPx(int dp) {
        float px = dp * context.getResources().getDisplayMetrics().density;
        return (int) px;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    RoleAdapterListener roleAdapterListener;

    public interface RoleAdapterListener {
        void onClick(RoleItem roleItem);
    }

    public void setRoleAdapterListener(RoleAdapterListener roleAdapterListener) {
        this.roleAdapterListener = roleAdapterListener;
    }
}
