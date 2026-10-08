package com.example.pantrybuddy;

import static org.junit.Assert.*;

import com.example.pantrybuddy.data.local.entity.PantryItem;
import com.example.pantrybuddy.data.local.entity.Recipe;
import com.example.pantrybuddy.data.local.entity.RecipeIngredient;
import com.example.pantrybuddy.domain.engine.StrictRecipeMatcher;
import com.example.pantrybuddy.domain.model.MatchResult;
import com.example.pantrybuddy.domain.model.RecipeWithIngredients;
import com.example.pantrybuddy.utils.PreferenceHelper;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

// Unit tests verifying marker sample scenario and preference constants
public class SettingsAndMarkerTest {

    private RecipeWithIngredients scrambleRecipe;

    @Before
    public void setUp() {
        Recipe recipe = new Recipe(
                "Tomato & Spinach Scramble",
                "Quick skillet scramble",
                15,
                2,
                "[]",
                "",
                "Easy"
        );
        recipe.setRecipeId(1L);

        List<RecipeIngredient> ingredients = new ArrayList<>();
        ingredients.add(new RecipeIngredient(1L, "Eggs", "egg", 4.0, "pcs"));
        ingredients.add(new RecipeIngredient(1L, "Tomatoes", "tomato", 200.0, "g"));
        ingredients.add(new RecipeIngredient(1L, "Spinach", "spinach", 80.0, "g"));
        ingredients.add(new RecipeIngredient(1L, "Olive Oil", "oil", 10.0, "ml"));
        ingredients.add(new RecipeIngredient(1L, "Salt", "salt", 2.0, "g"));
        ingredients.add(new RecipeIngredient(1L, "Black Pepper", "black pepper", 1.0, "g"));

        scrambleRecipe = new RecipeWithIngredients();
        scrambleRecipe.setRecipe(recipe);
        scrambleRecipe.setIngredients(ingredients);
    }

    @Test
    public void testPreferenceConstants() {
        assertEquals("pref_expiry_alerts_enabled", PreferenceHelper.KEY_EXPIRY_ALERTS);
        assertEquals("pref_unit_system", PreferenceHelper.KEY_UNIT_SYSTEM);
        assertEquals("pref_urgency_threshold_days", PreferenceHelper.KEY_THRESHOLD_DAYS);
        assertEquals("metric", PreferenceHelper.UNIT_METRIC);
        assertEquals("imperial", PreferenceHelper.UNIT_IMPERIAL);
        assertEquals(2, PreferenceHelper.DEFAULT_THRESHOLD_DAYS);
    }

    @Test
    public void testMarkerPantryScenarioStrictlyUnlocksTomatoSpinachScramble() {
        // Standard Figma Scenario items populated by "Reset Sample Data":
        // 4 eggs, 300g tomatoes, 100g spinach, 40ml olive oil, 10g salt, 6g black pepper
        List<PantryItem> markerPantry = new ArrayList<>();
        markerPantry.add(new PantryItem("Eggs", "egg", 4.0, "pcs", null, "Dairy & Eggs", 0));
        markerPantry.add(new PantryItem("Tomatoes", "tomato", 300.0, "g", null, "Vegetables", 0));
        markerPantry.add(new PantryItem("Spinach", "spinach", 100.0, "g", null, "Vegetables", 0));
        markerPantry.add(new PantryItem("Olive Oil", "oil", 40.0, "ml", null, "Staples", 0));
        markerPantry.add(new PantryItem("Salt", "salt", 10.0, "g", null, "Spices & Seasonings", 0));
        markerPantry.add(new PantryItem("Black Pepper", "black pepper", 6.0, "g", null, "Spices & Seasonings", 0));

        MatchResult result = StrictRecipeMatcher.evaluate(scrambleRecipe, markerPantry);

        assertTrue("Marker pantry must strictly qualify Tomato & Spinach Scramble as cookable", result.isStrictlyCookable());
        assertEquals("Missing ingredient count must be 0", 0, result.getMissingCount());
        assertTrue("Missing ingredients list must be empty", result.getMissingIngredients().isEmpty());
        assertTrue("Max servings supported must be at least 2", result.getMaxServings() >= 2);
    }

    @Test
    public void testMarkerPantryWithoutOilFailsStrictMatching() {
        // Assume user ran out of olive oil
        List<PantryItem> partialPantry = new ArrayList<>();
        partialPantry.add(new PantryItem("Eggs", "egg", 4.0, "pcs", null, "Dairy & Eggs", 0));
        partialPantry.add(new PantryItem("Tomatoes", "tomato", 300.0, "g", null, "Vegetables", 0));
        partialPantry.add(new PantryItem("Spinach", "spinach", 100.0, "g", null, "Vegetables", 0));
        partialPantry.add(new PantryItem("Salt", "salt", 10.0, "g", null, "Spices & Seasonings", 0));
        partialPantry.add(new PantryItem("Black Pepper", "black pepper", 6.0, "g", null, "Spices & Seasonings", 0));

        MatchResult result = StrictRecipeMatcher.evaluate(scrambleRecipe, partialPantry);

        assertFalse("Without olive oil, recipe must NOT be cookable (zero assumed staples guarantee)", result.isStrictlyCookable());
        assertEquals(1, result.getMissingCount());
        assertEquals("Olive Oil", result.getMissingIngredients().get(0).getIngredientName());
    }
}
