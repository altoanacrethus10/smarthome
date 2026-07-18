package com.example.smarthome.activities;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.example.smarthome.R;
import com.example.smarthome.constants.AppConstants;
import com.example.smarthome.database.local.DatabaseHelper;
import com.example.smarthome.database.remote.AuthenticationHelper;
import com.example.smarthome.models.User;
import com.example.smarthome.utils.NetworkUtils;
import com.example.smarthome.utils.SmsUtils;
import androidx.appcompat.widget.Toolbar;

public class SignUpActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etPhone, etPassword, etConfirmPassword;
    private RadioGroup rgUserType;
    private MaterialButton btnSignup;
    private TextView tvLogin, tvInternetStatus, tvDatabaseStatus;
    private boolean isInternetReady;
    private boolean isDatabaseReady;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // Initialize views
        etName = findViewById(R.id.et_signup_name);
        etEmail = findViewById(R.id.et_signup_email);
        etPhone = findViewById(R.id.et_signup_phone);
        etPassword = findViewById(R.id.et_signup_password);
        etConfirmPassword = findViewById(R.id.et_signup_confirm_password);
        rgUserType = findViewById(R.id.rg_user_type);
        btnSignup = findViewById(R.id.btn_signup);
        tvLogin = findViewById(R.id.tv_login);
        tvInternetStatus = findViewById(R.id.tv_signup_internet_status);
        tvDatabaseStatus = findViewById(R.id.tv_signup_database_status);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Create Account");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        refreshConnectionStatus();
        setupInputActions();

        // Sign up button click
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performSignUp();
            }
        });

        // Login link click
        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Go back to Login
            }
        });
    }

    private void performSignUp() {
        refreshConnectionStatus();

        if (!isInternetReady) {
            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_LONG).show();
            return;
        }

        if (!isDatabaseReady) {
            Toast.makeText(this, "SQLite database connection failed. Please restart the app.", Toast.LENGTH_LONG).show();
            return;
        }

        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validate inputs
        if (name.isEmpty()) {
            etName.setError(getString(R.string.field_required));
            etName.requestFocus();
            return;
        }

        if (email.isEmpty() || !email.contains("@")) {
            etEmail.setError(getString(R.string.invalid_email));
            etEmail.requestFocus();
            return;
        }

        if (phone.isEmpty() || phone.length() < 10) {
            etPhone.setError(getString(R.string.invalid_phone));
            etPhone.requestFocus();
            return;
        }

        if (password.isEmpty() || password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError(getString(R.string.password_mismatch));
            etConfirmPassword.requestFocus();
            return;
        }

        // Get user type
        int selectedId = rgUserType.getCheckedRadioButtonId();
        String userType = AppConstants.USER_TYPE_TENANT;
        if (selectedId == R.id.rb_owner) {
            userType = AppConstants.USER_TYPE_OWNER;
        }

        btnSignup.setEnabled(false);
        btnSignup.setText("Creating Account...");

        User newUser = new User();
        newUser.setName(name);
        newUser.setEmail(email);
        newUser.setPhone(phone);
        newUser.setUserType(userType);
        newUser.setCreatedAt(String.valueOf(System.currentTimeMillis()));

        AuthenticationHelper.getInstance(this).register(newUser, password, new AuthenticationHelper.AuthCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(SignUpActivity.this, R.string.signup_success, Toast.LENGTH_SHORT).show();
                
                // Send welcome SMS
                SmsUtils.sendWelcomeSms(SignUpActivity.this, phone, name);

                // Navigate to Main
                Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String error) {
                btnSignup.setEnabled(true);
                btnSignup.setText("Sign Up");
                Toast.makeText(SignUpActivity.this, "Registration failed: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshConnectionStatus();
    }

    private void setupInputActions() {
        etConfirmPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEND) {
                performSignUp();
                return true;
            }
            return false;
        });
    }

    private void refreshConnectionStatus() {
        isInternetReady = NetworkUtils.isInternetAvailable(this);
        isDatabaseReady = isSqliteDatabaseReady();

        updateStatusText(tvInternetStatus, "Internet Status", isInternetReady);
        updateStatusText(tvDatabaseStatus, "Database Status", isDatabaseReady);
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
}
