package com.example.pantrybuddy.data.local;

import android.content.Context;
import android.util.Log;
import com.example.pantrybuddy.data.local.entity.PantryItem;
import com.example.pantrybuddy.data.local.entity.Recipe;
import com.example.pantrybuddy.data.local.entity.RecipeIngredient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

// Populates starter recipes and demo pantry items
public class DatabaseInitializer {
    private static final String TAG = "DatabaseInitializer";

    // Reads recipes_seed.json and adds recipes to SQLite if empty
    public static void populateInitialRecipes(AppDatabase db, Context context) {
        if (db.recipeDao().getRecipeCount() > 0) {
            Log.d(TAG, "Recipes already seeded, skipping.");
            return;
        }

        try {
            InputStream inputStream = context.getAssets().open("recipes_seed.json");
            Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            Gson gson = new Gson();
            Type listType = new TypeToken<List<RecipeSeedDto>>() {}.getType();
            List<RecipeSeedDto> seedList = gson.fromJson(reader, listType);
            reader.close();

            if (seedList != null && !seedList.isEmpty()) {
                db.runInTransaction(() -> {
                    for (RecipeSeedDto dto : seedList) {
                        String instructionsJson = gson.toJson(dto.instructions);
                        Recipe recipe = new Recipe(
                            dto.title,
                            dto.description,
                            dto.cookTimeMinutes,
                            dto.defaultServings,
                            instructionsJson,
                            dto.imageUrl != null ? dto.imageUrl : "",
                            dto.difficulty
                        );
                        long recipeId = db.recipeDao().insertRecipe(recipe);

                        if (dto.ingredients != null) {
                            List<RecipeIngredient> ingredientEntities = new ArrayList<>();
                            for (RecipeSeedDto.IngredientSeedDto ingDto : dto.ingredients) {
                                ingredientEntities.add(new RecipeIngredient(
                                    recipeId,
                                    ingDto.name,
                                    ingDto.normalizedName,
                                    ingDto.amount,
                                    ingDto.unit
                                ));
                            }
                            db.recipeDao().insertIngredients(ingredientEntities);
                        }
                    }
                });
                Log.d(TAG, "Successfully seeded " + seedList.size() + " recipes.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error seeding recipes from assets", e);
        }
    }

    // Adds standard demo items to pantry for marker testing
    public static void populateSamplePantry(AppDatabase db) {
        long now = System.currentTimeMillis();

        Calendar todayCal = Calendar.getInstance();
        todayCal.set(Calendar.HOUR_OF_DAY, 23);
        todayCal.set(Calendar.MINUTE, 59);
        long useToday = todayCal.getTimeInMillis();

        Calendar tomorrowCal = Calendar.getInstance();
        tomorrowCal.add(Calendar.DAY_OF_YEAR, 1);
        tomorrowCal.set(Calendar.HOUR_OF_DAY, 23);
        tomorrowCal.set(Calendar.MINUTE, 59);
        long useTomorrow = tomorrowCal.getTimeInMillis();

        List<PantryItem> sampleItems = new ArrayList<>();
        sampleItems.add(new PantryItem("Eggs", "egg", 4.0, "pcs", null, "Dairy & Eggs", now));
        sampleItems.add(new PantryItem("Tomatoes", "tomato", 300.0, "g", useTomorrow, "Vegetables", now));
        sampleItems.add(new PantryItem("Spinach", "spinach", 100.0, "g", useToday, "Vegetables", now));
        sampleItems.add(new PantryItem("Olive Oil", "oil", 40.0, "ml", null, "Staples", now));
        sampleItems.add(new PantryItem("Salt", "salt", 10.0, "g", null, "Spices & Seasonings", now));
        sampleItems.add(new PantryItem("Black Pepper", "black pepper", 6.0, "g", null, "Spices & Seasonings", now));

        db.pantryDao().insertAll(sampleItems);
        Log.d(TAG, "Seeded 6 sample pantry items.");
    }

    // Resets pantry and ensures starter recipes are loaded
    public static void resetAllToSampleData(AppDatabase db, Context context) {
        db.runInTransaction(() -> {
            db.pantryDao().deleteAll();
            populateInitialRecipes(db, context);
            populateSamplePantry(db);
        });
    }
}
