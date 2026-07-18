package com.example.smarthome.activities;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.example.smarthome.R;
import com.example.smarthome.database.local.DatabaseHelper;
import com.example.smarthome.database.remote.AuthenticationHelper;
import com.example.smarthome.utils.NetworkUtils;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private TextView tvForgotPassword, tvSignup;
    private AuthenticationHelper authHelper;
    private boolean isInternetReady;
    private boolean isDatabaseReady;
    private boolean isCloudReady;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        authHelper = AuthenticationHelper.getInstance(this);
        if (authHelper.isLoggedIn()) {
            openMainScreen();
            return;
        }

        // Initialize views
        etEmail = findViewById(R.id.et_login_email);
        etPassword = findViewById(R.id.et_login_password);
        btnLogin = findViewById(R.id.btn_login);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        tvSignup = findViewById(R.id.tv_signup);

        refreshConnectionStatus();
        setupInputCleanup();
        setupInputActions();

        // Login button click
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogin();
            }
        });

        // Forgot password click
        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ResetPasswordActivity.class);
                startActivity(intent);
            }
        });

        // Sign up click
        tvSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
    }

    private void performLogin() {
        refreshConnectionStatus();

        if (!isInternetReady) {
            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_LONG).show();
            return;
        }

        if (!isDatabaseReady) {
            Toast.makeText(this, "Local database connection failed. Please restart the app.", Toast.LENGTH_LONG).show();
            return;
        }

        if (!isCloudReady) {
            Toast.makeText(this, "Firebase connection is not ready. Please try again.", Toast.LENGTH_LONG).show();
            return;
        }

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validate inputs
        if (email.isEmpty()) {
            etEmail.setError(getString(R.string.field_required));
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getString(R.string.invalid_email));
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError(getString(R.string.field_required));
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError(getString(R.string.password_min_length));
            etPassword.requestFocus();
            return;
        }

        setLoginLoading(true, R.string.logging_in);

        authHelper.login(email, password, new AuthenticationHelper.AuthCallback() {
            @Override
            public void onSuccess() {
                findViewById(R.id.login_progress).setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, R.string.login_success, Toast.LENGTH_SHORT).show();
                openMainScreen();
            }

            @Override
            public void onError(String error) {
                setLoginLoading(false, R.string.login);
                Toast.makeText(LoginActivity.this, "Login failed: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshConnectionStatus();
    }

    private void refreshConnectionStatus() {
        isInternetReady = NetworkUtils.isInternetAvailable(this);
        isDatabaseReady = isSqliteDatabaseReady();
        isCloudReady = isInternetReady && NetworkUtils.isCloudDatabaseReachable();
    }

    private boolean isSqliteDatabaseReady() {
        try {
            SQLiteDatabase database = DatabaseHelper.getInstance(this).getReadableDatabase();
            return database != null && database.isOpen();
        } catch (Exception e) {
            return false;
        }
    }

    private void updateStatusText(TextView view, String label, boolean isReady) {
        view.setText(label + ": " + (isReady ? "Connected" : "Not Connected"));
        view.setTextColor(getColor(isReady ? R.color.success : R.color.error));
        view.setBackgroundResource(isReady ? R.drawable.bg_status_success : R.drawable.bg_status_error);
    }

    private void setLoginLoading(boolean loading, int loginButtonText) {
        findViewById(R.id.login_progress).setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!loading);
        btnLogin.setText(loginButtonText);
    }

    private void setupInputCleanup() {
        TextWatcher clearErrorsWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                etEmail.setError(null);
                etPassword.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
        etEmail.addTextChangedListener(clearErrorsWatcher);
        etPassword.addTextChangedListener(clearErrorsWatcher);
    }

    private void setupInputActions() {
        etPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEND) {
                performLogin();
                return true;
            }
            return false;
        });
    }

    private void openMainScreen() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
