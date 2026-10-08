package com.example.pantrybuddy.utils;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.pantrybuddy.data.local.AppDatabase;
import com.example.pantrybuddy.data.local.entity.PantryItem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

// Background worker that inspects pantry stock and fires daily expiration reminders
public class ExpiryReminderWorker extends Worker {

    public static final String WORK_NAME_EXPIRY_REMINDER = "pantry_expiry_daily_reminder";

    public ExpiryReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();

        if (!PreferenceHelper.isExpiryAlertsEnabled(context)) {
            return Result.success();
        }

        int thresholdDays = PreferenceHelper.getUrgencyThresholdDays(context);
        long thresholdMillis = System.currentTimeMillis() + (thresholdDays * 24L * 60L * 60L * 1000L);

        AppDatabase db = AppDatabase.getDatabase(context);
        List<PantryItem> allItems = db.pantryDao().getAllPantryItemsSync();

        if (allItems == null || allItems.isEmpty()) {
            return Result.success();
        }

        List<PantryItem> expiringItems = new ArrayList<>();
        for (PantryItem item : allItems) {
            if (item.getExpiryDate() != null && item.getExpiryDate() <= thresholdMillis) {
                expiringItems.add(item);
            }
        }

        if (!expiringItems.isEmpty()) {
            StringBuilder summary = new StringBuilder();
            if (expiringItems.size() == 1) {
                summary.append(expiringItems.get(0).getName());
            } else if (expiringItems.size() == 2) {
                summary.append(expiringItems.get(0).getName())
                        .append(" and ")
                        .append(expiringItems.get(1).getName());
            } else {
                summary.append(expiringItems.get(0).getName())
                        .append(", ")
                        .append(expiringItems.get(1).getName())
                        .append(" and ")
                        .append(expiringItems.size() - 2)
                        .append(" more");
            }

            NotificationHelper.showExpiryNotification(context, expiringItems.size(), summary.toString());
        }

        return Result.success();
    }

    public static void scheduleDailyReminders(Context context) {
        // Run every 12 hours to cover morning (9 AM) and evening (5 PM) reminder windows
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                ExpiryReminderWorker.class,
                12,
                TimeUnit.HOURS
        ).build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME_EXPIRY_REMINDER,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
        );
    }

    public static void cancelReminders(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME_EXPIRY_REMINDER);
    }
}
