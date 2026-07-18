package com.example.smarthome.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smarthome.R;
import com.example.smarthome.adapters.ComplaintAdapter;
import com.example.smarthome.models.Booking;
import com.example.smarthome.models.Complaint;
import com.example.smarthome.repository.BookingRepository;
import com.example.smarthome.repository.ComplaintRepository;
import com.example.smarthome.utils.SharedPrefManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ComplaintsActivity extends AppCompatActivity {

    private RecyclerView rvComplaints;
    private View llEmpty;
    private View layoutList, layoutForm;
    private FloatingActionButton fabAdd;
    private TextInputEditText etSubject, etCategory, etDescription;
    private MaterialButton btnSubmit;
    private android.widget.Spinner spinnerHouse;

    private ComplaintAdapter adapter;
    private List<Complaint> complaintList = new ArrayList<>();
    private List<Booking> tenantBookings = new ArrayList<>();
    
    private ComplaintRepository complaintRepository;
    private com.example.smarthome.repository.BookingRepository bookingRepository;
    private SharedPrefManager sharedPrefManager;
    private boolean isOwner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaints);

        complaintRepository = ComplaintRepository.getInstance(this);
        bookingRepository = BookingRepository.getInstance(this);
        sharedPrefManager = SharedPrefManager.getInstance(this);
        isOwner = sharedPrefManager.isOwner();

        setupToolbar();
        initViews();
        setupRecyclerView();
        loadComplaints();

        if (isOwner) {
            fabAdd.setVisibility(View.GONE);
        } else {
            fabAdd.setOnClickListener(v -> toggleView(true));
            btnSubmit.setOnClickListener(v -> submitComplaint());
            loadTenantBookings();
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(isOwner ? "Tenant Complaints" : "My Complaints");
        }
        toolbar.setNavigationOnClickListener(v -> {
            if (layoutForm.getVisibility() == View.VISIBLE) {
                toggleView(false);
            } else {
                finish();
            }
        });
    }

    private void initViews() {
        rvComplaints = findViewById(R.id.rv_complaints);
        llEmpty = findViewById(R.id.ll_empty_complaints);
        layoutList = findViewById(R.id.layout_complaint_list);
        layoutForm = findViewById(R.id.layout_complaint_form);
        fabAdd = findViewById(R.id.fab_add_complaint);
        
        etSubject = findViewById(R.id.et_complaint_subject);
        spinnerHouse = findViewById(R.id.spinner_complaint_house);
        etCategory = findViewById(R.id.et_complaint_category);
        etDescription = findViewById(R.id.et_complaint_description);
        btnSubmit = findViewById(R.id.btn_submit_complaint);
    }

    private void setupRecyclerView() {
        adapter = new ComplaintAdapter(this, complaintList, complaint -> {
            if (isOwner) {
                showResponseDialog(complaint);
            }
        });
        rvComplaints.setLayoutManager(new LinearLayoutManager(this));
        rvComplaints.setAdapter(adapter);
    }

    private void toggleView(boolean showForm) {
        layoutList.setVisibility(showForm ? View.GONE : View.VISIBLE);
        layoutForm.setVisibility(showForm ? View.VISIBLE : View.GONE);
        fabAdd.setVisibility(showForm || isOwner ? View.GONE : View.VISIBLE);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(showForm ? "File a Complaint" : (isOwner ? "Tenant Complaints" : "My Complaints"));
        }
    }

    private void loadComplaints() {
        String userId = sharedPrefManager.getUserId();
        
        ComplaintRepository.RepositoryCallback<List<Complaint>> callback = new ComplaintRepository.RepositoryCallback<List<Complaint>>() {
            @Override
            public void onSuccess(List<Complaint> result) {
                complaintList.clear();
                if (result != null) {
                    complaintList.addAll(result);
                }
                adapter.notifyDataSetChanged();
                llEmpty.setVisibility(complaintList.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ComplaintsActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        };

        if (isOwner) {
            complaintRepository.getComplaintsByOwner(userId, callback);
        } else {
            complaintRepository.getComplaintsByUser(userId, callback);
        }
    }

    private void loadTenantBookings() {
        bookingRepository.getBookingsByTenant(sharedPrefManager.getUserId(), new BookingRepository.RepositoryCallback<List<Booking>>() {
            @Override
            public void onSuccess(List<Booking> result) {
                tenantBookings.clear();
                if (result != null) tenantBookings.addAll(result);
                
                List<String> houseTitles = new ArrayList<>();
                houseTitles.add("General / Other");
                for (Booking b : tenantBookings) houseTitles.add(b.getHouseTitle());
                
                android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
                        ComplaintsActivity.this, android.R.layout.simple_spinner_item, houseTitles);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerHouse.setAdapter(adapter);
            }

            @Override
            public void onError(String error) {}
        });
    }

    private void submitComplaint() {
        String subject = etSubject.getText().toString().trim();
        String category = etCategory.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (subject.isEmpty()) { etSubject.setError("Required"); return; }
        if (description.isEmpty()) { etDescription.setError("Required"); return; }

        btnSubmit.setEnabled(false);
        btnSubmit.setText("Submitting...");

        Complaint complaint = new Complaint(UUID.randomUUID().toString(), sharedPrefManager.getUserId(), subject, description, category);
        complaint.setUserName(sharedPrefManager.getUserName());
        complaint.setUserEmail(sharedPrefManager.getUserEmail());

        int houseIndex = spinnerHouse.getSelectedItemPosition();
        if (houseIndex > 0) { // Not General
            Booking selectedBooking = tenantBookings.get(houseIndex - 1);
            complaint.setHouseId(selectedBooking.getHouseId());
            complaint.setHouseTitle(selectedBooking.getHouseTitle());
            complaint.setOwnerId(selectedBooking.getOwnerId());
        }

        complaintRepository.createComplaint(complaint, new ComplaintRepository.RepositoryCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Toast.makeText(ComplaintsActivity.this, "Complaint filed", Toast.LENGTH_SHORT).show();
                toggleView(false);
                loadComplaints();
                btnSubmit.setEnabled(true);
                btnSubmit.setText("Submit Complaint");
                clearForm();
            }

            @Override
            public void onError(String error) {
                btnSubmit.setEnabled(true);
                btnSubmit.setText("Submit Complaint");
                Toast.makeText(ComplaintsActivity.this, "Failed: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearForm() {
        etSubject.setText("");
        etCategory.setText("");
        etDescription.setText("");
    }

    private void showResponseDialog(Complaint complaint) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_complaint_response, null);
        TextInputEditText etResponse = dialogView.findViewById(R.id.et_complaint_response);
        
        new MaterialAlertDialogBuilder(this)
                .setTitle("Respond to Complaint")
                .setView(dialogView)
                .setPositiveButton("Resolve", (dialog, which) -> {
                    String response = etResponse.getText().toString().trim();
                    updateComplaint(complaint.getId(), "resolved", response);
                })
                .setNeutralButton("Dismiss", null)
                .show();
    }

    private void updateComplaint(String id, String status, String response) {
        complaintRepository.updateComplaintStatus(id, status, response, new ComplaintRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(ComplaintsActivity.this, "Complaint updated", Toast.LENGTH_SHORT).show();
                loadComplaints();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ComplaintsActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (layoutForm.getVisibility() == View.VISIBLE) {
            toggleView(false);
        } else {
            super.onBackPressed();
        }
    }
}
