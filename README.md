# PantryBuddy

A smart zero-waste pantry tracker and recipe suggester for Android, built in Java.

Frontend Designed on Figma: [Home Buddy Smart Pantry](https://www.figma.com/design/4spg0re3UtponEqYMZZxdr/Home-Buddy-Smart-Pantry?node-id=0-1)

---

## What is PantryBuddy?

Most recipe apps recommend meals that still require you to run to the grocery store for 2 or 3 missing items. 

PantryBuddy works the other way around: you track what you currently have at home, and the app only suggests meals you can cook **right now** with 100% of your ingredients on hand.

### Core Rules

- **Zero-Waste Matching**: Recipes only appear if you have every required ingredient in sufficient quantity.
- **No Assumed Staples**: Common kitchen items like salt, pepper, butter, and cooking oil are not assumed to be in your cupboard. They must be in your pantry inventory. If you have 0 ml of oil, an oil-based recipe will not be suggested.
- **Expiry Priority**: Ingredients expiring today or tomorrow are highlighted so you can cook them before they spoil.
- **"Almost There" Bonus Tier**: If you are missing exactly 1 item (e.g. 10 ml olive oil or 1 egg), it gets listed in a separate secondary section so you know what single item unlocks that meal.
- **Smart Units and Plurals**: Recognizes singular and plural forms (tomato vs tomatoes, eggs vs egg) and handles unit conversions (grams to kg, ml to liters and tablespoons).
- **Private and Offline**: Runs locally using SQLite with Room. No tracking, no location/GPS permissions, and no external account required.

---

## Tech Stack

- **Language**: Java 17
- **Platform**: Android SDK (minSdk 24, targetSdk 37)
- **Architecture**: MVVM (Model-View-ViewModel) with Repository pattern
- **Database**: Android Room (SQLite) with pre-seeded recipe database
- **UI Framework**: Material Design 3 (M3) with XML layouts and ViewBinding
- **Navigation**: Android Jetpack Navigation Component with BottomNavigationView
- **Reactive Data**: Android LiveData & MediatorLiveData
- **JSON Serialization**: Google Gson
- **Image Loading**: Bumptech Glide
- **Background Tasks**: AndroidX WorkManager (scheduled expiry alerts)
- **Unit Testing**: JUnit 4

---

## App Screens & Flows

- **Home Dashboard**: Shows total pantry items, urgent items expiring today or tomorrow, and a featured recipe ready to cook.
- **Pantry Inventory**: Searchable list with category filter chips (Vegetables, Dairy, Staples, etc.), sorting, and color-coded freshness badges (Use today, Use tomorrow, Best before).
- **Add & Edit Ingredient**: Forms with quantity inputs, unit selectors (g, ml, pcs, tbsp, tsp), and expiry dates.
- **Suggested Recipes**: Split into 100% strictly cookable meals and the isolated "Almost There" 1-missing tier.
- **Recipe Detail & Ingredient Check**: Shows recipe instructions, cook times, and a 3-column verification table (Ingredient | Needed | At Home).
- **Step-by-Step Cooking**: Interactive cooking guide with built-in step countdown timers.
- **Meal Review & Stock Deduction**: Review what was actually used before deducting quantities from your pantry.
- **Settings & Testing**: Measurement unit switcher, notification preferences, and a "Reset Sample Data" button for testing.

---

## Testing & Demonstration Guide

To test the matching logic and see how the app behaves:

1. **Load Demo Pantry**:
   - Go to **Settings** and tap **Reset Sample Data**.
   - This sets up a sample pantry: 4 eggs, 300g tomatoes, 100g spinach, 40ml olive oil, 10g salt, 6g black pepper.
2. **Check Cookable Recipes**:
   - Go to the **Recipes** tab: "Tomato & Spinach Scramble" appears in the ready-to-cook list (all 6 ingredients present).
3. **Test Strict Disqualification**:
   - Go to **Pantry**, find Eggs, and edit the quantity from 4 down to 1.
   - Return to **Recipes**: The scramble disappears from the cookable feed.
4. **Test "Almost There" Section**:
   - Scroll down to the "Almost There" list: the scramble now appears there with a badge showing you are missing 3 eggs.
5. **Test Plurals & Units**:
   - Add an ingredient named "tomato" or "tomatoes" in grams or pieces; the normalizer recognizes them as the same item.
6. **Test Zero-Match State**:
   - Clear all items in the pantry; the app shows a helpful empty state explaining no recipes can be made and suggests common staples to add.

---

## How to Build and Run

1. Clone this repository to your machine.
2. Open the project in **Android Studio** (Ladybug or newer recommended).
3. Let Gradle finish syncing dependencies.
4. Run the project on an Android emulator (API 24+) or a physical device:
   - Click the green **Run** button or press `Shift + F10`.

To run the unit tests from the terminal:
```bash
./gradlew testDebugUnitTest
```

---

## Project Info

- **Course Project**: Mobile Application Development
- **Design**: Frontend Designed on Figma
