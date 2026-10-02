package com.example.pantrybuddy.data.local;

import java.util.List;

// Model for reading recipe seed JSON file
public class RecipeSeedDto {
    public String title;
    public String description;
    public int cookTimeMinutes;
    public int defaultServings;
    public String difficulty;
    public String imageUrl;
    public List<String> instructions;
    public List<IngredientSeedDto> ingredients;

    public static class IngredientSeedDto {
        public String name;
        public String normalizedName;
        public double amount;
        public String unit;
    }
}
