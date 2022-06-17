package com.example.mobilepalengke.DataClasses;

public class Message {

    private String id, sender, timestamp, value;

    public Message() {
    }

    public Message(String id, String sender, String timestamp, String value) {
        this.id = id;
        this.sender = sender;
        this.timestamp = timestamp;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public String getSender() {
        return sender;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getType() {
        return "text";
    }

    public String getValue() {
        return value;
    }
}
