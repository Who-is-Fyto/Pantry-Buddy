package com.example.pantrybuddy.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.pantrybuddy.data.local.AppDatabase;
import com.example.pantrybuddy.data.local.DatabaseInitializer;
import com.example.pantrybuddy.data.local.dao.PantryDao;
import com.example.pantrybuddy.data.local.entity.PantryItem;
import com.example.pantrybuddy.domain.engine.IngredientNormalizer;
import com.example.pantrybuddy.domain.engine.UnitConverter;
import com.example.pantrybuddy.domain.model.StockDeduction;

import java.util.List;
import java.util.concurrent.ExecutorService;

// Repository that handles pantry data operations
public class PantryRepository {

    private final PantryDao pantryDao;
    private final ExecutorService executor;
    private final LiveData<List<PantryItem>> allPantryItems;
    private final LiveData<Integer> itemCount;
    private final Application application;

    public PantryRepository(Application application) {
        this.application = application;
        AppDatabase db = AppDatabase.getDatabase(application);
        this.pantryDao = db.pantryDao();
        this.executor = AppDatabase.databaseWriteExecutor;
        this.allPantryItems = pantryDao.getAllPantryItems();
        this.itemCount = pantryDao.getItemCountLive();
    }

    // For testing with mock daos
    public PantryRepository(PantryDao pantryDao, ExecutorService executor) {
        this.application = null;
        this.pantryDao = pantryDao;
        this.executor = executor;
        this.allPantryItems = pantryDao.getAllPantryItems();
        this.itemCount = pantryDao.getItemCountLive();
    }

    public LiveData<List<PantryItem>> getAllPantryItems() {
        return allPantryItems;
    }

    public LiveData<List<PantryItem>> getItemsByCategory(String category) {
        return pantryDao.getItemsByCategory(category);
    }

    public LiveData<List<PantryItem>> searchItems(String query) {
        return pantryDao.searchItems(query);
    }

    public LiveData<Integer> getItemCount() {
        return itemCount;
    }

    public LiveData<List<PantryItem>> getItemsExpiringBefore(long thresholdTime) {
        return pantryDao.getItemsExpiringBefore(thresholdTime);
    }

    public void insert(PantryItem item) {
        insert(item, null);
    }

    public void insert(PantryItem item, Runnable onComplete) {
        executor.execute(() -> {
            if (item.getNormalizedName() == null || item.getNormalizedName().trim().isEmpty()) {
                item.setNormalizedName(IngredientNormalizer.normalize(item.getName()));
            }
            pantryDao.insert(item);
            if (onComplete != null) {
                onComplete.run();
            }
        });
    }

    public void insertAll(List<PantryItem> items) {
        executor.execute(() -> {
            for (PantryItem item : items) {
                if (item.getNormalizedName() == null || item.getNormalizedName().trim().isEmpty()) {
                    item.setNormalizedName(IngredientNormalizer.normalize(item.getName()));
                }
            }
            pantryDao.insertAll(items);
        });
    }

    public void update(PantryItem item) {
        executor.execute(() -> {
            if (item.getNormalizedName() == null || item.getNormalizedName().trim().isEmpty()) {
                item.setNormalizedName(IngredientNormalizer.normalize(item.getName()));
            }
            pantryDao.update(item);
        });
    }

    public void delete(PantryItem item) {
        executor.execute(() -> pantryDao.delete(item));
    }

    public void deleteById(long itemId) {
        executor.execute(() -> pantryDao.deleteById(itemId));
    }

    public void deleteAll() {
        executor.execute(() -> pantryDao.deleteAll());
    }

    // Deducts used ingredient amount or removes it if fully used
    public void deductIngredient(String ingredientName, double amountUsed, String unit) {
        executor.execute(() -> {
            String normalized = IngredientNormalizer.normalize(ingredientName);
            PantryItem existing = pantryDao.getItemByNormalizedName(normalized);
            if (existing == null) {
                return;
            }

            double requiredInPantryUnit = UnitConverter.convertAmount(
                    amountUsed, unit, existing.getUnit(), existing.getNormalizedName()
            );

            double remaining = existing.getQuantity() - requiredInPantryUnit;
            if (remaining <= 0.001) {
                pantryDao.delete(existing);
            } else {
                existing.setQuantity(remaining);
                pantryDao.update(existing);
            }
        });
    }

    // Deducts inventory stock using batch deductions
    public void deductStock(List<StockDeduction> deductions, Runnable onComplete) {
        executor.execute(() -> {
            pantryDao.deductStock(deductions);
            if (onComplete != null) {
                onComplete.run();
            }
        });
    }

    // Resets pantry back to the default sample ingredients
    public void resetSampleData() {
        resetSampleData(null);
    }

    public void resetSampleData(Runnable onComplete) {
        executor.execute(() -> {
            if (application != null) {
                AppDatabase db = AppDatabase.getDatabase(application);
                DatabaseInitializer.resetAllToSampleData(db, application);
            }
            if (onComplete != null) {
                onComplete.run();
            }
        });
    }
}
