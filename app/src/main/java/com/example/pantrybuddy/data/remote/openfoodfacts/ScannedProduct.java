package com.example.pantrybuddy.data.remote.openfoodfacts;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.io.Serializable;

// Normalized product model returned after parsing an Open Food Facts product entry
public class ScannedProduct implements Serializable {

    private final String barcode;
    private final String name;
    private final String brand;
    private final double quantity;
    private final String unit;
    private final String category;
    private final String imageUrl;

    public ScannedProduct(@NonNull String barcode,
                          @NonNull String name,
                          @Nullable String brand,
                          double quantity,
                          @NonNull String unit,
                          @NonNull String category,
                          @Nullable String imageUrl) {
        this.barcode = barcode;
        this.name = name;
        this.brand = brand != null ? brand : "";
        this.quantity = quantity;
        this.unit = unit;
        this.category = category;
        this.imageUrl = imageUrl != null ? imageUrl : "";
    }

    @NonNull
    public String getBarcode() {
        return barcode;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public String getBrand() {
        return brand;
    }

    public double getQuantity() {
        return quantity;
    }

    @NonNull
    public String getUnit() {
        return unit;
    }

    @NonNull
    public String getCategory() {
        return category;
    }

    @NonNull
    public String getImageUrl() {
        return imageUrl;
    }

    // Display title combining product name and brand if available
    @NonNull
    public String getDisplayTitle() {
        if (!brand.isEmpty() && !name.toLowerCase().contains(brand.toLowerCase())) {
            return name + " (" + brand + ")";
        }
        return name;
    }
}
