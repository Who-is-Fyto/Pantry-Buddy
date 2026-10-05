package com.example.pantrybuddy.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.pantrybuddy.R;
import com.example.pantrybuddy.data.local.entity.PantryItem;
import com.example.pantrybuddy.databinding.FragmentHomeBinding;
import com.example.pantrybuddy.domain.model.ExpiryStatus;
import com.example.pantrybuddy.domain.model.MatchResult;
import com.example.pantrybuddy.ui.pantry.PantryViewModel;
import com.example.pantrybuddy.ui.recipes.RecipeViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// Home dashboard showing pantry overview, expiry alerts, and featured cookable meal
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private PantryViewModel pantryViewModel;
    private RecipeViewModel recipeViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        pantryViewModel = new ViewModelProvider(requireActivity()).get(PantryViewModel.class);
        recipeViewModel = new ViewModelProvider(requireActivity()).get(RecipeViewModel.class);

        setupHeaderDateAndGreeting();
        setupObservers();
        setupClickListeners();
    }

    private void setupHeaderDateAndGreeting() {
        // Date formatting: SMART PANTRY • THURSDAY, 1 OCTOBER
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, d MMMM", Locale.US);
        String formattedDate = sdf.format(new Date()).toUpperCase(Locale.US);
        binding.tvHeaderCategory.setText(getString(R.string.home_header_format, formattedDate));

        // Time-based greeting
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting;
        if (hour < 12) {
            greeting = "Good morning, Maya";
        } else if (hour < 17) {
            greeting = "Good afternoon, Maya";
        } else {
            greeting = "Good evening, Maya";
        }
        binding.tvGreeting.setText(greeting);
    }

    private void setupObservers() {
        // Observe total pantry item count
        pantryViewModel.getAllPantryItems().observe(getViewLifecycleOwner(), items -> {
            int count = (items != null) ? items.size() : 0;
            binding.tvPantryItemCount.setText(getResources().getQuantityString(
                    R.plurals.pantry_ingredients_count, count, count
            ));
        });

        // Observe urgent items to use soon
        pantryViewModel.getExpiringSoonItems().observe(getViewLifecycleOwner(), expiringItems -> {
            if (expiringItems == null || expiringItems.isEmpty()) {
                binding.cardUrgentExpiry.setVisibility(View.GONE);
                return;
            }

            binding.cardUrgentExpiry.setVisibility(View.VISIBLE);
            binding.tvUrgentExpiryBadge.setText(
                    getString(R.string.items_to_use_soon, expiringItems.size())
            );

            StringBuilder sb = new StringBuilder();
            int limit = Math.min(expiringItems.size(), 3);
            for (int i = 0; i < limit; i++) {
                PantryItem item = expiringItems.get(i);
                ExpiryStatus status = ExpiryStatus.fromTimestamp(item.getExpiryDate());
                String statusText = (status == ExpiryStatus.TODAY) ? "Use today" :
                        (status == ExpiryStatus.TOMORROW) ? "Use tomorrow" : "Use soon";

                String qty = (item.getQuantity() == (long) item.getQuantity())
                        ? String.format(Locale.US, "%d", (long) item.getQuantity())
                        : String.format(Locale.US, "%.1f", item.getQuantity());

                sb.append(item.getName())
                        .append(" (").append(qty).append(" ").append(item.getUnit()).append(")")
                        .append(" • ").append(statusText);

                if (i < limit - 1) {
                    sb.append("\n");
                }
            }
            binding.tvUrgentExpiryList.setText(sb.toString());
        });

        // Observe featured cookable recipe
        recipeViewModel.getCookableRecipes().observe(getViewLifecycleOwner(), cookableList -> {
            if (cookableList == null || cookableList.isEmpty()) {
                binding.cardFeaturedRecipe.setVisibility(View.GONE);
                binding.cardZeroMatchState.setVisibility(View.VISIBLE);
                return;
            }

            binding.cardFeaturedRecipe.setVisibility(View.VISIBLE);
            binding.cardZeroMatchState.setVisibility(View.GONE);

            MatchResult featured = cookableList.get(0);
            long featuredRecipeId = featured.getRecipe().getRecipeId();
            binding.tvFeaturedTitle.setText(featured.getRecipe().getTitle());
            binding.tvFeaturedTime.setText(getString(R.string.cook_time_mins, featured.getRecipe().getCookTimeMinutes()));
            binding.tvFeaturedServings.setText(getString(R.string.servings_format, featured.getMaxServings()));
            binding.tvFeaturedDescription.setText(featured.getRecipe().getDescription());

            if (featured.isUsesExpiringSoonItem()) {
                binding.tvFeaturedUrgentNotice.setVisibility(View.VISIBLE);
                binding.tvFeaturedUrgentNotice.setText(R.string.uses_expiring_soon);
            } else {
                binding.tvFeaturedUrgentNotice.setVisibility(View.GONE);
            }

            View.OnClickListener openDetail = v -> {
                android.content.Intent intent = new android.content.Intent(requireContext(), com.example.pantrybuddy.ui.recipes.RecipeDetailActivity.class);
                intent.putExtra(com.example.pantrybuddy.ui.recipes.RecipeDetailActivity.EXTRA_RECIPE_ID, featuredRecipeId);
                startActivity(intent);
            };
            binding.cardFeaturedRecipe.setOnClickListener(openDetail);
            binding.btnViewFeaturedRecipe.setOnClickListener(openDetail);
        });
    }

    private void setupClickListeners() {
        binding.cardPantrySummary.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.navigation_pantry));

        binding.cardUrgentExpiry.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.navigation_recipes));

        binding.btnOpenPantryZero.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.navigation_pantry));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
