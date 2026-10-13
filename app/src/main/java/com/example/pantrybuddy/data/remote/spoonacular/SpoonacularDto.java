package com.example.pantrybuddy.data.remote.spoonacular;

import com.google.gson.annotations.SerializedName;
import java.util.List;

// Data transfer objects representing Spoonacular API responses
public class SpoonacularDto {

    // Complex search response wrapper
    public static class ComplexSearchResponse {
        @SerializedName("results")
        public List<RecipeItem> results;

        @SerializedName("offset")
        public int offset;

        @SerializedName("number")
        public int number;

        @SerializedName("totalResults")
        public int totalResults;
    }

    // Recipe representation used across search and information endpoints
    public static class RecipeItem {
        @SerializedName("id")
        public long id;

        @SerializedName("title")
        public String title;

        @SerializedName("image")
        public String image;

        @SerializedName("readyInMinutes")
        public int readyInMinutes;

        @SerializedName("servings")
        public int servings;

        @SerializedName("summary")
        public String summary;

        @SerializedName("instructions")
        public String instructions;

        @SerializedName("sourceName")
        public String sourceName;

        @SerializedName("sourceUrl")
        public String sourceUrl;

        @SerializedName("extendedIngredients")
        public List<ExtendedIngredient> extendedIngredients;

        @SerializedName("analyzedInstructions")
        public List<AnalyzedInstruction> analyzedInstructions;

        // findByIngredients summary fields
        @SerializedName("usedIngredientCount")
        public int usedIngredientCount;

        @SerializedName("missedIngredientCount")
        public int missedIngredientCount;

        @SerializedName("missedIngredients")
        public List<IngredientSummary> missedIngredients;

        @SerializedName("usedIngredients")
        public List<IngredientSummary> usedIngredients;
    }

    public static class ExtendedIngredient {
        @SerializedName("id")
        public long id;

        @SerializedName("name")
        public String name;

        @SerializedName("nameClean")
        public String nameClean;

        @SerializedName("original")
        public String original;

        @SerializedName("amount")
        public double amount;

        @SerializedName("unit")
        public String unit;
    }

    public static class IngredientSummary {
        @SerializedName("id")
        public long id;

        @SerializedName("name")
        public String name;

        @SerializedName("amount")
        public double amount;

        @SerializedName("unit")
        public String unit;

        @SerializedName("original")
        public String original;
    }

    public static class AnalyzedInstruction {
        @SerializedName("name")
        public String name;

        @SerializedName("steps")
        public List<Step> steps;
    }

    public static class Step {
        @SerializedName("number")
        public int number;

        @SerializedName("step")
        public String step;
    }
}
