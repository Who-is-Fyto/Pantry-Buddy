package com.example.pantrybuddy.ui.recipes;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.pantrybuddy.R;
import com.example.pantrybuddy.data.local.entity.PantryItem;
import com.example.pantrybuddy.databinding.FragmentRecipesBinding;
import com.example.pantrybuddy.domain.engine.PantryDiagnostics;
import com.example.pantrybuddy.domain.model.MatchResult;
import com.example.pantrybuddy.domain.model.RecipeWithIngredients;
import com.example.pantrybuddy.ui.pantry.AddIngredientBottomSheet;
import com.example.pantrybuddy.ui.pantry.PantryViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Fragment displaying strictly cookable recipes, quarantined almost there suggestions,
// and the diagnostic zero-match feedback state when no complete meals can be made
public class RecipesFragment extends Fragment {

    private FragmentRecipesBinding binding;
    private RecipeViewModel recipeViewModel;
    private PantryViewModel pantryViewModel;

    private RecipesAdapter adapter;
    private RecipesAdapter zeroAlmostAdapter;

    private List<MatchResult> rawCookableList = new ArrayList<>();
    private List<MatchResult> currentCookableList = new ArrayList<>();
    private List<MatchResult> currentAlmostThereList = new ArrayList<>();
    private List<PantryItem> currentPantryItems = new ArrayList<>();
    private List<RecipeWithIngredients> allRecipesList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRecipesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recipeViewModel = new ViewModelProvider(requireActivity()).get(RecipeViewModel.class);
        pantryViewModel = new ViewModelProvider(requireActivity()).get(PantryViewModel.class);

