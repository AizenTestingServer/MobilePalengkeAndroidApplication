package com.example.mobilepalengke.DataClasses;

public class CheckOutProduct {

    private String id;
    private int quantity;
    private double totalPrice;

    public CheckOutProduct() {
    }

    public CheckOutProduct(String id, int quantity, double totalPrice) {
        this.id = id;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

}
