package com.example.pantrybuddy.data.local.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entity representing an individual ingredient requirement for a Recipe.
 */
@Entity(
    tableName = "recipe_ingredients",
    foreignKeys = @ForeignKey(
        entity = Recipe.class,
        parentColumns = "recipeId",
        childColumns = "recipeId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {
        @Index("recipeId"),
        @Index("normalizedName")
    }
)
public class RecipeIngredient {
    @PrimaryKey(autoGenerate = true)
    private long ingredientId;
    private long recipeId;
    private String ingredientName;
    private String normalizedName;
    private double amount; // Amount required for Recipe.defaultServings
    private String unit;   // "g", "ml", "pcs", "tbsp", "tsp"

    public RecipeIngredient(long recipeId, String ingredientName, String normalizedName, double amount, String unit) {
        this.recipeId = recipeId;
        this.ingredientName = ingredientName;
        this.normalizedName = normalizedName;
        this.amount = amount;
        this.unit = unit;
    }

    public long getIngredientId() {
        return ingredientId;
    }

    public void setIngredientId(long ingredientId) {
        this.ingredientId = ingredientId;
    }

    public long getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(long recipeId) {
        this.recipeId = recipeId;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public void setIngredientName(String ingredientName) {
        this.ingredientName = ingredientName;
    }

    public String getNormalizedName() {
        return normalizedName;
    }

    public void setNormalizedName(String normalizedName) {
        this.normalizedName = normalizedName;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
