package com.example.smarthome.database.remote;

import android.content.Context;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseUser;
import com.example.smarthome.models.User;
import com.example.smarthome.utils.SharedPrefManager;

public class AuthenticationHelper {
    private static AuthenticationHelper instance;
    private FirebaseAuth firebaseAuth;
    private FirebaseService firebaseService;
    private FirestoreHelper firestoreHelper;
    private SharedPrefManager sharedPrefManager;

    private AuthenticationHelper(Context context) {
        firebaseService = FirebaseService.getInstance();
        firebaseAuth = firebaseService.getFirebaseAuth();
        firestoreHelper = FirestoreHelper.getInstance();
        sharedPrefManager = SharedPrefManager.getInstance(context);
    }

    public static synchronized AuthenticationHelper getInstance(Context context) {
        if (instance == null) {
            instance = new AuthenticationHelper(context);
        }
        return instance;
    }

    public void login(String email, String password, final AuthCallback callback) {
        firebaseService.loginUser(email, password, new FirebaseService.AuthCallback() {
            @Override
            public void onSuccess() {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null) {
                    // Fetch user profile from Firestore
                    firestoreHelper.getUser(firebaseUser.getUid(), new FirestoreHelper.FirestoreCallback<User>() {
                        @Override
                        public void onSuccess(User user) {
                            // Save user in SharedPreferences
                            sharedPrefManager.saveUser(
                                user.getId(),
                                user.getName(),
                                user.getEmail(),
                                user.getPhone(),
                                user.getUserType()
                            );
                            callback.onSuccess();
                        }

                        @Override
                        public void onError(String error) {
                            // Fallback if firestore fetch fails but auth succeeded
                            sharedPrefManager.saveUser(
                                firebaseUser.getUid(),
                                "User",
                                firebaseUser.getEmail(),
                                "",
                                "tenant"
                            );
                            callback.onSuccess();
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public void register(User user, String password, final AuthCallback callback) {
        firebaseService.registerUser(user.getEmail(), password, new FirebaseService.AuthCallback() {
            @Override
            public void onSuccess() {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null) {
                    user.setId(firebaseUser.getUid());
                    // Create user in Firestore
                    firestoreHelper.createUser(user, new FirestoreHelper.FirestoreCallback<String>() {
                        @Override
                        public void onSuccess(String result) {
                            // Save user in SharedPreferences
                            sharedPrefManager.saveUser(
                                user.getId(),
                                user.getName(),
                                user.getEmail(),
                                user.getPhone(),
                                user.getUserType()
                            );
                            callback.onSuccess();
                        }

                        @Override
                        public void onError(String error) {
                            callback.onError(error);
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public void resetPassword(String email, final AuthCallback callback) {
        firebaseService.resetPassword(email, new FirebaseService.AuthCallback() {
            @Override
            public void onSuccess() {
                callback.onSuccess();
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public void signInWithGoogle(AuthCredential credential, final AuthCallback callback) {
        firebaseService.signInWithCredential(credential, new FirebaseService.AuthCallback() {
            @Override
            public void onSuccess() {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser == null) {
                    callback.onError("Google sign-in failed. Please try again.");
                    return;
                }

                firestoreHelper.getUser(firebaseUser.getUid(), new FirestoreHelper.FirestoreCallback<User>() {
                    @Override
                    public void onSuccess(User user) {
                        saveSignedInUser(user);
                        callback.onSuccess();
                    }

                    @Override
                    public void onError(String error) {
                        User user = createGoogleUser(firebaseUser);
                        firestoreHelper.createUser(user, new FirestoreHelper.FirestoreCallback<String>() {
                            @Override
                            public void onSuccess(String result) {
                                saveSignedInUser(user);
                                callback.onSuccess();
                            }

                            @Override
                            public void onError(String error) {
                                saveSignedInUser(user);
                                callback.onSuccess();
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    private User createGoogleUser(FirebaseUser firebaseUser) {
        String now = String.valueOf(System.currentTimeMillis());
        String name = firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "Google User";
        String email = firebaseUser.getEmail() != null ? firebaseUser.getEmail() : "";
        String avatarUrl = firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : "";
        return new User(
                firebaseUser.getUid(),
                name,
                email,
                "",
                "tenant",
                avatarUrl,
                now
        );
    }

    private void saveSignedInUser(User user) {
        sharedPrefManager.saveUser(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getUserType()
        );
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().trim().isEmpty()) {
            sharedPrefManager.setUserAvatar(user.getAvatarUrl());
        }
    }

    public void logout() {
        firebaseService.signOut();
        sharedPrefManager.logout();
    }

    public boolean isLoggedIn() {
        return firebaseAuth.getCurrentUser() != null && sharedPrefManager.isLoggedIn();
    }

    public String getCurrentUserId() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public interface AuthCallback {
        void onSuccess();
        void onError(String error);
    }
}
