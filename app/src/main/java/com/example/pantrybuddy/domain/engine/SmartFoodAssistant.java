package com.example.pantrybuddy.domain.engine;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// Intelligent semi-AI food assistant providing real-time category, unit, quantity, and shelf-life predictions
public class SmartFoodAssistant {

    // Standardized category constants
    public static final String CAT_VEGETABLES = "Vegetables";
    public static final String CAT_FRUITS = "Fruits";
    public static final String CAT_DAIRY = "Dairy & Eggs";
    public static final String CAT_MEAT = "Meat & Fish";
    public static final String CAT_BAKERY = "Bakery & Bread";
    public static final String CAT_STAPLES = "Staples & Grains";
    public static final String CAT_CANNED = "Canned Goods";
    public static final String CAT_CONDIMENTS = "Condiments & Sauces";
    public static final String CAT_SPICES = "Spices & Seasonings";
    public static final String CAT_BEVERAGES = "Beverages";
    public static final String CAT_SNACKS = "Snacks & Sweets";

    public static class Suggestion {
        @NonNull public final String matchedName;
        @NonNull public final String category;
        @NonNull public final String unit;
        public final double defaultQuantity;
        public final int shelfLifeDays;
        @NonNull public final String explanation;

        public Suggestion(
                @NonNull String matchedName,
                @NonNull String category,
                @NonNull String unit,
                double defaultQuantity,
                int shelfLifeDays,
                @NonNull String explanation
        ) {
            this.matchedName = matchedName;
            this.category = category;
            this.unit = unit;
            this.defaultQuantity = defaultQuantity;
            this.shelfLifeDays = shelfLifeDays;
            this.explanation = explanation;
        }

        @NonNull
        public String getSummaryText() {
            String qtyStr = (defaultQuantity == (long) defaultQuantity)
                    ? String.valueOf((long) defaultQuantity)
                    : String.format(Locale.US, "%.1f", defaultQuantity);
            return category + " • " + qtyStr + " " + unit + " • ~" + shelfLifeDays + " days shelf life";
        }
    }

    private static final Map<String, Suggestion> EXACT_CATALOG = new HashMap<>();
    private static final List<String> COMPREHENSIVE_CATALOG_LIST = new ArrayList<>();

