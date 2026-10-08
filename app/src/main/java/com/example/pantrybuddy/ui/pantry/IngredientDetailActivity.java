package com.example.pantrybuddy.ui.pantry;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.pantrybuddy.R;
import com.example.pantrybuddy.data.local.entity.PantryItem;
import com.example.pantrybuddy.data.local.entity.RecipeIngredient;
import com.example.pantrybuddy.databinding.ActivityIngredientDetailBinding;
import com.example.pantrybuddy.domain.engine.IngredientNormalizer;
import com.example.pantrybuddy.domain.model.ExpiryStatus;
import com.example.pantrybuddy.domain.model.RecipeWithIngredients;
import com.example.pantrybuddy.ui.recipes.RecipeViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// Activity for viewing ingredient details, editing quantity/unit/expiry, or deleting
public class IngredientDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM_ID = "extra_item_id";

    private ActivityIngredientDetailBinding binding;
    private PantryViewModel pantryViewModel;
    private RecipeViewModel recipeViewModel;

    private long itemId = -1;
    private PantryItem currentItem;
    private Long selectedExpiryDateMs = null;
    private boolean initializedUi = false;
    private boolean isProcessing = false;

    private final SimpleDateFormat shortDateFormat = new SimpleDateFormat("d MMM yyyy", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityIngredientDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        itemId = getIntent().getLongExtra(EXTRA_ITEM_ID, -1);
        if (itemId == -1) {
            Toast.makeText(this, "Item not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        pantryViewModel = new ViewModelProvider(this).get(PantryViewModel.class);
        recipeViewModel = new ViewModelProvider(this).get(RecipeViewModel.class);

        setupToolbar();
        setupQuantityAdjusters();
        setupDatePicker();
        setupButtons();
        observeItemData();
    }

    private void setupToolbar() {
        binding.toolbarDetail.setNavigationOnClickListener(v -> finish());
    }

    private void observeItemData() {
        pantryViewModel.getAllPantryItems().observe(this, items -> {
            if (items == null) return;
            for (PantryItem item : items) {
                if (item.getItemId() == itemId) {
                    currentItem = item;
                    if (!initializedUi) {
                        populateUi(item);
                        initializedUi = true;
                    }
                    updateHeroCard(item);
                    break;
                }
            }
        });

        // Cross-reference banner with recipe catalog
        recipeViewModel.getAllRecipes().observe(this, this::updateCrossReference);
    }

    private void populateUi(PantryItem item) {
        selectedExpiryDateMs = item.getExpiryDate();

        // Quantity
        String qtyStr = (item.getQuantity() == (long) item.getQuantity())
                ? String.valueOf((long) item.getQuantity())
                : String.format(Locale.US, "%.1f", item.getQuantity());
        binding.etDetailQuantity.setText(qtyStr);

        // Unit Chip
        selectUnitChip(item.getUnit());

        // Category Chip
        selectCategoryChip(item.getCategory());

        // Expiry Date
        updateExpiryDateDisplay();
    }

    private void updateHeroCard(PantryItem item) {
        binding.tvDetailIngredientName.setText(item.getName());

        String qtyStr = (item.getQuantity() == (long) item.getQuantity())
                ? String.valueOf((long) item.getQuantity())
                : String.format(Locale.US, "%.1f", item.getQuantity());
        binding.tvDetailCurrentQty.setText(String.format("%s %s in pantry", qtyStr, item.getUnit()));
        binding.tvDetailCategoryBadge.setText(item.getCategory() != null ? item.getCategory() : "Other");

        // Freshness pill
        if (item.getExpiryDate() == null) {
            binding.tvDetailExpiryPill.setText("Non-perishable");
            binding.tvDetailExpiryPill.setBackgroundResource(R.drawable.bg_pill_neutral);
            binding.tvDetailExpiryPill.setTextColor(ContextCompat.getColor(this, R.color.colorTextSecondary));
        } else {
            ExpiryStatus status = ExpiryStatus.fromTimestamp(item.getExpiryDate());
            String dateFormatted = shortDateFormat.format(new Date(item.getExpiryDate()));
            switch (status) {
                case EXPIRED:
                    binding.tvDetailExpiryPill.setText("Expired");
                    binding.tvDetailExpiryPill.setBackgroundResource(R.drawable.bg_pill_danger);
                    binding.tvDetailExpiryPill.setTextColor(ContextCompat.getColor(this, R.color.colorDangerRose));
                    break;
                case TODAY:
                    binding.tvDetailExpiryPill.setText(String.format("Use today · %s", dateFormatted));
                    binding.tvDetailExpiryPill.setBackgroundResource(R.drawable.bg_pill_green);
                    binding.tvDetailExpiryPill.setTextColor(ContextCompat.getColor(this, R.color.colorAccentGreen));
                    break;
                case TOMORROW:
                    binding.tvDetailExpiryPill.setText("Use tomorrow");
                    binding.tvDetailExpiryPill.setBackgroundResource(R.drawable.bg_pill_amber);
                    binding.tvDetailExpiryPill.setTextColor(ContextCompat.getColor(this, R.color.colorAccentAmber));
                    break;
                case SOON:
                    binding.tvDetailExpiryPill.setText(String.format("Use soon · %s", dateFormatted));
                    binding.tvDetailExpiryPill.setBackgroundResource(R.drawable.bg_pill_amber);
                    binding.tvDetailExpiryPill.setTextColor(ContextCompat.getColor(this, R.color.colorAccentAmber));
                    break;
                case SAFE:
                default:
                    binding.tvDetailExpiryPill.setText(String.format("Best before %s", dateFormatted));
                    binding.tvDetailExpiryPill.setBackgroundResource(R.drawable.bg_pill_neutral);
                    binding.tvDetailExpiryPill.setTextColor(ContextCompat.getColor(this, R.color.colorTextSecondary));
                    break;
            }
        }
    }

    private void updateCrossReference(List<RecipeWithIngredients> recipes) {
        if (currentItem == null || recipes == null) return;

        int matchCount = 0;
        for (RecipeWithIngredients r : recipes) {
            if (r.getIngredients() == null) continue;
            for (RecipeIngredient req : r.getIngredients()) {
                if (IngredientNormalizer.matches(req.getNormalizedName(), currentItem.getNormalizedName())) {
                    matchCount++;
                    break;
                }
            }
        }

        if (matchCount > 0) {
            String recipeWord = (matchCount == 1) ? "recipe" : "recipes";
            binding.tvCrossReferenceText.setText(String.format(
                    Locale.US,
                    "Your %s is used in %d %s in the catalog.",
                    currentItem.getName().toLowerCase(Locale.US),
                    matchCount,
                    recipeWord
            ));
            binding.cardCrossReference.setVisibility(View.VISIBLE);
        } else {
            binding.cardCrossReference.setVisibility(View.GONE);
        }
    }

    private void setupQuantityAdjusters() {
        binding.btnIncrementQty.setOnClickListener(v -> adjustQuantity(1));
        binding.btnDecrementQty.setOnClickListener(v -> adjustQuantity(-1));
    }

    private void adjustQuantity(int direction) {
        String currentText = binding.etDetailQuantity.getText().toString().trim();
        double val = 0.0;
        try {
            val = Double.parseDouble(currentText);
        } catch (Exception ignored) {}

        String unit = getSelectedUnit();
        double step = 1.0;
        if ("g".equalsIgnoreCase(unit) || "ml".equalsIgnoreCase(unit)) {
            step = 50.0;
        } else if ("kg".equalsIgnoreCase(unit) || "l".equalsIgnoreCase(unit)) {
            step = 0.5;
        }

        val = Math.max(0.0, val + (direction * step));
        String formatted = (val == (long) val)
                ? String.valueOf((long) val)
                : String.format(Locale.US, "%.1f", val);
        binding.etDetailQuantity.setText(formatted);
    }

    private void setupDatePicker() {
        binding.layoutDetailDatePicker.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            if (selectedExpiryDateMs != null) {
                cal.setTimeInMillis(selectedExpiryDateMs);
            }
            DatePickerDialog dialog = new DatePickerDialog(
                    this,
                    (picker, year, month, dayOfMonth) -> {
                        Calendar chosen = Calendar.getInstance();
                        chosen.set(year, month, dayOfMonth, 23, 59, 59);
                        selectedExpiryDateMs = chosen.getTimeInMillis();
                        updateExpiryDateDisplay();
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
            );
            dialog.show();
        });

        binding.btnClearDetailExpiry.setOnClickListener(v -> {
            selectedExpiryDateMs = null;
            updateExpiryDateDisplay();
        });
    }

    private void updateExpiryDateDisplay() {
        if (selectedExpiryDateMs != null) {
            binding.tvDetailSelectedExpiry.setText(shortDateFormat.format(new Date(selectedExpiryDateMs)));
            binding.tvDetailSelectedExpiry.setTextColor(ContextCompat.getColor(this, R.color.colorTextPrimary));
            binding.btnClearDetailExpiry.setVisibility(View.VISIBLE);
        } else {
            binding.tvDetailSelectedExpiry.setText("No expiration date set");
            binding.tvDetailSelectedExpiry.setTextColor(ContextCompat.getColor(this, R.color.colorTextSecondary));
            binding.btnClearDetailExpiry.setVisibility(View.GONE);
        }
    }

    private void setupButtons() {
        // Save
        binding.btnSaveIngredient.setOnClickListener(v -> saveChanges());

        // Delete
        binding.btnDeleteIngredient.setOnClickListener(v -> confirmDelete());
    }

    private void saveChanges() {
        if (isProcessing || isFinishing() || currentItem == null) return;

        String qtyStr = binding.etDetailQuantity.getText().toString().trim();
        double newQuantity;
        try {
            newQuantity = Double.parseDouble(qtyStr);
            if (newQuantity <= 0.0) throw new NumberFormatException();
        } catch (Exception e) {
            binding.etDetailQuantity.setError("Enter quantity greater than 0");
            binding.etDetailQuantity.requestFocus();
            return;
        }

        isProcessing = true;
        binding.btnSaveIngredient.setEnabled(false);

        String newUnit = getSelectedUnit();
        String newCategory = getSelectedCategory();

        currentItem.setQuantity(newQuantity);
        currentItem.setUnit(newUnit);
        currentItem.setCategory(newCategory);
        currentItem.setExpiryDate(selectedExpiryDateMs);

        pantryViewModel.update(currentItem);
        Toast.makeText(this, "Updated " + currentItem.getName(), Toast.LENGTH_SHORT).show();
        finish();
    }

    private void confirmDelete() {
        if (isProcessing || isFinishing() || currentItem == null) return;

        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete ingredient?")
                .setMessage("Are you sure you want to remove " + currentItem.getName() + " from your pantry?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (isProcessing) return;
                    isProcessing = true;
                    binding.btnDeleteIngredient.setEnabled(false);
                    pantryViewModel.delete(currentItem);
                    Toast.makeText(this, "Removed from pantry", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void selectUnitChip(String unit) {
        if (unit == null) return;
        String u = unit.toLowerCase(Locale.US);
        if ("g".equals(u)) binding.chipDetailUnitG.setChecked(true);
        else if ("kg".equals(u)) binding.chipDetailUnitKg.setChecked(true);
        else if ("ml".equals(u)) binding.chipDetailUnitMl.setChecked(true);
        else if ("l".equals(u)) binding.chipDetailUnitL.setChecked(true);
        else if ("tbsp".equals(u)) binding.chipDetailUnitTbsp.setChecked(true);
        else if ("tsp".equals(u)) binding.chipDetailUnitTsp.setChecked(true);
        else binding.chipDetailUnitPcs.setChecked(true);
    }

    private String getSelectedUnit() {
        int checkedId = binding.chipGroupDetailUnit.getCheckedChipId();
        if (checkedId == R.id.chipDetailUnitG) return "g";
        if (checkedId == R.id.chipDetailUnitKg) return "kg";
        if (checkedId == R.id.chipDetailUnitMl) return "ml";
        if (checkedId == R.id.chipDetailUnitL) return "l";
        if (checkedId == R.id.chipDetailUnitTbsp) return "tbsp";
        if (checkedId == R.id.chipDetailUnitTsp) return "tsp";
        return "pcs";
    }

    private void selectCategoryChip(String category) {
        if (category == null) return;
        if ("Dairy & Eggs".equalsIgnoreCase(category)) binding.chipDetailCatDairy.setChecked(true);
        else if ("Staples".equalsIgnoreCase(category)) binding.chipDetailCatStaples.setChecked(true);
        else if ("Spices & Seasonings".equalsIgnoreCase(category)) binding.chipDetailCatSpices.setChecked(true);
        else if ("Meat & Fish".equalsIgnoreCase(category)) binding.chipDetailCatMeat.setChecked(true);
        else binding.chipDetailCatVegetables.setChecked(true);
    }

    private String getSelectedCategory() {
        int checkedId = binding.chipGroupDetailCategory.getCheckedChipId();
        if (checkedId == R.id.chipDetailCatDairy) return "Dairy & Eggs";
        if (checkedId == R.id.chipDetailCatStaples) return "Staples";
        if (checkedId == R.id.chipDetailCatSpices) return "Spices & Seasonings";
        if (checkedId == R.id.chipDetailCatMeat) return "Meat & Fish";
        return "Vegetables";
    }
}
