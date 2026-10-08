package com.example.pantrybuddy.domain.model;

import java.io.Serializable;

// Model representing before, used, and remaining amounts for an ingredient
public class DeductionDelta implements Serializable {
    private final long itemId;
    private final String itemName;
    private final String unit;
    private final double beforeQuantity;
    private final double usedQuantity;
    private final double remainingQuantity;

    public DeductionDelta(long itemId, String itemName, String unit, double beforeQuantity, double usedQuantity, double remainingQuantity) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.unit = unit;
        this.beforeQuantity = beforeQuantity;
        this.usedQuantity = usedQuantity;
        this.remainingQuantity = Math.max(0.0, remainingQuantity);
    }

    public long getItemId() {
        return itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public String getUnit() {
        return unit;
    }

    public double getBeforeQuantity() {
        return beforeQuantity;
    }

    public double getUsedQuantity() {
        return usedQuantity;
    }

    public double getRemainingQuantity() {
        return remainingQuantity;
    }

    public boolean isUsedUp() {
        return remainingQuantity <= 0.001;
    }
}
