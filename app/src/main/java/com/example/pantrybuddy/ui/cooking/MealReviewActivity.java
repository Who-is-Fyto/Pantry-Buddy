package com.example.pantrybuddy.ui.cooking;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.pantrybuddy.R;
import com.example.pantrybuddy.data.local.entity.PantryItem;
import com.example.pantrybuddy.data.local.entity.RecipeIngredient;
import com.example.pantrybuddy.databinding.ActivityMealReviewBinding;
import com.example.pantrybuddy.databinding.ItemMealReviewRowBinding;
import com.example.pantrybuddy.domain.engine.StrictRecipeMatcher;
import com.example.pantrybuddy.domain.engine.UnitConverter;
import com.example.pantrybuddy.domain.model.DeductionDelta;
import com.example.pantrybuddy.domain.model.RecipeWithIngredients;
import com.example.pantrybuddy.domain.model.StockDeduction;
import com.example.pantrybuddy.ui.pantry.PantryViewModel;
import com.example.pantrybuddy.ui.recipes.RecipeViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Meal completion review activity for verifying and editing post-cooking ingredient amounts
public class MealReviewActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "extra_recipe_id";
    public static final String EXTRA_SERVINGS = "extra_servings";

    private ActivityMealReviewBinding binding;
    private RecipeViewModel recipeViewModel;
    private PantryViewModel pantryViewModel;

    private long recipeId = -1;
    private int cookedServings = 2;

    private RecipeWithIngredients currentRecipeWithIngredients;
    private List<PantryItem> pantryItems = new ArrayList<>();
    private final List<ReviewRowHolder> rowHolders = new ArrayList<>();
    private boolean isTableBuilt = false;
    private boolean isSubmittingDeduction = false;

    private static class ReviewRowHolder {
        final ItemMealReviewRowBinding rowBinding;
        final RecipeIngredient recipeIngredient;
        final PantryItem pantryItem;
        double usedAmount;

        ReviewRowHolder(ItemMealReviewRowBinding rowBinding, RecipeIngredient recipeIngredient, PantryItem pantryItem, double usedAmount) {
            this.rowBinding = rowBinding;
            this.recipeIngredient = recipeIngredient;
            this.pantryItem = pantryItem;
            this.usedAmount = usedAmount;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMealReviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1);
        cookedServings = getIntent().getIntExtra(EXTRA_SERVINGS, 2);

        recipeViewModel = new ViewModelProvider(this).get(RecipeViewModel.class);
        pantryViewModel = new ViewModelProvider(this).get(PantryViewModel.class);

        setupToolbar();
        setupCheckboxAndCta();
        observeData();
    }

    private void setupToolbar() {
        binding.toolbarMealReview.setNavigationOnClickListener(v -> finish());
    }

    private void setupCheckboxAndCta() {
        updateCtaButtonState(false);

        binding.cbConfirmAmounts.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateCtaButtonState(isChecked);
        });

        binding.btnConfirmPantryUpdate.setOnClickListener(v -> confirmAndDeductStock());
    }

    private void updateCtaButtonState(boolean enabled) {
        binding.btnConfirmPantryUpdate.setEnabled(enabled);
        binding.btnConfirmPantryUpdate.setAlpha(enabled ? 1.0f : 0.5f);
    }

    private void observeData() {
        pantryViewModel.getAllPantryItems().observe(this, items -> {
            if (items != null) {
                pantryItems = items;
                tryBuildReviewTable();
            }
        });

        recipeViewModel.getRecipeById(recipeId).observe(this, rwi -> {
            if (rwi != null && rwi.getRecipe() != null) {
                currentRecipeWithIngredients = rwi;
                String subtitle = getString(
                        R.string.meal_review_subtitle_format,
                        rwi.getRecipe().getTitle(),
                        cookedServings,
                        cookedServings == 1 ? "serving" : "servings"
                );
                binding.tvMealReviewSubtitle.setText(subtitle);
                tryBuildReviewTable();
            }
        });
    }

    private void tryBuildReviewTable() {
        if (isTableBuilt || currentRecipeWithIngredients == null || pantryItems.isEmpty()) {
            return;
        }

        binding.llMealReviewRows.removeAllViews();
        rowHolders.clear();

        int defaultServings = Math.max(1, currentRecipeWithIngredients.getRecipe().getDefaultServings());
        List<RecipeIngredient> ingredients = currentRecipeWithIngredients.getIngredients();

        for (RecipeIngredient req : ingredients) {
            ItemMealReviewRowBinding rowBinding = ItemMealReviewRowBinding.inflate(getLayoutInflater(), binding.llMealReviewRows, false);

            double scaledDefaultUsed = (req.getAmount() / defaultServings) * cookedServings;
            PantryItem match = StrictRecipeMatcher.findMatchingPantryItem(req, pantryItems);

            double beforeQty = (match != null) ? match.getQuantity() : 0.0;
            String beforeUnit = (match != null) ? match.getUnit() : req.getUnit();

            // Set Ingredient Name
            rowBinding.tvReviewIngredientName.setText(req.getIngredientName());

            // Set Before Quantity
            rowBinding.tvReviewBeforeQuantity.setText(String.format("%s %s", formatQty(beforeQty), beforeUnit));

            // Set Actually Used initial value & unit
            rowBinding.etActuallyUsed.setText(formatQty(scaledDefaultUsed));
            rowBinding.tvReviewUnit.setText(req.getUnit());

            ReviewRowHolder holder = new ReviewRowHolder(rowBinding, req, match, scaledDefaultUsed);
            rowHolders.add(holder);

            // Calculate Initial Remaining
            updateRowRemaining(holder);

            // Listen for user edits
            rowBinding.etActuallyUsed.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    double inputVal = 0.0;
                    if (s != null && s.length() > 0) {
                        try {
                            inputVal = Double.parseDouble(s.toString().trim());
                        } catch (NumberFormatException ignored) {
                        }
                    }
                    holder.usedAmount = Math.max(0.0, inputVal);
                    updateRowRemaining(holder);
                }
            });

            binding.llMealReviewRows.addView(rowBinding.getRoot());
        }

        isTableBuilt = true;
    }

    private void updateRowRemaining(ReviewRowHolder holder) {
        double beforeQty = (holder.pantryItem != null) ? holder.pantryItem.getQuantity() : 0.0;
        String beforeUnit = (holder.pantryItem != null) ? holder.pantryItem.getUnit() : holder.recipeIngredient.getUnit();

        double leftover = UnitConverter.calculateLeftover(
                beforeQty,
                beforeUnit,
                holder.usedAmount,
                holder.recipeIngredient.getUnit(),
                holder.recipeIngredient.getNormalizedName()
        );

        if (leftover <= 0.001) {
            holder.rowBinding.tvWillRemain.setText(String.format("0 %s", beforeUnit));
            holder.rowBinding.tvUsedUpBadge.setVisibility(View.VISIBLE);
        } else {
            holder.rowBinding.tvWillRemain.setText(String.format("%s %s", formatQty(leftover), beforeUnit));
            holder.rowBinding.tvUsedUpBadge.setVisibility(View.GONE);
        }
    }

    private void confirmAndDeductStock() {
        if (isSubmittingDeduction || isFinishing()) return;
        if (rowHolders.isEmpty()) {
            Toast.makeText(this, "No ingredients to deduct.", Toast.LENGTH_SHORT).show();
            return;
        }

        isSubmittingDeduction = true;
        updateCtaButtonState(false);

        List<StockDeduction> deductions = new ArrayList<>();
        ArrayList<DeductionDelta> deltas = new ArrayList<>();

        for (ReviewRowHolder holder : rowHolders) {
            if (holder.pantryItem == null) continue;

            double usedInPantryUnit = UnitConverter.convertAmount(
                    holder.usedAmount,
                    holder.recipeIngredient.getUnit(),
                    holder.pantryItem.getUnit(),
                    holder.recipeIngredient.getNormalizedName()
            );

            deductions.add(new StockDeduction(holder.pantryItem.getItemId(), usedInPantryUnit));

            double beforeQty = holder.pantryItem.getQuantity();
            double remainingQty = Math.max(0.0, beforeQty - usedInPantryUnit);

            deltas.add(new DeductionDelta(
                    holder.pantryItem.getItemId(),
                    holder.pantryItem.getName(),
                    holder.pantryItem.getUnit(),
                    beforeQty,
                    usedInPantryUnit,
                    remainingQty
            ));
        }

        // Execute batch deduction atomically via ViewModel
        pantryViewModel.deductStock(deductions, () -> {
            runOnUiThread(() -> {
                Intent intent = new Intent(MealReviewActivity.this, PantryUpdateSuccessActivity.class);
                intent.putExtra(PantryUpdateSuccessActivity.EXTRA_DELTAS, deltas);
                startActivity(intent);
                finish();
            });
        });
    }

    private String formatQty(double val) {
        if (val == (long) val) {
            return String.valueOf((long) val);
        } else {
            return String.format(Locale.US, "%.1f", val);
        }
    }
}