    static {
        // Fruits
        addEntry("Apple", CAT_FRUITS, "pcs", 4.0, 14, "Fresh crisp fruit, best stored chilled");
        addEntry("Banana", CAT_FRUITS, "pcs", 5.0, 6, "Perishable fruit, keep at room temperature");
        addEntry("Orange", CAT_FRUITS, "pcs", 4.0, 14, "Citrus fruit with sturdy shelf life");
        addEntry("Lemon", CAT_FRUITS, "pcs", 3.0, 21, "Acidic citrus staple for seasonings and dressings");
        addEntry("Lime", CAT_FRUITS, "pcs", 3.0, 14, "Tart citrus for marinades, curries, and drinks");
        addEntry("Avocado", CAT_FRUITS, "pcs", 2.0, 5, "Healthy fat fruit, ripens rapidly at room temp");
        addEntry("Strawberries", CAT_FRUITS, "g", 250.0, 4, "Tender berries, consume or freeze promptly");
        addEntry("Blueberries", CAT_FRUITS, "g", 200.0, 7, "Antioxidant-rich berries, store in refrigerator");
        addEntry("Raspberries", CAT_FRUITS, "g", 150.0, 3, "Delicate berries with short refrigerated life");
        addEntry("Grapes", CAT_FRUITS, "g", 500.0, 10, "Table grapes, store dry and chilled");
        addEntry("Watermelon", CAT_FRUITS, "pcs", 1.0, 7, "Hydrating melon, keeps 7-10 days uncut");
        addEntry("Pineapple", CAT_FRUITS, "pcs", 1.0, 5, "Tropical fruit, consume once aromatic");
        addEntry("Mango", CAT_FRUITS, "pcs", 2.0, 6, "Stone fruit, softens upon ripening");
        addEntry("Peach", CAT_FRUITS, "pcs", 3.0, 5, "Summer stone fruit, store chilled when ripe");

        // Vegetables
        addEntry("Spinach", CAT_VEGETABLES, "g", 150.0, 4, "Tender leafy greens with high respiration rate");
        addEntry("Tomatoes", CAT_VEGETABLES, "g", 300.0, 7, "Culinary vegetable, keeps best above 12C");
        addEntry("Cherry Tomatoes", CAT_VEGETABLES, "g", 250.0, 7, "Bite-sized sweet tomatoes, keep dry");
        addEntry("Potatoes", CAT_VEGETABLES, "g", 1000.0, 30, "Hearty root staple, store in cool dark pantry");
        addEntry("Sweet Potatoes", CAT_VEGETABLES, "g", 800.0, 28, "Nutrient-dense tubers, keep in dry pantry");
        addEntry("Onions", CAT_VEGETABLES, "pcs", 3.0, 30, "Fundamental aromatic, store ventilated");
        addEntry("Red Onion", CAT_VEGETABLES, "pcs", 2.0, 25, "Crisp salad and cooking onion");
        addEntry("Garlic", CAT_VEGETABLES, "clove", 4.0, 60, "Aromatic seasoning, bulb lasts 2 months");
        addEntry("Carrots", CAT_VEGETABLES, "g", 500.0, 21, "Root vegetable, retains crunch under refrigeration");
        addEntry("Broccoli", CAT_VEGETABLES, "g", 350.0, 6, "Cruciferous floret, cook or steam promptly");
        addEntry("Cauliflower", CAT_VEGETABLES, "g", 500.0, 8, "Dense cruciferous vegetable");
        addEntry("Bell Pepper", CAT_VEGETABLES, "pcs", 2.0, 10, "Sweet crisp peppers, store in crisper drawer");
        addEntry("Cucumber", CAT_VEGETABLES, "pcs", 2.0, 7, "High-water produce, avoid freezing temps");
        addEntry("Zucchini", CAT_VEGETABLES, "pcs", 2.0, 6, "Summer squash, best sauteed or roasted");
        addEntry("Mushrooms", CAT_VEGETABLES, "g", 250.0, 5, "Store in breathable paper bag in fridge");
        addEntry("Lettuce", CAT_VEGETABLES, "pcs", 1.0, 5, "Crisp salad base, keep hydrated in fridge");
        addEntry("Kale", CAT_VEGETABLES, "g", 200.0, 7, "Sturdy dark leafy green");
        addEntry("Cabbage", CAT_VEGETABLES, "g", 800.0, 21, "Dense head cabbage, stores for weeks");
        addEntry("Celery", CAT_VEGETABLES, "stalk", 4.0, 14, "Crunchy aromatic stalk, store chilled in foil");

        // Dairy & Eggs
        addEntry("Eggs", CAT_DAIRY, "pcs", 6.0, 28, "Complete protein kitchen staple, keep refrigerated");
        addEntry("Milk", CAT_DAIRY, "ml", 1000.0, 7, "Fresh dairy milk, store on inner fridge shelf");
        addEntry("Whole Milk", CAT_DAIRY, "ml", 1000.0, 7, "Rich dairy milk, keep chilled below 4C");
        addEntry("Cheddar Cheese", CAT_DAIRY, "g", 200.0, 30, "Aged hard cheese, wrap tight after opening");
        addEntry("Mozzarella", CAT_DAIRY, "g", 200.0, 10, "Mild melting cheese, consume fresh");
        addEntry("Parmesan", CAT_DAIRY, "g", 150.0, 60, "Aged hard grating cheese with long shelf life");
        addEntry("Butter", CAT_DAIRY, "g", 250.0, 60, "Cultured or salted cooking fat, freezes well");
        addEntry("Greek Yogurt", CAT_DAIRY, "g", 500.0, 14, "Strained high-protein yogurt");
        addEntry("Yogurt", CAT_DAIRY, "g", 500.0, 14, "Fermented dairy, keep refrigerated");
        addEntry("Heavy Cream", CAT_DAIRY, "ml", 250.0, 10, "High-fat dairy for sauces and whipping");
        addEntry("Sour Cream", CAT_DAIRY, "g", 250.0, 14, "Cultured cream for dips and toppings");
        addEntry("Cream Cheese", CAT_DAIRY, "g", 200.0, 21, "Spread for bagels and baking");

        // Meat & Fish
        addEntry("Chicken Breast", CAT_MEAT, "g", 500.0, 3, "Lean poultry, cook within 3 days or freeze");
        addEntry("Chicken Thighs", CAT_MEAT, "g", 500.0, 3, "Juicy poultry cut, cook or freeze promptly");
        addEntry("Ground Beef", CAT_MEAT, "g", 500.0, 2, "Minced beef, cook within 48 hours or freeze");
        addEntry("Beef Steak", CAT_MEAT, "g", 400.0, 4, "Tender beef cut for searing");
        addEntry("Pork Chops", CAT_MEAT, "g", 450.0, 3, "Bone-in or boneless pork cut");
        addEntry("Bacon", CAT_MEAT, "slice", 8.0, 14, "Cured pork belly, keep sealed in fridge");
        addEntry("Salmon Fillet", CAT_MEAT, "g", 350.0, 2, "Omega-3 rich fresh fish, cook within 48h");
        addEntry("Fresh Tuna", CAT_MEAT, "g", 300.0, 2, "Seafood steak, cook or sear promptly");
        addEntry("Shrimp", CAT_MEAT, "g", 400.0, 2, "Quick-cooking seafood, store on ice or freeze");
        addEntry("Turkey Breast", CAT_MEAT, "g", 500.0, 3, "Lean white poultry");
        addEntry("Sausages", CAT_MEAT, "pcs", 4.0, 5, "Fresh breakfast or dinner sausages");

        // Bakery & Bread
        addEntry("White Bread", CAT_BAKERY, "slice", 10.0, 6, "Sliced sandwich bread, keep sealed");
        addEntry("Whole Wheat Bread", CAT_BAKERY, "slice", 10.0, 6, "Hearty sandwich bread");
        addEntry("Bagels", CAT_BAKERY, "pcs", 4.0, 5, "Dense breakfast bread, freezes well");
        addEntry("Tortillas", CAT_BAKERY, "pcs", 8.0, 21, "Flour or corn wraps, keep sealed");
        addEntry("Burger Buns", CAT_BAKERY, "pcs", 4.0, 5, "Soft bakery buns for sandwiches");
        addEntry("Pita Bread", CAT_BAKERY, "pcs", 6.0, 7, "Pocket bread for dips and sandwiches");
        addEntry("Croissants", CAT_BAKERY, "pcs", 2.0, 3, "Buttery flaky breakfast pastry");

        // Staples & Grains
        addEntry("White Rice", CAT_STAPLES, "g", 1000.0, 365, "Pantry staple grain, indefinite pantry life");
        addEntry("Brown Rice", CAT_STAPLES, "g", 1000.0, 180, "Whole grain rice with natural oils");
        addEntry("Spaghetti", CAT_STAPLES, "g", 500.0, 365, "Dry durum wheat pasta, long shelf life");
        addEntry("Penne Pasta", CAT_STAPLES, "g", 500.0, 365, "Dry pasta shapes for rich sauces");
        addEntry("Egg Noodles", CAT_STAPLES, "g", 400.0, 180, "Quick-cooking noodles for stir fries and soups");
        addEntry("All-Purpose Flour", CAT_STAPLES, "g", 1000.0, 240, "Core baking and thickening staple");
        addEntry("Rolled Oats", CAT_STAPLES, "g", 500.0, 365, "Breakfast grain for porridge and baking");
        addEntry("Quinoa", CAT_STAPLES, "g", 500.0, 365, "High-protein ancient grain staple");
        addEntry("Granulated Sugar", CAT_STAPLES, "g", 1000.0, 730, "Baking sweetener, virtually indefinite shelf life");
        addEntry("Brown Sugar", CAT_STAPLES, "g", 500.0, 365, "Moist sugar, store in airtight container");

        // Canned Goods
        addEntry("Canned Black Beans", CAT_CANNED, "can", 1.0, 730, "Ready-to-eat legume staple in brine");
        addEntry("Canned Kidney Beans", CAT_CANNED, "can", 1.0, 730, "Cooked red beans for stews and chili");
        addEntry("Canned Chickpeas", CAT_CANNED, "can", 1.0, 730, "Garbanzo beans for hummus, salads, curries");
        addEntry("Canned Diced Tomatoes", CAT_CANNED, "can", 1.0, 540, "Vine-ripened tomatoes for sauces");
        addEntry("Tomato Paste", CAT_CANNED, "tbsp", 4.0, 365, "Concentrated umami base for sauces");
        addEntry("Canned Tuna", CAT_CANNED, "can", 2.0, 730, "Shelf-stable protein in oil or water");
        addEntry("Chicken Broth", CAT_CANNED, "ml", 500.0, 365, "Savory cooking liquid base");
        addEntry("Vegetable Broth", CAT_CANNED, "ml", 500.0, 365, "Plant-based soup and risotto base");
        addEntry("Coconut Milk", CAT_CANNED, "can", 1.0, 540, "Creamy coconut extract for curries and soups");
        addEntry("Canned Sweetcorn", CAT_CANNED, "can", 1.0, 730, "Crisp corn kernels in brine");

        // Condiments & Sauces
        addEntry("Olive Oil", CAT_CONDIMENTS, "ml", 500.0, 365, "Cold-pressed cooking and dressing oil");
        addEntry("Vegetable Oil", CAT_CONDIMENTS, "ml", 1000.0, 365, "Neutral high-heat frying oil");
        addEntry("Soy Sauce", CAT_CONDIMENTS, "ml", 250.0, 365, "Fermented umami seasoning liquid");
        addEntry("Mayonnaise", CAT_CONDIMENTS, "g", 400.0, 90, "Emulsified spread, refrigerate after opening");
        addEntry("Ketchup", CAT_CONDIMENTS, "ml", 500.0, 180, "Tomato table condiment");
        addEntry("Mustard", CAT_CONDIMENTS, "g", 250.0, 180, "Tangy condiment, keeps well refrigerated");
        addEntry("Balsamic Vinegar", CAT_CONDIMENTS, "ml", 250.0, 730, "Aged sweet vinegar for glazes and salads");
        addEntry("Apple Cider Vinegar", CAT_CONDIMENTS, "ml", 500.0, 730, "Versatile tangy kitchen vinegar");
        addEntry("Honey", CAT_CONDIMENTS, "g", 350.0, 730, "Natural sweetener, never truly spoils");
        addEntry("Hot Sauce", CAT_CONDIMENTS, "ml", 150.0, 365, "Chili pepper condiment for heat");
        addEntry("Peanut Butter", CAT_CONDIMENTS, "g", 450.0, 180, "Protein nut spread, store cool and dry");

        // Spices & Seasonings
        addEntry("Table Salt", CAT_SPICES, "g", 500.0, 1000, "Essential mineral seasoning, indefinite life");
        addEntry("Black Pepper", CAT_SPICES, "g", 60.0, 730, "Freshly ground peppercorns for pungency");
        addEntry("Cinnamon", CAT_SPICES, "g", 40.0, 730, "Warm aromatic ground spice");
        addEntry("Oregano", CAT_SPICES, "g", 30.0, 730, "Dried Mediterranean herb");
        addEntry("Paprika", CAT_SPICES, "g", 50.0, 365, "Ground dried chili peppers for color and warmth");
        addEntry("Cumin", CAT_SPICES, "g", 50.0, 730, "Earthy ground seed for chili, curries, tacos");
        addEntry("Curry Powder", CAT_SPICES, "g", 50.0, 365, "Aromatic blend of turmeric, coriander, cumin");
        addEntry("Garlic Powder", CAT_SPICES, "g", 80.0, 730, "Dehydrated garlic seasoning");
        addEntry("Chili Flakes", CAT_SPICES, "g", 40.0, 730, "Crushed red peppers for heat");
        addEntry("Rosemary", CAT_SPICES, "g", 25.0, 730, "Woody herb for roasts and potatoes");

        // Beverages
        addEntry("Coffee Beans", CAT_BEVERAGES, "g", 250.0, 90, "Roasted coffee beans, keep airtight");
        addEntry("Ground Coffee", CAT_BEVERAGES, "g", 250.0, 60, "Brewing ground coffee");
        addEntry("Black Tea", CAT_BEVERAGES, "g", 100.0, 365, "Dry tea bags or loose leaves");
        addEntry("Green Tea", CAT_BEVERAGES, "g", 100.0, 365, "Unfermented herbal antioxidant tea");
        addEntry("Orange Juice", CAT_BEVERAGES, "ml", 1000.0, 10, "Pasteurized juice, keep chilled");
        addEntry("Almond Milk", CAT_BEVERAGES, "ml", 1000.0, 10, "Plant-based milk alternative");
        addEntry("Oat Milk", CAT_BEVERAGES, "ml", 1000.0, 10, "Creamy plant-based barista milk");

        // Snacks & Sweets
        addEntry("Almonds", CAT_SNACKS, "g", 200.0, 180, "Whole raw or roasted nuts");
        addEntry("Walnuts", CAT_SNACKS, "g", 200.0, 120, "Rich omega-3 nuts, keep cool");
        addEntry("Dark Chocolate", CAT_SNACKS, "g", 100.0, 365, "Cocoa confection, store below 20C");
        addEntry("Potato Chips", CAT_SNACKS, "g", 150.0, 60, "Crispy fried snack, keep bag sealed");
    }

