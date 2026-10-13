package com.example.pantrybuddy.ui.pantry;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.pantrybuddy.R;
import com.example.pantrybuddy.data.local.entity.PantryItem;
import com.example.pantrybuddy.databinding.BottomSheetPantryFilterBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

// Modal bottom sheet drawer for advanced pantry filtering by category, expiry urgency, and sort order
public class PantryFilterBottomSheet extends BottomSheetDialogFragment {

    private BottomSheetPantryFilterBinding binding;
    private PantryViewModel pantryViewModel;
    private List<PantryItem> allItems;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetPantryFilterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        pantryViewModel = new ViewModelProvider(requireActivity()).get(PantryViewModel.class);

        observeItems();
        setupListeners();
    }

    private void observeItems() {
        pantryViewModel.getAllPantryItems().observe(getViewLifecycleOwner(), items -> {
            allItems = items;
            updateCountButton();
        });
    }

    private void setupListeners() {
        binding.chipGroupDrawerCategories.setOnCheckedStateChangeListener((group, checkedIds) -> updateCountButton());

        binding.switchDrawerUseSoon.setOnCheckedChangeListener((buttonView, isChecked) -> updateCountButton());

        binding.btnResetFilters.setOnClickListener(v -> {
            binding.chipDrawerCatAll.setChecked(true);
            binding.switchDrawerUseSoon.setChecked(false);
            binding.chipSortExpiry.setChecked(true);
            updateCountButton();
        });

        binding.btnApplyPantryFilters.setOnClickListener(v -> {
            String selectedCategory = getSelectedCategory();
            boolean useSoon = binding.switchDrawerUseSoon.isChecked();
            PantryViewModel.SortOrder sortOrder = binding.chipSortName.isChecked()
                    ? PantryViewModel.SortOrder.NAME_ASC
                    : PantryViewModel.SortOrder.EXPIRY_ASC;

            pantryViewModel.setCategoryFilter(selectedCategory);
            pantryViewModel.setUseSoonOnly(useSoon);
            pantryViewModel.setSortOrder(sortOrder);

            dismiss();
        });
    }

    private String getSelectedCategory() {
        int checkedId = binding.chipGroupDrawerCategories.getCheckedChipId();
        if (checkedId == R.id.chipDrawerCatVegetables) return "Vegetables";
        if (checkedId == R.id.chipDrawerCatFruits) return "Fruits";
        if (checkedId == R.id.chipDrawerCatDairy) return "Dairy & Eggs";
        if (checkedId == R.id.chipDrawerCatMeat) return "Meat & Fish";
        if (checkedId == R.id.chipDrawerCatBakery) return "Bakery & Bread";
        if (checkedId == R.id.chipDrawerCatStaples) return "Staples & Grains";
        if (checkedId == R.id.chipDrawerCatCanned) return "Canned Goods";
        if (checkedId == R.id.chipDrawerCatCondiments) return "Condiments & Sauces";
        if (checkedId == R.id.chipDrawerCatSpices) return "Spices & Seasonings";
        if (checkedId == R.id.chipDrawerCatBeverages) return "Beverages";
        if (checkedId == R.id.chipDrawerCatSnacks) return "Snacks & Sweets";
        return "All";
    }

    private void updateCountButton() {
        if (binding == null) return;
        String category = getSelectedCategory();
        boolean useSoon = binding.switchDrawerUseSoon.isChecked();

        int matchCount = 0;
        if (allItems != null) {
            long now = System.currentTimeMillis();
            long soonThreshold = now + (2L * 86_400_000L);

            for (PantryItem item : allItems) {
                boolean matchesCategory = "All".equalsIgnoreCase(category)
                        || (item.getCategory() != null && (item.getCategory().equalsIgnoreCase(category)
                        || (category.startsWith("Staples") && item.getCategory().startsWith("Staples"))));

                boolean matchesUrgency = !useSoon
                        || (item.getExpiryDate() != null && item.getExpiryDate() <= soonThreshold);

                if (matchesCategory && matchesUrgency) {
                    matchCount++;
                }
            }
        }

        binding.btnApplyPantryFilters.setText(getString(R.string.filter_show_button_format, matchCount));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
