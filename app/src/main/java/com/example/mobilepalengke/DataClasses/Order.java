package com.example.mobilepalengke.DataClasses;

import java.util.Map;

public class Order {

    private String id, ownerId, paymentMethod, status, timestamp;
    private Map<String, String> address, mobileNumbers;
    private Map<String, CheckOutProduct> products;

    public Order() {
    }

    public Order(String id, String ownerId, String paymentMethod, String status, String timestamp,
                 Map<String, String> address, Map<String, String> mobileNumbers,
                 Map<String, CheckOutProduct> products) {
        this.id = id;
        this.ownerId = ownerId;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.timestamp = timestamp;
        this.address = address;
        this.mobileNumbers = mobileNumbers;
        this.products = products;
    }

    public String getId() {
        return id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getStatus() {
        return status;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public Map<String, String> getAddress() {
        return address;
    }

    public Map<String, String> getMobileNumbers() {
        return mobileNumbers;
    }

    public Map<String, CheckOutProduct> getProducts() {
        return products;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
