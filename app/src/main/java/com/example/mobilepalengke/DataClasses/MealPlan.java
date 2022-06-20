package com.example.mobilepalengke.DataClasses;

import java.util.Map;

public class MealPlan {

    private String id, img, name, description;
    private Map<String, String> categories, ingredients, instructions, products;
    private boolean deactivated;
    private int prepTime, cookTime, servings;

    public MealPlan() {
    }

    public MealPlan(String id, String img, String name, String description,
                    Map<String, String> categories, Map<String, String> ingredients,
                    Map<String, String> instructions, Map<String, String> products,
                    boolean deactivated, int prepTime, int cookTime, int servings) {
        this.id = id;
        this.img = img;
        this.name = name;
        this.description = description;
        this.categories = categories;
        this.ingredients = ingredients;
        this.instructions = instructions;
        this.products = products;
        this.deactivated = deactivated;
        this.prepTime = prepTime;
        this.cookTime = cookTime;
        this.servings = servings;
    }

    public String getId() {
        return id;
    }

    public String getImg() {
        return img;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, String> getCategories() {
        return categories;
    }

    public Map<String, String> getIngredients() {
        return ingredients;
    }

    public Map<String, String> getInstructions() {
        return instructions;
    }

    public Map<String, String> getProducts() {
        return products;
    }

    public boolean isDeactivated() {
        return deactivated;
    }

    public int getPrepTime() {
        return prepTime;
    }

    public int getCookTime() {
        return cookTime;
    }

    public int getServings() {
        return servings;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategories(Map<String, String> categories) {
        this.categories = categories;
    }

    public void setIngredients(Map<String, String> ingredients) {
        this.ingredients = ingredients;
    }

    public void setInstructions(Map<String, String> instructions) {
        this.instructions = instructions;
    }

    public void setProducts(Map<String, String> products) {
        this.products = products;
    }

    public void setDeactivated(boolean deactivated) {
        this.deactivated = deactivated;
    }

    public void setPrepTime(int prepTime) {
        this.prepTime = prepTime;
    }

    public void setCookTime(int cookTime) {
        this.cookTime = cookTime;
    }

    public void setServings(int servings) {
        this.servings = servings;
    }
}
