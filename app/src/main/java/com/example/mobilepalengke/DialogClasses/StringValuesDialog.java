package com.example.mobilepalengke.DialogClasses;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.example.mobilepalengke.Adapters.StringValueListAdapter;
import com.example.mobilepalengke.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class StringValuesDialog {

    private TextView textView, tvValueCaption;

    private final Context context;
    private final Activity activity;
    private Dialog dialog;

    LoadingDialog loadingDialog;
    MessageDialog messageDialog;
    StringValueDialog stringValueDialog;
    ConfirmationDialog confirmationDialog;

    private final Map<String, String> mapValues = new HashMap<>();

    List<String> values = new ArrayList<>();

    StringValueListAdapter stringValueListAdapter;

    int selectedPosition;

    public StringValuesDialog(Context context) {
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
        dialog.setContentView(R.layout.dialog_string_values_layout);
        dialog.setCanceledOnTouchOutside(false);

        loadingDialog = new LoadingDialog(context);
        messageDialog = new MessageDialog(context);
        stringValueDialog = new StringValueDialog(context);
        confirmationDialog = new ConfirmationDialog(context);

        RecyclerView recyclerView = dialog.findViewById(R.id.recyclerView);
        textView = dialog.findViewById(R.id.textView);
        tvValueCaption = dialog.findViewById(R.id.tvValueCaption);
        Button btnAddValue = dialog.findViewById(R.id.btnAddValue);
        Button btnConfirm = dialog.findViewById(R.id.btnConfirm);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        stringValueListAdapter = new StringValueListAdapter(context, values);
        stringValueListAdapter.setOrderedListAdapterListener(new StringValueListAdapter.OrderedListAdapterListener() {
            @Override
            public void onClick(String value, int position) {
                selectedPosition = position;
                stringValueDialog.showDialog(textView.getText().toString());
                stringValueDialog.setData(value);
            }

            @Override
            public void onRemove(String value, int position) {
                selectedPosition = position;
                confirmationDialog.showDialog();
                confirmationDialog.setTextCaption("Do you want to remove the selected value?");
            }
        });
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(stringValueListAdapter);

        stringValueDialog.setDialogListener(value -> {
            if (selectedPosition < 0) values.add(value);
            else {
                List<String> valuesTemp = new ArrayList<>(values);
                values.clear();

                for (int i = 0; i < valuesTemp.size(); i++) {
                    if (i == selectedPosition) values.add(value);
                    else values.add(valuesTemp.get(i));
                }
            }

            updateList();
        });

        confirmationDialog.setDialogListener(() -> {
            loadingDialog.showDialog();

            List<String> valuesTemp = new ArrayList<>(values);
            values.clear();

            for (int i = 0; i < valuesTemp.size(); i++)
                if (i != selectedPosition) values.add(valuesTemp.get(i));

            updateList();

            loadingDialog.dismissDialog();
            confirmationDialog.dismissDialog();
        });

        btnAddValue.setOnClickListener(view -> {
            selectedPosition = -1;
            stringValueDialog.showDialog(textView.getText().toString());
        });

        btnConfirm.setOnClickListener(view -> {
            mapValues.clear();
            for (String value : values) {
                String keyName;

                StringBuilder idBuilder = new StringBuilder();

                for (int i = 0; i < 7 - String.valueOf(mapValues.size() + 1).length(); i++)
                    idBuilder.append("0");
                idBuilder.append(mapValues.size() + 1);

                keyName = String.valueOf(idBuilder);

                mapValues.put(keyName, value);
            }

            if (dialogListener != null)
                dialogListener.onConfirm(mapValues);
        });

        btnCancel.setOnClickListener(view -> dismissDialog());
    }

    private void setDialogWindow() {
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(activity, R.drawable.bg_corner));
    }

    public void showDialog(String title) {
        dialog.show();

        textView.setText(title);
    }

    public void dismissDialog() {
        dialog.dismiss();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(Map<String, String> mapValues) {
        values.clear();
        mapValues.forEach((s, s2) -> values.add(s2));

        updateList();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateList() {
        values.sort(String::compareToIgnoreCase);

        if (values.size() == 0)
            tvValueCaption.setVisibility(View.VISIBLE);
        else tvValueCaption.setVisibility(View.GONE);
        tvValueCaption.bringToFront();

        stringValueListAdapter.notifyDataSetChanged();
    }

    DialogListener dialogListener;

    public interface DialogListener {
        void onConfirm(Map<String, String> mapValues);
    }

    public void setDialogListener(DialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }
}