        setupRecyclerView();
        setupZeroMatchView();
        setupSearchInput();
        setupFilterChips();
        setupObservers();
    }

    private void setupRecyclerView() {
        adapter = new RecipesAdapter();
        adapter.setOnRecipeClickListener(matchResult -> {
            if (matchResult.getRecipe() != null) {
                android.content.Intent intent = new android.content.Intent(requireContext(), RecipeDetailActivity.class);
                intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, matchResult.getRecipe().getRecipeId());
                startActivity(intent);
            }
        });

        binding.rvRecipes.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRecipes.setAdapter(adapter);
    }

    private void setupZeroMatchView() {
        zeroAlmostAdapter = new RecipesAdapter();
        zeroAlmostAdapter.setOnRecipeClickListener(matchResult -> {
            if (matchResult.getRecipe() != null) {
                android.content.Intent intent = new android.content.Intent(requireContext(), RecipeDetailActivity.class);
                intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, matchResult.getRecipe().getRecipeId());
                startActivity(intent);
            }
        });

        binding.viewZeroMatches.rvZeroAlmostThere.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.viewZeroMatches.rvZeroAlmostThere.setAdapter(zeroAlmostAdapter);

        binding.viewZeroMatches.btnAddIngredientZero.setOnClickListener(v -> {
            if (getParentFragmentManager().findFragmentByTag("AddIngredientBottomSheet") == null) {
                AddIngredientBottomSheet sheet = new AddIngredientBottomSheet();
                sheet.show(getParentFragmentManager(), "AddIngredientBottomSheet");
            }
        });

        binding.viewZeroMatches.btnCheckPantryZero.setOnClickListener(v ->
                androidx.navigation.fragment.NavHostFragment.findNavController(this).navigate(R.id.navigation_pantry));

        binding.viewZeroMatches.btnDiscoverOnlineZero.setOnClickListener(v ->
                recipeViewModel.syncSpoonacularRecipes());

        binding.viewZeroMatches.btnZeroShowMissing1or2.setOnClickListener(v ->
                binding.chipFilterMissing1or2.setChecked(true));

        binding.btnSyncSpoonacular.setOnClickListener(v ->
                recipeViewModel.syncSpoonacularRecipes());
    }

    private void setupSearchInput() {
        binding.etRecipeSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = (s != null) ? s.toString() : "";
                recipeViewModel.setRecipeSearchQuery(query);
                binding.btnClearRecipeSearch.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.btnClearRecipeSearch.setOnClickListener(v -> binding.etRecipeSearch.setText(""));
    }

    private void setupFilterChips() {
        binding.chipGroupRecipeFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty() || checkedIds.contains(R.id.chipFilterAll)) {
                recipeViewModel.setMaxCookTimeFilter(0);
                recipeViewModel.setOnlyExpiringSoonFilter(false);
                recipeViewModel.setOnlyMissing1or2Filter(false);
            } else if (checkedIds.contains(R.id.chipFilterMissing1or2)) {
                recipeViewModel.setMaxCookTimeFilter(0);
                recipeViewModel.setOnlyExpiringSoonFilter(false);
                recipeViewModel.setOnlyMissing1or2Filter(true);
            } else if (checkedIds.contains(R.id.chipFilterUnder20)) {
                recipeViewModel.setMaxCookTimeFilter(20);
                recipeViewModel.setOnlyExpiringSoonFilter(false);
                recipeViewModel.setOnlyMissing1or2Filter(false);
            } else if (checkedIds.contains(R.id.chipFilterUnder30)) {
                recipeViewModel.setMaxCookTimeFilter(30);
                recipeViewModel.setOnlyExpiringSoonFilter(false);
                recipeViewModel.setOnlyMissing1or2Filter(false);
            } else if (checkedIds.contains(R.id.chipFilterExpiringSoon)) {
                recipeViewModel.setMaxCookTimeFilter(0);
                recipeViewModel.setOnlyExpiringSoonFilter(true);
                recipeViewModel.setOnlyMissing1or2Filter(false);
            }
        });
    }

    private void setupObservers() {
        // Raw cookable list (unfiltered) to determine zero-match state
        recipeViewModel.getCookableRecipes().observe(getViewLifecycleOwner(), cookable -> {
            rawCookableList = (cookable != null) ? cookable : new ArrayList<>();
            updateAdapterFeed();
        });

        // Filtered cookable recipes (for search and chips)
        recipeViewModel.getFilteredCookableRecipes().observe(getViewLifecycleOwner(), filteredCookable -> {
            currentCookableList = (filteredCookable != null) ? filteredCookable : new ArrayList<>();
            updateAdapterFeed();
        });

        // Filtered almost there recipes
        recipeViewModel.getFilteredAlmostThereRecipes().observe(getViewLifecycleOwner(), almostThere -> {
            currentAlmostThereList = (almostThere != null) ? almostThere : new ArrayList<>();
            updateAdapterFeed();
        });

        // Pantry items for stock audit and diagnostics
        pantryViewModel.getAllPantryItems().observe(getViewLifecycleOwner(), items -> {
            currentPantryItems = (items != null) ? items : new ArrayList<>();
            updateAdapterFeed();
        });

        // All recipes for unlocking hint calculation
        recipeViewModel.getAllRecipes().observe(getViewLifecycleOwner(), recipes -> {
            allRecipesList = (recipes != null) ? recipes : new ArrayList<>();
            updateAdapterFeed();
        });

        // Spoonacular sync progress observer
        recipeViewModel.getIsSyncingRecipes().observe(getViewLifecycleOwner(), syncing -> {
            boolean isSyncing = Boolean.TRUE.equals(syncing);
            binding.progressSyncRecipes.setVisibility(isSyncing ? View.VISIBLE : View.GONE);
            binding.btnSyncSpoonacular.setVisibility(isSyncing ? View.GONE : View.VISIBLE);
            binding.viewZeroMatches.btnDiscoverOnlineZero.setEnabled(!isSyncing);
        });

        // Spoonacular sync status feedback message
        recipeViewModel.getSyncStatusMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });

        // Observer for missing 1-2 items filter
        recipeViewModel.getOnlyMissing1or2Filter().observe(getViewLifecycleOwner(), filterActive -> {
            updateAdapterFeed();
        });
    }

    private void updateAdapterFeed() {
        boolean showMissingOnly = Boolean.TRUE.equals(recipeViewModel.getOnlyMissing1or2Filter().getValue());
        boolean hasCookable = !rawCookableList.isEmpty();

        if (hasCookable || showMissingOnly) {
            // Cookable recipes exist in the pantry OR user explicitly chose to see missing 1-2 items
            binding.layoutRecipesHeader.setVisibility(View.VISIBLE);
            binding.layoutSearchAndFilters.setVisibility(View.VISIBLE);
            binding.viewZeroMatches.getRoot().setVisibility(View.GONE);

            List<MatchResult> displayCookable = showMissingOnly ? Collections.emptyList() : currentCookableList;
            List<MatchResult> displayAlmost = currentAlmostThereList;

            boolean isFilterEmpty = displayCookable.isEmpty() && displayAlmost.isEmpty();
            if (isFilterEmpty) {
                binding.layoutEmptyState.setVisibility(View.VISIBLE);
                binding.rvRecipes.setVisibility(View.GONE);
            } else {
                binding.layoutEmptyState.setVisibility(View.GONE);
                binding.rvRecipes.setVisibility(View.VISIBLE);
                adapter.setData(displayCookable, displayAlmost, showMissingOnly);
            }
        } else {
            // Zero-Match Feedback State (Figma Panel 10)
            binding.layoutRecipesHeader.setVisibility(View.GONE);
            binding.layoutSearchAndFilters.setVisibility(View.GONE);
            binding.rvRecipes.setVisibility(View.GONE);
            binding.layoutEmptyState.setVisibility(View.GONE);
            binding.viewZeroMatches.getRoot().setVisibility(View.VISIBLE);

            // Populate active stock summary
            String stockSummary = PantryDiagnostics.formatActiveStockSummary(currentPantryItems);
            binding.viewZeroMatches.tvPantryStockList.setText(stockSummary);
            int count = currentPantryItems.size();
            binding.viewZeroMatches.tvPantryStockCount.setText(
                    getResources().getQuantityString(R.plurals.pantry_ingredients_count, count, count)
            );

            // Populate unlocking hint
            String hint = PantryDiagnostics.generateUnlockingRecommendation(
                    allRecipesList, currentPantryItems, currentAlmostThereList
            );
            binding.viewZeroMatches.tvUnlockingHint.setText(hint);

            // Show almost-there recipes if available
            if (!currentAlmostThereList.isEmpty()) {
                binding.viewZeroMatches.btnZeroShowMissing1or2.setVisibility(View.VISIBLE);
                binding.viewZeroMatches.layoutZeroAlmostThere.setVisibility(View.VISIBLE);
                zeroAlmostAdapter.setAlmostThereOnly(currentAlmostThereList);
            } else {
                binding.viewZeroMatches.btnZeroShowMissing1or2.setVisibility(View.GONE);
                binding.viewZeroMatches.layoutZeroAlmostThere.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
