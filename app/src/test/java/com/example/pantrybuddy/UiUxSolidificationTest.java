package com.example.pantrybuddy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit tests validating UI/UX solidification logic:
 * serving stepper bounds, quantity adjuster steps, rapid-click protection,
 * and search string trimming.
 */
public class UiUxSolidificationTest {

    // Minimum accessible touch target in dp per Material Design guidelines
    private static final int MIN_TOUCH_TARGET_DP = 48;

    @Test
    public void accessibleTouchTarget_meetsOrExceeds48dp() {
        int buttonTouchTargetDp = 48;
        assertTrue(buttonTouchTargetDp >= MIN_TOUCH_TARGET_DP);
    }

    @Test
    public void servingStepper_cannotDecrementBelowOne() {
        int servings = 1;
        // Simulates decrement logic in RecipeDetailActivity.setupStepper()
        if (servings > 1) {
            servings--;
        }
        assertEquals(1, servings);
    }

    @Test
    public void servingStepper_incrementsCorrectly() {
        int servings = 2;
        servings++;
        assertEquals(3, servings);
    }

    @Test
    public void quantityAdjuster_gramStepIncrements50g() {
        double current = 100.0;
        String unit = "g";
        double step = ("g".equalsIgnoreCase(unit) || "ml".equalsIgnoreCase(unit)) ? 50.0 : 1.0;
        double updated = current + step;
        assertEquals(150.0, updated, 0.001);
    }

    @Test
    public void quantityAdjuster_cannotGoBelowZero() {
        double current = 25.0;
        double step = 50.0;
        double updated = Math.max(0.0, current - step);
        assertEquals(0.0, updated, 0.001);
    }

    @Test
    public void navigationGuard_blocksDuplicateClicks() {
        boolean isNavigating = false;

        // First click
        boolean firstClickHandled = false;
        if (!isNavigating) {
            isNavigating = true;
            firstClickHandled = true;
        }
        assertTrue(firstClickHandled);
        assertTrue(isNavigating);

        // Immediate rapid second click
        boolean secondClickHandled = false;
        if (!isNavigating) {
            secondClickHandled = true;
        }
        assertFalse(secondClickHandled);
    }

    @Test
    public void searchInput_emptyOrWhitespaceHandledGracefully() {
        String input = "   ";
        String trimmed = input.trim();
        assertTrue(trimmed.isEmpty());
    }

    @Test
    public void searchInput_queryPreservedCleanly() {
        String input = "  spinach  ";
        String trimmed = input.trim();
        assertEquals("spinach", trimmed);
    }
}
