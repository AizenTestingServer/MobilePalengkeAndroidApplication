package com.example.mobilepalengke.DataClasses;

import java.util.Map;

public class Cart {

    private String ownerId;
    private Map<String, CartProduct> cartProducts;

    public Cart() {
    }

    public Cart(String ownerId, Map<String, CartProduct> cartProducts) {
        this.ownerId = ownerId;
        this.cartProducts = cartProducts;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public Map<String, CartProduct> getCartProducts() {
        return cartProducts;
    }
}
