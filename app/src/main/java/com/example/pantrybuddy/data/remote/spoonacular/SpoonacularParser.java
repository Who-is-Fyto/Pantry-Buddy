package com.example.pantrybuddy.data.remote.spoonacular;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.pantrybuddy.data.local.entity.Recipe;
import com.example.pantrybuddy.data.local.entity.RecipeIngredient;
import com.example.pantrybuddy.domain.engine.IngredientNormalizer;
import com.example.pantrybuddy.domain.model.RecipeWithIngredients;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

// Parser converting raw Spoonacular JSON responses into normalized domain and Room database entities
public class SpoonacularParser {

    private static final Gson gson = new Gson();

    @NonNull
    public static List<RecipeWithIngredients> parseComplexSearchJson(@Nullable String json) {
        if (json == null || json.trim().isEmpty()) {
            return Collections.emptyList();
        }

        try {
            SpoonacularDto.ComplexSearchResponse response =
                    gson.fromJson(json, SpoonacularDto.ComplexSearchResponse.class);
            if (response == null || response.results == null) {
                return Collections.emptyList();
            }

            List<RecipeWithIngredients> recipes = new ArrayList<>();
            for (SpoonacularDto.RecipeItem item : response.results) {
                RecipeWithIngredients rwi = parseRecipeItem(item);
                if (rwi != null) {
                    recipes.add(rwi);
                }
            }
            return recipes;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @NonNull
    public static List<RecipeWithIngredients> parseFindByIngredientsJson(@Nullable String json) {
        if (json == null || json.trim().isEmpty()) {
            return Collections.emptyList();
        }

        try {
            Type listType = new TypeToken<List<SpoonacularDto.RecipeItem>>() {}.getType();
            List<SpoonacularDto.RecipeItem> items = gson.fromJson(json, listType);
            if (items == null) {
                return Collections.emptyList();
            }

            List<RecipeWithIngredients> recipes = new ArrayList<>();
            for (SpoonacularDto.RecipeItem item : items) {
                RecipeWithIngredients rwi = parseRecipeItem(item);
                if (rwi != null) {
                    recipes.add(rwi);
                }
            }
            return recipes;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @Nullable
    public static RecipeWithIngredients parseRecipeInformationJson(@Nullable String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }

        try {
            SpoonacularDto.RecipeItem item = gson.fromJson(json, SpoonacularDto.RecipeItem.class);
            return parseRecipeItem(item);
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    public static RecipeWithIngredients parseRecipeItem(@Nullable SpoonacularDto.RecipeItem item) {
        if (item == null) {
            return null;
        }

        String title = (item.title != null && !item.title.trim().isEmpty())
                ? item.title.trim()
                : "Untitled Recipe";

        String description = cleanDescription(item.summary, item.instructions);
        int cookTime = (item.readyInMinutes > 0) ? item.readyInMinutes : 25;
        int servings = (item.servings > 0) ? item.servings : 2;
        String difficulty = determineDifficulty(cookTime, item.analyzedInstructions);
        String imageUrl = (item.image != null) ? item.image.trim() : "";
        if (!imageUrl.isEmpty() && !imageUrl.startsWith("http://") && !imageUrl.startsWith("https://")) {
            imageUrl = "https://img.spoonacular.com/recipes/" + imageUrl;
        }
        String instructionsJson = extractInstructionsJson(item);
        String sourceName = (item.sourceName != null && !item.sourceName.trim().isEmpty())
                ? item.sourceName.trim()
                : "Spoonacular";
        String sourceUrl = (item.sourceUrl != null) ? item.sourceUrl.trim() : "";

        Recipe recipe = new Recipe(
                title,
                description,
                cookTime,
                servings,
                instructionsJson,
                imageUrl,
                difficulty,
                "spoonacular",
                sourceUrl,
                sourceName,
                System.currentTimeMillis()
        );

        List<RecipeIngredient> ingredients = extractIngredients(item);

        return new RecipeWithIngredients(recipe, ingredients);
    }

    @NonNull
    private static List<RecipeIngredient> extractIngredients(@NonNull SpoonacularDto.RecipeItem item) {
        List<RecipeIngredient> ingredients = new ArrayList<>();

        if (item.extendedIngredients != null && !item.extendedIngredients.isEmpty()) {
            for (SpoonacularDto.ExtendedIngredient ext : item.extendedIngredients) {
                String rawName = (ext.nameClean != null && !ext.nameClean.trim().isEmpty())
                        ? ext.nameClean.trim()
                        : ((ext.name != null && !ext.name.trim().isEmpty()) ? ext.name.trim() : ext.original);

                if (rawName == null || rawName.trim().isEmpty()) {
                    continue;
                }

                String cleanName = rawName.trim();
                String normalized = IngredientNormalizer.normalize(cleanName);
                double amount = (ext.amount > 0) ? ext.amount : 1.0;
                String unit = normalizeSpoonacularUnit(ext.unit, amount);

                ingredients.add(new RecipeIngredient(0, cleanName, normalized, amount, unit));
            }
        } else {
            // Fallback: merge usedIngredients and missedIngredients from findByIngredients endpoint
            List<SpoonacularDto.IngredientSummary> combined = new ArrayList<>();
            if (item.usedIngredients != null) {
                combined.addAll(item.usedIngredients);
            }
            if (item.missedIngredients != null) {
                combined.addAll(item.missedIngredients);
            }

            for (SpoonacularDto.IngredientSummary sum : combined) {
                if (sum.name == null || sum.name.trim().isEmpty()) {
                    continue;
                }
                String cleanName = sum.name.trim();
                String normalized = IngredientNormalizer.normalize(cleanName);
                double amount = (sum.amount > 0) ? sum.amount : 1.0;
                String unit = normalizeSpoonacularUnit(sum.unit, amount);

                ingredients.add(new RecipeIngredient(0, cleanName, normalized, amount, unit));
            }
        }

        return ingredients;
    }

    @NonNull
    private static String extractInstructionsJson(@NonNull SpoonacularDto.RecipeItem item) {
        List<String> stepTexts = new ArrayList<>();

        if (item.analyzedInstructions != null && !item.analyzedInstructions.isEmpty()) {
            for (SpoonacularDto.AnalyzedInstruction ai : item.analyzedInstructions) {
                if (ai.steps != null) {
                    for (SpoonacularDto.Step step : ai.steps) {
                        if (step.step != null && !step.step.trim().isEmpty()) {
                            stepTexts.add(step.step.trim());
                        }
                    }
                }
            }
        }

        if (stepTexts.isEmpty() && item.instructions != null && !item.instructions.trim().isEmpty()) {
            String plain = stripHtml(item.instructions);
            String[] split = plain.split("\\r?\\n|(?<=[.!?])\\s+");
            for (String s : split) {
                String clean = s.trim();
                if (!clean.isEmpty() && clean.length() > 5) {
                    stepTexts.add(clean);
                }
            }
        }

        if (stepTexts.isEmpty()) {
            stepTexts.add("Prepare ingredients according to your preferred taste.");
            stepTexts.add("Combine ingredients and cook thoroughly until done.");
            stepTexts.add("Serve immediately and enjoy!");
        }

        return gson.toJson(stepTexts);
    }

    @NonNull
    private static String cleanDescription(@Nullable String summary, @Nullable String instructions) {
        if (summary != null && !summary.trim().isEmpty()) {
            String plain = stripHtml(summary);
            // Return first two sentences or first 160 characters
            int periodIdx = plain.indexOf('.');
            if (periodIdx > 20 && periodIdx < 200) {
                int secondPeriod = plain.indexOf('.', periodIdx + 1);
                if (secondPeriod > periodIdx && secondPeriod < 240) {
                    return plain.substring(0, secondPeriod + 1).trim();
                }
                return plain.substring(0, periodIdx + 1).trim();
            }
            if (plain.length() > 160) {
                return plain.substring(0, 157).trim() + "...";
            }
            return plain.trim();
        }

        if (instructions != null && !instructions.trim().isEmpty()) {
            String plain = stripHtml(instructions);
            return plain.length() > 140 ? plain.substring(0, 137).trim() + "..." : plain.trim();
        }

        return "Delicious home-cooked recipe prepared with fresh ingredients.";
    }

    @NonNull
    public static String stripHtml(@NonNull String html) {
        return html.replaceAll("<[^>]*>", "").replaceAll("&nbsp;", " ").trim();
    }

    @NonNull
    public static String normalizeSpoonacularUnit(@Nullable String rawUnit, double amount) {
        if (rawUnit == null || rawUnit.trim().isEmpty()) {
            return amount >= 25.0 ? "g" : "pcs";
        }

        String u = rawUnit.trim().toLowerCase(Locale.US);

        if (u.equals("tbsp") || u.equals("tablespoon") || u.equals("tablespoons") || u.equals("tbs")) {
            return "tbsp";
        }
        if (u.equals("tsp") || u.equals("teaspoon") || u.equals("teaspoons")) {
            return "tsp";
        }
        if (u.equals("g") || u.equals("gram") || u.equals("grams")) {
            return "g";
        }
        if (u.equals("kg") || u.equals("kilogram") || u.equals("kilograms")) {
            return "kg";
        }
        if (u.equals("ml") || u.equals("milliliter") || u.equals("milliliters")) {
            return "ml";
        }
        if (u.equals("l") || u.equals("liter") || u.equals("liters") || u.equals("litre")) {
            return "l";
        }
        if (u.equals("cup") || u.equals("cups")) {
            return "ml"; // In cooking calculations, 1 cup is approx 240ml
        }
        if (u.equals("oz") || u.equals("ounce") || u.equals("ounces")) {
            return "g";
        }
        if (u.equals("clove") || u.equals("cloves") || u.equals("pinch") || u.equals("piece")
                || u.equals("pieces") || u.equals("slice") || u.equals("slices") || u.equals("serving")) {
            return "pcs";
        }

        return "pcs";
    }

    @NonNull
    private static String determineDifficulty(int cookTime, @Nullable List<SpoonacularDto.AnalyzedInstruction> instructions) {
        int stepsCount = 0;
        if (instructions != null) {
            for (SpoonacularDto.AnalyzedInstruction ai : instructions) {
                if (ai.steps != null) {
                    stepsCount += ai.steps.size();
                }
            }
        }

        if (cookTime <= 20 && stepsCount <= 4) {
            return "Easy";
        } else if (cookTime <= 45 && stepsCount <= 8) {
            return "Medium";
        } else {
            return "Hard";
        }
    }
}
