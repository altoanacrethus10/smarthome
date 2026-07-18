package com.example.smarthome.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.smarthome.R;
import com.example.smarthome.constants.AppConstants;
import com.example.smarthome.databinding.ActivityMainBinding;
import com.example.smarthome.database.remote.AuthenticationHelper;
import com.example.smarthome.fragments.BrowseHousesFragment;
import com.example.smarthome.fragments.DashboardFragment;
import com.example.smarthome.fragments.FavoritesFragment;
import com.example.smarthome.fragments.MyListingsFragment;
import com.example.smarthome.utils.NotificationUtils;
import com.example.smarthome.utils.PermissionHelper;
import com.example.smarthome.utils.SharedPrefManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final String EXTRA_OPEN_SECTION = "open_section";
    private ActivityMainBinding binding;
    private SharedPrefManager sharedPrefManager;
    private boolean isNavigatingInternally = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sharedPrefManager = SharedPrefManager.getInstance(this);

        // Initialize UI
        setSupportActionBar(binding.toolbar);
        setupNavigationDrawer();
        setupBottomNavigation();
        updateNavHeader();

        NotificationUtils.createNotificationChannel(this);
        ensureCommunicationPermissions();

        // Load initial state
        if (savedInstanceState == null) {
            openRequestedSection(getIntent());
        }
    }

    private void setupNavigationDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, binding.drawerLayout, binding.toolbar,
                R.string.nav_dashboard, R.string.nav_dashboard
        );
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Load role-specific drawer menu
        binding.navView.getMenu().clear();
        if (sharedPrefManager.isOwner()) {
            binding.navView.inflateMenu(R.menu.drawer_menu_owner);
        } else {
            binding.navView.inflateMenu(R.menu.drawer_menu_tenant);
        }
        
        binding.navView.setNavigationItemSelectedListener(this);
    }

    private void setupBottomNavigation() {
        // Clear and inflate correct menu based on role
        binding.bottomNav.getMenu().clear();
        if (sharedPrefManager.isOwner()) {
            binding.bottomNav.inflateMenu(R.menu.bottom_nav_owner);
        } else {
            binding.bottomNav.inflateMenu(R.menu.bottom_nav_tenant);
        }

        binding.bottomNav.setOnItemSelectedListener(item -> {
            if (isNavigatingInternally) return true;
            return handleNavigation(item.getItemId(), false);
        });
    }

    private void updateNavHeader() {
        View headerView = binding.navView.getHeaderView(0);
        if (headerView == null) return;

        TextView tvName = headerView.findViewById(R.id.nav_user_name);
        TextView tvType = headerView.findViewById(R.id.nav_user_type);
        ImageView ivAvatar = headerView.findViewById(R.id.nav_user_avatar);
        View btnEdit = headerView.findViewById(R.id.nav_edit_profile);

        String name = sharedPrefManager.getUserName();
        String userType = sharedPrefManager.getUserType();
        String avatarUrl = sharedPrefManager.getUserAvatar();

        if (tvName != null) tvName.setText(name != null ? name : "User");
        if (tvType != null) tvType.setText(sharedPrefManager.isOwner() ? "Home Owner" : "Tenant");

        if (ivAvatar != null) {
            com.bumptech.glide.Glide.with(this)
                    .load(avatarUrl != null && !avatarUrl.isEmpty() ? avatarUrl : R.drawable.ic_user)
                    .circleCrop()
                    .into(ivAvatar);
        }

        if (btnEdit != null) {
            btnEdit.setOnClickListener(v -> {
                startActivity(new Intent(this, EditProfileActivity.class));
                binding.drawerLayout.closeDrawer(GravityCompat.START);
            });
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        boolean handled = handleNavigation(item.getItemId(), true);
        binding.drawerLayout.closeDrawer(GravityCompat.START);
        return handled;
    }

    public boolean handleNavigation(int id, boolean fromDrawer) {
        boolean isOwner = sharedPrefManager.isOwner();

        // 1. Fragment Navigation
        if (id == R.id.nav_dashboard || id == R.id.nav_tenant_home) {
            loadFragment(new DashboardFragment(), isOwner ? "Dashboard" : "Home");
            syncSelection(id, fromDrawer);
            return true;
        } else if (id == R.id.nav_browse_houses) {
            loadFragment(new BrowseHousesFragment(), "Browse Houses");
            syncSelection(id, fromDrawer);
            return true;
        } else if (id == R.id.nav_my_listings || id == R.id.nav_properties) {
            loadFragment(new MyListingsFragment(), "My Listings");
            syncSelection(id, fromDrawer);
            return true;
        }

        // 2. Activity Navigation
        if (id == R.id.nav_favorites || id == R.id.nav_tenant_saved) {
            startActivity(new Intent(this, SavedPropertiesActivity.class));
            syncSelection(id, fromDrawer);
            return true;
        } else if (id == R.id.nav_bookings) {
            if (isOwner) startActivity(new Intent(this, BookingsActivity.class));
            else startActivity(new Intent(this, MyBookingsActivity.class));
            syncSelection(id, fromDrawer);
            return true;
        } else if (id == R.id.nav_account) {
            startActivity(new Intent(this, AccountActivity.class));
            syncSelection(id, fromDrawer);
            return true;
        } else if (id == R.id.nav_payments) {
            startActivity(new Intent(this, ReportActivity.class));
            return true;
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.nav_help) {
            startActivity(new Intent(this, HelpActivity.class));
            return true;
        } else if (id == R.id.nav_feedback) {
            startActivity(new Intent(this, UserFeedbackActivity.class));
            return true;
        } else if (id == R.id.nav_contact) {
            startActivity(new Intent(this, ContactActivity.class));
            return true;
        } else if (id == R.id.nav_complaints) {
            startActivity(new Intent(this, ComplaintsActivity.class));
            return true;
        } else if (id == R.id.nav_logout) {
            logout();
            return true;
        }

        return false;
    }

    private void syncSelection(int id, boolean fromDrawer) {
        isNavigatingInternally = true;
        if (fromDrawer) {
            binding.bottomNav.setSelectedItemId(id);
        } else {
            binding.navView.setCheckedItem(id);
        }
        isNavigatingInternally = false;
    }

    private void openRequestedSection(Intent intent) {
        String section = intent != null ? intent.getStringExtra(EXTRA_OPEN_SECTION) : null;
        int id = R.id.nav_dashboard;
        if ("browse".equals(section)) id = R.id.nav_browse_houses;
        else if ("favorites".equals(section)) id = R.id.nav_favorites;
        else if ("listings".equals(section)) id = R.id.nav_my_listings;
        else if ("bookings".equals(section)) id = R.id.nav_bookings;
        
        handleNavigation(id, true);
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
        if (sharedPrefManager.isOwner()) {
            getMenuInflater().inflate(R.menu.menu_owner_overflow, menu);
        } else {
            getMenuInflater().inflate(R.menu.menu_tenant_overflow, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add_property) {
            startActivity(new Intent(this, AddListingActivity.class));
            return true;
        } else if (id == R.id.action_edit_profile) {
            startActivity(new Intent(this, EditProfileActivity.class));
            return true;
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_help) {
            startActivity(new Intent(this, HelpActivity.class));
            return true;
        } else if (id == R.id.action_share) {
            shareApp();
            return true;
        } else if (id == R.id.action_notifications) {
            startActivity(new Intent(this, NotificationsActivity.class));
            return true;
        } else if (id == R.id.action_favorites) {
            startActivity(new Intent(this, SavedPropertiesActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
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

    private void logout() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Logout")
                .setMessage(R.string.logout_confirmation)
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Yes, Logout", (dialog, which) -> {
                    AuthenticationHelper.getInstance(this).logout();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finishAffinity();
                })
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateNotificationBadge();
    }

    private void updateNotificationBadge() {
        // Show badge on profile/settings icon in bottom nav as requested
        int settingsTabId = sharedPrefManager.isOwner() ? R.id.nav_account : R.id.nav_tenant_profile;
        com.google.android.material.badge.BadgeDrawable badge = binding.bottomNav.getOrCreateBadge(settingsTabId);
        badge.setVisible(true);
        badge.setNumber(2); // Mock count for demo
    }
}
