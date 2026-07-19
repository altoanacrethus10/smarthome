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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.smarthome.R;
import com.example.smarthome.activities.HouseDetailActivity;
import com.example.smarthome.activities.MainActivity;
import com.example.smarthome.adapters.SavedPropertiesAdapter;
import com.example.smarthome.models.House;
import com.example.smarthome.repository.HouseRepository;
import com.example.smarthome.utils.SharedPrefManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SavedPropertiesFragment extends Fragment {

    private RecyclerView rv;

    private HouseRepository houseRepository;
    private SharedPrefManager sharedPrefManager;

    private SavedPropertiesAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private final List<House> savedHouses = new ArrayList<>();

    private final Set<String> savedIds = new HashSet<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_saved_properties, container, false);

        rv = view.findViewById(R.id.rv_saved_properties);
        rv.setLayoutManager(new GridLayoutManager(getContext(), 2));

        houseRepository = HouseRepository.getInstance(requireContext());
        sharedPrefManager = SharedPrefManager.getInstance(requireContext());

        adapter = new SavedPropertiesAdapter(getContext(), new SavedPropertiesAdapter.Listener() {
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

        MaterialButton btnBrowse = view.findViewById(R.id.btn_browse_properties);
        btnBrowse.setOnClickListener(v ->
                ((MainActivity) requireActivity()).openTab(R.id.nav_houses));

        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.primary);
        swipeRefresh.setOnRefreshListener(this::loadSavedProperties);

        loadSavedProperties();

        return view;
    }

    private void openHouseDetails(House house) {
        if (house == null || house.getId() == null) return;
        Intent intent = new Intent(getActivity(), HouseDetailActivity.class);
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
            if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
            return;
        }

        List<String> ids = houseRepository.getUserFavorites(sharedPrefManager.getUserId());
        if (ids != null) savedIds.addAll(ids);

        if (savedIds.isEmpty()) {
            updateUi();
            adapter.submitList(new ArrayList<>());
            updateUi();
            if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
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
                        if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                    }
                }

                @Override
                public void onError(String error) {
                    if (resultById.size() >= expected - 1) {
                        savedHouses.clear();
                        savedHouses.addAll(resultById.values());
                        adapter.submitList(savedHouses);
                        updateUi();
                        if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                    }
                }
            });
        }

        // interim count
        updateUi();
    }

    private void updateUi() {
        View root = getView();
        if (root == null) return;

        int count = adapter != null ? adapter.getItemCount() : savedHouses.size();

        boolean empty = count == 0;
        RecyclerView list = root.findViewById(R.id.rv_saved_properties);
        setVisible(list, !empty);
        setVisible(root.findViewById(R.id.empty_state), empty);
    }

    private void setVisible(View v, boolean visible) {
        if (v == null) return;
        v.setVisibility(visible ? View.VISIBLE : View.GONE);
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

        Snackbar snackbar = Snackbar.make(requireActivity().findViewById(android.R.id.content), "Removed from saved", Snackbar.LENGTH_LONG);
        snackbar.setAction("Undo", v -> {
            houseRepository.addFavorite(userId, house.getId(), new HouseRepository.RepositoryCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    loadSavedProperties();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(getContext(), "Undo failed", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(getContext(), "Failed to remove", Toast.LENGTH_SHORT).show();
                            loadSavedProperties();
                        }
                    });
                }
            }
        });

        snackbar.show();
    }
}
