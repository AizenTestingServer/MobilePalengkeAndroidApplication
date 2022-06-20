package com.example.mobilepalengke.DialogClasses;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.mobilepalengke.Adapters.AdminProductAdapter;
import com.example.mobilepalengke.Adapters.IconOptionAdapter;
import com.example.mobilepalengke.DataClasses.IconOption;
import com.example.mobilepalengke.DataClasses.Product;
import com.example.mobilepalengke.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AddProductToMealPlanDialog {

    ConstraintLayout productsLayout, productCategoriesLayout;
    EditText etSearchProduct;
    TextView tvSelectedCategory, btnChangeCategory, tvProductCaption;
    Button btnCancel, btnBack;
    RecyclerView recyclerView, recyclerView2;

    private final Context context;
    private final Activity activity;
    private Dialog dialog;

    LoadingDialog loadingDialog;
    MessageDialog messageDialog;

    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;

    boolean isListening = true;

    int currentStep = 0, maxStep = 1;

    String searchValue;

    Query productsQuery, productCategoriesQuery;

    List<Product> products = new ArrayList<>(), productsCopy = new ArrayList<>();
    List<String> productsCategories = new ArrayList<>(), productsCategoriesCopy = new ArrayList<>();

    AdminProductAdapter adminProductAdapter;

    IconOptionAdapter iconOptionAdapter;

    List<IconOption> productCategories = new ArrayList<>();
    List<String> productCategoriesId = new ArrayList<>();

    int selectedCategoryIndex = 0, overallProductCount = 0;
    String selectedCategoryId;

    public AddProductToMealPlanDialog(Context context) {
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
        dialog.setContentView(R.layout.dialog_add_product_to_meal_plan_layout);
        dialog.setCanceledOnTouchOutside(false);

        productsLayout = dialog.findViewById(R.id.productsLayout);
        productCategoriesLayout = dialog.findViewById(R.id.productCategoriesLayout);

        etSearchProduct = dialog.findViewById(R.id.etSearchProduct);
        tvSelectedCategory = dialog.findViewById(R.id.tvSelectedCategory);
        btnChangeCategory = dialog.findViewById(R.id.btnChangeCategory);
        recyclerView = dialog.findViewById(R.id.recyclerView);
        btnCancel = dialog.findViewById(R.id.btnCancel);
        tvProductCaption = dialog.findViewById(R.id.tvProductCaption);

        recyclerView2 = dialog.findViewById(R.id.recyclerView2);
        btnBack = dialog.findViewById(R.id.btnBack);

        loadingDialog = new LoadingDialog(context);
        messageDialog = new MessageDialog(context);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        firebaseDatabase = FirebaseDatabase.getInstance(context.getString(R.string.firebase_RTDB_url));
        productsQuery = firebaseDatabase.getReference("products").orderByChild("name");
        productCategoriesQuery = firebaseDatabase.getReference("productCategories").orderByChild("name");

        loadingDialog.showDialog();
        isListening = true;
        productsQuery.addValueEventListener(getProdValueListener());

        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false);
        adminProductAdapter = new AdminProductAdapter(context, products);
        adminProductAdapter.setProductAdapterListener(product -> {
            if (dialogListener != null)
                dialogListener.onItemClick(product);
        });
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adminProductAdapter);

        iconOptionAdapter = new IconOptionAdapter(context, productCategories);
        iconOptionAdapter.setIconOptionAdapterListener(new IconOptionAdapter.IconOptionAdapterListener() {
            @Override
            public void onClick(IconOption iconOption) {

            }

            @Override
            public void onClick(IconOption iconOption, int position) {
                currentStep--;

                tvSelectedCategory.setText(iconOption.getLabelName());

                selectedCategoryIndex = position;
                selectedCategoryId = productCategoriesId.get(position);

                productCategoriesLayout.setVisibility(View.GONE);
                productsLayout.setVisibility(View.VISIBLE);

                filterProducts();
            }
        });
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL,
                false);
        recyclerView2.setLayoutManager(linearLayoutManager2);
        recyclerView2.setAdapter(iconOptionAdapter);

        etSearchProduct.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                searchValue = editable.toString();

                filterProducts();
            }
        });

        btnChangeCategory.setOnClickListener(view1 -> {
            if (currentStep < maxStep)
                currentStep++;

            productsLayout.setVisibility(View.GONE);
            productCategoriesLayout.setVisibility(View.VISIBLE);
        });

        btnBack.setOnClickListener(view1 -> {
            currentStep--;

            productCategoriesLayout.setVisibility(View.GONE);
            productsLayout.setVisibility(View.VISIBLE);
        });

        btnCancel.setOnClickListener(view -> dismissDialog());
    }

    private void setDialogWindow() {
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(activity, R.drawable.bg_corner));
    }

    public void showDialog() {
        dialog.show();
    }

    public void dismissDialog() {
        dialog.dismiss();
    }

    private ValueEventListener getProdValueListener() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    overallProductCount = 0;
                    products.clear();

                    if (snapshot.exists()) {
                        overallProductCount = (int) snapshot.getChildrenCount();

                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Product product = dataSnapshot.getValue(Product.class);
                            if (product != null && !product.isDeactivated())
                                products.add(product);
                        }
                    }

                    productCategoriesQuery.addValueEventListener(getProdCatValueListener());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "productsQuery:onCancelled", error.toException());
                loadingDialog.dismissDialog();

                messageDialog.setTextCaption("Failed to get the products.");
                messageDialog.setTextType(2);
                messageDialog.showDialog();
            }
        };
    }

    private ValueEventListener getProdCatValueListener() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    productsCategories.clear();
                    productCategories.clear();
                    productCategoriesId.clear();

                    if (snapshot.exists()) {
                        productCategories.add(new IconOption(context.getString(R.string.all), 0));
                        productCategoriesId.add("prodCat00");

                        for (Product product : products) {
                            List<String> categoryIds = product.getCategories() != null ?
                                    new ArrayList<>(product.getCategories().values()) :
                                    new ArrayList<>();
                            List<String> categories = new ArrayList<>();

                            for (String categoryId : categoryIds)
                                categories.add(snapshot.child(categoryId.trim())
                                        .child("name").getValue(String.class));

                            productsCategories.add(TextUtils.join(", ", categories));
                        }

                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            productCategories.add(new IconOption(dataSnapshot.child("name").getValue(String.class), 0));
                            productCategoriesId.add(dataSnapshot.getKey());

                            if (selectedCategoryId != null && selectedCategoryId.equals(dataSnapshot.getKey())) {
                                selectedCategoryIndex = productCategoriesId.size() - 1;
                                tvSelectedCategory.setText(productCategories.get(selectedCategoryIndex).getLabelName());
                            }
                        }
                    }

                    productsCopy.clear();
                    productsCategoriesCopy.clear();

                    productsCopy.addAll(products);
                    productsCategoriesCopy.addAll(productsCategories);

                    filterProducts();

                    iconOptionAdapter.notifyDataSetChanged();

                    loadingDialog.dismissDialog();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "productCategoriesQuery:onCancelled", error.toException());
                loadingDialog.dismissDialog();

                messageDialog.setTextCaption("Failed to get the product categories.");
                messageDialog.setTextType(2);
                messageDialog.showDialog();
            }
        };
    }

    @SuppressLint("NotifyDataSetChanged")
    private void filterProducts() {
        List<Product> productsTemp = new ArrayList<>(productsCopy);
        List<String> productsCategoriesTemp = new ArrayList<>(productsCategoriesCopy);

        products.clear();
        productsCategories.clear();

        for (int i = 0; i < productsTemp.size(); i++) {
            List<String> categoriesId = productsTemp.get(i).getCategories() != null ?
                    new ArrayList<>(productsTemp.get(i).getCategories().values()) :
                    new ArrayList<>();

            boolean isSelectedCategory = selectedCategoryId == null || selectedCategoryIndex == 0 ||
                    categoriesId.contains(selectedCategoryId);

            boolean isSearchedValue = searchValue == null || searchValue.trim().length() == 0 ||
                    productsTemp.get(i).getName().toLowerCase().contains(searchValue.trim().toLowerCase()) ||
                    productsCategoriesTemp.get(i).toLowerCase().contains(searchValue.trim().toLowerCase());

            if (isSelectedCategory && isSearchedValue) {
                products.add(productsTemp.get(i));
                productsCategories.add(productsCategoriesTemp.get(i));
            }
        }

        if (products.size() == 0)
            tvProductCaption.setVisibility(View.VISIBLE);
        else tvProductCaption.setVisibility(View.GONE);
        tvProductCaption.bringToFront();

        adminProductAdapter.notifyDataSetChanged();
    }

    public void onBackPressed() {
        if (currentStep > 0)
            currentStep--;

        if (currentStep == 0) {
            productCategoriesLayout.setVisibility(View.GONE);
            productsLayout.setVisibility(View.VISIBLE);
        }
    }

    DialogListener dialogListener;

    public interface DialogListener {
        void onItemClick(Product product);
    }

    public void setDialogListener(DialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }
}
