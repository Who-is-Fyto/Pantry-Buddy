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

    // Maps Open Food Facts taxonomy tags and category strings to PantryBuddy 5 core categories
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

        // 1. Dairy & Eggs
        if (text.contains("dairy") || text.contains("lait") || text.contains("milk")
                || text.contains("cheese") || text.contains("fromage") || text.contains("egg")
                || text.contains("oeuf") || text.contains("butter") || text.contains("beurre")
                || text.contains("yogurt") || text.contains("yaourt") || text.contains("cream")) {
            return "Dairy & Eggs";
        }

        // 2. Meat & Fish
        if (text.contains("meat") || text.contains("viande") || text.contains("poultry")
                || text.contains("chicken") || text.contains("poulet") || text.contains("beef")
                || text.contains("boeuf") || text.contains("pork") || text.contains("porc")
                || text.contains("fish") || text.contains("poisson") || text.contains("seafood")
                || text.contains("tuna") || text.contains("thon") || text.contains("salmon")
                || text.contains("saumon") || text.contains("sausage") || text.contains("saucisse")) {
            return "Meat & Fish";
        }

        // 3. Spices & Seasonings
        if (text.contains("spice") || text.contains("epice") || text.contains("herb")
                || text.contains("seasoning") || text.contains("assaisonnement") || text.contains("salt")
                || text.contains("sel") || text.contains("pepper") || text.contains("poivre")
                || text.contains("sauce") || text.contains("condiment") || text.contains("vinegar")
                || text.contains("vinaigre") || text.contains("mustard") || text.contains("moutarde")
                || text.contains("mayonnaise") || text.contains("curry") || text.contains("cinnamon")) {
            return "Spices & Seasonings";
        }

        // 4. Vegetables & Fruits
        if (text.contains("vegetable") || text.contains("legume") || text.contains("fruit")
                || text.contains("salad") || text.contains("salade") || text.contains("tomato")
                || text.contains("tomate") || text.contains("potato") || text.contains("pomme de terre")
                || text.contains("onion") || text.contains("oignon") || text.contains("carrot")
                || text.contains("carotte") || text.contains("spinach") || text.contains("epinard")
                || text.contains("mushroom") || text.contains("champignon") || text.contains("apple")
                || text.contains("pomme") || text.contains("banana") || text.contains("banane")) {
            return "Vegetables";
        }

        // 5. Staples (Cereals, Grains, Pasta, Bakery, Oils, Canned pantry staples)
        return "Staples";
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
        if (unit.equals("tbsp") || unit.equals("tablespoon")) {
            return "tbsp";
        }
        if (unit.equals("tsp") || unit.equals("teaspoon")) {
            return "tsp";
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
