package com.example.smarthome.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smarthome.R;
import com.example.smarthome.activities.HouseDetailActivity;
import com.example.smarthome.adapters.HouseAdapter;
import com.example.smarthome.models.House;
import com.example.smarthome.repository.HouseRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BrowseHousesFragment extends Fragment {

    private EditText etSearch;
    private ImageView ivSearch;
    private Spinner spinnerLocation, spinnerPrice, spinnerBedrooms;
    private RecyclerView rvHouses;
    private HouseAdapter houseAdapter;
    private List<House> houseList;
    private List<House> filteredList;
    private HouseRepository houseRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_browse_houses, container, false);

        houseRepository = HouseRepository.getInstance(getContext());

        // Initialize views
        etSearch = view.findViewById(R.id.et_search);
        ivSearch = view.findViewById(R.id.iv_search);
        spinnerLocation = view.findViewById(R.id.spinner_location);
        spinnerPrice = view.findViewById(R.id.spinner_price);
        spinnerBedrooms = view.findViewById(R.id.spinner_bedrooms);
        rvHouses = view.findViewById(R.id.rv_houses);

        // Setup RecyclerView
        setupRecyclerView();

        // Load houses
        loadHouses();

        // Search click listener
        ivSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performSearch();
            }
        });
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                performSearch();
                return true;
            }
            return false;
        });

        return view;
    }

    private void setupRecyclerView() {
        houseList = new ArrayList<>();
        filteredList = new ArrayList<>();
        houseAdapter = new HouseAdapter(getContext(), filteredList, new HouseAdapter.OnHouseClickListener() {
            @Override
            public void onHouseClick(House house) {
                Intent intent = new Intent(getActivity(), HouseDetailActivity.class);
                intent.putExtra("house_id", house.getId());
                startActivity(intent);
            }

            @Override
            public void onRentClick(House house) {}
        });
        rvHouses.setLayoutManager(new LinearLayoutManager(getContext()));
        rvHouses.setAdapter(houseAdapter);
    }

    private void loadHouses() {
        houseRepository.getAllHouses(new HouseRepository.RepositoryCallback<List<House>>() {
            @Override
            public void onSuccess(List<House> result) {
                if (result != null) {
                    houseList.clear();
                    houseList.addAll(result);
                    filteredList.clear();
                    filteredList.addAll(houseList);
                    houseAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(String error) {
                android.widget.Toast.makeText(getContext(), "Failed to load houses: " + error, android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performSearch() {
        String query = safeLower(etSearch.getText() != null ? etSearch.getText().toString() : "");
        String location = safeText(spinnerLocation.getSelectedItem());
        String price = safeText(spinnerPrice.getSelectedItem());
        String bedrooms = safeText(spinnerBedrooms.getSelectedItem());

        filteredList.clear();

        for (House house : houseList) {
            boolean matches = true;

            // Filter by search query
            if (!query.isEmpty()) {
                if (!safeLower(house.getTitle()).contains(query) &&
                        !safeLower(house.getDescription()).contains(query) &&
                        !safeLower(house.getLocation()).contains(query)) {
                    matches = false;
                }
            }

            // Filter by location
            if (!location.equals("All Locations") && matches) {
                if (!safeLower(house.getLocation()).contains(safeLower(location))) {
                    matches = false;
                }
            }

            // Filter by price
            if (!price.equals("Any Price") && matches) {
                int[] priceRange = parsePriceRange(price);
                if (priceRange != null && (house.getPrice() < priceRange[0] || house.getPrice() > priceRange[1])) {
                    matches = false;
                }
            }

            // Filter by bedrooms
            if (!bedrooms.equals("Any") && matches) {
                int bedCount = parseLeadingNumber(bedrooms);
                if (bedCount > 0 && house.getBedrooms() != bedCount) {
                    matches = false;
                }
            }

            if (matches) {
                filteredList.add(house);
            }
        }

        houseAdapter.notifyDataSetChanged();
    }

    private String safeText(Object value) {
        return value == null ? "" : value.toString().trim();
    }

    private String safeLower(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private int[] parsePriceRange(String price) {
        String[] priceRange = price.split("-");
        if (priceRange.length != 2) {
            return null;
        }
        int minPrice = parseMoney(priceRange[0]);
        int maxPrice = parseMoney(priceRange[1]);
        if (minPrice < 0 || maxPrice < 0) {
            return null;
        }
        return new int[]{minPrice, maxPrice};
    }

    private int parseMoney(String value) {
        try {
            String digits = value.replaceAll("[^0-9]", "");
            return digits.isEmpty() ? -1 : Integer.parseInt(digits);
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    private int parseLeadingNumber(String value) {
        try {
            String[] parts = value.trim().split("\\s+");
            return parts.length == 0 ? -1 : Integer.parseInt(parts[0]);
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }
}
