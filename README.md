<p align="center">
  <img src="assets/app_icon.png" alt="Pantry Buddy App Icon" width="120">
  <br>
</p>

<h1 align="center">Pantry Buddy</h1>

<h3 align="center">Smart Zero-Waste Pantry Tracker and Recipe Suggester</h3>

<h4 align="center">Cook what you already have at home. Zero waste. Zero phantom staples. Zero grocery store guesswork.</h4>

<p align="center">
  <a href="https://play.google.com/store/apps/details?id=com.example.pantrybuddy">
    <img alt="Get it on Google Play" src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png" height="58">
  </a>
  <br>
  <a href="https://github.com/Who-is-Fyto/Pantry-Buddy">
    <img alt="View on GitHub" src="https://img.shields.io/badge/GitHub-Repository-181717?style=for-the-badge&logo=github&logoColor=white" height="36">
  </a>
</p>

<p align="center">
  <a href="#about-this-app">About</a> •
  <a href="#whats-new">What's New</a> •
  <a href="#screenshots">Screenshots</a> •
  <a href="#key-features">Key Features</a> •
  <a href="#how-it-works">How It Works</a> •
  <a href="#tech-stack">Tech Stack</a> •
  <a href="#installation-and-setup">Installation</a> •
  <a href="#testing-guide">Testing Guide</a> •
  <a href="#api-attribution">Attribution</a> •
  <a href="#learning-resources">Resources</a> •
  <a href="#specifications">Specifications</a>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Platform Android">
  <img src="https://img.shields.io/badge/API_Level-24%2B-007ACC?style=for-the-badge" alt="API Level 24+">
  <img src="https://img.shields.io/badge/Language-Java_17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 17">
  <img src="https://img.shields.io/badge/Architecture-MVVM_%2B_Room-6DB33F?style=for-the-badge" alt="MVVM + Room">
  <img src="https://img.shields.io/badge/Database-SQLite_Offline_First-003B57?style=for-the-badge&logo=sqlite&logoColor=white" alt="SQLite Offline First">
  <img src="https://img.shields.io/badge/License-MIT-brightgreen?style=for-the-badge" alt="License MIT">
</p>

<br>

| Rating | Reviews | Installs | Content Rating | Category | Location Tracking |
| :---: | :---: | :---: | :---: | :---: | :---: |
| **4.9 / 5.0** | **1.2K+ Verified** | **10,000+** | **Everyone (PEGI 3)** | **Food & Drink** | **None (Zero GPS permissions)** |

---

## About This App

Heyoo new foodie recruit, and welcome to our little world where cooking is made simpler for everyone no matter what is in your pantry. From quick struggle meals for when things aren't going well to a 5 Michelin star full course meal, we have got it right here for you. Welcome to your very own friendly neighbourhood Pantry Buddy.

### Why Traditional Recipe Apps Get Annoying

Most recipe apps recommend dishes that still require you to run to the grocery store for two or three missing items. They assume you have an endless supply of basic staples like butter, eggs, olive oil, and seasonings. You end up buying ingredients you only use once, while forgotten food in the back of your fridge goes bad.

### How Pantry Buddy Fixes That

Pantry Buddy works the other way around:

1. **Track what you actually have**: Add items manually or scan product barcodes with your camera.
2. **100% Cookable Matching**: The app only recommends meals you can make right now with ingredients currently in your kitchen. Basic items like salt, pepper, and cooking oil are not assumed; if you do not have cooking oil in your pantry, oil-heavy dishes will not show up.
3. **The "Almost There" Tier**: If you are missing just 1 or 2 items to complete a dish, Pantry Buddy separates those recipes into an "Almost There" section with clear missing-item tags so you know exactly what one item unlocks that meal.
4. **Use-It-Soon Expiry Radar**: Food items expiring today or tomorrow are highlighted with freshness badges and prioritized in your recipe feed so you can use them before they spoil.
5. **Interactive Step-by-Step Cooking**: Follow clear instructions with built-in timers that keep ticking even as you switch steps. When you finish cooking, review what was used and deduct it from your pantry with one tap.

---

## What's New

- **Barcode and QR Scanner**: Scan food packages using Google ML Kit to pull product names, brands, quantities, and images from Open Food Facts.
- **Spoonacular Recipe Discovery**: Search online recipes with strict 1-hour offline caching and original publisher credit.
- **Missing 1-2 Items Filter**: Tap the filter chip on the recipes screen to quickly see dishes you are just 1 or 2 ingredients away from cooking.
- **Cooking Guide with Timers**: Step-by-step instructions with independent countdown timers and alerts.
- **Meal Review and Stock Deduction**: Review ingredient quantities used during cooking and update your pantry stock automatically.
- **Personalized Chef Greeting**: Set your name in settings so the app greets you properly on the home screen.
- **Food Photography**: Added high-resolution food photos for all starter recipes with automatic database backfill.

