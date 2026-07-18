package com.example.smarthome.models;

import java.util.List;

public class House {
    private String id;
    private String ownerId;
    private String ownerName;
    private String title;
    private String description;
    private String location;
    private double latitude;
    private double longitude;
    private double price;
    private int bedrooms;
    private int bathrooms;
    private double area; // in square meters
    private String propertyType; // "Apartment", "House", "Studio", "Room", etc.
    private List<String> images;
    private List<String> features;
    private boolean isAvailable;
    private boolean isFurnished;
    private double rating;
    private int reviewCount;
    private String createdAt;
    private String updatedAt;
    private int views;
    private int favorites;

    // Empty constructor for Firebase
    public House() {}

    // Constructor with required fields
    public House(String id, String ownerId, String title, String location,
                 double price, int bedrooms, int bathrooms) {
        this.id = id;
        this.ownerId = ownerId;
        this.title = title;
        this.location = location;
        this.price = price;
        this.bedrooms = bedrooms;
        this.bathrooms = bathrooms;
        this.isAvailable = true;
        this.isFurnished = false;
        this.rating = 0.0;
        this.reviewCount = 0;
        this.views = 0;
        this.favorites = 0;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getBedrooms() { return bedrooms; }
    public void setBedrooms(int bedrooms) { this.bedrooms = bedrooms; }

    public int getBathrooms() { return bathrooms; }
    public void setBathrooms(int bathrooms) { this.bathrooms = bathrooms; }

    public String getPropertyType() { return propertyType != null ? propertyType : "House"; }
    public void setPropertyType(String propertyType) { this.propertyType = propertyType; }

    public double getArea() { return area; }
    public void setArea(double area) { this.area = area; }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }

    public List<String> getFeatures() { return features; }
    public void setFeatures(List<String> features) { this.features = features; }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    public boolean isFurnished() { return isFurnished; }
    public void setFurnished(boolean furnished) { isFurnished = furnished; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public int getViews() { return views; }
    public void setViews(int views) { this.views = views; }

    public int getFavorites() { return favorites; }
    public void setFavorites(int favorites) { this.favorites = favorites; }

    // Helper methods
    public String getFormattedPrice() {
        return String.format("Tsh %,.0f", price);
    }

    public String getLocationShort() {
        if (location == null) return "";
        String[] parts = location.split(",");
        return parts[0].trim();
    }

    public void incrementViews() {
        this.views++;
    }

    public void incrementFavorites() {
        this.favorites++;
    }

    public void decrementFavorites() {
        if (this.favorites > 0) {
            this.favorites--;
        }
    }

    @Override
    public String toString() {
        return "House{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", location='" + location + '\'' +
                ", price=" + price +
                ", bedrooms=" + bedrooms +
                ", bathrooms=" + bathrooms +
                '}';
    }
}