package com.example.pantrybuddy.domain.model;

import java.util.Calendar;

// Status for item freshness and badges
public enum ExpiryStatus {
    EXPIRED,
    TODAY,
    TOMORROW,
    SOON,
    SAFE;

    public static ExpiryStatus fromTimestamp(Long expiryDateMs) {
        if (expiryDateMs == null) {
            return SAFE;
        }

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long startOfToday = cal.getTimeInMillis();

        cal.add(Calendar.DAY_OF_YEAR, 1);
        long startOfTomorrow = cal.getTimeInMillis();

        cal.add(Calendar.DAY_OF_YEAR, 1);
        long startOfDayAfterTomorrow = cal.getTimeInMillis();

        cal.add(Calendar.DAY_OF_YEAR, 2);
        long soonThreshold = cal.getTimeInMillis();

        if (expiryDateMs < startOfToday) {
            return EXPIRED;
        } else if (expiryDateMs < startOfTomorrow) {
            return TODAY;
        } else if (expiryDateMs < startOfDayAfterTomorrow) {
            return TOMORROW;
        } else if (expiryDateMs < soonThreshold) {
            return SOON;
        } else {
            return SAFE;
        }
    }
}