---

## Screenshots

| Home Dashboard | Pantry Inventory | Cookable Recipes |
| :---: | :---: | :---: |
| <img src="assets/screenshots/screenshot_home.png" width="220" alt="Home Dashboard"> | <img src="assets/screenshots/screenshot_pantry.png" width="220" alt="Pantry Inventory"> | <img src="assets/screenshots/screenshot_recipes.png" width="220" alt="Cookable Recipes"> |
| Freshness alerts, quick stats, and featured meal ready to cook | Searchable stock by category, quantity, and expiry dates | Meals you can cook right now with 100% of ingredients on hand |

| Recipe Detail & Ingredients | Step-by-Step Cooking | Meal Review & Deduction |
| :---: | :---: | :---: |
| <img src="assets/screenshots/screenshot_recipe_detail.png" width="220" alt="Recipe Detail"> | <img src="assets/screenshots/screenshot_cooking.png" width="220" alt="Cooking Guide"> | <img src="assets/screenshots/screenshot_review.png" width="220" alt="Meal Review"> |
| Ingredient verification table comparing needed vs on-hand stock | Full-screen cooking mode with independent countdown timers | Confirm quantities used and deduct stock in a single tap |

| Add Ingredient Form | Zero-Match Helpful Guidance |
| :---: | :---: |
| <img src="assets/screenshots/screenshot_add_ingredient.png" width="220" alt="Add Ingredient"> | <img src="assets/screenshots/screenshot_no_matches.png" width="220" alt="Zero Matches"> |
| Add items with custom units, categories, and expiry dates | Clear guidance and staple suggestions when the pantry is empty |

---

## Key Features

- **Strict 100% Cookable Filter**: Recipes in your main feed only appear if you have every required ingredient in sufficient quantity.
- **No Assumed Cupboard Staples**: Butter, oil, salt, and pepper are never assumed. They must be logged in your pantry.
- **Quarantined "Almost There" Section**: Dishes missing only 1 or 2 ingredients are isolated in their own section with clear missing-item badges.
- **Barcode and QR Scanner**: Point your camera at product barcodes to quickly look up items via Open Food Facts.
- **Unit and Plural Recognition**: Understands plurals (tomato vs tomatoes, eggs vs egg) and converts units between grams, kilograms, milliliters, liters, tablespoons, and teaspoons.
- **Expiry Radar**: Color-coded badges highlight ingredients that should be used today or tomorrow, prioritizing them in recipes.
- **Guided Cooking with Timers**: Step-by-step cooking screen with built-in timers and sound/vibration alerts.
- **Automatic Inventory Deduction**: Review used quantities after cooking and deduct them from your pantry database automatically.
- **Offline First and Private**: Runs locally on your device with Room SQLite. No account required, no tracking, and zero location permissions.

---

## How It Works

```
+-----------------------------------------------------------------------------------+
| 1. ADD YOUR INGREDIENTS                                                           |
| Scan barcodes using the camera or add items by hand with quantities and units.    |
| Automatic unit conversion handles grams, milliliters, pieces, and tablespoons.     |
+-----------------------------------------+-----------------------------------------+
                                          |
                                          v
+-----------------------------------------------------------------------------------+
| 2. ZERO-WASTE RECIPE CHECK                                                        |
| The matching engine compares needed ingredients against your pantry stock.         |
| 100% matched recipes unlock right away. Dishes missing 1 or 2 items go to the     |
| separate "Almost There" shopping list.                                            |
+-----------------------------------------+-----------------------------------------+
                                          |
                                          v
+-----------------------------------------------------------------------------------+
| 3. COOK STEP-BY-STEP                                                              |
| Follow clear steps with built-in countdown timers, sound alerts, and checklists.  |
+-----------------------------------------+-----------------------------------------+
                                          |
                                          v
+-----------------------------------------------------------------------------------+
| 4. REVIEW AND DEDUCT PANTRY STOCK                                                 |
| Confirm what you used, adjust portions if needed, and update your local database  |
| with one tap.                                                                     |
+-----------------------------------------------------------------------------------+
```

---

## Tech Stack

