package com.example.smarthome.models;

public class Complaint {
    private String id;
    private String userId;
    private String userName;
    private String userEmail;
    private String ownerId;
    private String houseId;
    private String houseTitle;
    private String subject;
    private String category;
    private String description;
    private String status; // "pending", "reviewing", "resolved", "rejected"
    private String response;
    private String createdAt;
    private String updatedAt;

    // Empty constructor for Firebase
    public Complaint() {}

    // Constructor with required fields
    public Complaint(String id, String userId, String subject, String description, String category) {
        this.id = id;
        this.userId = userId;
        this.subject = subject;
        this.description = description;
        this.category = category;
        this.status = "pending";
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

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public String getHouseId() { return houseId; }
    public void setHouseId(String houseId) { this.houseId = houseId; }

    public String getHouseTitle() { return houseTitle; }
    public void setHouseTitle(String houseTitle) { this.houseTitle = houseTitle; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    // Helper methods
    public boolean isPending() {
        return "pending".equalsIgnoreCase(status);
    }

    public boolean isReviewing() {
        return "reviewing".equalsIgnoreCase(status);
    }

    public boolean isResolved() {
        return "resolved".equalsIgnoreCase(status);
    }

    public boolean isRejected() {
        return "rejected".equalsIgnoreCase(status);
    }

    public String getStatusText() {
        switch (status != null ? status.toLowerCase() : "") {
            case "pending":
                return "Pending";
            case "reviewing":
                return "Under Review";
            case "resolved":
                return "Resolved ✓";
            case "rejected":
                return "Rejected ✗";
            default:
                return "Unknown";
        }
    }

    public int getStatusColor() {
        switch (status != null ? status.toLowerCase() : "") {
            case "pending":
                return android.R.color.holo_orange_light;
            case "reviewing":
                return android.R.color.holo_blue_light;
            case "resolved":
                return android.R.color.holo_green_light;
            case "rejected":
                return android.R.color.holo_red_light;
            default:
                return android.R.color.darker_gray;
        }
    }

    @Override
    public String toString() {
        return "Complaint{" +
                "id='" + id + '\'' +
                ", subject='" + subject + '\'' +
                ", category='" + category + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}