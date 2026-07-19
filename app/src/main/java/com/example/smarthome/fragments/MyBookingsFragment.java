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

import com.example.smarthome.R;
import com.example.smarthome.activities.BookingDetailActivity;
import com.example.smarthome.adapters.MyBookingsTenantAdapter;
import com.example.smarthome.models.Booking;
import com.example.smarthome.repository.BookingRepository;
import com.example.smarthome.utils.SharedPrefManager;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class MyBookingsFragment extends Fragment {

    private TabLayout tabLayout;
    private RecyclerView rvBookings;

    private View emptyState;

    private BookingRepository bookingRepository;
    private SharedPrefManager sharedPrefManager;

    private final List<Booking> allBookings = new ArrayList<>();

    private MyBookingsTenantAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_bookings, container, false);

        tabLayout = view.findViewById(R.id.tab_layout);
        rvBookings = view.findViewById(R.id.rv_bookings);
        emptyState = view.findViewById(R.id.empty_state);

        rvBookings.setLayoutManager(new LinearLayoutManager(requireContext()));

        sharedPrefManager = SharedPrefManager.getInstance(requireContext());
        bookingRepository = BookingRepository.getInstance(requireContext());

        adapter = new MyBookingsTenantAdapter(requireContext(), new ArrayList<>(), new MyBookingsTenantAdapter.Listener() {
            @Override
            public void onViewDetails(Booking booking) {
                // BookingDetailActivity may not exist in this codebase yet; keep navigation consistent with requirement.
                Intent intent = new Intent(getActivity(), BookingDetailActivity.class);
                intent.putExtra("booking_id", booking.getId());
                startActivity(intent);
            }

            @Override
            public void onCancelBooking(Booking booking) {
                // Actual cancellation + confirmation dialog handled in adapter for UI coupling; requirement says confirm dialog.
                adapter.requestCancel(booking);
            }
        });
        rvBookings.setAdapter(adapter);

        setupTabs();
        loadBookingsOnce();

        return view;
    }

    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterAndRender(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                filterAndRender(tab.getPosition());
            }
        });

        // Default: Upcoming (0)
        TabLayout.Tab firstTab = tabLayout.getTabAt(0);
        if (firstTab != null) {
            firstTab.select();
        }
    }

    private void loadBookingsOnce() {
        // NOTE: requirement asks real-time listener. This step currently loads once using existing repository.
        // We will upgrade repository to use snapshot listener when implementing the full feature.
        String userId = sharedPrefManager.getUserId();
        if (userId == null) {
            Toast.makeText(getContext(), "Please login", Toast.LENGTH_SHORT).show();
            return;
        }

        bookingRepository.getBookingsByTenant(userId, new BookingRepository.RepositoryCallback<List<Booking>>() {
            @Override
            public void onSuccess(List<Booking> result) {
                allBookings.clear();
                if (result != null) allBookings.addAll(result);
                filterAndRender(tabLayout.getSelectedTabPosition());
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_LONG).show();
                renderEmpty();
            }
        });
    }

    private void filterAndRender(int tabPosition) {
        List<Booking> filtered = new ArrayList<>();

        // tabPosition: 0 Upcoming, 1 Past, 2 Cancelled
        for (Booking b : allBookings) {
            if (b == null) continue;
            String s = b.getStatus() == null ? "" : b.getStatus().toLowerCase();

            if (tabPosition == 0) {
                // Upcoming: pending + confirmed
                if ("pending".equals(s) || "confirmed".equals(s)) filtered.add(b);
            } else if (tabPosition == 1) {
                // Past: completed
                if ("completed".equals(s)) filtered.add(b);
            } else {
                // Cancelled
                if ("cancelled".equals(s)) filtered.add(b);
            }
        }

        adapter.submitList(filtered);
        boolean empty = filtered.isEmpty();
        emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    private void renderEmpty() {
        adapter.submitList(new ArrayList<>());
        emptyState.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (bookingRepository != null && sharedPrefManager != null) {
            loadBookingsOnce();
        }
    }
}
