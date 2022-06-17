package com.example.mobilepalengke.DataClasses;

import java.util.Map;

public class Chat {

    private String id;
    private Map<String, String> participants;
    private Map<String, Message> messages;
    private Map<String, Boolean> isRead;
    private Map<String, Boolean> isReading;

    public Chat() {
    }

    public Chat(String id, Map<String, String> participants, Map<String, Message> messages,
            Map<String, Boolean> isRead, Map<String, Boolean> isReading) {
        this.id = id;
        this.participants = participants;
        this.messages = messages;
        this.isRead = isRead;
        this.isReading = isReading;
    }

    public String getId() {
        return id;
    }

    public Map<String, String> getParticipants() {
        return participants;
    }

    public Map<String, Message> getMessages() {
        return messages;
    }

    public Map<String, Boolean> getIsRead() {
        return isRead;
    }

    public Map<String, Boolean> getIsReading() {
        return isReading;
    }
}
