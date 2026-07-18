package com.example.smarthome.viewmodel;

import android.app.Application;
import android.content.Context;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.smarthome.models.User;
import com.example.smarthome.repository.UserRepository;
import com.example.smarthome.utils.SharedPrefManager;

public class AccountViewModel extends AndroidViewModel {
    private final MutableLiveData<User> user = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> updateSuccess = new MutableLiveData<>(false);

    private UserRepository userRepository;
    private SharedPrefManager sharedPrefManager;

    public AccountViewModel(Application application) {
        super(application);
        Context context = application.getApplicationContext();
        userRepository = UserRepository.getInstance(context);
        sharedPrefManager = SharedPrefManager.getInstance(context);

        loadUserProfile();
    }

    public void loadUserProfile() {
        String userId = sharedPrefManager.getUserId();
        if (userId == null) {
            errorMessage.setValue("User not logged in");
            return;
        }

        isLoading.setValue(true);
        errorMessage.setValue(null);

        userRepository.getUser(userId, new UserRepository.RepositoryCallback<User>() {
            @Override
            public void onSuccess(User userData) {
                user.setValue(userData);
                isLoading.setValue(false);
            }

            @Override
            public void onError(String error) {
                // Fallback to SharedPref data
                String name = sharedPrefManager.getUserName();
                String email = sharedPrefManager.getUserEmail();
                String phone = sharedPrefManager.getUserPhone();
                String userType = sharedPrefManager.getUserType();
                
                if (name != null) {
                    User fallbackUser = new User(userId, name, email, phone, userType, null, null);
                    user.setValue(fallbackUser);
                }
                isLoading.setValue(false);
                errorMessage.setValue("Failed to load profile: " + error);
            }
        });
    }

    public void updateProfile(String name, String phone, String avatarUrl) {
        User currentUser = user.getValue();
        if (currentUser == null) {
            errorMessage.setValue("No user data to update");
            return;
        }

        if (name.isEmpty()) {
            errorMessage.setValue("Name is required");
            return;
        }

        if (phone.isEmpty() || phone.length() < 10) {
            errorMessage.setValue("Valid phone number is required");
            return;
        }

        isLoading.setValue(true);
        errorMessage.setValue(null);
        updateSuccess.setValue(false);

        // Update user object
        currentUser.setName(name);
        currentUser.setPhone(phone);
        if (avatarUrl != null) {
            currentUser.setAvatarUrl(avatarUrl);
        }
        currentUser.setUpdatedAt(String.valueOf(System.currentTimeMillis()));

        userRepository.updateUser(currentUser, new UserRepository.RepositoryCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                // Update SharedPref
                sharedPrefManager.updateUser(name, phone);
                if (avatarUrl != null) {
                    sharedPrefManager.setUserAvatar(avatarUrl);
                }
                
                user.setValue(currentUser);
                isLoading.setValue(false);
                updateSuccess.setValue(true);
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue("Failed to update profile: " + error);
            }
        });
    }

    public void updateAvatar(String avatarUrl) {
        User currentUser = user.getValue();
        if (currentUser == null) return;

        currentUser.setAvatarUrl(avatarUrl);
        user.setValue(currentUser);
        
        // Update in SharedPref
        sharedPrefManager.setUserAvatar(avatarUrl);
        
        // Update in repository
        userRepository.updateUser(currentUser, new UserRepository.RepositoryCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                // Success
            }

            @Override
            public void onError(String error) {
                errorMessage.setValue("Failed to update avatar: " + error);
            }
        });
    }

    public void clearUpdateSuccess() {
        updateSuccess.setValue(false);
    }

    // Getters for LiveData
    public LiveData<User> getUser() { return user; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getUpdateSuccess() { return updateSuccess; }
}
