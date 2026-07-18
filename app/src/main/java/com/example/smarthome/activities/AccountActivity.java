package com.example.smarthome.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.smarthome.R;
import com.example.smarthome.database.remote.AuthenticationHelper;
import com.example.smarthome.database.remote.FirebaseStorageHelper;
import com.example.smarthome.models.Booking;
import com.example.smarthome.repository.BookingRepository;
import com.example.smarthome.repository.HouseRepository;
import com.example.smarthome.repository.UserRepository;
import com.example.smarthome.utils.SharedPrefManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

public class AccountActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 100;

    private ImageView ivProfileImage;
    private TextView tvAccountName, tvAccountEmail, tvAccountPhone;
    private MaterialButton btnEditProfile, btnLogout;
    private ProgressBar accountProgress;

    private SharedPrefManager sharedPrefManager;
    private UserRepository userRepository;
    private BookingRepository bookingRepository;
    private HouseRepository houseRepository;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        sharedPrefManager = SharedPrefManager.getInstance(this);
        userRepository = UserRepository.getInstance(this);
        bookingRepository = BookingRepository.getInstance(this);
        houseRepository = HouseRepository.getInstance(this);

        setupToolbar();
        initViews();
        setupMenu();
        loadUserData();
        loadStats();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Profile");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initViews() {
        ivProfileImage = findViewById(R.id.iv_profile_image);
        tvAccountName = findViewById(R.id.tv_account_name);
        tvAccountEmail = findViewById(R.id.tv_account_email);
        tvAccountPhone = findViewById(R.id.tv_account_phone);
        btnEditProfile = findViewById(R.id.btn_edit_profile);
        btnLogout = findViewById(R.id.btn_logout);
        accountProgress = findViewById(R.id.account_progress);

        findViewById(R.id.btn_change_photo).setOnClickListener(v -> openImagePicker());
        btnEditProfile.setOnClickListener(v -> startActivity(new Intent(this, EditProfileActivity.class)));
        btnLogout.setOnClickListener(v -> confirmLogout());
    }

    private void setupMenu() {
        // My Bookings
        setupMenuItem(R.id.menu_my_bookings, R.drawable.ic_home, "My Bookings", v -> 
            startActivity(new Intent(this, MyBookingsActivity.class)));

        // Saved Properties
        setupMenuItem(R.id.menu_saved_properties, R.drawable.ic_favorite, "Saved Properties", v -> 
            startActivity(new Intent(this, SavedPropertiesActivity.class)));

        // Payment History
        setupMenuItem(R.id.menu_payment_history, R.drawable.ic_check, "Payment History", v -> 
            Toast.makeText(this, "Payment History coming soon", Toast.LENGTH_SHORT).show());

        // Notifications
        setupMenuItem(R.id.menu_notifications, R.drawable.ic_notifications, "Notifications", v -> 
            Toast.makeText(this, "Notifications coming soon", Toast.LENGTH_SHORT).show());

        // Settings
        setupMenuItem(R.id.menu_settings, R.drawable.ic_settings, "Settings", v -> 
            startActivity(new Intent(this, SettingsActivity.class)));

        // Help
        setupMenuItem(R.id.menu_help, R.drawable.ic_help, "Help & Support", v -> 
            startActivity(new Intent(this, HelpActivity.class)));

        // Share
        setupMenuItem(R.id.menu_share, R.drawable.ic_share, "Share App", v -> shareApp());
    }

    private void setupMenuItem(int id, int icon, String label, View.OnClickListener listener) {
        View view = findViewById(id);
        if (view != null) {
            ImageView ivIcon = view.findViewById(R.id.iv_menu_icon);
            TextView tvLabel = view.findViewById(R.id.tv_menu_label);
            if (ivIcon != null) ivIcon.setImageResource(icon);
            if (tvLabel != null) tvLabel.setText(label);
            view.setOnClickListener(listener);
        }
    }

    private void loadUserData() {
        tvAccountName.setText(sharedPrefManager.getUserName());
        tvAccountEmail.setText(sharedPrefManager.getUserEmail());
        tvAccountPhone.setText(sharedPrefManager.getUserPhone());

        String avatarUrl = sharedPrefManager.getUserAvatar();
        Glide.with(this)
                .load(avatarUrl != null && !avatarUrl.isEmpty() ? avatarUrl : R.drawable.ic_user)
                .placeholder(R.drawable.ic_user)
                .circleCrop()
                .into(ivProfileImage);
    }

    private void loadStats() {
        String userId = sharedPrefManager.getUserId();
        if (userId == null) return;

        // Total Bookings & Active
        bookingRepository.getBookingsByTenant(userId, new BookingRepository.RepositoryCallback<List<Booking>>() {
            @Override
            public void onSuccess(List<Booking> bookings) {
                int total = bookings != null ? bookings.size() : 0;
                int active = 0;
                if (bookings != null) {
                    for (Booking b : bookings) {
                        if ("pending".equalsIgnoreCase(b.getStatus()) || "confirmed".equalsIgnoreCase(b.getStatus())) {
                            active++;
                        }
                    }
                }
                updateStat(R.id.stat_total_bookings, String.valueOf(total), "Total Bookings", R.drawable.ic_home);
                updateStat(R.id.stat_active_bookings, String.valueOf(active), "Active", R.drawable.ic_check);
            }

            @Override
            public void onError(String error) {}
        });

        // Saved Properties
        int favorites = houseRepository.getUserFavorites(userId).size();
        updateStat(R.id.stat_favorites, String.valueOf(favorites), "Saved", R.drawable.ic_favorite);
    }

    private void updateStat(int id, String value, String label, int icon) {
        View view = findViewById(id);
        if (view != null) {
            TextView tvValue = view.findViewById(R.id.tv_stat_value);
            TextView tvLabel = view.findViewById(R.id.tv_stat_label);
            ImageView ivIcon = view.findViewById(R.id.iv_stat_icon);
            if (tvValue != null) tvValue.setText(value);
            if (tvLabel != null) tvLabel.setText(label);
            if (ivIcon != null) ivIcon.setImageResource(icon);
            
            view.setOnClickListener(v -> {
                if (label.contains("Bookings") || label.contains("Active")) {
                    startActivity(new Intent(this, MyBookingsActivity.class));
                } else if (label.contains("Saved")) {
                    startActivity(new Intent(this, SavedPropertiesActivity.class));
                }
            });
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Profile Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            uploadImage();
        }
    }

    private void uploadImage() {
        if (selectedImageUri == null) return;
        
        accountProgress.setVisibility(View.VISIBLE);
        FirebaseStorageHelper.getInstance().uploadImage(selectedImageUri, "profiles", new FirebaseStorageHelper.StorageCallback<String>() {
            @Override
            public void onSuccess(String imageUrl) {
                updateUserAvatar(imageUrl);
            }

            @Override
            public void onError(String error) {
                accountProgress.setVisibility(View.GONE);
                Toast.makeText(AccountActivity.this, "Upload failed: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUserAvatar(String imageUrl) {
        userRepository.updateUserAvatar(sharedPrefManager.getUserId(), imageUrl, new UserRepository.RepositoryCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                accountProgress.setVisibility(View.GONE);
                sharedPrefManager.setUserAvatar(imageUrl);
                loadUserData();
                Toast.makeText(AccountActivity.this, "Profile picture updated", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                accountProgress.setVisibility(View.GONE);
                Toast.makeText(AccountActivity.this, "Update failed: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void shareApp() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "SmartHome App");
        intent.putExtra(Intent.EXTRA_TEXT, "Hey! Check out SmartHome app to find your dream house: https://smarthome.example.com");
        startActivity(Intent.createChooser(intent, "Share via"));
    }

    private void confirmLogout() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> logout())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void logout() {
        AuthenticationHelper.getInstance(this).logout();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
        loadStats();
    }
}
