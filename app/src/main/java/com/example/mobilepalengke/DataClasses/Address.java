package com.example.mobilepalengke.DataClasses;

public class Address {

    String id, ownerId, name, value;

    public Address() {
    }



    public Address(String id, String ownerId, String name, String value) {
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
