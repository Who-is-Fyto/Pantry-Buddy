package com.example.pantrybuddy.data.repository;

import android.app.Application;

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

    // Runs matching algorithm whenever recipes or pantry items change
    public void runMatching() {
        executor.execute(() -> {
            List<RecipeWithIngredients> recipes = allRecipes.getValue();
            if (recipes == null) {
                recipes = recipeDao.getAllRecipesWithIngredientsSync();
            }

            List<PantryItem> pantry = allPantryItems.getValue();
            if (pantry == null) {
                pantry = pantryDao.getAllPantryItemsSync();
            }

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
