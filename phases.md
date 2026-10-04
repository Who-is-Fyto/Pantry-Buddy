# PantryBuddy — Implementation Phases & Git Roadmap

> **15-Phase Incremental Development Plan for PantryBuddy (Home Buddy Smart Pantry)**  
> Each phase represents a modular, testable, and committable milestone designed to ensure continuous integration, full course rubric compliance, and clean Git history.

---

## Phase Roadmap Overview

| Phase | Phase Name | Focus Area | Status | Target Git Commit Message |
| :---: | :--- | :--- | :---: | :--- |
| **Phase 1** | **Dependencies & Build Config** | Gradle, Room, M3, Glide, WorkManager | Done | `configured gradle dependencies and compatibility` |
| **Phase 2** | **Figma Design System & Themes** | Colors, Lora & Inter fonts, styles, shapes | Done | `added figma colors, fonts, drawables and themes` |
| **Phase 3** | **Database Schema & Room Entities** | `PantryItem`, `Recipe`, `RecipeIngredient`, DAOs | Done | `added room entities and daos for pantry items and recipes` |
| **Phase 4** | **18 Pre-Seeded Recipes & Callback** | JSON asset, DB seeding callback, staples | Done | `added 18 seeded recipes and database prepopulate callback` |
| **Phase 5** | **Normalization & Unit Conversion** | `IngredientNormalizer`, `UnitConverter`, Unit Tests | Done | `created ingredient normalizer and unit converter with tests` |
| **Phase 6** | **Strict Matcher & "Almost There"** | 100% strict matching algorithm & isolated tier | Done | `implemented strict matching algorithm and almost there logic` |
| **Phase 7** | **Repository & ViewModel Architecture**| Repositories, LiveData, ViewModels | Done | `added repositories and viewmodels for pantry and recipes` |
| **Phase 8** | **Navigation Shell & Home Dashboard** | Bottom navigation bar, `HomeFragment` | Done | `setup bottom navigation and home dashboard screen` |
| **Phase 9** | **Pantry Inventory List** | `RecyclerView`, search, filter chips, expiry pills | Done | `created pantry inventory list with recyclerview and expiry pills` |
| **Phase 10**| **Pantry Item CRUD** | Add bottom sheet, item detail, edit & delete | Done | `implemented add, edit, and delete for pantry items` |
| **Phase 11**| **Suggested Recipes Screen** | Strict 100% feed & quarantined "Almost There" | Done | `built suggested recipes screen with strict and almost there lists` |
| **Phase 12**| **Zero-Match Feedback State** | Diagnostic empty state, stock audit, hints | Pending | `added empty state feedback screen when zero recipes match` |
| **Phase 13**| **Recipe Detail & 3-Column Check** | 3-column table, dynamic serving scaler | Pending | `built recipe detail screen with 3 column ingredient check` |
| **Phase 14**| **Cooking Guide & Stock Deduction** | Step timer, meal review, atomic inventory deduction | Pending | `created cooking guide with timer and meal review pantry deduction` |
| **Phase 15**| **Settings, Expiry Alerts & Audit** | Preferences, sample reset button, WorkManager | Pending | `added settings screen, expiry alerts, and sample data reset button` |

---

## Detailed Phase Specifications

---

### Phase 1: Dependencies & Build Configuration
- [x] **Objective:** Equip the Android project with all required libraries and build tool configurations.
- [x] **Deliverables:**
  - Update `app/build.gradle.kts`:
    - Material Design 3 (`com.google.android.material:material`)
    - AndroidX Room (`androidx.room:room-runtime`, `androidx.room:room-compiler`)
    - AndroidX Lifecycle (`androidx.lifecycle:lifecycle-viewmodel`, `androidx.lifecycle:lifecycle-livedata`)
    - AndroidX Navigation Component (`androidx.navigation:navigation-fragment`, `androidx.navigation:navigation-ui`)
    - WorkManager (`androidx.work:work-runtime`)
    - Gson (`com.google.code.gson:gson`)
    - Glide (`com.github.bumptech.glide:glide`, `com.github.bumptech.glide:compiler`)
    - JUnit 4 & AndroidX Test runners for domain testing
