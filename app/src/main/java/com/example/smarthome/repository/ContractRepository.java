package com.example.smarthome.repository;

import android.content.Context;
import android.util.Log;

import com.example.smarthome.database.local.DatabaseHelper;
import com.example.smarthome.database.remote.FirestoreHelper;
import com.example.smarthome.models.Contract;

import java.util.List;

public class ContractRepository {
    private static final String TAG = "ContractRepository";
    private static ContractRepository instance;
    private DatabaseHelper databaseHelper;
    private FirestoreHelper firestoreHelper;

    private ContractRepository(Context context) {
        databaseHelper = DatabaseHelper.getInstance(context);
        firestoreHelper = FirestoreHelper.getInstance();
    }

    public static synchronized ContractRepository getInstance(Context context) {
        if (instance == null) {
            instance = new ContractRepository(context);
        }
        return instance;
    }

    public void createContract(Contract contract, final RepositoryCallback<String> callback) {
        // For simplicity, we mostly use local for simulation now, 
        // but let's try to add it to Firestore too if we add the methods there.
        long result = databaseHelper.insertContract(contract);
        if (result != -1) {
            callback.onSuccess(contract.getId());
        } else {
            callback.onError("Failed to save contract locally");
        }
    }

    public List<Contract> getContractsByUser(String userId) {
        return databaseHelper.getContractsByUser(userId);
    }

    public interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}
