package com.example.mobilepalengke.Adapters;

import com.example.mobilepalengke.Fragments.CartFragment;
import com.example.mobilepalengke.Fragments.OrdersFragment;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class CartFragmentAdapter extends FragmentStateAdapter {

    public CartFragment cartFragment;
    public OrdersFragment ordersFragment;

    public CartFragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle,
                               CartFragment cartFragment, OrdersFragment ordersFragment, String productId) {
        super(fragmentManager, lifecycle);

        this.cartFragment = cartFragment;
        this.ordersFragment = ordersFragment;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 1)
            return ordersFragment;
        return cartFragment;
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
