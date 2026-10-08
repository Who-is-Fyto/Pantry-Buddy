package com.example.pantrybuddy.utils;

import android.content.Context;
import android.content.SharedPreferences;

// Helper to persist user preferences for notifications, measurement units, and anti-waste thresholds
public class PreferenceHelper {

    private static final String PREF_NAME = "pantry_buddy_prefs";

    public static final String KEY_EXPIRY_ALERTS = "pref_expiry_alerts_enabled";
    public static final String KEY_UNIT_SYSTEM = "pref_unit_system";
    public static final String KEY_THRESHOLD_DAYS = "pref_urgency_threshold_days";
    public static final String KEY_USER_NAME = "pref_user_name";

    public static final String UNIT_METRIC = "metric";
    public static final String UNIT_IMPERIAL = "imperial";

    public static final String DEFAULT_USER_NAME = "Chef";
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

    // Returns the saved display name, or "Chef" if empty or never set
    public static String getUserName(Context context) {
        String name = getPrefs(context).getString(KEY_USER_NAME, DEFAULT_USER_NAME);
        if (name == null || name.trim().isEmpty()) {
            return DEFAULT_USER_NAME;
        }
        return name.trim();
    }

    // Saves the display name. Trims whitespace; falls back to default if blank.
    public static void setUserName(Context context, String name) {
        String cleaned = (name == null) ? DEFAULT_USER_NAME : name.trim();
        if (cleaned.isEmpty()) {
            cleaned = DEFAULT_USER_NAME;
        }
        getPrefs(context).edit().putString(KEY_USER_NAME, cleaned).apply();
    }
}
