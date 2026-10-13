package com.example.pantrybuddy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.pantrybuddy.data.local.entity.PantryItem;
import com.example.pantrybuddy.data.local.entity.RecipeIngredient;
import com.example.pantrybuddy.data.remote.spoonacular.SpoonacularParser;
import com.example.pantrybuddy.domain.engine.StrictRecipeMatcher;
import com.example.pantrybuddy.domain.model.MatchResult;
import com.example.pantrybuddy.domain.model.RecipeWithIngredients;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Unit tests validating Spoonacular JSON parsing, ingredient mapping, and strict matcher evaluation
public class SpoonacularParserTest {

    @Test
    public void testParseComplexSearchJson() {
        String json = "{\n" +
                "  \"results\": [\n" +
                "    {\n" +
                "      \"id\": 716429,\n" +
                "      \"title\": \"Garlic Tomato Pasta\",\n" +
                "      \"image\": \"https://img.spoonacular.com/recipes/716429-312x231.jpg\",\n" +
                "      \"readyInMinutes\": 20,\n" +
                "      \"servings\": 2,\n" +
                "      \"summary\": \"<p>A fast and flavorful <b>Garlic Tomato Pasta</b> recipe.</p>\",\n" +
                "      \"extendedIngredients\": [\n" +
                "        {\n" +
                "          \"id\": 11215,\n" +
                "          \"name\": \"garlic\",\n" +
                "          \"nameClean\": \"garlic\",\n" +
                "          \"amount\": 3.0,\n" +
                "          \"unit\": \"cloves\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"id\": 11529,\n" +
                "          \"name\": \"tomatoes\",\n" +
                "          \"nameClean\": \"tomatoes\",\n" +
                "          \"amount\": 200.0,\n" +
                "          \"unit\": \"g\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"analyzedInstructions\": [\n" +
                "        {\n" +
                "          \"steps\": [\n" +
                "            {\"number\": 1, \"step\": \"Mince the garlic.\"},\n" +
                "            {\"number\": 2, \"step\": \"Sauté tomatoes and garlic together until tender.\"}\n" +
                "          ]\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"totalResults\": 1\n" +
                "}";

        List<RecipeWithIngredients> list = SpoonacularParser.parseComplexSearchJson(json);
        assertNotNull(list);
        assertEquals(1, list.size());

        RecipeWithIngredients rwi = list.get(0);
        assertEquals("Garlic Tomato Pasta", rwi.getRecipe().getTitle());
        assertEquals(20, rwi.getRecipe().getCookTimeMinutes());
        assertEquals(2, rwi.getRecipe().getDefaultServings());
        assertEquals("https://img.spoonacular.com/recipes/716429-312x231.jpg", rwi.getRecipe().getImageUrl());
        assertFalse(rwi.getRecipe().getDescription().contains("<p>"));
        assertTrue(rwi.getRecipe().getDescription().contains("Garlic Tomato Pasta"));

        List<RecipeIngredient> ingredients = rwi.getIngredients();
        assertEquals(2, ingredients.size());

        RecipeIngredient garlic = ingredients.get(0);
        assertEquals("garlic", garlic.getIngredientName());
        assertEquals(3.0, garlic.getAmount(), 0.001);
        assertEquals("pcs", garlic.getUnit());

        RecipeIngredient tomatoes = ingredients.get(1);
        assertEquals("tomatoes", tomatoes.getIngredientName());
        assertEquals(200.0, tomatoes.getAmount(), 0.001);
        assertEquals("g", tomatoes.getUnit());
    }

    @Test
    public void testParseFindByIngredientsJson() {
        String json = "[\n" +
                "  {\n" +
                "    \"id\": 640352,\n" +
                "    \"title\": \"Cranberry Apple Crisp\",\n" +
                "    \"image\": \"https://img.spoonacular.com/recipes/640352-312x231.jpg\",\n" +
                "    \"usedIngredientCount\": 2,\n" +
                "    \"missedIngredientCount\": 1,\n" +
                "    \"usedIngredients\": [\n" +
                "      {\"id\": 9003, \"name\": \"apples\", \"amount\": 4.0, \"unit\": \"\"}\n" +
                "    ],\n" +
                "    \"missedIngredients\": [\n" +
                "      {\"id\": 9078, \"name\": \"cranberries\", \"amount\": 150.0, \"unit\": \"g\"}\n" +
                "    ]\n" +
                "  }\n" +
                "]";

        List<RecipeWithIngredients> list = SpoonacularParser.parseFindByIngredientsJson(json);
        assertNotNull(list);
        assertEquals(1, list.size());

        RecipeWithIngredients rwi = list.get(0);
        assertEquals("Cranberry Apple Crisp", rwi.getRecipe().getTitle());
        assertEquals(2, rwi.getIngredients().size());
    }

