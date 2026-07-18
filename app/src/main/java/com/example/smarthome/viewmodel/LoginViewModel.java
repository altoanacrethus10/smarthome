package com.example.smarthome.viewmodel;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.smarthome.database.remote.AuthenticationHelper;
import com.example.smarthome.models.User;
import com.example.smarthome.repository.UserRepository;
import com.example.smarthome.utils.SharedPrefManager;

public class LoginViewModel extends AndroidViewModel {
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>(false);
    private final MutableLiveData<User> currentUser = new MutableLiveData<>();

    private AuthenticationHelper authHelper;
    private UserRepository userRepository;
    private SharedPrefManager sharedPrefManager;

    public LoginViewModel(Application application) {
        super(application);
        Context context = application.getApplicationContext();
        authHelper = AuthenticationHelper.getInstance(context);
        userRepository = UserRepository.getInstance(context);
        sharedPrefManager = SharedPrefManager.getInstance(context);

        // Check if user is already logged in
        if (authHelper.isLoggedIn()) {
            loadUserData();
        }
    }

    public void login(String email, String password) {
        if (email.isEmpty()) {
            errorMessage.setValue("Email is required");
            return;
        }

        if (password.isEmpty()) {
            errorMessage.setValue("Password is required");
            return;
        }

        if (password.length() < 6) {
            errorMessage.setValue("Password must be at least 6 characters");
            return;
        }

        isLoading.setValue(true);
        errorMessage.setValue(null);

        authHelper.login(email, password, new AuthenticationHelper.AuthCallback() {
            @Override
            public void onSuccess() {
                isLoading.setValue(false);
                loginSuccess.setValue(true);
                loadUserData();
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }

    public void register(String name, String email, String phone, String password, 
                         String confirmPassword, String userType) {
        if (name.isEmpty()) {
            errorMessage.setValue("Name is required");
            return;
        }

        if (email.isEmpty() || !email.contains("@")) {
            errorMessage.setValue("Valid email is required");
            return;
        }

        if (phone.isEmpty() || phone.length() < 10) {
            errorMessage.setValue("Valid phone number is required");
            return;
        }

        if (password.isEmpty() || password.length() < 6) {
            errorMessage.setValue("Password must be at least 6 characters");
            return;
        }

        if (!password.equals(confirmPassword)) {
            errorMessage.setValue("Passwords do not match");
            return;
        }

        isLoading.setValue(true);
        errorMessage.setValue(null);

        User newUser = new User();
        newUser.setName(name);
        newUser.setEmail(email);
        newUser.setPhone(phone);
        newUser.setUserType(userType);
        newUser.setCreatedAt(String.valueOf(System.currentTimeMillis()));

        authHelper.register(newUser, password, new AuthenticationHelper.AuthCallback() {
            @Override
            public void onSuccess() {
                isLoading.setValue(false);
                loginSuccess.setValue(true);
                loadUserData();
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }

    public void resetPassword(String email) {
        if (email.isEmpty() || !email.contains("@")) {
            errorMessage.setValue("Valid email is required");
            return;
        }

        isLoading.setValue(true);
        errorMessage.setValue(null);

        authHelper.resetPassword(email, new AuthenticationHelper.AuthCallback() {
            @Override
            public void onSuccess() {
                isLoading.setValue(false);
                errorMessage.setValue("Password reset link sent to your email");
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }

    private void loadUserData() {
        String userId = authHelper.getCurrentUserId();
        if (userId != null) {
            userRepository.getUser(userId, new UserRepository.RepositoryCallback<User>() {
                @Override
                public void onSuccess(User user) {
                    currentUser.setValue(user);
                    if (user != null) {
                        sharedPrefManager.saveUser(
                            user.getId(),
                            user.getName(),
                            user.getEmail(),
                            user.getPhone(),
                            user.getUserType()
                        );
                    }
                }

                @Override
                public void onError(String error) {
                    // User might not exist in Firestore yet
                    // Use SharedPref data as fallback
                    String name = sharedPrefManager.getUserName();
                    String email = sharedPrefManager.getUserEmail();
                    String phone = sharedPrefManager.getUserPhone();
                    String userType = sharedPrefManager.getUserType();
                    
                    if (name != null) {
                        User fallbackUser = new User(userId, name, email, phone, userType, null, null);
                        currentUser.setValue(fallbackUser);
                    }
                }
            });
        }
    }

    public void logout() {
        authHelper.logout();
        loginSuccess.setValue(false);
        currentUser.setValue(null);
    }

    // Getters for LiveData
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getLoginSuccess() { return loginSuccess; }
    public LiveData<User> getCurrentUser() { return currentUser; }

    public boolean isLoggedIn() {
        return authHelper.isLoggedIn();
    }
}
