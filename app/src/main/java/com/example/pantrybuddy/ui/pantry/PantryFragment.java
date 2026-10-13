package com.example.pantrybuddy.ui.pantry;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.pantrybuddy.R;
import com.example.pantrybuddy.data.local.entity.PantryItem;
import com.example.pantrybuddy.data.remote.openfoodfacts.ScannedProduct;
import com.example.pantrybuddy.databinding.FragmentPantryBinding;
import com.example.pantrybuddy.ui.scanner.BarcodeScannerActivity;

// Fragment displaying the reactive pantry inventory list, search, filter chips, and sorting
public class PantryFragment extends Fragment {

    private FragmentPantryBinding binding;
    private PantryViewModel pantryViewModel;
    private PantryAdapter adapter;
    private PantryViewModel.SortOrder currentSort = PantryViewModel.SortOrder.EXPIRY_ASC;

    private final ActivityResultLauncher<Intent> scanBarcodeLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    ScannedProduct product = (ScannedProduct) result.getData()
                            .getSerializableExtra(BarcodeScannerActivity.EXTRA_SCANNED_PRODUCT);
                    if (product != null) {
                        AddIngredientBottomSheet sheet = AddIngredientBottomSheet.newInstance(product);
                        sheet.show(getParentFragmentManager(), "AddIngredientBottomSheet");
                    } else {
                        String barcode = result.getData().getStringExtra(BarcodeScannerActivity.EXTRA_SCANNED_BARCODE);
                        if (barcode != null && !barcode.trim().isEmpty()) {
                            AddIngredientBottomSheet sheet = AddIngredientBottomSheet.newInstance(
                                    new ScannedProduct(barcode, barcode, "", 1.0, "pcs", "Staples", "")
                            );
                            sheet.show(getParentFragmentManager(), "AddIngredientBottomSheet");
                        }
                    }
                }
            });

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
        setupScannerButtons();
        setupFab();
        setupObservers();
    }

    private void setupScannerButtons() {
        View.OnClickListener scanListener = v -> {
            Intent intent = new Intent(requireContext(), BarcodeScannerActivity.class);
            scanBarcodeLauncher.launch(intent);
        };
        binding.btnScanBarcode.setOnClickListener(scanListener);
        binding.btnEmptyScanBarcode.setOnClickListener(scanListener);
    }

    private void setupRecyclerView() {
        adapter = new PantryAdapter(item -> {
            android.content.Intent intent = new android.content.Intent(requireContext(), IngredientDetailActivity.class);
            intent.putExtra(IngredientDetailActivity.EXTRA_ITEM_ID, item.getItemId());
            startActivity(intent);
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

        binding.btnPantryFilterDrawer.setOnClickListener(v -> {
            if (getParentFragmentManager().findFragmentByTag("PantryFilterBottomSheet") == null) {
                PantryFilterBottomSheet bottomSheet = new PantryFilterBottomSheet();
                bottomSheet.show(getParentFragmentManager(), "PantryFilterBottomSheet");
            }
        });
    }

    private void setupCategoryChips() {
        binding.chipGroupCategories.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty() || checkedIds.contains(R.id.chipAll)) {
                pantryViewModel.setCategoryFilter("All");
                pantryViewModel.setUseSoonOnly(false);
            } else if (checkedIds.contains(R.id.chipVegetables)) {
                pantryViewModel.setCategoryFilter("Vegetables");
                pantryViewModel.setUseSoonOnly(false);
            } else if (checkedIds.contains(R.id.chipFruits)) {
                pantryViewModel.setCategoryFilter("Fruits");
                pantryViewModel.setUseSoonOnly(false);
            } else if (checkedIds.contains(R.id.chipDairy)) {
                pantryViewModel.setCategoryFilter("Dairy & Eggs");
                pantryViewModel.setUseSoonOnly(false);
            } else if (checkedIds.contains(R.id.chipMeat)) {
                pantryViewModel.setCategoryFilter("Meat & Fish");
                pantryViewModel.setUseSoonOnly(false);
            } else if (checkedIds.contains(R.id.chipBakery)) {
                pantryViewModel.setCategoryFilter("Bakery & Bread");
                pantryViewModel.setUseSoonOnly(false);
            } else if (checkedIds.contains(R.id.chipStaples)) {
                pantryViewModel.setCategoryFilter("Staples & Grains");
                pantryViewModel.setUseSoonOnly(false);
            } else if (checkedIds.contains(R.id.chipCanned)) {
                pantryViewModel.setCategoryFilter("Canned Goods");
                pantryViewModel.setUseSoonOnly(false);
            } else if (checkedIds.contains(R.id.chipCondiments)) {
                pantryViewModel.setCategoryFilter("Condiments & Sauces");
                pantryViewModel.setUseSoonOnly(false);
            } else if (checkedIds.contains(R.id.chipSpices)) {
                pantryViewModel.setCategoryFilter("Spices & Seasonings");
                pantryViewModel.setUseSoonOnly(false);
            } else if (checkedIds.contains(R.id.chipBeverages)) {
                pantryViewModel.setCategoryFilter("Beverages");
                pantryViewModel.setUseSoonOnly(false);
            } else if (checkedIds.contains(R.id.chipSnacks)) {
                pantryViewModel.setCategoryFilter("Snacks & Sweets");
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
                binding.btnSortOrder.setText("Name A-Z ↑");
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
            if (getParentFragmentManager().findFragmentByTag("AddIngredientBottomSheet") == null) {
                AddIngredientBottomSheet bottomSheet = new AddIngredientBottomSheet();
                bottomSheet.show(getParentFragmentManager(), "AddIngredientBottomSheet");
            }
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
