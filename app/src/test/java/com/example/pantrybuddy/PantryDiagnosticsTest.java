package com.example.pantrybuddy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.pantrybuddy.data.local.entity.PantryItem;
import com.example.pantrybuddy.data.local.entity.Recipe;
import com.example.pantrybuddy.data.local.entity.RecipeIngredient;
import com.example.pantrybuddy.domain.engine.PantryDiagnostics;
import com.example.pantrybuddy.domain.model.MatchResult;
import com.example.pantrybuddy.domain.model.RecipeWithIngredients;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// Unit tests for zero-match pantry diagnostics and unlocking recommendations
public class PantryDiagnosticsTest {

    @Test
    public void testEmptyPantryRecommendation() {
        String recommendation = PantryDiagnostics.generateUnlockingRecommendation(
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()
        );
        assertNotNull(recommendation);
        assertTrue(recommendation.contains("empty"));
        assertTrue(recommendation.contains("Add staple ingredients"));
    }

    @Test
    public void testAlmostThereUnlockingRecommendation() {
        Recipe r1 = new Recipe("Scramble", "Fast meal", 10, 2, "[]", "", "Easy");
        r1.setRecipeId(1L);
        RecipeWithIngredients rwi1 = new RecipeWithIngredients();
        rwi1.setRecipe(r1);

        MatchResult.MissingIngredientInfo missingOil = new MatchResult.MissingIngredientInfo(
                "Olive Oil", 10.0, "ml", 0.0, true
        );
        MatchResult mr1 = new MatchResult(
                rwi1, false, 1, Collections.singletonList(missingOil), 0, false, Long.MAX_VALUE
        );

        Recipe r2 = new Recipe("Roasted Veggies", "Crispy veggies", 25, 2, "[]", "", "Easy");
        r2.setRecipeId(2L);
        RecipeWithIngredients rwi2 = new RecipeWithIngredients();
        rwi2.setRecipe(r2);
        MatchResult mr2 = new MatchResult(
                rwi2, false, 1, Collections.singletonList(missingOil), 0, false, Long.MAX_VALUE
        );

        List<PantryItem> pantry = Arrays.asList(
                new PantryItem("Tomatoes", "tomato", 200.0, "g", null, "Vegetables", System.currentTimeMillis()),
                new PantryItem("Spinach", "spinach", 80.0, "g", null, "Vegetables", System.currentTimeMillis())
        );

        List<MatchResult> almostThere = Arrays.asList(mr1, mr2);

        String recommendation = PantryDiagnostics.generateUnlockingRecommendation(
                Arrays.asList(rwi1, rwi2),
                pantry,
                almostThere
        );

        assertNotNull(recommendation);
        assertTrue(recommendation.contains("Olive Oil"));
        assertTrue(recommendation.contains("10"));
        assertTrue(recommendation.contains("ml"));
        assertTrue(recommendation.contains("unlock 2 recipes"));
    }

    @Test
    public void testFormatActiveStockSummary() {
        long now = System.currentTimeMillis();
        long tomorrow = now + (24L * 60 * 60 * 1000);

        List<PantryItem> items = Arrays.asList(
                new PantryItem("Tomatoes", "tomato", 300.0, "g", tomorrow, "Vegetables", now),
                new PantryItem("Olive Oil", "oil", 40.0, "ml", null, "Staples", now)
        );

        String summary = PantryDiagnostics.formatActiveStockSummary(items);
        assertNotNull(summary);
        assertTrue(summary.contains("Tomatoes (300 g)"));
        assertTrue(summary.contains("use tomorrow"));
        assertTrue(summary.contains("Olive Oil (40 ml)"));
    }

    @Test
    public void testFormatActiveStockSummaryEmpty() {
        String summary = PantryDiagnostics.formatActiveStockSummary(Collections.emptyList());
        assertEquals("No ingredients currently in your pantry.", summary);
    }
}
