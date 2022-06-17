package com.example.mobilepalengke.DataClasses;

import java.util.Map;

public class Order {

    private String id, ownerId, status, timestamp;
    private Address address;
    private Map<String, CheckOutProduct> products;

    public Order() {
    }

    public Order(String id, String ownerId, String status, String timestamp, Address address,
            Map<String, CheckOutProduct> products) {
        this.id = id;
        this.ownerId = ownerId;
        this.status = status;
        this.timestamp = timestamp;
        this.address = address;
        this.products = products;
    }

    public String getId() {
        return id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getStatus() {
        return status;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public Address getAddress() {
        return address;
    }

    public Map<String, CheckOutProduct> getProducts() {
        return products;
    }
}
