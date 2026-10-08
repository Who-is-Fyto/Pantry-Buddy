package com.example.pantrybuddy.domain.model;

import java.io.Serializable;

// Represents an amount of an ingredient used during cooking to deduct from the pantry
public class StockDeduction implements Serializable {
    private final long itemId;
    private final double amountUsed;

    public StockDeduction(long itemId, double amountUsed) {
        this.itemId = itemId;
        this.amountUsed = amountUsed;
    }

    public long getItemId() {
        return itemId;
    }

    public double getAmountUsed() {
        return amountUsed;
    }
}
