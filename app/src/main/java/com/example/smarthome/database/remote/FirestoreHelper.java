package com.example.smarthome.database.remote;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.example.smarthome.constants.AppConstants;
import com.example.smarthome.models.Booking;
import com.example.smarthome.models.Complaint;
import com.example.smarthome.models.Feedback;
import com.example.smarthome.models.House;
import com.example.smarthome.models.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreHelper {
    private static final String TAG = "FirestoreHelper";
    private static FirestoreHelper instance;
    private FirebaseFirestore firestore;

    private FirestoreHelper() {
        try {
            firestore = FirebaseFirestore.getInstance();
        } catch (IllegalStateException e) {
            Log.e(TAG, "Firebase not initialized. Make sure to add google-services.json and enable the plugin.");
        }
    }

    public static synchronized FirestoreHelper getInstance() {
        if (instance == null) {
            instance = new FirestoreHelper();
        }
        return instance;
    }

    private boolean isFirestoreEnabled() {
        return firestore != null;
    }

    // ==================== USER OPERATIONS ====================

    public void createUser(User user, final FirestoreCallback<String> callback) {
        if (!isFirestoreEnabled()) {
            callback.onError("Firestore is not initialized");
            return;
        }
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("name", user.getName());
        userMap.put("email", user.getEmail());
        userMap.put("phone", user.getPhone());
        userMap.put("userType", user.getUserType());
        userMap.put("avatarUrl", user.getAvatarUrl());
        userMap.put("createdAt", user.getCreatedAt());
        userMap.put("updatedAt", user.getUpdatedAt());
        userMap.put("isActive", user.isActive());

        firestore.collection(AppConstants.USERS_COLLECTION)
                .document(user.getId())
                .set(userMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        callback.onSuccess(user.getId());
                        Log.d(TAG, "User created: " + user.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onError(e.getMessage());
                        Log.e(TAG, "Failed to create user: " + e.getMessage());
                    }
                });
    }

    public void getUser(String userId, final FirestoreCallback<User> callback) {
        if (!isFirestoreEnabled()) {
            callback.onError("Firestore is not initialized");
            return;
        }
        firestore.collection(AppConstants.USERS_COLLECTION)
                .document(userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            User user = documentSnapshot.toObject(User.class);
                            callback.onSuccess(user);
                            Log.d(TAG, "User retrieved: " + userId);
                        } else {
                            callback.onError("User not found");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onError(e.getMessage());
                        Log.e(TAG, "Failed to get user: " + e.getMessage());
                    }
                });
    }

    public void updateUser(User user, final FirestoreCallback<Void> callback) {
        if (!isFirestoreEnabled()) {
            callback.onError("Firestore is not initialized");
            return;
        }
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("name", user.getName());
        userMap.put("phone", user.getPhone());
        userMap.put("avatarUrl", user.getAvatarUrl());
        userMap.put("updatedAt", String.valueOf(System.currentTimeMillis()));

        firestore.collection(AppConstants.USERS_COLLECTION)
                .document(user.getId())
                .update(userMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        callback.onSuccess(null);
                        Log.d(TAG, "User updated: " + user.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onError(e.getMessage());
                        Log.e(TAG, "Failed to update user: " + e.getMessage());
                    }
                });
    }

    // ==================== HOUSE OPERATIONS ====================

    public void createHouse(House house, final FirestoreCallback<String> callback) {
        if (!isFirestoreEnabled()) {
            callback.onError("Firestore is not initialized");
            return;
        }
        Map<String, Object> houseMap = new HashMap<>();
        houseMap.put("id", house.getId());
        houseMap.put("ownerId", house.getOwnerId());
        houseMap.put("ownerName", house.getOwnerName());
        houseMap.put("title", house.getTitle());
        houseMap.put("description", house.getDescription());
        houseMap.put("location", house.getLocation());
        houseMap.put("latitude", house.getLatitude());
        houseMap.put("longitude", house.getLongitude());
        houseMap.put("price", house.getPrice());
        houseMap.put("bedrooms", house.getBedrooms());
        houseMap.put("bathrooms", house.getBathrooms());
        houseMap.put("area", house.getArea());
        houseMap.put("propertyType", house.getPropertyType());
        houseMap.put("images", house.getImages());
        houseMap.put("features", house.getFeatures());
        houseMap.put("isAvailable", house.isAvailable());
        houseMap.put("createdAt", house.getCreatedAt());
        houseMap.put("updatedAt", house.getUpdatedAt());
        houseMap.put("views", house.getViews());
        houseMap.put("favorites", house.getFavorites());

        firestore.collection(AppConstants.HOUSES_COLLECTION)
                .document(house.getId())
                .set(houseMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        callback.onSuccess(house.getId());
                        Log.d(TAG, "House created: " + house.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onError(e.getMessage());
                        Log.e(TAG, "Failed to create house: " + e.getMessage());
                    }
                });
    }

    public void getAllHouses(final FirestoreCallback<List<House>> callback) {
        if (!isFirestoreEnabled()) {
            callback.onError("Firestore is not initialized");
            return;
        }
        firestore.collection(AppConstants.HOUSES_COLLECTION)
                .whereEqualTo("isAvailable", true)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<House> houses = new ArrayList<>();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            House house = document.toObject(House.class);
                            houses.add(house);
                        }
                        callback.onSuccess(houses);
                        Log.d(TAG, "Retrieved " + houses.size() + " houses");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onError(e.getMessage());
                        Log.e(TAG, "Failed to get houses: " + e.getMessage());
                    }
                });
    }

    public void getHouse(String houseId, final FirestoreCallback<House> callback) {
        if (!isFirestoreEnabled()) {
            callback.onError("Firestore is not initialized");
            return;
        }
        firestore.collection(AppConstants.HOUSES_COLLECTION)
                .document(houseId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            House house = documentSnapshot.toObject(House.class);
                            callback.onSuccess(house);
                            Log.d(TAG, "House retrieved: " + houseId);
                        } else {
                            callback.onError("House not found");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onError(e.getMessage());
                        Log.e(TAG, "Failed to get house: " + e.getMessage());
                    }
                });
    }

    public void deleteHouse(String houseId, final FirestoreCallback<Void> callback) {
        if (!isFirestoreEnabled()) {
            callback.onError("Firestore is not initialized");
            return;
        }
        firestore.collection(AppConstants.HOUSES_COLLECTION)
                .document(houseId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        callback.onSuccess(null);
                        Log.d(TAG, "House deleted: " + houseId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onError(e.getMessage());
                        Log.e(TAG, "Failed to delete house: " + e.getMessage());
                    }
                });
    }

    public void getHousesByOwner(String ownerId, final FirestoreCallback<List<House>> callback) {
        if (!isFirestoreEnabled()) {
            callback.onError("Firestore is not initialized");
            return;
        }
        firestore.collection(AppConstants.HOUSES_COLLECTION)
                .whereEqualTo("ownerId", ownerId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<House> houses = new ArrayList<>();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            House house = document.toObject(House.class);
                            houses.add(house);
                        }
                        callback.onSuccess(houses);
                        Log.d(TAG, "Retrieved " + houses.size() + " houses for owner: " + ownerId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onError(e.getMessage());
                        Log.e(TAG, "Failed to get houses by owner: " + e.getMessage());
                    }
                });
    }

    // ==================== BOOKING OPERATIONS ====================

    public void createBooking(Booking booking, final FirestoreCallback<String> callback) {
        if (!isFirestoreEnabled()) {
            callback.onError("Firestore is not initialized");
            return;
        }
        Map<String, Object> bookingMap = new HashMap<>();
        bookingMap.put("id", booking.getId());
        bookingMap.put("houseId", booking.getHouseId());
        bookingMap.put("houseTitle", booking.getHouseTitle());
        bookingMap.put("houseLocation", booking.getHouseLocation());
        bookingMap.put("tenantId", booking.getTenantId());
        bookingMap.put("tenantName", booking.getTenantName());
        bookingMap.put("tenantPhone", booking.getTenantPhone());
        bookingMap.put("ownerId", booking.getOwnerId());
        bookingMap.put("ownerName", booking.getOwnerName());
        bookingMap.put("moveInDate", booking.getMoveInDate());
        bookingMap.put("moveOutDate", booking.getMoveOutDate());
        bookingMap.put("numberOfTenants", booking.getNumberOfTenants());
        bookingMap.put("totalPrice", booking.getTotalPrice());
        bookingMap.put("securityDeposit", booking.getSecurityDeposit());
        bookingMap.put("serviceFee", booking.getServiceFee());
        bookingMap.put("paymentMethod", booking.getPaymentMethod());
        bookingMap.put("paymentStatus", booking.getPaymentStatus());
        bookingMap.put("referenceNumber", booking.getReferenceNumber());
        bookingMap.put("status", booking.getStatus());
        bookingMap.put("notes", booking.getNotes());
        bookingMap.put("createdAt", booking.getCreatedAt());
        bookingMap.put("updatedAt", booking.getUpdatedAt());

        firestore.collection(AppConstants.BOOKINGS_COLLECTION)
                .document(booking.getId())
                .set(bookingMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        callback.onSuccess(booking.getId());
                        Log.d(TAG, "Booking created: " + booking.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onError(e.getMessage());
                        Log.e(TAG, "Failed to create booking: " + e.getMessage());
                    }
                });
    }

    public void getBooking(String bookingId, final FirestoreCallback<Booking> callback) {
        if (!isFirestoreEnabled()) {
            callback.onError("Firestore is not initialized");
            return;
        }
        firestore.collection(AppConstants.BOOKINGS_COLLECTION)
                .document(bookingId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            Booking booking = documentSnapshot.toObject(Booking.class);
                            callback.onSuccess(booking);
                        } else {
                            callback.onError("Booking not found");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onError(e.getMessage());
                    }
                });
    }

    public void getBookingsByTenant(String tenantId, final FirestoreCallback<List<Booking>> callback) {
        if (!isFirestoreEnabled()) {
            callback.onError("Firestore is not initialized");
            return;
        }
        firestore.collection(AppConstants.BOOKINGS_COLLECTION)
                .whereEqualTo("tenantId", tenantId)
                .orderBy("createdAt")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<Booking> bookings = new ArrayList<>();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Booking booking = document.toObject(Booking.class);
                            bookings.add(booking);
                        }
                        callback.onSuccess(bookings);
                        Log.d(TAG, "Retrieved " + bookings.size() + " bookings for tenant: " + tenantId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onError(e.getMessage());
                        Log.e(TAG, "Failed to get bookings: " + e.getMessage());
                    }
                });
    }

    public void getBookingsByOwner(String ownerId, final FirestoreCallback<List<Booking>> callback) {
        if (!isFirestoreEnabled()) {
            callback.onError("Firestore is not initialized");
            return;
        }
        firestore.collection(AppConstants.BOOKINGS_COLLECTION)
                .whereEqualTo("ownerId", ownerId)
                .orderBy("createdAt")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<Booking> bookings = new ArrayList<>();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Booking booking = document.toObject(Booking.class);
                            bookings.add(booking);
                        }
                        callback.onSuccess(bookings);
                        Log.d(TAG, "Retrieved " + bookings.size() + " bookings for owner: " + ownerId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onError(e.getMessage());
                        Log.e(TAG, "Failed to get owner bookings: " + e.getMessage());
                    }
                });
    }

    public void updateBookingStatus(String bookingId, String status, final FirestoreCallback<Void> callback) {
        if (!isFirestoreEnabled()) {
            callback.onError("Firestore is not initialized");
            return;
        }
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status);
        updates.put("updatedAt", String.valueOf(System.currentTimeMillis()));

        firestore.collection(AppConstants.BOOKINGS_COLLECTION)
                .document(bookingId)
                .update(updates)
                .addOnSuccessListener(unused -> {
                    callback.onSuccess(null);
                    Log.d(TAG, "Booking status updated: " + bookingId + " -> " + status);
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                    Log.e(TAG, "Failed to update booking status: " + e.getMessage());
                });
    }

    // ==================== FEEDBACK OPERATIONS ====================

    public void createFeedback(Feedback feedback, final FirestoreCallback<String> callback) {
        if (!isFirestoreEnabled()) {
            callback.onError("Firestore is not initialized");
            return;
        }
        Map<String, Object> feedbackMap = new HashMap<>();
        feedbackMap.put("id", feedback.getId());
        feedbackMap.put("userId", feedback.getUserId());
        feedbackMap.put("userName", feedback.getUserName());
        feedbackMap.put("userEmail", feedback.getUserEmail());
        feedbackMap.put("houseId", feedback.getHouseId());
        feedbackMap.put("houseTitle", feedback.getHouseTitle());
        feedbackMap.put("rating", feedback.getRating());
        feedbackMap.put("feedbackType", feedback.getFeedbackType());
        feedbackMap.put("message", feedback.getMessage());
        feedbackMap.put("createdAt", feedback.getCreatedAt());
        feedbackMap.put("updatedAt", feedback.getUpdatedAt());

        firestore.collection(AppConstants.FEEDBACK_COLLECTION)
                .document(feedback.getId())
                .set(feedbackMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        callback.onSuccess(feedback.getId());
                        Log.d(TAG, "Feedback created: " + feedback.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onError(e.getMessage());
                        Log.e(TAG, "Failed to create feedback: " + e.getMessage());
                    }
                });
    }

    public void getFeedbackByHouseId(String houseId, final FirestoreCallback<List<Feedback>> callback) {
        if (!isFirestoreEnabled()) {
            callback.onError("Firestore is not initialized");
            return;
        }
        firestore.collection(AppConstants.FEEDBACK_COLLECTION)
                .whereEqualTo("houseId", houseId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Feedback> feedbackList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Feedback feedback = document.toObject(Feedback.class);
                        feedbackList.add(feedback);
                    }
                    callback.onSuccess(feedbackList);
                    Log.d(TAG, "Retrieved " + feedbackList.size() + " feedback items for house: " + houseId);
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                    Log.e(TAG, "Failed to get feedback by house: " + e.getMessage());
                });
    }

    // ==================== COMPLAINT OPERATIONS ====================

    public void createComplaint(Complaint complaint, final FirestoreCallback<String> callback) {
        if (!isFirestoreEnabled()) {
            callback.onError("Firestore is not initialized");
            return;
        }
        Map<String, Object> complaintMap = new HashMap<>();
        complaintMap.put("id", complaint.getId());
        complaintMap.put("userId", complaint.getUserId());
        complaintMap.put("userName", complaint.getUserName());
        complaintMap.put("userEmail", complaint.getUserEmail());
        complaintMap.put("ownerId", complaint.getOwnerId());
        complaintMap.put("houseId", complaint.getHouseId());
        complaintMap.put("houseTitle", complaint.getHouseTitle());
        complaintMap.put("subject", complaint.getSubject());
        complaintMap.put("category", complaint.getCategory());
        complaintMap.put("description", complaint.getDescription());
        complaintMap.put("status", complaint.getStatus());
        complaintMap.put("response", complaint.getResponse());
        complaintMap.put("createdAt", complaint.getCreatedAt());
        complaintMap.put("updatedAt", complaint.getUpdatedAt());

        firestore.collection(AppConstants.COMPLAINTS_COLLECTION)
                .document(complaint.getId())
                .set(complaintMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        callback.onSuccess(complaint.getId());
                        Log.d(TAG, "Complaint created: " + complaint.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onError(e.getMessage());
                        Log.e(TAG, "Failed to create complaint: " + e.getMessage());
                    }
                });
    }

    public void getComplaintsByUser(String userId, final FirestoreCallback<List<Complaint>> callback) {
        if (!isFirestoreEnabled()) {
            callback.onError("Firestore is not initialized");
            return;
        }
        firestore.collection(AppConstants.COMPLAINTS_COLLECTION)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Complaint> complaints = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        complaints.add(document.toObject(Complaint.class));
                    }
                    callback.onSuccess(complaints);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void getComplaintsByOwner(String ownerId, final FirestoreCallback<List<Complaint>> callback) {
        if (!isFirestoreEnabled()) {
            callback.onError("Firestore is not initialized");
            return;
        }
        firestore.collection(AppConstants.COMPLAINTS_COLLECTION)
                .whereEqualTo("ownerId", ownerId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Complaint> complaints = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        complaints.add(document.toObject(Complaint.class));
                    }
                    callback.onSuccess(complaints);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void updateComplaintStatus(String complaintId, String status, String response, final FirestoreCallback<Void> callback) {
        if (!isFirestoreEnabled()) {
            callback.onError("Firestore is not initialized");
            return;
        }
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status);
        if (response != null) updates.put("response", response);
        updates.put("updatedAt", String.valueOf(System.currentTimeMillis()));

        firestore.collection(AppConstants.COMPLAINTS_COLLECTION)
                .document(complaintId)
                .update(updates)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // ==================== CALLBACK INTERFACE ====================

    public interface FirestoreCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}
