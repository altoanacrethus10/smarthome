package com.example.smarthome.activities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.smarthome.R;

public class BookingDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
            getSupportActionBar().setTitle("Booking Details");
        }

        String bookingId = getIntent().getStringExtra("booking_id");
        TextView tv = findViewById(R.id.tv_booking_detail_booking_id);
        tv.setText(bookingId != null ? bookingId : "");
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

