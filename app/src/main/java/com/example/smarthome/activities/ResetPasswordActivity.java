package com.example.smarthome.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.example.smarthome.R;
import com.example.smarthome.utils.NetworkUtils;

public class ResetPasswordActivity extends AppCompatActivity {

    private ImageView ivBack;
    private TextInputEditText etEmail;
    private MaterialButton btnResetPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        // Initialize views
        ivBack = findViewById(R.id.iv_back);
        etEmail = findViewById(R.id.et_reset_email);
        btnResetPassword = findViewById(R.id.btn_reset_password);

        // Back button click
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Reset password button click
        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });
    }

    private void resetPassword() {
        // Check internet
        if (!NetworkUtils.isInternetAvailable(this)) {
            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_LONG).show();
            return;
        }

        String email = etEmail.getText().toString().trim();

        if (email.isEmpty() || !email.contains("@")) {
            etEmail.setError(getString(R.string.invalid_email));
            etEmail.requestFocus();
            return;
        }

        // In a real app, send reset email via Firebase
        com.google.firebase.auth.FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(ResetPasswordActivity.this, "Password reset link sent to your email: " + email, Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    String error = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                    Toast.makeText(ResetPasswordActivity.this, "Failed: " + error, Toast.LENGTH_LONG).show();
                }
            });
    }
}