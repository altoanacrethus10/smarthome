package com.example.smarthome.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.button.MaterialButton;
import com.example.smarthome.R;
import com.example.smarthome.adapters.SavedPropertiesAdapter;
import com.example.smarthome.models.House;
import com.example.smarthome.repository.HouseRepository;
import com.example.smarthome.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SavedPropertiesActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView rv;

    private HouseRepository houseRepository;
    private SharedPrefManager sharedPrefManager;

    private SavedPropertiesAdapter adapter;
    private final List<House> savedHouses = new ArrayList<>();

    private final Set<String> savedIds = new HashSet<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_properties);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        }

        rv = findViewById(R.id.rv_saved_properties);
        rv.setLayoutManager(new GridLayoutManager(this, 2));

        houseRepository = HouseRepository.getInstance(this);
        sharedPrefManager = SharedPrefManager.getInstance(this);

        adapter = new SavedPropertiesAdapter(this, new SavedPropertiesAdapter.Listener() {
            @Override
            public void onOpenDetails(House house) {
                openHouseDetails(house);
            }

            @Override
            public void onOpenCard(House house) {
                openHouseDetails(house);
            }

            @Override
            public void onUnsave(House house) {
                removeFromSavedWithUndo(house);
            }
        });
        rv.setAdapter(adapter);

        MaterialButton btnBrowse = findViewById(R.id.btn_browse_properties);
        btnBrowse.setOnClickListener(v -> {
            startActivity(new Intent(SavedPropertiesActivity.this, MainActivity.class)
                    .putExtra(MainActivity.EXTRA_OPEN_SECTION, "browse"));
            finish();
        });

        loadSavedProperties();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void openHouseDetails(House house) {
        if (house == null || house.getId() == null) return;
        Intent intent = new Intent(this, HouseDetailActivity.class);
        intent.putExtra("house_id", house.getId());
        startActivity(intent);
    }

    private void loadSavedProperties() {
        // Favorites are stored in SQLite in this codebase (DatabaseHelper favorites table).
        // The requirements ask for Firestore real-time updates; repository currently has no
        // real-time favorites listener, so this screen loads the latest saved data and reflects
        // changes immediately on unsave.

        savedHouses.clear();
        savedIds.clear();

        if (!sharedPrefManager.isLoggedIn()) {
            updateUi();
            return;
        }

        List<String> ids = houseRepository.getUserFavorites(sharedPrefManager.getUserId());
        if (ids != null) savedIds.addAll(ids);

        if (savedIds.isEmpty()) {
            updateUi();
            adapter.submitList(new ArrayList<>());
            updateUi();
            return;
        }

        List<String> idList = new ArrayList<>(savedIds);
        Map<String, House> resultById = new HashMap<>();
        int expected = idList.size();

        for (String id : idList) {
            houseRepository.getHouse(id, new HouseRepository.RepositoryCallback<House>() {
                @Override
                public void onSuccess(House result) {
                    if (result != null && result.getId() != null) {
                        resultById.put(result.getId(), result);
                    }
                    if (resultById.size() >= expected) {
                        savedHouses.clear();
                        savedHouses.addAll(resultById.values());
                        adapter.submitList(savedHouses);
                        updateUi();
                    }
                }

                @Override
                public void onError(String error) {
                    if (resultById.size() >= expected - 1) {
                        savedHouses.clear();
                        savedHouses.addAll(resultById.values());
                        adapter.submitList(savedHouses);
                        updateUi();
                    }
                }
            });
        }

        // interim count
        updateUi();
    }

    private void updateUi() {
        int count = adapter != null ? adapter.getItemCount() : savedHouses.size();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle(count + " saved");
        }

        boolean empty = count == 0;
        RecyclerView list = findViewById(R.id.rv_saved_properties);
        ViewCompatHelper.setVisible(list, !empty);
        ViewCompatHelper.setVisible(findViewById(R.id.empty_state), empty);
    }

    private void removeFromSavedWithUndo(House house) {
        if (house == null || house.getId() == null) return;
        String userId = sharedPrefManager.getUserId();
        if (userId == null) return;

        // Optimistic remove
        List<House> updated = new ArrayList<>();
        for (House h : savedHouses) {
            if (h != null && h.getId() != null && h.getId().equals(house.getId())) continue;
            updated.add(h);
        }
        savedHouses.clear();
        savedHouses.addAll(updated);
        adapter.submitList(savedHouses);
        updateUi();

        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Removed from saved", Snackbar.LENGTH_LONG);
        snackbar.setAction("Undo", v -> {
            houseRepository.addFavorite(userId, house.getId(), new HouseRepository.RepositoryCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    loadSavedProperties();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(SavedPropertiesActivity.this, "Undo failed", Toast.LENGTH_SHORT).show();
                    loadSavedProperties();
                }
            });
        });

        snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                // If user didn't undo, persist removal
                if (event != DISMISS_EVENT_ACTION) {
                    houseRepository.removeFavorite(userId, house.getId(), new HouseRepository.RepositoryCallback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean result) {
                            loadSavedProperties();
                        }

                        @Override
                        public void onError(String error) {
                            Toast.makeText(SavedPropertiesActivity.this, "Failed to remove", Toast.LENGTH_SHORT).show();
                            loadSavedProperties();
                        }
                    });
                }
            }
        });

        snackbar.show();
    }

    // Small local helper to avoid importing View from multiple packages
    private static class ViewCompatHelper {
        static void setVisible(android.view.View v, boolean visible) {
            if (v == null) return;
            v.setVisibility(visible ? android.view.View.VISIBLE : android.view.View.GONE);
        }
    }
}

