package com.example.smarthome.models;

import java.util.concurrent.TimeUnit;

public class Contract {
    private String id;
    private String tenantId;
    private String tenantName;
    private String ownerId;
    private String ownerName;
    private String houseId;
    private String houseTitle;
    private String startDate; // Timestamp
    private String endDate;   // Timestamp
    private double monthlyRent;
    private String status; // "active", "expired", "terminated"
    private String createdAt;

    public Contract() {}

    public Contract(String id, String tenantId, String ownerId, String houseId, String startDate, String endDate) {
        this.id = id;
        this.tenantId = tenantId;
        this.ownerId = ownerId;
        this.houseId = houseId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = "active";
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getTenantName() { return tenantName; }
    public void setTenantName(String tenantName) { this.tenantName = tenantName; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getHouseId() { return houseId; }
    public void setHouseId(String houseId) { this.houseId = houseId; }

    public String getHouseTitle() { return houseTitle; }
    public void setHouseTitle(String houseTitle) { this.houseTitle = houseTitle; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public double getMonthlyRent() { return monthlyRent; }
    public void setMonthlyRent(double monthlyRent) { this.monthlyRent = monthlyRent; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    // Helper methods
    public long getRemainingDays() {
        if (endDate == null) return 0;
        try {
            long end = Long.parseLong(endDate);
            long now = System.currentTimeMillis();
            if (end <= now) return 0;
            long diff = end - now;
            return TimeUnit.MILLISECONDS.toDays(diff);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public boolean isActive() {
        return "active".equalsIgnoreCase(status) && getRemainingDays() > 0;
    }
}
