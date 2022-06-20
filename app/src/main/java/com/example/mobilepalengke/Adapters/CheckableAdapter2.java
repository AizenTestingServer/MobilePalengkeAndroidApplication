package com.example.mobilepalengke.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.mobilepalengke.DataClasses.CheckableItem;
import com.example.mobilepalengke.R;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class CheckableAdapter2 extends RecyclerView.Adapter<CheckableAdapter2.ViewHolder> {

    List<CheckableItem> checkableItems, selectedCheckableItems = new ArrayList<>();

    LayoutInflater layoutInflater;

    Context context;

    int level;

    boolean isCheckListening = true;

    public CheckableAdapter2(Context context, List<CheckableItem> checkableItems) {
        this.checkableItems = checkableItems;
        this.layoutInflater = LayoutInflater.from(context);

        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.custom_checkable_list_layout_2, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ConstraintLayout constraintLayout = holder.constraintLayout;
        CheckBox cbLabel = holder.cbLabel;
        TextView tvDescription = holder.tvDescription;

        CheckableItem checkableItem = checkableItems.get(position);

        cbLabel.setText(checkableItem.getLabelName());
        tvDescription.setText(checkableItem.getDescription());

        int level = Integer.parseInt(checkableItem.getDescription().split(" ")[1]);

        cbLabel.setEnabled(this.level > level);

        cbLabel.setTextAppearance(R.style.FlatButtonStyle4);
        tvDescription.setTextAppearance(R.style.TVFontStyle11);

        if (!cbLabel.isEnabled()) {
            cbLabel.setTextAppearance(R.style.FlatButtonStyle5);
            tvDescription.setTextAppearance(R.style.TVFontStyle5);
        }

        int top = dpToPx(0), bottom = dpToPx(0);

        boolean isFirstItem = position == 0, isLastItem = position == checkableItems.size() - 1;

        if (isFirstItem)
            top = dpToPx(2);
        if (isLastItem)
            bottom = dpToPx(2);

        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) constraintLayout.getLayoutParams();
        layoutParams.topMargin = top;
        layoutParams.bottomMargin = bottom;
        constraintLayout.setLayoutParams(layoutParams);

        cbLabel.setOnCheckedChangeListener((compoundButton, b) -> {
            if (isCheckListening) {
                if (b)
                    addSelectedCheckableItem(new CheckableItem(checkableItem.getId(), checkableItem.getLabelName()));
                else
                    removeSelectedCheckableItem(checkableItem.getId());

                if (checkableAdapterListener != null) {
                    checkableAdapterListener.onClick();
                    checkableAdapterListener.onClick(checkableItem);
                    checkableAdapterListener.onClick(checkableItem, position);
                }
            }
        });

        isCheckListening = false;
        cbLabel.setChecked(false);
        for (CheckableItem selectedCheckableItem : selectedCheckableItems)
            if (selectedCheckableItem.getId().equals(checkableItem.getId())) {
                cbLabel.setChecked(true);
                break;
            }
        isCheckListening = true;
    }

    @Override
    public int getItemCount() {
        return checkableItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout constraintLayout, backgroundLayout;
        CheckBox cbLabel;
        TextView tvDescription;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            constraintLayout = itemView.findViewById(R.id.constraintLayout);
            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);
            cbLabel = itemView.findViewById(R.id.cbLabel);
            tvDescription = itemView.findViewById(R.id.tvDescription);
        }
    }

    private void addSelectedCheckableItem(CheckableItem checkableItem) {
        selectedCheckableItems.add(checkableItem);
    }

    private void removeSelectedCheckableItem(String id) {
        selectedCheckableItems.removeIf(selectedCheckableItem -> selectedCheckableItem.getId().equals(id));
    }

    private int dpToPx(int dp) {
        float px = dp * context.getResources().getDisplayMetrics().density;
        return (int) px;
    }

    public List<CheckableItem> getSelectedCheckableItems() {
        return selectedCheckableItems;
    }

    public void setSelectedCheckableItems(List<CheckableItem> selectedCheckableItems) {
        this.selectedCheckableItems.clear();
        this.selectedCheckableItems.addAll(selectedCheckableItems);
    }

    public void setLevel(int level) {
        this.level = level;
    }

    CheckableAdapterListener checkableAdapterListener;

    public interface CheckableAdapterListener {
        void onClick();
        void onClick(CheckableItem checkableItem);
        void onClick(CheckableItem checkableItem, int position);
    }

    public void setCheckableAdapterListener(CheckableAdapterListener checkableAdapterListener) {
        this.checkableAdapterListener = checkableAdapterListener;
    }
}
