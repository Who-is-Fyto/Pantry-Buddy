package com.example.pantrybuddy.utils;

import android.content.Context;
import android.content.SharedPreferences;

// Helper to persist user preferences for notifications, measurement units, and anti-waste thresholds
public class PreferenceHelper {

    private static final String PREF_NAME = "pantry_buddy_prefs";

    public static final String KEY_EXPIRY_ALERTS = "pref_expiry_alerts_enabled";
    public static final String KEY_UNIT_SYSTEM = "pref_unit_system";
    public static final String KEY_THRESHOLD_DAYS = "pref_urgency_threshold_days";

    public static final String UNIT_METRIC = "metric";
    public static final String UNIT_IMPERIAL = "imperial";

    public static final int DEFAULT_THRESHOLD_DAYS = 2;

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static boolean isExpiryAlertsEnabled(Context context) {
        return getPrefs(context).getBoolean(KEY_EXPIRY_ALERTS, true);
    }

    public static void setExpiryAlertsEnabled(Context context, boolean enabled) {
        getPrefs(context).edit().putBoolean(KEY_EXPIRY_ALERTS, enabled).apply();
    }

    public static String getUnitSystem(Context context) {
        return getPrefs(context).getString(KEY_UNIT_SYSTEM, UNIT_METRIC);
    }

    public static void setUnitSystem(Context context, String unitSystem) {
        getPrefs(context).edit().putString(KEY_UNIT_SYSTEM, unitSystem).apply();
    }

    public static int getUrgencyThresholdDays(Context context) {
        return getPrefs(context).getInt(KEY_THRESHOLD_DAYS, DEFAULT_THRESHOLD_DAYS);
    }

    public static void setUrgencyThresholdDays(Context context, int days) {
        getPrefs(context).edit().putInt(KEY_THRESHOLD_DAYS, days).apply();
    }
}
