package com.example.mobilepalengke.DataClasses;

import java.util.Map;

public class Product {

    private String id, name, img;
    private Map<String, String> categories, descriptions;
    private boolean deactivated;
    private double price;

    public Product() {
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
