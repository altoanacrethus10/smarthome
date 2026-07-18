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

public class HouseViewModel extends AndroidViewModel {
    private final MutableLiveData<List<House>> houses = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<House>> filteredHouses = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<House> selectedHouse = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isFavorite = new MutableLiveData<>(false);

    private HouseRepository houseRepository;
    private SharedPrefManager sharedPrefManager;
    private List<House> allHouses = new ArrayList<>();

    public HouseViewModel(Application application) {
        super(application);
        Context context = application.getApplicationContext();
        houseRepository = HouseRepository.getInstance(context);
        sharedPrefManager = SharedPrefManager.getInstance(context);

        loadAllHouses();
    }

    public void loadAllHouses() {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        houseRepository.getAllHouses(new HouseRepository.RepositoryCallback<List<House>>() {
            @Override
            public void onSuccess(List<House> houseList) {
                allHouses = houseList;
                houses.setValue(houseList);
                filteredHouses.setValue(houseList);
                isLoading.setValue(false);
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue("Failed to load houses: " + error);
            }
        });
    }

    public void loadHousesByOwner(String ownerId) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        houseRepository.getHousesByOwner(ownerId, new HouseRepository.RepositoryCallback<List<House>>() {
            @Override
            public void onSuccess(List<House> houseList) {
                allHouses = houseList;
                houses.setValue(houseList);
                filteredHouses.setValue(houseList);
                isLoading.setValue(false);
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue("Failed to load owner houses: " + error);
            }
        });
    }

    public void loadHouseDetails(String houseId) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        houseRepository.getHouse(houseId, new HouseRepository.RepositoryCallback<House>() {
            @Override
            public void onSuccess(House house) {
                selectedHouse.setValue(house);
                isLoading.setValue(false);
                
                // Check if favorite
                if (sharedPrefManager.isLoggedIn()) {
                    boolean fav = houseRepository.isFavorite(sharedPrefManager.getUserId(), houseId);
                    isFavorite.setValue(fav);
                }
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue("Failed to load house details: " + error);
            }
        });
    }

    public void searchHouses(String query, String location, String priceRange, String bedrooms) {
        List<House> filtered = new ArrayList<>();

        for (House house : allHouses) {
            boolean matches = true;

            // Filter by search query
            if (!query.isEmpty()) {
                String lowerQuery = query.toLowerCase();
                if (!house.getTitle().toLowerCase().contains(lowerQuery) &&
                    !house.getDescription().toLowerCase().contains(lowerQuery) &&
                    !house.getLocation().toLowerCase().contains(lowerQuery)) {
                    matches = false;
                }
            }

            // Filter by location
            if (!location.equals("All Locations") && matches) {
                if (!house.getLocation().toLowerCase().contains(location.toLowerCase())) {
                    matches = false;
                }
            }

            // Filter by price
            if (!priceRange.equals("Any Price") && matches) {
                try {
                    String[] range = priceRange.replace("Tsh ", "").replace(",", "").split(" - ");
                    if (range.length == 2) {
                        int minPrice = Integer.parseInt(range[0].trim());
                        int maxPrice = Integer.parseInt(range[1].trim());
                        if (house.getPrice() < minPrice || house.getPrice() > maxPrice) {
                            matches = false;
                        }
                    }
                } catch (Exception e) {
                    // Parse error, skip filter
                }
            }

            // Filter by bedrooms
            if (!bedrooms.equals("Any") && matches) {
                try {
                    int bedCount = Integer.parseInt(bedrooms.split(" ")[0]);
                    if (house.getBedrooms() != bedCount) {
                        matches = false;
                    }
                } catch (Exception e) {
                    // Parse error, skip filter
                }
            }

            if (matches) {
                filtered.add(house);
            }
        }

        filteredHouses.setValue(filtered);
    }

    public void toggleFavorite(String houseId) {
        String userId = sharedPrefManager.getUserId();
        if (userId == null) {
            errorMessage.setValue("Please login to save favorites");
            return;
        }

        boolean currentFavorite = isFavorite.getValue() != null && isFavorite.getValue();
        
        if (currentFavorite) {
            houseRepository.removeFavorite(userId, houseId, new HouseRepository.RepositoryCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    isFavorite.setValue(false);
                }

                @Override
                public void onError(String error) {
                    errorMessage.setValue("Failed to remove favorite: " + error);
                }
            });
        } else {
            houseRepository.addFavorite(userId, houseId, new HouseRepository.RepositoryCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    isFavorite.setValue(true);
                }

                @Override
                public void onError(String error) {
                    errorMessage.setValue("Failed to add favorite: " + error);
                }
            });
        }
    }

    public void refreshHouses() {
        loadAllHouses();
    }

    // Getters for LiveData
    public LiveData<List<House>> getHouses() { return houses; }
    public LiveData<List<House>> getFilteredHouses() { return filteredHouses; }
    public LiveData<House> getSelectedHouse() { return selectedHouse; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getIsFavorite() { return isFavorite; }
}