    private static void addEntry(
            String name,
            String category,
            String unit,
            double defaultQuantity,
            int shelfLifeDays,
            String explanation
    ) {
        Suggestion s = new Suggestion(name, category, unit, defaultQuantity, shelfLifeDays, explanation);
        EXACT_CATALOG.put(name.toLowerCase(Locale.US), s);
        COMPREHENSIVE_CATALOG_LIST.add(name);
    }

    @NonNull
    public static List<String> getComprehensiveCatalog() {
        return Collections.unmodifiableList(COMPREHENSIVE_CATALOG_LIST);
    }

    @NonNull
    public static String[] getComprehensiveCatalogArray() {
        return COMPREHENSIVE_CATALOG_LIST.toArray(new String[0]);
    }

    // Predicts category, measurement unit, default quantity, and shelf life based on user typing
    @NonNull
    public static Suggestion predict(@Nullable String rawInput) {
        if (rawInput == null || rawInput.trim().length() < 2) {
            return new Suggestion(
                    "Ingredient",
                    CAT_VEGETABLES,
                    "pcs",
                    1.0,
                    7,
                    "Enter an ingredient name to receive smart category, unit, and shelf-life recommendations."
            );
        }

        String input = rawInput.trim().toLowerCase(Locale.US);

        // 1. Direct Catalog Match
        if (EXACT_CATALOG.containsKey(input)) {
            return EXACT_CATALOG.get(input);
        }

        // 2. Prefix / Substring match in catalog
        for (Map.Entry<String, Suggestion> entry : EXACT_CATALOG.entrySet()) {
            if (entry.getKey().startsWith(input) || input.startsWith(entry.getKey()) || entry.getKey().contains(input)) {
                return entry.getValue();
            }
        }

        // 3. Smart Keyword & Semantic Classification
        String predictedCat = predictCategory(input);
        String predictedUnit = predictUnit(input, predictedCat);
        double predictedQty = predictDefaultQuantity(input, predictedUnit);
        int predictedShelfLife = predictShelfLifeDays(input, predictedCat);

        String titleCase = toTitleCase(rawInput.trim());
        String explanation = "Predicted " + predictedCat + " with standard measurement unit '" + predictedUnit + "'";

        return new Suggestion(
                titleCase,
                predictedCat,
                predictedUnit,
                predictedQty,
                predictedShelfLife,
                explanation
        );
    }

