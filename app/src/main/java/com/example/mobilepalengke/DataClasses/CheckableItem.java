package com.example.mobilepalengke.DataClasses;

public class CheckableItem {

    private String id, labelName, description;

    public CheckableItem() {
    }

    public CheckableItem(String id) {
        this.id = id;
    }

    public CheckableItem(String id, String labelName) {
        this.id = id;
        this.labelName = labelName;
    }

    public CheckableItem(String id, String labelName, String description) {
        this.id = id;
        this.labelName = labelName;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getLabelName() {
        return labelName;
    }

    public String getDescription() {
        return description;
    }
}
