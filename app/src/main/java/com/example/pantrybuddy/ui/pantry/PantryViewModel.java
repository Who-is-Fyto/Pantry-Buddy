package com.example.pantrybuddy.ui.pantry;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.pantrybuddy.data.local.entity.PantryItem;
import com.example.pantrybuddy.data.repository.PantryRepository;
import com.example.pantrybuddy.domain.model.ExpiryStatus;
import com.example.pantrybuddy.domain.model.StockDeduction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// ViewModel managing pantry inventory, search, and category filters
public class PantryViewModel extends AndroidViewModel {

    public enum SortOrder {
        EXPIRY_ASC,
        NAME_ASC,
        DATE_ADDED_DESC
    }

    private final PantryRepository repository;
    private final LiveData<List<PantryItem>> allPantryItems;
    private final LiveData<Integer> itemCount;

    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<String> categoryFilter = new MutableLiveData<>("All");
    private final MutableLiveData<Boolean> useSoonOnly = new MutableLiveData<>(false);
    private final MutableLiveData<SortOrder> sortOrder = new MutableLiveData<>(SortOrder.EXPIRY_ASC);

    private final MediatorLiveData<List<PantryItem>> filteredPantryItems = new MediatorLiveData<>();
    private final MediatorLiveData<List<PantryItem>> expiringSoonItems = new MediatorLiveData<>();

    public PantryViewModel(@NonNull Application application) {
        this(application, new PantryRepository(application));
    }

    // Testing constructor
    public PantryViewModel(@NonNull Application application, PantryRepository repository) {
        super(application);
        this.repository = repository;
        this.allPantryItems = repository.getAllPantryItems();
        this.itemCount = repository.getItemCount();

        setupFilteredMediator();
        setupExpiringSoonMediator();
    }

    private void setupFilteredMediator() {
        filteredPantryItems.addSource(allPantryItems, items -> applyFilters());
        filteredPantryItems.addSource(searchQuery, query -> applyFilters());
        filteredPantryItems.addSource(categoryFilter, category -> applyFilters());
        filteredPantryItems.addSource(useSoonOnly, soonOnly -> applyFilters());
        filteredPantryItems.addSource(sortOrder, sort -> applyFilters());
    }

    private void setupExpiringSoonMediator() {
        expiringSoonItems.addSource(allPantryItems, items -> {
            if (items == null) {
                expiringSoonItems.setValue(Collections.emptyList());
                return;
            }
            List<PantryItem> expiring = new ArrayList<>();
            for (PantryItem item : items) {
                ExpiryStatus status = ExpiryStatus.fromTimestamp(item.getExpiryDate());
                if (status == ExpiryStatus.EXPIRED || status == ExpiryStatus.TODAY || status == ExpiryStatus.TOMORROW || status == ExpiryStatus.SOON) {
                    expiring.add(item);
                }
            }
            expiringSoonItems.setValue(expiring);
        });
    }

    private void applyFilters() {
        List<PantryItem> rawList = allPantryItems.getValue();
        if (rawList == null) {
            filteredPantryItems.setValue(Collections.emptyList());
            return;
        }

        String query = searchQuery.getValue();
        String category = categoryFilter.getValue();
        Boolean soonOnly = useSoonOnly.getValue();
        SortOrder sort = sortOrder.getValue();

        List<PantryItem> result = new ArrayList<>();
        for (PantryItem item : rawList) {
            if (category != null && !category.equalsIgnoreCase("All")) {
                if (!category.equalsIgnoreCase(item.getCategory())) {
                    continue;
                }
            }

            if (query != null && !query.trim().isEmpty()) {
                String lowerQ = query.trim().toLowerCase();
                boolean nameMatch = item.getName() != null && item.getName().toLowerCase().contains(lowerQ);
                boolean catMatch = item.getCategory() != null && item.getCategory().toLowerCase().contains(lowerQ);
                if (!nameMatch && !catMatch) {
                    continue;
                }
            }

            if (Boolean.TRUE.equals(soonOnly)) {
                ExpiryStatus status = ExpiryStatus.fromTimestamp(item.getExpiryDate());
                if (status != ExpiryStatus.TODAY && status != ExpiryStatus.TOMORROW && status != ExpiryStatus.EXPIRED && status != ExpiryStatus.SOON) {
                    continue;
                }
            }

            result.add(item);
        }

        if (sort == SortOrder.NAME_ASC) {
            Collections.sort(result, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        } else if (sort == SortOrder.DATE_ADDED_DESC) {
            Collections.sort(result, (a, b) -> Long.compare(b.getDateAdded(), a.getDateAdded()));
        } else {
            Collections.sort(result, (a, b) -> {
                if (a.getExpiryDate() == null && b.getExpiryDate() == null) {
                    return a.getName().compareToIgnoreCase(b.getName());
                }
                if (a.getExpiryDate() == null) return 1;
                if (b.getExpiryDate() == null) return -1;
                int cmp = Long.compare(a.getExpiryDate(), b.getExpiryDate());
                if (cmp != 0) return cmp;
                return a.getName().compareToIgnoreCase(b.getName());
            });
        }

        filteredPantryItems.setValue(result);
    }

    public LiveData<List<PantryItem>> getAllPantryItems() {
        return allPantryItems;
    }

    public LiveData<List<PantryItem>> getFilteredPantryItems() {
        return filteredPantryItems;
    }

    public LiveData<List<PantryItem>> getExpiringSoonItems() {
        return expiringSoonItems;
    }

    public LiveData<Integer> getItemCount() {
        return itemCount;
    }

    public LiveData<String> getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    public LiveData<String> getCategoryFilter() {
        return categoryFilter;
    }

    public void setCategoryFilter(String category) {
        categoryFilter.setValue(category);
    }

    public LiveData<Boolean> getUseSoonOnly() {
        return useSoonOnly;
    }

    public void setUseSoonOnly(boolean soonOnly) {
        useSoonOnly.setValue(soonOnly);
    }

    public void setSortOrder(SortOrder order) {
        sortOrder.setValue(order);
    }

    public void insert(PantryItem item) {
        repository.insert(item);
    }

    public void insert(PantryItem item, Runnable onComplete) {
        repository.insert(item, onComplete);
    }

    public void update(PantryItem item) {
        repository.update(item);
    }

    public void delete(PantryItem item) {
        repository.delete(item);
    }

    public void deleteById(long id) {
        repository.deleteById(id);
    }

    public void resetSampleData() {
        repository.resetSampleData();
    }

    public void resetSampleData(Runnable onComplete) {
        repository.resetSampleData(onComplete);
    }

    public void deductStock(List<StockDeduction> deductions, Runnable onComplete) {
        repository.deductStock(deductions, onComplete);
    }
}
