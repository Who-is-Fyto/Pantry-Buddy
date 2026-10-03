package com.example.pantrybuddy.domain.engine;

import java.util.HashMap;
import java.util.Map;

// Cleans up ingredient names so singular/plural and spelling differences match
public class IngredientNormalizer {

    private static final Map<String, String> ALIAS_MAP = new HashMap<>();

    static {
        // Oils & Fats
        ALIAS_MAP.put("olive oil", "oil");
        ALIAS_MAP.put("extra virgin olive oil", "oil");
        ALIAS_MAP.put("cooking oil", "oil");
        ALIAS_MAP.put("vegetable oil", "oil");
        ALIAS_MAP.put("canola oil", "oil");
        ALIAS_MAP.put("sunflower oil", "oil");

        // Seasonings
        ALIAS_MAP.put("salt", "salt");
        ALIAS_MAP.put("table salt", "salt");
        ALIAS_MAP.put("sea salt", "salt");
        ALIAS_MAP.put("kosher salt", "salt");
        ALIAS_MAP.put("fine salt", "salt");
        ALIAS_MAP.put("black pepper", "black pepper");
        ALIAS_MAP.put("ground black pepper", "black pepper");
        ALIAS_MAP.put("cracked black pepper", "black pepper");
        ALIAS_MAP.put("pepper", "black pepper");
        ALIAS_MAP.put("curry powder", "curry powder");
        ALIAS_MAP.put("cumin", "cumin");
        ALIAS_MAP.put("ground cumin", "cumin");
        ALIAS_MAP.put("cinnamon", "cinnamon");
        ALIAS_MAP.put("ground cinnamon", "cinnamon");
        ALIAS_MAP.put("chili flakes", "chili");
        ALIAS_MAP.put("red chili flakes", "chili");
        ALIAS_MAP.put("chilli", "chili");

        // Produce & Aromatics
        ALIAS_MAP.put("garlic clove", "garlic");
        ALIAS_MAP.put("garlic cloves", "garlic");
        ALIAS_MAP.put("clove of garlic", "garlic");
        ALIAS_MAP.put("cloves of garlic", "garlic");
        ALIAS_MAP.put("lemon juice", "lemon juice");
        ALIAS_MAP.put("fresh lemon juice", "lemon juice");
        ALIAS_MAP.put("spinach leaves", "spinach");
        ALIAS_MAP.put("baby spinach", "spinach");
        ALIAS_MAP.put("button mushrooms", "mushroom");
        ALIAS_MAP.put("mushrooms", "mushroom");
        ALIAS_MAP.put("white mushrooms", "mushroom");

        // Grains, Legumes & Pastas
        ALIAS_MAP.put("cooked rice", "rice");
        ALIAS_MAP.put("white rice", "rice");
        ALIAS_MAP.put("steamed rice", "rice");
        ALIAS_MAP.put("egg noodles", "noodles");
        ALIAS_MAP.put("spaghetti", "pasta");
        ALIAS_MAP.put("penne", "pasta");
        ALIAS_MAP.put("fusilli", "pasta");
        ALIAS_MAP.put("pasta noodles", "pasta");
        ALIAS_MAP.put("red lentils", "lentil");
        ALIAS_MAP.put("lentils", "lentil");
        ALIAS_MAP.put("canned chickpeas", "chickpea");
        ALIAS_MAP.put("chickpeas", "chickpea");
        ALIAS_MAP.put("garbanzo", "chickpea");
        ALIAS_MAP.put("garbanzo beans", "chickpea");
        ALIAS_MAP.put("white beans", "white bean");
        ALIAS_MAP.put("cannellini beans", "white bean");
        ALIAS_MAP.put("canned tuna", "tuna");
        ALIAS_MAP.put("tuna flakes", "tuna");
        ALIAS_MAP.put("canned sweetcorn", "sweetcorn");
        ALIAS_MAP.put("sweet corn", "sweetcorn");
        ALIAS_MAP.put("corn", "sweetcorn");

        // Dairy & Bakery
        ALIAS_MAP.put("cheddar", "cheese");
        ALIAS_MAP.put("cheddar cheese", "cheese");
        ALIAS_MAP.put("bread slices", "bread");
        ALIAS_MAP.put("white bread", "bread");
        ALIAS_MAP.put("whole wheat bread", "bread");
        ALIAS_MAP.put("soy sauce", "soy sauce");
    }

