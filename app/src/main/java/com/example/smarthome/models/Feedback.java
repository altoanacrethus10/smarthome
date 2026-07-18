package com.example.smarthome.models;

public class Feedback {
    private String id;
    private String userId;
    private String userName;
    private String userEmail;
    private String houseId;
    private String houseTitle;
    private float rating; // 1-5
    private String feedbackType;
    private String message;
    private String createdAt;
    private String updatedAt;

    // Empty constructor for Firebase
    public Feedback() {}

    // Constructor with required fields
    public Feedback(String id, String userId, float rating, String message) {
        this.id = id;
        this.userId = userId;
        this.rating = rating;
        this.message = message;
        this.createdAt = String.valueOf(System.currentTimeMillis());
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getHouseId() { return houseId; }
    public void setHouseId(String houseId) { this.houseId = houseId; }

    public String getHouseTitle() { return houseTitle; }
    public void setHouseTitle(String houseTitle) { this.houseTitle = houseTitle; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public String getFeedbackType() { return feedbackType; }
    public void setFeedbackType(String feedbackType) { this.feedbackType = feedbackType; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    // Helper methods
    public String getRatingText() {
        if (rating >= 4.5) return "Excellent";
        if (rating >= 4.0) return "Very Good";
        if (rating >= 3.0) return "Good";
        if (rating >= 2.0) return "Fair";
        return "Poor";
    }

    public String getRatingStars() {
        StringBuilder stars = new StringBuilder();
        int fullStars = (int) rating;
        for (int i = 0; i < fullStars; i++) {
            stars.append("⭐");
        }
        if (rating - fullStars >= 0.5) {
            stars.append("⭐");
        }
        return stars.toString();
    }

    @Override
    public String toString() {
        return "Feedback{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", rating=" + rating +
                ", message='" + message + '\'' +
                '}';
    }
}