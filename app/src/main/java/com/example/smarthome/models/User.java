package com.example.smarthome.models;

public class User {
    private String id;
    private String name;
    private String email;
    private String phone;
    private String userType; // "owner" or "tenant"
    private String avatarUrl;
    private String createdAt;
    private String updatedAt;
    private boolean isActive;

    // Empty constructor for Firebase
    public User() {}

    // Full constructor
    public User(String id, String name, String email, String phone,
                String userType, String avatarUrl, String createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.userType = userType;
        this.avatarUrl = avatarUrl;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
        this.isActive = true;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    // Helper methods
    public boolean isOwner() {
        return "owner".equalsIgnoreCase(userType);
    }

    public boolean isTenant() {
        return "tenant".equalsIgnoreCase(userType);
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", userType='" + userType + '\'' +
                '}';
    }
}