- [x] **Verification:** Clean Gradle build and project sync with zero compilation warnings.
- [x] **Commit Command:**
  ```bash
  git add app/build.gradle.kts
  git commit -m "chore: configure Gradle dependencies and Java 11/17 compatibility"
  ```

---

### Phase 2: Figma Design System, Themes & Custom UI Tokens
- [x] **Objective:** Implement the visual foundation from the Figma canvas (`Node 0:1`).
- [x] **Deliverables:**
  - `res/values/colors.xml`: Base oatmeal (`#F7F2EA`), card surface (`#FFFCF7`), border stroke (`#EDE2D6`), espresso text (`#34251F`), taupe secondary (`#82746A`), sage green (`#4D6650` & `#E8EEDF`), terracotta action (`#795642`), caramel amber (`#95622E`), rose danger (`#D9534F`).
  - `res/font/`: Download and embed `Lora` (SemiBold) and `Inter` (Regular, SemiBold, Bold) font families.
  - `res/values/themes.xml`: Set Material 3 light theme using Figma palette and typography scales.
  - `res/drawable/`: Create background shapes with rounded corners (`20dp` cards, `12dp` buttons, `100dp` status pills) and subtle borders.
- [x] **Verification:** Visual preview of sample themed card and button in Android Studio layout editor.
- [x] **Commit Command:**
  ```bash
  git add app/src/main/res/
  git commit -m "feat(ui): implement Figma color tokens, typography, and component styles"
  ```

---

### Phase 3: Room Database Schema & Entities
- [x] **Objective:** Establish the local SQLite schema for pantry items, recipes, and recipe ingredients.
- [x] **Deliverables:**
  - `data/local/entity/PantryItem.java`: Entity with `itemId`, `name`, `normalizedName`, `quantity`, `unit`, `expiryDate`, `category`, `dateAdded`.
  - `data/local/entity/Recipe.java`: Entity with `recipeId`, `title`, `description`, `cookTimeMinutes`, `defaultServings`, `instructionsJson`, `difficulty`.
  - `data/local/entity/RecipeIngredient.java`: Entity with foreign key cascading from `Recipe`, `ingredientName`, `normalizedName`, `amountPerServing`, `unit`.
  - `domain/model/RecipeWithIngredients.java`: `@Relation` composite POJO.
  - `data/local/dao/PantryDao.java`: Query methods for reactive `LiveData<List<PantryItem>>`, insert, update, delete, and find.
  - `data/local/dao/RecipeDao.java`: Query methods for all recipes with ingredients.
  - `data/local/AppDatabase.java`: Room database definition.
- [x] **Verification:** Room compiles successfully without annotation processing or schema migration errors.
- [x] **Commit Command:**
  ```bash
  git add app/src/main/java/com/example/pantrybuddy/data/ app/src/main/java/com/example/pantrybuddy/domain/model/
  git commit -m "added room entities and daos for pantry items and recipes"
  ```

---

### Phase 4: Pre-Seeded Recipe Asset & Database Initializer
- [x] **Objective:** Pre-seed the local Room SQLite database on first app launch with **18 curated zero-waste recipes**.
- [x] **Deliverables:**
  - `app/src/main/assets/recipes_seed.json`: JSON catalog containing all 18 recipes specified in `DESIGN.md` Section 6, with itemized staples (salt, oil, pepper) and preparation instructions.
  - `data/local/AppDatabase.java`: Add `RoomDatabase.Callback` on `onCreate()` to parse `recipes_seed.json` with Gson and insert all recipes and ingredients in an asynchronous transaction.
