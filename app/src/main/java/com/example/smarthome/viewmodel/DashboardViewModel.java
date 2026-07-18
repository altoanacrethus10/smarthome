package com.example.smarthome.viewmodel;

import android.app.Application;
import android.content.Context;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.smarthome.models.House;
import com.example.smarthome.repository.HouseRepository;
import com.example.smarthome.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;

public class DashboardViewModel extends AndroidViewModel {
    private final MutableLiveData<List<House>> recentHouses = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Integer> totalHouses = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> totalTenants = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> totalBookings = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private HouseRepository houseRepository;
    private SharedPrefManager sharedPrefManager;

    public DashboardViewModel(Application application) {
        super(application);
        Context context = application.getApplicationContext();
        houseRepository = HouseRepository.getInstance(context);
        sharedPrefManager = SharedPrefManager.getInstance(context);

        loadDashboardData();
    }

    public void loadDashboardData() {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        // Load houses
        houseRepository.getAllHouses(new HouseRepository.RepositoryCallback<List<House>>() {
            @Override
            public void onSuccess(List<House> houses) {
                // Show recent houses (first 3)
                List<House> recent = new ArrayList<>();
                if (houses != null && !houses.isEmpty()) {
                    int count = Math.min(3, houses.size());
                    for (int i = 0; i < count; i++) {
                        recent.add(houses.get(i));
                    }
                    totalHouses.setValue(houses.size());
                } else {
                    // Sample data if no houses
                    loadSampleHouses();
                }
                recentHouses.setValue(recent);
                isLoading.setValue(false);

                // Load other stats
                loadStats();
            }

            @Override
            public void onError(String error) {
                // Load sample data if error
                loadSampleHouses();
                isLoading.setValue(false);
                errorMessage.setValue("Failed to load houses: " + error);
            }
        });
    }

    private void loadSampleHouses() {
        List<House> sampleHouses = new ArrayList<>();
        House house1 = new House("1", "owner1", "Modern House in Kinondoni", "Kinondoni, Dar es Salaam", 350000, 3, 2);
        house1.setDescription("Beautiful modern house with all amenities. Close to shopping malls and schools.");
        house1.setAvailable(true);
        sampleHouses.add(house1);

        House house2 = new House("2", "owner2", "Spacious Apartment in Mbezi", "Mbezi, Dar es Salaam", 450000, 4, 3);
        house2.setDescription("Spacious apartment with ocean view. Secure compound with parking.");
        house2.setAvailable(true);
        sampleHouses.add(house2);

        House house3 = new House("3", "owner3", "Cozy Studio in Kariakoo", "Kariakoo, Dar es Salaam", 150000, 1, 1);
        house3.setDescription("Perfect for singles or couples. Close to public transport and markets.");
        house3.setAvailable(true);
        sampleHouses.add(house3);

        recentHouses.setValue(sampleHouses);
        totalHouses.setValue(sampleHouses.size());
    }

    private void loadStats() {
        // In a real app, fetch from Firebase
        totalTenants.setValue(156);
        totalBookings.setValue(12);
    }

    public void refreshData() {
        loadDashboardData();
    }

    // Getters for LiveData
    public LiveData<List<House>> getRecentHouses() { return recentHouses; }
    public LiveData<Integer> getTotalHouses() { return totalHouses; }
    public LiveData<Integer> getTotalTenants() { return totalTenants; }
    public LiveData<Integer> getTotalBookings() { return totalBookings; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
}
