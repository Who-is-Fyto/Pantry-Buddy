package com.example.pantrybuddy.domain.model;

import androidx.room.Embedded;
import androidx.room.Relation;
import com.example.pantrybuddy.data.local.entity.Recipe;
import com.example.pantrybuddy.data.local.entity.RecipeIngredient;
import java.util.List;

// Combines recipe with its ingredient list
public class RecipeWithIngredients {
    @Embedded
    public Recipe recipe;

    @Relation(
        parentColumn = "recipeId",
        entityColumn = "recipeId"
    )
    public List<RecipeIngredient> ingredients;

    public Recipe getRecipe() {
        return recipe;
    }

    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }

    public List<RecipeIngredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<RecipeIngredient> ingredients) {
        this.ingredients = ingredients;
    }
}
