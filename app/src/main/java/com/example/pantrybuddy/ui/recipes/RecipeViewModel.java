package com.example.pantrybuddy.ui.recipes;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.pantrybuddy.data.repository.RecipeRepository;
import com.example.pantrybuddy.domain.model.MatchResult;
import com.example.pantrybuddy.domain.model.RecipeWithIngredients;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// ViewModel managing strictly cookable recipes and almost there suggestions
public class RecipeViewModel extends AndroidViewModel {

    private final RecipeRepository repository;

    private final LiveData<List<MatchResult>> cookableRecipes;
    private final LiveData<List<MatchResult>> almostThereRecipes;
    private final LiveData<Boolean> zeroMatchState;
    private final LiveData<List<RecipeWithIngredients>> allRecipes;

    private final MutableLiveData<String> recipeSearchQuery = new MutableLiveData<>("");
    private final MutableLiveData<Integer> maxCookTimeFilter = new MutableLiveData<>(0); // 0 means no time limit
    private final MutableLiveData<Boolean> onlyExpiringSoonFilter = new MutableLiveData<>(false);

    private final MediatorLiveData<List<MatchResult>> filteredCookableRecipes = new MediatorLiveData<>();
    private final MediatorLiveData<List<MatchResult>> filteredAlmostThereRecipes = new MediatorLiveData<>();

    public RecipeViewModel(@NonNull Application application) {
        this(application, new RecipeRepository(application));
    }

    // Testing constructor
    public RecipeViewModel(@NonNull Application application, RecipeRepository repository) {
        super(application);
        this.repository = repository;
        this.cookableRecipes = repository.getCookableRecipes();
        this.almostThereRecipes = repository.getAlmostThereRecipes();
        this.zeroMatchState = repository.getZeroMatchState();
        this.allRecipes = repository.getAllRecipes();

        setupFilters();
    }

    private void setupFilters() {
        filteredCookableRecipes.addSource(cookableRecipes, list -> applyCookableFilters());
        filteredCookableRecipes.addSource(recipeSearchQuery, query -> applyCookableFilters());
        filteredCookableRecipes.addSource(maxCookTimeFilter, time -> applyCookableFilters());
        filteredCookableRecipes.addSource(onlyExpiringSoonFilter, flag -> applyCookableFilters());

        filteredAlmostThereRecipes.addSource(almostThereRecipes, list -> applyAlmostThereFilters());
        filteredAlmostThereRecipes.addSource(recipeSearchQuery, query -> applyAlmostThereFilters());
        filteredAlmostThereRecipes.addSource(maxCookTimeFilter, time -> applyAlmostThereFilters());
    }

    private void applyCookableFilters() {
        List<MatchResult> raw = cookableRecipes.getValue();
        if (raw == null) {
            filteredCookableRecipes.setValue(Collections.emptyList());
            return;
        }

        String query = recipeSearchQuery.getValue();
        Integer maxTime = maxCookTimeFilter.getValue();
        Boolean expiringOnly = onlyExpiringSoonFilter.getValue();

        List<MatchResult> result = new ArrayList<>();
        for (MatchResult mr : raw) {
            if (query != null && !query.trim().isEmpty()) {
                String title = mr.getRecipe().getTitle();
                if (title == null || !title.toLowerCase().contains(query.trim().toLowerCase())) {
                    continue;
                }
            }

            if (maxTime != null && maxTime > 0) {
                if (mr.getRecipe().getCookTimeMinutes() > maxTime) {
                    continue;
                }
            }

            if (Boolean.TRUE.equals(expiringOnly)) {
                if (!mr.isUsesExpiringSoonItem()) {
                    continue;
                }
            }

            result.add(mr);
        }

        filteredCookableRecipes.setValue(result);
    }

    private void applyAlmostThereFilters() {
        List<MatchResult> raw = almostThereRecipes.getValue();
        if (raw == null) {
            filteredAlmostThereRecipes.setValue(Collections.emptyList());
            return;
        }

        String query = recipeSearchQuery.getValue();
        Integer maxTime = maxCookTimeFilter.getValue();

        List<MatchResult> result = new ArrayList<>();
        for (MatchResult mr : raw) {
            if (query != null && !query.trim().isEmpty()) {
                String title = mr.getRecipe().getTitle();
                if (title == null || !title.toLowerCase().contains(query.trim().toLowerCase())) {
                    continue;
                }
            }

            if (maxTime != null && maxTime > 0) {
                if (mr.getRecipe().getCookTimeMinutes() > maxTime) {
                    continue;
                }
            }

            result.add(mr);
        }

        filteredAlmostThereRecipes.setValue(result);
    }

    public LiveData<List<MatchResult>> getCookableRecipes() {
        return cookableRecipes;
    }

    public LiveData<List<MatchResult>> getFilteredCookableRecipes() {
        return filteredCookableRecipes;
    }

    public LiveData<List<MatchResult>> getAlmostThereRecipes() {
        return almostThereRecipes;
    }

    public LiveData<List<MatchResult>> getFilteredAlmostThereRecipes() {
        return filteredAlmostThereRecipes;
    }

    public void setOnlyExpiringSoonFilter(boolean onlyExpiringSoon) {
        onlyExpiringSoonFilter.setValue(onlyExpiringSoon);
    }

    public LiveData<Boolean> getZeroMatchState() {
        return zeroMatchState;
    }

    public LiveData<List<RecipeWithIngredients>> getAllRecipes() {
        return allRecipes;
    }

    public LiveData<RecipeWithIngredients> getRecipeById(long recipeId) {
        return repository.getRecipeById(recipeId);
    }

    public void setRecipeSearchQuery(String query) {
        recipeSearchQuery.setValue(query);
    }

    public void setMaxCookTimeFilter(int minutes) {
        maxCookTimeFilter.setValue(minutes);
    }

    public void refreshMatches() {
        repository.runMatching();
    }

    public void evaluateRecipe(long recipeId, RecipeRepository.OnMatchEvaluatedListener listener) {
        repository.evaluateRecipe(recipeId, listener);
    }
}
