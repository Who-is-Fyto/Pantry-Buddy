# PantryBuddy — Frontend Design & Feature Gap Audit (Figma Comparison)

> **Document Purpose:** Comprehensive side-by-side audit comparing the current Android app build against the official Figma design prototype (**Home Buddy Smart Pantry**, Node `0:1`).  
> **Reference File:** [Figma: Home Buddy Smart Pantry](https://www.figma.com/design/4spg0re3UtponEqYMZZxdr/Home-Buddy-Smart-Pantry?node-id=0-1)  
> **Milestone:** Phase FP-2.5 (Design Fidelity & Missing Frontend Features Backlog)

---

## Executive Summary

While the core functional architecture (Strict Matching, Zero-Waste deduction, Room database, and unit tests) is 100% operational, the visual appearance and user flow contain specific frontend details, micro-copy banners, and modal dialogs that were designed in Figma but have not yet been fully implemented in the Android UI.

This audit breaks down every single missing frontend feature, screen by screen, so they can be prioritized and built in future polish phases.

---

## Screen-by-Screen Frontend Feature Audit

---

### 1. Home Dashboard (`HomeFragment` / Figma Frame #2:9100)

| Figma Design Element | Current Android Build | Status & What Is Missing |
| :--- | :--- | :--- |
| **Top App Bar Icons** | Generic or absent | In Figma, top bar has a culinary **cooking pot icon** on the left and a **notification bell icon** on the right. Tapping the bell opens the Expiry Reminder Bottom Sheet. |
| **Eyebrow & Subtitle** | Basic text | Figma uses `SMART PANTRY • THURSDAY, 1 OCTOBER` in terracotta clay (`#795642`) with letter-spacing `0.08`, followed by personalized greeting and *"Let's make something lovely with what you have."* |
| **Pantry Summary Card Quick-Add Button** | Entire card is clickable to open pantry | Figma features an inline round `+` icon button right on the "Your Pantry" summary card to immediately add an ingredient without switching tabs first. |
| **Expiring Ingredients Thumbnail Previews** | Text list only | Figma renders food thumbnails / visual badges next to each expiring item (Spinach 100g – Use today; Tomatoes 300g – Use tomorrow). |
| **"From Your Pantry" Section Header & Action** | Static text | Figma has a section header with an active inline link button: **"See 2 recipes →"** that filters straight to the recipes tab. |
| **Guaranteed Matching Micro-Copy Tag** | Missing | Figma places a reassuring pill badge above the featured recipe: *"Every ingredient checked, in the right amounts. Even the oil, salt and pepper."* |
| **Featured Recipe Photography** | Vector placeholder icon | Figma has high-resolution photography for the featured dish with rounded corners (`16dp`–`20dp`). |

---

### 2. Expiry Reminder Bottom Sheet (Figma Frame #2:9864) — **Entire Screen Missing**

> **Current State:** The app has a background `WorkManager` scheduler that sends system notifications, but the **in-app modal bottom sheet designed in Figma does not exist yet**.

**Missing Frontend Features:**
- **Modal Trigger:** Tapping the notification bell in the Home top bar or the urgent expiry banner should open this bottom sheet.
- **Header:** *"A little nudge for today"* with subtitle: *"Two ingredients could use a little love. Your pantry is still unchanged."*
- **Itemized Expiry List:** Displays the urgent ingredients with date tags:
  - `Spinach · 100 g` — *Use today · 1 Oct*
  - `Tomatoes · 300 g` — *Use tomorrow · 2 Oct*
- **Recipe Teaser Card:**
  - *"2 recipes, all ingredients already at home"*
  - *"Both use your spinach and tomatoes. Choose one, and we'll recheck the pantry after cooking."*
- **Action Buttons:**
  - **"See recipes I can cook"** (Primary clay button navigating directly to filtered recipes).
  - **"Remind me today at 5 pm"** (Outlined secondary button snoozing notification).
- **Safety Disclaimer Footer:**
  - *"Dates are reminders, not a safety guarantee. Check freshness before cooking."*

---

### 3. Pantry Inventory (`PantryFragment` / Figma Frame #2:9173)

| Figma Design Element | Current Android Build | Status & What Is Missing |
| :--- | :--- | :--- |
| **Top Eyebrow** | "Pantry Inventory" | Figma specifies `SMART PANTRY • BEFORE COOKING` with title `Your pantry`. |
| **Pantry Filter Button** | Horizontal chip row only | Figma has a dedicated **"Filter" chip with a slider icon (`sliders-horizontal`)** that triggers the full Pantry Filter Bottom Sheet. |
| **Ingredient Card Visuals** | Text-only cards | Figma designs show small visual thumbnails or food category icons for each ingredient. |
| **Inventory List Header** | Basic count text | Figma uses a clean dual header: `"6 ingredients"` on the left, `"Soonest expiry ↓"` on the right. |
| **Item Category Badge on Card** | Muted text with bullet dot | Figma includes clear category pills (e.g. `Eggs & dairy`, `Vegetables`). |
| **Bottom Reassurance Banner** | Missing | Figma includes a sticky or footer reassurance card: *"Keep quantities up to date. We only suggest meals you can make entirely from this list."* |

---

### 4. Pantry Filter Bottom Sheet (Figma Frame #2:9785) — **Entire Screen Missing**

> **Current State:** The pantry currently only has basic horizontal chips. The **comprehensive filter drawer designed in Figma is missing**.

**Missing Frontend Features:**
- **Trigger:** Tapping the "Filter" button with slider icon in the pantry search bar.
- **Header:** *"Find what you need"*, subtitle: *"Narrow down your pantry without changing what's in it."*
- **Category Filter Section:** Multi-select chips (`All ingredients`, `Vegetables`, `Eggs & dairy`, `Staples`, `Spices`) with a "Reset" link button.
- **Urgency Toggle:** Switch / checkbox for *"Use soon only — Due today or in the next 2 days"*.
- **Sort Selection:** Radio or chip selection between *"Soonest expiry"* and *"Ingredient name"*.
- **Live Match Preview & CTA:**
  - Shows dynamic matches: *"Matches: Spinach · 100 g and Tomatoes · 300 g"*.
  - Primary button: **"Show 2 ingredients"** (updates the list dynamically).

---

### 5. Add Ingredient Form (Figma Frame #2:9285)

| Figma Design Element | Current Android Build | Status & What Is Missing |
| :--- | :--- | :--- |
| **Draft State Indicator Badge** | Missing | Figma displays a subtle amber/neutral pill at the top: `Unsaved entry · not in your pantry yet` with a pencil icon. |
| **Quantity Helper Note** | Error message only on invalid input | Figma has permanent helper copy under the quantity/unit row: *"Count or weigh what you actually have. Use grams or millilitres for loose ingredients."* |
| **Visual Category Icons** | Text chips | Figma chips feature category icon indicators alongside the text. |
| **Staples Reassurance Box** | Present as generic text | Match exact Figma styling: Ivory card background, border stroke, and clean typography: *"Small ingredients count, too. Add oil, salt and spices separately. Nothing is assumed to be in your cupboard."* |

---

### 6. Ingredient Detail & Edit (`IngredientDetailActivity` / Figma Frame #2:9346)

| Figma Design Element | Current Android Build | Status & What Is Missing |
| :--- | :--- | :--- |
| **Top App Bar Delete Action** | Red button at the bottom of the scroll view | In Figma, deletion is handled cleanly via a **trash icon button (`trash-2`) in the top toolbar**, keeping the bottom focused on saving. |
| **Food Photography Banner** | No image header | Figma shows a hero food photograph/card for the ingredient at the top. |
| **Cross-Reference Recipe Link** | Missing secondary button | Figma includes a dedicated secondary button: **"See recipes using tomatoes"** that jumps directly to the recipe catalog filtered by this item. |
| **Shelf-Life Status Card** | Simple text pill | Figma shows a combined card: `"Use tomorrow • Best used by 2 October"` with a clock icon and subtitle *"Your tomatoes are included in 2 recipes you can make right now."* |
| **Helper Micro-Copy** | Missing | Under the edit fields: *"Eaten some already? Correct the amount here and we'll check your recipe matches again."* |

---

### 7. Cookable Recipe Results (`RecipesFragment` / Figma Frame #2:9406)

| Figma Design Element | Current Android Build | Status & What Is Missing |
| :--- | :--- | :--- |
| **Header Eyebrow** | "SUGGESTED RECIPES • SMART PANTRY" | Figma specifies `SMART PANTRY • YOUR PANTRY · BEFORE COOKING`. |
| **Result Count Section Header** | Missing | Figma includes a dedicated count header above the cards: `"2 recipes you can cook"`. |
| **Filter Bar Options** | Basic chips | Figma includes filter options with icons (`clock-3` for `Under 20 min`, alert for `Use soon first`) and a filter settings icon. |
| **Recipe Card Snapshot Preview** | Single line text | Figma formats the ingredient snapshot with clean line breaks or styled tags: `150 g tomatoes · 50 g spinach · 10 ml olive oil` / `1 g salt · 1 g black pepper`. |
| **Bottom Explanation Footer** | Missing | Figma places an essential footer card: *"Matches are checked separately. After you cook one, we'll recheck what's possible with what remains."* |

---

### 8. Recipe Detail & 3-Column Verification (`RecipeDetailActivity` / Figma Frame #2:9476)

| Figma Design Element | Current Android Build | Status & What Is Missing |
| :--- | :--- | :--- |
| **Toolbar Bookmark Action** | Back arrow only | Figma includes a bookmark / favorite icon button (`bookmark`) on the right of the top toolbar. |
| **Metadata Tagging** | Basic time & servings | Figma tags include difficulty and equipment: `"15 minutes • Easy · one pan"`. |
| **Dynamic Serving Info** | "2 servings" | Figma highlights: `"2 servings • Maximum with your 4 eggs"` explaining *why* 2 is the maximum. |
| **Verification Header** | "Ingredient Verification" | Figma uses conversational phrasing: `"A check before you cook"`. |
| **Equipment & Residue Notice** | Basic post-cooking projection | Figma includes equipment list: *"You'll need a frying pan, a bowl and a spatula. Quantities leave 100 g tomatoes and 20 g spinach for later."* |

---

### 9. Guided Step-by-Step Cooking (`CookingActivity` / Figma Frame #2:9566)

| Figma Design Element | Current Android Build | Status & What Is Missing |
| :--- | :--- | :--- |
| **Segmented Progress Track** | Continuous `LinearProgressIndicator` | Figma uses a segmented multi-step bar showing individual step segments rather than a continuous bar. |
| **Step-Specific Subtitles** | Generic "Step X" | Figma steps have individual titles: Step 1: *"Prep & whisk"*, Step 2: *"Soften the vegetables"*, Step 3: *"Gently scramble"*. |
| **Multi-Step Timeline Visibility** | Only current step visible | Figma displays the active step prominently, but also shows compact preview cards of previous steps (*"Prep & whisk · Done"*) and upcoming steps (*"Gently scramble · Next"*). |
| **Dynamic Next CTA Button** | "Next step" | Figma dynamically names the target step in the button: **"Next · gently scramble"**. |
| **Timer Status Label** | Standard numeric countdown | Figma includes contextual timer label: `"Tomatoes · timer ready"` and `"Start ▶"`. |

---

### 10. Meal Completion Quantity Review (`MealReviewActivity` / Figma Frame #2:9621)

| Figma Design Element | Current Android Build | Status & What Is Missing |
| :--- | :--- | :--- |
| **Completed Meal Header Card** | Text title only | Figma shows a hero summary card with food photograph, dish title, and badge: `"Tomato & spinach scramble · 2 servings cooked"`. |
| **Reassurance Banner with Pencil** | Missing | Figma includes an editable note card: *"Your pantry hasn't changed yet — These amounts come from the recipe. Edit each one to match what you actually used."* |
| **Table Layout Optimization** | 4 cramped columns (`INGREDIENT`, `BEFORE`, `USED`, `REMAIN`) | In Figma, the table uses **3 columns**: `INGREDIENT`, `ACTUALLY USED`, `WILL REMAIN`. The "Before" quantity is placed as a clean subtitle directly below the ingredient name (`"Before: 300 g"`). This avoids horizontal scrolling on smaller screens. |
| **Explanatory Deduction Note** | Missing | Figma adds a note below the checkbox: *"Confirming subtracts only these quantities. Eggs will be marked used up; the other 5 ingredients stay in your pantry."* |

---

### 11. Confirmed Pantry Update Success (Figma Frame #2:9920)

| Figma Design Element | Current Android Build | Status & What Is Missing |
| :--- | :--- | :--- |
| **Presentation Format** | Full-screen activity with toolbar | In Figma, this is designed as an **animated celebratory modal bottom sheet** sliding up over the darkened previous screen! |
| **Delta Table Format** | Multi-line text list | Figma uses a crisp 2-column delta format: `INGREDIENT` and `BEFORE → NOW` (e.g. `Eggs: 4 → 0 pcs`, `Tomatoes: 300 → 100 g`, `Spinach: 100 → 20 g`). |
| **Proactive Re-Check Status Card** | Missing | Figma includes an immediate re-matching status card: *"Recipe matches checked again — No complete matches with these remaining amounts. Check your pantry if anything needs correcting."* |

---

### 12. Zero-Match Feedback State (`NoMatchesState` / Figma Frame #2:9716)

| Figma Design Element | Current Android Build | Status & What Is Missing |
| :--- | :--- | :--- |
| **Shelf-Life Reminder for Leftovers** | Generic unlock hint | Figma features a specific card for remaining perishable items: *"Still use your spinach today — The 20 g left keeps its 1 Oct expiry. Cooking didn't reset any dates."* |
| **Secondary Action Note** | Single CTA button | Figma includes helper copy under the button: *"Forgot to record something already at home? Add it to your pantry, then check again."* |

---

## Prioritized Implementation Roadmap for Figma Alignment

When ready to tackle these frontend improvements, we recommend grouping them into three focused milestones:

### Priority 1: High-Impact Missing Screens & Modals
1. **Expiry Reminder Bottom Sheet (`ExpiryReminderBottomSheet`):**
   - Connect the top-bar bell icon to open this modal bottom sheet showing urgent items, recipe teasers, and a 5 PM snooze button.
2. **Pantry Filter Bottom Sheet (`PantryFilterBottomSheet`):**
   - Add the filter slider button in `fragment_pantry.xml` and build the drawer for category multi-selection, urgency toggle, and sorting.
3. **Pantry Update Success Bottom Sheet:**
   - Migrate `PantryUpdateSuccessActivity` to a bottom sheet dialog (`PantryUpdateSuccessBottomSheet`) with the `BEFORE → NOW` table format.

### Priority 2: Layout & Table Polish
1. **Meal Review Table Streamlining:**
   - Move "Before: X g" directly underneath the ingredient name to streamline from 4 columns to 3 columns (`INGREDIENT`, `ACTUALLY USED`, `WILL REMAIN`).
2. **Step-by-Step Cooking Guide Timeline:**
   - Add step titles, segmented progress indicator, and compact previous/next preview cards.
   - Update CTA to display dynamic next step label (e.g. "Next · gently scramble").
3. **Ingredient Detail Action Placement:**
   - Move the delete action into the top toolbar trash icon and add the "See recipes using [ingredient]" action button.

### Priority 3: Visual Polish & Micro-Copy
1. **Food Photography & Asset Loading:**
   - Integrate realistic recipe photography via Glide (or prepare for Spoonacular API in Phase FP-4).
2. **Micro-Copy & Figma Design Tokens:**
   - Add the missing reassurance pills and banner copy across Home, Recipes, and Pantry.
   - Ensure corner radii match `20dp`–`24dp` for cards and `100dp` for pills.