    @NonNull
    private static String predictCategory(@NonNull String input) {
        // Beverages
        if (containsAny(input, "coffee", "tea", "juice", "soda", "water", "drink", "cola",
                "espresso", "latte", "lemonade", "beverage")) {
            return CAT_BEVERAGES;
        }

        // Fruits
        if (containsAny(input, "apple", "banana", "berry", "berr", "orange", "lemon", "lime", "grape",
                "avocado", "mango", "peach", "pear", "melon", "watermelon", "pineapple",
                "strawberr", "blueberr", "raspberr", "cherry", "cherr", "kiwi", "plum", "fruit")) {
            return CAT_FRUITS;
        }

        // Condiments & Sauces
        if (containsAny(input, "oil", "sauce", "mayo", "mayonnaise", "ketchup", "mustard",
                "vinegar", "dressing", "honey", "bbq", "syrup", "jam", "jelly", "peanut butter",
                "spread", "condiment")) {
            return CAT_CONDIMENTS;
        }

        // Dairy & Eggs
        if (containsAny(input, "milk", "cheese", "yogurt", "cheddar", "mozzarella", "parmesan",
                "egg", "butter", "cream", "ricotta", "feta", "gouda", "brie", "curd", "dairy")) {
            return CAT_DAIRY;
        }

        // Meat & Fish
        if (containsAny(input, "chicken", "beef", "pork", "steak", "bacon", "salmon", "tuna",
                "turkey", "shrimp", "sausage", "ham", "mince", "meat", "fish", "lamb",
                "prawn", "duck", "cod", "tilapia", "seafood")) {
            return CAT_MEAT;
        }

        // Bakery
        if (containsAny(input, "bread", "bagel", "tortilla", "bun", "croissant", "pita",
                "baguette", "toast", "roll", "brioche", "muffin", "wrap", "bakery", "pastry")) {
            return CAT_BAKERY;
        }

        // Canned Goods
        if (containsAny(input, "canned", "can of", "tin", "paste", "black bean", "kidney bean",
                "chickpea", "broth", "stock", "coconut milk")) {
            return CAT_CANNED;
        }

        // Spices & Seasonings
        if (containsAny(input, "salt", "pepper", "cinnamon", "oregano", "paprika", "cumin",
                "curry", "rosemary", "thyme", "basil", "chili", "nutmeg", "turmeric",
                "seasoning", "spice", "herb", "vanilla")) {
            return CAT_SPICES;
        }

        // Snacks & Sweets
        if (containsAny(input, "almond", "walnut", "peanut", "cashew", "nut", "chocolate",
                "chip", "popcorn", "cookie", "cracker", "pretzel", "seed", "snack", "candy")) {
            return CAT_SNACKS;
        }

        // Staples & Grains
        if (containsAny(input, "rice", "pasta", "flour", "noodle", "spaghetti", "oat",
                "quinoa", "cereal", "couscous", "penne", "macaroni", "grain", "lentil", "staple")) {
            return CAT_STAPLES;
        }

        // Vegetables
        if (containsAny(input, "spinach", "tomato", "potato", "onion", "garlic", "carrot",
                "broccoli", "lettuce", "pepper", "cucumber", "zucchini", "kale", "cabbage",
                "cauliflower", "celery", "mushroom", "asparagus", "eggplant", "pea", "corn",
                "radish", "ginger", "shallot", "leek", "beet", "veg")) {
            return CAT_VEGETABLES;
        }

        return CAT_STAPLES;
    }

