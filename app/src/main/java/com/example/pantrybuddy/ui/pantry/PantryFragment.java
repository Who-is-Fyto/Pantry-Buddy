package com.example.pantrybuddy.ui.pantry;

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
import com.example.pantrybuddy.data.local.entity.PantryItem;
import com.example.pantrybuddy.databinding.FragmentPantryBinding;

// Fragment displaying the reactive pantry inventory list, search, filter chips, and sorting
public class PantryFragment extends Fragment {

    private FragmentPantryBinding binding;
    private PantryViewModel pantryViewModel;
    private PantryAdapter adapter;
    private PantryViewModel.SortOrder currentSort = PantryViewModel.SortOrder.EXPIRY_ASC;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPantryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        pantryViewModel = new ViewModelProvider(requireActivity()).get(PantryViewModel.class);

        setupRecyclerView();
        setupSearchInput();
        setupCategoryChips();
        setupSortSelector();
        setupFab();
        setupObservers();
    }

    private void setupRecyclerView() {
        adapter = new PantryAdapter(item -> {
            // Item click: placeholder until Phase 10 Edit/Detail flow
            Toast.makeText(requireContext(), item.getName() + ": " + item.getQuantity() + " " + item.getUnit(), Toast.LENGTH_SHORT).show();
        });

        binding.rvPantryItems.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvPantryItems.setAdapter(adapter);
    }

    private void setupSearchInput() {
        binding.etPantrySearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = (s != null) ? s.toString() : "";
                pantryViewModel.setSearchQuery(query);
                binding.btnClearSearch.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.btnClearSearch.setOnClickListener(v -> binding.etPantrySearch.setText(""));
    }

    private void setupCategoryChips() {
        binding.chipGroupCategories.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty() || checkedIds.contains(R.id.chipAll)) {
                pantryViewModel.setCategoryFilter("All");
                pantryViewModel.setUseSoonOnly(false);
            } else if (checkedIds.contains(R.id.chipVegetables)) {
                pantryViewModel.setCategoryFilter("Vegetables");
                pantryViewModel.setUseSoonOnly(false);
            } else if (checkedIds.contains(R.id.chipDairy)) {
                pantryViewModel.setCategoryFilter("Dairy & Eggs");
                pantryViewModel.setUseSoonOnly(false);
            } else if (checkedIds.contains(R.id.chipStaples)) {
                pantryViewModel.setCategoryFilter("Staples");
                pantryViewModel.setUseSoonOnly(false);
            } else if (checkedIds.contains(R.id.chipSpices)) {
                pantryViewModel.setCategoryFilter("Spices & Seasonings");
                pantryViewModel.setUseSoonOnly(false);
            } else if (checkedIds.contains(R.id.chipMeat)) {
                pantryViewModel.setCategoryFilter("Meat & Fish");
                pantryViewModel.setUseSoonOnly(false);
            } else if (checkedIds.contains(R.id.chipUseSoon)) {
                pantryViewModel.setCategoryFilter("All");
                pantryViewModel.setUseSoonOnly(true);
            }
        });
    }

    private void setupSortSelector() {
        updateSortButtonText();
        binding.btnSortOrder.setOnClickListener(v -> {
            if (currentSort == PantryViewModel.SortOrder.EXPIRY_ASC) {
                currentSort = PantryViewModel.SortOrder.NAME_ASC;
            } else if (currentSort == PantryViewModel.SortOrder.NAME_ASC) {
                currentSort = PantryViewModel.SortOrder.DATE_ADDED_DESC;
            } else {
                currentSort = PantryViewModel.SortOrder.EXPIRY_ASC;
            }
            pantryViewModel.setSortOrder(currentSort);
            updateSortButtonText();
        });
    }

    private void updateSortButtonText() {
        switch (currentSort) {
            case NAME_ASC:
                binding.btnSortOrder.setText("Name A–Z ↑");
                break;
            case DATE_ADDED_DESC:
                binding.btnSortOrder.setText("Recently added ↓");
                break;
            case EXPIRY_ASC:
            default:
                binding.btnSortOrder.setText("Soonest expiry ↓");
                break;
        }
    }

    private void setupFab() {
        binding.fabAddIngredient.setOnClickListener(v -> {
            // Launches Add Ingredient flow (Phase 10)
            Toast.makeText(requireContext(), "Add Ingredient will be added in Phase 10", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupObservers() {
        pantryViewModel.getFilteredPantryItems().observe(getViewLifecycleOwner(), items -> {
            adapter.submitList(items);

            int count = (items != null) ? items.size() : 0;
            binding.tvFilteredCount.setText(getResources().getQuantityString(
                    R.plurals.pantry_ingredients_count, count, count
            ));

            boolean isEmpty = (items == null || items.isEmpty());
            binding.layoutEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            binding.rvPantryItems.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
