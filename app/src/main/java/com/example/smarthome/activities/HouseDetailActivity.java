package com.example.smarthome.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.smarthome.R;
import com.example.smarthome.adapters.AmenityAdapter;
import com.example.smarthome.adapters.FeedbackAdapter;
import com.example.smarthome.adapters.ImageSliderAdapter;
import com.example.smarthome.models.Booking;
import com.example.smarthome.models.Feedback;
import com.example.smarthome.models.House;
import com.example.smarthome.models.User;
import com.example.smarthome.repository.BookingRepository;
import com.example.smarthome.repository.FeedbackRepository;
import com.example.smarthome.repository.HouseRepository;
import com.example.smarthome.repository.UserRepository;
import com.example.smarthome.services.SinchService;
import com.example.smarthome.utils.BluetoothUtils;
import com.example.smarthome.utils.NotificationUtils;
import com.example.smarthome.utils.SharedPrefManager;
import com.example.smarthome.utils.SmsUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;
import java.util.UUID;

public class HouseDetailActivity extends AppCompatActivity implements android.content.ServiceConnection {

    private FloatingActionButton ivFavorite;
    private ViewPager2 viewPagerImages;
    private TextView tvTitle, tvPrice, tvLocation, tvDescription;
    private TextView tvBedrooms, tvBathrooms, tvArea, tvStatus, tvFurnished;
    private TextView tvRating, tvReviewsCount, btnReadMore, btnWriteReview;
    private androidx.recyclerview.widget.RecyclerView rvAmenities, rvReviews;
    private TextInputEditText etCheckIn, etCheckOut, etTenantsCount;
    private MaterialCardView cardOwner, cardTenantInfo;
    private TextView tvOwnerName, tvOwnerPhone, tvTenantNameDetail;
    private ImageButton btnCallOwner, btnMessageOwner, btnShare, btnContactTenantChat;
    private MaterialButton btnRentNow, btnViewMap, btnEditProperty, btnViewBookings, btnViewEarnings;
    private LinearLayout llOwnerActions, llBookingSection;

