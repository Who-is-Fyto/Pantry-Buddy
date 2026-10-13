package com.example.pantrybuddy.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.pantrybuddy.R;
import com.example.pantrybuddy.data.local.entity.PantryItem;
import com.example.pantrybuddy.databinding.BottomSheetExpiryReminderBinding;
import com.example.pantrybuddy.databinding.ItemExpiryReminderRowBinding;
import com.example.pantrybuddy.domain.model.ExpiryStatus;
import com.example.pantrybuddy.domain.model.MatchResult;
import com.example.pantrybuddy.ui.pantry.PantryViewModel;
import com.example.pantrybuddy.ui.recipes.RecipeViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// Modal bottom sheet offering proactive nudges for ingredients expiring today or tomorrow
public class ExpiryReminderBottomSheet extends BottomSheetDialogFragment {

    private BottomSheetExpiryReminderBinding binding;
    private PantryViewModel pantryViewModel;
    private RecipeViewModel recipeViewModel;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM", Locale.US);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetExpiryReminderBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        pantryViewModel = new ViewModelProvider(requireActivity()).get(PantryViewModel.class);
        recipeViewModel = new ViewModelProvider(requireActivity()).get(RecipeViewModel.class);

        setupObservers();
        setupActions();
    }

    private void setupObservers() {
        pantryViewModel.getExpiringSoonItems().observe(getViewLifecycleOwner(), expiringItems -> {
            if (binding == null) return;
            binding.llExpiryItemsContainer.removeAllViews();

            if (expiringItems == null || expiringItems.isEmpty()) {
                binding.tvExpirySheetSubtitle.setText("All your ingredients are fresh. Your pantry is well stocked.");
                return;
            }

            int count = expiringItems.size();
            binding.tvExpirySheetSubtitle.setText(getString(R.string.expiry_nudge_subtitle_format, count));

            for (PantryItem item : expiringItems) {
                ItemExpiryReminderRowBinding rowBinding = ItemExpiryReminderRowBinding.inflate(
                        getLayoutInflater(), binding.llExpiryItemsContainer, false);

                String qty = (item.getQuantity() == (long) item.getQuantity())
                        ? String.format(Locale.US, "%d", (long) item.getQuantity())
                        : String.format(Locale.US, "%.1f", item.getQuantity());

                rowBinding.tvExpiryItemNameAndQty.setText(String.format(
                        Locale.US, "%s · %s %s", item.getName(), qty, item.getUnit()));

                String dateStr = (item.getExpiryDate() != null)
                        ? dateFormat.format(new Date(item.getExpiryDate()))
                        : "";

                ExpiryStatus status = ExpiryStatus.fromTimestamp(item.getExpiryDate());
                String statusLabel;
                if (status == ExpiryStatus.TODAY) {
                    statusLabel = "Use today · " + dateStr;
                } else if (status == ExpiryStatus.TOMORROW) {
                    statusLabel = "Use tomorrow · " + dateStr;
                } else {
                    statusLabel = "Use soon · " + dateStr;
                }
                rowBinding.tvExpiryItemStatus.setText(statusLabel);

                binding.llExpiryItemsContainer.addView(rowBinding.getRoot());
            }
        });

        recipeViewModel.getCookableRecipes().observe(getViewLifecycleOwner(), cookableList -> {
            if (binding == null) return;
            int recipeCount = (cookableList != null) ? cookableList.size() : 0;
            if (recipeCount > 0) {
                binding.cardRecipeTeaser.setVisibility(View.VISIBLE);
                binding.tvRecipeTeaserTitle.setText(getString(R.string.expiry_recipes_teaser_format, recipeCount));
            } else {
                binding.cardRecipeTeaser.setVisibility(View.GONE);
            }
        });
    }

    private void setupActions() {
        binding.btnSeeCookableRecipes.setOnClickListener(v -> {
            dismiss();
            try {
                NavHostFragment.findNavController(this).navigate(R.id.navigation_recipes);
            } catch (Exception ignored) {
            }
        });

        binding.btnSnoozeToday5pm.setOnClickListener(v -> {
            Toast.makeText(requireContext(), R.string.toast_snooze_5pm, Toast.LENGTH_SHORT).show();
            dismiss();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
