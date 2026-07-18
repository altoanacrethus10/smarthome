package com.example.smarthome.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.example.smarthome.R;
import com.example.smarthome.models.Feedback;
import com.example.smarthome.repository.FeedbackRepository;
import com.example.smarthome.utils.SharedPrefManager;

import java.util.UUID;

public class UserFeedbackActivity extends AppCompatActivity {

    private RatingBar ratingBar;
    private TextInputEditText etFeedbackType, etFeedbackMessage;
    private MaterialButton btnSubmit;
    private FeedbackRepository feedbackRepository;
    private SharedPrefManager sharedPrefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        feedbackRepository = FeedbackRepository.getInstance(this);
        sharedPrefManager = SharedPrefManager.getInstance(this);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Initialize views
        ratingBar = findViewById(R.id.rb_feedback);
        etFeedbackType = findViewById(R.id.et_feedback_type);
        etFeedbackMessage = findViewById(R.id.et_feedback_message);
        btnSubmit = findViewById(R.id.btn_submit_feedback);

        // Submit button click
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitFeedback();
            }
        });
    }

    private void submitFeedback() {
        float rating = ratingBar.getRating();
        String type = etFeedbackType.getText().toString().trim();
        String message = etFeedbackMessage.getText().toString().trim();

        if (message.isEmpty()) {
            etFeedbackMessage.setError(getString(R.string.field_required));
            etFeedbackMessage.requestFocus();
            return;
        }

        btnSubmit.setEnabled(false);
        btnSubmit.setText("Submitting...");

        Feedback feedback = new Feedback();
        feedback.setId(UUID.randomUUID().toString());
        feedback.setUserId(sharedPrefManager.getUserId());
        feedback.setUserName(sharedPrefManager.getUserName());
        feedback.setUserEmail(sharedPrefManager.getUserEmail());
        feedback.setRating(rating);
        feedback.setFeedbackType(type);
        feedback.setMessage(message);
        feedback.setCreatedAt(String.valueOf(System.currentTimeMillis()));

        feedbackRepository.createFeedback(feedback, new FeedbackRepository.RepositoryCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Toast.makeText(UserFeedbackActivity.this, "Thank you for your feedback!", Toast.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void onError(String error) {
                btnSubmit.setEnabled(true);
                btnSubmit.setText("Submit Feedback");
                Toast.makeText(UserFeedbackActivity.this, "Failed to submit: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}