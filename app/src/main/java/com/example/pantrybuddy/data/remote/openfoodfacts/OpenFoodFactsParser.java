package com.example.pantrybuddy.data.remote.openfoodfacts;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Parser and categorizer for raw Open Food Facts API responses
public class OpenFoodFactsParser {

    private static final Gson gson = new Gson();

    // Regex matching numbers with optional decimal point followed by measurement units
    private static final Pattern QUANTITY_PATTERN = Pattern.compile(
            "([0-9]+(?:[.,][0-9]+)?)\\s*([a-zA-Z]+)?"
    );

    @Nullable
    public static ScannedProduct parseJson(@Nullable String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }

        try {
            OpenFoodFactsDto.Response response = gson.fromJson(json, OpenFoodFactsDto.Response.class);
            return parseResponse(response);
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    public static ScannedProduct parseResponse(@Nullable OpenFoodFactsDto.Response response) {
        if (response == null || response.status != 1 || response.product == null) {
            return null;
        }

        OpenFoodFactsDto.Product product = response.product;
        String barcode = response.code != null ? response.code.trim() : "";

        // Resolve product name with multiple language/generic fallbacks
        String name = "";
        if (product.productName != null && !product.productName.trim().isEmpty()) {
            name = product.productName.trim();
        } else if (product.productNameEn != null && !product.productNameEn.trim().isEmpty()) {
            name = product.productNameEn.trim();
        } else if (product.genericName != null && !product.genericName.trim().isEmpty()) {
            name = product.genericName.trim();
        }

        if (name.isEmpty()) {
            name = "Scanned Item";
        }

        String brand = product.brands != null ? product.brands.trim() : "";
        String category = mapCategory(product.categoriesTags, product.categories, name);

        ParsedQuantity parsedQty = parseQuantity(product.quantity);
        String imageUrl = "";
        if (product.imageFrontUrl != null && !product.imageFrontUrl.trim().isEmpty()) {
            imageUrl = product.imageFrontUrl.trim();
        } else if (product.imageUrl != null && !product.imageUrl.trim().isEmpty()) {
            imageUrl = product.imageUrl.trim();
        }

        return new ScannedProduct(
                barcode,
                name,
                brand,
                parsedQty.amount,
                parsedQty.unit,
                category,
                imageUrl
        );
    }

    // Maps Open Food Facts taxonomy tags and category strings to PantryBuddy 11 core categories
    @NonNull
    public static String mapCategory(@Nullable List<String> categoryTags,
                                     @Nullable String categoriesString,
                                     @NonNull String productName) {
        StringBuilder combined = new StringBuilder();
        if (categoryTags != null) {
            for (String tag : categoryTags) {
                combined.append(" ").append(tag.toLowerCase(Locale.US));
            }
        }
        if (categoriesString != null) {
            combined.append(" ").append(categoriesString.toLowerCase(Locale.US));
        }
        combined.append(" ").append(productName.toLowerCase(Locale.US));

        String text = combined.toString();

        // 1. Beverages
        if (text.contains("beverage") || text.contains("drink") || text.contains("boisson")
                || text.contains("juice") || text.contains("jus") || text.contains("soda")
                || text.contains("coffee") || text.contains("cafe") || text.contains("tea")
                || text.contains("eau") || text.contains("water")) {
            return "Beverages";
        }

        // 2. Snacks & Sweets
        if (text.contains("snack") || text.contains("sweet") || text.contains("candy")
                || text.contains("chocolate") || text.contains("chocolat") || text.contains("biscuit")
                || text.contains("cookie") || text.contains("chips") || text.contains("crisps")
                || text.contains("bonbon") || text.contains("confectionery")) {
            return "Snacks & Sweets";
        }

        // 3. Bakery & Bread
        if (text.contains("bakery") || text.contains("bread") || text.contains("pain")
                || text.contains("boulangerie") || text.contains("croissant") || text.contains("brioche")
                || text.contains("baguette") || text.contains("tortilla") || text.contains("bun")
                || text.contains("pastry") || text.contains("patisserie")) {
            return "Bakery & Bread";
        }

        // 4. Dairy & Eggs
        if (text.contains("dairy") || text.contains("lait") || text.contains("milk")
                || text.contains("cheese") || text.contains("fromage") || text.contains("egg")
                || text.contains("oeuf") || text.contains("butter") || text.contains("beurre")
                || text.contains("yogurt") || text.contains("yaourt") || text.contains("cream")
                || text.contains("creme")) {
            return "Dairy & Eggs";
        }

        // 5. Meat & Fish
        if (text.contains("meat") || text.contains("viande") || text.contains("poultry")
                || text.contains("chicken") || text.contains("poulet") || text.contains("beef")
                || text.contains("boeuf") || text.contains("pork") || text.contains("porc")
                || text.contains("fish") || text.contains("poisson") || text.contains("seafood")
                || text.contains("tuna") || text.contains("thon") || text.contains("salmon")
                || text.contains("saumon") || text.contains("sausage") || text.contains("saucisse")
                || text.contains("ham") || text.contains("jambon") || text.contains("bacon")) {
            return "Meat & Fish";
        }

        // 6. Canned Goods
        if (text.contains("canned") || text.contains("conserve") || text.contains("tin")
                || text.contains("canned-foods") || text.contains("conserves")) {
            return "Canned Goods";
        }

        // 7. Condiments & Sauces
        if (text.contains("sauce") || text.contains("condiment") || text.contains("ketchup")
                || text.contains("mustard") || text.contains("moutarde") || text.contains("mayonnaise")
                || text.contains("mayo") || text.contains("vinegar") || text.contains("vinaigre")
                || text.contains("oil") || text.contains("huile") || text.contains("dressing")
                || text.contains("pesto")) {
            return "Condiments & Sauces";
        }

        // 8. Spices & Seasonings
        if (text.contains("spice") || text.contains("epice") || text.contains("herb")
                || text.contains("herbe") || text.contains("seasoning") || text.contains("assaisonnement")
                || text.contains("salt") || text.contains("sel") || text.contains("pepper")
                || text.contains("poivre") || text.contains("curry") || text.contains("cinnamon")
                || text.contains("paprika") || text.contains("oregano") || text.contains("cumin")) {
            return "Spices & Seasonings";
        }

        // 9. Fruits
        if (text.contains("fruit") || text.contains("apple") || text.contains("pomme")
                || text.contains("banana") || text.contains("banane") || text.contains("berry")
                || text.contains("fraise") || text.contains("strawberry") || text.contains("grape")
                || text.contains("raisin") || text.contains("orange") || text.contains("lemon")
                || text.contains("citron") || text.contains("avocado") || text.contains("avocat")
                || text.contains("mango") || text.contains("peach") || text.contains("melon")) {
            return "Fruits";
        }

        // 10. Vegetables
        if (text.contains("vegetable") || text.contains("legume") || text.contains("salad")
                || text.contains("salade") || text.contains("tomato") || text.contains("tomate")
                || text.contains("potato") || text.contains("pomme de terre") || text.contains("onion")
                || text.contains("oignon") || text.contains("carrot") || text.contains("carotte")
                || text.contains("spinach") || text.contains("epinard") || text.contains("mushroom")
                || text.contains("champignon") || text.contains("garlic") || text.contains("ail")
                || text.contains("cucumber") || text.contains("poivron") || text.contains("broccoli")) {
            return "Vegetables";
        }

        // 11. Fallback to SmartFoodAssistant or Staples & Grains
        com.example.pantrybuddy.domain.engine.SmartFoodAssistant.Suggestion suggestion =
                com.example.pantrybuddy.domain.engine.SmartFoodAssistant.predict(productName);
        if (suggestion != null && suggestion.category != null && !suggestion.category.isEmpty()) {
            return suggestion.category;
        }

        return "Staples & Grains";
    }

    // Parses raw quantity text into numeric value and normalized unit (g, kg, ml, l, pcs)
    @NonNull
    public static ParsedQuantity parseQuantity(@Nullable String rawQuantity) {
        if (rawQuantity == null || rawQuantity.trim().isEmpty()) {
            return new ParsedQuantity(1.0, "pcs");
        }

        Matcher matcher = QUANTITY_PATTERN.matcher(rawQuantity.trim());
        if (matcher.find()) {
            String numberStr = matcher.group(1);
            String unitStr = matcher.group(2);

            if (numberStr != null) {
                try {
                    double amount = Double.parseDouble(numberStr.replace(',', '.'));
                    String unit = normalizeUnit(unitStr, amount);
                    return new ParsedQuantity(amount, unit);
                } catch (NumberFormatException ignored) {
                }
            }
        }

        return new ParsedQuantity(1.0, "pcs");
    }

    @NonNull
    private static String normalizeUnit(@Nullable String rawUnit, double amount) {
        if (rawUnit == null || rawUnit.trim().isEmpty()) {
            // If no unit provided, assume grams for larger weights, otherwise pieces
            return amount >= 20.0 ? "g" : "pcs";
        }

        String unit = rawUnit.trim().toLowerCase(Locale.US);
        if (unit.equals("kg") || unit.equals("kilogram") || unit.equals("kilograms")) {
            return "kg";
        }
        if (unit.equals("g") || unit.equals("gram") || unit.equals("grams") || unit.equals("gr")) {
            return "g";
        }
        if (unit.equals("l") || unit.equals("liter") || unit.equals("liters") || unit.equals("litre") || unit.equals("litres")) {
            return "l";
        }
        if (unit.equals("ml") || unit.equals("milliliter") || unit.equals("milliliters") || unit.equals("cl")) {
            return "ml";
        }
        if (unit.equals("tbsp") || unit.equals("tablespoon") || unit.equals("tablespoons")) {
            return "tbsp";
        }
        if (unit.equals("tsp") || unit.equals("teaspoon") || unit.equals("teaspoons")) {
            return "tsp";
        }
        if (unit.equals("cup") || unit.equals("cups")) {
            return "cup";
        }
        if (unit.equals("oz") || unit.equals("ounce") || unit.equals("ounces")) {
            return "oz";
        }
        if (unit.equals("can") || unit.equals("cans") || unit.equals("tin") || unit.equals("tins")) {
            return "can";
        }
        if (unit.equals("slice") || unit.equals("slices")) {
            return "slice";
        }
        if (unit.equals("clove") || unit.equals("cloves")) {
            return "clove";
        }
        if (unit.equals("bunch") || unit.equals("bunches")) {
            return "bunch";
        }
        if (unit.equals("pinch") || unit.equals("pinches")) {
            return "pinch";
        }

        return "pcs";
    }

    public static class ParsedQuantity {
        public final double amount;
        public final String unit;

        public ParsedQuantity(double amount, @NonNull String unit) {
            this.amount = amount;
            this.unit = unit;
        }
    }
}
