package com.example.pantrybuddy.domain.engine;

import com.example.pantrybuddy.data.local.entity.PantryItem;
import com.example.pantrybuddy.data.local.entity.RecipeIngredient;
import com.example.pantrybuddy.domain.model.MatchResult;
import com.example.pantrybuddy.domain.model.RecipeWithIngredients;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Checks if user has 100% of the ingredients to make a recipe
public class StrictRecipeMatcher {

    // Check one recipe against what we have in the pantry
    public static MatchResult evaluate(RecipeWithIngredients recipeWithIngredients, List<PantryItem> pantryItems) {
        if (recipeWithIngredients == null || recipeWithIngredients.getRecipe() == null) {
            return new MatchResult(null, false, 0, null, 0, false, Long.MAX_VALUE);
        }

        List<RecipeIngredient> required = recipeWithIngredients.getIngredients();
        if (required == null || required.isEmpty()) {
            return new MatchResult(recipeWithIngredients, false, 0, null, 0, false, Long.MAX_VALUE);
        }

        int missingCount = 0;
        List<MatchResult.MissingIngredientInfo> missingList = new ArrayList<>();
        int maxServings = Integer.MAX_VALUE;
        boolean usesExpiringSoon = false;
        long earliestExpiry = Long.MAX_VALUE;

        long now = System.currentTimeMillis();
        long twoDaysFromNow = now + (2L * 24 * 60 * 60 * 1000);

        for (RecipeIngredient req : required) {
            PantryItem matchingItem = findMatchingPantryItem(req, pantryItems);

            if (matchingItem == null || matchingItem.getQuantity() <= 0.001) {
                missingCount++;
                missingList.add(new MatchResult.MissingIngredientInfo(
                        req.getIngredientName(),
                        req.getAmount(),
                        req.getUnit(),
                        0.0,
                        true
                ));
            } else {
                boolean sufficient = UnitConverter.isSufficient(
                        matchingItem.getQuantity(),
                        matchingItem.getUnit(),
                        req.getAmount(),
                        req.getUnit(),
                        req.getNormalizedName()
                );

                if (!sufficient) {
                    missingCount++;
                    missingList.add(new MatchResult.MissingIngredientInfo(
                            req.getIngredientName(),
                            req.getAmount(),
                            req.getUnit(),
                            matchingItem.getQuantity(),
                            false
                    ));
                } else {
                    int defaultServings = Math.max(1, recipeWithIngredients.getRecipe().getDefaultServings());
                    double singleServingReq = req.getAmount() / defaultServings;
                    int itemServings = UnitConverter.calculateMaxServings(
                            matchingItem.getQuantity(),
                            matchingItem.getUnit(),
                            singleServingReq,
                            req.getUnit(),
                            req.getNormalizedName()
                    );
                    maxServings = Math.min(maxServings, itemServings);

                    // Check if ingredient is expiring soon (today or within 2 days)
                    if (matchingItem.getExpiryDate() != null && matchingItem.getExpiryDate() <= twoDaysFromNow) {
                        usesExpiringSoon = true;
                        if (matchingItem.getExpiryDate() < earliestExpiry) {
                            earliestExpiry = matchingItem.getExpiryDate();
                        }
                    }
                }
            }
        }

        boolean isStrictlyCookable = (missingCount == 0 && maxServings >= 1);
        if (!isStrictlyCookable) {
            maxServings = 0;
        }

        return new MatchResult(
                recipeWithIngredients,
                isStrictlyCookable,
                missingCount,
                missingList,
                maxServings,
                usesExpiringSoon,
                earliestExpiry
        );
    }

    // Returns only 100% cookable recipes, expiring ingredients first
    public static List<MatchResult> filterStrictlyCookable(List<RecipeWithIngredients> allRecipes, List<PantryItem> pantryItems) {
        if (allRecipes == null || allRecipes.isEmpty()) {
            return Collections.emptyList();
        }

        List<MatchResult> cookable = new ArrayList<>();
        for (RecipeWithIngredients r : allRecipes) {
            MatchResult result = evaluate(r, pantryItems);
            if (result.isStrictlyCookable()) {
                cookable.add(result);
            }
        }

        // Put expiring items first, then quickest recipes
        Collections.sort(cookable, (a, b) -> {
            if (a.isUsesExpiringSoonItem() != b.isUsesExpiringSoonItem()) {
                return a.isUsesExpiringSoonItem() ? -1 : 1;
            }
            if (a.isUsesExpiringSoonItem() && b.isUsesExpiringSoonItem()) {
                int cmp = Long.compare(a.getEarliestExpiryTimestamp(), b.getEarliestExpiryTimestamp());
                if (cmp != 0) return cmp;
            }
            return Integer.compare(a.getRecipe().getCookTimeMinutes(), b.getRecipe().getCookTimeMinutes());
        });

        return cookable;
    }

    // Matches ingredient names even if user typed singular or plural
    public static PantryItem findMatchingPantryItem(RecipeIngredient req, List<PantryItem> pantryItems) {
        if (req == null || pantryItems == null) return null;

        for (PantryItem item : pantryItems) {
            if (IngredientNormalizer.matches(item.getNormalizedName(), req.getNormalizedName())) {
                return item;
            }
            if (IngredientNormalizer.matches(item.getName(), req.getIngredientName())) {
                return item;
            }
        }
        return null;
    }
}
