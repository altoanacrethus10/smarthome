package com.example.smarthome.models;

import java.util.List;

public class Conversation {
    private String lastMessage;
    private String lastTimestamp;
    private String propertyId;
    private List<String> participants;

    private String chatId;
    private String otherUserId;
    private String otherUserName;
    private String otherAvatarUrl;
    private String propertyTitle;

    public Conversation() {}

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public String getLastTimestamp() { return lastTimestamp; }
    public void setLastTimestamp(String lastTimestamp) { this.lastTimestamp = lastTimestamp; }

    public String getPropertyId() { return propertyId; }
    public void setPropertyId(String propertyId) { this.propertyId = propertyId; }

    public List<String> getParticipants() { return participants; }
    public void setParticipants(List<String> participants) { this.participants = participants; }

    public String getChatId() { return chatId; }
    public void setChatId(String chatId) { this.chatId = chatId; }

    public String getOtherUserId() { return otherUserId; }
    public void setOtherUserId(String otherUserId) { this.otherUserId = otherUserId; }

    public String getOtherUserName() { return otherUserName; }
    public void setOtherUserName(String otherUserName) { this.otherUserName = otherUserName; }

    public String getOtherAvatarUrl() { return otherAvatarUrl; }
    public void setOtherAvatarUrl(String otherAvatarUrl) { this.otherAvatarUrl = otherAvatarUrl; }

    public String getPropertyTitle() { return propertyTitle; }
    public void setPropertyTitle(String propertyTitle) { this.propertyTitle = propertyTitle; }
}
