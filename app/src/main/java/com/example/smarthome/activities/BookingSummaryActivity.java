package com.example.smarthome.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.smarthome.R;
import com.example.smarthome.models.Booking;
import com.example.smarthome.models.House;
import com.example.smarthome.repository.BookingRepository;
import com.example.smarthome.repository.HouseRepository;
import com.example.smarthome.utils.NotificationUtils;
import com.example.smarthome.utils.SharedPrefManager;
import com.example.smarthome.utils.SmsUtils;
import com.google.android.material.button.MaterialButton;

import java.util.UUID;

public class BookingSummaryActivity extends AppCompatActivity {

    private ImageView ivProperty;
    private TextView tvName, tvLocation, tvCheckIn, tvCheckOut, tvGuests;
    private TextView tvBasePrice, tvSecurityDeposit, tvServiceFee, tvTotalPrice, tvBasePriceLabel;
    private RadioGroup rgPaymentMethods;
    private MaterialButton btnConfirm;

    private House house;
    private String checkInDate, checkOutDate;
    private String ownerPhone;
    private int tenantCount;
    private String notes;
    private double totalAmount;
    private double securityDeposit = 500000.0; // Fixed for demo
    private double serviceFee = 50000.0; // Fixed for demo

    private BookingRepository bookingRepository;
    private HouseRepository houseRepository;
    private SharedPrefManager sharedPrefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_summary);

        // Get data from intent
        String houseId = getIntent().getStringExtra("house_id");
        checkInDate = getIntent().getStringExtra("check_in");
        checkOutDate = getIntent().getStringExtra("check_out");
        ownerPhone = getIntent().getStringExtra("owner_phone");
        tenantCount = getIntent().getIntExtra("tenants", 1);
        notes = getIntent().getStringExtra("notes");

        bookingRepository = BookingRepository.getInstance(this);
        houseRepository = HouseRepository.getInstance(this);
        sharedPrefManager = SharedPrefManager.getInstance(this);

        initViews();
        setupToolbar();
        loadHouseData(houseId);

        btnConfirm.setOnClickListener(v -> processPayment());
    }

    private void initViews() {
        ivProperty = findViewById(R.id.iv_property_image);
        tvName = findViewById(R.id.tv_property_name);
        tvLocation = findViewById(R.id.tv_property_location);
        tvCheckIn = findViewById(R.id.tv_check_in_date);
        tvCheckOut = findViewById(R.id.tv_check_out_date);
        tvGuests = findViewById(R.id.tv_guests_count);
        tvBasePrice = findViewById(R.id.tv_base_price);
        tvBasePriceLabel = findViewById(R.id.tv_base_price_label);
        tvSecurityDeposit = findViewById(R.id.tv_security_deposit);
        tvServiceFee = findViewById(R.id.tv_service_fee);
        tvTotalPrice = findViewById(R.id.tv_total_price);
        rgPaymentMethods = findViewById(R.id.rg_payment_methods);
        btnConfirm = findViewById(R.id.btn_confirm_booking);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Booking Summary");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadHouseData(String houseId) {
        houseRepository.getHouse(houseId, new HouseRepository.RepositoryCallback<House>() {
            @Override
            public void onSuccess(House houseData) {
                house = houseData;
                displaySummary();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(BookingSummaryActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void displaySummary() {
        if (house == null) return;

        tvName.setText(house.getTitle());
        tvLocation.setText(house.getLocation());
        tvCheckIn.setText(checkInDate);
        tvCheckOut.setText(checkOutDate != null && !checkOutDate.isEmpty() ? checkOutDate : "Not set");
        tvGuests.setText(String.valueOf(tenantCount));

        Glide.with(this)
                .load(house.getImages() != null && !house.getImages().isEmpty() ? house.getImages().get(0) : R.drawable.ic_house_placeholder)
                .placeholder(R.drawable.ic_house_placeholder)
                .into(ivProperty);

        // Price calculation
        double rent = house.getPrice();
        totalAmount = rent + securityDeposit + serviceFee;

        tvBasePrice.setText(house.getFormattedPrice());
        tvSecurityDeposit.setText(String.format("Tsh %,.0f", securityDeposit));
        tvServiceFee.setText(String.format("Tsh %,.0f", serviceFee));
        tvTotalPrice.setText(String.format("Tsh %,.0f", totalAmount));
        tvBasePriceLabel.setText("Rent (1 month)");
    }

    private void processPayment() {
        int selectedId = rgPaymentMethods.getCheckedRadioButtonId();
        String method = "M-Pesa";
        
        if (selectedId == R.id.rb_bank) method = "Bank Transfer";
        else if (selectedId == R.id.rb_card) method = "Card";
        else if (selectedId == R.id.rb_cash) method = "Cash";

        if (method.equals("M-Pesa")) {
            simulateMpesaPush();
        } else {
            finalizeBooking(method, "pending");
        }
    }

    private void simulateMpesaPush() {
        btnConfirm.setEnabled(false);
        btnConfirm.setText("Processing M-Pesa...");
        
        Toast.makeText(this, "STK Push sent to " + sharedPrefManager.getUserPhone(), Toast.LENGTH_LONG).show();
        
        // Simulate payment success after 2 seconds
        new android.os.Handler().postDelayed(() -> {
            finalizeBooking("M-Pesa", "paid");
        }, 2000);
    }

    private void finalizeBooking(String method, String paymentStatus) {
        String bookingId = UUID.randomUUID().toString();
        String ref = "SH-" + System.currentTimeMillis() % 1000000;

        Booking booking = new Booking(bookingId, house.getId(), sharedPrefManager.getUserId(), checkInDate);
        booking.setHouseTitle(house.getTitle());
        booking.setHouseLocation(house.getLocation());
        booking.setTenantName(sharedPrefManager.getUserName());
        booking.setTenantPhone(sharedPrefManager.getUserPhone());
        booking.setOwnerId(house.getOwnerId());
        booking.setOwnerName(house.getOwnerName());
        booking.setMoveOutDate(checkOutDate);
        booking.setNumberOfTenants(tenantCount);
        booking.setTotalPrice(totalAmount);
        booking.setSecurityDeposit(securityDeposit);
        booking.setServiceFee(serviceFee);
        booking.setPaymentMethod(method);
        booking.setPaymentStatus(paymentStatus);
        booking.setReferenceNumber(ref);
        booking.setNotes(notes);
        booking.setCreatedAt(String.valueOf(System.currentTimeMillis()));

        bookingRepository.createBooking(booking, new BookingRepository.RepositoryCallback<String>() {
            @Override
            public void onSuccess(String result) {
                // Update availability
                house.setAvailable(false);
                houseRepository.updateHouse(house, new HouseRepository.RepositoryCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        showSuccess(ref);
                    }

                    @Override
                    public void onError(String error) {
                        showSuccess(ref); // Still show success even if availability update fails locally
                    }
                });

                // Notify owner
                NotificationUtils.showBookingRequestNotification(BookingSummaryActivity.this,
                        sharedPrefManager.getUserName(), house.getTitle());

                // Send SMS to owner
                if (ownerPhone != null && !ownerPhone.isEmpty() && !ownerPhone.equals("No contact info")) {
                    SmsUtils.sendSms(BookingSummaryActivity.this, ownerPhone,
                            "SmartHome: " + sharedPrefManager.getUserName() + " has booked " + house.getTitle() +
                                    ". Payment: " + method + " (" + paymentStatus + "). Ref: " + ref);
                }
            }

            @Override
            public void onError(String error) {
                btnConfirm.setEnabled(true);
                btnConfirm.setText("Confirm Booking");
                Toast.makeText(BookingSummaryActivity.this, "Booking failed: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showSuccess(String ref) {
        Intent intent = new Intent(this, ReceiptActivity.class);
        intent.putExtra("booking_id", ref); // Using ref as ID for demo summary
        intent.putExtra("is_new_booking", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
