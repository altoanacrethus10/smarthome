package com.example.smarthome.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.smarthome.R;
import com.example.smarthome.models.Booking;
import com.example.smarthome.repository.BookingRepository;
import com.example.smarthome.utils.BluetoothUtils;
import com.example.smarthome.utils.DateFormatter;
import com.example.smarthome.utils.SharedPrefManager;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class ReportActivity extends AppCompatActivity {

    private TextView tvReportDate, tvTotalBookings, tvTotalRent, tvTotalDeposit, tvHistory;
    private BookingRepository bookingRepository;
    private SharedPrefManager sharedPrefManager;
    private String reportText = "SmartHome report is loading...";
    private boolean isOwner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        bookingRepository = BookingRepository.getInstance(this);
        sharedPrefManager = SharedPrefManager.getInstance(this);
        isOwner = sharedPrefManager.isOwner();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(isOwner ? "Earnings Report" : "Payment History");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        tvReportDate = findViewById(R.id.tv_report_date);
        tvTotalBookings = findViewById(R.id.tv_report_total_bookings);
        tvTotalRent = findViewById(R.id.tv_report_total_rent);
        tvTotalDeposit = findViewById(R.id.tv_report_total_deposit);
        tvHistory = findViewById(R.id.tv_report_history);
        MaterialButton btnShare = findViewById(R.id.btn_share_report);
        MaterialButton btnBluetooth = findViewById(R.id.btn_bluetooth_report);

        tvReportDate.setText(DateFormatter.getCurrentDateTime());
        loadReport();

        btnShare.setOnClickListener(v -> shareReport());
        btnBluetooth.setOnClickListener(v -> BluetoothUtils.shareViaBluetooth(this, reportText));
    }

    private void loadReport() {
        String userId = sharedPrefManager.getUserId();
        if (userId == null) return;

        BookingRepository.RepositoryCallback<List<Booking>> callback = new BookingRepository.RepositoryCallback<List<Booking>>() {
            @Override
            public void onSuccess(List<Booking> bookings) { buildReport(bookings); }
            @Override
            public void onError(String error) { Toast.makeText(ReportActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show(); }
        };

        if (isOwner) bookingRepository.getBookingsByOwner(userId, callback);
        else bookingRepository.getBookingsByTenant(userId, callback);
    }

    private void buildReport(List<Booking> bookings) {
        int totalBookings = 0;
        double totalRent = 0;
        StringBuilder historyBuilder = new StringBuilder();

        if (bookings != null) {
            for (Booking b : bookings) {
                if ("paid".equalsIgnoreCase(b.getPaymentStatus()) || "confirmed".equalsIgnoreCase(b.getStatus())) {
                    totalBookings++;
                    totalRent += b.getTotalPrice();
                    historyBuilder.append("- ").append(b.getHouseTitle()).append(" | ").append(formatMoney(b.getTotalPrice())).append("\n");
                }
            }
        }

        if (historyBuilder.length() == 0) historyBuilder.append("No transactions found.");

        tvTotalBookings.setText(String.valueOf(totalBookings));
        tvTotalRent.setText(formatMoney(totalRent));
        tvTotalDeposit.setText(formatMoney(totalRent * 0.1)); // Example calculation
        tvHistory.setText(historyBuilder.toString());

        reportText = (isOwner ? "EARNINGS REPORT" : "PAYMENT HISTORY") + "\n"
                + "Date: " + tvReportDate.getText() + "\n"
                + "Total: " + totalBookings + "\n"
                + "Amount: " + formatMoney(totalRent);
    }

    private void shareReport() {
        Intent intent = new Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, reportText);
        startActivity(Intent.createChooser(intent, "Share Report"));
    }

    private String formatMoney(double amount) {
        return String.format("Tsh %,.0f", amount);
    }
}
