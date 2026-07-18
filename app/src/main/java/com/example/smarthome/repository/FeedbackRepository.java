package com.example.smarthome.repository;

import android.content.Context;
import android.util.Log;

import com.example.smarthome.database.remote.FirestoreHelper;
import com.example.smarthome.models.Feedback;

import java.util.List;

public class FeedbackRepository {
    private static final String TAG = "FeedbackRepository";
    private static FeedbackRepository instance;
    private FirestoreHelper firestoreHelper;

    private FeedbackRepository(Context context) {
        firestoreHelper = FirestoreHelper.getInstance();
    }

    public static synchronized FeedbackRepository getInstance(Context context) {
        if (instance == null) {
            instance = new FeedbackRepository(context);
        }
        return instance;
    }

    public void createFeedback(Feedback feedback, final RepositoryCallback<String> callback) {
        firestoreHelper.createFeedback(feedback, new FirestoreHelper.FirestoreCallback<String>() {
            @Override
            public void onSuccess(String result) {
                callback.onSuccess(result);
                Log.d(TAG, "Feedback submitted to Firebase");
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
                Log.e(TAG, "Failed to submit feedback: " + error);
            }
        });
    }

    public void getFeedbackByHouseId(String houseId, final RepositoryCallback<List<Feedback>> callback) {
        firestoreHelper.getFeedbackByHouseId(houseId, new FirestoreHelper.FirestoreCallback<List<Feedback>>() {
            @Override
            public void onSuccess(List<Feedback> result) {
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
