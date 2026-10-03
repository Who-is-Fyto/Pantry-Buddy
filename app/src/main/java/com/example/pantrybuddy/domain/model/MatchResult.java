package com.example.pantrybuddy.domain.model;

import com.example.pantrybuddy.data.local.entity.Recipe;
import java.util.ArrayList;
import java.util.List;

// Stores matching result for a recipe and any missing items
public class MatchResult {

    private final RecipeWithIngredients recipeWithIngredients;
    private final boolean isStrictlyCookable;
    private final int missingCount;
    private final List<MissingIngredientInfo> missingIngredients;
    private final int maxServings;
    private final boolean usesExpiringSoonItem;
    private final long earliestExpiryTimestamp;

    public MatchResult(
            RecipeWithIngredients recipeWithIngredients,
            boolean isStrictlyCookable,
            int missingCount,
            List<MissingIngredientInfo> missingIngredients,
            int maxServings,
            boolean usesExpiringSoonItem,
            long earliestExpiryTimestamp) {
        this.recipeWithIngredients = recipeWithIngredients;
        this.isStrictlyCookable = isStrictlyCookable;
        this.missingCount = missingCount;
        this.missingIngredients = missingIngredients != null ? missingIngredients : new ArrayList<>();
        this.maxServings = maxServings;
        this.usesExpiringSoonItem = usesExpiringSoonItem;
        this.earliestExpiryTimestamp = earliestExpiryTimestamp;
    }

    public RecipeWithIngredients getRecipeWithIngredients() {
        return recipeWithIngredients;
    }

    public Recipe getRecipe() {
        return recipeWithIngredients != null ? recipeWithIngredients.getRecipe() : null;
    }

    public boolean isStrictlyCookable() {
        return isStrictlyCookable;
    }

    public int getMissingCount() {
        return missingCount;
    }

    public List<MissingIngredientInfo> getMissingIngredients() {
        return missingIngredients;
    }

    public int getMaxServings() {
        return maxServings;
    }

    public boolean isUsesExpiringSoonItem() {
        return usesExpiringSoonItem;
    }

    public long getEarliestExpiryTimestamp() {
        return earliestExpiryTimestamp;
    }

    public boolean isAlmostThere() {
        return !isStrictlyCookable && missingCount == 1;
    }

    public static class MissingIngredientInfo {
        private final String ingredientName;
        private final double amountNeeded;
        private final String unit;
        private final double amountAtHome;
        private final boolean isCompletelyMissing;

        public MissingIngredientInfo(String ingredientName, double amountNeeded, String unit, double amountAtHome, boolean isCompletelyMissing) {
            this.ingredientName = ingredientName;
            this.amountNeeded = amountNeeded;
            this.unit = unit;
            this.amountAtHome = amountAtHome;
            this.isCompletelyMissing = isCompletelyMissing;
        }

        public String getIngredientName() {
            return ingredientName;
        }

        public double getAmountNeeded() {
            return amountNeeded;
        }

        public String getUnit() {
            return unit;
        }

        public double getAmountAtHome() {
            return amountAtHome;
        }

        public boolean isCompletelyMissing() {
            return isCompletelyMissing;
        }

        public String getDisplayText() {
            if (isCompletelyMissing) {
                return "Missing: " + formatQty(amountNeeded) + " " + unit + " " + ingredientName;
            } else {
                double diff = amountNeeded - amountAtHome;
                return "Need " + formatQty(diff) + " " + unit + " more " + ingredientName +
                        " (have " + formatQty(amountAtHome) + " " + unit + ")";
            }
        }

        private String formatQty(double qty) {
            if (qty == Math.floor(qty)) {
                return String.valueOf((int) qty);
            }
            return String.format(java.util.Locale.US, "%.1f", qty);
        }
    }
}
