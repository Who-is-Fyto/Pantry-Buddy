package com.example.pantrybuddy.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.pantrybuddy.data.local.entity.PantryItem;
import java.util.List;

@Dao
public interface PantryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(PantryItem item);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<PantryItem> items);

    @Update
    void update(PantryItem item);

    @Delete
    void delete(PantryItem item);

    @Query("DELETE FROM pantry_items WHERE itemId = :id")
    void deleteById(long id);

    @Query("DELETE FROM pantry_items")
    void deleteAll();

    @Query("SELECT * FROM pantry_items ORDER BY CASE WHEN expiryDate IS NULL THEN 1 ELSE 0 END, expiryDate ASC, name ASC")
    LiveData<List<PantryItem>> getAllPantryItems();

    @Query("SELECT * FROM pantry_items ORDER BY CASE WHEN expiryDate IS NULL THEN 1 ELSE 0 END, expiryDate ASC, name ASC")
    List<PantryItem> getAllPantryItemsSync();

    @Query("SELECT * FROM pantry_items WHERE itemId = :id LIMIT 1")
    PantryItem getItemById(long id);

    @Query("SELECT * FROM pantry_items WHERE normalizedName = :normalizedName LIMIT 1")
    PantryItem getItemByNormalizedName(String normalizedName);

    @Query("SELECT * FROM pantry_items WHERE category = :category ORDER BY CASE WHEN expiryDate IS NULL THEN 1 ELSE 0 END, expiryDate ASC")
    LiveData<List<PantryItem>> getItemsByCategory(String category);

    @Query("SELECT * FROM pantry_items WHERE name LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' ORDER BY name ASC")
    LiveData<List<PantryItem>> searchItems(String query);

    @Query("SELECT COUNT(*) FROM pantry_items")
    int getItemCount();
}
