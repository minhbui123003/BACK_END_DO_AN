package com.TTT.Tniciu_API.Chatbot;

public class Message {
    private String role;
    private String type;
    private String content;
    private String content_type;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}

