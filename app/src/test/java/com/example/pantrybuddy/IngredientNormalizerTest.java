package com.example.pantrybuddy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.pantrybuddy.domain.engine.IngredientNormalizer;

import org.junit.Test;

public class IngredientNormalizerTest {

    @Test
    public void testPluralStemming_standardPlurals() {
        assertEquals("egg", IngredientNormalizer.normalize("eggs"));
        assertEquals("egg", IngredientNormalizer.normalize("Egg"));
        assertEquals("onion", IngredientNormalizer.normalize("onions"));
        assertEquals("carrot", IngredientNormalizer.normalize("carrots"));
        assertEquals("mushroom", IngredientNormalizer.normalize("mushrooms"));
    }

    @Test
    public void testPluralStemming_irregularAndEs() {
        assertEquals("tomato", IngredientNormalizer.normalize("tomatoes"));
        assertEquals("tomato", IngredientNormalizer.normalize("Tomato"));
        assertEquals("potato", IngredientNormalizer.normalize("potatoes"));
        assertEquals("leaf", IngredientNormalizer.normalize("leaves"));
        assertEquals("strawberry", IngredientNormalizer.normalize("strawberries"));
    }

    @Test
    public void testWordsPreservingEndingS() {
        assertEquals("rice", IngredientNormalizer.normalize("rice"));
        assertEquals("cheese", IngredientNormalizer.normalize("cheese"));
        assertEquals("hummus", IngredientNormalizer.normalize("hummus"));
    }

    @Test
    public void testAliasesAndDescriptors() {
        assertEquals("oil", IngredientNormalizer.normalize("olive oil"));
        assertEquals("oil", IngredientNormalizer.normalize("extra virgin olive oil"));
        assertEquals("oil", IngredientNormalizer.normalize("cooking oil"));
        assertEquals("oil", IngredientNormalizer.normalize("vegetable oil"));

        assertEquals("salt", IngredientNormalizer.normalize("table salt"));
        assertEquals("salt", IngredientNormalizer.normalize("sea salt"));
        assertEquals("salt", IngredientNormalizer.normalize("kosher salt"));

        assertEquals("black pepper", IngredientNormalizer.normalize("black pepper"));
        assertEquals("black pepper", IngredientNormalizer.normalize("ground black pepper"));
        assertEquals("black pepper", IngredientNormalizer.normalize("cracked black pepper"));

        assertEquals("garlic", IngredientNormalizer.normalize("cloves of garlic"));
        assertEquals("garlic", IngredientNormalizer.normalize("garlic cloves"));
        assertEquals("garlic", IngredientNormalizer.normalize("fresh garlic"));

        assertEquals("spinach", IngredientNormalizer.normalize("baby spinach"));
        assertEquals("spinach", IngredientNormalizer.normalize("spinach leaves"));
    }

    @Test
    public void testMatches() {
        assertTrue(IngredientNormalizer.matches("tomato", "tomatoes"));
        assertTrue(IngredientNormalizer.matches("Tomato", "tomato"));
        assertTrue(IngredientNormalizer.matches("eggs", "Egg"));
        assertTrue(IngredientNormalizer.matches("olive oil", "oil"));
        assertTrue(IngredientNormalizer.matches("cooking oil", "olive oil"));
        assertTrue(IngredientNormalizer.matches("cloves of garlic", "garlic"));

        assertFalse(IngredientNormalizer.matches("tomato", "potato"));
        assertFalse(IngredientNormalizer.matches("spinach", "egg"));
    }
}
