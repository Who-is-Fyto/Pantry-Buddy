package com.example.pantrybuddy;

import static org.junit.Assert.assertEquals;

import com.example.pantrybuddy.domain.model.ExpiryStatus;

import org.junit.Test;

import java.util.Calendar;

public class ExpiryStatusTest {

    @Test
    public void testExpiryStatus_nullIsSafe() {
        assertEquals(ExpiryStatus.SAFE, ExpiryStatus.fromTimestamp(null));
    }

    @Test
    public void testExpiryStatus_expiredYesterday() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        assertEquals(ExpiryStatus.EXPIRED, ExpiryStatus.fromTimestamp(cal.getTimeInMillis()));
    }

    @Test
    public void testExpiryStatus_expiresToday() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        assertEquals(ExpiryStatus.TODAY, ExpiryStatus.fromTimestamp(cal.getTimeInMillis()));
    }

    @Test
    public void testExpiryStatus_expiresTomorrow() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 1);
        cal.set(Calendar.HOUR_OF_DAY, 12);
        assertEquals(ExpiryStatus.TOMORROW, ExpiryStatus.fromTimestamp(cal.getTimeInMillis()));
    }

    @Test
    public void testExpiryStatus_expiresSoon() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 2);
        cal.set(Calendar.HOUR_OF_DAY, 12);
        assertEquals(ExpiryStatus.SOON, ExpiryStatus.fromTimestamp(cal.getTimeInMillis()));
    }

    @Test
    public void testExpiryStatus_expiresFarFutureIsSafe() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 10);
        assertEquals(ExpiryStatus.SAFE, ExpiryStatus.fromTimestamp(cal.getTimeInMillis()));
    }
}
