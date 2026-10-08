package com.example.pantrybuddy.ui.settings;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.pantrybuddy.R;
import com.example.pantrybuddy.databinding.FragmentSettingsBinding;
import com.example.pantrybuddy.ui.pantry.PantryViewModel;
import com.example.pantrybuddy.utils.ExpiryReminderWorker;
import com.example.pantrybuddy.utils.NotificationHelper;
import com.example.pantrybuddy.utils.PreferenceHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

// Settings fragment for managing notifications, units, anti-waste urgency, and sample data reset
public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private PantryViewModel pantryViewModel;

    private final ActivityResultLauncher<String> requestNotificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                Context context = getContext();
                if (context != null) {
                    if (isGranted) {
                        PreferenceHelper.setExpiryAlertsEnabled(context, true);
                        ExpiryReminderWorker.scheduleDailyReminders(context);
                    } else {
                        PreferenceHelper.setExpiryAlertsEnabled(context, false);
                        if (binding != null) {
                            binding.switchExpiryAlerts.setChecked(false);
                        }
                    }
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        pantryViewModel = new ViewModelProvider(requireActivity()).get(PantryViewModel.class);

        setupInitialState();
        setupListeners();
    }

    private void setupInitialState() {
        Context context = requireContext();

        // 1. Notification switch
        boolean alertsEnabled = PreferenceHelper.isExpiryAlertsEnabled(context);
        binding.switchExpiryAlerts.setChecked(alertsEnabled);

        // 2. Unit preference
        String unitSystem = PreferenceHelper.getUnitSystem(context);
        if (PreferenceHelper.UNIT_IMPERIAL.equalsIgnoreCase(unitSystem)) {
            binding.rbImperial.setChecked(true);
        } else {
            binding.rbMetric.setChecked(true);
        }

        // 3. Urgency threshold
        int thresholdDays = PreferenceHelper.getUrgencyThresholdDays(context);
        if (thresholdDays == 1) {
            binding.chipThreshold1.setChecked(true);
        } else if (thresholdDays == 3) {
            binding.chipThreshold3.setChecked(true);
        } else {
            binding.chipThreshold2.setChecked(true);
        }
    }

    private void setupListeners() {
        Context context = requireContext();

        // Notification Switch
        binding.switchExpiryAlerts.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                } else {
                    PreferenceHelper.setExpiryAlertsEnabled(context, true);
                    ExpiryReminderWorker.scheduleDailyReminders(context);
                }
            } else {
                PreferenceHelper.setExpiryAlertsEnabled(context, false);
                ExpiryReminderWorker.cancelReminders(context);
            }
        });

        // Measurement Units RadioGroup
        binding.rgMeasurementUnits.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbImperial) {
                PreferenceHelper.setUnitSystem(context, PreferenceHelper.UNIT_IMPERIAL);
                Toast.makeText(context, getString(R.string.toast_units_updated, getString(R.string.unit_imperial)), Toast.LENGTH_SHORT).show();
            } else {
                PreferenceHelper.setUnitSystem(context, PreferenceHelper.UNIT_METRIC);
                Toast.makeText(context, getString(R.string.toast_units_updated, getString(R.string.unit_metric)), Toast.LENGTH_SHORT).show();
            }
        });

        // Anti-Waste Urgency Threshold Chips
        binding.cgUrgencyThreshold.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                binding.chipThreshold2.setChecked(true);
                return;
            }

            int selectedId = checkedIds.get(0);
            int days = 2;
            if (selectedId == R.id.chipThreshold1) {
                days = 1;
            } else if (selectedId == R.id.chipThreshold3) {
                days = 3;
            }

            PreferenceHelper.setUrgencyThresholdDays(context, days);
            Toast.makeText(context, getString(R.string.toast_threshold_updated, days), Toast.LENGTH_SHORT).show();
        });

        // Marker Testing Tool: Reset Sample Pantry Button
        binding.btnResetSampleData.setOnClickListener(v -> showResetConfirmationDialog());
    }

    private void showResetConfirmationDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_reset_title)
                .setMessage(R.string.dialog_reset_message)
                .setNegativeButton(R.string.action_cancel, (dialog, which) -> dialog.dismiss())
                .setPositiveButton(R.string.action_reset, (dialog, which) -> {
                    pantryViewModel.resetSampleData(() -> {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), R.string.toast_reset_success, Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                })
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
