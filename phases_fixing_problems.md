# PantryBuddy — Fixing-Problems Branch Roadmap

> **Enhancement, API Integration, and UI/Backend Solidification Plan**  
> Branch: `Fixing-Problems`  
> This roadmap builds upon the completed 15-phase MVP foundation to integrate external food APIs, barcode/QR code scanning, user personalization, and comprehensive UI/UX polish.

---

## Roadmap Overview

| Phase | Phase Name | Focus Area | Status | Target Git Commit Message |
| :---: | :--- | :--- | :---: | :--- |
| **Phase FP-1** | **User Personalization & Dynamic Profile** | Custom user profile name, reactive greetings, settings edit | Done | `added user personalization and dynamic dashboard greeting` |
| **Phase FP-2** | **UI/UX Solidification & Layout Audit** | Button listeners, screen fit, keyboard resize, dialogs | Done | `solidified ui layouts, button interactions, and responsive scroll views` |
| **Phase FP-2.5** | **Figma Design & Missing Features Audit** | Full screen-by-screen Figma audit and gap documentation | Done | `documented figma frontend design gaps and missing features backlog` |
| **Phase FP-3** | **QR & Barcode Scanning (Open Food Facts API)** | Camera barcode scanner, Open Food Facts REST client, auto-add | Pending | `implemented barcode qr scanning with open food facts api integration` |
| **Phase FP-4** | **Spoonacular Recipe API Integration** | Spoonacular REST client, remote recipes, image loading, offline cache | Pending | `integrated spoonacular api for dynamic recipes and offline cache` |
| **Phase FP-5** | **Backend Robustness, Error Handling & Tests** | Network error resilience, lint cleanup, unit test suite | Pending | `completed backend robustness, offline fallbacks, and test verification` |

---

## Detailed Phase Specifications

---

### Phase FP-1: User Personalization & Dynamic Profile Greeting

- [ ] **Objective:** Replace the hardcoded "Maya" greeting on the Home dashboard with a user personalization system persisted in local preferences.
- [ ] **Deliverables:**
  - `utils/PreferenceHelper.java`:
    - Add `KEY_USER_NAME` ("pref_user_name", default: "Chef" or "Friend").
    - Add `getUserName(Context context)` and `setUserName(Context context, String name)`.
  - `ui/settings/SettingsFragment.java` & `res/layout/fragment_settings.xml`:
    - Add a "User Profile & Personalization" card in Settings.
    - Editable text field or dialog to let the user update their display name.
    - Save action with validation (trim spaces, handle empty input with default name).
  - `ui/home/HomeFragment.java`:
    - Update `setupHeaderDateAndGreeting()` to fetch the personalized name dynamically from `PreferenceHelper`.
    - Time-based greetings:
      - 05:00 - 11:59: "Good morning, {name}"
      - 12:00 - 16:59: "Good afternoon, {name}"
      - 17:00 - 04:59: "Good evening, {name}"
    - Re-evaluate greeting on `onResume()` so profile updates in Settings immediately reflect when switching back to Home.
  - `app/src/test/java/com/example/pantrybuddy/UserPersonalizationTest.java`:
    - Unit tests verifying name formatting, default fallbacks, and time-based greeting generation.
- [ ] **Verification:** Change user name in Settings to "Michael"; navigate to Home and verify the header reads "Good morning, Michael" (or current time greeting).
- [ ] **Commit Command:**
  ```bash
  git add app/src/main/res/ app/src/main/java/com/example/pantrybuddy/ app/src/test/ phases_fixing_problems.md
  git commit -m "added user personalization and dynamic dashboard greeting"
  ```

---

### Phase FP-2: UI/UX Solidification & Responsive Layout Audit

