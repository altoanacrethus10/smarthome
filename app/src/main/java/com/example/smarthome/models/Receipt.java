package com.example.smarthome.models;

public class Receipt {
    private String id;
    private String receiptNumber;
    private String bookingId;
    private String tenantId;
    private String tenantName;
    private String tenantPhone;
    private String ownerId;
    private String ownerName;
    private String houseId;
    private String houseTitle;
    private String houseLocation;
    private double rentAmount;
    private double depositAmount;
    private double totalAmount;
    private String paymentDate;
    private String paymentMethod; // "cash", "bank transfer", "mobile money"
    private String status; // "paid", "pending", "refunded"
    private String notes;
    private String createdAt;

    // Empty constructor for Firebase
    public Receipt() {}

    // Constructor with required fields
    public Receipt(String id, String bookingId, String tenantId, double rentAmount, double depositAmount) {
        this.id = id;
        this.bookingId = bookingId;
        this.tenantId = tenantId;
        this.rentAmount = rentAmount;
        this.depositAmount = depositAmount;
        this.totalAmount = rentAmount + depositAmount;
        this.status = "paid";
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getReceiptNumber() { return receiptNumber; }
    public void setReceiptNumber(String receiptNumber) { this.receiptNumber = receiptNumber; }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

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

    public String getHouseId() { return houseId; }
    public void setHouseId(String houseId) { this.houseId = houseId; }

    public String getHouseTitle() { return houseTitle; }
    public void setHouseTitle(String houseTitle) { this.houseTitle = houseTitle; }

    public String getHouseLocation() { return houseLocation; }
    public void setHouseLocation(String houseLocation) { this.houseLocation = houseLocation; }

    public double getRentAmount() { return rentAmount; }
    public void setRentAmount(double rentAmount) {
        this.rentAmount = rentAmount;
        calculateTotal();
    }

    public double getDepositAmount() { return depositAmount; }
    public void setDepositAmount(double depositAmount) {
        this.depositAmount = depositAmount;
        calculateTotal();
    }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getPaymentDate() { return paymentDate; }
    public void setPaymentDate(String paymentDate) { this.paymentDate = paymentDate; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    // Helper methods
    private void calculateTotal() {
        this.totalAmount = this.rentAmount + this.depositAmount;
    }

    public String getFormattedRentAmount() {
        return String.format("Tsh %,.0f", rentAmount);
    }

    public String getFormattedDepositAmount() {
        return String.format("Tsh %,.0f", depositAmount);
    }

    public String getFormattedTotalAmount() {
        return String.format("Tsh %,.0f", totalAmount);
    }

    public boolean isPaid() {
        return "paid".equalsIgnoreCase(status);
    }

    public boolean isPending() {
        return "pending".equalsIgnoreCase(status);
    }

    public boolean isRefunded() {
        return "refunded".equalsIgnoreCase(status);
    }

    public String getStatusText() {
        switch (status != null ? status.toLowerCase() : "") {
            case "paid":
                return "Paid ✓";
            case "pending":
                return "Pending";
            case "refunded":
                return "Refunded";
            default:
                return "Unknown";
        }
    }

    @Override
    public String toString() {
        return "Receipt{" +
                "id='" + id + '\'' +
                ", receiptNumber='" + receiptNumber + '\'' +
                ", tenantName='" + tenantName + '\'' +
                ", totalAmount=" + totalAmount +
                ", status='" + status + '\'' +
                '}';
    }
}