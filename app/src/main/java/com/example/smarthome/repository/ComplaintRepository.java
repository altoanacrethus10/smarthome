package com.example.smarthome.repository;

import android.content.Context;
import android.util.Log;

import com.example.smarthome.database.remote.FirestoreHelper;
import com.example.smarthome.models.Complaint;

import java.util.List;

public class ComplaintRepository {
    private static final String TAG = "ComplaintRepository";
    private static ComplaintRepository instance;
    private FirestoreHelper firestoreHelper;

    private ComplaintRepository(Context context) {
        firestoreHelper = FirestoreHelper.getInstance();
    }

    public static synchronized ComplaintRepository getInstance(Context context) {
        if (instance == null) {
            instance = new ComplaintRepository(context);
        }
        return instance;
    }

    public void createComplaint(Complaint complaint, final RepositoryCallback<String> callback) {
        firestoreHelper.createComplaint(complaint, new FirestoreHelper.FirestoreCallback<String>() {
            @Override
            public void onSuccess(String result) {
                callback.onSuccess(result);
                Log.d(TAG, "Complaint submitted to Firebase");
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
                Log.e(TAG, "Failed to submit complaint: " + error);
            }
        });
    }

    public void getComplaintsByUser(String userId, final RepositoryCallback<List<Complaint>> callback) {
        firestoreHelper.getComplaintsByUser(userId, new FirestoreHelper.FirestoreCallback<List<Complaint>>() {
            @Override
            public void onSuccess(List<Complaint> result) {
                callback.onSuccess(result);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public void getComplaintsByOwner(String ownerId, final RepositoryCallback<List<Complaint>> callback) {
        firestoreHelper.getComplaintsByOwner(ownerId, new FirestoreHelper.FirestoreCallback<List<Complaint>>() {
            @Override
            public void onSuccess(List<Complaint> result) {
                callback.onSuccess(result);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public void updateComplaintStatus(String complaintId, String status, String response, final RepositoryCallback<Void> callback) {
        firestoreHelper.updateComplaintStatus(complaintId, status, response, new FirestoreHelper.FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                callback.onSuccess(result);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}
