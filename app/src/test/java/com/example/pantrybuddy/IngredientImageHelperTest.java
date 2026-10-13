package com.example.pantrybuddy;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.pantrybuddy.utils.IngredientImageHelper;

import org.junit.Test;

public class IngredientImageHelperTest {

    @Test
    public void testGetImageUrlForExactIngredients() {
        String eggUrl = IngredientImageHelper.getImageUrlForIngredient("Eggs", "egg");
        assertNotNull(eggUrl);
        assertTrue(eggUrl.contains("egg.png"));

        String tomatoUrl = IngredientImageHelper.getImageUrlForIngredient("Tomatoes", "tomato");
        assertNotNull(tomatoUrl);
        assertTrue(tomatoUrl.contains("tomato.png"));

        String spinachUrl = IngredientImageHelper.getImageUrlForIngredient("Spinach", "spinach");
        assertNotNull(spinachUrl);
        assertTrue(spinachUrl.contains("spinach.jpg"));

        String cheeseUrl = IngredientImageHelper.getImageUrlForIngredient("Cheddar Cheese", "cheese");
        assertNotNull(cheeseUrl);
        assertTrue(cheeseUrl.contains("cheddar-cheese.png"));
    }

    @Test
    public void testGetImageUrlForPartialAndNormalizedMatches() {
        String tunaUrl = IngredientImageHelper.getImageUrlForIngredient("Chunk Light Canned Tuna in Water", null);
        assertNotNull(tunaUrl);
        assertTrue(tunaUrl.contains("canned-tuna.png"));

        String potatoUrl = IngredientImageHelper.getImageUrlForIngredient("Baby Yukon Gold Potatoes", null);
        assertNotNull(potatoUrl);
        assertTrue(potatoUrl.contains("potatoes-yukon-gold.png"));

        String sweetcornUrl = IngredientImageHelper.getImageUrlForIngredient("Canned Sweetcorn", "sweetcorn");
        assertNotNull(sweetcornUrl);
        assertTrue(sweetcornUrl.contains("corn.png"));
    }

    @Test
    public void testGetImageUrlForNullAndEmpty() {
        assertNull(IngredientImageHelper.getImageUrlForIngredient(null, null));
        assertNull(IngredientImageHelper.getImageUrlForIngredient("", "   "));
    }

    @Test
    public void testGetImageUrlFallbackHeuristic() {
        String obscureUrl = IngredientImageHelper.getImageUrlForIngredient("Truffle Oil", null);
        assertNotNull(obscureUrl);
        assertTrue(obscureUrl.contains("https://img.spoonacular.com/ingredients_100x100/"));
    }
}
