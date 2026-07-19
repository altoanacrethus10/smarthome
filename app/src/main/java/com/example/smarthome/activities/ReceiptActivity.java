package com.example.smarthome.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintManager;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.example.smarthome.R;
import com.example.smarthome.models.Booking;
import com.example.smarthome.repository.BookingRepository;
import com.example.smarthome.utils.BluetoothUtils;
import com.example.smarthome.utils.DateFormatter;
import com.example.smarthome.utils.SharedPrefManager;

public class ReceiptActivity extends AppCompatActivity {

    private TextView tvReceiptNumber, tvReceiptDate, tvTenantName, tvTenantPhone;
    private TextView tvPropertyLocation, tvPropertyDetails;
    private TextView tvRentAmount, tvDepositAmount, tvTotalAmount;
    private MaterialButton btnShareReceipt, btnBluetoothReceipt, btnPrintReceipt, btnViewBookings;
    private SharedPrefManager sharedPrefManager;
    private BookingRepository bookingRepository;
    private String bookingId;
    private boolean isNewBooking = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);

        sharedPrefManager = SharedPrefManager.getInstance(this);
        bookingRepository = BookingRepository.getInstance(this);

        bookingId = getIntent().getStringExtra("booking_id");
        isNewBooking = getIntent().getBooleanExtra("is_new_booking", false);

        if (bookingId == null) {
            Toast.makeText(this, "Booking ID missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNewBooking) {
                    startActivity(new Intent(ReceiptActivity.this, MainActivity.class));
                    finish();
                } else {
                    finish();
                }
            }
        });

        // Initialize views
        tvReceiptNumber = findViewById(R.id.tv_receipt_number);
        tvReceiptDate = findViewById(R.id.tv_receipt_date);
        tvTenantName = findViewById(R.id.tv_tenant_name);
        tvTenantPhone = findViewById(R.id.tv_tenant_phone);
        tvPropertyLocation = findViewById(R.id.tv_property_location);
        tvPropertyDetails = findViewById(R.id.tv_property_details);
        tvRentAmount = findViewById(R.id.tv_rent_amount);
        tvDepositAmount = findViewById(R.id.tv_deposit_amount);
        tvTotalAmount = findViewById(R.id.tv_total_amount);
        btnShareReceipt = findViewById(R.id.btn_share_receipt);
        btnBluetoothReceipt = findViewById(R.id.btn_bluetooth_receipt);
        btnPrintReceipt = findViewById(R.id.btn_print_receipt);
        btnViewBookings = findViewById(R.id.btn_view_my_bookings);

        if (isNewBooking) {
            findViewById(R.id.ll_success_message).setVisibility(View.VISIBLE);
            btnViewBookings.setVisibility(View.VISIBLE);
        }

        // Load receipt data
        loadReceiptData();

        btnViewBookings.setOnClickListener(v -> {
            Intent intent = new Intent(ReceiptActivity.this, MainActivity.class);
            intent.putExtra(MainActivity.EXTRA_OPEN_SECTION, "bookings");
            startActivity(intent);
            finish();
        });

        // Share button click
        btnShareReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareReceipt();
            }
        });

        btnBluetoothReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BluetoothUtils.shareReceiptViaBluetooth(ReceiptActivity.this, buildReceiptText());
            }
        });

        // Print button click
        btnPrintReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printReceipt();
            }
        });
    }

    private void loadReceiptData() {
        bookingRepository.getBooking(bookingId, new BookingRepository.RepositoryCallback<Booking>() {
            @Override
            public void onSuccess(Booking booking) {
                if (booking.getReferenceNumber() != null) {
                    tvReceiptNumber.setText(booking.getReferenceNumber());
                } else {
                    tvReceiptNumber.setText("RCP-" + booking.getId().substring(0, 8).toUpperCase());
                }
                
                tvReceiptDate.setText(DateFormatter.getCurrentDate());

                tvTenantName.setText("Name: " + booking.getTenantName());
                tvTenantPhone.setText("Phone: " + booking.getTenantPhone());

                tvPropertyLocation.setText("Location: " + booking.getHouseLocation());
                tvPropertyDetails.setText(booking.getHouseTitle());
                
                tvRentAmount.setText(String.format("Tsh %,.0f", booking.getTotalPrice() - booking.getSecurityDeposit() - booking.getServiceFee()));
                tvDepositAmount.setText(String.format("Tsh %,.0f", booking.getSecurityDeposit()));
                tvTotalAmount.setText(String.format("Tsh %,.0f", booking.getTotalPrice()));
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ReceiptActivity.this, "Error loading booking: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void shareReceipt() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, buildReceiptText());
        startActivity(Intent.createChooser(shareIntent, "Share Receipt"));
    }

    private void printReceipt() {
        PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
        WebView webView = new WebView(this);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                android.print.PrintDocumentAdapter adapter = webView.createPrintDocumentAdapter(
                        "Receipt_" + tvReceiptNumber.getText().toString());
                printManager.print(
                        "SmartHome Receipt " + tvReceiptNumber.getText().toString(),
                        adapter,
                        new PrintAttributes.Builder().build());
            }
        });
        webView.loadDataWithBaseURL(null, buildReceiptHtml(), "text/HTML", "UTF-8", null);
    }

    private String buildReceiptHtml() {
        return "<html><body style=\"font-family:monospace;white-space:pre-wrap;font-size:14px;\">"
                + buildReceiptText().replace("\n", "<br>")
                + "</body></html>";
    }

    private String buildReceiptText() {
        return "SMARTHOME RECEIPT\n" +
                "==================\n" +
                "Receipt: " + tvReceiptNumber.getText().toString() + "\n" +
                "Date: " + tvReceiptDate.getText().toString() + "\n" +
                "Tenant: " + tvTenantName.getText().toString() + "\n" +
                "Property: " + tvPropertyLocation.getText().toString() + "\n" +
                "Rent: " + tvRentAmount.getText().toString() + "\n" +
                "Deposit: " + tvDepositAmount.getText().toString() + "\n" +
                "Total: " + tvTotalAmount.getText().toString() + "\n" +
                "==================\n" +
                "Thank you for choosing SmartHome!";
    }
}
