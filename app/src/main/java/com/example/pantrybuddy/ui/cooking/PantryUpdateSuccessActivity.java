package com.example.pantrybuddy.ui.cooking;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.pantrybuddy.MainActivity;
import com.example.pantrybuddy.R;
import com.example.pantrybuddy.databinding.ActivityPantryUpdateSuccessBinding;
import com.example.pantrybuddy.databinding.ItemPantryDeltaRowBinding;
import com.example.pantrybuddy.domain.model.DeductionDelta;
import com.example.pantrybuddy.ui.pantry.PantryViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Post-cooking confirmation screen showing the exact inventory delta and remaining stock
public class PantryUpdateSuccessActivity extends AppCompatActivity {

    public static final String EXTRA_DELTAS = "extra_deltas";

    private ActivityPantryUpdateSuccessBinding binding;
    private PantryViewModel pantryViewModel;
    private ArrayList<DeductionDelta> deltas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityPantryUpdateSuccessBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        pantryViewModel = new ViewModelProvider(this).get(PantryViewModel.class);

        @SuppressWarnings("unchecked")
        ArrayList<DeductionDelta> passedDeltas = (ArrayList<DeductionDelta>) getIntent().getSerializableExtra(EXTRA_DELTAS);
        if (passedDeltas != null) {
            deltas = passedDeltas;
        }

        setupToolbar();
        setupCtaButton();
        renderDeltas();
        observeRemainingCount();
    }

    private void setupToolbar() {
        binding.toolbarSuccess.setNavigationOnClickListener(v -> navigateToPantry());
    }

    private void setupCtaButton() {
        binding.btnViewUpdatedPantry.setOnClickListener(v -> navigateToPantry());
    }

    private void navigateToPantry() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(MainActivity.EXTRA_NAV_TAB, R.id.navigation_pantry);
        startActivity(intent);
        finish();
    }

    private void renderDeltas() {
        binding.llDeltaRows.removeAllViews();

        for (DeductionDelta delta : deltas) {
            ItemPantryDeltaRowBinding rowBinding = ItemPantryDeltaRowBinding.inflate(
                    getLayoutInflater(),
                    binding.llDeltaRows,
                    false
            );

            rowBinding.tvDeltaItemName.setText(delta.getItemName());

            if (delta.isUsedUp()) {
                String detail = String.format(
                        Locale.US,
                        "%s → 0 %s (%s %s used)",
                        formatQty(delta.getBeforeQuantity()),
                        delta.getUnit(),
                        formatQty(delta.getUsedQuantity()),
                        delta.getUnit()
                );
                rowBinding.tvDeltaDetails.setText(detail);
                rowBinding.tvDeltaStatusPill.setText(R.string.label_used_up);
                rowBinding.tvDeltaStatusPill.setBackgroundResource(R.drawable.bg_pill_neutral);
                rowBinding.tvDeltaStatusPill.setTextColor(ContextCompat.getColor(this, R.color.colorTextSecondary));
            } else {
                String detail = String.format(
                        Locale.US,
                        "%s → %s %s (%s %s used)",
                        formatQty(delta.getBeforeQuantity()),
                        formatQty(delta.getRemainingQuantity()),
                        delta.getUnit(),
                        formatQty(delta.getUsedQuantity()),
                        delta.getUnit()
                );
                rowBinding.tvDeltaDetails.setText(detail);
                rowBinding.tvDeltaStatusPill.setText(String.format(
                        Locale.US,
                        "%s %s left",
                        formatQty(delta.getRemainingQuantity()),
                        delta.getUnit()
                ));
                rowBinding.tvDeltaStatusPill.setBackgroundResource(R.drawable.bg_pill_green);
                rowBinding.tvDeltaStatusPill.setTextColor(ContextCompat.getColor(this, R.color.colorAccentGreen));
            }

            binding.llDeltaRows.addView(rowBinding.getRoot());
        }
    }

    private void observeRemainingCount() {
        pantryViewModel.getItemCount().observe(this, count -> {
            int remainingItems = (count != null) ? count : 0;

            List<String> usedUpNames = new ArrayList<>();
            for (DeductionDelta delta : deltas) {
                if (delta.isUsedUp()) {
                    usedUpNames.add(delta.getItemName());
                }
            }

            String usedUpNotice = "";
            if (!usedUpNames.isEmpty()) {
                if (usedUpNames.size() == 1) {
                    usedUpNotice = usedUpNames.get(0) + " is marked used up. ";
                } else if (usedUpNames.size() == 2) {
                    usedUpNotice = usedUpNames.get(0) + " and " + usedUpNames.get(1) + " are marked used up. ";
                } else {
                    usedUpNotice = usedUpNames.size() + " items are marked used up. ";
                }
            }

            String notification = getString(
                    R.string.pantry_update_notification_format,
                    remainingItems,
                    usedUpNotice
            );
            binding.tvSuccessNotification.setText(notification);
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
