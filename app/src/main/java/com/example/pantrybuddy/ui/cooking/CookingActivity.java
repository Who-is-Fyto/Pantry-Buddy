package com.example.pantrybuddy.ui.cooking;

import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.pantrybuddy.R;
import com.example.pantrybuddy.data.local.entity.Recipe;
import com.example.pantrybuddy.databinding.ActivityCookingBinding;
import com.example.pantrybuddy.domain.model.RecipeWithIngredients;
import com.example.pantrybuddy.ui.recipes.RecipeViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Step-by-step guided cooking assistant with an interactive countdown timer
public class CookingActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "extra_recipe_id";
    public static final String EXTRA_SERVINGS = "extra_servings";

    private ActivityCookingBinding binding;
    private RecipeViewModel recipeViewModel;

    private long recipeId = -1;
    private int cookedServings = 2;
    private RecipeWithIngredients currentRecipeWithIngredients;
    private List<String> steps = new ArrayList<>();
    private int currentStepIndex = 0;

    // Timer state
    private CountDownTimer countDownTimer;
    private long totalStepDurationMs = 180_000L;
    private long timeLeftMs = 180_000L;
    private boolean isTimerRunning = false;
    private boolean isNavigatingToReview = false;

    private static final Pattern DURATION_PATTERN = Pattern.compile("(\\d+)\\s*(?:min|minute)", Pattern.CASE_INSENSITIVE);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityCookingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1);
        cookedServings = getIntent().getIntExtra(EXTRA_SERVINGS, 2);

        recipeViewModel = new ViewModelProvider(this).get(RecipeViewModel.class);

        setupToolbar();
        setupNavigationButtons();
        setupTimerControls();
        setupBackPressHandler();
        observeRecipe();
    }

    private void setupToolbar() {
        binding.toolbarCooking.setNavigationOnClickListener(v -> showExitConfirmationDialog());
    }

    private void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitConfirmationDialog();
            }
        });
    }

    private void showExitConfirmationDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.cooking_exit_title)
                .setMessage(R.string.cooking_exit_message)
                .setNegativeButton(R.string.action_keep_cooking, (dialog, which) -> dialog.dismiss())
                .setPositiveButton(R.string.action_exit, (dialog, which) -> finish())
                .show();
    }

    private void setupNavigationButtons() {
        binding.btnPrevStep.setOnClickListener(v -> {
            if (currentStepIndex > 0) {
                currentStepIndex--;
                displayCurrentStep();
            }
        });

        binding.btnNextStep.setOnClickListener(v -> {
            if (currentStepIndex < steps.size() - 1) {
                currentStepIndex++;
                displayCurrentStep();
            } else {
                finishCookingAndProceedToReview();
            }
        });
    }

    private void finishCookingAndProceedToReview() {
        if (isNavigatingToReview || isFinishing()) return;
        isNavigatingToReview = true;
        stopTimer();
        Intent intent = new Intent(this, MealReviewActivity.class);
        intent.putExtra(MealReviewActivity.EXTRA_RECIPE_ID, recipeId);
        intent.putExtra(MealReviewActivity.EXTRA_SERVINGS, cookedServings);
        startActivity(intent);
        finish();
    }

    private void setupTimerControls() {
        binding.btnPlayPauseTimer.setOnClickListener(v -> {
            if (isTimerRunning) {
                pauseTimer();
            } else {
                startTimer();
            }
        });

        binding.btnResetTimer.setOnClickListener(v -> resetTimer());
    }

    private void observeRecipe() {
        recipeViewModel.getRecipeById(recipeId).observe(this, rwi -> {
            if (rwi == null || rwi.getRecipe() == null) return;
            currentRecipeWithIngredients = rwi;
            Recipe recipe = rwi.getRecipe();

            binding.tvCookingRecipeTitle.setText(recipe.getTitle());
            parseSteps(recipe.getInstructionsJson());
            displayCurrentStep();
        });
    }

    private void parseSteps(String json) {
        steps.clear();
        if (json != null && !json.trim().isEmpty()) {
            try {
                Type listType = new TypeToken<List<String>>() {}.getType();
                List<String> parsed = new Gson().fromJson(json, listType);
                if (parsed != null && !parsed.isEmpty()) {
                    steps.addAll(parsed);
                }
            } catch (Exception ignored) {
            }
        }

        if (steps.isEmpty()) {
            steps.add("Prepare ingredients and cook according to recipe guidelines.");
        }
    }

    private void displayCurrentStep() {
        if (steps.isEmpty()) return;

        stopTimer();

        int totalSteps = steps.size();
        binding.tvStepProgressCounter.setText(getString(R.string.cooking_step_format, currentStepIndex + 1, totalSteps));
        binding.tvStepBadge.setText(getString(R.string.cooking_step_badge, currentStepIndex + 1));

        String instruction = steps.get(currentStepIndex);
        binding.tvStepInstruction.setText(instruction);

        // Progress bar
        binding.cookingProgressBar.setMax(totalSteps);
        binding.cookingProgressBar.setProgress(currentStepIndex + 1);

        // Calculate estimated time left
        int remainingSteps = totalSteps - (currentStepIndex + 1);
        int estimatedMinutesLeft = Math.max(2, remainingSteps * 3);
        binding.tvEstimatedTimeLeft.setText(getString(R.string.cooking_time_left_format, estimatedMinutesLeft));

        // Navigation button labels & state
        if (currentStepIndex == 0) {
            binding.btnPrevStep.setEnabled(false);
            binding.btnPrevStep.setAlpha(0.4f);
        } else {
            binding.btnPrevStep.setEnabled(true);
            binding.btnPrevStep.setAlpha(1.0f);
        }

        if (currentStepIndex == totalSteps - 1) {
            binding.btnNextStep.setText(R.string.action_finish_cooking);
        } else {
            binding.btnNextStep.setText(R.string.action_next_step);
        }

        // Setup timer for this step
        configureTimerForStep(instruction);
    }

    private void configureTimerForStep(String instructionText) {
        int durationMinutes = 3; // Default 3 minutes
        Matcher matcher = DURATION_PATTERN.matcher(instructionText);
        if (matcher.find()) {
            try {
                String val = matcher.group(1);
                if (val != null) {
                    durationMinutes = Integer.parseInt(val);
                }
            } catch (Exception ignored) {
            }
        }

        totalStepDurationMs = durationMinutes * 60_000L;
        timeLeftMs = totalStepDurationMs;
        isTimerRunning = false;

        binding.tvTimerLabel.setText(R.string.cooking_timer_label);
        binding.btnPlayPauseTimer.setIconResource(R.drawable.ic_play);
        updateTimerDisplay();
    }

    private void startTimer() {
        if (timeLeftMs <= 0) {
            timeLeftMs = totalStepDurationMs;
        }

        countDownTimer = new CountDownTimer(timeLeftMs, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftMs = millisUntilFinished;
                updateTimerDisplay();
            }

            @Override
            public void onFinish() {
                timeLeftMs = 0;
                isTimerRunning = false;
                updateTimerDisplay();
                binding.tvTimerLabel.setText(R.string.cooking_timer_finished);
                binding.btnPlayPauseTimer.setIconResource(R.drawable.ic_play);
                triggerTimerAlert();
            }
        }.start();

        isTimerRunning = true;
        binding.btnPlayPauseTimer.setIconResource(R.drawable.ic_pause);
    }

    private void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isTimerRunning = false;
        binding.btnPlayPauseTimer.setIconResource(R.drawable.ic_play);
    }

    private void resetTimer() {
        stopTimer();
        timeLeftMs = totalStepDurationMs;
        binding.tvTimerLabel.setText(R.string.cooking_timer_label);
        binding.btnPlayPauseTimer.setIconResource(R.drawable.ic_play);
        updateTimerDisplay();
    }

    private void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        isTimerRunning = false;
    }

    private void updateTimerDisplay() {
        long totalSeconds = (timeLeftMs + 999) / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        binding.tvTimerCountdown.setText(String.format(Locale.US, "%02d:%02d", minutes, seconds));
    }

    private void triggerTimerAlert() {
        try {
            Uri notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notificationUri);
            if (r != null) {
                r.play();
            }
        } catch (Exception ignored) {
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                VibratorManager vibratorManager = (VibratorManager) getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
                if (vibratorManager != null) {
                    Vibrator vibrator = vibratorManager.getDefaultVibrator();
                    vibrator.vibrate(VibrationEffect.createOneShot(600, VibrationEffect.DEFAULT_AMPLITUDE));
                }
            } else {
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                if (v != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createOneShot(600, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        v.vibrate(600);
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
    }
}
