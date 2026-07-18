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
import com.example.smarthome.activities.AddListingActivity;
import com.example.smarthome.activities.HouseDetailActivity;
import com.example.smarthome.adapters.HouseAdapter;
import com.example.smarthome.models.House;
import com.example.smarthome.repository.HouseRepository;
import com.example.smarthome.utils.SharedPrefManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MyListingsFragment extends Fragment {

    private RecyclerView rvMyListings;
    private HouseAdapter houseAdapter;
    private final List<House> houseList = new ArrayList<>();
    private HouseRepository houseRepository;
    private SharedPrefManager sharedPrefManager;
    private View emptyView;
    private FloatingActionButton fabAddListing;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_listings, container, false);

        houseRepository = HouseRepository.getInstance(getContext());
        sharedPrefManager = SharedPrefManager.getInstance(getContext());

        rvMyListings = view.findViewById(R.id.rv_my_listings);
        emptyView = view.findViewById(R.id.ll_empty_listings);
        fabAddListing = view.findViewById(R.id.fab_add_listing);
        
        setupRecyclerView();
        setupFab();
        loadMyListings();

        return view;
    }

    private void setupRecyclerView() {
        houseAdapter = new HouseAdapter(getContext(), houseList, new HouseAdapter.OnHouseClickListener() {
            @Override
            public void onHouseClick(House house) {
                Intent intent = new Intent(getActivity(), HouseDetailActivity.class);
                intent.putExtra("house_id", house.getId());
                startActivity(intent);
            }

            @Override
            public void onRentClick(House house) {}
        });
        rvMyListings.setLayoutManager(new LinearLayoutManager(getContext()));
        rvMyListings.setAdapter(houseAdapter);
    }

    private void setupFab() {
        if (fabAddListing != null) {
            fabAddListing.setOnClickListener(v -> {
                if (sharedPrefManager != null && sharedPrefManager.isOwner()) {
                    startActivity(new Intent(getActivity(), AddListingActivity.class));
                } else {
                    Toast.makeText(getContext(), "Only owners can add listings", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadMyListings() {
        String userId = sharedPrefManager.getUserId();
        if (userId == null) return;

        houseRepository.getHousesByOwner(userId, new HouseRepository.RepositoryCallback<List<House>>() {
            @Override
            public void onSuccess(List<House> result) {
                houseList.clear();
                if (result != null) {
                    // Defensive deduplication: ensure each house appears only once by id
                    Map<String, House> uniqueById = new LinkedHashMap<>();
                    for (House h : result) {
                        if (h == null) continue;
                        String id = h.getId();
                        if (id == null) continue;
                        uniqueById.put(id, h);
                    }
                    houseList.addAll(uniqueById.values());
                }
                
                if (houseList.isEmpty()) {
                    rvMyListings.setVisibility(View.GONE);
                    if (emptyView != null) emptyView.setVisibility(View.VISIBLE);
                } else {
                    rvMyListings.setVisibility(View.VISIBLE);
                    if (emptyView != null) emptyView.setVisibility(View.GONE);
                    houseAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadMyListings();
    }
}
