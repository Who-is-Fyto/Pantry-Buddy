package com.example.pantrybuddy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.pantrybuddy.domain.engine.SmartFoodAssistant;

import org.junit.Test;

public class SmartFoodAssistantTest {

    @Test
    public void testExactCatalogLookups() {
        SmartFoodAssistant.Suggestion milk = SmartFoodAssistant.predict("Milk");
        assertNotNull(milk);
        assertEquals(SmartFoodAssistant.CAT_DAIRY, milk.category);
        assertEquals("ml", milk.unit);
        assertEquals(1000.0, milk.defaultQuantity, 0.001);
        assertEquals(7, milk.shelfLifeDays);

        SmartFoodAssistant.Suggestion chicken = SmartFoodAssistant.predict("Chicken Breast");
        assertNotNull(chicken);
        assertEquals(SmartFoodAssistant.CAT_MEAT, chicken.category);
        assertEquals("g", chicken.unit);
        assertEquals(500.0, chicken.defaultQuantity, 0.001);

        SmartFoodAssistant.Suggestion apple = SmartFoodAssistant.predict("Apple");
        assertNotNull(apple);
        assertEquals(SmartFoodAssistant.CAT_FRUITS, apple.category);
        assertEquals("pcs", apple.unit);
        assertEquals(4.0, apple.defaultQuantity, 0.001);

        SmartFoodAssistant.Suggestion oil = SmartFoodAssistant.predict("Olive Oil");
        assertNotNull(oil);
        assertEquals(SmartFoodAssistant.CAT_CONDIMENTS, oil.category);
        assertEquals("ml", oil.unit);

        SmartFoodAssistant.Suggestion bread = SmartFoodAssistant.predict("White Bread");
        assertNotNull(bread);
        assertEquals(SmartFoodAssistant.CAT_BAKERY, bread.category);

        SmartFoodAssistant.Suggestion coffee = SmartFoodAssistant.predict("Ground Coffee");
        assertNotNull(coffee);
        assertEquals(SmartFoodAssistant.CAT_BEVERAGES, coffee.category);

        SmartFoodAssistant.Suggestion chips = SmartFoodAssistant.predict("Potato Chips");
        assertNotNull(chips);
        assertEquals(SmartFoodAssistant.CAT_SNACKS, chips.category);
    }

    @Test
    public void testKeywordSemanticPredictions() {
        // Unknown or complex fruit
        SmartFoodAssistant.Suggestion berries = SmartFoodAssistant.predict("wild fresh blueberries");
        assertEquals(SmartFoodAssistant.CAT_FRUITS, berries.category);

        // Unknown dairy product
        SmartFoodAssistant.Suggestion yogurt = SmartFoodAssistant.predict("organic greek yogurt cup");
        assertEquals(SmartFoodAssistant.CAT_DAIRY, yogurt.category);

        // Unknown meat item
        SmartFoodAssistant.Suggestion mince = SmartFoodAssistant.predict("lean ground beef mince");
        assertEquals(SmartFoodAssistant.CAT_MEAT, mince.category);

        // Unknown bakery item
        SmartFoodAssistant.Suggestion baguette = SmartFoodAssistant.predict("crusty french baguette");
        assertEquals(SmartFoodAssistant.CAT_BAKERY, baguette.category);

        // Unknown canned product
        SmartFoodAssistant.Suggestion cannedBeans = SmartFoodAssistant.predict("canned kidney beans");
        assertEquals(SmartFoodAssistant.CAT_CANNED, cannedBeans.category);

        // Unknown condiment
        SmartFoodAssistant.Suggestion mayo = SmartFoodAssistant.predict("creamy garlic mayonnaise");
        assertEquals(SmartFoodAssistant.CAT_CONDIMENTS, mayo.category);

        // Unknown spice
        SmartFoodAssistant.Suggestion paprika = SmartFoodAssistant.predict("smoked spanish paprika");
        assertEquals(SmartFoodAssistant.CAT_SPICES, paprika.category);

        // Unknown beverage
        SmartFoodAssistant.Suggestion tea = SmartFoodAssistant.predict("iced green tea lemon");
        assertEquals(SmartFoodAssistant.CAT_BEVERAGES, tea.category);

        // Unknown snack
        SmartFoodAssistant.Suggestion walnuts = SmartFoodAssistant.predict("roasted walnut halves");
        assertEquals(SmartFoodAssistant.CAT_SNACKS, walnuts.category);

        // Unknown staple
        SmartFoodAssistant.Suggestion pasta = SmartFoodAssistant.predict("whole wheat fusilli pasta");
        assertEquals(SmartFoodAssistant.CAT_STAPLES, pasta.category);
    }

    @Test
    public void testSafeHandlingOfNullAndBlank() {
        SmartFoodAssistant.Suggestion nullResult = SmartFoodAssistant.predict(null);
        assertNotNull(nullResult);
        assertEquals("Ingredient", nullResult.matchedName);

        SmartFoodAssistant.Suggestion emptyResult = SmartFoodAssistant.predict("   ");
        assertNotNull(emptyResult);
        assertEquals("Ingredient", emptyResult.matchedName);
    }

    @Test
    public void testCatalogCompleteness() {
        String[] catalog = SmartFoodAssistant.getComprehensiveCatalogArray();
        assertNotNull(catalog);
        assertTrue(catalog.length >= 100);
    }

    @Test
    public void testSummaryMicrocopyFormatting() {
        SmartFoodAssistant.Suggestion suggestion = SmartFoodAssistant.predict("Milk");
        assertNotNull(suggestion);
        String summary = suggestion.getSummaryText();
        assertNotNull(summary);
        assertTrue(summary.contains("Dairy & Eggs"));
        assertTrue(summary.contains("1000 ml"));
        assertTrue(summary.contains("7 days"));
    }
}
