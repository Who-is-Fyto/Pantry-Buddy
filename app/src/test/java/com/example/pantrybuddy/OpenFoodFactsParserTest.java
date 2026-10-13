package com.example.pantrybuddy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.example.pantrybuddy.data.remote.openfoodfacts.OpenFoodFactsParser;
import com.example.pantrybuddy.data.remote.openfoodfacts.ScannedProduct;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

// Unit tests validating Open Food Facts JSON parsing, category mapping, and quantity normalization
public class OpenFoodFactsParserTest {

    @Test
    public void testParseValidProductJson() {
        String json = "{\n" +
                "  \"code\": \"3017620422003\",\n" +
                "  \"status\": 1,\n" +
                "  \"status_verbose\": \"product found\",\n" +
                "  \"product\": {\n" +
                "    \"product_name\": \"Nutella\",\n" +
                "    \"brands\": \"Ferrero\",\n" +
                "    \"quantity\": \"400 g\",\n" +
                "    \"categories\": \"Breakfasts, Spreads, Sweet spreads\",\n" +
                "    \"categories_tags\": [\"en:breakfasts\", \"en:spreads\", \"en:sweet-spreads\"],\n" +
                "    \"image_url\": \"https://images.openfoodfacts.org/nutella.jpg\"\n" +
                "  }\n" +
                "}";

        ScannedProduct product = OpenFoodFactsParser.parseJson(json);
        assertNotNull(product);
        assertEquals("3017620422003", product.getBarcode());
        assertEquals("Nutella", product.getName());
        assertEquals("Ferrero", product.getBrand());
        assertEquals(400.0, product.getQuantity(), 0.001);
        assertEquals("g", product.getUnit());
        assertEquals("Snacks & Sweets", product.getCategory());
        assertEquals("https://images.openfoodfacts.org/nutella.jpg", product.getImageUrl());
        assertEquals("Nutella (Ferrero)", product.getDisplayTitle());
    }

    @Test
    public void testParseProductNotFoundJson() {
        String json = "{\n" +
                "  \"code\": \"0000000000000\",\n" +
                "  \"status\": 0,\n" +
                "  \"status_verbose\": \"product not found\"\n" +
                "}";

        ScannedProduct product = OpenFoodFactsParser.parseJson(json);
        assertNull(product);
    }

    @Test
    public void testParseNullOrEmptyJson() {
        assertNull(OpenFoodFactsParser.parseJson(null));
        assertNull(OpenFoodFactsParser.parseJson(""));
        assertNull(OpenFoodFactsParser.parseJson("   "));
        assertNull(OpenFoodFactsParser.parseJson("{ invalid json }"));
    }

    @Test
    public void testParseEnglishOrGenericNameFallback() {
        String json = "{\n" +
                "  \"code\": \"12345\",\n" +
                "  \"status\": 1,\n" +
                "  \"product\": {\n" +
                "    \"product_name\": \"\",\n" +
                "    \"product_name_en\": \"Organic Spinach\",\n" +
                "    \"quantity\": \"250 g\",\n" +
                "    \"categories\": \"Vegetables\"\n" +
                "  }\n" +
                "}";

        ScannedProduct product = OpenFoodFactsParser.parseJson(json);
        assertNotNull(product);
        assertEquals("Organic Spinach", product.getName());
        assertEquals("Vegetables", product.getCategory());
        assertEquals(250.0, product.getQuantity(), 0.001);
        assertEquals("g", product.getUnit());
    }

