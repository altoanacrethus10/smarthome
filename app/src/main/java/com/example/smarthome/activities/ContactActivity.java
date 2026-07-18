package com.example.smarthome.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.smarthome.R;

public class ContactActivity extends AppCompatActivity {

    private View tvCopyEmail, tvCallPhone, tvWhatsapp, tvLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

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
        tvCopyEmail = findViewById(R.id.tv_copy_email);
        tvCallPhone = findViewById(R.id.tv_call_phone);
        tvWhatsapp = findViewById(R.id.tv_whatsapp);
        tvLocation = findViewById(R.id.tv_location);

        // Copy email
        tvCopyEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyToClipboard("support@smarthome.com");
                Toast.makeText(ContactActivity.this, "Email copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        });

        // Call phone
        tvCallPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:+255700000000"));
                startActivity(intent);
            }
        });

        // WhatsApp
        tvWhatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://wa.me/255700000000"));
                startActivity(intent);
            }
        });

        // Location
        tvLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("geo:-6.7761,39.2025?q=Dar+es+Salaam+Tanzania"));
                startActivity(intent);
            }
        });
    }

    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Email", text);
        clipboard.setPrimaryClip(clip);
    }
}
