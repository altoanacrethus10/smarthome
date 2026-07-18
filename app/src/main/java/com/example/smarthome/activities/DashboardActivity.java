package com.example.smarthome.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smarthome.R;

public class DashboardActivity extends AppCompatActivity {

    private TextView tvTotalHouses, tvTotalTenants, tvTotalBookings;
    private RecyclerView rvRecentHouses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize views
        tvTotalHouses = findViewById(R.id.tv_total_houses);
        tvTotalTenants = findViewById(R.id.tv_total_tenants);
        tvTotalBookings = findViewById(R.id.tv_total_bookings);
        rvRecentHouses = findViewById(R.id.rv_recent_houses);

        // Setup RecyclerView
        rvRecentHouses.setLayoutManager(new LinearLayoutManager(this));

        // Load data
        loadDashboardData();

        findViewById(R.id.btn_open_report).setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, ReportActivity.class)));
    }

    private void loadDashboardData() {
        // In a real app, fetch from database
        tvTotalHouses.setText("24");
        tvTotalTenants.setText("156");
        tvTotalBookings.setText("12");
    }
}
