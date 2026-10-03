package com.example.pantrybuddy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.pantrybuddy.data.local.entity.PantryItem;
import com.example.pantrybuddy.data.local.entity.Recipe;
import com.example.pantrybuddy.data.local.entity.RecipeIngredient;
import com.example.pantrybuddy.domain.engine.AlmostThereMatcher;
import com.example.pantrybuddy.domain.engine.StrictRecipeMatcher;
import com.example.pantrybuddy.domain.model.MatchResult;
import com.example.pantrybuddy.domain.model.RecipeWithIngredients;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class StrictRecipeMatcherTest {

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
    public void testStrictMatch_allIngredientsPresent_isCookable() {
        long now = System.currentTimeMillis();
        List<PantryItem> pantry = Arrays.asList(
                new PantryItem("Eggs", "egg", 4.0, "pcs", null, "Dairy & Eggs", now),
                new PantryItem("Tomatoes", "tomato", 300.0, "g", null, "Vegetables", now),
                new PantryItem("Spinach", "spinach", 100.0, "g", null, "Vegetables", now),
                new PantryItem("Olive Oil", "oil", 40.0, "ml", null, "Staples", now),
                new PantryItem("Salt", "salt", 10.0, "g", null, "Spices & Seasonings", now),
                new PantryItem("Black Pepper", "black pepper", 6.0, "g", null, "Spices & Seasonings", now)
        );

        MatchResult result = StrictRecipeMatcher.evaluate(scrambleRecipe, pantry);
        assertTrue("Recipe must be strictly cookable when all items present", result.isStrictlyCookable());
        assertEquals("Missing count must be 0", 0, result.getMissingCount());
        assertEquals(2, result.getMaxServings());
        assertFalse("Should not be in almost there list", result.isAlmostThere());
    }

    @Test
    public void testStrictMatch_insufficientQuantity_isDisqualified() {
        long now = System.currentTimeMillis();
        // User has only 1 egg instead of 4
        List<PantryItem> pantry = Arrays.asList(
                new PantryItem("Eggs", "egg", 1.0, "pcs", null, "Dairy & Eggs", now),
                new PantryItem("Tomatoes", "tomato", 300.0, "g", null, "Vegetables", now),
                new PantryItem("Spinach", "spinach", 100.0, "g", null, "Vegetables", now),
                new PantryItem("Olive Oil", "oil", 40.0, "ml", null, "Staples", now),
                new PantryItem("Salt", "salt", 10.0, "g", null, "Spices & Seasonings", now),
                new PantryItem("Black Pepper", "black pepper", 6.0, "g", null, "Spices & Seasonings", now)
        );

        MatchResult result = StrictRecipeMatcher.evaluate(scrambleRecipe, pantry);
        assertFalse("Recipe must be disqualified if quantity is insufficient", result.isStrictlyCookable());
        assertEquals("Missing count must be 1 (eggs)", 1, result.getMissingCount());
        assertTrue("Recipe missing 1 item must qualify for Almost There bonus tier", result.isAlmostThere());
        assertFalse(result.getMissingIngredients().get(0).isCompletelyMissing());
    }

    @Test
    public void testStrictMatch_missingStapleOil_isDisqualified() {
        long now = System.currentTimeMillis();
        // Missing cooking oil completely
        List<PantryItem> pantry = Arrays.asList(
                new PantryItem("Eggs", "egg", 4.0, "pcs", null, "Dairy & Eggs", now),
                new PantryItem("Tomatoes", "tomato", 300.0, "g", null, "Vegetables", now),
                new PantryItem("Spinach", "spinach", 100.0, "g", null, "Vegetables", now),
                new PantryItem("Salt", "salt", 10.0, "g", null, "Spices & Seasonings", now),
                new PantryItem("Black Pepper", "black pepper", 6.0, "g", null, "Spices & Seasonings", now)
        );

        MatchResult result = StrictRecipeMatcher.evaluate(scrambleRecipe, pantry);
        assertFalse("Recipe must be disqualified if cooking oil is missing", result.isStrictlyCookable());
        assertEquals(1, result.getMissingCount());
        assertTrue(result.isAlmostThere());
        assertTrue("Oil must be flagged as completely missing", result.getMissingIngredients().get(0).isCompletelyMissing());
    }

    @Test
    public void testStrictMatch_missingMultipleItems_disqualifiedFromBoth() {
        long now = System.currentTimeMillis();
        // Missing both eggs and spinach
        List<PantryItem> pantry = Arrays.asList(
                new PantryItem("Tomatoes", "tomato", 300.0, "g", null, "Vegetables", now),
                new PantryItem("Olive Oil", "oil", 40.0, "ml", null, "Staples", now),
                new PantryItem("Salt", "salt", 10.0, "g", null, "Spices & Seasonings", now),
                new PantryItem("Black Pepper", "black pepper", 6.0, "g", null, "Spices & Seasonings", now)
        );

        MatchResult result = StrictRecipeMatcher.evaluate(scrambleRecipe, pantry);
        assertFalse("Must not be strictly cookable", result.isStrictlyCookable());
        assertEquals(2, result.getMissingCount());
        assertFalse("Must NOT appear in Almost There when missing 2+ items", result.isAlmostThere());
    }

    @Test
    public void testStrictMatch_unitAndPluralityRobustness() {
        long now = System.currentTimeMillis();
        // Tomatoes in kg, singular names
        List<PantryItem> pantry = Arrays.asList(
                new PantryItem("Egg", "egg", 6.0, "pcs", null, "Dairy & Eggs", now),
                new PantryItem("Tomato", "tomato", 0.5, "kg", null, "Vegetables", now), // 0.5 kg = 500g (100g/serving -> 5 servings)
                new PantryItem("Baby Spinach", "spinach", 150.0, "g", null, "Vegetables", now), // 150g (40g/serving -> 3 servings)
                new PantryItem("Vegetable Oil", "oil", 2.0, "tbsp", null, "Staples", now), // 2 tbsp = 30ml (5ml/serving -> 6 servings)
                new PantryItem("Table Salt", "salt", 10.0, "g", null, "Spices & Seasonings", now),
                new PantryItem("Ground Black Pepper", "black pepper", 5.0, "g", null, "Spices & Seasonings", now)
        );

        MatchResult result = StrictRecipeMatcher.evaluate(scrambleRecipe, pantry);
        assertTrue("Matching must succeed despite unit differences (kg vs g) and naming aliases", result.isStrictlyCookable());
        assertEquals(0, result.getMissingCount());
        assertEquals(3, result.getMaxServings()); // Bottleneck: 6 eggs / 2 per serving = 3 servings, 150g spinach / 40g = 3 servings
    }

    @Test
    public void testFilterLists_strictQuarantine() {
        long now = System.currentTimeMillis();
        List<PantryItem> completePantry = Arrays.asList(
                new PantryItem("Eggs", "egg", 4.0, "pcs", null, "Dairy & Eggs", now),
                new PantryItem("Tomatoes", "tomato", 300.0, "g", null, "Vegetables", now),
                new PantryItem("Spinach", "spinach", 100.0, "g", null, "Vegetables", now),
                new PantryItem("Olive Oil", "oil", 40.0, "ml", null, "Staples", now),
                new PantryItem("Salt", "salt", 10.0, "g", null, "Spices & Seasonings", now),
                new PantryItem("Black Pepper", "black pepper", 6.0, "g", null, "Spices & Seasonings", now)
        );

        List<RecipeWithIngredients> list = Collections.singletonList(scrambleRecipe);

        List<MatchResult> cookable = StrictRecipeMatcher.filterStrictlyCookable(list, completePantry);
        List<MatchResult> almostThere = AlmostThereMatcher.filterAlmostThere(list, completePantry);

        assertEquals("Cookable must contain the recipe", 1, cookable.size());
        assertEquals("Almost there must NOT contain cookable recipe", 0, almostThere.size());
    }
}
