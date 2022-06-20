package com.example.mobilepalengke.DataClasses;

import java.util.Map;

public class NotificationItem {

    private String id, description, title, value, timestamp, activity;
    private int category, visibility, importance;
    private boolean read, notified;
    private Map<String ,String> attributes;

    public NotificationItem() {
    }

    public NotificationItem(String id, String description, String title, String value,
                            String timestamp, String activity, int category, int visibility,
                            int importance, boolean read, boolean notified,
                            Map<String, String> attributes) {
        this.id = id;
        this.description = description;
        this.title = title;
        this.value = value;
        this.timestamp = timestamp;
        this.activity = activity;
        this.category = category;
        this.visibility = visibility;
        this.importance = importance;
        this.read = read;
        this.notified = notified;
        this.attributes = attributes;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }

    public String getValue() {
        return value;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getActivity() {
        return activity;
    }

    public int getCategory() {
        return category;
    }

    public int getVisibility() {
        return visibility;
    }

    public int getImportance() {
        return importance;
    }

    public boolean isRead() {
        return read;
    }

    public boolean isNotified() {
        return notified;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }
}
