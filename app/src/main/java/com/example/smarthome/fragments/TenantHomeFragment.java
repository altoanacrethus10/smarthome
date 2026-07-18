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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smarthome.R;
import com.example.smarthome.activities.BookingsActivity;
import com.example.smarthome.activities.ContactActivity;
import com.example.smarthome.activities.HouseDetailActivity;
import com.example.smarthome.adapters.HouseAdapter;
import com.example.smarthome.adapters.TenantCategoryAdapter;
import com.example.smarthome.models.House;
import com.example.smarthome.repository.HouseRepository;
import com.example.smarthome.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class TenantHomeFragment extends Fragment {

    private static final String PREF_SEARCH_HISTORY_KEY = "tenant_search_history";

    private EditText etSearch;
    private ImageButton btnFilter;

    private RecyclerView rvCategories;
    private TenantCategoryAdapter categoryAdapter;

    private RecyclerView rvRecent;
    private RecyclerView rvRecommended;

    private View emptyState;
    private TextView tvEmptyTitle;
    private TextView tvEmptySuggestion1;
    private TextView tvEmptySuggestion2;

    private View contentSections;

    private View btnViewSaved;
    private View btnViewBookings;
    private View btnContactSupport;

    private HouseAdapter recentAdapter;
    private HouseAdapter recommendedAdapter;

    private final List<House> allAvailableHouses = new ArrayList<>();
    private final List<House> filteredRecent = new ArrayList<>();
    private final List<House> filteredRecommended = new ArrayList<>();

    private HouseRepository houseRepository;
    private SharedPrefManager sharedPrefManager;

    private String selectedCategory = "All";
    private String query = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tenant_home, container, false);

        sharedPrefManager = SharedPrefManager.getInstance(requireContext());
        houseRepository = HouseRepository.getInstance(requireContext());

        bindViews(view);
        setupAdapters();
        setupSearch();
        setupQuickActions();
        setupCategoryClicks();

        loadData();
        return view;
    }

    private void bindViews(View view) {
        etSearch = view.findViewById(R.id.et_search);
        btnFilter = view.findViewById(R.id.btn_filter);

        rvCategories = view.findViewById(R.id.rv_categories);
        rvRecent = view.findViewById(R.id.rv_recent);
        rvRecommended = view.findViewById(R.id.rv_recommended);

        emptyState = view.findViewById(R.id.empty_state);
        tvEmptyTitle = view.findViewById(R.id.tv_empty_title);
        tvEmptySuggestion1 = view.findViewById(R.id.tv_empty_suggestion_1);
        tvEmptySuggestion2 = view.findViewById(R.id.tv_empty_suggestion_2);

        contentSections = view.findViewById(R.id.content_sections);

        btnViewSaved = view.findViewById(R.id.btn_view_saved);
        btnViewBookings = view.findViewById(R.id.btn_view_bookings);
        btnContactSupport = view.findViewById(R.id.btn_contact_support);

        // Optional: view all links
        View tvViewAllRecent = view.findViewById(R.id.tv_view_all_recent);
        if (tvViewAllRecent != null) {
            tvViewAllRecent.setOnClickListener(v -> {
                // Reuse browse houses screen for now
                startActivity(new Intent(getActivity(), com.example.smarthome.activities.DashboardActivity.class));
            });
        }
    }

    private void setupAdapters() {
        categoryAdapter = new TenantCategoryAdapter(requireContext(), categoryItems(), item -> {
            selectedCategory = item.label;
            applyFiltersAndRefresh();
        });
        rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvCategories.setAdapter(categoryAdapter);

        HouseAdapter.FavoriteStateProvider favoriteProvider = house -> houseRepository.isFavorite(sharedPrefManager.getUserId(), house.getId());

        HouseAdapter.OnHouseClickListener listener = new HouseAdapter.OnHouseClickListener() {
            @Override
            public void onHouseClick(House house) {
                Intent intent = new Intent(getActivity(), HouseDetailActivity.class);
                intent.putExtra("house_id", house.getId());
                startActivity(intent);
            }

            @Override
            public void onRentClick(House house) {}

            @Override
            public void onFavoriteClick(House house) {
                toggleFavorite(house);
            }
        };

        recentAdapter = new HouseAdapter(getContext(), filteredRecent, listener);
        recommendedAdapter = new HouseAdapter(getContext(), filteredRecommended, listener);

        recentAdapter.setFavoriteStateProvider(favoriteProvider);
        recommendedAdapter.setFavoriteStateProvider(favoriteProvider);

        recentAdapter.setDistanceTextProvider(house -> "");
        recommendedAdapter.setDistanceTextProvider(house -> "");

        rvRecent.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvRecommended.setLayoutManager(new GridLayoutManager(getContext(), 2));

        rvRecent.setAdapter(recentAdapter);
        rvRecommended.setAdapter(recommendedAdapter);
    }

    private List<TenantCategoryAdapter.CategoryItem> categoryItems() {
        List<TenantCategoryAdapter.CategoryItem> items = new ArrayList<>();
        items.add(new TenantCategoryAdapter.CategoryItem("All", R.drawable.ic_home));
        items.add(new TenantCategoryAdapter.CategoryItem("Apartments", R.drawable.ic_area));
        items.add(new TenantCategoryAdapter.CategoryItem("Houses", R.drawable.ic_house));
        items.add(new TenantCategoryAdapter.CategoryItem("Rooms", R.drawable.ic_bed));
        items.add(new TenantCategoryAdapter.CategoryItem("Villas", R.drawable.ic_house));
        // Use a generic icon if a dedicated commercial icon asset is not available.
        items.add(new TenantCategoryAdapter.CategoryItem("Commercial", R.drawable.ic_home));

        return items;
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                query = s == null ? "" : s.toString().trim();
                applyFiltersAndRefresh();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        if (btnFilter != null) {
            // Filter dialog is not implemented in this iteration (requirements include controls).
            btnFilter.setOnClickListener(v -> Toast.makeText(getContext(), "Filters coming soon", Toast.LENGTH_SHORT).show());
        }
    }

    private void setupQuickActions() {
        if (btnViewSaved != null) btnViewSaved.setOnClickListener(v -> {
            // Saved properties UI uses FavoritesFragment
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, new com.example.smarthome.fragments.FavoritesFragment())
                    .commit();
        });

        if (btnViewBookings != null) btnViewBookings.setOnClickListener(v -> startActivity(new Intent(getActivity(), com.example.smarthome.activities.MyBookingsActivity.class)));

        if (btnContactSupport != null) btnContactSupport.setOnClickListener(v -> startActivity(new Intent(getActivity(), ContactActivity.class)));
    }

    private void setupCategoryClicks() {
        // handled in adapter listener
    }

    private void loadData() {
        // Requirements: load properties and filter availability.
        houseRepository.getAllHouses(new HouseRepository.RepositoryCallback<List<House>>() {
            @Override
            public void onSuccess(List<House> result) {
                allAvailableHouses.clear();
                if (result != null) {
                    for (House h : result) {
                        if (h != null && h.isAvailable()) {
                            allAvailableHouses.add(h);
                        }
                    }
                }
                applyFiltersAndRefresh();
            }

            @Override
            public void onError(String error) {
                showEmpty("Could not load properties", "Check your connection", "Tap to retry");
            }
        });
    }

    private void applyFiltersAndRefresh() {
        List<House> filtered = new ArrayList<>();
        for (House house : allAvailableHouses) {
            if (matchesCategory(house) && matchesQuery(house)) {
                filtered.add(house);
            }
        }

        // Recent: basic sort (highest price last is arbitrary; keep stable order)
        filteredRecent.clear();
        filteredRecent.addAll(filtered);

        // Recommended: favorites + preferred area + search history keyword overlap
        filteredRecommended.clear();
        filteredRecommended.addAll(buildRecommended(filtered));

        updateEmptyState();
        recentAdapter.notifyDataSetChanged();
        recommendedAdapter.notifyDataSetChanged();
    }

    private boolean matchesCategory(House house) {
        if (selectedCategory == null || "All".equalsIgnoreCase(selectedCategory)) return true;
        String type = house.getPropertyType() == null ? "" : house.getPropertyType();
        // Loose mapping
        switch (selectedCategory) {
            case "Apartments":
                return type.toLowerCase(Locale.US).contains("apartment");
            case "Houses":
                return type.toLowerCase(Locale.US).contains("house");
            case "Rooms":
                return type.toLowerCase(Locale.US).contains("room");
            case "Villas":
                return type.toLowerCase(Locale.US).contains("villa");
            case "Commercial":
                return type.toLowerCase(Locale.US).contains("commercial");
            default:
                return true;
        }
    }

    private boolean matchesQuery(House house) {
        if (query == null) return true;
        String q = query.toLowerCase(Locale.US);
        if (q.isEmpty()) return true;

        return containsAny(house.getTitle(), q)
                || containsAny(house.getLocation(), q)
                || containsAny(house.getDescription(), q)
                || containsAny(house.getPropertyType(), q);
    }

    private List<House> buildRecommended(List<House> pool) {
        String preferredArea = sharedPrefManager.getPreferredArea();
        Set<String> historyTokens = loadSearchHistoryTokens();
        Set<String> favoriteIds = loadFavoriteIds();

        List<House> sorted = new ArrayList<>(pool);
        Collections.sort(sorted, (a, b) -> score(b, preferredArea, historyTokens, favoriteIds)
                - score(a, preferredArea, historyTokens, favoriteIds));

        // Cap
        int cap = 4;
        if (sorted.size() <= cap) return sorted;
        return sorted.subList(0, cap);
    }

    private int score(House house, String preferredArea, Set<String> historyTokens, Set<String> favoriteIds) {
        int s = 0;
        if (favoriteIds.contains(house.getId())) s += 50;
        if (preferredArea != null && !preferredArea.trim().isEmpty()) {
            if (house.getLocation() != null && house.getLocation().toLowerCase(Locale.US).contains(preferredArea.toLowerCase(Locale.US))) s += 20;
        }
        if (historyTokens != null && !historyTokens.isEmpty()) {
            String blob = ((house.getTitle() == null ? "" : house.getTitle()) + " " +
                    (house.getLocation() == null ? "" : house.getLocation()) + " " +
                    (house.getPropertyType() == null ? "" : house.getPropertyType())).toLowerCase(Locale.US);
            for (String t : historyTokens) {
                if (!t.isEmpty() && blob.contains(t)) {
                    s += 10;
                }
            }
        }
        return s;
    }

    private Set<String> loadSearchHistoryTokens() {
        // Placeholder: no dedicated API in SharedPrefManager; implement minimal in-memory based on current query.
        // Real persistence would require additional methods.
        Set<String> tokens = new HashSet<>();
        if (query != null && !query.trim().isEmpty()) {
            String[] parts = query.toLowerCase(Locale.US).split("\\s+");
            for (String p : parts) {
                if (p.length() > 2) tokens.add(p);
            }
        }
        return tokens;
    }

    private Set<String> loadFavoriteIds() {
        Set<String> ids = new HashSet<>();
        String userId = sharedPrefManager.getUserId();
        if (userId == null) return ids;

        // Uses local cache via HouseRepository/DB.
        List<String> favs = houseRepository.getUserFavorites(userId);
        if (favs != null) ids.addAll(favs);
        return ids;
    }

    private void toggleFavorite(House house) {
        if (!sharedPrefManager.isTenant()) {
            Toast.makeText(getContext(), R.string.owner_cannot_save_favorites, Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = sharedPrefManager.getUserId();
        if (userId == null) return;

        boolean favorite = houseRepository.isFavorite(userId, house.getId());
        if (favorite) {
            houseRepository.removeFavorite(userId, house.getId(), new HouseRepository.RepositoryCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    applyFiltersAndRefresh();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(getContext(), "Favorite update failed", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            houseRepository.addFavorite(userId, house.getId(), new HouseRepository.RepositoryCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    applyFiltersAndRefresh();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(getContext(), "Favorite update failed", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateEmptyState() {
        boolean empty = filteredRecent.isEmpty() && filteredRecommended.isEmpty();
        contentSections.setVisibility(empty ? View.GONE : View.VISIBLE);
        emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);

        if (empty) {
            StringBuilder msg = new StringBuilder();
            msg.append("No results");
            if (selectedCategory != null && !"All".equalsIgnoreCase(selectedCategory)) msg.append(" in ").append(selectedCategory);
            if (query != null && !query.trim().isEmpty()) msg.append(" for '").append(query.trim()).append("'");
            tvEmptyTitle.setText(msg.toString());
            tvEmptySuggestion1.setText("Try a different category");
            tvEmptySuggestion2.setText("Clear search or adjust filters");
        }
    }

    private void showEmpty(String title, String s1, String s2) {
        contentSections.setVisibility(View.GONE);
        emptyState.setVisibility(View.VISIBLE);
        tvEmptyTitle.setText(title);
        tvEmptySuggestion1.setText(s1);
        tvEmptySuggestion2.setText(s2);
    }

    private boolean containsAny(String value, String q) {
        return value != null && value.toLowerCase(Locale.US).contains(q);
    }
}

