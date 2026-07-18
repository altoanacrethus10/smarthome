package com.example.smarthome.repository;

import android.content.Context;
import android.util.Log;

import com.example.smarthome.database.local.DatabaseHelper;
import com.example.smarthome.database.remote.FirestoreHelper;
import com.example.smarthome.models.User;

import java.util.List;

public class UserRepository {
    private static final String TAG = "UserRepository";
    private static UserRepository instance;
    private DatabaseHelper databaseHelper;
    private FirestoreHelper firestoreHelper;

    private UserRepository(Context context) {
        databaseHelper = DatabaseHelper.getInstance(context);
        firestoreHelper = FirestoreHelper.getInstance();
    }

    public static synchronized UserRepository getInstance(Context context) {
        if (instance == null) {
            instance = new UserRepository(context);
        }
        return instance;
    }

    // ==================== CREATE USER ====================

    public void createUser(User user, final RepositoryCallback<String> callback) {
        // First save to Firebase
        firestoreHelper.createUser(user, new FirestoreHelper.FirestoreCallback<String>() {
            @Override
            public void onSuccess(String userId) {
                // Then save locally
                long result = databaseHelper.insertUser(user);
                if (result != -1) {
                    callback.onSuccess(userId);
                    Log.d(TAG, "User created in both Firebase and SQLite");
                } else {
                    callback.onError("Failed to save user locally");
                }
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
                Log.e(TAG, "Failed to create user in Firebase: " + error);
            }
        });
    }

    // ==================== GET USER ====================

    public void getUser(String userId, final RepositoryCallback<User> callback) {
        // Try to get from SQLite first (offline)
        User localUser = databaseHelper.getUser(userId);
        
        if (localUser != null) {
            callback.onSuccess(localUser);
            Log.d(TAG, "User retrieved from local database");
            
            // Also fetch from Firebase to update if needed
            fetchUserFromFirebase(userId, callback);
        } else {
            // Not in local, fetch from Firebase
            fetchUserFromFirebase(userId, callback);
        }
    }

    private void fetchUserFromFirebase(String userId, final RepositoryCallback<User> callback) {
        firestoreHelper.getUser(userId, new FirestoreHelper.FirestoreCallback<User>() {
            @Override
            public void onSuccess(User user) {
                // Save to local for offline use
                if (user != null) {
                    databaseHelper.insertUser(user);
                    callback.onSuccess(user);
                    Log.d(TAG, "User retrieved from Firebase and cached locally");
                }
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
                Log.e(TAG, "Failed to get user from Firebase: " + error);
            }
        });
    }

    // ==================== UPDATE USER ====================

    public void updateUser(User user, final RepositoryCallback<Boolean> callback) {
        // Update locally first
        databaseHelper.updateUser(user);
        
        // Then update Firebase
        firestoreHelper.updateUser(user, new FirestoreHelper.FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                callback.onSuccess(true);
                Log.d(TAG, "User updated in both Firebase and SQLite");
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
                Log.e(TAG, "Failed to update user in Firebase: " + error);
            }
        });
    }

    public void updateUserAvatar(String userId, String imageUrl, final RepositoryCallback<Boolean> callback) {
        User user = new User();
        user.setId(userId);
        user.setAvatarUrl(imageUrl);
        
        updateUser(user, callback);
    }

    // ==================== CHECK USER EXISTS ====================

    public boolean isUserExists(String userId) {
        return databaseHelper.isUserExists(userId);
    }

    // ==================== GET ALL USERS ====================

    // In a real app, you might want to get all users
    // But for this app, we'll just get from Firebase if needed

    // ==================== CALLBACK INTERFACE ====================

    public interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}