    @Test
    public void testUnitNormalization() {
        assertEquals("tbsp", SpoonacularParser.normalizeSpoonacularUnit("tablespoon", 2));
        assertEquals("tbsp", SpoonacularParser.normalizeSpoonacularUnit("tbsp", 1));
        assertEquals("tsp", SpoonacularParser.normalizeSpoonacularUnit("teaspoon", 1));
        assertEquals("g", SpoonacularParser.normalizeSpoonacularUnit("grams", 100));
        assertEquals("kg", SpoonacularParser.normalizeSpoonacularUnit("kg", 1));
        assertEquals("ml", SpoonacularParser.normalizeSpoonacularUnit("cup", 1));
        assertEquals("pcs", SpoonacularParser.normalizeSpoonacularUnit("cloves", 3));
        assertEquals("pcs", SpoonacularParser.normalizeSpoonacularUnit("", 2));
        assertEquals("g", SpoonacularParser.normalizeSpoonacularUnit("", 50));
    }

    @Test
    public void testHtmlStripping() {
        String raw = "<p>This is a <b>tasty</b> meal with &nbsp; great herbs.</p>";
        String clean = SpoonacularParser.stripHtml(raw);
        assertEquals("This is a tasty meal with   great herbs.", clean);
    }

    @Test
    public void testStrictMatcherOnSpoonacularRecipe() {
        // Build a recipe parsed from Spoonacular: requires 4 eggs and 200g tomatoes
        String json = "{\n" +
                "  \"results\": [\n" +
                "    {\n" +
                "      \"id\": 9991,\n" +
                "      \"title\": \"Tomato Scramble\",\n" +
                "      \"image\": \"https://img.spoonacular.com/recipes/9991.jpg\",\n" +
                "      \"readyInMinutes\": 10,\n" +
                "      \"servings\": 2,\n" +
                "      \"extendedIngredients\": [\n" +
                "        {\"id\": 1123, \"name\": \"eggs\", \"amount\": 4.0, \"unit\": \"\"},\n" +
                "        {\"id\": 11529, \"name\": \"tomatoes\", \"amount\": 200.0, \"unit\": \"g\"}\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        List<RecipeWithIngredients> recipes = SpoonacularParser.parseComplexSearchJson(json);
        assertEquals(1, recipes.size());
        RecipeWithIngredients rwi = recipes.get(0);

        // Case 1: Pantry has enough eggs and tomatoes -> Strictly Cookable
        List<PantryItem> fullPantry = new ArrayList<>();
        fullPantry.add(new PantryItem("Eggs", "egg", 4.0, "pcs", null, "Dairy & Eggs", 0));
        fullPantry.add(new PantryItem("Tomatoes", "tomato", 300.0, "g", null, "Vegetables", 0));

        MatchResult cookableResult = StrictRecipeMatcher.evaluate(rwi, fullPantry);
        assertNotNull(cookableResult);
        assertTrue(cookableResult.isStrictlyCookable());
        assertEquals(0, cookableResult.getMissingCount());

        // Case 2: Pantry has only eggs, missing tomatoes -> Not cookable, missing 1
        List<PantryItem> partialPantry = Collections.singletonList(
                new PantryItem("Eggs", "egg", 4.0, "pcs", null, "Dairy & Eggs", 0)
        );

        MatchResult partialResult = StrictRecipeMatcher.evaluate(rwi, partialPantry);
        assertNotNull(partialResult);
        assertFalse(partialResult.isStrictlyCookable());
        assertEquals(1, partialResult.getMissingCount());
    }

    @Test
    public void testEmptyOrInvalidInputs() {
        assertTrue(SpoonacularParser.parseComplexSearchJson(null).isEmpty());
        assertTrue(SpoonacularParser.parseComplexSearchJson("").isEmpty());
        assertTrue(SpoonacularParser.parseComplexSearchJson("{ invalid }").isEmpty());
        assertNull(SpoonacularParser.parseRecipeInformationJson(null));
        assertNull(SpoonacularParser.parseRecipeItem(null));
    }
}