    // Normalizes name: lowercases, removes brackets/punctuation, stems plurals
    public static String normalize(String name) {
        if (name == null) {
            return "";
        }

        // Remove brackets and punctuation
        String cleaned = name.toLowerCase().replaceAll("\\(.*?\\)", " ");
        cleaned = cleaned.replaceAll("[^a-z0-9\\s]", " ");
        cleaned = cleaned.replaceAll("\\s+", " ").trim();

        if (cleaned.isEmpty()) {
            return "";
        }

        // Direct alias match
        if (ALIAS_MAP.containsKey(cleaned)) {
            return ALIAS_MAP.get(cleaned);
        }

        // Strip adjectives like "fresh", "canned"
        String[] words = cleaned.split(" ");
        if (words.length > 1) {
            String firstWord = words[0];
            if (isDescriptor(firstWord)) {
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i < words.length; i++) {
                    if (sb.length() > 0) sb.append(" ");
                    sb.append(words[i]);
                }
                cleaned = sb.toString();
                if (ALIAS_MAP.containsKey(cleaned)) {
                    return ALIAS_MAP.get(cleaned);
                }
            }
        }

        // Turn plurals into singular
        cleaned = stemPlural(cleaned);

        if (ALIAS_MAP.containsKey(cleaned)) {
            return ALIAS_MAP.get(cleaned);
        }

        return cleaned;
    }

    // Returns true if ingredients match after cleanup
    public static boolean matches(String ingredientA, String ingredientB) {
        String normA = normalize(ingredientA);
        String normB = normalize(ingredientB);

        if (normA.isEmpty() || normB.isEmpty()) {
            return false;
        }

        if (normA.equals(normB)) {
            return true;
        }

        if (normA.contains(normB) || normB.contains(normA)) {
            return true;
        }

        return false;
    }

    // Turns plural words like eggs and tomatoes into egg and tomato
    private static String stemPlural(String word) {
        if (word == null || word.length() <= 3) {
            return word;
        }

        // Irregular plurals
        if (word.endsWith("leaves")) {
            return word.substring(0, word.length() - 6) + "leaf";
        }
        if (word.equals("tomatoes")) {
            return "tomato";
        }
        if (word.equals("potatoes")) {
            return "potato";
        }

        // Do not strip 's' from words where 's' is part of the singular form
        if (word.equals("cheese") || word.equals("rice") || word.equals("hummus") || word.endsWith("ss")) {
            return word;
        }

        // -ies -> -y (e.g. strawberries -> strawberry, berries -> berry)
        if (word.endsWith("ies") && word.length() > 4) {
            return word.substring(0, word.length() - 3) + "y";
        }

        // -es for words ending in ch, sh, x, s, z (e.g. radishes -> radish)
        if (word.endsWith("es") && word.length() > 4) {
            String beforeEs = word.substring(0, word.length() - 2);
            if (beforeEs.endsWith("ch") || beforeEs.endsWith("sh") || beforeEs.endsWith("x") || beforeEs.endsWith("s") || beforeEs.endsWith("z")) {
                return beforeEs;
            }
        }

        // -s for standard plurals (e.g. eggs -> egg, onions -> onion, carrots -> carrot)
        if (word.endsWith("s") && !word.endsWith("ss")) {
            return word.substring(0, word.length() - 1);
        }

        return word;
    }

    private static boolean isDescriptor(String word) {
        switch (word) {
            case "fresh":
            case "dried":
            case "dry":
            case "chopped":
            case "diced":
            case "sliced":
            case "minced":
            case "grated":
            case "shredded":
            case "canned":
            case "raw":
            case "cooked":
            case "large":
            case "medium":
            case "small":
            case "extra":
                return true;
            default:
                return false;
        }
    }
}
