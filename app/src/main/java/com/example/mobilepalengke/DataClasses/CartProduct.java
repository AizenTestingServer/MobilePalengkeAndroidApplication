package com.example.mobilepalengke.DataClasses;

public class CartProduct {

    private String id;
    private int quantity;

    public CartProduct() {
    }

    public CartProduct(String id, int quantity) {
        this.id = id;
        this.quantity = quantity;
    }

    public String getId() {
        return id;
    }

    public int getQuantity() {
        return quantity;
    }
}
