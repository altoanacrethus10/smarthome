package com.example.smarthome.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.smarthome.R;
import com.example.smarthome.constants.AppConstants;
import com.example.smarthome.databinding.ActivityMainBinding;
import com.example.smarthome.fragments.BrowseHousesFragment;
import com.example.smarthome.fragments.DashboardFragment;
import com.example.smarthome.fragments.MyBookingsFragment;
import com.example.smarthome.fragments.MyListingsFragment;
import com.example.smarthome.fragments.OwnerBookingsFragment;
import com.example.smarthome.fragments.ProfileFragment;
import com.example.smarthome.fragments.SavedPropertiesFragment;
import com.example.smarthome.utils.NotificationUtils;
import com.example.smarthome.utils.PermissionHelper;
import com.example.smarthome.utils.SharedPrefManager;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_OPEN_SECTION = "open_section";

    private ActivityMainBinding binding;
    private SharedPrefManager sharedPrefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sharedPrefManager = SharedPrefManager.getInstance(this);

        setSupportActionBar(binding.toolbar);
        setupBottomNavigation();

        NotificationUtils.createNotificationChannel(this);
        ensureCommunicationPermissions();

        if (savedInstanceState == null) {
            openRequestedSection(getIntent());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        openRequestedSection(intent);
    }

    private void setupBottomNavigation() {
        binding.bottomNav.getMenu().clear();
        if (sharedPrefManager.isOwner()) {
            binding.bottomNav.inflateMenu(R.menu.bottom_nav_owner);
        } else {
            binding.bottomNav.inflateMenu(R.menu.bottom_nav_tenant);
        }

        binding.bottomNav.setOnItemSelectedListener(item -> handleNavigation(item.getItemId()));
        // Reselecting the current tab should not reload the fragment.
        binding.bottomNav.setOnItemReselectedListener(item -> { });
    }

    /** Switches the visible tab programmatically (used by fragments and deep links). */
    public void openTab(int menuItemId) {
        binding.bottomNav.setSelectedItemId(menuItemId);
    }

    private boolean handleNavigation(int id) {
        boolean isOwner = sharedPrefManager.isOwner();

        Fragment fragment;
        String title;

        if (id == R.id.nav_home) {
            fragment = new DashboardFragment();
            title = isOwner ? "Dashboard" : "Home";
        } else if (id == R.id.nav_houses) {
            fragment = isOwner ? new MyListingsFragment() : new BrowseHousesFragment();
            title = isOwner ? "My Properties" : "Browse Houses";
        } else if (id == R.id.nav_saved) {
            fragment = new SavedPropertiesFragment();
            title = "Saved Properties";
        } else if (id == R.id.nav_bookings) {
            fragment = isOwner ? new OwnerBookingsFragment() : new MyBookingsFragment();
            title = isOwner ? "Booking Requests" : "My Bookings";
        } else if (id == R.id.nav_profile) {
            fragment = new ProfileFragment();
            title = "Profile";
        } else {
            return false;
        }

        loadFragment(fragment, title);
        return true;
    }

    private void openRequestedSection(Intent intent) {
        String section = intent != null ? intent.getStringExtra(EXTRA_OPEN_SECTION) : null;
        int id;
        if ("browse".equals(section) || "listings".equals(section)) {
            id = R.id.nav_houses;
        } else if ("bookings".equals(section)) {
            id = R.id.nav_bookings;
        } else if ("favorites".equals(section) || "saved".equals(section)) {
            // Owner has no saved tab; avoid a broken deep link.
            id = sharedPrefManager.isOwner() ? R.id.nav_home : R.id.nav_saved;
        } else if ("profile".equals(section)) {
            id = R.id.nav_profile;
        } else {
            id = R.id.nav_home;
        }

        if (binding.bottomNav.getSelectedItemId() == id) {
            // setSelectedItemId is a no-op when the item is already selected, so load explicitly.
            handleNavigation(id);
        } else {
            binding.bottomNav.setSelectedItemId(id);
        }
    }

    private void loadFragment(Fragment fragment, String title) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_overflow, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_share) {
            shareApp();
            return true;
        } else if (id == R.id.action_notifications) {
            startActivity(new Intent(this, NotificationsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.bottomNav.getOrCreateBadge(R.id.nav_profile).setVisible(true);
        binding.bottomNav.getOrCreateBadge(R.id.nav_profile).setNumber(2);
    }

    private void ensureCommunicationPermissions() {
        String[] permissions = PermissionHelper.getAllPermissions();
        if (!PermissionHelper.hasPermissions(this, permissions)) {
            PermissionHelper.requestPermissions(this, permissions, AppConstants.REQUEST_ALL_PERMISSIONS);
        }
    }

    private void shareApp() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out SmartHome! https://play.google.com/store/apps/details?id=com.example.smarthome");
        startActivity(Intent.createChooser(shareIntent, "Share SmartHome"));
    }
}
