package com.example.smarthome.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.example.smarthome.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;

public class FavoritesFragment extends Fragment {

    private RecyclerView rvFavorites;
    private View emptyView;
    private HouseAdapter houseAdapter;
    private final List<House> houseList = new ArrayList<>();
    private HouseRepository houseRepository;
    private SharedPrefManager sharedPrefManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);

        houseRepository = HouseRepository.getInstance(getContext());
        sharedPrefManager = SharedPrefManager.getInstance(getContext());

        rvFavorites = view.findViewById(R.id.rv_favorites);
        emptyView = view.findViewById(R.id.ll_empty_favorites);

        setupRecyclerView();
        loadFavorites();

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
            
            @Override
            public void onFavoriteClick(House house) {
                // Implement remove logic if needed
            }
        });
        rvFavorites.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFavorites.setAdapter(houseAdapter);
    }

    private void loadFavorites() {
        if (!sharedPrefManager.isLoggedIn()) return;
        
        List<String> favoriteIds = houseRepository.getUserFavorites(sharedPrefManager.getUserId());
        
        if (favoriteIds.isEmpty()) {
            rvFavorites.setVisibility(View.GONE);
            if (emptyView != null) emptyView.setVisibility(View.VISIBLE);
        } else {
            houseList.clear();
            for (String id : favoriteIds) {
                houseRepository.getHouse(id, new HouseRepository.RepositoryCallback<House>() {
                    @Override
                    public void onSuccess(House result) {
                        houseList.add(result);
                        if (houseList.size() == favoriteIds.size()) {
                            rvFavorites.setVisibility(View.VISIBLE);
                            if (emptyView != null) emptyView.setVisibility(View.GONE);
                            houseAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onError(String error) {}
                });
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFavorites();
    }
}
