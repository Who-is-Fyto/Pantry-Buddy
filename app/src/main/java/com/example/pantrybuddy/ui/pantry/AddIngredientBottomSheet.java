package com.example.pantrybuddy.ui.pantry;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.pantrybuddy.R;
import com.example.pantrybuddy.data.local.entity.PantryItem;
import com.example.pantrybuddy.data.remote.openfoodfacts.ScannedProduct;
import com.example.pantrybuddy.databinding.BottomSheetAddIngredientBinding;
import com.example.pantrybuddy.domain.engine.IngredientNormalizer;
import com.example.pantrybuddy.ui.scanner.BarcodeScannerActivity;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

// Bottom sheet dialog for adding a new ingredient with quantity, unit, category and expiry
public class AddIngredientBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_PREFILL_NAME = "arg_prefill_name";
    private static final String ARG_PREFILL_QTY = "arg_prefill_qty";
    private static final String ARG_PREFILL_UNIT = "arg_prefill_unit";
    private static final String ARG_PREFILL_CATEGORY = "arg_prefill_category";

    private BottomSheetAddIngredientBinding binding;
    private PantryViewModel pantryViewModel;
    private Long selectedExpiryDateMs = null;
    private boolean isSubmitting = false;

    private static final String[] COMMON_INGREDIENTS = {
            "Eggs", "Tomatoes", "Spinach", "Olive Oil", "Cooking Oil", "Table Salt",
            "Black Pepper", "Potatoes", "Onions", "Garlic", "White Rice", "Milk",
            "Cheddar Cheese", "Butter", "Bread", "Spaghetti", "Egg Noodles", "Carrots",
            "Mushrooms", "Chickpeas", "White Beans", "Canned Tuna", "Sweetcorn",
            "Mayonnaise", "Lemon Juice", "Soy Sauce", "Curry Powder", "Cinnamon",
            "Sugar", "Cumin", "Chili Flakes", "Rosemary"
    };

    private final ActivityResultLauncher<Intent> scanBarcodeLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    ScannedProduct product = (ScannedProduct) result.getData()
                            .getSerializableExtra(BarcodeScannerActivity.EXTRA_SCANNED_PRODUCT);
                    if (product != null) {
                        populateProduct(product);
                        Toast.makeText(requireContext(), "Scanned: " + product.getName(), Toast.LENGTH_SHORT).show();
                    } else {
                        String barcode = result.getData().getStringExtra(BarcodeScannerActivity.EXTRA_SCANNED_BARCODE);
                        if (barcode != null && !barcode.trim().isEmpty()) {
                            binding.actvIngredientName.setText(barcode);
                            binding.actvIngredientName.requestFocus();
                        }
                    }
                }
            });

    @NonNull
    public static AddIngredientBottomSheet newInstance() {
        return new AddIngredientBottomSheet();
    }

    @NonNull
    public static AddIngredientBottomSheet newInstance(@Nullable ScannedProduct product) {
        AddIngredientBottomSheet sheet = new AddIngredientBottomSheet();
        if (product != null) {
            Bundle args = new Bundle();
            args.putString(ARG_PREFILL_NAME, product.getDisplayTitle());
            args.putDouble(ARG_PREFILL_QTY, product.getQuantity());
            args.putString(ARG_PREFILL_UNIT, product.getUnit());
            args.putString(ARG_PREFILL_CATEGORY, product.getCategory());
            sheet.setArguments(args);
        }
        return sheet;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog bsd = (BottomSheetDialog) dialogInterface;
            FrameLayout bottomSheet = bsd.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
            }
        });
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetAddIngredientBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        pantryViewModel = new ViewModelProvider(requireActivity()).get(PantryViewModel.class);

        setupAutocomplete();
        setupDatePicker();
        setupScanButton();
        setupSubmitButton();
        checkAndApplyArguments();
    }

    private void setupScanButton() {
        binding.btnScanBarcodeInSheet.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), BarcodeScannerActivity.class);
            scanBarcodeLauncher.launch(intent);
        });
    }

    private void checkAndApplyArguments() {
        Bundle args = getArguments();
        if (args != null && args.containsKey(ARG_PREFILL_NAME)) {
            String name = args.getString(ARG_PREFILL_NAME, "");
            double qty = args.getDouble(ARG_PREFILL_QTY, 1.0);
            String unit = args.getString(ARG_PREFILL_UNIT, "pcs");
            String category = args.getString(ARG_PREFILL_CATEGORY, "Staples");

            populateFields(name, qty, unit, category);
        }
    }

    public void populateProduct(@NonNull ScannedProduct product) {
        populateFields(product.getDisplayTitle(), product.getQuantity(), product.getUnit(), product.getCategory());
    }

    public void populateFields(@NonNull String name, double quantity, @NonNull String unit, @NonNull String category) {
        if (binding == null) return;

        binding.actvIngredientName.setText(name);

        if (quantity == Math.floor(quantity)) {
            binding.etIngredientQuantity.setText(String.format(Locale.US, "%.0f", quantity));
        } else {
            binding.etIngredientQuantity.setText(String.format(Locale.US, "%.1f", quantity));
        }

        selectUnitChip(unit);
        selectCategoryChip(category);
    }

    private void selectUnitChip(@NonNull String unit) {
        String u = unit.toLowerCase(Locale.US);
        if (u.equals("g")) binding.chipUnitG.setChecked(true);
        else if (u.equals("kg")) binding.chipUnitKg.setChecked(true);
        else if (u.equals("ml")) binding.chipUnitMl.setChecked(true);
        else if (u.equals("l")) binding.chipUnitL.setChecked(true);
        else if (u.equals("tbsp")) binding.chipUnitTbsp.setChecked(true);
        else if (u.equals("tsp")) binding.chipUnitTsp.setChecked(true);
        else binding.chipUnitPcs.setChecked(true);
    }

    private void selectCategoryChip(@NonNull String category) {
        if (category.equalsIgnoreCase("Dairy & Eggs")) binding.chipCatDairy.setChecked(true);
        else if (category.equalsIgnoreCase("Staples")) binding.chipCatStaples.setChecked(true);
        else if (category.equalsIgnoreCase("Spices & Seasonings")) binding.chipCatSpices.setChecked(true);
        else if (category.equalsIgnoreCase("Meat & Fish")) binding.chipCatMeat.setChecked(true);
        else binding.chipCatVegetables.setChecked(true);
    }

    private void setupAutocomplete() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                COMMON_INGREDIENTS
        );
        binding.actvIngredientName.setAdapter(adapter);

        binding.actvIngredientName.setOnItemClickListener((parent, view, position, id) -> {
            String selected = (String) parent.getItemAtPosition(position);
            suggestCategoryAndUnit(selected);
        });
    }

    // Auto-selects intuitive category and unit when an ingredient is picked
    private void suggestCategoryAndUnit(String name) {
        String lower = name.toLowerCase(Locale.US);
        if (lower.contains("oil") || lower.contains("rice") || lower.contains("bread") || lower.contains("bean") || lower.contains("chickpea") || lower.contains("noodle") || lower.contains("spaghetti")) {
            binding.chipCatStaples.setChecked(true);
            if (lower.contains("oil")) binding.chipUnitMl.setChecked(true);
            else if (lower.contains("rice") || lower.contains("bean") || lower.contains("chickpea")) binding.chipUnitG.setChecked(true);
            else if (lower.contains("bread")) binding.chipUnitPcs.setChecked(true);
        } else if (lower.contains("egg") || lower.contains("milk") || lower.contains("cheese") || lower.contains("butter")) {
            binding.chipCatDairy.setChecked(true);
            if (lower.contains("egg")) binding.chipUnitPcs.setChecked(true);
            else if (lower.contains("milk")) binding.chipUnitMl.setChecked(true);
            else binding.chipUnitG.setChecked(true);
        } else if (lower.contains("salt") || lower.contains("pepper") || lower.contains("cumin") || lower.contains("cinnamon") || lower.contains("curry") || lower.contains("chili") || lower.contains("rosemary")) {
            binding.chipCatSpices.setChecked(true);
            binding.chipUnitG.setChecked(true);
        } else if (lower.contains("tuna") || lower.contains("chicken") || lower.contains("beef") || lower.contains("fish")) {
            binding.chipCatMeat.setChecked(true);
            binding.chipUnitG.setChecked(true);
        } else {
            binding.chipCatVegetables.setChecked(true);
            if (lower.contains("spinach")) binding.chipUnitG.setChecked(true);
            else binding.chipUnitPcs.setChecked(true);
        }
    }

    private void setupDatePicker() {
        binding.layoutExpiryDatePicker.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            if (selectedExpiryDateMs != null) {
                cal.setTimeInMillis(selectedExpiryDateMs);
            }
            DatePickerDialog dialog = new DatePickerDialog(
                    requireContext(),
                    (picker, year, month, dayOfMonth) -> {
                        Calendar chosen = Calendar.getInstance();
                        chosen.set(year, month, dayOfMonth, 23, 59, 59);
                        selectedExpiryDateMs = chosen.getTimeInMillis();

                        SimpleDateFormat sdf = new SimpleDateFormat("d MMMM yyyy", Locale.US);
                        binding.tvAddSelectedExpiry.setText(sdf.format(chosen.getTime()));
                        binding.tvAddSelectedExpiry.setTextColor(
                                ContextCompat.getColor(requireContext(), R.color.colorTextPrimary)
                        );
                        binding.btnClearSelectedExpiry.setVisibility(View.VISIBLE);
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
            );
            dialog.show();
        });

        binding.btnClearSelectedExpiry.setOnClickListener(v -> {
            selectedExpiryDateMs = null;
            binding.tvAddSelectedExpiry.setText("No expiration date set");
            binding.tvAddSelectedExpiry.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.colorTextSecondary)
            );
            binding.btnClearSelectedExpiry.setVisibility(View.GONE);
        });
    }

    private void setupSubmitButton() {
        binding.btnAddIngredientSubmit.setOnClickListener(v -> {
            if (isSubmitting) return;

            String name = binding.actvIngredientName.getText().toString().trim();
            if (name.isEmpty()) {
                binding.actvIngredientName.setError("Please enter ingredient name");
                binding.actvIngredientName.requestFocus();
                return;
            }

            String qtyStr = binding.etIngredientQuantity.getText().toString().trim();
            double quantity;
            try {
                quantity = Double.parseDouble(qtyStr);
                if (quantity <= 0.0) {
                    throw new NumberFormatException();
                }
            } catch (Exception e) {
                binding.etIngredientQuantity.setError("Enter quantity greater than 0");
                binding.etIngredientQuantity.requestFocus();
                return;
            }

            isSubmitting = true;
            binding.btnAddIngredientSubmit.setEnabled(false);

            String unit = getSelectedUnit();
            String category = getSelectedCategory();
            String normalizedName = IngredientNormalizer.normalize(name);

            PantryItem item = new PantryItem(
                    name,
                    normalizedName,
                    quantity,
                    unit,
                    selectedExpiryDateMs,
                    category,
                    System.currentTimeMillis()
            );

            pantryViewModel.insert(item);
            Toast.makeText(requireContext(), "Added " + name + " to pantry", Toast.LENGTH_SHORT).show();
            dismiss();
        });
    }

    private String getSelectedUnit() {
        int checkedId = binding.chipGroupAddUnit.getCheckedChipId();
        if (checkedId == R.id.chipUnitG) return "g";
        if (checkedId == R.id.chipUnitKg) return "kg";
        if (checkedId == R.id.chipUnitMl) return "ml";
        if (checkedId == R.id.chipUnitL) return "l";
        if (checkedId == R.id.chipUnitTbsp) return "tbsp";
        if (checkedId == R.id.chipUnitTsp) return "tsp";
        return "pcs";
    }

    private String getSelectedCategory() {
        int checkedId = binding.chipGroupAddCategory.getCheckedChipId();
        if (checkedId == R.id.chipCatDairy) return "Dairy & Eggs";
        if (checkedId == R.id.chipCatStaples) return "Staples";
        if (checkedId == R.id.chipCatSpices) return "Spices & Seasonings";
        if (checkedId == R.id.chipCatMeat) return "Meat & Fish";
        return "Vegetables";
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