- [x] **Verification:** Run app once on emulator; inspect SQLite with Database Inspector to verify all 18 recipes and their itemized ingredients exist.
- [x] **Commit Command:**
  ```bash
  git add app/src/main/assets/recipes_seed.json app/src/main/java/com/example/pantrybuddy/data/ app/src/test/
  git commit -m "added 18 seeded recipes and database prepopulate callback"
  ```

---

### Phase 5: Normalization Engine & Unit Conversion
- [x] **Objective:** Implement the real-world string normalizer and unit equivalence engine to prevent exact-string match penalties.
- [x] **Deliverables:**
  - `domain/engine/IngredientNormalizer.java`:
    - Trims whitespace, removes non-alphanumeric punctuation, converts to lower case.
    - Suffix stemming: `-es` (`tomatoes` $\to$ `tomato`), `-s` (`eggs` $\to$ `egg`, `onions` $\to$ `onion`), `-ies` (`strawberries` $\to$ `strawberry`).
    - Alias mapping: `olive oil` / `vegetable oil` $\to$ `oil`.
  - `domain/engine/UnitConverter.java`:
    - Mass: `g` $\leftrightarrow$ `kg`
    - Volume: `ml` $\leftrightarrow$ `l`, `tbsp`, `tsp`
    - Count: `pcs`, `piece`, `item`
  - `src/test/java/com/example/pantrybuddy/IngredientNormalizerTest.java`: JUnit test cases verifying singular/plural normalization and unit conversion accuracy.
- [x] **Verification:** All JUnit test cases pass cleanly in terminal (`./gradlew test`).
- [x] **Commit Command:**
  ```bash
  git add app/src/main/java/com/example/pantrybuddy/domain/engine/ app/src/test/ phases.md
  git commit -m "created ingredient normalizer and unit converter with tests"
  ```

---

### Phase 6: Strict Recipe Matching Engine & Isolated "Almost There" Logic
- [x] **Objective:** Build the core business logic enforcing the 100% strict matching rule, plus the segregated 1-missing bonus stretch.
- [x] **Deliverables:**
  - `domain/model/MatchResult.java`: Result model (`isCookable`, `missingIngredientCount`, `missingIngredientsList`, `maxServingsPossible`).
  - `domain/engine/StrictRecipeMatcher.java`:
    - Evaluates required ingredients against pantry items.
    - If `missingCount == 0` AND all quantities $\ge$ required: Recipe is qualified.
    - Disqualifies any partial recipe from the strict list.
    - Computes maximum servings possible based on limiting ingredient.
  - `domain/engine/AlmostThereMatcher.java`:
    - Analyzes non-qualifying recipes to isolate those missing **exactly 1 ingredient** (or short on 1 quantity).
  - `src/test/java/com/example/pantrybuddy/StrictRecipeMatcherTest.java`: Unit tests testing complete pantry, 1-missing pantry, and insufficient quantity scenarios.
- [x] **Verification:** Unit tests confirm 4 eggs out of 5 required rejects the recipe from strict suggestions; missing 1 ingredient flags into "Almost There".
- [x] **Commit Command:**
  ```bash
  git add app/src/main/java/com/example/pantrybuddy/domain/ app/src/test/ phases.md
  git commit -m "implemented strict matching algorithm and almost there logic"
  ```

---

### Phase 7: Repository & ViewModel Architecture
- [x] **Objective:** Connect the local Room database and matching engine to reactive Android Jetpack ViewModels.
- [x] **Deliverables:**
  - `data/repository/PantryRepository.java`: Manages pantry CRUD operations via background thread executors; exposes `LiveData<List<PantryItem>>`.
  - `data/repository/RecipeRepository.java`: Loads recipes with ingredients, runs `StrictRecipeMatcher` and `AlmostThereMatcher`, and exposes results.
  - `ui/pantry/PantryViewModel.java`: Exposes pantry items, active category filters, and search queries.
  - `ui/recipes/RecipeViewModel.java`: Exposes strictly cookable recipes, "Almost There" recipes, and zero-match state.
