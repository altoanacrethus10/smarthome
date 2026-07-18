package com.example.smarthome.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.example.smarthome.R;
import com.example.smarthome.constants.AppConstants;
import com.example.smarthome.database.remote.FirebaseStorageHelper;
import com.example.smarthome.models.House;
import com.example.smarthome.repository.HouseRepository;
import com.example.smarthome.utils.ImageUtils;
import com.example.smarthome.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AddListingActivity extends AppCompatActivity {

    private TextInputEditText etTitle, etDescription, etPrice, etBedrooms, etBathrooms, etArea;
    private Spinner spinnerLocation, spinnerPropertyType;
    private ChipGroup chipGroupFeatures;
    private ImageView ivHouseImage;
    private MaterialButton btnSelectImage, btnAddListing;

    private HouseRepository houseRepository;
    private SharedPrefManager sharedPrefManager;
    private Uri selectedImageUri;
    private final List<String> selectedFeatures = new ArrayList<>();
    private boolean isEditMode = false;
    private String editingHouseId;
    private House editingHouse;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = 
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                selectedImageUri = result.getData().getData();
                if (selectedImageUri != null) {
                    Glide.with(this).load(selectedImageUri).placeholder(R.drawable.ic_house_placeholder).into(ivHouseImage);
                }
            }
        });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_listing);

        houseRepository = HouseRepository.getInstance(this);
        sharedPrefManager = SharedPrefManager.getInstance(this);
        editingHouseId = getIntent().getStringExtra("house_id");
        isEditMode = editingHouseId != null && !editingHouseId.trim().isEmpty();

        initViews();
        setupToolbar();
        setupSpinners();
        setupFeatureChips();
        setupClickListeners();

        if (isEditMode) loadExistingHouse();
    }

    private void initViews() {
        etTitle = findViewById(R.id.et_listing_title);
        etDescription = findViewById(R.id.et_listing_description);
        etPrice = findViewById(R.id.et_listing_price);
        etBedrooms = findViewById(R.id.et_listing_bedrooms);
        etBathrooms = findViewById(R.id.et_listing_bathrooms);
        etArea = findViewById(R.id.et_listing_area);
        spinnerLocation = findViewById(R.id.spinner_listing_location);
        spinnerPropertyType = findViewById(R.id.spinner_listing_type);
        chipGroupFeatures = findViewById(R.id.chip_group_listing_features);
        ivHouseImage = findViewById(R.id.iv_listing_image);
        btnSelectImage = findViewById(R.id.btn_select_image);
        btnAddListing = findViewById(R.id.btn_add_listing);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(isEditMode ? "Edit Listing" : "Add New Listing");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupSpinners() {
        ArrayAdapter<String> locAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, AppConstants.DAR_LOCATIONS);
        locAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLocation.setAdapter(locAdapter);

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, AppConstants.PROPERTY_TYPES);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPropertyType.setAdapter(typeAdapter);
    }

    private void setupFeatureChips() {
        chipGroupFeatures.removeAllViews();
        for (String feature : AppConstants.HOUSE_FEATURES) {
            Chip chip = new Chip(this);
            chip.setText(feature);
            chip.setCheckable(true);
            chip.setChipStrokeWidth(1f);
            chip.setChipStrokeColorResource(R.color.primary);
            chip.setChipBackgroundColorResource(android.R.color.transparent);
            chip.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.black));
            
            chip.setOnCheckedChangeListener((v, checked) -> {
                if (checked) selectedFeatures.add(feature);
                else selectedFeatures.remove(feature);
            });
            chipGroupFeatures.addView(chip);
        }
    }

    private void setupClickListeners() {
        btnSelectImage.setOnClickListener(v -> openImagePicker());
        btnAddListing.setOnClickListener(v -> validateAndSubmit());
    }

    private void loadExistingHouse() {
        houseRepository.getHouse(editingHouseId, new HouseRepository.RepositoryCallback<House>() {
            @Override
            public void onSuccess(House h) {
                editingHouse = h;
                if (h != null) fillForm(h);
            }
            @Override
            public void onError(String e) {}
        });
    }

    private void fillForm(House h) {
        etTitle.setText(h.getTitle());
        etDescription.setText(h.getDescription());
        etPrice.setText(String.valueOf((int)h.getPrice()));
        etBedrooms.setText(String.valueOf(h.getBedrooms()));
        etBathrooms.setText(String.valueOf(h.getBathrooms()));
        etArea.setText(String.valueOf(h.getArea()));
        
        btnAddListing.setText("Update Property");
        if (h.getImages() != null && !h.getImages().isEmpty()) {
            Glide.with(this).load(h.getImages().get(0)).into(ivHouseImage);
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT).setType("image/*");
        imagePickerLauncher.launch(Intent.createChooser(intent, "Select Image"));
    }

    private void validateAndSubmit() {
        String title = etTitle.getText().toString().trim();
        String desc = etDescription.getText().toString().trim();
        String price = etPrice.getText().toString().trim();
        
        if (title.isEmpty()) { etTitle.setError("Required"); return; }
        if (desc.isEmpty()) { etDescription.setError("Required"); return; }
        if (price.isEmpty()) { etPrice.setError("Required"); return; }

        if (!isEditMode && selectedImageUri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        House h = isEditMode ? editingHouse : new House();
        if (!isEditMode) {
            h.setId(UUID.randomUUID().toString());
            h.setOwnerId(sharedPrefManager.getUserId());
            h.setOwnerName(sharedPrefManager.getUserName());
            h.setCreatedAt(String.valueOf(System.currentTimeMillis()));
        }
        
        h.setTitle(title);
        h.setDescription(desc);
        h.setPrice(Double.parseDouble(price));
        h.setLocation(spinnerLocation.getSelectedItem().toString());
        h.setPropertyType(spinnerPropertyType.getSelectedItem().toString());
        h.setBedrooms(Integer.parseInt(etBedrooms.getText().toString()));
        h.setBathrooms(Integer.parseInt(etBathrooms.getText().toString()));
        h.setArea(Double.parseDouble(etArea.getText().toString()));
        h.setFeatures(selectedFeatures);

        btnAddListing.setEnabled(false);
        btnAddListing.setText("Saving...");

        if (selectedImageUri != null) {
            uploadImageAndSave(h);
        } else {
            saveToFirestore(h);
        }
    }

    private void uploadImageAndSave(House h) {
        FirebaseStorageHelper.getInstance().uploadImage(selectedImageUri, "houses", new FirebaseStorageHelper.StorageCallback<String>() {
            @Override
            public void onSuccess(String url) {
                List<String> imgs = new ArrayList<>();
                imgs.add(url);
                h.setImages(imgs);
                saveToFirestore(h);
            }
            @Override
            public void onError(String e) {
                btnAddListing.setEnabled(true);
                btnAddListing.setText("Add Listing");
                Toast.makeText(AddListingActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveToFirestore(House h) {
        HouseRepository.RepositoryCallback<String> cb = new HouseRepository.RepositoryCallback<String>() {
            @Override
            public void onSuccess(String s) { finish(); }
            @Override
            public void onError(String e) {
                btnAddListing.setEnabled(true);
                btnAddListing.setText("Add Listing");
            }
        };
        
        if (isEditMode) {
            houseRepository.updateHouse(h, new HouseRepository.RepositoryCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) { finish(); }
                @Override
                public void onError(String e) {
                    btnAddListing.setEnabled(true);
                    btnAddListing.setText("Update Property");
                }
            });
        } else {
            houseRepository.createHouse(h, cb);
        }
    }
}
