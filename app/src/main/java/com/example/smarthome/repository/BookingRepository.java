package com.example.smarthome.repository;

import android.content.Context;
import android.util.Log;

import com.example.smarthome.database.local.DatabaseHelper;
import com.example.smarthome.database.remote.FirestoreHelper;
import com.example.smarthome.models.Booking;

import java.util.List;

public class BookingRepository {
    private static final String TAG = "BookingRepository";
    private static BookingRepository instance;
    private DatabaseHelper databaseHelper;
    private FirestoreHelper firestoreHelper;

    private BookingRepository(Context context) {
        databaseHelper = DatabaseHelper.getInstance(context);
        firestoreHelper = FirestoreHelper.getInstance();
    }

    public static synchronized BookingRepository getInstance(Context context) {
        if (instance == null) {
            instance = new BookingRepository(context);
        }
        return instance;
    }

    // ==================== CREATE BOOKING ====================

    public void createBooking(Booking booking, final RepositoryCallback<String> callback) {
        firestoreHelper.createBooking(booking, new FirestoreHelper.FirestoreCallback<String>() {
            @Override
            public void onSuccess(String bookingId) {
                databaseHelper.insertBooking(booking);
                callback.onSuccess(bookingId);
                Log.d(TAG, "Booking created in Firebase and SQLite: " + bookingId);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
                Log.e(TAG, "Failed to create booking in Firebase: " + error);
            }
        });
    }

    public void getBooking(String bookingId, final RepositoryCallback<Booking> callback) {
        firestoreHelper.getBooking(bookingId, new FirestoreHelper.FirestoreCallback<Booking>() {
            @Override
            public void onSuccess(Booking booking) {
                callback.onSuccess(booking);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    // ==================== GET BOOKINGS BY TENANT ====================

    public void getBookingsByTenant(String tenantId, final RepositoryCallback<List<Booking>> callback) {
        firestoreHelper.getBookingsByTenant(tenantId, new FirestoreHelper.FirestoreCallback<List<Booking>>() {
            @Override
            public void onSuccess(List<Booking> bookings) {
                callback.onSuccess(bookings);
                Log.d(TAG, "Bookings retrieved for tenant: " + tenantId + ", count: " + bookings.size());
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
                Log.e(TAG, "Failed to get bookings: " + error);
            }
        });
    }

    public void getBookingsByOwner(String ownerId, final RepositoryCallback<List<Booking>> callback) {
        firestoreHelper.getBookingsByOwner(ownerId, new FirestoreHelper.FirestoreCallback<List<Booking>>() {
            @Override
            public void onSuccess(List<Booking> bookings) {
                callback.onSuccess(bookings);
                Log.d(TAG, "Bookings retrieved for owner: " + ownerId + ", count: " + bookings.size());
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
                Log.e(TAG, "Failed to get owner bookings: " + error);
            }
        });
    }

    // ==================== UPDATE BOOKING STATUS ====================

    public void updateBookingStatus(String bookingId, String status, final RepositoryCallback<Boolean> callback) {
        firestoreHelper.updateBookingStatus(bookingId, status, new FirestoreHelper.FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                databaseHelper.updateBookingStatus(bookingId, status);
                callback.onSuccess(true);
                Log.d(TAG, "Booking status updated in Firebase and SQLite: " + bookingId + " -> " + status);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
                Log.e(TAG, "Failed to update booking status: " + error);
            }
        });
    }

    // ==================== CALLBACK INTERFACE ====================

    public interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}