- [x] **Verification:** ViewModels successfully emit database state updates via LiveData observers.
- [x] **Commit Command:**
  ```bash
  git add app/src/main/java/com/example/pantrybuddy/data/repository/ app/src/main/java/com/example/pantrybuddy/ui/
  git commit -m "added repositories and viewmodels for pantry and recipes"
  ```

---

### Phase 8: App Navigation Shell & Home Dashboard
- [x] **Objective:** Build the main activity container, bottom navigation bar, and Figma Panel 1 (`HomeFragment`).
- [x] **Deliverables:**
  - `res/menu/bottom_nav_menu.xml`: 4 navigation tabs (`Home`, `Pantry`, `Recipes`, `Settings`).
  - `ui/MainActivity.java` and `res/layout/activity_main.xml`: Bottom navigation host container.
  - `ui/home/HomeFragment.java` and `res/layout/fragment_home.xml`:
    - Hero header ("Good morning, Maya", subtitle).
    - Pantry summary card ("X ingredients in your pantry · before cooking").
    - Urgent expiry alert card ("A little love, soon · items to use soon").
    - Featured cookable recipe preview card with quick link to recipe detail.
- [x] **Verification:** App launches into Home Dashboard; bottom navigation switches smoothly between tabs.
- [x] **Commit Command:**
  ```bash
  git add app/src/main/res/ app/src/main/java/com/example/pantrybuddy/ui/ phases.md
  git commit -m "setup bottom navigation and home dashboard screen"
  ```

---

### Phase 9: Pantry Inventory List Screen
- [x] **Objective:** Build Figma Panel 2 (`PantryFragment`) with a reactive `RecyclerView` showing all pantry items.
- [x] **Deliverables:**
  - `res/layout/fragment_pantry.xml`: Search input, category filter chips (`All`, `Vegetables`, `Eggs & Dairy`, `Staples`, `Spices`), and sort selector.
  - `res/layout/item_pantry_ingredient.xml`: Card design with item name, quantity, category badge, and dynamic expiration pill (`Use today` green, `Use tomorrow` amber, `Best before` neutral).
  - `ui/pantry/PantryAdapter.java`: `ListAdapter` with `DiffUtil` for smooth list animations.
  - Footer notice: *"Keep quantities up to date. We only suggest meals you can make entirely from this list."*
  - Floating Action Button (FAB) for adding ingredients.
- [x] **Verification:** Adding dummy items displays correctly in the list with corresponding freshness pill badges.
- [x] **Commit Command:**
  ```bash
  git add app/src/main/res/ app/src/main/java/com/example/pantrybuddy/ui/pantry/ phases.md
  git commit -m "created pantry inventory list with recyclerview and expiry pills"
  ```

---

### Phase 10: Pantry Item Management (Add, Edit, Delete CRUD)
- [x] **Objective:** Complete full CRUD functionality for pantry items (Figma Panels 3 & 4).
- [x] **Deliverables:**
  - `ui/pantry/AddIngredientBottomSheet.java` and `res/layout/bottom_sheet_add_ingredient.xml`:
    - Autocomplete ingredient name input.
    - Numeric quantity input with unit chip selector (`g`, `ml`, `pcs`, `tbsp`, `tsp`).
    - Expiry date picker with DatePickerDialog.
    - Category dropdown.
    - Explanatory note: *"Small ingredients count, too. Add oil, salt and spices separately."*
  - `ui/pantry/IngredientDetailActivity.java` and `res/layout/activity_ingredient_detail.xml`:
    - Edit existing quantity, unit, or expiry date.
    - Cross-reference banner: *"Your tomatoes are included in X recipes you can make right now."*
    - Delete button with confirmation dialog.
- [x] **Verification:** Add an ingredient, view it in the inventory, edit its quantity, and delete it. Database updates verified.
- [x] **Commit Command:**
  ```bash
  git add app/src/main/res/ app/src/main/java/com/example/pantrybuddy/ui/pantry/ phases.md
  git commit -m "implemented add, edit, and delete for pantry items"
  ```

