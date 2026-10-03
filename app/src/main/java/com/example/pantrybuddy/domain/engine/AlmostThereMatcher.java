package com.example.pantrybuddy.domain.engine;

import com.example.pantrybuddy.data.local.entity.PantryItem;
import com.example.pantrybuddy.domain.model.MatchResult;
import com.example.pantrybuddy.domain.model.RecipeWithIngredients;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Finds recipes missing just 1 ingredient for bonus stretch
public class AlmostThereMatcher {

    // Only returns recipes missing exactly 1 item
    public static List<MatchResult> filterAlmostThere(List<RecipeWithIngredients> allRecipes, List<PantryItem> pantryItems) {
        if (allRecipes == null || allRecipes.isEmpty()) {
            return Collections.emptyList();
        }

        List<MatchResult> almostThere = new ArrayList<>();
        for (RecipeWithIngredients r : allRecipes) {
            MatchResult result = StrictRecipeMatcher.evaluate(r, pantryItems);
            // Strict quarantine: only exactly 1 missing ingredient
            if (result.isAlmostThere()) {
                almostThere.add(result);
            }
        }

        Collections.sort(almostThere, (a, b) ->
                Integer.compare(a.getRecipe().getCookTimeMinutes(), b.getRecipe().getCookTimeMinutes()));

        return almostThere;
    }
}
