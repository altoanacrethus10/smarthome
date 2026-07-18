package com.example.smarthome.database.remote;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

public class FirebaseStorageHelper {
    private static final String TAG = "FirebaseStorageHelper";
    private static FirebaseStorageHelper instance;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    private FirebaseStorageHelper() {
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
    }

    public static synchronized FirebaseStorageHelper getInstance() {
        if (instance == null) {
            instance = new FirebaseStorageHelper();
        }
        return instance;
    }

    /**
     * Upload an image to Firebase Storage with a specific path
     * @param imageUri Local URI of the image
     * @param path Full path in storage (e.g., "property_images/ownerId/propertyId/timestamp.jpg")
     * @param callback Success returns the download URL
     */
    public void uploadImageWithPath(Uri imageUri, String path, final StorageCallback<String> callback) {
        if (imageUri == null) {
            callback.onError("Image URI is null");
            return;
        }

        final StorageReference ref = storageRef.child(path);
        Log.d(TAG, "Uploading image to storage path: " + path);

        ref.putFile(imageUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return ref.getDownloadUrl();
                })
                .addOnSuccessListener(uri -> {
                    callback.onSuccess(uri.toString());
                    Log.d(TAG, "Image uploaded successfully: " + uri);
                })
                .addOnFailureListener(e -> {
                    String message = e.getMessage() != null ? e.getMessage() : "Failed to upload image";
                    callback.onError(message);
                    Log.e(TAG, "Failed to upload image: " + message);
                });
    }

    public void uploadImageWithPath(byte[] data, String path, final StorageCallback<String> callback) {
        if (data == null) {
            callback.onError("Data is null");
            return;
        }

        final StorageReference ref = storageRef.child(path);

        ref.putBytes(data)
                .addOnSuccessListener(taskSnapshot -> taskSnapshot.getStorage().getDownloadUrl()
                        .addOnSuccessListener(uri -> callback.onSuccess(uri.toString()))
                        .addOnFailureListener(e -> callback.onError(e.getMessage() != null ? e.getMessage() : "Failed to get download URL")))
                .addOnFailureListener(e -> callback.onError(e.getMessage() != null ? e.getMessage() : "Failed to upload image"));
    }

    /**
     * Upload an image to Firebase Storage
     * @param imageUri Local URI of the image
     * @param folder Folder name in storage (e.g., "houses" or "profiles")
     * @param callback Success returns the download URL
     */
    public void uploadImage(Uri imageUri, String folder, final StorageCallback<String> callback) {
        String fileName = UUID.randomUUID().toString() + ".jpg";
        String path = normalizeFolder(folder) + "/" + fileName;
        uploadImageWithPath(imageUri, path, callback);
    }

    public void uploadImage(byte[] data, String folder, final StorageCallback<String> callback) {
        String fileName = UUID.randomUUID().toString() + ".jpg";
        String path = normalizeFolder(folder) + "/" + fileName;
        uploadImageWithPath(data, path, callback);
    }

    public void deleteImage(String imageUrl, final StorageCallback<Boolean> callback) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            callback.onSuccess(true);
            return;
        }

        try {
            StorageReference ref = storage.getReferenceFromUrl(imageUrl);
            ref.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    callback.onSuccess(true);
                    Log.d(TAG, "Image deleted successfully");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    callback.onError(e.getMessage());
                    Log.e(TAG, "Failed to delete image: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    private String normalizeFolder(String folder) {
        if (folder == null || folder.trim().isEmpty()) {
            return "";
        }
        String normalized = folder.trim();
        while (normalized.startsWith("/")) normalized = normalized.substring(1);
        while (normalized.endsWith("/")) normalized = normalized.substring(0, normalized.length() - 1);
        return normalized;
    }

    public interface StorageCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}
