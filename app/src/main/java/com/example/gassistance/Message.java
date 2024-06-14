package com.example.gassistance;

public class Message {
    private String content;
    private boolean sentByUser;

    public Message(String content, boolean sentByUser) {
        this.content = content;
        this.sentByUser = sentByUser;
    }

    public String getContent() {
        return content;
    }

    public boolean isSentByUser() {
        return sentByUser;
    }
}