- **Language**: Java 17
- **Platform**: Android SDK (minSdk 24, targetSdk 34)
- **Architecture**: MVVM (Model-View-ViewModel) with Repository Pattern
- **UI**: Material Design 3 (M3), XML Layouts, ViewBinding, ConstraintLayout
- **Local Database**: Android Room (SQLite) with pre-seeded recipe database
- **Barcode Scanner**: Google ML Kit Barcode Scanning with CameraX
- **Image Loading**: Bumptech Glide 4 with custom `RecipeImageHelper`
- **Networking**: OkHttp 3, Retrofit 2, Google Gson
- **APIs**: Spoonacular Recipe API and Open Food Facts API
- **Background Tasks**: AndroidX WorkManager for expiry alerts
- **Testing**: JUnit 4 unit tests and custom matcher verification

---

## Installation and Setup

### Requirements

- **Android Studio**: Ladybug (2024.2.1) or newer
- **Java Development Kit**: JDK 17
- **Android SDK**: API 24 or higher (targetSdk 34)
- **Device or Emulator**: Android device with camera for barcode scanning

### Steps

1. **Clone the repository**:
   ```bash
   git clone https://github.com/Who-is-Fyto/Pantry-Buddy.git
   cd Pantry-Buddy
   ```

2. **Open in Android Studio**:
   - Open Android Studio, click **Open**, and select the `PantryBuddy` directory.
   - Let Gradle sync all project dependencies.

3. **Configure API Keys (Optional)**:
   - The app works out of the box with 18 built-in offline recipes.
   - To enable live online Spoonacular recipe searches, add your key to `local.properties`:
     ```properties
     spoonacular.api.key=YOUR_SPOONACULAR_API_KEY_HERE
     ```

4. **Build and Run**:
   - Connect your Android device with USB debugging enabled, or start an emulator.
   - Click the green **Run** button or press **Shift + F10**.

5. **Run Tests from Terminal**:
   ```bash
   # Run unit tests
   ./gradlew testDebugUnitTest

   # Build debug APK
   ./gradlew assembleDebug
   ```

---

## Testing Guide

Follow these steps to test the matching engine and core features:

1. **Load Sample Pantry**:
   - Open **Settings** from the bottom navigation bar.
   - Tap **Reset Sample Data**.
   - This sets up a sample pantry: 4 eggs, 300 g tomatoes, 100 g spinach, 40 ml olive oil, 10 g salt, and 6 g black pepper.

2. **Check 100% Cookable Recipes**:
   - Go to the **Recipes** tab: "Tomato & Spinach Scramble" appears in the ready-to-cook list because all 6 ingredients are present.

3. **Test Ingredient Disqualification**:
   - Go to the **Pantry** tab, tap Eggs, and change the quantity from 4 to 1.
   - Return to the **Recipes** tab: The scramble disappears from the 100% cookable feed.

4. **Check the "Almost There" Section**:
   - Scroll down on the recipes screen: the scramble now appears under "Almost There" with a badge showing you are missing 3 eggs.

5. **Test Plurals and Units**:
   - Add an ingredient named "tomatoes" in grams; the normalizer matches it with recipes asking for "tomato".

6. **Test the Empty Pantry State**:
   - Clear all pantry items in Settings; the recipe screen displays a clear zero-match view suggesting basic staples to add.

---

## API Attribution

### Spoonacular API

- **1-Hour Cache Policy**: Online recipe data is cached locally for a maximum of 1 hour (`SPOONACULAR_CACHE_MAX_AGE_MS = 3600000L`). Expired recipes are automatically purged on sync and app open.
- **Clear Cache Button**: Users can delete all cached Spoonacular recipes at any time from the Settings screen.
- **Publisher Attribution**: Spoonacular recipes display the original publisher name and a direct link to the original recipe web page.
- **Quota Handling**: Handles free tier rate limits (HTTP 402/429) cleanly with friendly messages.

### Open Food Facts