    private House house;
    private String houseId;
    private HouseRepository houseRepository;
    private UserRepository userRepository;
    private BookingRepository bookingRepository;
    private FeedbackRepository feedbackRepository;
    private SharedPrefManager sharedPrefManager;
    private boolean isFavorite = false;
    private boolean isDescriptionExpanded = false;
    private SinchService.SinchServiceInterface mSinchServiceInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_house_detail);

        houseId = getIntent().getStringExtra("house_id");
        if (houseId == null) {
            Toast.makeText(this, "House not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        houseRepository = HouseRepository.getInstance(this);
        userRepository = UserRepository.getInstance(this);
        bookingRepository = BookingRepository.getInstance(this);
        feedbackRepository = FeedbackRepository.getInstance(this);
        sharedPrefManager = SharedPrefManager.getInstance(this);

        initViews();
        setupToolbar();
        loadHouseData();
        setupClickListeners();
        getApplicationContext().bindService(new Intent(this, SinchService.class), this, BIND_AUTO_CREATE);
    }

    @Override
    public void onServiceConnected(android.content.ComponentName name, android.os.IBinder service) {
        mSinchServiceInterface = (SinchService.SinchServiceInterface) service;
    }

    @Override
    public void onServiceDisconnected(android.content.ComponentName name) {
        mSinchServiceInterface = null;
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
            getSupportActionBar().setTitle("");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void initViews() {
        ivFavorite = findViewById(R.id.iv_favorite);
        viewPagerImages = findViewById(R.id.view_pager_images);
        tvTitle = findViewById(R.id.tv_house_title);
        tvPrice = findViewById(R.id.tv_house_price);
        tvLocation = findViewById(R.id.tv_house_location);
        tvDescription = findViewById(R.id.tv_house_description);
        tvBedrooms = findViewById(R.id.tv_bedrooms);
        tvBathrooms = findViewById(R.id.tv_bathrooms);
        tvArea = findViewById(R.id.tv_area);
        tvStatus = findViewById(R.id.tv_status);
        tvFurnished = findViewById(R.id.tv_furnished);
        tvRating = findViewById(R.id.tv_rating);
        tvReviewsCount = findViewById(R.id.tv_reviews_count);
        btnReadMore = findViewById(R.id.btn_read_more);
        btnWriteReview = findViewById(R.id.btn_write_review);
        rvAmenities = findViewById(R.id.rv_amenities);
        rvReviews = findViewById(R.id.rv_reviews);
        etCheckIn = findViewById(R.id.et_check_in);
        etCheckOut = findViewById(R.id.et_check_out);
        etTenantsCount = findViewById(R.id.et_tenants_count);
        cardOwner = findViewById(R.id.card_owner);
        tvOwnerName = findViewById(R.id.tv_owner_name);
        tvOwnerPhone = findViewById(R.id.tv_owner_phone);
        btnCallOwner = findViewById(R.id.btn_call_owner);
        btnMessageOwner = findViewById(R.id.btn_message_owner);
        btnRentNow = findViewById(R.id.btn_rent_now);
        btnShare = findViewById(R.id.btn_share);
        btnViewMap = findViewById(R.id.btn_view_map);
        
        // Owner Actions
        llOwnerActions = findViewById(R.id.ll_owner_actions);
        btnEditProperty = findViewById(R.id.btn_edit_property_detail);
        btnViewBookings = findViewById(R.id.btn_view_property_bookings);
        btnViewEarnings = findViewById(R.id.btn_view_property_earnings);
        cardTenantInfo = findViewById(R.id.card_tenant_info);
        tvTenantNameDetail = findViewById(R.id.tv_tenant_name_detail);
        btnContactTenantChat = findViewById(R.id.btn_contact_tenant_chat);
        
        llBookingSection = findViewById(R.id.ll_booking_section);
    }

    private void setupClickListeners() {
        ivFavorite.setOnClickListener(v -> toggleFavorite());
        btnCallOwner.setOnClickListener(v -> callOwner());
        btnMessageOwner.setOnClickListener(v -> messageOwner());
        btnRentNow.setOnClickListener(v -> processBooking());
        btnShare.setOnClickListener(v -> shareHouse());
        btnViewMap.setOnClickListener(v -> viewOnMap());
        btnReadMore.setOnClickListener(v -> toggleDescription());
        btnWriteReview.setOnClickListener(v -> {
            Intent intent = new Intent(this, UserFeedbackActivity.class);
            intent.putExtra("house_id", house.getId());
            intent.putExtra("house_title", house.getTitle());
            startActivity(intent);
        });
        etCheckIn.setOnClickListener(v -> showDatePicker("Select Check-in Date", etCheckIn));
        etCheckOut.setOnClickListener(v -> showDatePicker("Select Check-out Date", etCheckOut));

        // Owner Actions
        btnEditProperty.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddListingActivity.class);
            intent.putExtra("house_id", house.getId());
            startActivity(intent);
        });
        btnViewBookings.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(MainActivity.EXTRA_OPEN_SECTION, "bookings");
            startActivity(intent);
        });
        btnViewEarnings.setOnClickListener(v -> {
            Intent intent = new Intent(this, ReportActivity.class);
            intent.putExtra("house_id", house.getId());
            startActivity(intent);
        });
    }

    private void loadHouseData() {
        houseRepository.getHouse(houseId, new HouseRepository.RepositoryCallback<House>() {
            @Override
            public void onSuccess(House houseData) {
                house = houseData;
                displayHouseData();
                loadOwnerData();
                checkTenantInfo();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(HouseDetailActivity.this, "Failed to load house: " + error, Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void displayHouseData() {
        if (house == null) return;

        tvTitle.setText(house.getTitle());
        tvPrice.setText(house.getFormattedPrice() + "/month");
        tvLocation.setText(house.getLocation());
        tvDescription.setText(house.getDescription() != null ? house.getDescription() : "No description available");

        tvBedrooms.setText(String.valueOf(house.getBedrooms()));
        tvBathrooms.setText(String.valueOf(house.getBathrooms()));
        tvArea.setText(house.getArea() > 0 ? String.valueOf((int)house.getArea()) : "N/A");
        tvFurnished.setText(house.isFurnished() ? "Yes" : "No");

        tvRating.setText(String.format("%.1f", house.getRating() > 0 ? house.getRating() : 4.5));
        tvReviewsCount.setText("(" + (house.getReviewCount() > 0 ? house.getReviewCount() : 12) + " reviews)");

        if (house.isAvailable()) {
            tvStatus.setText("Available");
            tvStatus.setBackgroundResource(R.drawable.bg_status_success);
            btnRentNow.setEnabled(true);
            btnRentNow.setText("Book Now");
        } else {
            tvStatus.setText("Rented");
            tvStatus.setBackgroundResource(R.drawable.bg_status_error);
            btnRentNow.setEnabled(false);
            btnRentNow.setText("Currently Rented");
        }

        applyRoleBasedVisibility();
        setupAmenities();
        loadReviews();
        loadImages();

        if (sharedPrefManager.isLoggedIn() && sharedPrefManager.isTenant()) {
            isFavorite = houseRepository.isFavorite(sharedPrefManager.getUserId(), house.getId());
            updateFavoriteIcon();
        }
    }

    private void applyRoleBasedVisibility() {
        boolean isOwner = sharedPrefManager.isOwner();
        
        if (isOwner) {
            ivFavorite.setVisibility(View.GONE);
            findViewById(R.id.card_bottom_bar).setVisibility(View.GONE);
            llBookingSection.setVisibility(View.GONE);
            cardOwner.setVisibility(View.GONE);
            llOwnerActions.setVisibility(View.VISIBLE);
        } else {
            ivFavorite.setVisibility(View.VISIBLE);
            findViewById(R.id.card_bottom_bar).setVisibility(View.VISIBLE);
            llBookingSection.setVisibility(View.VISIBLE);
            cardOwner.setVisibility(View.VISIBLE);
            llOwnerActions.setVisibility(View.GONE);
        }
    }

    private void checkTenantInfo() {
        if (sharedPrefManager.isOwner() && !house.isAvailable()) {
            // In a real app, fetch the latest confirmed booking for this house
            // to show tenant details. For now, show a placeholder.
            cardTenantInfo.setVisibility(View.VISIBLE);
            tvTenantNameDetail.setText("Current Tenant Info Available");
        }
    }

    private void toggleDescription() {
        if (isDescriptionExpanded) {
            tvDescription.setMaxLines(3);
            btnReadMore.setText("Read more");
        } else {
            tvDescription.setMaxLines(Integer.MAX_VALUE);
            btnReadMore.setText("Read less");
        }
        isDescriptionExpanded = !isDescriptionExpanded;
    }

    private void showDatePicker(String title, TextInputEditText target) {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(title)
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();
        datePicker.addOnPositiveButtonClickListener(selection -> {
            target.setText(datePicker.getHeaderText());
            calculateTotalPrice();
        });
        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    private void calculateTotalPrice() {
        if (house == null) return;
        tvPrice.setText(house.getFormattedPrice());
        TextView tvTotalLabel = findViewById(R.id.tv_total_label);
        if (tvTotalLabel != null) tvTotalLabel.setText("/mo");
    }

    private void setupAmenities() {
        List<AmenityAdapter.Amenity> amenities = new java.util.ArrayList<>();
        List<String> features = house.getFeatures();
        
        if (features != null && !features.isEmpty()) {
            for (String feature : features) {
                amenities.add(new AmenityAdapter.Amenity(feature, R.drawable.ic_check));
            }
        } else {
            amenities.add(new AmenityAdapter.Amenity("Wi-Fi", R.drawable.ic_check));
            amenities.add(new AmenityAdapter.Amenity("Parking", R.drawable.ic_check));
            amenities.add(new AmenityAdapter.Amenity("Security", R.drawable.ic_check));
            amenities.add(new AmenityAdapter.Amenity("Water", R.drawable.ic_check));
            amenities.add(new AmenityAdapter.Amenity("Electricity", R.drawable.ic_check));
            amenities.add(new AmenityAdapter.Amenity("Kitchen", R.drawable.ic_check));
        }

        rvAmenities.setAdapter(new AmenityAdapter(amenities));
    }

    private void loadReviews() {
        feedbackRepository.getFeedbackByHouseId(houseId, new FeedbackRepository.RepositoryCallback<List<Feedback>>() {
            @Override
            public void onSuccess(List<Feedback> result) {
                if (result != null && !result.isEmpty()) {
                    rvReviews.setAdapter(new FeedbackAdapter(HouseDetailActivity.this, result));
                }
            }

            @Override
            public void onError(String error) {
                Log.e("HouseDetail", "Error loading reviews: " + error);
            }
        });
    }

    private void loadImages() {
        List<String> images = house.getImages();
        if (images != null && !images.isEmpty()) {
            viewPagerImages.setVisibility(View.VISIBLE);
            findViewById(R.id.iv_house_image).setVisibility(View.GONE);
            viewPagerImages.setAdapter(new ImageSliderAdapter(this, images));
        } else {
            viewPagerImages.setVisibility(View.GONE);
            ImageView imageView = findViewById(R.id.iv_house_image);
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageResource(R.drawable.ic_house_placeholder);
        }
    }

    private void loadOwnerData() {
        if (house == null || house.getOwnerId() == null) return;

        userRepository.getUser(house.getOwnerId(), new UserRepository.RepositoryCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user != null) {
                    tvOwnerName.setText(user.getName());
                    tvOwnerPhone.setText(user.getPhone() != null ? user.getPhone() : "No phone");
                    ImageView avatar = findViewById(R.id.iv_owner_avatar);
                    Glide.with(HouseDetailActivity.this)
                            .load(user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty() ? user.getAvatarUrl() : R.drawable.ic_user)
                            .circleCrop()
                            .into(avatar);
                }
            }

            @Override
            public void onError(String error) {}
        });
    }

    private void toggleFavorite() {
        if (sharedPrefManager.isOwner()) return;
        if (!sharedPrefManager.isLoggedIn()) {
            Toast.makeText(this, "Please login to save favorites", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = sharedPrefManager.getUserId();
        if (isFavorite) {
            houseRepository.removeFavorite(userId, house.getId(), new HouseRepository.RepositoryCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    isFavorite = false;
                    updateFavoriteIcon();
                    Toast.makeText(HouseDetailActivity.this, "Removed from favorites", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String error) {}
            });
        } else {
            houseRepository.addFavorite(userId, house.getId(), new HouseRepository.RepositoryCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    isFavorite = true;
                    updateFavoriteIcon();
                    Toast.makeText(HouseDetailActivity.this, "Added to favorites", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String error) {}
            });
        }
    }

    private void updateFavoriteIcon() {
        ivFavorite.setImageResource(isFavorite ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);
    }

    private void callOwner() {
        if (house == null || house.getOwnerId() == null) return;
        
        if (mSinchServiceInterface != null && mSinchServiceInterface.isStarted()) {
            com.sinch.android.rtc.calling.Call call = mSinchServiceInterface.callUser(house.getOwnerId());
            if (call != null) {
                Intent intent = new Intent(this, CallActivity.class);
                intent.putExtra("CALL_ID", call.getCallId());
                intent.putExtra("IS_INCOMING", false);
                startActivity(intent);
            }
        } else {
            // Fallback to regular call if Sinch is not ready
            String phone = tvOwnerPhone.getText().toString();
            if (phone.isEmpty() || phone.equals("No contact info") || phone.equals("No phone")) {
                Toast.makeText(this, "Contact info not available", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phone));
            startActivity(intent);
        }
    }

    private void messageOwner() {
        if (house == null) return;
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("other_user_id", house.getOwnerId());
        intent.putExtra("other_user_name", tvOwnerName.getText().toString());
        intent.putExtra("house_id", house.getId());
        startActivity(intent);
    }

    private void processBooking() {
        String moveIn = etCheckIn.getText() != null ? etCheckIn.getText().toString().trim() : "";
        String moveOut = etCheckOut.getText() != null ? etCheckOut.getText().toString().trim() : "";
        
        if (moveIn.isEmpty()) {
            Toast.makeText(this, "Select check-in date", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, BookingSummaryActivity.class);
        intent.putExtra("house_id", house.getId());
        intent.putExtra("check_in", moveIn);
        intent.putExtra("check_out", moveOut);
        intent.putExtra("owner_phone", tvOwnerPhone.getText().toString());
        startActivity(intent);
    }

    private void shareHouse() {
        String shareText = "🏠 " + house.getTitle() + "\n" + house.getLocation() + "\nPrice: " + house.getFormattedPrice();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(intent, "Share Property"));
    }

    private void viewOnMap() {
        String locationQuery = Uri.encode(house.getLocation() + ", Tanzania");
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + locationQuery));
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        if (mSinchServiceInterface != null) {
            getApplicationContext().unbindService(this);
        }
        super.onDestroy();
    }
}
