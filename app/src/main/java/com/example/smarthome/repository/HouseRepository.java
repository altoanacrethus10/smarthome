package com.example.smarthome.repository;

import android.content.Context;
import android.util.Log;

import com.example.smarthome.database.local.DatabaseHelper;
import com.example.smarthome.database.remote.FirestoreHelper;
import com.example.smarthome.models.House;

import java.util.ArrayList;
import java.util.List;

public class HouseRepository {
    private static final String TAG = "HouseRepository";
    private static HouseRepository instance;
    private DatabaseHelper databaseHelper;
    private FirestoreHelper firestoreHelper;

    private HouseRepository(Context context) {
        databaseHelper = DatabaseHelper.getInstance(context);
        firestoreHelper = FirestoreHelper.getInstance();
    }

    public static synchronized HouseRepository getInstance(Context context) {
        if (instance == null) {
            instance = new HouseRepository(context);
        }
        return instance;
    }

    // ==================== CREATE HOUSE ====================

    public void createHouse(House house, final RepositoryCallback<String> callback) {
        firestoreHelper.createHouse(house, new FirestoreHelper.FirestoreCallback<String>() {
            @Override
            public void onSuccess(String houseId) {
                long result = databaseHelper.insertHouse(house);
                if (result != -1) {
                    callback.onSuccess(houseId);
                    Log.d(TAG, "House created in both Firebase and SQLite");
                } else {
                    callback.onError("Failed to save house locally");
                }
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
                Log.e(TAG, "Failed to create house in Firebase: " + error);
            }
        });
    }

    // ==================== GET ALL HOUSES ====================

    public void getAllHouses(final RepositoryCallback<List<House>> callback) {
        // Fetch from Firebase directly for fresh data in production
        firestoreHelper.getAllHouses(new FirestoreHelper.FirestoreCallback<List<House>>() {
            @Override
            public void onSuccess(List<House> houses) {
                // Update local cache
                for (House house : houses) {
                    if (databaseHelper.isHouseExists(house.getId())) {
                        databaseHelper.updateHouse(house);
                    } else {
                        databaseHelper.insertHouse(house);
                    }
                }
                callback.onSuccess(houses);
            }

            @Override
            public void onError(String error) {
                // Fallback to local data if offline
                List<House> localHouses = databaseHelper.getAllHouses();
                if (!localHouses.isEmpty()) {
                    callback.onSuccess(localHouses);
                } else {
                    callback.onError(error);
                }
            }
        });
    }

    public void getHousesByOwner(String ownerId, final RepositoryCallback<List<House>> callback) {
        firestoreHelper.getHousesByOwner(ownerId, new FirestoreHelper.FirestoreCallback<List<House>>() {
            @Override
            public void onSuccess(List<House> houses) {
                // Update local cache
                for (House house : houses) {
                    if (databaseHelper.isHouseExists(house.getId())) {
                        databaseHelper.updateHouse(house);
                    } else {
                        databaseHelper.insertHouse(house);
                    }
                }
                callback.onSuccess(houses);
            }

            @Override
            public void onError(String error) {
                // Fallback to local
                List<House> localHouses = databaseHelper.getHousesByOwner(ownerId);
                if (!localHouses.isEmpty()) {
                    callback.onSuccess(localHouses);
                } else {
                    callback.onError(error);
                }
            }
        });
    }

    public void getHouse(String houseId, final RepositoryCallback<House> callback) {
        firestoreHelper.getHouse(houseId, new FirestoreHelper.FirestoreCallback<House>() {
            @Override
            public void onSuccess(House house) {
                if (house != null) {
                    if (databaseHelper.isHouseExists(house.getId())) {
                        databaseHelper.updateHouse(house);
                    } else {
                        databaseHelper.insertHouse(house);
                    }
                    callback.onSuccess(house);
                } else {
                    callback.onError("House not found");
                }
            }

            @Override
            public void onError(String error) {
                // Fallback to local
                House localHouse = databaseHelper.getHouse(houseId);
                if (localHouse != null) {
                    callback.onSuccess(localHouse);
                } else {
                    callback.onError(error);
                }
            }
        });
    }

    // ==================== UPDATE HOUSE ====================

    public void updateHouse(House house, final RepositoryCallback<Boolean> callback) {
        int localResult = databaseHelper.updateHouse(house);
        
        firestoreHelper.createHouse(house, new FirestoreHelper.FirestoreCallback<String>() {
            @Override
            public void onSuccess(String houseId) {
                callback.onSuccess(true);
                Log.d(TAG, "House updated in both Firebase and SQLite");
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
                Log.e(TAG, "Failed to update house in Firebase: " + error);
            }
        });
    }

    // ==================== DELETE HOUSE ====================

    public void deleteHouse(String houseId, final RepositoryCallback<Boolean> callback) {
        firestoreHelper.deleteHouse(houseId, new FirestoreHelper.FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                databaseHelper.deleteHouse(houseId);
                callback.onSuccess(true);
                Log.d(TAG, "House deleted in both Firebase and SQLite");
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
                Log.e(TAG, "Failed to delete house in Firebase: " + error);
            }
        });
    }

    // ==================== FAVORITE OPERATIONS ====================

    public void addFavorite(String userId, String houseId, final RepositoryCallback<Boolean> callback) {
        long result = databaseHelper.addFavorite(userId, houseId);
        if (result != -1) {
            callback.onSuccess(true);
            Log.d(TAG, "Favorite added in SQLite");
        } else {
            callback.onError("Failed to add favorite");
        }
    }

    public void removeFavorite(String userId, String houseId, final RepositoryCallback<Boolean> callback) {
        int result = databaseHelper.removeFavorite(userId, houseId);
        if (result > 0) {
            callback.onSuccess(true);
            Log.d(TAG, "Favorite removed from SQLite");
        } else {
            callback.onError("Failed to remove favorite");
        }
    }

    public boolean isFavorite(String userId, String houseId) {
        return databaseHelper.isFavorite(userId, houseId);
    }

    public List<String> getUserFavorites(String userId) {
        return databaseHelper.getUserFavorites(userId);
    }

    // ==================== CALLBACK INTERFACE ====================

    public interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}
