package com.example.pantrybuddy;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.pantrybuddy.data.local.RecipeSeedDto;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.List;

public class RecipeSeedTest {

    @Test
    public void testRecipesSeedJson_containsAtLeast18ValidRecipes() throws Exception {
        File seedFile = new File("src/main/assets/recipes_seed.json");
        if (!seedFile.exists()) {
            seedFile = new File("app/src/main/assets/recipes_seed.json");
        }
        assertTrue("Seed file must exist", seedFile.exists());

        Gson gson = new Gson();
        Type listType = new TypeToken<List<RecipeSeedDto>>() {}.getType();
        List<RecipeSeedDto> recipes = gson.fromJson(new FileReader(seedFile), listType);

        assertNotNull("Recipes list should not be null", recipes);
        assertTrue("Must have at least 18 recipes per course brief", recipes.size() >= 18);

        for (RecipeSeedDto recipe : recipes) {
            assertNotNull("Recipe title cannot be null", recipe.title);
            assertFalse("Recipe title cannot be empty", recipe.title.trim().isEmpty());
            assertTrue("Cook time must be positive: " + recipe.title, recipe.cookTimeMinutes > 0);
            assertTrue("Default servings must be positive: " + recipe.title, recipe.defaultServings > 0);
            assertNotNull("Instructions cannot be null: " + recipe.title, recipe.instructions);
            assertFalse("Instructions cannot be empty: " + recipe.title, recipe.instructions.isEmpty());
            assertNotNull("Ingredients cannot be null: " + recipe.title, recipe.ingredients);
            assertTrue("Recipe must have at least 3 itemized ingredients: " + recipe.title, recipe.ingredients.size() >= 3);

            for (RecipeSeedDto.IngredientSeedDto ing : recipe.ingredients) {
                assertNotNull("Ingredient name required: " + recipe.title, ing.name);
                assertNotNull("Normalized name required: " + recipe.title, ing.normalizedName);
                assertTrue("Amount must be positive: " + ing.name + " in " + recipe.title, ing.amount > 0);
                assertNotNull("Unit required: " + ing.name + " in " + recipe.title, ing.unit);
            }
        }
    }
}