    @Test
    public void testQuantityParsingVariants() {
        OpenFoodFactsParser.ParsedQuantity q1 = OpenFoodFactsParser.parseQuantity("500 g");
        assertEquals(500.0, q1.amount, 0.001);
        assertEquals("g", q1.unit);

        OpenFoodFactsParser.ParsedQuantity q2 = OpenFoodFactsParser.parseQuantity("1.5 kg");
        assertEquals(1.5, q2.amount, 0.001);
        assertEquals("kg", q2.unit);

        OpenFoodFactsParser.ParsedQuantity q3 = OpenFoodFactsParser.parseQuantity("250 ml");
        assertEquals(250.0, q3.amount, 0.001);
        assertEquals("ml", q3.unit);

        OpenFoodFactsParser.ParsedQuantity q4 = OpenFoodFactsParser.parseQuantity("1 L");
        assertEquals(1.0, q4.amount, 0.001);
        assertEquals("l", q4.unit);

        OpenFoodFactsParser.ParsedQuantity q5 = OpenFoodFactsParser.parseQuantity("12 eggs");
        assertEquals(12.0, q5.amount, 0.001);
        assertEquals("pcs", q5.unit);

        OpenFoodFactsParser.ParsedQuantity q6 = OpenFoodFactsParser.parseQuantity("100");
        assertEquals(100.0, q6.amount, 0.001);
        assertEquals("g", q6.unit);

        OpenFoodFactsParser.ParsedQuantity q7 = OpenFoodFactsParser.parseQuantity("6");
        assertEquals(6.0, q7.amount, 0.001);
        assertEquals("pcs", q7.unit);

        OpenFoodFactsParser.ParsedQuantity q8 = OpenFoodFactsParser.parseQuantity(null);
        assertEquals(1.0, q8.amount, 0.001);
        assertEquals("pcs", q8.unit);
    }

    @Test
    public void testCategoryMappingClassifications() {
        // Dairy & Eggs
        assertEquals("Dairy & Eggs", OpenFoodFactsParser.mapCategory(
                Collections.singletonList("en:cheeses"), "Dairy products", "Cheddar"
        ));
        assertEquals("Dairy & Eggs", OpenFoodFactsParser.mapCategory(
                Collections.singletonList("en:eggs"), "Fresh eggs", "Free Range Eggs"
        ));
        assertEquals("Dairy & Eggs", OpenFoodFactsParser.mapCategory(
                Collections.singletonList("en:milks"), "Whole milk", "Milk"
        ));

        // Meat & Fish
        assertEquals("Meat & Fish", OpenFoodFactsParser.mapCategory(
                Collections.singletonList("en:poultry"), "Poultry", "Chicken Breast"
        ));
        assertEquals("Meat & Fish", OpenFoodFactsParser.mapCategory(
                Collections.singletonList("en:fishes"), "Canned tuna", "Skipjack Tuna"
        ));

        // Condiments & Sauces
        assertEquals("Condiments & Sauces", OpenFoodFactsParser.mapCategory(
                Collections.singletonList("en:condiments"), "Condiments", "Ketchup"
        ));
        assertEquals("Condiments & Sauces", OpenFoodFactsParser.mapCategory(
                Collections.singletonList("en:sauces"), "Salad dressings", "Mustard Sauce"
        ));

        // Spices & Seasonings
        assertEquals("Spices & Seasonings", OpenFoodFactsParser.mapCategory(
                Collections.singletonList("en:spices"), "Spices", "Black Pepper"
        ));
        assertEquals("Spices & Seasonings", OpenFoodFactsParser.mapCategory(
                Collections.singletonList("en:herbs"), "Dried herbs", "Oregano"
        ));

        // Fruits
        assertEquals("Fruits", OpenFoodFactsParser.mapCategory(
                Collections.singletonList("en:fruits"), "Fresh fruits", "Red Apples"
        ));

        // Vegetables
        assertEquals("Vegetables", OpenFoodFactsParser.mapCategory(
                Collections.singletonList("en:vegetables"), "Fresh vegetables", "Baby Spinach"
        ));

        // Staples & Grains
        assertEquals("Staples & Grains", OpenFoodFactsParser.mapCategory(
                Collections.singletonList("en:pastas"), "Italian pasta", "Spaghetti"
        ));
        assertEquals("Staples & Grains", OpenFoodFactsParser.mapCategory(
                Arrays.asList("en:cereals", "en:grains"), "Rice", "Basmati Rice"
        ));
    }
}
