package com.example.phiduongchat.Models;

public class ChatGroup {
    private String name;
    private String message;
    private String photoUrl;
    private String imageUrl;
    private String sender;
    private String key;

    public ChatGroup(String name, String message, String photoUrl, String imageUrl, String sender, String key) {
        this.name = name;
        this.message = message;
        this.photoUrl = photoUrl;
        this.imageUrl = imageUrl;
        this.sender = sender;
        this.key = key;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public ChatGroup() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
