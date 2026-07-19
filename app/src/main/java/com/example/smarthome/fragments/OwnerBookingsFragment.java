package com.example.smarthome.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.smarthome.R;
import com.example.smarthome.activities.CallActivity;
import com.example.smarthome.activities.ChatActivity;
import com.example.smarthome.activities.ReceiptActivity;
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

public class OwnerBookingsFragment extends Fragment implements BookingAdapter.OnBookingInteractionListener {

    private RecyclerView rvBookings;
    private SwipeRefreshLayout swipeRefresh;
    private View emptyState;
    private BookingAdapter adapter;
    private final List<Booking> bookingList = new ArrayList<>();

    private BookingRepository bookingRepository;
    private ContractRepository contractRepository;
    private HouseRepository houseRepository;
    private SharedPrefManager sharedPrefManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_owner_bookings, container, false);

        sharedPrefManager = SharedPrefManager.getInstance(requireContext());
        bookingRepository = BookingRepository.getInstance(requireContext());
        contractRepository = ContractRepository.getInstance(requireContext());
        houseRepository = HouseRepository.getInstance(requireContext());

        rvBookings = view.findViewById(R.id.rv_bookings);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        emptyState = view.findViewById(R.id.empty_state);

        adapter = new BookingAdapter(requireContext(), bookingList, true, this);
        rvBookings.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvBookings.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(this::loadBookings);
        swipeRefresh.setColorSchemeResources(R.color.primary);

        loadBookings();

        return view;
    }

    private void loadBookings() {
        swipeRefresh.setRefreshing(true);
        String ownerId = sharedPrefManager.getUserId();

        bookingRepository.getBookingsByOwner(ownerId, new BookingRepository.RepositoryCallback<List<Booking>>() {
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
                Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                emptyState.setVisibility(bookingList.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public void onAccept(Booking booking) {
        updateStatus(booking, "accepted");
    }

    @Override
    public void onReject(Booking booking) {
        new MaterialAlertDialogBuilder(requireContext())
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
                Toast.makeText(getContext(), "Booking " + status, Toast.LENGTH_SHORT).show();
                if ("accepted".equalsIgnoreCase(status) || "paid".equalsIgnoreCase(status)) {
                    NotificationUtils.showBookingConfirmedNotification(requireContext(), booking.getHouseTitle());
                }
                loadBookings();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Failed to update: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onPay(Booking booking) {
        simulatePayment(booking);
    }

    private void simulatePayment(Booking booking) {
        new MaterialAlertDialogBuilder(requireContext())
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
                NotificationUtils.showBookingConfirmedNotification(requireContext(), booking.getHouseTitle());
                createContractAndFinish(booking);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Payment failed: " + error, Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(getContext(), "Payment Successful! House Rented.", Toast.LENGTH_LONG).show();
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
                Toast.makeText(getContext(), "Contract creation failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onChat(Booking booking) {
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra("other_user_id", booking.getTenantId());
        intent.putExtra("other_user_name", booking.getTenantName());
        intent.putExtra("house_id", booking.getHouseId());
        startActivity(intent);
    }

    @Override
    public void onCall(Booking booking) {
        Intent intent = new Intent(getActivity(), CallActivity.class);
        intent.putExtra("caller_name", booking.getTenantName());
        intent.putExtra("house_title", booking.getHouseTitle());
        startActivity(intent);
    }

    @Override
    public void onViewReceipt(Booking booking) {
        Intent intent = new Intent(getActivity(), ReceiptActivity.class);
        intent.putExtra("booking_id", booking.getId());
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (bookingRepository != null && sharedPrefManager != null) {
            loadBookings();
        }
    }
}