---

### Phase 11: Suggested Recipes Screen & Isolated "Almost There" Tier
- [x] **Objective:** Build Figma Panel 5 (`RecipesFragment`) displaying strictly 100% cookable recipes, plus the quarantined 1-missing section.
- [x] **Deliverables:**
  - `res/layout/fragment_recipes.xml`: Header banner (*"Dinner is already here • You have everything for these recipes"*), filter tags, and dual-section list.
  - `res/layout/item_recipe_card.xml`: Recipe card with image thumbnail, title, cook time, servings, badge *"6 of 6 ingredients at home"*, and tag *"Uses spinach today"*.
  - `res/layout/item_almost_there_card.xml`: Distinct card with amber banner:  *"Missing 1 Ingredient: 10 ml Olive Oil"*.
  - `ui/recipes/RecipesAdapter.java`: Multi-view type adapter rendering strictly cookable cards and clearly segregated "Almost There" cards.
- [x] **Verification:** Strict list contains only 100% matched recipes; missing 1 item falls exclusively into the lower "Almost There" container.
- [x] **Commit Command:**
  ```bash
  git add app/src/main/res/ app/src/main/java/com/example/pantrybuddy/ui/recipes/ app/src/test/ phases.md
  git commit -m "built suggested recipes screen with strict and almost there lists"
  ```

---

### Phase 12: Zero-Match Feedback State & Pantry Diagnostics
- [ ] **Objective:** Build Figma Panel 10 (`NoMatchesState`) to provide constructive, honest feedback when zero recipes match the current pantry.
- [ ] **Deliverables:**
  - `res/layout/view_zero_matches.xml`:
    - Empathetic heading: *"Dinner needs a little more love."*
    - Explanatory copy: *"None of our recipes can be made with your remaining amounts. We won't show meals that need something you don't have."*
    - Active pantry stock summary card (reassuring user their items are tracked).
    - Unlocking recommendation chip (e.g. *"Adding 4 eggs or 100 g rice would unlock 3 recipes with your current vegetables"*).
    - Button: *"Add an ingredient"* linking directly to Add dialog.
  - Integrate zero-match view into `RecipesFragment` when `cookableRecipes.isEmpty()`.
- [ ] **Verification:** Clear pantry or set quantities to 0; verify the warm diagnostic empty state appears instead of a blank screen.
- [ ] **Commit Command:**
  ```bash
  git add app/src/main/res/ app/src/main/java/com/example/pantrybuddy/ui/recipes/
  git commit -m "feat(recipes): implement zero-match explanatory screen and unlock hints"
  ```

---

### Phase 13: Recipe Detail & 3-Column Verification Table
- [ ] **Objective:** Build Figma Panel 6 (`RecipeDetailActivity`) with dynamic serving scaler and 3-column ingredient audit.
- [ ] **Deliverables:**
  - `res/layout/activity_recipe_detail.xml`:
    - Recipe image hero banner, title, cook time, tags.
    - Dynamic serving counter (+ / - buttons) with indicator: *"2 servings (Maximum with your 4 eggs)"*.
    - **3-Column Verification Table**:
      - Column 1: `INGREDIENT`
      - Column 2: `NEEDED` (scaled to selected servings)
      - Column 3: `AT HOME` (current pantry amount)
    - Post-cooking projection: *"Quantities leave 100 g tomatoes and 20 g spinach for later."*
    - Primary CTA: *"Start cooking · 2 servings"*.
  - `ui/recipes/RecipeDetailActivity.java`: Logic for scaling ingredient quantities and updating the 3-column table dynamically.
- [ ] **Verification:** Changing servings dynamically multiplies `NEEDED` quantities; if needed exceeds at-home, warning indicator displays.
- [ ] **Commit Command:**
  ```bash
  git add app/src/main/res/ app/src/main/java/com/example/pantrybuddy/ui/recipes/
  git commit -m "feat(recipes): create Recipe Detail with 3-column ingredient verification"
  ```

