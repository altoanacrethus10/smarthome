package com.example.smarthome.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import com.example.smarthome.R;
import com.example.smarthome.database.remote.AuthenticationHelper;
import com.example.smarthome.utils.SharedPrefManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends AppCompatActivity {

    private SharedPrefManager sharedPrefManager;
    private SwitchMaterial switchBiometric, switchNotifPreview, switchPropertyAlerts, 
                           switchBookingAlerts, switchPaymentAlerts, switchPromoAlerts;
    private TextView tvAppVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sharedPrefManager = SharedPrefManager.getInstance(this);

        setupToolbar();
        initViews();
        setupMenu();
        loadSettings();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Settings");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initViews() {
        switchBiometric = findViewById(R.id.switch_biometric);
        switchNotifPreview = findViewById(R.id.switch_notification_preview);
        switchPropertyAlerts = findViewById(R.id.switch_property_alerts);
        switchBookingAlerts = findViewById(R.id.switch_booking_alerts);
        switchPaymentAlerts = findViewById(R.id.switch_payment_alerts);
        switchPromoAlerts = findViewById(R.id.switch_promo_alerts);
        tvAppVersion = findViewById(R.id.tv_app_version);

        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            tvAppVersion.setText(versionName);
        } catch (Exception e) {
            tvAppVersion.setText("1.0.0");
        }
    }

    private void setupMenu() {
        // Account Section
        setupMenuItem(R.id.setting_profile, R.drawable.ic_user, "Edit Profile", v -> 
            startActivity(new Intent(this, EditProfileActivity.class)));
        
        setupMenuItem(R.id.setting_change_password, R.drawable.ic_lock, "Change Password", v -> 
            startActivity(new Intent(this, ResetPasswordActivity.class)));

        // Preferences
        setupMenuItem(R.id.setting_language, R.drawable.ic_settings, "Language", v -> showLanguageDialog());
        setupMenuItem(R.id.setting_theme, R.drawable.ic_dark_mode, "Theme", v -> showThemeDialog());
        setupMenuItem(R.id.setting_currency, R.drawable.ic_check, "Currency", v -> showCurrencyDialog());

        // Support
        setupMenuItem(R.id.setting_help, R.drawable.ic_help, "Help Center", v -> 
            startActivity(new Intent(this, HelpActivity.class)));
        
        setupMenuItem(R.id.setting_contact, R.drawable.ic_contact, "Contact Us", v -> 
            startActivity(new Intent(this, ContactActivity.class)));
        
        setupMenuItem(R.id.setting_feedback, R.drawable.ic_feedback, "Send Feedback", v -> 
            startActivity(new Intent(this, UserFeedbackActivity.class)));

        // About
        setupMenuItem(R.id.setting_privacy, R.drawable.ic_privacy, "Privacy Policy", v -> 
            startActivity(new Intent(this, PrivacyPolicyActivity.class)));
        
        setupMenuItem(R.id.setting_terms, R.drawable.ic_check, "Terms of Service", v -> 
            Toast.makeText(this, "Terms of Service coming soon", Toast.LENGTH_SHORT).show());
        
        setupMenuItem(R.id.setting_about, R.drawable.ic_help, "About SmartHome", v -> 
            Toast.makeText(this, "SmartHome v1.0.0", Toast.LENGTH_SHORT).show());

        // Danger Zone
        findViewById(R.id.btn_logout).setOnClickListener(v -> confirmLogout());
        findViewById(R.id.btn_delete_account).setOnClickListener(v -> confirmDeleteAccount());
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

    private void loadSettings() {
        switchBiometric.setChecked(sharedPrefManager.isBiometricEnabled());
        switchNotifPreview.setChecked(sharedPrefManager.isNotificationPreviewEnabled());
        switchPropertyAlerts.setChecked(sharedPrefManager.isNewListingsAlertsEnabled());
        switchBookingAlerts.setChecked(sharedPrefManager.isBookingConfirmationsEnabled());
        switchPaymentAlerts.setChecked(sharedPrefManager.isPaymentRemindersEnabled());
        switchPromoAlerts.setChecked(sharedPrefManager.isPromotionalMessagesEnabled());

        switchBiometric.setOnCheckedChangeListener((v, checked) -> sharedPrefManager.setBiometricEnabled(checked));
        switchNotifPreview.setOnCheckedChangeListener((v, checked) -> sharedPrefManager.setNotificationPreviewEnabled(checked));
        switchPropertyAlerts.setOnCheckedChangeListener((v, checked) -> sharedPrefManager.setNewListingsAlertsEnabled(checked));
        switchBookingAlerts.setOnCheckedChangeListener((v, checked) -> sharedPrefManager.setBookingConfirmationsEnabled(checked));
        switchPaymentAlerts.setOnCheckedChangeListener((v, checked) -> sharedPrefManager.setPaymentRemindersEnabled(checked));
        switchPromoAlerts.setOnCheckedChangeListener((v, checked) -> sharedPrefManager.setPromotionalMessagesEnabled(checked));
    }

    private void showLanguageDialog() {
        String[] languages = {"English", "Swahili"};
        int checkedItem = sharedPrefManager.getLanguage().equals("Swahili") ? 1 : 0;

        new MaterialAlertDialogBuilder(this)
                .setTitle("Select Language")
                .setSingleChoiceItems(languages, checkedItem, (dialog, which) -> {
                    sharedPrefManager.setLanguage(languages[which]);
                    Toast.makeText(this, "Language set to " + languages[which], Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .show();
    }

    private void showThemeDialog() {
        String[] themes = {"Light", "Dark", "System Default"};
        int checkedItem = 0;
        if (sharedPrefManager.isDarkMode()) checkedItem = 1;

        new MaterialAlertDialogBuilder(this)
                .setTitle("Select Theme")
                .setSingleChoiceItems(themes, checkedItem, (dialog, which) -> {
                    boolean darkMode = (which == 1);
                    sharedPrefManager.setDarkMode(darkMode);
                    AppCompatDelegate.setDefaultNightMode(darkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
                    dialog.dismiss();
                })
                .show();
    }

    private void showCurrencyDialog() {
        String[] currencies = {"TZS", "USD"};
        int checkedItem = sharedPrefManager.getCurrency().equals("USD") ? 1 : 0;

        new MaterialAlertDialogBuilder(this)
                .setTitle("Select Currency")
                .setSingleChoiceItems(currencies, checkedItem, (dialog, which) -> {
                    sharedPrefManager.setCurrency(currencies[which]);
                    Toast.makeText(this, "Currency set to " + currencies[which], Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .show();
    }

    private void confirmLogout() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    AuthenticationHelper.getInstance(this).logout();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDeleteAccount() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Account")
                .setMessage("This action is permanent and cannot be undone. Are you sure?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // In a real app, re-authenticate first
                    Toast.makeText(this, "Account deletion requested", Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