    @NonNull
    private static String predictUnit(@NonNull String input, @NonNull String category) {
        if (containsAny(input, "canned", "tin", "tuna")) {
            return "can";
        }
        if (containsAny(input, "bread", "toast", "bacon")) {
            return "slice";
        }
        if (containsAny(input, "garlic")) {
            return "clove";
        }
        if (containsAny(input, "herb", "parsley", "coriander", "cilantro", "bananas")) {
            return "bunch";
        }
        if (category.equals(CAT_CONDIMENTS) || category.equals(CAT_BEVERAGES)
                || containsAny(input, "oil", "milk", "sauce", "vinegar", "juice", "water", "broth", "cream")) {
            return "ml";
        }
        if (category.equals(CAT_SPICES)) {
            return "g";
        }
        if (category.equals(CAT_STAPLES) || category.equals(CAT_MEAT)
                || containsAny(input, "flour", "sugar", "rice", "pasta", "spinach", "cheese")) {
            return "g";
        }
        if (category.equals(CAT_FRUITS) || category.equals(CAT_VEGETABLES)
                || containsAny(input, "egg", "apple", "tomato", "potato", "onion", "lemon", "lime", "orange")) {
            return "pcs";
        }
        return "pcs";
    }

    private static double predictDefaultQuantity(@NonNull String input, @NonNull String unit) {
        if (unit.equals("can")) return 1.0;
        if (unit.equals("clove")) return 3.0;
        if (unit.equals("slice")) return 4.0;
        if (unit.equals("bunch")) return 1.0;

        if (unit.equals("ml")) {
            if (containsAny(input, "milk", "water", "juice")) return 1000.0;
            if (containsAny(input, "oil")) return 500.0;
            return 250.0;
        }

        if (unit.equals("g")) {
            if (containsAny(input, "flour", "sugar", "rice", "potato")) return 1000.0;
            if (containsAny(input, "meat", "chicken", "beef", "pork", "pasta")) return 500.0;
            if (containsAny(input, "cheese", "butter", "spinach", "mushroom")) return 200.0;
            if (containsAny(input, "salt", "pepper", "spice", "cinnamon")) return 50.0;
            return 250.0;
        }

        if (unit.equals("pcs")) {
            if (containsAny(input, "egg")) return 6.0;
            if (containsAny(input, "apple", "banana", "tomato", "potato", "orange", "lemon")) return 4.0;
            return 2.0;
        }

        return 1.0;
    }

