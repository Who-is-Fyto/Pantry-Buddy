package com.example.pantrybuddy.data.repository;

import android.app.Application;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.pantrybuddy.data.local.AppDatabase;
import com.example.pantrybuddy.data.local.dao.PantryDao;
import com.example.pantrybuddy.data.local.dao.RecipeDao;
import com.example.pantrybuddy.data.local.entity.PantryItem;
import com.example.pantrybuddy.domain.engine.AlmostThereMatcher;
import com.example.pantrybuddy.domain.engine.StrictRecipeMatcher;
import com.example.pantrybuddy.domain.model.MatchResult;
import com.example.pantrybuddy.domain.model.RecipeWithIngredients;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

// Repository managing recipes and matching results
public class RecipeRepository {

    public interface OnMatchEvaluatedListener {
        void onMatchEvaluated(MatchResult result);
    }

    private final RecipeDao recipeDao;
    private final PantryDao pantryDao;
    private final ExecutorService executor;

    private final LiveData<List<RecipeWithIngredients>> allRecipes;
    private final LiveData<List<PantryItem>> allPantryItems;

    private final MediatorLiveData<List<MatchResult>> cookableRecipes = new MediatorLiveData<>();
    private final MediatorLiveData<List<MatchResult>> almostThereRecipes = new MediatorLiveData<>();
    private final MediatorLiveData<Boolean> zeroMatchState = new MediatorLiveData<>();

    public RecipeRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        this.recipeDao = db.recipeDao();
        this.pantryDao = db.pantryDao();
        this.executor = AppDatabase.databaseWriteExecutor;
        this.allRecipes = recipeDao.getAllRecipesWithIngredients();
        this.allPantryItems = pantryDao.getAllPantryItems();

