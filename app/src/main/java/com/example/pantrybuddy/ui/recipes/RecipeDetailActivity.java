package com.example.pantrybuddy.ui.recipes;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.pantrybuddy.R;
import com.example.pantrybuddy.data.local.entity.PantryItem;
import com.example.pantrybuddy.data.local.entity.Recipe;
import com.example.pantrybuddy.data.local.entity.RecipeIngredient;
import com.example.pantrybuddy.databinding.ActivityRecipeDetailBinding;
import com.example.pantrybuddy.databinding.ItemIngredientVerificationRowBinding;
import com.example.pantrybuddy.domain.engine.StrictRecipeMatcher;
import com.example.pantrybuddy.domain.engine.UnitConverter;
import com.example.pantrybuddy.domain.model.RecipeWithIngredients;
import com.example.pantrybuddy.ui.cooking.CookingActivity;
import com.example.pantrybuddy.ui.pantry.PantryViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Activity displaying detailed recipe information, dynamic serving scaler, and 3-column verification table
public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "extra_recipe_id";
    public static final String EXTRA_SERVINGS = "extra_servings";

    private ActivityRecipeDetailBinding binding;
    private RecipeViewModel recipeViewModel;
    private PantryViewModel pantryViewModel;

    private long recipeId = -1;
    private int currentServings = 2;
    private int defaultServings = 2;
    private RecipeWithIngredients currentRecipeWithIngredients;
    private List<PantryItem> pantryItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRecipeDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1);
        if (recipeId == -1) {
            Toast.makeText(this, "Recipe not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        recipeViewModel = new ViewModelProvider(this).get(RecipeViewModel.class);
        pantryViewModel = new ViewModelProvider(this).get(PantryViewModel.class);

        setupToolbar();
        setupStepper();
        setupCtaButton();
        observeData();
    }

    private void setupToolbar() {
        binding.toolbarDetail.setNavigationOnClickListener(v -> finish());
    }

    private void setupStepper() {
        binding.btnDecrementServings.setOnClickListener(v -> {
            if (currentServings > 1) {
                currentServings--;
                updateServingUi();
            }
        });

        binding.btnIncrementServings.setOnClickListener(v -> {
            currentServings++;
            updateServingUi();
        });
    }

    private void setupCtaButton() {
        binding.btnStartCooking.setOnClickListener(v -> {
            if (currentRecipeWithIngredients != null) {
                Intent intent = new Intent(this, CookingActivity.class);
                intent.putExtra(CookingActivity.EXTRA_RECIPE_ID, recipeId);
                intent.putExtra(CookingActivity.EXTRA_SERVINGS, currentServings);
                startActivity(intent);
            }
        });
    }

    private void observeData() {
        // Recipe observer
        recipeViewModel.getRecipeById(recipeId).observe(this, rwi -> {
            if (rwi == null || rwi.getRecipe() == null) return;
            currentRecipeWithIngredients = rwi;
            defaultServings = Math.max(1, rwi.getRecipe().getDefaultServings());

            populateStaticHeader(rwi.getRecipe());
            updateServingUi();
        });

        // Pantry observer
        pantryViewModel.getAllPantryItems().observe(this, items -> {
            pantryItems = (items != null) ? items : new ArrayList<>();
            updateServingUi();
        });
    }

    private void populateStaticHeader(Recipe recipe) {
        binding.tvRecipeTitle.setText(recipe.getTitle());
        binding.tvRecipeDescription.setText(recipe.getDescription());
        binding.tvRecipeCookTime.setText(String.format(Locale.US, "%d min", recipe.getCookTimeMinutes()));
        binding.tvRecipeDifficulty.setText(recipe.getDifficulty() != null ? recipe.getDifficulty() : "Easy");
        binding.tvRecipeBaseServings.setText(String.format(Locale.US, "Default: %d servings", defaultServings));

        if (recipe.getImageUrl() != null && !recipe.getImageUrl().trim().isEmpty()) {
            Glide.with(this)
                    .load(recipe.getImageUrl())
                    .placeholder(R.drawable.ic_restaurant)
                    .error(R.drawable.ic_restaurant)
                    .into(binding.ivRecipeHero);
        } else {
            binding.ivRecipeHero.setImageResource(R.drawable.ic_restaurant);
        }
    }

    private void updateServingUi() {
        if (currentRecipeWithIngredients == null || currentRecipeWithIngredients.getIngredients() == null) {
            return;
        }

        binding.tvCurrentServings.setText(String.format(Locale.US, "%d %s", currentServings, currentServings == 1 ? "serving" : "servings"));

        // 1. Calculate max servings possible and identify the limiting ingredient
        int maxPossibleServings = Integer.MAX_VALUE;
        String limitingIngredient = null;
        double limitingOnHand = 0.0;
        String limitingOnHandUnit = "";

        List<RecipeIngredient> ingredients = currentRecipeWithIngredients.getIngredients();
        for (RecipeIngredient req : ingredients) {
            PantryItem match = StrictRecipeMatcher.findMatchingPantryItem(req, pantryItems);
            double singleServingReq = req.getAmount() / defaultServings;

            if (match == null || match.getQuantity() <= 0.001) {
                maxPossibleServings = 0;
                limitingIngredient = req.getIngredientName();
                limitingOnHand = 0.0;
                limitingOnHandUnit = req.getUnit();
                break;
            } else {
                int canMake = UnitConverter.calculateMaxServings(
                        match.getQuantity(),
                        match.getUnit(),
                        singleServingReq,
                        req.getUnit(),
                        req.getNormalizedName()
                );
                if (canMake < maxPossibleServings) {
                    maxPossibleServings = canMake;
                    limitingIngredient = match.getName();
                    limitingOnHand = match.getQuantity();
                    limitingOnHandUnit = match.getUnit();
                }
            }
        }

        if (maxPossibleServings == Integer.MAX_VALUE) {
            maxPossibleServings = 0;
        }

        // 2. Update limit indicator notice
        if (currentServings <= maxPossibleServings && maxPossibleServings > 0) {
            binding.tvServingLimitNotice.setBackgroundResource(R.drawable.bg_pill_green);
            binding.tvServingLimitNotice.setTextColor(ContextCompat.getColor(this, R.color.colorAccentGreen));

            if (currentServings == maxPossibleServings && limitingIngredient != null) {
                binding.tvServingLimitNotice.setText(String.format(
                        Locale.US,
                        "%d servings (Maximum with your %s %s %s)",
                        maxPossibleServings,
                        formatQty(limitingOnHand),
                        limitingOnHandUnit,
                        limitingIngredient
                ));
            } else {
                binding.tvServingLimitNotice.setText(String.format(
                        Locale.US,
                        "You have enough ingredients for up to %d servings",
                        maxPossibleServings
                ));
            }
        } else {
            binding.tvServingLimitNotice.setBackgroundResource(R.drawable.bg_pill_amber);
            binding.tvServingLimitNotice.setTextColor(ContextCompat.getColor(this, R.color.colorAccentAmber));
            binding.tvServingLimitNotice.setText(String.format(
                    Locale.US,
                    "%d servings exceeds current pantry stock (Max: %d)",
                    currentServings,
                    maxPossibleServings
            ));
        }

        // 3. Render 3-Column Verification Table
        binding.llIngredientVerificationRows.removeAllViews();
        boolean allSufficient = true;
        List<String> leftoversList = new ArrayList<>();

        for (RecipeIngredient req : ingredients) {
            ItemIngredientVerificationRowBinding rowBinding =
                    ItemIngredientVerificationRowBinding.inflate(getLayoutInflater(), binding.llIngredientVerificationRows, false);

            double needed = (req.getAmount() / defaultServings) * currentServings;
            PantryItem match = StrictRecipeMatcher.findMatchingPantryItem(req, pantryItems);
            double atHome = (match != null) ? match.getQuantity() : 0.0;
            String atHomeUnit = (match != null) ? match.getUnit() : req.getUnit();

            boolean sufficient = (match != null) && UnitConverter.isSufficient(
                    atHome, atHomeUnit, needed, req.getUnit(), req.getNormalizedName()
            );

            if (!sufficient) {
                allSufficient = false;
            }

            // Column 1: Name and status icon
            rowBinding.tvIngredientName.setText(req.getIngredientName());
            if (sufficient) {
                rowBinding.ivIngredientStatus.setImageResource(R.drawable.ic_check);
                rowBinding.ivIngredientStatus.setImageTintList(
                        ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorAccentGreen))
                );
            } else if (match == null || atHome <= 0.001) {
                rowBinding.ivIngredientStatus.setImageResource(R.drawable.ic_close_circle);
                rowBinding.ivIngredientStatus.setImageTintList(
                        ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorDangerRose))
                );
            } else {
                rowBinding.ivIngredientStatus.setImageResource(R.drawable.ic_alert_amber);
                rowBinding.ivIngredientStatus.setImageTintList(
                        ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorAccentAmber))
                );
            }

            // Column 2: Needed amount
            rowBinding.tvIngredientNeeded.setText(String.format("%s %s", formatQty(needed), req.getUnit()));

            // Column 3: At Home amount & difference
            rowBinding.tvIngredientAtHome.setText(String.format("%s %s", formatQty(atHome), atHomeUnit));

            if (!sufficient) {
                rowBinding.tvIngredientDifference.setVisibility(View.VISIBLE);
                double shortfall = UnitConverter.calculateShortfall(
                        atHome, atHomeUnit, needed, req.getUnit(), req.getNormalizedName()
                );
                rowBinding.tvIngredientDifference.setText(String.format("Short %s %s", formatQty(shortfall), req.getUnit()));
            } else {
                rowBinding.tvIngredientDifference.setVisibility(View.GONE);
                double leftover = UnitConverter.calculateLeftover(
                        atHome, atHomeUnit, needed, req.getUnit(), req.getNormalizedName()
                );
                if (leftover > 0.001) {
                    leftoversList.add(String.format("%s %s %s", formatQty(leftover), req.getUnit(), req.getIngredientName().toLowerCase(Locale.US)));
                }
            }

            binding.llIngredientVerificationRows.addView(rowBinding.getRoot());
        }

        // 4. Post-Cooking Projection
        if (allSufficient && !leftoversList.isEmpty()) {
            StringBuilder sb = new StringBuilder("Quantities leave ");
            for (int i = 0; i < leftoversList.size(); i++) {
                sb.append(leftoversList.get(i));
                if (i < leftoversList.size() - 2) {
                    sb.append(", ");
                } else if (i == leftoversList.size() - 2) {
                    sb.append(" and ");
                }
            }
            sb.append(" for later.");
            binding.tvPostCookingProjection.setText(sb.toString());
        } else if (allSufficient) {
            binding.tvPostCookingProjection.setText("All selected pantry ingredients will be used up completely.");
        } else {
            binding.tvPostCookingProjection.setText("Some ingredients are short for this serving size. Reduce servings or check your pantry.");
        }

        // 5. Update Start Cooking Button
        binding.btnStartCooking.setText(String.format(
                Locale.US,
                "Start cooking · %d %s",
                currentServings,
                currentServings == 1 ? "serving" : "servings"
        ));

        if (!allSufficient) {
            binding.btnStartCooking.setEnabled(false);
            binding.btnStartCooking.setAlpha(0.6f);
        } else {
            binding.btnStartCooking.setEnabled(true);
            binding.btnStartCooking.setAlpha(1.0f);
        }
    }

    private String formatQty(double val) {
        if (val == (long) val) {
            return String.valueOf((long) val);
        } else {
            return String.format(Locale.US, "%.1f", val);
        }
    }
}
