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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.pantrybuddy.R;
import com.example.pantrybuddy.databinding.FragmentRecipesBinding;
import com.example.pantrybuddy.domain.model.MatchResult;

import java.util.ArrayList;
import java.util.List;

// Fragment displaying strictly cookable recipes and quarantined almost there suggestions
public class RecipesFragment extends Fragment {

    private FragmentRecipesBinding binding;
    private RecipeViewModel recipeViewModel;
    private RecipesAdapter adapter;

    private List<MatchResult> currentCookableList = new ArrayList<>();
    private List<MatchResult> currentAlmostThereList = new ArrayList<>();

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

        setupRecyclerView();
        setupSearchInput();
        setupFilterChips();
        setupObservers();
    }

    private void setupRecyclerView() {
        adapter = new RecipesAdapter();
        adapter.setOnRecipeClickListener(matchResult -> {
            if (matchResult.getRecipe() != null) {
                Toast.makeText(requireContext(), matchResult.getRecipe().getTitle(), Toast.LENGTH_SHORT).show();
            }
        });

        binding.rvRecipes.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRecipes.setAdapter(adapter);
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
            } else if (checkedIds.contains(R.id.chipFilterUnder20)) {
                recipeViewModel.setMaxCookTimeFilter(20);
                recipeViewModel.setOnlyExpiringSoonFilter(false);
            } else if (checkedIds.contains(R.id.chipFilterUnder30)) {
                recipeViewModel.setMaxCookTimeFilter(30);
                recipeViewModel.setOnlyExpiringSoonFilter(false);
            } else if (checkedIds.contains(R.id.chipFilterExpiringSoon)) {
                recipeViewModel.setMaxCookTimeFilter(0);
                recipeViewModel.setOnlyExpiringSoonFilter(true);
            }
        });
    }

    private void setupObservers() {
        recipeViewModel.getFilteredCookableRecipes().observe(getViewLifecycleOwner(), cookable -> {
            currentCookableList = (cookable != null) ? cookable : new ArrayList<>();
            updateAdapterFeed();
        });

        recipeViewModel.getFilteredAlmostThereRecipes().observe(getViewLifecycleOwner(), almostThere -> {
            currentAlmostThereList = (almostThere != null) ? almostThere : new ArrayList<>();
            updateAdapterFeed();
        });
    }

    private void updateAdapterFeed() {
        adapter.setData(currentCookableList, currentAlmostThereList);

        boolean isTotallyEmpty = currentCookableList.isEmpty() && currentAlmostThereList.isEmpty();
        binding.layoutEmptyState.setVisibility(isTotallyEmpty ? View.VISIBLE : View.GONE);
        binding.rvRecipes.setVisibility(isTotallyEmpty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
