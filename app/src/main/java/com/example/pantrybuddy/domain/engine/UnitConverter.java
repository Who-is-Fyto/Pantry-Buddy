package com.example.pantrybuddy.domain.engine;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

// Helper for converting units like g to kg, ml to liters
public class UnitConverter {

    public enum Dimension {
        MASS,
        VOLUME,
        COUNT,
        UNKNOWN
    }

    private static final Map<String, Double> MASS_TO_GRAMS = new HashMap<>();
    private static final Map<String, Double> VOLUME_TO_ML = new HashMap<>();
    private static final Map<String, Double> COUNT_TO_PCS = new HashMap<>();

    // Average weight in grams for 1 piece of common vegetables/eggs
    private static final Map<String, Double> PIECE_WEIGHT_ESTIMATES = new HashMap<>();

    static {
        // Mass -> grams
        MASS_TO_GRAMS.put("g", 1.0);
        MASS_TO_GRAMS.put("gram", 1.0);
        MASS_TO_GRAMS.put("grams", 1.0);
        MASS_TO_GRAMS.put("kg", 1000.0);
        MASS_TO_GRAMS.put("kilogram", 1000.0);
        MASS_TO_GRAMS.put("kilograms", 1000.0);
        MASS_TO_GRAMS.put("oz", 28.3495);
        MASS_TO_GRAMS.put("ounce", 28.3495);
        MASS_TO_GRAMS.put("ounces", 28.3495);
        MASS_TO_GRAMS.put("lb", 453.592);
        MASS_TO_GRAMS.put("lbs", 453.592);
        MASS_TO_GRAMS.put("pound", 453.592);
        MASS_TO_GRAMS.put("pounds", 453.592);

        // Volume -> ml
        VOLUME_TO_ML.put("ml", 1.0);
        VOLUME_TO_ML.put("milliliter", 1.0);
        VOLUME_TO_ML.put("milliliters", 1.0);
        VOLUME_TO_ML.put("l", 1000.0);
        VOLUME_TO_ML.put("liter", 1000.0);
        VOLUME_TO_ML.put("liters", 1000.0);
        VOLUME_TO_ML.put("tbsp", 15.0);
        VOLUME_TO_ML.put("tablespoon", 15.0);
        VOLUME_TO_ML.put("tablespoons", 15.0);
        VOLUME_TO_ML.put("tsp", 5.0);
        VOLUME_TO_ML.put("teaspoon", 5.0);
        VOLUME_TO_ML.put("teaspoons", 5.0);
        VOLUME_TO_ML.put("cup", 240.0);
        VOLUME_TO_ML.put("cups", 240.0);
        VOLUME_TO_ML.put("fl oz", 29.5735);

        // Count -> pcs
        COUNT_TO_PCS.put("pcs", 1.0);
        COUNT_TO_PCS.put("piece", 1.0);
        COUNT_TO_PCS.put("pieces", 1.0);
        COUNT_TO_PCS.put("item", 1.0);
        COUNT_TO_PCS.put("items", 1.0);
        COUNT_TO_PCS.put("count", 1.0);
        COUNT_TO_PCS.put("clove", 1.0);
        COUNT_TO_PCS.put("cloves", 1.0);
        COUNT_TO_PCS.put("slice", 1.0);
        COUNT_TO_PCS.put("slices", 1.0);

        // Average weights in grams
        PIECE_WEIGHT_ESTIMATES.put("egg", 50.0);
        PIECE_WEIGHT_ESTIMATES.put("tomato", 120.0);
        PIECE_WEIGHT_ESTIMATES.put("potato", 150.0);
        PIECE_WEIGHT_ESTIMATES.put("onion", 120.0);
        PIECE_WEIGHT_ESTIMATES.put("garlic", 5.0);
        PIECE_WEIGHT_ESTIMATES.put("carrot", 80.0);
        PIECE_WEIGHT_ESTIMATES.put("cucumber", 150.0);
    }

