# PantryBuddy (Home Buddy Smart Pantry)

> **A Java Android application that suggests recipes based *strictly* on leftover ingredients to eradicate food waste.**

[![Platform](https://img.shields.io/badge/Platform-Android%20(Java)-green.svg)](https://developer.android.com)
[![Language](https://img.shields.io/badge/Language-Java%2011%20%2F%2017-orange.svg)](https://www.oracle.com/java/)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-24%20(Android%207.0)-blue.svg)](https://developer.android.com)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-37-blue.svg)](https://developer.android.com)
[![Design](https://img.shields.io/badge/Figma-Home%20Buddy%20Smart%20Pantry-purple.svg)](https://www.figma.com/design/4spg0re3UtponEqYMZZxdr/Home-Buddy-Smart-Pantry?node-id=0-1)
[![Privacy](https://img.shields.io/badge/Privacy-Zero%20Location%20%2F%20No%20Maps-brightgreen.svg)](#-privacy--zero-location-guarantee)

---

## The Core Value: The Strict-Matching Rule

Most cooking applications ask what ingredients you have, then recommend a recipe that still requires a trip to the supermarket for 3 missing items.

**PantryBuddy is different.** The foundational value of this application is its **Strict-Matching Business Rule**:
* **100% On-Hand Guarantee:** A recipe is suggested **only** if the user already has every single required ingredient in sufficient quantity at home.
* **No Hidden Assumptions:** Small pantry staples—such as cooking oil, table salt, black pepper, butter, and seasonings—are **never** assumed to be in your cupboard. They are explicitly tracked in your pantry inventory. If a recipe needs 10 ml of olive oil and you have 0 ml, the recipe is disqualified.
* **No Partial Matches in Main Feed:** If a recipe needs 5 ingredients and you have 4, it will **never** appear in the suggested recipes list.
* **Zero Food Waste:** By prioritizing ingredients closest to their expiration date, you cook what needs using *before* it spoils.
* **No Unplanned Shopping Trips:** Dinner is already in your kitchen.

---

## Course Rubric & Brief Compliance Checklist

| Required Feature | Brief Specification | PantryBuddy Implementation |
| :--- | :--- | :--- |
| **Pantry Management** | Add, edit, delete pantry items (name, quantity, unit, optional expiry). | Full database CRUD backed by Room SQLite, supporting decimal quantities and expiration tracking. |
| **Pantry List Screen** | `RecyclerView` bound to local database. | Fast, reactive `RecyclerView` with real-time expiry pill badges (`Use today`, `Use tomorrow`). |
| **Recipe Collection** | Set of at least 15–20 recipes pre-loaded/seeded on first run. | **18 curated zero-waste recipes** pre-seeded via SQLite Room callback, itemized down to basic staples. |
| **Strict-Matching Logic** | Only show recipes where 100% of ingredients are present in required quantity. | Domain engine (`StrictRecipeMatcher`) enforcing zero-assumption matching on the primary suggestions feed. |
| **Recipe Detail Screen** | Full ingredient list and preparation method. | 3-column verification table (`INGREDIENT` \| `NEEDED` \| `AT HOME`) and dynamic serving scaler. |
| **Settings / Profile Screen** | Toggle for expiry alerts, units preference, etc. | Dedicated `SettingsFragment` with notification toggles, metric/imperial unit switcher, and **"Reset Sample Data" marker button**. |
| **Zero-Match Feedback** | Friendly UI when 0 recipes match (*"No recipes match your pantry yet"*). | `NoMatchesState` screen with stock diagnostic review and recommendation of what single item unlocks meals. |
| **Bonus Stretch: "Almost There"** | Recipes missing only 1 ingredient for bonus credit. | Clearly separated secondary section with amber tags (*"Missing: 10 ml Olive Oil"*), strictly quarantined from main list. |
| **Robust Matching** | Resilient against singular/plural and unit variations. | `IngredientNormalizer` handles `-s`, `-es`, `-ies`, irregular plurals, and unit conversions (`g` ↔ `kg`, `ml` ↔ `l`). |
| **Strict Restriction** | **NO Google Maps, NO mapping SDKs, NO GPS/Location.** | Zero location permissions in `AndroidManifest.xml` and zero mapping SDK dependencies. |

---

## The 13 Screen Panels (From Figma Specification)

PantryBuddy implements the visual identity and user flows designed in [Figma Prototype](https://www.figma.com/design/4spg0re3UtponEqYMZZxdr/Home-Buddy-Smart-Pantry?node-id=0-1):

| # | Screen / Panel | Class Name | Function & Key Elements |
| :-: | :--- | :--- | :--- |
| **1** | **Home Dashboard** | `HomeFragment` | Daily greeting, pantry count, urgent expiring item nudges, and featured cookable recipe teaser. |
| **2** | **Pantry Inventory** | `PantryFragment` | Searchable `RecyclerView` categorized with real-time expiration pills (`Use today`, `4 days left`, `Best before`). |
| **3** | **Add Ingredient** | `AddIngredientActivity` | Form with autocomplete, numeric decimal quantity, unit chips (`g`, `ml`, `pcs`), category picker, and DatePicker. |
| **4** | **Ingredient Detail & Edit** | `IngredientDetailActivity` | Detailed item card showing which recipes it participates in, with inline quantity adjustment and delete option. |
| **5** | **Suggested Recipes** | `RecipesFragment` | Strict feed of 100% cookable meals with badge *"6 of 6 ingredients at home"*, plus separated "Almost There" bonus section. |
| **6** | **Recipe Detail & Check** | `RecipeDetailActivity` | Complete recipe breakdown with **3-column verification table** (`INGREDIENT` \| `NEEDED` \| `AT HOME`) and serving scaler. |
| **7** | **Step-by-Step Cooking** | `CookingActivity` | Fullscreen distraction-free cooking assistant with built-in interactive countdown timers and sound/vibrate alerts. |
| **8** | **Meal Completion Review** | `MealReviewActivity` | Post-cooking verification table showing exact quantities used before any pantry deduction occurs. |
| **9** | **Pantry Update Success** | `PantryUpdateSuccessActivity` | Confirmation of inventory delta (e.g. `Eggs: 4 → 0 pcs`), triggering real-time re-evaluation of matches. |
| **10**| **Zero-Match Feedback** | `NoMatchesState` | Warm, diagnostic empty state explaining why no recipes match remaining stock, prompting a pantry audit. |
| **11**| **Pantry Filter Sheet** | `PantryFilterBottomSheet` | Drawer to filter pantry by Category, "Use soon only" toggle, and expiry sorting. |
| **12**| **Expiry Reminder Sheet**| `ExpiryReminderBottomSheet`| Contextual alert showing ingredients needing attention, with a 5 PM snooze reminder option. |
| **13**| **Settings & Preferences**| `SettingsFragment` | Expiry alert toggles, unit switcher (`Metric` vs `Imperial`), and **"Reset Sample Data" marker demo button**. |

---

## Visual Design Tokens

- **Canvas Background (`#F7F2EA`):** Warm oatmeal / cream background.
- **Card Surfaces (`#FFFCF7`):** Soft ivory elevated containers with subtle `#EDE2D6` warm borders.
- **Primary Text (`#34251F`):** Deep espresso roast for high-contrast legibility.
- **Muted Text (`#82746A`):** Warm taupe for secondary descriptions and quantities.
- **Accent Green (`#4D6650` & `#E8EEDF`):** Sage forest green for freshness pills, "In your pantry" tags, and badges.
- **Action Clay (`#795642`):** Warm terracotta for primary buttons and touch points.
- **Warning Amber (`#95622E`):** Caramel for items expiring tomorrow and "Almost There" tags.
- **Typography:**
  - **Headings:** `Lora` (Serif) for warmth and culinary craft.
  - **Interface & Data:** `Inter` (Sans-Serif) for crisp, readable numbers and labels.

---

## Pre-Loaded Seed Recipes (18 Catalog Items)

PantryBuddy pre-seeds 18 realistic leftover-focused recipes directly into the local SQLite database on first launch:

1. **Tomato & Spinach Scramble** (Eggs, tomatoes, spinach, olive oil, salt, pepper)
2. **Classic Vegetable Frittata** (Eggs, potatoes, onions, cooking oil, salt, pepper)
3. **Garlic & Egg Fried Rice** (Cooked rice, eggs, garlic, cooking oil, soy sauce, salt)
4. **Rustic Potato & Onion Hash** (Potatoes, onions, cooking oil, salt, pepper)
5. **Mediterranean Chickpea Salad** (Chickpeas, tomatoes, cucumber, olive oil, lemon juice, salt)
6. **Simple Garlic Aglio e Olio Pasta** (Spaghetti, garlic, olive oil, chili flakes, salt)
7. **Creamy Lentil & Spinach Dhal** (Red lentils, spinach, onion, garlic, cooking oil, salt, curry powder)
8. **Cheese & Herb Omelette** (Eggs, cheddar cheese, butter, salt, pepper)
9. **Crispy Pan-Roasted Rosemary Potatoes** (Potatoes, olive oil, garlic, salt, dried rosemary)
10. **Quick Tomato & Cannellini Bean Stew** (White beans, tomatoes, onion, garlic, olive oil, salt)
11. **Stir-Fried Egg Noodles with Vegetables** (Egg noodles, cabbage, carrot, cooking oil, soy sauce, pepper)
12. **French Onion Melt Toast** (Bread, onions, cheese, butter, salt)
13. **Curried Fried Egg with Steamed Rice** (Eggs, cooked rice, cooking oil, curry powder, salt)
14. **Sautéed Garlic Spinach with Butter** (Spinach, garlic, butter, salt, pepper)
15. **Mushroom & Garlic Pan Sauté** (Mushrooms, garlic, olive oil, butter, salt, pepper)
16. **Sweet Cinnamon French Toast** (Bread, eggs, milk, butter, cinnamon, sugar)
17. **Zesty Tuna & Sweetcorn Salad** (Canned tuna, sweetcorn, mayonnaise, lemon juice, salt, pepper)
18. **Shakshuka-Style Poached Eggs in Tomato Sauce** (Eggs, tomatoes, onion, garlic, olive oil, salt, cumin)

*Every recipe itemizes all required ingredients down to salt, cooking fat, and spices to guarantee no unverified assumptions.*

---

## The Normalization & Matching Engine

The core business logic is encapsulated in `com.example.pantrybuddy.domain.engine`:

1. **Singular / Plural Stemming (`IngredientNormalizer`):**
   - Matches `"tomato"` to `"tomatoes"`, `"egg"` to `"eggs"`, `"potato"` to `"potatoes"`.
   - Handles irregular forms (`leaves` $\to$ `leaf`) and strips incidental descriptors (`fresh`, `diced`, `extra virgin`).
2. **Unit Conversion Matrix (`UnitConverter`):**
   - Mass: grams (`g`) $\leftrightarrow$ kilograms (`kg`).
   - Volume: milliliters (`ml`) $\leftrightarrow$ liters (`l`), tablespoons (`tbsp`), teaspoons (`tsp`).
   - Count: pieces (`pcs`).
3. **Strict Quarantine for "Almost There":**
   - The primary feed only displays recipes where every ingredient is present in sufficient quantity.
   - Recipes missing exactly 1 ingredient are isolated in the secondary "Almost There" section, preventing accidental false recommendations.

---

## ️ Technology Stack

| Layer | Component | Version / Library | Purpose |
| :--- | :--- | :--- | :--- |
| **Language** | **Java** | OpenJDK 11 / 17 | Core programming language. |
| **Platform** | **Android SDK** | `minSdk = 24`, `targetSdk = 37` | Wide device compatibility. |
| **UI Framework** | **Material Design 3 (M3)** | `com.google.android.material:material` | Clean cards, bottom sheets, and chips. |
| **Architecture** | **MVVM + Repository** | Android Jetpack Lifecycle (`ViewModel`, `LiveData`) | Separation of concerns and testability. |
| **Local Database**| **Room (SQLite)** | `androidx.room:room-runtime:2.6.x` | ACID-compliant ORM with reactive queries. |
| **JSON Parser** | **Gson** | `com.google.code.gson:gson:2.11.x` | Seed asset deserialization. |
| **Image Loading**| **Glide** | `com.github.bumptech.glide:glide:4.16.x` | Efficient bitmap rendering and caching. |
| **Background** | **WorkManager** | `androidx.work:work-runtime:2.9.x` | Persistent expiry reminder notifications. |

---

## Privacy & Zero-Location Guarantee

In strict compliance with the project brief:
- **No Google Maps SDK:** Zero map libraries are imported.
- **Zero Location Permissions:** `ACCESS_FINE_LOCATION` and `ACCESS_COARSE_LOCATION` are omitted from `AndroidManifest.xml`.
- **Offline & Private:** All pantry data and recipe matching execute strictly on your device.

---

## Marker Testing & Demonstration Guide

To quickly verify and grade the application against all rubric requirements:

1. **Open Settings** $\to$ Tap **"Reset Sample Data"**.
   - Populates pantry with Figma scenario: 4 eggs, 300g tomatoes, 100g spinach, 40ml olive oil, 10g salt, 6g black pepper.
2. **Open Suggested Recipes:**
   - **Tomato & spinach scramble** appears as 100% cookable (6 of 6 ingredients present).
3. **Test Strict Disqualification:**
   - Open Pantry $\to$ Edit Eggs $\to$ Change quantity from 4 to **1 pc**.
   - Return to Recipes: **Tomato & spinach scramble disappears from suggested recipes**.
4. **Test "Almost There" Bonus Stretch:**
   - Scroll down to the clearly segregated "Almost There" section:
   - Tomato & spinach scramble now appears there with an amber badge: *"Missing 3 Eggs"*.
5. **Test Plural / Singular Robustness:**
   - Add ingredient named `"tomato"` (singular) or `"tomatoes"` (plural). The app treats them as identical.
6. **Test Zero-Match Feedback:**
   - Delete all pantry items.
   - Screen renders a friendly empty-state screen (*"No recipes match your pantry yet — add more ingredients"*), with recommendations on what staples to add.

---

## Getting Started

### Prerequisites
- **Android Studio** (Ladybug, Hedgehog, or newer)
- **Android SDK Platform 37** (or 34+)
- **JDK 11** or **JDK 17** configured in Android Studio

### Opening and Running the Project
1. Open Android Studio.
2. Select **Open** and choose:
   ```
   /home/michael/AndroidStudioProjects/PantryBuddy
   ```
3. Wait for Gradle sync to complete.
4. Select an emulator or connected device and press **Run** (<kbd>Shift</kbd> + <kbd>F10</kbd>).

---

## Design Attribution
Designed in Figma: [Home Buddy Smart Pantry Prototype](https://www.figma.com/design/4spg0re3UtponEqYMZZxdr/Home-Buddy-Smart-Pantry?node-id=0-1) by Michael.
