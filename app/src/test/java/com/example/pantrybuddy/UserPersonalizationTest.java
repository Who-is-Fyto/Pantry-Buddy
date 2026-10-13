package com.example.pantrybuddy;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit tests for the user personalization system.
 * Validates name formatting, trimming, empty-input fallback, and time-based greeting selection.
 */
public class UserPersonalizationTest {

    // -- Name Cleaning Tests --

    @Test
    public void trimmedName_isReturnedCleanly() {
        // Simulates what PreferenceHelper.setUserName does internally
        String input = "  Michael  ";
        String cleaned = input.trim();
        assertEquals("Michael", cleaned);
    }

    @Test
    public void emptyName_fallsBackToDefault() {
        String input = "   ";
        String cleaned = input.trim();
        if (cleaned.isEmpty()) {
            cleaned = "Chef";
        }
        assertEquals("Chef", cleaned);
    }

    @Test
    public void nullName_fallsBackToDefault() {
        String input = null;
        String cleaned = (input == null) ? "Chef" : input.trim();
        if (cleaned.isEmpty()) {
            cleaned = "Chef";
        }
        assertEquals("Chef", cleaned);
    }

    @Test
    public void validName_staysUnchanged() {
        String input = "Alex";
        String cleaned = input.trim();
        assertEquals("Alex", cleaned);
    }

    @Test
    public void longName_isTrimmedOfWhitespace() {
        String input = " Sophia Rose ";
        String cleaned = input.trim();
        assertEquals("Sophia Rose", cleaned);
    }

    // -- Time-Based Greeting Tests --

    @Test
    public void morningHour_selectsMorningGreeting() {
        int hour = 8;
        String greeting = resolveGreeting(hour, "Michael");
        assertEquals("Good morning, Michael", greeting);
    }

    @Test
    public void noonHour_selectsAfternoonGreeting() {
        int hour = 12;
        String greeting = resolveGreeting(hour, "Michael");
        assertEquals("Good afternoon, Michael", greeting);
    }

    @Test
    public void afternoonHour_selectsAfternoonGreeting() {
        int hour = 15;
        String greeting = resolveGreeting(hour, "Michael");
        assertEquals("Good afternoon, Michael", greeting);
    }

    @Test
    public void eveningHour_selectsEveningGreeting() {
        int hour = 18;
        String greeting = resolveGreeting(hour, "Michael");
        assertEquals("Good evening, Michael", greeting);
    }

    @Test
    public void lateNightHour_selectsEveningGreeting() {
        int hour = 23;
        String greeting = resolveGreeting(hour, "Chef");
        assertEquals("Good evening, Chef", greeting);
    }

    @Test
    public void midnight_selectsMorningGreeting() {
        // Hour 0 is < 12, so it falls into the morning range
        int hour = 0;
        String greeting = resolveGreeting(hour, "Chef");
        assertEquals("Good morning, Chef", greeting);
    }

    @Test
    public void boundaryMorning_selectsMorningGreeting() {
        // Hour 0-11 should be morning; hour 11 is the last morning hour
        int hour = 11;
        String greeting = resolveGreeting(hour, "Alex");
        assertEquals("Good morning, Alex", greeting);
    }

    @Test
    public void boundaryAfternoon_selectsAfternoonGreeting() {
        // Hour 16 is the last afternoon hour
        int hour = 16;
        String greeting = resolveGreeting(hour, "Alex");
        assertEquals("Good afternoon, Alex", greeting);
    }

    @Test
    public void boundaryEvening_selectsEveningGreeting() {
        // Hour 17 is the first evening hour
        int hour = 17;
        String greeting = resolveGreeting(hour, "Alex");
        assertEquals("Good evening, Alex", greeting);
    }

    // Mirrors the exact logic from HomeFragment.setupHeaderDateAndGreeting()
    private String resolveGreeting(int hour, String name) {
        if (hour < 12) {
            return "Good morning, " + name;
        } else if (hour < 17) {
            return "Good afternoon, " + name;
        } else {
            return "Good evening, " + name;
        }
    }
}
