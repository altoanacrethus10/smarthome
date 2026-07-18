package com.example.smarthome.models;

public class Booking {
    private String id;
    private String houseId;
    private String houseTitle;
    private String houseLocation;
    private String tenantId;
    private String tenantName;
    private String tenantPhone;
    private String ownerId;
    private String ownerName;
    private String moveInDate;
    private String moveOutDate;
    private int numberOfTenants;
    private double totalPrice;
    private double securityDeposit;
    private double serviceFee;
    private String paymentMethod; // "M-Pesa", "Bank Transfer", "Card", "Cash"
    private String paymentStatus; // "pending", "paid", "failed"
    private String referenceNumber;
    private String status; // "pending", "confirmed", "cancelled", "completed"
    private String notes;
    private String createdAt;
    private String updatedAt;

    // Empty constructor for Firebase
    public Booking() {}

    // Constructor with required fields
    public Booking(String id, String houseId, String tenantId, String moveInDate) {
        this.id = id;
        this.houseId = houseId;
        this.tenantId = tenantId;
        this.moveInDate = moveInDate;
        this.status = "pending";
        this.paymentStatus = "pending";
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getHouseId() { return houseId; }
    public void setHouseId(String houseId) { this.houseId = houseId; }

    public String getHouseTitle() { return houseTitle; }
    public void setHouseTitle(String houseTitle) { this.houseTitle = houseTitle; }

    public String getHouseLocation() { return houseLocation; }
    public void setHouseLocation(String houseLocation) { this.houseLocation = houseLocation; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getTenantName() { return tenantName; }
    public void setTenantName(String tenantName) { this.tenantName = tenantName; }

    public String getTenantPhone() { return tenantPhone; }
    public void setTenantPhone(String tenantPhone) { this.tenantPhone = tenantPhone; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getMoveInDate() { return moveInDate; }
    public void setMoveInDate(String moveInDate) { this.moveInDate = moveInDate; }

    public String getMoveOutDate() { return moveOutDate; }
    public void setMoveOutDate(String moveOutDate) { this.moveOutDate = moveOutDate; }

    public int getNumberOfTenants() { return numberOfTenants; }
    public void setNumberOfTenants(int numberOfTenants) { this.numberOfTenants = numberOfTenants; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public double getSecurityDeposit() { return securityDeposit; }
    public void setSecurityDeposit(double securityDeposit) { this.securityDeposit = securityDeposit; }

    public double getServiceFee() { return serviceFee; }
    public void setServiceFee(double serviceFee) { this.serviceFee = serviceFee; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    // Helper methods
    public boolean isPending() {
        return "pending".equalsIgnoreCase(status);
    }

    public boolean isConfirmed() {
        return "confirmed".equalsIgnoreCase(status);
    }

    public boolean isCancelled() {
        return "cancelled".equalsIgnoreCase(status);
    }

    public boolean isCompleted() {
        return "completed".equalsIgnoreCase(status);
    }

    public String getStatusText() {
        switch (status != null ? status.toLowerCase() : "") {
            case "pending":
                return "Pending";
            case "confirmed":
                return "Confirmed ✓";
            case "cancelled":
                return "Cancelled ✗";
            case "completed":
                return "Completed";
            default:
                return "Unknown";
        }
    }

    public int getStatusColor() {
        switch (status != null ? status.toLowerCase() : "") {
            case "pending":
                return android.R.color.holo_orange_light;
            case "confirmed":
                return android.R.color.holo_green_light;
            case "cancelled":
                return android.R.color.holo_red_light;
            case "completed":
                return android.R.color.holo_blue_light;
            default:
                return android.R.color.darker_gray;
        }
    }

    @Override
    public String toString() {
        return "Booking{" +
                "id='" + id + '\'' +
                ", houseId='" + houseId + '\'' +
                ", tenantId='" + tenantId + '\'' +
                ", moveInDate='" + moveInDate + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}