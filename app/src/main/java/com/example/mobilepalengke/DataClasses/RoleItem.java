package com.example.mobilepalengke.DataClasses;

public class RoleItem {

    private String id, name, type;
    private int level;
    private boolean deactivated, defaultOnRegister, fixed;

    public RoleItem() {
    }

    public RoleItem(Role role, String type) {
        this.id = role.getId();
        this.name = role.getName();
        this.type = type;
        this.level = role.getLevel();
        this.deactivated = role.isDeactivated();
        this.defaultOnRegister = role.isDefaultOnRegister();
        this.fixed = role.isFixed();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
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

    public void setType(String type) {
        this.type = type;
    }
}
