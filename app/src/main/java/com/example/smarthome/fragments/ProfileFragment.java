package com.example.smarthome.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.smarthome.R;
import com.example.smarthome.activities.EditProfileActivity;
import com.example.smarthome.activities.HelpActivity;
import com.example.smarthome.activities.LoginActivity;
import com.example.smarthome.activities.MainActivity;
import com.example.smarthome.activities.NotificationsActivity;
import com.example.smarthome.activities.ReportActivity;
import com.example.smarthome.activities.SettingsActivity;
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

public class ProfileFragment extends Fragment {

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        sharedPrefManager = SharedPrefManager.getInstance(requireContext());
        userRepository = UserRepository.getInstance(requireContext());
        bookingRepository = BookingRepository.getInstance(requireContext());
        houseRepository = HouseRepository.getInstance(requireContext());

        initViews(view);
        setupMenu(view);
        loadUserData();
        loadStats();

        return view;
    }

    private void initViews(View view) {
        ivProfileImage = view.findViewById(R.id.iv_profile_image);
        tvAccountName = view.findViewById(R.id.tv_account_name);
        tvAccountEmail = view.findViewById(R.id.tv_account_email);
        tvAccountPhone = view.findViewById(R.id.tv_account_phone);
        btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        btnLogout = view.findViewById(R.id.btn_logout);
        accountProgress = view.findViewById(R.id.account_progress);

        view.findViewById(R.id.btn_change_photo).setOnClickListener(v -> openImagePicker());
        btnEditProfile.setOnClickListener(v -> startActivity(new Intent(getActivity(), EditProfileActivity.class)));
        btnLogout.setOnClickListener(v -> confirmLogout());
    }

    private void setupMenu(View view) {
        // My Bookings
        setupMenuItem(view, R.id.menu_my_bookings, R.drawable.ic_home, "My Bookings", v ->
                ((MainActivity) requireActivity()).openTab(R.id.nav_bookings));

        // Saved Properties
        setupMenuItem(view, R.id.menu_saved_properties, R.drawable.ic_favorite, "Saved Properties", v ->
                ((MainActivity) requireActivity()).openTab(R.id.nav_saved));

        // Payment History
        setupMenuItem(view, R.id.menu_payment_history, R.drawable.ic_check, "Payment History", v ->
                startActivity(new Intent(getActivity(), ReportActivity.class)));

        // Notifications
        setupMenuItem(view, R.id.menu_notifications, R.drawable.ic_notifications, "Notifications", v ->
                startActivity(new Intent(getActivity(), NotificationsActivity.class)));

        // Settings
        setupMenuItem(view, R.id.menu_settings, R.drawable.ic_settings, "Settings", v ->
                startActivity(new Intent(getActivity(), SettingsActivity.class)));

        // Help
        setupMenuItem(view, R.id.menu_help, R.drawable.ic_help, "Help & Support", v ->
                startActivity(new Intent(getActivity(), HelpActivity.class)));

        // Share
        setupMenuItem(view, R.id.menu_share, R.drawable.ic_share, "Share App", v -> shareApp());
    }

    private void setupMenuItem(View root, int id, int icon, String label, View.OnClickListener listener) {
        View view = root.findViewById(id);
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
        View root = getView();
        if (root == null) return;
        View view = root.findViewById(id);
        if (view != null) {
            TextView tvValue = view.findViewById(R.id.tv_stat_value);
            TextView tvLabel = view.findViewById(R.id.tv_stat_label);
            ImageView ivIcon = view.findViewById(R.id.iv_stat_icon);
            if (tvValue != null) tvValue.setText(value);
            if (tvLabel != null) tvLabel.setText(label);
            if (ivIcon != null) ivIcon.setImageResource(icon);

            view.setOnClickListener(v -> {
                if (label.contains("Bookings") || label.contains("Active")) {
                    ((MainActivity) requireActivity()).openTab(R.id.nav_bookings);
                } else if (label.contains("Saved")) {
                    ((MainActivity) requireActivity()).openTab(R.id.nav_saved);
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
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == android.app.Activity.RESULT_OK && data != null && data.getData() != null) {
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
                Toast.makeText(getContext(), "Upload failed: " + error, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getContext(), "Profile picture updated", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                accountProgress.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Update failed: " + error, Toast.LENGTH_SHORT).show();
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
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> logout())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void logout() {
        AuthenticationHelper.getInstance(requireContext()).logout();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finishAffinity();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserData();
        loadStats();
    }
}
