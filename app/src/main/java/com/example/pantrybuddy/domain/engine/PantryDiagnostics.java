package com.example.pantrybuddy.domain.engine;

import com.example.pantrybuddy.data.local.entity.PantryItem;
import com.example.pantrybuddy.domain.model.ExpiryStatus;
import com.example.pantrybuddy.domain.model.MatchResult;
import com.example.pantrybuddy.domain.model.RecipeWithIngredients;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// Generates helpful unlocking recommendations and pantry stock summaries when zero recipes match
public class PantryDiagnostics {

    // Analyzes the pantry and recipes to suggest which single ingredient would unlock the most meals
    public static String generateUnlockingRecommendation(
            List<RecipeWithIngredients> allRecipes,
            List<PantryItem> pantryItems,
            List<MatchResult> almostThereRecipes) {

        if (pantryItems == null || pantryItems.isEmpty()) {
            return "Your pantry is currently empty. Add staple ingredients like eggs, cooking oil, or rice to start seeing recipes.";
        }

        // 1. If almost-there recipes exist (recipes missing exactly 1 item)
        if (almostThereRecipes != null && !almostThereRecipes.isEmpty()) {
            Map<String, List<MatchResult>> missingMap = new HashMap<>();
            Map<String, MatchResult.MissingIngredientInfo> infoMap = new HashMap<>();

            for (MatchResult mr : almostThereRecipes) {
                if (mr.getMissingIngredients() != null && !mr.getMissingIngredients().isEmpty()) {
                    MatchResult.MissingIngredientInfo missing = mr.getMissingIngredients().get(0);
                    String key = missing.getIngredientName().toLowerCase(Locale.US);
                    if (!missingMap.containsKey(key)) {
                        missingMap.put(key, new ArrayList<>());
                        infoMap.put(key, missing);
                    }
                    missingMap.get(key).add(mr);
                }
            }

            // Find missing ingredient that unlocks the most recipes
            String bestKey = null;
            int maxUnlocked = 0;
            for (Map.Entry<String, List<MatchResult>> entry : missingMap.entrySet()) {
                if (entry.getValue().size() > maxUnlocked) {
                    maxUnlocked = entry.getValue().size();
                    bestKey = entry.getKey();
                }
            }

            if (bestKey != null) {
                MatchResult.MissingIngredientInfo info = infoMap.get(bestKey);
                String recipeWord = (maxUnlocked == 1) ? "recipe" : "recipes";

                double neededAmount = info.isCompletelyMissing()
                        ? info.getAmountNeeded()
                        : Math.max(0.1, info.getAmountNeeded() - info.getAmountAtHome());

                String qtyStr = (neededAmount == (long) neededAmount)
                        ? String.valueOf((long) neededAmount)
                        : String.format(Locale.US, "%.1f", neededAmount);

                return String.format(
                        Locale.US,
                        "Adding %s %s %s would immediately unlock %d %s with your current pantry.",
                        qtyStr,
                        info.getUnit(),
                        info.getIngredientName(),
                        maxUnlocked,
                        recipeWord
                );
            }
        }

        // 2. If no almost-there recipes exist, check which item is most needed across all recipes
        if (allRecipes != null && !allRecipes.isEmpty()) {
            Map<String, Integer> missingTally = new HashMap<>();
            for (RecipeWithIngredients rwi : allRecipes) {
                MatchResult result = StrictRecipeMatcher.evaluate(rwi, pantryItems);
                if (result.getMissingIngredients() != null) {
                    for (MatchResult.MissingIngredientInfo m : result.getMissingIngredients()) {
                        String name = m.getIngredientName();
                        missingTally.put(name, missingTally.getOrDefault(name, 0) + 1);
                    }
                }
            }

            String topItem = null;
            int topCount = 0;
            for (Map.Entry<String, Integer> entry : missingTally.entrySet()) {
                if (entry.getValue() > topCount) {
                    topCount = entry.getValue();
                    topItem = entry.getKey();
                }
            }

            if (topItem != null) {
                return String.format(
                        Locale.US,
                        "Adding staples like %s would bring several meals closer to being cookable.",
                        topItem
                );
            }
        }

        return "Add a few everyday ingredients like eggs, cooking oil, or rice to unlock recipe suggestions.";
    }

    // Creates a readable summary of active pantry stock with shelf-life status
    public static String formatActiveStockSummary(List<PantryItem> pantryItems) {
        if (pantryItems == null || pantryItems.isEmpty()) {
            return "No ingredients currently in your pantry.";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pantryItems.size(); i++) {
            PantryItem item = pantryItems.get(i);
            String qty = (item.getQuantity() == (long) item.getQuantity())
                    ? String.valueOf((long) item.getQuantity())
                    : String.format(Locale.US, "%.1f", item.getQuantity());

            sb.append("• ").append(item.getName()).append(" (").append(qty).append(" ").append(item.getUnit()).append(")");

            if (item.getExpiryDate() != null) {
                ExpiryStatus status = ExpiryStatus.fromTimestamp(item.getExpiryDate());
                if (status == ExpiryStatus.TODAY) {
                    sb.append(" — use today");
                } else if (status == ExpiryStatus.TOMORROW) {
                    sb.append(" — use tomorrow");
                } else if (status == ExpiryStatus.EXPIRED) {
                    sb.append(" — expired");
                }
            }

            if (i < pantryItems.size() - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}
