package com.example.mobilepalengke.Adapters;

import android.os.Bundle;

import com.example.mobilepalengke.Fragments.CartFragment;
import com.example.mobilepalengke.Fragments.CheckOutFragment;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class CartFragmentAdapter extends FragmentStateAdapter {

    public CartFragment cartFragment;
    public CheckOutFragment checkOutFragment;

    public CartFragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle,
            CartFragment cartFragment, CheckOutFragment checkOutFragment, String productId) {
        super(fragmentManager, lifecycle);

        this.cartFragment = cartFragment;
        this.checkOutFragment = checkOutFragment;

        Bundle bundle = new Bundle();
        bundle.putString("productId", productId);
        cartFragment.setArguments(bundle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 1)
            return checkOutFragment;
        return cartFragment;
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
