package com.example.mobilepalengke.DataClasses;

import java.util.Map;

public class MealPlan {

    private String id, img, instructions, name;
    private Map<String, String> categories, products;
    private boolean deactivated;

    public MealPlan() {
    }

    public MealPlan(String id, String img, String instructions, String name, Map<String, String> categories,
            Map<String, String> products) {
        this.id = id;
        this.img = img;
        this.instructions = instructions;
        this.name = name;
        this.categories = categories;
        this.products = products;
    }

    public String getId() {
        return id;
    }

    public String getImg() {
        return img;
    }

    public String getInstructions() {
        return instructions;
    }

    public String getName() {
        return name;
    }

    public Map<String, String> getCategories() {
        return categories;
    }

    public Map<String, String> getProducts() {
        return products;
    }

    public boolean isDeactivated() {
        return deactivated;
    }
}
