package com.example.mobilepalengke.DataClasses;

import java.util.Map;

public class User {

    private String id, lastName, firstName;
    private Map<String, String> roles, mobileNumbers;

    public User() {
    }

    public User(String id, String lastName, String firstName) {
        this.id = id;
        this.lastName = lastName;
        this.firstName = firstName;
    }

    public String getId() {
        return id;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public Map<String, String> getRoles() {
        return roles;
    }

    public Map<String, String> getMobileNumbers() {
        return mobileNumbers;
    }

    public void setRoles(Map<String, String> roles) {
        this.roles = roles;
    }
}
