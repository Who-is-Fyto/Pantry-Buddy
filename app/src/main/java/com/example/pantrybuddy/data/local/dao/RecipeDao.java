package com.example.pantrybuddy.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import com.example.pantrybuddy.data.local.entity.Recipe;
import com.example.pantrybuddy.data.local.entity.RecipeIngredient;
import com.example.pantrybuddy.domain.model.RecipeWithIngredients;
import java.util.List;

@Dao
public interface RecipeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertRecipe(Recipe recipe);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertRecipes(List<Recipe> recipes);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertIngredients(List<RecipeIngredient> ingredients);

    @Transaction
    @Query("SELECT * FROM recipes ORDER BY title ASC")
    LiveData<List<RecipeWithIngredients>> getAllRecipesWithIngredients();

    @Transaction
    @Query("SELECT * FROM recipes ORDER BY title ASC")
    List<RecipeWithIngredients> getAllRecipesWithIngredientsSync();

    @Transaction
    @Query("SELECT * FROM recipes WHERE recipeId = :recipeId LIMIT 1")
    RecipeWithIngredients getRecipeWithIngredientsById(long recipeId);

    @Transaction
    @Query("SELECT * FROM recipes WHERE recipeId = :recipeId LIMIT 1")
    LiveData<RecipeWithIngredients> getRecipeWithIngredientsByIdLive(long recipeId);

    @Query("SELECT COUNT(*) FROM recipes")
    int getRecipeCount();

    @Query("SELECT * FROM recipes WHERE title = :title LIMIT 1")
    Recipe getRecipeByTitle(String title);

    @Query("DELETE FROM recipes")
    void deleteAllRecipes();

    @Query("DELETE FROM recipe_ingredients")
    void deleteAllIngredients();

    @Query("DELETE FROM recipes WHERE source = 'spoonacular' AND cachedTimestamp < :expiryThresholdMs")
    int purgeExpiredSpoonacularCache(long expiryThresholdMs);

    @Query("DELETE FROM recipes WHERE source = 'spoonacular'")
    int deleteAllSpoonacularRecipes();

    @Query("UPDATE recipes SET imageUrl = :imageUrl WHERE title = :title AND (imageUrl IS NULL OR imageUrl = '')")
    void updateRecipeImageIfMissing(String title, String imageUrl);

    @Query("UPDATE recipes SET imageUrl = :imageUrl WHERE title = :title")
    void updateRecipeImage(String title, String imageUrl);
}
