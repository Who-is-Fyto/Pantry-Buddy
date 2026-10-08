package com.example.pantrybuddy;

import static org.junit.Assert.*;

import com.example.pantrybuddy.domain.engine.UnitConverter;
import com.example.pantrybuddy.domain.model.DeductionDelta;
import com.example.pantrybuddy.domain.model.StockDeduction;

import org.junit.Test;

// Unit tests for post-cooking inventory deduction models and calculations
public class StockDeductionTest {

    @Test
    public void testStockDeductionProperties() {
        StockDeduction deduction = new StockDeduction(42L, 200.0);
        assertEquals(42L, deduction.getItemId());
        assertEquals(200.0, deduction.getAmountUsed(), 0.0001);
    }

    @Test
    public void testDeductionDeltaUsedUp() {
        DeductionDelta delta = new DeductionDelta(1L, "Eggs", "pcs", 4.0, 4.0, 0.0);
        assertEquals(1L, delta.getItemId());
        assertEquals("Eggs", delta.getItemName());
        assertEquals("pcs", delta.getUnit());
        assertEquals(4.0, delta.getBeforeQuantity(), 0.0001);
        assertEquals(4.0, delta.getUsedQuantity(), 0.0001);
        assertEquals(0.0, delta.getRemainingQuantity(), 0.0001);
        assertTrue(delta.isUsedUp());
    }

    @Test
    public void testDeductionDeltaPartiallyRemaining() {
        DeductionDelta delta = new DeductionDelta(2L, "Tomatoes", "g", 300.0, 200.0, 100.0);
        assertFalse(delta.isUsedUp());
        assertEquals(100.0, delta.getRemainingQuantity(), 0.0001);
    }

    @Test
    public void testLeftoverCalculationSameUnits() {
        double before = 300.0;
        double used = 200.0;
        double leftover = UnitConverter.calculateLeftover(before, "g", used, "g", "tomato");
        assertEquals(100.0, leftover, 0.001);
    }

    @Test
    public void testLeftoverCalculationExhausted() {
        double before = 4.0;
        double used = 4.0;
        double leftover = UnitConverter.calculateLeftover(before, "pcs", used, "pcs", "egg");
        assertEquals(0.0, leftover, 0.001);
    }

    @Test
    public void testLeftoverCalculationOverused() {
        double before = 2.0;
        double used = 4.0;
        double leftover = UnitConverter.calculateLeftover(before, "pcs", used, "pcs", "egg");
        assertEquals(0.0, leftover, 0.001);
    }

    @Test
    public void testConversionDuringDeduction() {
        // Recipe used 200g, pantry has 1kg
        double usedInKg = UnitConverter.convertAmount(200.0, "g", "kg", "tomato");
        assertEquals(0.2, usedInKg, 0.001);

        double remainingKg = 1.0 - usedInKg;
        assertEquals(0.8, remainingKg, 0.001);
    }
}
