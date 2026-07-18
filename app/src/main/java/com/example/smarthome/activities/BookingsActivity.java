package com.example.smarthome.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.smarthome.R;
import com.example.smarthome.adapters.BookingAdapter;
import com.example.smarthome.models.Booking;
import com.example.smarthome.models.Contract;
import com.example.smarthome.repository.BookingRepository;
import com.example.smarthome.repository.ContractRepository;
import com.example.smarthome.repository.HouseRepository;
import com.example.smarthome.utils.NotificationUtils;
import com.example.smarthome.utils.SharedPrefManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BookingsActivity extends AppCompatActivity implements BookingAdapter.OnBookingInteractionListener {

    private RecyclerView rvBookings;
    private SwipeRefreshLayout swipeRefresh;
    private View emptyState;
    private BookingAdapter adapter;
    private List<Booking> bookingList = new ArrayList<>();
    
    private BookingRepository bookingRepository;
    private ContractRepository contractRepository;
    private HouseRepository houseRepository;
    private SharedPrefManager sharedPrefManager;
    private boolean isOwner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookings);

        sharedPrefManager = SharedPrefManager.getInstance(this);
        bookingRepository = BookingRepository.getInstance(this);
        contractRepository = ContractRepository.getInstance(this);
        houseRepository = HouseRepository.getInstance(this);
        isOwner = sharedPrefManager.isOwner();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(isOwner ? "Booking Requests" : "My Bookings");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        rvBookings = findViewById(R.id.rv_bookings);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        emptyState = findViewById(R.id.empty_state);

        adapter = new BookingAdapter(this, bookingList, isOwner, this);
        rvBookings.setLayoutManager(new LinearLayoutManager(this));
        rvBookings.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(this::loadBookings);
        swipeRefresh.setColorSchemeResources(R.color.primary);

        loadBookings();
    }

    private void loadBookings() {
        swipeRefresh.setRefreshing(true);
        String userId = sharedPrefManager.getUserId();
        
        BookingRepository.RepositoryCallback<List<Booking>> callback = new BookingRepository.RepositoryCallback<List<Booking>>() {
            @Override
            public void onSuccess(List<Booking> result) {
                swipeRefresh.setRefreshing(false);
                bookingList.clear();
                if (result != null) {
                    bookingList.addAll(result);
                }
                adapter.notifyDataSetChanged();
                emptyState.setVisibility(bookingList.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(String error) {
                swipeRefresh.setRefreshing(false);
                Toast.makeText(BookingsActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                emptyState.setVisibility(bookingList.isEmpty() ? View.VISIBLE : View.GONE);
            }
        };

        if (isOwner) {
            bookingRepository.getBookingsByOwner(userId, callback);
        } else {
            bookingRepository.getBookingsByTenant(userId, callback);
        }
    }

    @Override
    public void onAccept(Booking booking) {
        updateStatus(booking, "accepted");
    }

    @Override
    public void onReject(Booking booking) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Reject Booking")
                .setMessage("Are you sure you want to reject this booking request?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Reject", (dialog, which) -> updateStatus(booking, "rejected"))
                .show();
    }

    private void updateStatus(Booking booking, String status) {
        bookingRepository.updateBookingStatus(booking.getId(), status, new BookingRepository.RepositoryCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                Toast.makeText(BookingsActivity.this, "Booking " + status, Toast.LENGTH_SHORT).show();
                if ("accepted".equalsIgnoreCase(status) || "paid".equalsIgnoreCase(status)) {
                    NotificationUtils.showBookingConfirmedNotification(BookingsActivity.this, booking.getHouseTitle());
                }
                loadBookings();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(BookingsActivity.this, "Failed to update: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onPay(Booking booking) {
        simulatePayment(booking);
    }

    private void simulatePayment(Booking booking) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Simulate Payment")
                .setMessage("Total Amount: Tsh " + String.format("%,.0f", booking.getTotalPrice()) + "\n\nThis is a simulation. Click Confirm to pay.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Confirm Payment", (dialog, which) -> processPayment(booking))
                .show();
    }

    private void processPayment(Booking booking) {
        bookingRepository.updateBookingStatus(booking.getId(), "paid", new BookingRepository.RepositoryCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                NotificationUtils.showBookingConfirmedNotification(BookingsActivity.this, booking.getHouseTitle());
                createContractAndFinish(booking);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(BookingsActivity.this, "Payment failed: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createContractAndFinish(Booking booking) {
        String contractId = UUID.randomUUID().toString();
        long now = System.currentTimeMillis();
        long sixMonthsMs = 6L * 30 * 24 * 60 * 60 * 1000;
        
        Contract contract = new Contract(
                contractId,
                booking.getTenantId(),
                booking.getOwnerId(),
                booking.getHouseId(),
                String.valueOf(now),
                String.valueOf(now + sixMonthsMs)
        );
        contract.setTenantName(booking.getTenantName());
        contract.setOwnerName(booking.getOwnerName());
        contract.setHouseTitle(booking.getHouseTitle());
        contract.setMonthlyRent(booking.getTotalPrice());
        contract.setCreatedAt(String.valueOf(now));

        contractRepository.createContract(contract, new ContractRepository.RepositoryCallback<String>() {
            @Override
            public void onSuccess(String result) {
                // Also update house status to rented
                houseRepository.getHouse(booking.getHouseId(), new HouseRepository.RepositoryCallback<com.example.smarthome.models.House>() {
                    @Override
                    public void onSuccess(com.example.smarthome.models.House house) {
                        house.setAvailable(false);
                        houseRepository.updateHouse(house, new HouseRepository.RepositoryCallback<Boolean>() {
                            @Override
                            public void onSuccess(Boolean result) {
                                Toast.makeText(BookingsActivity.this, "Payment Successful! House Rented.", Toast.LENGTH_LONG).show();
                                loadBookings();
                            }

                            @Override
                            public void onError(String error) {
                                loadBookings();
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        loadBookings();
                    }
                });
            }

            @Override
            public void onError(String error) {
                Toast.makeText(BookingsActivity.this, "Contract creation failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onChat(Booking booking) {
        Intent intent = new Intent(this, ChatActivity.class);
        if (isOwner) {
            intent.putExtra("other_user_id", booking.getTenantId());
            intent.putExtra("other_user_name", booking.getTenantName());
        } else {
            intent.putExtra("other_user_id", booking.getOwnerId());
            intent.putExtra("other_user_name", booking.getOwnerName());
        }
        intent.putExtra("house_id", booking.getHouseId());
        startActivity(intent);
    }

    @Override
    public void onCall(Booking booking) {
        Intent intent = new Intent(this, CallActivity.class);
        if (isOwner) {
            intent.putExtra("caller_name", booking.getTenantName());
        } else {
            intent.putExtra("caller_name", booking.getOwnerName());
        }
        intent.putExtra("house_title", booking.getHouseTitle());
        startActivity(intent);
    }

    @Override
    public void onViewReceipt(Booking booking) {
        Intent intent = new Intent(this, ReceiptActivity.class);
        intent.putExtra("booking_id", booking.getId());
        startActivity(intent);
    }
}
