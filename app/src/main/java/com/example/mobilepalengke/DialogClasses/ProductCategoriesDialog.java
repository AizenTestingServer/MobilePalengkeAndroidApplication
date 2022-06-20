package com.example.mobilepalengke.DialogClasses;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import com.example.mobilepalengke.Adapters.CheckableAdapter;
import com.example.mobilepalengke.DataClasses.CheckableItem;
import com.example.mobilepalengke.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ProductCategoriesDialog {

    private Button btnConfirm;

    private final Context context;
    private final Activity activity;
    private Dialog dialog;

    private final List<CheckableItem> checkableItems = new ArrayList<>();
    private final List<CheckableItem> selectedCheckableItems = new ArrayList<>();

    private CheckableAdapter checkableAdapter;

    private final Map<String, String> mapSelectedItems = new HashMap<>();

    public ProductCategoriesDialog(Context context) {
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
        dialog.setContentView(R.layout.dialog_product_categories_layout);
        dialog.setCanceledOnTouchOutside(false);

        RecyclerView recyclerView = dialog.findViewById(R.id.recyclerView);
        btnConfirm = dialog.findViewById(R.id.btnConfirm);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        checkableAdapter = new CheckableAdapter(context, checkableItems);
        checkableAdapter.setCheckableAdapterListener(new CheckableAdapter.CheckableAdapterListener() {
            @Override
            public void onClick() {
                selectedCheckableItems.clear();
                selectedCheckableItems.addAll(checkableAdapter.getSelectedCheckableItems());

                mapSelectedItems.clear();
                for (CheckableItem selectedCheckableItem : selectedCheckableItems) {
                    String prodCatIndex = "prodCat" + ((String.valueOf(mapSelectedItems.size() + 1).length() < 2)
                            ? "0" + (mapSelectedItems.size() + 1)
                            : (int) (mapSelectedItems.size() + 1));

                    mapSelectedItems.put(prodCatIndex, selectedCheckableItem.getId());
                }

                btnConfirm.setVisibility(View.VISIBLE);
            }

            @Override
            public void onClick(CheckableItem checkableItem) {

            }

            @Override
            public void onClick(CheckableItem checkableItem, int position) {

            }
        });
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(checkableAdapter);

        btnConfirm.setOnClickListener(view -> {
            if (dialogListener != null)
                dialogListener.onConfirm(mapSelectedItems, selectedCheckableItems);
        });

        btnCancel.setOnClickListener(view -> dismissDialog());
    }

    private void setDialogWindow() {
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(activity, R.drawable.bg_corner));
    }

    public void showDialog() {
        dialog.show();

        btnConfirm.setVisibility(View.GONE);
    }

    public void dismissDialog() {
        dialog.dismiss();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setCheckableItems(List<CheckableItem> checkableItems, List<CheckableItem> selectedCheckableItems) {
        this.checkableItems.clear();
        this.checkableItems.addAll(checkableItems);

        checkableAdapter.setSelectedCheckableItems(selectedCheckableItems);
        checkableAdapter.notifyDataSetChanged();
    }

    DialogListener dialogListener;

    public interface DialogListener {
        void onConfirm(Map<String, String> mapSelectedItems, List<CheckableItem> selectedCheckableItems);
    }

    public void setDialogListener(DialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }
}
