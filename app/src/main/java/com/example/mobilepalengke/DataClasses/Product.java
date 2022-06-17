package com.example.mobilepalengke.DataClasses;

import java.util.Map;

public class Product {

    private String id, name, img;
    private Map<String, String> categories, descriptions;
    private boolean deactivated;
    private double price;

    public Product() {
    }

    public Product(String id, String name, String img, Map<String, String> categories, Map<String, String> descriptions, double price) {
        this.id = id;
        this.name = name;
        this.img = img;
        this.categories = categories;
        this.descriptions = descriptions;
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getImg() {
        return img;
    }

    public Map<String, String> getCategories() {
        return categories;
    }

    public Map<String, String> getDescriptions() {
        return descriptions;
    }

    public boolean isDeactivated() {
        return deactivated;
    }

    public double getPrice() {
        return price;
    }
}
