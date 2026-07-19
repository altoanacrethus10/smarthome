package com.example.smarthome.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smarthome.activities.MainActivity;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.smarthome.R;
import com.example.smarthome.activities.BrowseHousesActivity;
import com.example.smarthome.activities.HouseDetailActivity;
import com.example.smarthome.activities.ReportActivity;
import com.example.smarthome.adapters.HouseAdapter;
import com.example.smarthome.adapters.OwnerPropertyAdapter;
import com.example.smarthome.activities.AddListingActivity;
import com.example.smarthome.activities.HelpActivity;





import com.example.smarthome.models.Booking;
import com.example.smarthome.models.House;
import com.example.smarthome.repository.BookingRepository;
import com.example.smarthome.repository.HouseRepository;
import com.example.smarthome.utils.SharedPrefManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class DashboardFragment extends Fragment {

    private static final String[] POPULAR_AREAS = {
            "Kinondoni", "Mbezi", "Sinza", "Masaki", "Mikocheni", "Temeke",
            "Kigamboni", "Ilala", "Ubungo", "Kariakoo"
    };

    private TextView tvTotalHouses, tvTotalTenants, tvTotalBookings, tvOccupancyRate;
    private com.google.android.material.progressindicator.LinearProgressIndicator pbOccupancy;
    private TextView tvEarningsMonth, tvEarningsYear;
    private RecyclerView rvRecentHouses;
    private SwipeRefreshLayout swipeRefresh;
    private OwnerPropertyAdapter ownerHouseAdapter;

    private List<House> ownerHouseList;

    private TextView tvGreeting, tvLocation, tvEmptyTitle;
    private TextInputEditText etSearch;
    private ImageButton btnLocation;
    private MaterialButton btnEmptyAction;
    private Chip btnFilterLocation, btnFilterPrice, btnFilterBedrooms, btnFilterHouseType;
    private Chip chipAvailable;
    private ChipGroup chipGroupAreas;
    private View contentSections, emptyState;
    private RecyclerView rvRecommended, rvLatest;

    private HouseAdapter recommendedAdapter, latestAdapter;
    private final List<House> allHouses = new ArrayList<>();
    private final List<House> recommendedHouses = new ArrayList<>();
    private final List<House> latestHouses = new ArrayList<>();

    private SharedPrefManager sharedPrefManager;
    private HouseRepository houseRepository;
    private BookingRepository bookingRepository;
    private String selectedLocation = "All Locations";
    private String selectedPrice = "Any Price";
    private String selectedBedrooms = "Any";
    private String selectedHouseType = "Any Type";
    private boolean filtersReturnedNothing = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        sharedPrefManager = SharedPrefManager.getInstance(requireContext());
        houseRepository = HouseRepository.getInstance(requireContext());
        bookingRepository = BookingRepository.getInstance(requireContext());

        if (sharedPrefManager.isOwner()) {
            View ownerView = inflater.inflate(R.layout.fragment_dashboard_owner, container, false);
            setupOwnerDashboard(ownerView);
            return ownerView;
        }

        View tenantView = inflater.inflate(R.layout.fragment_tenant_dashboard, container, false);
        setupTenantDashboard(tenantView);
        return tenantView;
    }

    private void setupOwnerDashboard(View view) {
        tvTotalHouses = view.findViewById(R.id.tv_total_houses);
        tvTotalTenants = view.findViewById(R.id.tv_total_tenants);
        tvTotalBookings = view.findViewById(R.id.tv_total_bookings);
        tvOccupancyRate = view.findViewById(R.id.tv_occupancy_rate);
        pbOccupancy = view.findViewById(R.id.pb_occupancy);
        tvEarningsMonth = view.findViewById(R.id.tv_earnings_month);
        tvEarningsYear = view.findViewById(R.id.tv_earnings_year);
        rvRecentHouses = view.findViewById(R.id.rv_recent_houses);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);

        // Greeting + notification bell
        TextView tvGreetingOwner = view.findViewById(R.id.tv_owner_greeting);
        if (tvGreetingOwner != null) {
            String name = sharedPrefManager.getUserName();
            if (name == null || name.trim().isEmpty()) name = "David";
            tvGreetingOwner.setText("Hello, " + name + "!");
        }

        ImageButton btnNotification = view.findViewById(R.id.btn_notification);
        if (btnNotification != null) {
            btnNotification.setOnClickListener(v -> {
                // Owner notifications currently map to bookings screen.
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).openTab(R.id.nav_bookings);
                }
            });
        }


        ownerHouseList = new ArrayList<>();
        ownerHouseAdapter = new OwnerPropertyAdapter(requireContext(), ownerHouseList, new OwnerPropertyAdapter.Listener() {


            @Override
            public void onView(House house) {
                openHouseDetails(house);
            }

            @Override
            public void onEdit(House house) {
                Intent intent = new Intent(getActivity(), AddListingActivity.class);
                intent.putExtra("house_id", house.getId());
                startActivity(intent);
            }

            @Override
            public void onDelete(House house) {
                Toast.makeText(getContext(), "Delete not wired yet", Toast.LENGTH_SHORT).show();
            }
        });

        rvRecentHouses.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRecentHouses.setAdapter(ownerHouseAdapter);

        swipeRefresh.setOnRefreshListener(this::loadOwnerDashboardData);
        swipeRefresh.setColorSchemeResources(R.color.primary);
        view.findViewById(R.id.btn_open_report).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openTab(R.id.nav_bookings);
            }
        });
        view.findViewById(R.id.btn_add_house).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), AddListingActivity.class)));
        view.findViewById(R.id.btn_open_payments).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), ReportActivity.class)));

        // View all listings link
        View tvViewAll = view.findViewById(R.id.tv_view_all_properties);
        if (tvViewAll != null) {
            tvViewAll.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).openTab(R.id.nav_houses);
                }
            });
        }





        loadOwnerDashboardData();
    }

    private void setupTenantDashboard(View view) {
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        tvGreeting = view.findViewById(R.id.tv_greeting);
        tvLocation = view.findViewById(R.id.tv_location);
        etSearch = view.findViewById(R.id.et_search);
        btnLocation = view.findViewById(R.id.btn_change_location);
        btnFilterLocation = view.findViewById(R.id.btn_filter_location);
        btnFilterPrice = view.findViewById(R.id.btn_filter_price);
        btnFilterBedrooms = view.findViewById(R.id.btn_filter_bedrooms);
        btnFilterHouseType = view.findViewById(R.id.btn_filter_house_type);
        chipAvailable = view.findViewById(R.id.chip_available);
        chipGroupAreas = view.findViewById(R.id.chip_group_areas);
        rvRecommended = view.findViewById(R.id.rv_recommended);
        rvLatest = view.findViewById(R.id.rv_latest);
        contentSections = view.findViewById(R.id.content_sections);
        emptyState = view.findViewById(R.id.empty_state);
        tvEmptyTitle = view.findViewById(R.id.tv_empty_title);
        btnEmptyAction = view.findViewById(R.id.btn_empty_action);

        String tenantName = sharedPrefManager.getUserName();
        tvGreeting.setText("Welcome, " + (tenantName != null && !tenantName.trim().isEmpty() ? tenantName : "Tenant"));
        
        view.findViewById(R.id.btn_profile).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openTab(R.id.nav_profile);
            }
        });

        updateLocationText();
        setupTenantAdapters();
        setupFilterControls();
        setupPopularAreas();
        setupQuickActions(view);

        swipeRefresh.setColorSchemeResources(R.color.primary);
        swipeRefresh.setOnRefreshListener(this::loadTenantHouses);
        btnEmptyAction.setOnClickListener(v -> {
            if (filtersReturnedNothing) {
                clearFilters();
            } else {
                loadTenantHouses();
            }
        });

        loadTenantHouses();
    }

    private void setupTenantAdapters() {
        HouseAdapter.OnHouseClickListener listener = new HouseAdapter.OnHouseClickListener() {
            @Override
            public void onHouseClick(House house) {
                openHouseDetails(house);
            }

            @Override
            public void onRentClick(House house) {
                // Implement if needed
            }

            @Override
            public void onFavoriteClick(House house) {
                toggleFavorite(house);
            }

            @Override
            public void onContactClick(House house) {
                if (house == null) return;
                Intent intent = new Intent(getActivity(), com.example.smarthome.activities.ChatActivity.class);
                intent.putExtra("other_user_id", house.getOwnerId());
                intent.putExtra("other_user_name", house.getOwnerName());
                intent.putExtra("house_id", house.getId());
                startActivity(intent);
            }
        };

        recommendedAdapter = new HouseAdapter(getContext(), recommendedHouses, listener);
        latestAdapter = new HouseAdapter(getContext(), latestHouses, listener);

        HouseAdapter.FavoriteStateProvider favoriteProvider =
                house -> houseRepository.isFavorite(sharedPrefManager.getUserId(), house.getId());

        recommendedAdapter.setFavoriteStateProvider(favoriteProvider);
        latestAdapter.setFavoriteStateProvider(favoriteProvider);
        
        recommendedAdapter.setDistanceTextProvider(this::buildDistanceText);
        latestAdapter.setDistanceTextProvider(this::buildDistanceText);

        rvRecommended.setLayoutManager(new LinearLayoutManager(getContext()));
        rvLatest.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRecommended.setAdapter(recommendedAdapter);
        rvLatest.setAdapter(latestAdapter);
    }

    private void setupFilterControls() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { applyTenantFilters(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        chipAvailable.setOnClickListener(v -> applyTenantFilters());

        btnLocation.setOnClickListener(v -> showLocationPicker());
        btnFilterLocation.setOnClickListener(v -> showChoiceDialog("Location", getStringArray(R.array.locations), selectedLocation, value -> {
            selectedLocation = value;
            btnFilterLocation.setText(value.equals("All Locations") ? "Location" : value);
            applyTenantFilters();
        }));
        btnFilterPrice.setOnClickListener(v -> showChoiceDialog("Price Range", getStringArray(R.array.price_ranges), selectedPrice, value -> {
            selectedPrice = value;
            btnFilterPrice.setText(value.equals("Any Price") ? "Price" : value.replace(",000", "k"));
            applyTenantFilters();
        }));
        btnFilterBedrooms.setOnClickListener(v -> showChoiceDialog("Bedrooms", getStringArray(R.array.bedroom_counts), selectedBedrooms, value -> {
            selectedBedrooms = value;
            btnFilterBedrooms.setText(value.equals("Any") ? "Beds" : value.replace(" Bedrooms", " Beds").replace(" Bedroom", " Bed"));
            applyTenantFilters();
        }));
        btnFilterHouseType.setOnClickListener(v -> showChoiceDialog("House Type", new String[]{"Any Type", "Apartment", "House", "Studio", "Room", "Villa"}, selectedHouseType, value -> {
            selectedHouseType = value;
            btnFilterHouseType.setText(value.equals("Any Type") ? "Type" : value);
            applyTenantFilters();
        }));
    }

    private void setupPopularAreas() {
        chipGroupAreas.removeAllViews();
        for (String area : POPULAR_AREAS) {
            Chip chip = new Chip(requireContext());
            chip.setText(area);
            chip.setCheckable(true);
            chip.setOnClickListener(v -> {
                selectedLocation = area;
                sharedPrefManager.setPreferredArea(area);
                btnFilterLocation.setText(area);
                updateLocationText();
                applyTenantFilters();
            });
            chipGroupAreas.addView(chip);
        }
    }

    private void setupQuickActions(View view) {
        if (view.findViewById(R.id.btn_profile) != null) {
            view.findViewById(R.id.btn_profile).setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).openTab(R.id.nav_profile);
                }
            });
        }
        if (view.findViewById(R.id.btn_my_bookings) != null) {
            view.findViewById(R.id.btn_my_bookings).setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).openTab(R.id.nav_bookings);
                }
            });
        }
        if (view.findViewById(R.id.btn_explore_search) != null) {
            view.findViewById(R.id.btn_explore_search).setOnClickListener(v ->
                    startActivity(new Intent(getActivity(), BrowseHousesActivity.class)));
        }
        if (view.findViewById(R.id.btn_favorites) != null) {
            view.findViewById(R.id.btn_favorites).setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).openTab(R.id.nav_saved);
                }
            });
        }
    }

    private void loadOwnerDashboardData() {
        if (swipeRefresh != null) swipeRefresh.setRefreshing(true);
        String ownerId = sharedPrefManager.getUserId();

        houseRepository.getHousesByOwner(ownerId, new HouseRepository.RepositoryCallback<List<House>>() {
            @Override
            public void onSuccess(List<House> result) {
                ownerHouseList.clear();
                int rentedCount = 0;
                double totalEarningsMonth = 0;

                if (result != null) {
                    // Defensive deduplication: ensure each house appears only once by id
                    Map<String, House> uniqueById = new LinkedHashMap<>();
                    for (House h : result) {
                        if (h == null) continue;
                        String id = h.getId();
                        if (id == null) continue;
                        uniqueById.put(id, h);
                    }
                    List<House> uniqueHouses = new ArrayList<>(uniqueById.values());

                    for (House house : uniqueHouses) {
                        if (!house.isAvailable()) {
                            rentedCount++;
                            totalEarningsMonth += house.getPrice();
                        }
                    }

                    for (int i = 0; i < Math.min(uniqueHouses.size(), 5); i++) {
                        ownerHouseList.add(uniqueHouses.get(i));
                    }
                    
                    int totalHouses = uniqueHouses.size();
                    tvTotalHouses.setText(String.valueOf(totalHouses));
                    tvTotalTenants.setText(String.valueOf(rentedCount));
                    
                    if (totalHouses > 0) {
                        int rate = (rentedCount * 100) / totalHouses;
                        tvOccupancyRate.setText(rate + "%");
                        pbOccupancy.setProgress(rate);
                    } else {
                        tvOccupancyRate.setText("0%");
                        pbOccupancy.setProgress(0);
                    }
                    
                    tvEarningsMonth.setText(String.format(Locale.US, "Tsh %,.0f", totalEarningsMonth));
                    tvEarningsYear.setText(String.format(Locale.US, "Tsh %,.0f", totalEarningsMonth * 12));
                    
                    View emptyState = getView() != null ? getView().findViewById(R.id.ll_empty_state) : null;
                    if (emptyState != null) {
                        emptyState.setVisibility(uniqueHouses.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                }
                ownerHouseAdapter.notifyDataSetChanged();

                bookingRepository.getBookingsByOwner(ownerId, new BookingRepository.RepositoryCallback<List<Booking>>() {
                    @Override
                    public void onSuccess(List<Booking> bookings) {
                        if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                        int pendingCount = 0;
                        if (bookings != null) {
                            for (Booking b : bookings) if (b.isPending()) pendingCount++;
                        }
                        tvTotalBookings.setText(String.valueOf(pendingCount));
                    }

                    @Override
                    public void onError(String error) {
                        if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onError(String error) {
                if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
            }
        });
    }

    private void loadTenantHouses() {
        showTenantLoading(true);
        houseRepository.getAllHouses(new HouseRepository.RepositoryCallback<List<House>>() {
            @Override
            public void onSuccess(List<House> result) {
                allHouses.clear();
                if (result != null) {
                    allHouses.addAll(result);
                }
                showTenantLoading(false);
                applyTenantFilters();
            }

            @Override
            public void onError(String error) {
                showTenantLoading(false);
                contentSections.setVisibility(View.GONE);
                emptyState.setVisibility(View.VISIBLE);
                tvEmptyTitle.setText("We could not load houses. Check your connection.");
            }
        });
    }

    private void applyTenantFilters() {
        List<House> filtered = new ArrayList<>();
        String query = etSearch.getText() != null ? etSearch.getText().toString().trim().toLowerCase(Locale.US) : "";

        for (House house : allHouses) {
            if (house.isAvailable() && matchesTenantFilters(house, query)) {
                filtered.add(house);
            }
        }

        filtersReturnedNothing = allHouses.size() > 0 && filtered.isEmpty();
        updateTenantSections(filtered);
    }

    private boolean matchesTenantFilters(House house, String query) {
        if (house == null) return false;
        if (!query.isEmpty() && !searchBlob(house).contains(query)) return false;
        if (!"All Locations".equals(selectedLocation) && !containsIgnoreCase(house.getLocation(), selectedLocation)) return false;
        if (!matchesPrice(house.getPrice(), selectedPrice)) return false;
        if (!matchesCount(house.getBedrooms(), selectedBedrooms)) return false;
        if (!"Any Type".equals(selectedHouseType) && !searchBlob(house).contains(selectedHouseType.toLowerCase(Locale.US))) return false;
        if (chipAvailable.isChecked() && !house.isAvailable()) return false;
        return true;
    }

    private String searchBlob(House house) {
        StringBuilder builder = new StringBuilder();
        append(builder, house.getTitle());
        append(builder, house.getLocation());
        append(builder, house.getDescription());
        append(builder, house.getFormattedPrice());
        append(builder, String.valueOf((int) house.getPrice()));
        append(builder, house.getBedrooms() + " bedrooms");
        append(builder, house.getBathrooms() + " bathrooms");
        if (house.getFeatures() != null) {
            for (String feature : house.getFeatures()) append(builder, feature);
        }
        return builder.toString().toLowerCase(Locale.US);
    }

    private void updateTenantSections(List<House> filtered) {
        recommendedHouses.clear();
        latestHouses.clear();

        List<House> recommended = new ArrayList<>(filtered);
        Collections.sort(recommended, Comparator.comparing(this::distanceRank));
        for (House house : recommended) {
            if (recommendedHouses.size() >= 4) break;
            recommendedHouses.add(house);
        }

        for (House house : filtered) {
            if (latestHouses.size() >= 8) break;
            latestHouses.add(house);
        }

        recommendedAdapter.notifyDataSetChanged();
        latestAdapter.notifyDataSetChanged();

        boolean noListings = allHouses.isEmpty();
        boolean noMatches = !noListings && filtered.isEmpty();
        contentSections.setVisibility(noListings || noMatches ? View.GONE : View.VISIBLE);
        emptyState.setVisibility(noListings || noMatches ? View.VISIBLE : View.GONE);
        tvEmptyTitle.setText(noListings ? "No houses available right now." : "No houses match your filters.");
        btnEmptyAction.setText(noListings ? "Refresh" : "Clear Filters");
    }

    private void showTenantLoading(boolean loading) {
        if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
        contentSections.setVisibility(loading ? View.GONE : View.VISIBLE);
        emptyState.setVisibility(View.GONE);
    }

    private void clearFilters() {
        if (etSearch.getText() != null) etSearch.getText().clear();
        selectedLocation = "All Locations";
        selectedPrice = "Any Price";
        selectedBedrooms = "Any";
        selectedHouseType = "Any Type";
        btnFilterLocation.setText("Location");
        btnFilterPrice.setText("Price");
        btnFilterBedrooms.setText("Beds");
        btnFilterHouseType.setText("Type");
        chipAvailable.setChecked(false);
        chipGroupAreas.clearCheck();
        applyTenantFilters();
    }

    private void openHouseDetails(House house) {
        Intent intent = new Intent(getActivity(), HouseDetailActivity.class);
        intent.putExtra("house_id", house.getId());
        startActivity(intent);
    }

    private void toggleFavorite(House house) {
        if (!sharedPrefManager.isTenant()) {
            Toast.makeText(getContext(), R.string.owner_cannot_save_favorites, Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = sharedPrefManager.getUserId();
        boolean favorite = houseRepository.isFavorite(userId, house.getId());
        HouseRepository.RepositoryCallback<Boolean> callback = new HouseRepository.RepositoryCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                Toast.makeText(getContext(), favorite ? "Removed from favorites" : "Added to favorites", Toast.LENGTH_SHORT).show();
                applyTenantFilters();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Favorite update failed", Toast.LENGTH_SHORT).show();
            }
        };
        if (favorite) {
            houseRepository.removeFavorite(userId, house.getId(), callback);
        } else {
            houseRepository.addFavorite(userId, house.getId(), callback);
        }
    }

    private void showLocationPicker() {
        String[] choices = new String[POPULAR_AREAS.length + 1];
        choices[0] = "Use Current Location";
        System.arraycopy(POPULAR_AREAS, 0, choices, 1, POPULAR_AREAS.length);
        showChoiceDialog("Preferred Area", choices, sharedPrefManager.getPreferredArea(), value -> {
            String area = value.equals("Use Current Location") ? "Dar es Salaam" : value;
            sharedPrefManager.setPreferredArea(area);
            selectedLocation = value.equals("Use Current Location") ? "All Locations" : value;
            updateLocationText();
            applyTenantFilters();
            Toast.makeText(getContext(), getString(R.string.preferred_area_updated, area), Toast.LENGTH_SHORT).show();
        });
    }

    private void updateLocationText() {
        String area = sharedPrefManager.getPreferredArea();
        if (area == null || area.isEmpty()) area = "Dar es Salaam";
        tvLocation.setText(area.contains("Dar es Salaam") ? area : area + ", TZ");
    }

    private void showChoiceDialog(String title, String[] choices, String selected, ChoiceCallback callback) {
        int checked = 0;
        for (int i = 0; i < choices.length; i++) {
            if (choices[i].equals(selected)) {
                checked = i;
                break;
            }
        }
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setSingleChoiceItems(choices, checked, (dialog, which) -> {
                    callback.onChoice(choices[which]);
                    dialog.dismiss();
                })
                .show();
    }

    private String[] getStringArray(int resId) {
        return getResources().getStringArray(resId);
    }

    private boolean matchesPrice(double price, String range) {
        if ("Any Price".equals(range)) return true;
        if (range.endsWith("+")) {
            return price >= Double.parseDouble(range.replace("+", "").replace(",", "").trim());
        }
        String[] parts = range.split("-");
        if (parts.length != 2) return true;
        double min = Double.parseDouble(parts[0].replace(",", "").trim());
        double max = Double.parseDouble(parts[1].replace(",", "").trim());
        return price >= min && price <= max;
    }

    private boolean matchesCount(int count, String selected) {
        if ("Any".equals(selected)) return true;
        if (selected.startsWith("5+") || selected.startsWith("4+")) {
            return count >= Integer.parseInt(selected.substring(0, 1));
        }
        try {
            return count == Integer.parseInt(selected.substring(0, 1));
        } catch (NumberFormatException ignored) {
            return true;
        }
    }

    private boolean isNearPreferredArea(House house) {
        String preferred = sharedPrefManager.getPreferredArea();
        if (preferred == null || preferred.equals("Dar es Salaam")) return true;
        return containsIgnoreCase(house.getLocation(), preferred);
    }

    private int distanceRank(House house) {
        return isNearPreferredArea(house) ? 0 : 1;
    }

    private String buildDistanceText(House house) {
        if (isNearPreferredArea(house)) {
            return "Near your preferred area";
        }
        return "";
    }

    private boolean containsIgnoreCase(String value, String needle) {
        return value != null && needle != null
                && value.toLowerCase(Locale.US).contains(needle.toLowerCase(Locale.US));
    }

    private void append(StringBuilder builder, String value) {
        if (value != null) builder.append(value).append(' ');
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sharedPrefManager != null && sharedPrefManager.isOwner()) {
            loadOwnerDashboardData();
        } else if (recommendedAdapter != null) {
            applyTenantFilters();
        }
    }

    private interface ChoiceCallback {
        void onChoice(String value);
    }
}
