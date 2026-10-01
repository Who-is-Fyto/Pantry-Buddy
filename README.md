# 🥑 PantryBuddy (Home Buddy Smart Pantry)

> **A Java Android application that suggests recipes based *strictly* on leftover ingredients to eliminate food waste.**

[![Platform](https://img.shields.io/badge/Platform-Android%20(Java)-green.svg)](https://developer.android.com)
[![Language](https://img.shields.io/badge/Language-Java%2011%20%2F%2017-orange.svg)](https://www.oracle.com/java/)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-24%20(Android%207.0)-blue.svg)](https://developer.android.com)
[![Design](https://img.shields.io/badge/Figma-Home%20Buddy%20Smart%20Pantry-purple.svg)](https://www.figma.com/design/4spg0re3UtponEqYMZZxdr/Home-Buddy-Smart-Pantry?node-id=0-1)

---

## 🌟 The Core Value: The Strict-Matching Rule

Most cooking applications ask what ingredients you have, then recommend a recipe that still requires 3 more items from the supermarket.

**PantryBuddy is different.** The core value of this application is its **Strict-Matching Business Rule**:
* **100% On-Hand Guarantee:** A recipe is suggested **only** if the user already has every single required ingredient in sufficient quantity at home.
* **No Hidden Assumptions:** Small staples—cooking oil, salt, black pepper, butter, spices—are **never** assumed to be in your cupboard. They are explicitly tracked in your pantry inventory.
* **Zero Food Waste:** By prioritizing ingredients closest to their expiration date, you cook what needs using *before* it spoils.
* **No Unplanned Shopping Trips:** Dinner is already in your kitchen.

---

## 📱 The 12 Screen Panels (From Figma Specification)

The application implements the complete 12-panel user flow designed in [Figma](https://www.figma.com/design/4spg0re3UtponEqYMZZxdr/Home-Buddy-Smart-Pantry?node-id=0-1):

| # | Panel / Screen | Role in User Journey |
| :-: | :--- | :--- |
| **1** | **Home Dashboard** | Daily greeting, pantry count, urgent expiring item nudges, and quick recipe teasers. |
| **2** | **Pantry Inventory** | Searchable list of ingredients categorized with real-time expiration pills (`Use today`, `4 days left`, `Best before`). |
| **3** | **Add Ingredient** | Modal form to log ingredients with quantities, units (`g`, `ml`, `pcs`), categories, and expiry dates. |
| **4** | **Ingredient Detail & Edit** | Detailed item card showing which recipes it participates in, with inline quantity adjustment. |
| **5** | **Cookable Recipe Results** | Strict feed of 100% cookable meals with badge *"6 of 6 ingredients at home"*. |
| **6** | **Recipe Detail & Check** | Full recipe breakdown with a **3-column verification table** (`INGREDIENT` \| `NEEDED` \| `AT HOME`) and serving scaler. |
| **7** | **Step-by-Step Cooking** | Fullscreen distraction-free cooking assistant with built-in interactive countdown timers. |
| **8** | **Meal Completion Review** | Post-cooking verification table showing exact quantities used before any pantry deduction occurs. |
| **9** | **Pantry Update Success** | Confirmation of inventory delta (e.g. `Eggs: 4 → 0 pcs`), triggering re-evaluation of matches. |
| **10**| **No Cookable Matches** | Transparent empty state explaining why no recipes match remaining stock, prompting a pantry audit. |
| **11**| **Pantry Filter Sheet** | Bottom sheet drawer to filter pantry by Category, "Use soon only", and expiry sorting. |
| **12**| **Expiry Reminder Sheet**| Proactive morning/evening alert for ingredients needing attention, with a 5 PM snooze option. |

---

## 🎨 Visual Design System

PantryBuddy features an organic, comforting, culinary aesthetic:
- **Canvas Base (`#F7F2EA`):** Warm oatmeal / cream background.
- **Card Surfaces (`#FFFCF7`):** Soft ivory elevated containers with subtle `#EDE2D6` borders.
- **Primary Text (`#34251F`):** Deep espresso roast for high-contrast, effortless legibility.
- **Muted Text (`#82746A`):** Warm taupe for secondary descriptions and timestamps.
- **Accent Green (`#4D6650` & `#E8EEDF`):** Fresh sage green for freshness pills, "In your pantry" tags, and badges.
- **Action Clay (`#795642`):** Warm terracotta for primary buttons and touch points.
- **Typography:**
  - **Headings:** `Lora` (Serif) for warmth and culinary craft.
  - **Interface & Data:** `Inter` (Sans-Serif) for crisp, readable numbers and labels.

---

## 🛠️ Technology Stack

| Layer | Component | Description |
| :--- | :--- | :--- |
| **Language** | **Java (11 / 17)** | Strict, object-oriented Android development. |
| **Platform** | **Android SDK 37** | `minSdk = 24` (Android 7.0+) for broad device reach. |
| **Architecture** | **MVVM + Repository** | Clean architecture utilizing Android Architecture Components (`ViewModel`, `LiveData`). |
| **Local Database** | **Room (SQLite)** | ACID-compliant local database storing pantry items and recipes with compile-time query verification. |
| **Matching Engine** | **`StrictRecipeMatcher`** | Pure Java domain service calculating ingredient availability, unit conversions, and serving limits. |
| **Networking** | **Retrofit 2 + Gson** | REST client for querying online recipe databases (Spoonacular / TheMealDB) when connected. |
| **Image Loading** | **Glide** | Fast image rendering and memory caching for recipe photography. |
| **Background Tasks** | **WorkManager** | Scheduled daily worker (9:00 AM & 5:00 PM) for expiration reminder push notifications. |

---

## 📂 Project Architecture

```
PantryBuddy/
├── DESIGN.md                               # Comprehensive Technical & UI/UX Design Doc
├── README.md                               # Project documentation
├── app/
│   ├── build.gradle.kts                    # App dependencies & SDK configuration
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── assets/                         # Pre-seeded recipe JSON & default database
│       ├── java/com/example/pantrybuddy/
│       │   ├── data/
│       │   │   ├── local/                  # Room Entities, DAOs, and AppDatabase
│       │   │   ├── remote/                 # Retrofit interfaces & DTOs
│       │   │   └── repository/             # Pantry & Recipe repositories
│       │   ├── domain/
│       │   │   └── engine/                 # StrictRecipeMatcher & UnitConverter
│       │   ├── ui/
│       │   │   ├── home/                   # Home Dashboard
│       │   │   ├── pantry/                 # Pantry Inventory, Add, & Filter sheets
│       │   │   ├── recipes/                # Recipe Results & 3-Column Verification
│       │   │   └── cooking/                # Guided Cooking & Meal Completion Review
│       │   └── utils/                      # Notifications, Date formatters, WorkManager
│       └── res/
│           ├── font/                       # Lora & Inter font assets
│           ├── values/                     # Colors, styles, dimensions
│           └── layout/                     # Activity & fragment XML layouts
```

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio** (Ladybug, Hedgehog, or newer)
- **Android SDK Platform 37** (or 34+)
- **JDK 11** or **JDK 17** configured in Android Studio

### Opening the Project
1. Launch Android Studio.
2. Click **Open** and select:
   ```
   /home/michael/AndroidStudioProjects/PantryBuddy
   ```
3. Allow Gradle to sync dependencies.
4. Run the project on an Android Emulator or connected physical device (<kbd>Shift</kbd> + <kbd>F10</kbd>).

---

## 📄 Design Attribution
Designed in Figma: [Home Buddy Smart Pantry Prototype](https://www.figma.com/design/4spg0re3UtponEqYMZZxdr/Home-Buddy-Smart-Pantry?node-id=0-1) by Michael.