        setupMediators();
    }

    // Constructor for testing with mock daos
    public RecipeRepository(RecipeDao recipeDao, PantryDao pantryDao, ExecutorService executor) {
        this.recipeDao = recipeDao;
        this.pantryDao = pantryDao;
        this.executor = executor;
        this.allRecipes = recipeDao.getAllRecipesWithIngredients();
        this.allPantryItems = pantryDao.getAllPantryItems();

        setupMediators();
    }

    private void setupMediators() {
        cookableRecipes.addSource(allRecipes, recipes -> runMatching());
        cookableRecipes.addSource(allPantryItems, pantry -> runMatching());

        almostThereRecipes.addSource(allRecipes, recipes -> runMatching());
        almostThereRecipes.addSource(allPantryItems, pantry -> runMatching());
    }

    public static final long SPOONACULAR_CACHE_MAX_AGE_MS = 60 * 60 * 1000L; // Strict 1-hour caching limit

    // Runs matching algorithm whenever recipes or pantry items change
    public void runMatching() {
        executor.execute(() -> {
            List<RecipeWithIngredients> recipes = recipeDao.getAllRecipesWithIngredientsSync();
            List<PantryItem> pantry = pantryDao.getAllPantryItemsSync();

            if (recipes == null) {
                recipes = Collections.emptyList();
            }
            if (pantry == null) {
                pantry = Collections.emptyList();
            }

            List<MatchResult> cookable = StrictRecipeMatcher.filterStrictlyCookable(recipes, pantry);
            List<MatchResult> almost = AlmostThereMatcher.filterAlmostThere(recipes, pantry);

            cookableRecipes.postValue(cookable);
            almostThereRecipes.postValue(almost);
            zeroMatchState.postValue(cookable.isEmpty());
        });
    }

    public LiveData<List<RecipeWithIngredients>> getAllRecipes() {
        return allRecipes;
    }

    public LiveData<RecipeWithIngredients> getRecipeById(long recipeId) {
        return recipeDao.getRecipeWithIngredientsByIdLive(recipeId);
    }

    public LiveData<List<MatchResult>> getCookableRecipes() {
        return cookableRecipes;
    }

    public LiveData<List<MatchResult>> getAlmostThereRecipes() {
        return almostThereRecipes;
    }

    public LiveData<Boolean> getZeroMatchState() {
        return zeroMatchState;
    }

    public interface OnSyncCompletedListener {
        void onSyncCompleted(int newRecipesCount, @Nullable String errorMessage);
    }

    // Purges Spoonacular cached recipes older than 1 hour in compliance with API terms
    public void purgeExpiredCache() {
        executor.execute(() -> {
            long threshold = System.currentTimeMillis() - SPOONACULAR_CACHE_MAX_AGE_MS;
            recipeDao.purgeExpiredSpoonacularCache(threshold);
            runMatching();
        });
    }

    // Immediately purges all Spoonacular data on user request
    public void clearAllSpoonacularCache(@Nullable Runnable onCleared) {
        executor.execute(() -> {
            recipeDao.deleteAllSpoonacularRecipes();
            runMatching();
            if (onCleared != null) {
                onCleared.run();
            }
        });
    }

    // Fetches matching recipes from Spoonacular based on current pantry items and caches into Room
    public void syncSpoonacularRecipes(@Nullable OnSyncCompletedListener listener) {
        executor.execute(() -> {
            // 1. Enforce 1-hour cache expiration policy first
            long threshold = System.currentTimeMillis() - SPOONACULAR_CACHE_MAX_AGE_MS;
            recipeDao.purgeExpiredSpoonacularCache(threshold);

            List<PantryItem> pantry = pantryDao.getAllPantryItemsSync();
            if (pantry == null || pantry.isEmpty()) {
                if (listener != null) {
                    listener.onSyncCompleted(0, "Pantry is empty");
                }
                return;
            }

            java.util.List<String> ingredients = new java.util.ArrayList<>();
            for (PantryItem item : pantry) {
                if (item.getQuantity() > 0) {
                    ingredients.add(item.getNormalizedName());
                }
            }

            if (ingredients.isEmpty()) {
                if (listener != null) {
                    listener.onSyncCompleted(0, "No available ingredients in pantry");
                }
                return;
            }

            try {
                List<RecipeWithIngredients> remoteRecipes =
                        com.example.pantrybuddy.data.remote.spoonacular.SpoonacularService.getInstance()
                                .searchRecipesByIngredientsSync(ingredients);

                if (remoteRecipes != null && !remoteRecipes.isEmpty()) {
                    int added = 0;
                    for (RecipeWithIngredients rwi : remoteRecipes) {
                        com.example.pantrybuddy.data.local.entity.Recipe recipe = rwi.getRecipe();
                        if (recipe == null || rwi.getIngredients() == null || rwi.getIngredients().isEmpty()) {
                            continue;
                        }

                        com.example.pantrybuddy.data.local.entity.Recipe existing = recipeDao.getRecipeByTitle(recipe.getTitle());
                        long recipeId;
                        if (existing != null) {
                            recipeId = existing.getRecipeId();
                            recipe.setRecipeId(recipeId);
                            recipeDao.insertRecipe(recipe); // Update cachedTimestamp
                        } else {
                            recipeId = recipeDao.insertRecipe(recipe);
                            for (com.example.pantrybuddy.data.local.entity.RecipeIngredient ing : rwi.getIngredients()) {
                                ing.setRecipeId(recipeId);
                            }
                            recipeDao.insertIngredients(rwi.getIngredients());
                            added++;
                        }
                    }

                    // Directly re-evaluate matching against SQLite
                    runMatching();
                    if (listener != null) {
                        listener.onSyncCompleted(added, null);
                    }
                } else {
                    if (listener != null) {
                        listener.onSyncCompleted(0, "No new recipes found for current ingredients");
                    }
                }
            } catch (Exception e) {
                if (listener != null) {
                    listener.onSyncCompleted(0, e.getMessage());
                }
            }
        });
    }

    // Evaluates a single recipe asynchronously with current pantry items
    public void evaluateRecipe(long recipeId, OnMatchEvaluatedListener listener) {
        executor.execute(() -> {
            RecipeWithIngredients recipe = recipeDao.getRecipeWithIngredientsById(recipeId);
            List<PantryItem> pantry = pantryDao.getAllPantryItemsSync();
            MatchResult result = null;
            if (recipe != null && pantry != null) {
                result = StrictRecipeMatcher.evaluate(recipe, pantry);
            }
            if (listener != null) {
                listener.onMatchEvaluated(result);
            }
        });
    }
}