- [x] **Objective:** Audit and solidify all interactive controls, ensure content fits cleanly across various screen densities, and fix keyboard overflow.
- [x] **Deliverables:**
  - **Screen Fitting & Soft Keyboard Insets:**
    - Audit all activities and bottom sheets for `android:windowSoftInputMode="adjustResize"`.
    - Verify that all scrollable screens (`fragment_home.xml`, `fragment_pantry.xml`, `fragment_recipes.xml`, `activity_recipe_detail.xml`, `activity_cooking.xml`, `activity_meal_review.xml`, `activity_pantry_update_success.xml`, `fragment_settings.xml`) use `NestedScrollView` with `fillViewport="true"` and adequate bottom padding (80dp - 120dp) so floating buttons and bottom navigation bars never obscure content.
  - **Button & Touch Target Verification:**
    - Verify all click listeners across Home, Pantry, Recipes, Cooking, Meal Review, and Settings.
    - Ensure touch targets meet minimum accessible sizes (48dp height/width).
    - Handle edge cases: rapid repeated clicks on action buttons (debounce or disable during processing).
  - **Typography & Theme Polish:**
    - Ensure consistent styling using Figma design tokens (`@color/colorBackground`, `@color/colorSurface`, `@color/colorTextPrimary`, `@color/colorAccentClay`, `@color/colorAccentGreen`).
    - Eliminate any text truncation or awkward line wrapping on smaller screen sizes.
  - **Dialog & Navigation Polish:**
    - Clean dismissal and clear state transitions for all bottom sheets and confirmation dialogs.
- [x] **Verification:** Manual inspection across all 4 navigation tabs and 4 sub-activities. Form inputs stay visible above keyboard; all buttons respond reliably.
- [ ] **Commit Command:**
  ```bash
  git add app/src/main/res/ app/src/main/java/com/example/pantrybuddy/ phases_fixing_problems.md
  git commit -m "solidified ui layouts, button interactions, and responsive scroll views"
  ```

---

### Phase FP-3: QR & Barcode Scanning with Open Food Facts API

- [ ] **Objective:** Enable instant pantry item entry by scanning package barcodes (EAN-13, UPC-A, QR codes) and querying the Open Food Facts API to retrieve product name, category, and quantity.
- [ ] **Deliverables:**
  - **Dependencies & Permissions:**
    - Add Google ML Kit Barcode Scanning (`com.google.mlkit:barcode-scanning`) and CameraX (`androidx.camera:camera-camera2`, `androidx.camera:camera-lifecycle`, `androidx.camera:camera-view`).
    - Add Camera permission `<uses-permission android:name="android.permission.CAMERA" />` in `AndroidManifest.xml`.
    - Audit to guarantee zero location permissions.
  - **Barcode Scanner Activity:**
    - `ui/scanner/BarcodeScannerActivity.java` & `res/layout/activity_barcode_scanner.xml`:
      - Camera preview viewfinder with scanning reticle overlay.
      - Flashlight toggle button.
      - Automatic barcode detection via ML Kit.
      - Runtime camera permission prompt with clear explanation.
  - **Open Food Facts API Client:**
    - `data/remote/openfoodfacts/OpenFoodFactsService.java`:
      - HTTP client querying `https://world.openfoodfacts.org/api/v2/product/{barcode}.json`.
      - Parses product name (`product_name`), category (`categories`), quantity/net content (`quantity`), and brand.
    - `data/remote/openfoodfacts/OpenFoodFactsDto.java`:
      - Data transfer objects for product response parsing via Gson.
  - **Pantry Integration:**
    - Add a "Scan Barcode" action button in `PantryFragment` and `AddIngredientBottomSheet`.
    - When a barcode is detected and resolved, pre-populate `AddIngredientBottomSheet` with the parsed item name, category, and quantity.
    - Allow user review and manual adjustment before confirming the item into SQLite.
  - `app/src/test/java/com/example/pantrybuddy/OpenFoodFactsParserTest.java`:
    - Unit tests verifying JSON parsing for standard Open Food Facts product responses.
- [ ] **Verification:** Mock or live scan of sample food barcode parses product name and opens pre-filled Add Ingredient sheet; app functions cleanly offline if network is unavailable.
- [ ] **Commit Command:**
  ```bash
  git add app/src/main/ app/build.gradle.kts app/src/test/ phases_fixing_problems.md
  git commit -m "implemented barcode qr scanning with open food facts api integration"
  ```

---

### Phase FP-4: Spoonacular Recipe API Integration & Image Enrichment

