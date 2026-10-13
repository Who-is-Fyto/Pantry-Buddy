package com.example.pantrybuddy.data.local.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

// Recipe table for storing cook time, servings, instructions, photos, and compliance metadata
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

    // Spoonacular compliance, 1-hour caching, and source attribution fields
    private String source = "local";       // "local" or "spoonacular"
    private String sourceUrl = "";         // Original recipe webpage URL
    private String sourceName = "";        // Original publisher/source name
    private long cachedTimestamp = 0L;     // Unix timestamp when cached (for 1-hour purge)

    public Recipe(String title, String description, int cookTimeMinutes, int defaultServings,
                  String instructionsJson, String imageUrl, String difficulty) {
        this.title = title;
        this.description = description;
        this.cookTimeMinutes = cookTimeMinutes;
        this.defaultServings = defaultServings;
        this.instructionsJson = instructionsJson;
        this.imageUrl = imageUrl;
        this.difficulty = difficulty;
        this.source = "local";
        this.sourceUrl = "";
        this.sourceName = "";
        this.cachedTimestamp = 0L;
    }

    @Ignore
    public Recipe(String title, String description, int cookTimeMinutes, int defaultServings,
                  String instructionsJson, String imageUrl, String difficulty,
                  String source, String sourceUrl, String sourceName, long cachedTimestamp) {
        this.title = title;
        this.description = description;
        this.cookTimeMinutes = cookTimeMinutes;
        this.defaultServings = defaultServings;
        this.instructionsJson = instructionsJson;
        this.imageUrl = imageUrl;
        this.difficulty = difficulty;
        this.source = source != null ? source : "local";
        this.sourceUrl = sourceUrl != null ? sourceUrl : "";
        this.sourceName = sourceName != null ? sourceName : "";
        this.cachedTimestamp = cachedTimestamp;
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

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public long getCachedTimestamp() {
        return cachedTimestamp;
    }

    public void setCachedTimestamp(long cachedTimestamp) {
        this.cachedTimestamp = cachedTimestamp;
    }

    public boolean isFromSpoonacular() {
        return "spoonacular".equalsIgnoreCase(source);
    }
}