---

### Phase 14: Step-by-Step Cooking Guide & Post-Cooking Inventory Deduction
- [ ] **Objective:** Build Figma Panels 7, 8, and 9 for guided cooking, meal completion review, and atomic pantry inventory deduction.
- [ ] **Deliverables:**
  - `ui/cooking/CookingActivity.java` and `res/layout/activity_cooking.xml`:
    - Step-by-step progress indicator (*"Step 2 of 3 • About 8 min left"*).
    - Technique description with highlighted ingredient measurements.
    - Built-in interactive countdown timer (`03:00`) with Play/Pause and sound/vibration notification.
  - `ui/cooking/MealReviewActivity.java` and `res/layout/activity_meal_review.xml`:
    - Table displaying: `INGREDIENT` \| `BEFORE` \| `ACTUALLY USED` (editable) \| `WILL REMAIN`.
    - Verification checkbox: *"I checked these amounts. They reflect what I actually used, including oil, salt and pepper."*
    - CTA: *"Confirm quantities & update pantry"*.
  - `ui/cooking/PantryUpdateSuccessActivity.java`:
    - Shows inventory delta (e.g. `Eggs: 4 → 0 pcs; Tomatoes: 300 → 100 g`).
    - Executes `@Transaction` atomic stock reduction in `PantryDao`.
- [ ] **Verification:** Complete a cooking session; verify pantry stock is deducted accurately and recipes are re-evaluated.
- [ ] **Commit Command:**
  ```bash
  git add app/src/main/res/ app/src/main/java/com/example/pantrybuddy/ui/cooking/
  git commit -m "feat(cooking): build guided cooking mode and atomic pantry deduction"
  ```

---

### Phase 15: Settings Screen, Expiry Alerts & Marker Verification Suite
- [ ] **Objective:** Build Figma Panel 13 (`SettingsFragment`), WorkManager background expiry reminders, and marker testing reset button.
- [ ] **Deliverables:**
  - `ui/settings/SettingsFragment.java` and `res/layout/fragment_settings.xml`:
    - Expiry push alert toggle switch (9:00 AM & 5:00 PM).
    - Unit preference radio group (`Metric: g, ml` vs `Imperial: oz, fl oz`).
    - Anti-waste threshold selector (1 day, 2 days, 3 days).
    - **"Reset Sample Data" marker demo button**: Populates pantry with default Figma scenario (4 eggs, 300g tomatoes, 100g spinach, 40ml olive oil, 10g salt, 6g black pepper).
    - Privacy guarantee card: *"No Location or GPS used. No network tracking. Local SQLite database."*
  - `utils/NotificationHelper.java` & `utils/ExpiryReminderWorker.java`: Periodic WorkManager task to trigger expiry notification.
  - `AndroidManifest.xml` verification: Audit to confirm zero location permissions.
- [ ] **Verification:** Tap "Reset Sample Data"; verify pantry resets instantly to sample items and Tomato & spinach scramble is suggested.
- [ ] **Commit Command:**
  ```bash
  git add app/src/main/res/ app/src/main/java/com/example/pantrybuddy/ui/settings/ app/src/main/java/com/example/pantrybuddy/utils/
  git commit -m "feat(settings): add Settings screen, sample data reset, and expiry alerts"
  ```

---

## Recommended Git Workflow for Each Phase

For every phase:
1. **Implement:** Write and test the Java classes, layout XMLs, or drawables specified in that phase.
2. **Verify:** Run `./gradlew compileDebugSources` or the relevant JUnit/instrumentation test.
3. **Stage:** `git add <files-modified-in-phase>`
4. **Commit:** Use the standardized commit message designated for that phase:
   ```bash
   git commit -m "<designated commit message>"
   ```
5. **Push:** `git push origin master` (or your remote branch).
6. **Update `phases.md`:** Check off `[x]` the completed phase to keep your progress visible!
