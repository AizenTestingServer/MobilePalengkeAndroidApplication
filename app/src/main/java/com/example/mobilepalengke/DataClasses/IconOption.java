package com.example.mobilepalengke.DataClasses;

public class IconOption {

    private final String labelName;
    private final int icon;

    public IconOption(String labelName, int icon) {
        this.labelName = labelName;
        this.icon = icon;
    }

    public String getLabelName() {
        return labelName;
    }

    public int getIcon() {
        return icon;
    }
}