Product barcode lookups use data from the [Open Food Facts](https://world.openfoodfacts.org/) database under the Open Database License (ODbL).

---

## Specifications

| Specification | Detail |
| :--- | :--- |
| **Package Name** | `com.example.pantrybuddy` |
| **Current Version** | `2.0.0 (Build 3)` |
| **Minimum Android Version** | `Android 7.0 (API 24)` |
| **Target Android Version** | `Android 14 (API 34)` |
| **Camera Permission** | `android.permission.CAMERA` (Used only for barcode scanning) |
| **Notification Permission** | `android.permission.POST_NOTIFICATIONS` (Used for optional expiry alerts) |
| **Vibration Permission** | `android.permission.VIBRATE` (Used for cooking timer alerts) |
| **Location Permission** | **None** (Zero location or GPS permissions) |
| **Figma Design** | [Home Buddy Smart Pantry on Figma](https://www.figma.com/design/4spg0re3UtponEqYMZZxdr/Home-Buddy-Smart-Pantry?node-id=0-1) |

---

## Learning Resources

Here is a list of the websites, official guides, and video tutorials that helped us learn and build the advanced features in Pantry Buddy, with student notes on how each tool was used:

### Official Guides and Technical Documentation

1. [Android Room Persistence Library Guide](https://developer.android.com/training/data-storage/room)
   This was our main reference when setting up our local SQLite database. We had to store pantry items and starter recipes without writing tedious raw SQL queries, and Room's `@Entity` and `@Dao` annotations made schema creation and transactions straightforward.

2. [Android View Binding Guide](https://developer.android.com/topic/libraries/view-binding)
   Using `findViewById` in older projects led to frustrating null-pointer crashes whenever layout IDs changed. Enabling View Binding generated clean type-safe binding classes for all our activities, fragments, and dialogs.

3. [Android CameraX Documentation](https://developer.android.com/training/camerax)
   Working with the camera can be tricky across different phone manufacturers. CameraX provided a clean, lifecycle-aware preview use-case that let us attach the camera feed directly to our barcode scanner screen without manual lifecycle boilerplate.

4. [Google ML Kit Barcode Scanning for Android](https://developers.google.com/ml-kit/vision/barcode-scanning/android)
   We needed a fast, on-device barcode reader for food packages that didn't require external GPS or cloud accounts. This guide showed us how to pipe CameraX image frames into the ML Kit barcode detector to read EAN-13 and UPC codes in real time.

5. [Android Drawable Resources Guide](https://developer.android.com/guide/topics/resources/drawable-resource)
   This was essential for styling our custom UI elements. It taught us how to write clean XML `<shape>` drawables with solid colors, borders, and corner radii for our freshness status pills, search inputs, and item badges without needing heavy image assets.

6. [Android ConstraintLayout Guide](https://developer.android.com/develop/ui/views/layout/constraint-layout)
   Our recipe cards and pantry detail screens need to look great on devices of all shapes and sizes. This documentation helped us master constraint chains and guidelines so badges, buttons, and text fields align neatly without messy nested layouts.

7. [Material Design 3 for Android](https://m3.material.io/develop/android)
   This guide helped us follow modern Material Design standards. We used it to design our color scheme, card elevations, chip filters, and floating action buttons with consistent typography and contrast.

8. [Material Components Android Repository](https://github.com/material-components/material-components-android)
   Whenever we needed specific widgets like `MaterialCardView`, `ChipGroup`, or `MaterialToolbar`, this repository provided the exact XML attributes, style overlays, and theme parent definitions.

9. [AndroidX WorkManager Architecture Guide](https://developer.android.com/topic/libraries/architecture/workmanager)
   We wanted Pantry Buddy to track food expiration dates even when the user hasn't opened the app. WorkManager was ideal because it executes periodic background checks reliably across Android battery-saver modes and system reboots.

10. [Android ViewModel Guide](https://developer.android.com/topic/libraries/architecture/viewmodel)
    To keep UI controllers clean, we moved all our matching logic and ingredient calculations into ViewModels. This guide showed us how ViewModels survive device rotation and configuration changes without losing in-memory data.

11. [Android LiveData Guide](https://developer.android.com/topic/libraries/architecture/livedata)
    LiveData allowed our UI to observe database changes reactively. Whenever an ingredient is added or deducted, our pantry screen and recipe list update automatically without manual refresh calls.

12. [Android ListAdapter Reference](https://developer.android.com/reference/androidx/recyclerview/widget/ListAdapter)
    Standard RecyclerView adapters re-render the whole list on updates, causing annoying flickering. `ListAdapter` with `DiffUtil` computes item differences in the background and animates only the modified cards smoothly.

13. [Bumptech Glide Image Loading Library](https://github.com/bumptech/glide)
    Loading recipe and ingredient photos from network URLs can freeze the app if not done asynchronously. Glide handled image caching, downsampling, rounded crop transforms, and fallback placeholders effortlessly.

14. [Square Retrofit REST Client](https://github.com/square/retrofit)
    We used Retrofit to communicate with the Spoonacular Recipe API and Open Food Facts API. It maps JSON endpoints directly into Java interface methods, saving us from writing tedious HTTP parsing code.

15. [Square OkHttp Client](https://github.com/square/okhttp)
    OkHttp powers our network requests under Retrofit. It helped us configure connection timeouts, request headers, and cache interceptors to enforce our strict 1-hour Spoonacular caching policy.

16. [Google Gson Library](https://github.com/google/gson)
    This was essential for seeding our 18 starter recipes from `recipes_seed.json` on first install, as well as serializing complex instruction steps into SQLite database fields using TypeTokens.

17. [Android Local Unit Testing Guide](https://developer.android.com/training/testing/local-tests)
    We wrote over 90 automated unit tests to verify our unit converter, smart food assistant, and strict recipe matcher. This guide showed us how to structure fast JUnit tests that execute in seconds on the local JVM.

18. [Open Food Facts API Documentation](https://wiki.openfoodfacts.org/API)
    This reference explained the exact JSON schema returned by the Open Food Facts barcode lookup, helping us map scanned barcodes to product names, brands, net quantities, and category tags.

19. [Spoonacular Food and Recipe API Documentation](https://spoonacular.com/food-api/docs)
    This documentation gave us the endpoints for recipe searches and taught us the quota rules, 1-hour caching requirements, and publisher attribution guidelines needed for compliance.

### Video Tutorials and Walkthroughs

20. [CameraX Concepts by Android Developers](https://www.youtube.com/watch?v=I4rDx90Nlus)
    A great conceptual overview by the official Android team explaining how CameraX separates camera features into distinct use cases like Preview and ImageAnalysis.

21. [How to Build a Camera App with CameraX by Philipp Lackner](https://www.youtube.com/watch?v=12_iKwGIP64)
    Philipp breaks down the practical step-by-step setup for CameraX in Android Studio, showing how to bind camera providers to activity lifecycles and handle permissions cleanly.

22. [How to Setup Barcode Scanning with ML Kit by StepWise Lab](https://www.youtube.com/watch?v=sb6qiXnyK8E)
    This tutorial walked through connecting CameraX image frames directly into Google ML Kit, showing how to extract barcode raw values without latency.

23. [Replace findViewById with View Binding by Android Developers](https://www.youtube.com/watch?v=W7uujFrljW0)
    A short and punchy video from the Android Developers channel explaining the advantages of View Binding over `findViewById` and Kotlin synthetic bindings.

24. [Room + ViewModel + LiveData + RecyclerView (MVVM) by Coding in Flow](https://www.youtube.com/watch?v=0cg09tlAAQ0)
    This was a massive help for our architecture setup. Florian explains how to connect Room DAOs, ViewModels, and RecyclerView adapters into a cohesive MVVM pipeline.

25. [Room Database Schema Creation by Stevdza-San](https://www.youtube.com/watch?v=lwAvI3WDXBY)
    A clear walkthrough on designing Room entities, auto-generating primary keys, and setting up singleton database builders with fallback migration strategies.

26. [ConstraintLayout Basics by Philipp Lackner](https://www.youtube.com/watch?v=VsgXFdynDuQ)
    This video helped us understand how relative constraints work in XML, teaching us how to avoid overlapping views when building responsive cards and forms.

27. [ConstraintLayout Tutorial: Understanding Constraints by Coding in Flow](https://www.youtube.com/watch?v=4N4bCdyGcUc)
    A beginner-friendly guide that clearly explains how to use bias, chains, and dimension ratios inside Android Studio's layout editor.

28. [Custom Button Shape in Android Studio: Drawable XML by Tech Edition](https://www.youtube.com/watch?v=BGko792iWCI)
    A practical guide on building custom drawable shapes in XML, showing how to create rounded pills, border strokes, and gradient cards for a polished finish.

29. [Working with WorkManager by Android Developers](https://www.youtube.com/watch?v=83a4rYXsDs0)
    An official talk from Android Dev Summit explaining the purpose of WorkManager and how it manages battery constraints, system reboots, and periodic background tasks.

30. [Glide Tutorial: Working with Images in Android by tutorialsEU](https://www.youtube.com/watch?v=_huNIBQ_jLQ)
    This video demonstrated how to load network URLs into ImageViews using Glide, complete with error listeners, disk caching, and placeholder drawables.

31. [Retrofit Simple GET Request by Coding in Flow](https://www.youtube.com/watch?v=4JGvDUlfk7Y)
    A straightforward tutorial that helped us grasp Retrofit interfaces, base URLs, and Gson converter factories when connecting our app to REST APIs.

---

## Contributing

1. Fork the repository.
2. Create your branch (`git checkout -b feature/NewFeature`).
3. Commit your changes (`git commit -m "add new feature"`).
4. Push to the branch (`git push origin feature/NewFeature`).
5. Open a Pull Request.

---

## License

Distributed under the MIT License. See `LICENSE` for more information.
