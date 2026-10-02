package com.example.pantrybuddy.data.local.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

// Pantry item stored in SQLite
@Entity(
    tableName = "pantry_items",
    indices = {
        @Index("name"),
        @Index("normalizedName"),
        @Index("expiryDate")
    }
)
public class PantryItem {
    @PrimaryKey(autoGenerate = true)
    private long itemId;
    private String name;
    private String normalizedName;
    private double quantity;
    private String unit;          // "g", "ml", "pcs", "tbsp", "tsp"
    private Long expiryDate;      // Milliseconds timestamp, null if no expiry
    private String category;      // "Vegetables", "Dairy & Eggs", "Meat & Fish", "Staples", "Spices & Seasonings"
    private long dateAdded;

    public PantryItem(String name, String normalizedName, double quantity, String unit, Long expiryDate, String category, long dateAdded) {
        this.name = name;
        this.normalizedName = normalizedName;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
        this.category = category;
        this.dateAdded = dateAdded;
    }

    public long getItemId() {
        return itemId;
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNormalizedName() {
        return normalizedName;
    }

    public void setNormalizedName(String normalizedName) {
        this.normalizedName = normalizedName;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Long getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Long expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(long dateAdded) {
        this.dateAdded = dateAdded;
    }
}
