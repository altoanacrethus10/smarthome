package com.example.smarthome.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.splashscreen.SplashScreen;

import com.example.smarthome.R;
import com.example.smarthome.database.remote.AuthenticationHelper;
import com.example.smarthome.utils.NetworkUtils;
import com.example.smarthome.utils.PermissionHelper;
import com.example.smarthome.utils.SharedPrefManager;

public class SplashActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView tvStatus;
    private static final int PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        
        // Apply theme preference
        SharedPrefManager sharedPrefManager = SharedPrefManager.getInstance(this);
        if (sharedPrefManager.isDarkMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize views
        progressBar = findViewById(R.id.progress_bar);
        tvStatus = findViewById(R.id.tv_status);

        // Check permissions first
        if (PermissionHelper.hasPermissions(this, PermissionHelper.getAllPermissions())) {
            startInitialization();
        } else {
            PermissionHelper.requestPermissions(this, PermissionHelper.getAllPermissions(), PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            // Proceed anyway, we'll check individually when needed
            startInitialization();
        }
    }

    private void startInitialization() {
        // Check internet and database on a separate thread
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkNetworkAndDatabase();
            }
        }, 1000);
    }

    private void checkNetworkAndDatabase() {
        // Check internet connection
        if (!NetworkUtils.isInternetAvailable(this)) {
            tvStatus.setText(R.string.no_internet);
            progressBar.setVisibility(android.view.View.GONE);
            Toast.makeText(this, "No internet connection. Please check your network.", Toast.LENGTH_LONG).show();

            // Retry after 3 seconds
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    checkNetworkAndDatabase();
                }
            }, 3000);
            return;
        }

        // Check cloud database connection
        tvStatus.setText("Connecting to SmartHome cloud...");

        if (NetworkUtils.isCloudDatabaseReachable()) {
            tvStatus.setText("Connected! Loading...");
            proceedToNextScreen();
        } else {
            tvStatus.setText("Cannot reach cloud database. Please try again.");
            progressBar.setVisibility(android.view.View.GONE);
            Toast.makeText(this, "Cloud database connection failed", Toast.LENGTH_LONG).show();

            // Retry after 3 seconds
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    checkNetworkAndDatabase();
                }
            }, 3000);
        }
    }

    private void proceedToNextScreen() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Check if user is already logged in
                AuthenticationHelper authHelper = AuthenticationHelper.getInstance(SplashActivity.this);

                if (authHelper.isLoggedIn()) {
                    // User is logged in, go to MainActivity
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    // User is not logged in, go to LoginActivity
                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
                finish();
            }
        }, 1000);
    }
}