    // Standardizes unit abbreviations
    public static String canonicalUnit(String unit) {
        if (unit == null) return "pcs";
        String u = unit.toLowerCase(Locale.US).trim();
        if (MASS_TO_GRAMS.containsKey(u)) {
            if (u.equals("kg") || u.startsWith("kilo")) return "kg";
            return "g";
        }
        if (VOLUME_TO_ML.containsKey(u)) {
            if (u.equals("l") || u.startsWith("liter")) return "l";
            if (u.startsWith("tbsp") || u.startsWith("tablespoon")) return "tbsp";
            if (u.startsWith("tsp") || u.startsWith("teaspoon")) return "tsp";
            return "ml";
        }
        if (COUNT_TO_PCS.containsKey(u)) {
            return "pcs";
        }
        return u;
    }

    // Returns if unit is mass, volume, or count
    public static Dimension getDimension(String unit) {
        if (unit == null) return Dimension.UNKNOWN;
        String u = unit.toLowerCase(Locale.US).trim();
        if (MASS_TO_GRAMS.containsKey(u)) return Dimension.MASS;
        if (VOLUME_TO_ML.containsKey(u)) return Dimension.VOLUME;
        if (COUNT_TO_PCS.containsKey(u)) return Dimension.COUNT;
        return Dimension.UNKNOWN;
    }

    // Converts to base unit: grams, ml, or pieces
    public static double convertToBase(double amount, String unit) {
        if (unit == null) return amount;
        String u = unit.toLowerCase(Locale.US).trim();

        if (MASS_TO_GRAMS.containsKey(u)) {
            return amount * MASS_TO_GRAMS.get(u);
        }
        if (VOLUME_TO_ML.containsKey(u)) {
            return amount * VOLUME_TO_ML.get(u);
        }
        if (COUNT_TO_PCS.containsKey(u)) {
            return amount * COUNT_TO_PCS.get(u);
        }
        return amount;
    }

    // Converts an amount from one unit to another unit
    public static double convertAmount(double fromAmount, String fromUnit, String toUnit, String ingredientNormalizedName) {
        if (fromUnit == null || toUnit == null || fromUnit.equalsIgnoreCase(toUnit)) {
            return fromAmount;
        }

        Dimension fromDim = getDimension(fromUnit);
        Dimension toDim = getDimension(toUnit);

        if (fromDim == toDim && fromDim != Dimension.UNKNOWN) {
            double base = convertToBase(fromAmount, fromUnit);
            String u = toUnit.toLowerCase(Locale.US).trim();
            if (toDim == Dimension.MASS && MASS_TO_GRAMS.containsKey(u)) {
                return base / MASS_TO_GRAMS.get(u);
            }
            if (toDim == Dimension.VOLUME && VOLUME_TO_ML.containsKey(u)) {
                return base / VOLUME_TO_ML.get(u);
            }
            if (toDim == Dimension.COUNT && COUNT_TO_PCS.containsKey(u)) {
                return base / COUNT_TO_PCS.get(u);
            }
            return base;
        }

        if (ingredientNormalizedName != null) {
            String norm = ingredientNormalizedName.toLowerCase(Locale.US).trim();
            if (PIECE_WEIGHT_ESTIMATES.containsKey(norm)) {
                double avgPieceGrams = PIECE_WEIGHT_ESTIMATES.get(norm);
                if (fromDim == Dimension.COUNT && toDim == Dimension.MASS) {
                    double grams = fromAmount * avgPieceGrams;
                    String u = toUnit.toLowerCase(Locale.US).trim();
                    return grams / MASS_TO_GRAMS.getOrDefault(u, 1.0);
                }
                if (fromDim == Dimension.MASS && toDim == Dimension.COUNT) {
                    double grams = convertToBase(fromAmount, fromUnit);
                    return grams / avgPieceGrams;
                }
            }
        }

        return fromAmount;
    }

