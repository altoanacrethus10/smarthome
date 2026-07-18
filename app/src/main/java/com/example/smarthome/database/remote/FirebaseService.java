package com.example.smarthome.database.remote;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.smarthome.constants.AppConstants;

public class FirebaseService {
    private static final String TAG = "FirebaseService";
    private static FirebaseService instance;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;

    private FirebaseService() {
        try {
            firebaseAuth = FirebaseAuth.getInstance();
            firestore = FirebaseFirestore.getInstance();
            currentUser = firebaseAuth.getCurrentUser();
        } catch (Exception e) {
            Log.e(TAG, "Firebase initialization failed: " + e.getMessage());
        }
    }

    public static synchronized FirebaseService getInstance() {
        if (instance == null) {
            instance = new FirebaseService();
        }
        return instance;
    }

    private boolean isFirebaseAvailable() {
        return firebaseAuth != null && firestore != null;
    }

    public FirebaseAuth getFirebaseAuth() {
        return firebaseAuth;
    }

    public FirebaseFirestore getFirestore() {
        return firestore;
    }

    public FirebaseUser getCurrentUser() {
        return firebaseAuth != null ? firebaseAuth.getCurrentUser() : null;
    }

    public boolean isUserLoggedIn() {
        return firebaseAuth != null && firebaseAuth.getCurrentUser() != null;
    }

    public String getCurrentUserId() {
        FirebaseUser user = getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public String getCurrentUserEmail() {
        FirebaseUser user = getCurrentUser();
        return user != null ? user.getEmail() : null;
    }

    public String getCurrentUserPhone() {
        FirebaseUser user = getCurrentUser();
        return user != null ? user.getPhoneNumber() : null;
    }

    // ==================== AUTHENTICATION METHODS ====================

    public void loginUser(String email, String password, final AuthCallback callback) {
        if (!isFirebaseAvailable()) {
            callback.onError("Firebase not available");
            return;
        }
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            currentUser = firebaseAuth.getCurrentUser();
                            callback.onSuccess();
                            Log.d(TAG, "Login successful");
                        } else {
                            String error = task.getException() != null ? 
                                    task.getException().getMessage() : "Login failed";
                            callback.onError(error);
                            Log.e(TAG, "Login failed: " + error);
                        }
                    }
                });
    }

    public void registerUser(String email, String password, final AuthCallback callback) {
        if (!isFirebaseAvailable()) {
            callback.onError("Firebase not available");
            return;
        }
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            currentUser = firebaseAuth.getCurrentUser();
                            callback.onSuccess();
                            Log.d(TAG, "Registration successful");
                        } else {
                            String error = task.getException() != null ? 
                                    task.getException().getMessage() : "Registration failed";
                            callback.onError(error);
                            Log.e(TAG, "Registration failed: " + error);
                        }
                    }
                });
    }

    public void resetPassword(String email, final AuthCallback callback) {
        if (!isFirebaseAvailable()) {
            callback.onError("Firebase not available");
            return;
        }
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            callback.onSuccess();
                            Log.d(TAG, "Password reset email sent");
                        } else {
                            String error = task.getException() != null ? 
                                    task.getException().getMessage() : "Reset failed";
                            callback.onError(error);
                            Log.e(TAG, "Password reset failed: " + error);
                        }
                    }
                });
    }

    public void signInWithCredential(AuthCredential credential, final AuthCallback callback) {
        if (!isFirebaseAvailable()) {
            callback.onError("Firebase not available");
            return;
        }
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            currentUser = firebaseAuth.getCurrentUser();
                            callback.onSuccess();
                            Log.d(TAG, "Credential sign-in successful");
                        } else {
                            String error = task.getException() != null ?
                                    task.getException().getMessage() : "Credential sign-in failed";
                            callback.onError(error);
                            Log.e(TAG, "Credential sign-in failed: " + error);
                        }
                    }
                });
    }

    public void signOut() {
        if (firebaseAuth != null) {
            firebaseAuth.signOut();
            currentUser = null;
            Log.d(TAG, "User signed out");
        }
    }

    // ==================== CALLBACK INTERFACE ====================

    public interface AuthCallback {
        void onSuccess();
        void onError(String error);
    }
}
