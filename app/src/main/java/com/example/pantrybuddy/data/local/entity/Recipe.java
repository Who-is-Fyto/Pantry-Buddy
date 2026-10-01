package com.example.pantrybuddy.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entity representing a recipe header in the database.
 */
@Entity(tableName = "recipes")
public class Recipe {
    @PrimaryKey(autoGenerate = true)
    private long recipeId;
    private String title;
    private String description;
    private int cookTimeMinutes;
    private int defaultServings;
    private String instructionsJson; // JSON array of step strings
    private String imageUrl;
    private String difficulty;       // "Easy", "Medium", "Hard"

    public Recipe(String title, String description, int cookTimeMinutes, int defaultServings, String instructionsJson, String imageUrl, String difficulty) {
        this.title = title;
        this.description = description;
        this.cookTimeMinutes = cookTimeMinutes;
        this.defaultServings = defaultServings;
        this.instructionsJson = instructionsJson;
        this.imageUrl = imageUrl;
        this.difficulty = difficulty;
    }

    public long getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(long recipeId) {
        this.recipeId = recipeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCookTimeMinutes() {
        return cookTimeMinutes;
    }

    public void setCookTimeMinutes(int cookTimeMinutes) {
        this.cookTimeMinutes = cookTimeMinutes;
    }

    public int getDefaultServings() {
        return defaultServings;
    }

    public void setDefaultServings(int defaultServings) {
        this.defaultServings = defaultServings;
    }

    public String getInstructionsJson() {
        return instructionsJson;
    }

    public void setInstructionsJson(String instructionsJson) {
        this.instructionsJson = instructionsJson;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }
}