    // Checks if we have enough of an ingredient in pantry
    public static boolean isSufficient(double pantryAmount, String pantryUnit, double requiredAmount, String requiredUnit, String ingredientNormalizedName) {
        if (requiredAmount <= 0.0) return true;
        if (pantryAmount <= 0.0) return false;

        Dimension pDim = getDimension(pantryUnit);
        Dimension rDim = getDimension(requiredUnit);

        // Direct dimension match (Mass <-> Mass, Volume <-> Volume, Count <-> Count)
        if (pDim == rDim && pDim != Dimension.UNKNOWN) {
            double pantryBase = convertToBase(pantryAmount, pantryUnit);
            double requiredBase = convertToBase(requiredAmount, requiredUnit);
            return pantryBase >= (requiredBase - 0.001);
        }

        // Cross-dimension heuristic: Count <-> Mass (e.g. 2 pcs tomatoes vs 200 g tomatoes)
        if (ingredientNormalizedName != null) {
            String norm = ingredientNormalizedName.toLowerCase(Locale.US).trim();
            if (PIECE_WEIGHT_ESTIMATES.containsKey(norm)) {
                double avgPieceGrams = PIECE_WEIGHT_ESTIMATES.get(norm);

                // Pantry has Count, Recipe requires Mass (g)
                if (pDim == Dimension.COUNT && rDim == Dimension.MASS) {
                    double pantryEstimatedGrams = pantryAmount * avgPieceGrams;
                    double requiredGrams = convertToBase(requiredAmount, requiredUnit);
                    return pantryEstimatedGrams >= (requiredGrams - 0.001);
                }

                // Pantry has Mass (g), Recipe requires Count
                if (pDim == Dimension.MASS && rDim == Dimension.COUNT) {
                    double pantryGrams = convertToBase(pantryAmount, pantryUnit);
                    double requiredEstimatedGrams = requiredAmount * avgPieceGrams;
                    return pantryGrams >= (requiredEstimatedGrams - 0.001);
                }
            }
        }

        // If unknown units, compare raw quantities directly
        return pantryAmount >= (requiredAmount - 0.001);
    }

    // Calculates how many servings this ingredient amount can support
    public static int calculateMaxServings(double pantryAmount, String pantryUnit, double reqAmountSingleServing, String reqUnit, String ingredientNormalizedName) {
        if (reqAmountSingleServing <= 0.0) return Integer.MAX_VALUE;
        if (pantryAmount <= 0.0) return 0;

        Dimension pDim = getDimension(pantryUnit);
        Dimension rDim = getDimension(reqUnit);

        if (pDim == rDim && pDim != Dimension.UNKNOWN) {
            double pantryBase = convertToBase(pantryAmount, pantryUnit);
            double reqBase = convertToBase(reqAmountSingleServing, reqUnit);
            return (int) Math.floor(pantryBase / reqBase);
        }

        if (ingredientNormalizedName != null) {
            String norm = ingredientNormalizedName.toLowerCase(Locale.US).trim();
            if (PIECE_WEIGHT_ESTIMATES.containsKey(norm)) {
                double avgPieceGrams = PIECE_WEIGHT_ESTIMATES.get(norm);
                if (pDim == Dimension.COUNT && rDim == Dimension.MASS) {
                    double pantryGrams = pantryAmount * avgPieceGrams;
                    double reqGrams = convertToBase(reqAmountSingleServing, reqUnit);
                    return (int) Math.floor(pantryGrams / reqGrams);
                }
                if (pDim == Dimension.MASS && rDim == Dimension.COUNT) {
                    double pantryGrams = convertToBase(pantryAmount, pantryUnit);
                    double reqGrams = reqAmountSingleServing * avgPieceGrams;
                    return (int) Math.floor(pantryGrams / reqGrams);
                }
            }
        }

        return (int) Math.floor(pantryAmount / reqAmountSingleServing);
    }
}
