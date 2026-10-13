package com.example.pantrybuddy.domain.engine;

import com.example.pantrybuddy.data.local.entity.PantryItem;
import com.example.pantrybuddy.domain.model.MatchResult;
import com.example.pantrybuddy.domain.model.RecipeWithIngredients;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Finds recipes missing 1 or 2 ingredients
public class AlmostThereMatcher {

    // Returns recipes missing 1 or 2 items (sorted by missing count, then cook time)
    public static List<MatchResult> filterAlmostThere(List<RecipeWithIngredients> allRecipes, List<PantryItem> pantryItems) {
        return filterMissingUpTo(allRecipes, pantryItems, 2);
    }

    public static List<MatchResult> filterMissing1or2(List<RecipeWithIngredients> allRecipes, List<PantryItem> pantryItems) {
        return filterMissingUpTo(allRecipes, pantryItems, 2);
    }

    public static List<MatchResult> filterMissingUpTo(List<RecipeWithIngredients> allRecipes, List<PantryItem> pantryItems, int maxMissing) {
        if (allRecipes == null || allRecipes.isEmpty()) {
            return Collections.emptyList();
        }

        List<MatchResult> almostThere = new ArrayList<>();
        for (RecipeWithIngredients r : allRecipes) {
            MatchResult result = StrictRecipeMatcher.evaluate(r, pantryItems);
            if (!result.isStrictlyCookable() && result.getMissingCount() > 0 && result.getMissingCount() <= maxMissing) {
                almostThere.add(result);
            }
        }

        Collections.sort(almostThere, (a, b) -> {
            int cmp = Integer.compare(a.getMissingCount(), b.getMissingCount());
            if (cmp != 0) return cmp;
            return Integer.compare(a.getRecipe().getCookTimeMinutes(), b.getRecipe().getCookTimeMinutes());
        });

        return almostThere;
    }
}