    private static int predictShelfLifeDays(@NonNull String input, @NonNull String category) {
        if (category.equals(CAT_MEAT)) {
            if (containsAny(input, "fish", "salmon", "tuna", "seafood", "shrimp")) return 2;
            if (containsAny(input, "ground", "mince")) return 2;
            return 3;
        }
        if (category.equals(CAT_DAIRY)) {
            if (containsAny(input, "milk")) return 7;
            if (containsAny(input, "yogurt")) return 14;
            if (containsAny(input, "egg")) return 28;
            if (containsAny(input, "parmesan", "cheddar")) return 35;
            return 14;
        }
        if (category.equals(CAT_BAKERY)) return 6;
        if (category.equals(CAT_FRUITS)) {
            if (containsAny(input, "berry", "strawberr", "raspberr")) return 4;
            if (containsAny(input, "banana", "avocado")) return 5;
            return 14;
        }
        if (category.equals(CAT_VEGETABLES)) {
            if (containsAny(input, "spinach", "lettuce", "mushroom")) return 4;
            if (containsAny(input, "potato", "onion", "garlic")) return 30;
            return 10;
        }
        if (category.equals(CAT_CONDIMENTS)) return 180;
        if (category.equals(CAT_CANNED)) return 540;
        if (category.equals(CAT_STAPLES)) return 365;
        if (category.equals(CAT_SPICES)) return 730;
        if (category.equals(CAT_BEVERAGES)) return 14;
        if (category.equals(CAT_SNACKS)) return 90;
        return 14;
    }

    private static boolean containsAny(@NonNull String text, @NonNull String... keywords) {
        for (String kw : keywords) {
            if (text.contains(kw)) {
                return true;
            }
        }
        return false;
    }

    @NonNull
    private static String toTitleCase(@NonNull String input) {
        if (input.isEmpty()) return input;
        String[] words = input.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (!words[i].isEmpty()) {
                sb.append(Character.toUpperCase(words[i].charAt(0)));
                if (words[i].length() > 1) {
                    sb.append(words[i].substring(1).toLowerCase(Locale.US));
                }
                if (i < words.length - 1) sb.append(" ");
            }
        }
        return sb.toString();
    }
}
