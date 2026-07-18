package com.example.smarthome.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.smarthome.R;
import com.example.smarthome.database.remote.FirebaseStorageHelper;
import com.example.smarthome.models.User;
import com.example.smarthome.repository.UserRepository;
import com.example.smarthome.utils.SharedPrefManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class EditProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 101;

    private ImageView ivProfileImage;
    private TextInputEditText etName, etEmail, etPhone;
    private MaterialButton btnUpdate;
    private ProgressBar progressBar;

    private SharedPrefManager sharedPrefManager;
    private UserRepository userRepository;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        sharedPrefManager = SharedPrefManager.getInstance(this);
        userRepository = UserRepository.getInstance(this);

        setupToolbar();
        initViews();
        loadUserData();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Edit Profile");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initViews() {
        ivProfileImage = findViewById(R.id.iv_edit_profile_image);
        etName = findViewById(R.id.et_edit_name);
        etEmail = findViewById(R.id.et_edit_email);
        etPhone = findViewById(R.id.et_edit_phone);
        btnUpdate = findViewById(R.id.btn_update_profile);
        progressBar = findViewById(R.id.edit_progress);

        findViewById(R.id.btn_edit_change_photo).setOnClickListener(v -> openImagePicker());
        btnUpdate.setOnClickListener(v -> validateAndUpdate());
    }

    private void loadUserData() {
        etName.setText(sharedPrefManager.getUserName());
        etEmail.setText(sharedPrefManager.getUserEmail());
        etPhone.setText(sharedPrefManager.getUserPhone());

        String avatarUrl = sharedPrefManager.getUserAvatar();
        Glide.with(this)
                .load(avatarUrl != null && !avatarUrl.isEmpty() ? avatarUrl : R.drawable.ic_user)
                .placeholder(R.drawable.ic_user)
                .circleCrop()
                .into(ivProfileImage);
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
            Glide.with(this).load(selectedImageUri).circleCrop().into(ivProfileImage);
        }
    }

    private void validateAndUpdate() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError("Name is required");
            return;
        }

        if (selectedImageUri != null) {
            uploadImage(name, phone);
        } else {
            updateUser(name, phone, sharedPrefManager.getUserAvatar());
        }
    }

    private void uploadImage(String name, String phone) {
        progressBar.setVisibility(View.VISIBLE);
        btnUpdate.setEnabled(false);

        FirebaseStorageHelper.getInstance().uploadImage(selectedImageUri, "profiles", new FirebaseStorageHelper.StorageCallback<String>() {
            @Override
            public void onSuccess(String imageUrl) {
                updateUser(name, phone, imageUrl);
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                btnUpdate.setEnabled(true);
                Toast.makeText(EditProfileActivity.this, "Image upload failed: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUser(String name, String phone, String avatarUrl) {
        progressBar.setVisibility(View.VISIBLE);
        btnUpdate.setEnabled(false);

        User user = new User();
        user.setId(sharedPrefManager.getUserId());
        user.setName(name);
        user.setPhone(phone);
        user.setAvatarUrl(avatarUrl);

        userRepository.updateUser(user, new UserRepository.RepositoryCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                sharedPrefManager.updateUser(name, phone);
                if (avatarUrl != null) sharedPrefManager.setUserAvatar(avatarUrl);
                
                progressBar.setVisibility(View.GONE);
                btnUpdate.setEnabled(true);
                Toast.makeText(EditProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                btnUpdate.setEnabled(true);
                Toast.makeText(EditProfileActivity.this, "Update failed: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
