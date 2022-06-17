package com.example.mobilepalengke.DataClasses;

public class Role {

    private String id, name;
    int level;
    private boolean deactivated, defaultOnRegister, fixed;

    public Role() {
    }

    public Role(String id, String name, int level) {
        this.id = id;
        this.name = name;
        this.level = level;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }

    public boolean isDeactivated() {
        return deactivated;
    }

    public boolean isDefaultOnRegister() {
        return defaultOnRegister;
    }

    public boolean isFixed() {
        return fixed;
    }
}