- [ ] **Objective:** Integrate the Spoonacular API to discover dynamic recipes, fetch high-resolution food photography, and cache recipes locally while enforcing 100% strict pantry matching.
- [ ] **Deliverables:**
  - **API Configuration & Secure Key Setup:**
    - Configure Spoonacular API endpoint (`https://api.spoonacular.com/recipes/`).
    - Support API key configuration via `local.properties` / `BuildConfig` with fallback demo key or offline mode indicator.
  - **Spoonacular REST Client:**
    - `data/remote/spoonacular/SpoonacularService.java`:
      - Query recipes by ingredients: `findByIngredients?ingredients={pantryIngredients}&number=10&ranking=1`.
      - Fetch detailed recipe info: `{id}/information?includeNutrition=false`.
    - `data/remote/spoonacular/SpoonacularDto.java`:
      - DTO classes for recipe titles, image URLs, preparation minutes, servings, itemized ingredients, and step instructions.
  - **Ingredient Normalization & Strict Matching Bridge:**
    - Map Spoonacular ingredient names through `IngredientNormalizer`.
    - Run candidate recipes through `StrictRecipeMatcher` to guarantee only 100% cookable recipes appear in the main feed.
    - Recipes missing exactly 1 item feed exclusively into the quarantined "Almost There" tier.
  - **Local Room Database Cache:**
    - Cache downloaded recipes into Room (`Recipe` and `RecipeIngredient` entities).
    - Ensure offline-first functionality: app continues to recommend cached recipes even when device is offline.
  - `app/src/test/java/com/example/pantrybuddy/SpoonacularParserTest.java`:
    - Unit tests validating JSON parsing, ingredient mapping, and strict matcher evaluation of Spoonacular responses.
- [ ] **Verification:** Fetch remote recipes, verify high-quality images render in Glide, and confirm recipes with missing ingredients are strictly excluded or placed into Almost There.
- [ ] **Commit Command:**
  ```bash
  git add app/src/main/ app/build.gradle.kts app/src/test/ phases_fixing_problems.md
  git commit -m "integrated spoonacular api for dynamic recipes and offline cache"
  ```

---

### Phase FP-5: Backend Robustness, Error Handling & Code Quality

- [ ] **Objective:** Fortify backend resilience with network error boundaries, clean up compiler/lint warnings, and ensure complete test coverage.
- [ ] **Deliverables:**
  - **Network Resilience & Offline Status:**
    - Graceful error handling for API timeouts, HTTP 429 rate limits, and network dropouts.
    - User-friendly offline banner or snackbar informing user that local pantry inventory and cached recipes are active.
    - Zero crashes under network failure.
  - **Code Quality & Lint Cleanup:**
    - Address unused method warnings in entities and DAOs.
    - Preserve all Room entity getters and setters required for reflection and serialization.
    - Clean up unused imports, dead code, and ensure strict compliance with Android Java best practices.
  - **Permissions & Privacy Audit:**
    - Verify `AndroidManifest.xml` contains only necessary permissions (`VIBRATE`, `POST_NOTIFICATIONS`, `CAMERA`, `INTERNET`).
    - Verify zero location permissions (`ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION` completely absent).
  - **Comprehensive Test Suite:**
    - Run all unit tests across normalizers, unit converters, matchers, diagnostics, workers, and API parsers (`./gradlew test`).
    - Clean debug build verification (`./gradlew assembleDebug`).
- [ ] **Verification:** All unit tests pass cleanly in Gradle terminal; app compiles without warnings or errors.
- [ ] **Commit Command:**
  ```bash
  git add app/src/main/ app/src/test/ phases_fixing_problems.md
  git commit -m "completed backend robustness, offline fallbacks, and test verification"
  ```

---

## Branch Development Guidelines

1. **Step-by-Step Execution:** Complete each phase sequentially (FP-1 through FP-5).
2. **Offline-First Principle:** Local SQLite database remains the primary source of truth. External APIs enhance the experience but never block core functionality.
3. **Zero Location Permissions:** Under no circumstances should location or GPS permissions be added.
4. **Zero Emojis:** Maintain clean, professional, and student-like documentation and code without any emoji characters.
5. **Continuous Verification:** Run `./gradlew compileDebugSources test` at every milestone before providing commit commands.
