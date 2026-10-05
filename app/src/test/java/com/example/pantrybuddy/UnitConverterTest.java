package com.example.pantrybuddy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.pantrybuddy.domain.engine.UnitConverter;

import org.junit.Test;

public class UnitConverterTest {

    @Test
    public void testConvertToBase_mass() {
        assertEquals(500.0, UnitConverter.convertToBase(0.5, "kg"), 0.001);
        assertEquals(1000.0, UnitConverter.convertToBase(1.0, "kg"), 0.001);
        assertEquals(250.0, UnitConverter.convertToBase(250.0, "g"), 0.001);
    }

    @Test
    public void testConvertToBase_volume() {
        assertEquals(1000.0, UnitConverter.convertToBase(1.0, "l"), 0.001);
        assertEquals(500.0, UnitConverter.convertToBase(0.5, "liter"), 0.001);
        assertEquals(30.0, UnitConverter.convertToBase(2.0, "tbsp"), 0.001);
        assertEquals(15.0, UnitConverter.convertToBase(3.0, "tsp"), 0.001);
        assertEquals(240.0, UnitConverter.convertToBase(1.0, "cup"), 0.001);
    }

    @Test
    public void testIsSufficient_sameDimension() {
        // Mass: 0.5 kg (500g) >= 200g
        assertTrue(UnitConverter.isSufficient(0.5, "kg", 200.0, "g", "flour"));
        // Mass: 100g < 200g
        assertFalse(UnitConverter.isSufficient(100.0, "g", 200.0, "g", "flour"));

        // Volume: 1 liter (1000ml) >= 250ml
        assertTrue(UnitConverter.isSufficient(1.0, "l", 250.0, "ml", "milk"));
        // Volume: 1 tbsp (15ml) >= 10ml
        assertTrue(UnitConverter.isSufficient(1.0, "tbsp", 10.0, "ml", "oil"));
        // Volume: 5 ml < 15ml
        assertFalse(UnitConverter.isSufficient(5.0, "ml", 1.0, "tbsp", "oil"));

        // Count: 4 eggs >= 2 eggs
        assertTrue(UnitConverter.isSufficient(4.0, "pcs", 2.0, "pcs", "egg"));
        // Count: 1 egg < 4 eggs
        assertFalse(UnitConverter.isSufficient(1.0, "pcs", 4.0, "pcs", "egg"));
    }

    @Test
    public void testIsSufficient_crossDimensionHeuristics() {
        // User has 2 pcs tomatoes (estimated 240g), recipe needs 200g tomatoes -> Sufficient
        assertTrue(UnitConverter.isSufficient(2.0, "pcs", 200.0, "g", "tomato"));

        // User has 1 pc tomato (estimated 120g), recipe needs 300g tomatoes -> Insufficient
        assertFalse(UnitConverter.isSufficient(1.0, "pcs", 300.0, "g", "tomato"));
    }

    @Test
    public void testCalculateMaxServings() {
        // 4 eggs in pantry, 2 eggs required per 2 servings (so 1 egg per serving)
        int maxServings = UnitConverter.calculateMaxServings(4.0, "pcs", 2.0, "pcs", "egg");
        assertEquals(2, maxServings);

        // 1000 ml milk, 250 ml required
        int milkServings = UnitConverter.calculateMaxServings(1.0, "l", 250.0, "ml", "milk");
        assertEquals(4, milkServings);
    }

    @Test
    public void testConvertAmount() {
        // 1 kg to g -> 1000g
        assertEquals(1000.0, UnitConverter.convertAmount(1.0, "kg", "g", "flour"), 0.001);
        // 500 g to kg -> 0.5kg
        assertEquals(0.5, UnitConverter.convertAmount(500.0, "g", "kg", "flour"), 0.001);
        // 1 l to ml -> 1000ml
        assertEquals(1000.0, UnitConverter.convertAmount(1.0, "l", "ml", "milk"), 0.001);
        // 30 ml to tbsp -> 2 tbsp
        assertEquals(2.0, UnitConverter.convertAmount(30.0, "ml", "tbsp", "oil"), 0.001);
    }

    @Test
    public void testCalculateShortfallAndLeftover() {
        // Need 200g, have 150g -> shortfall is 50g, leftover is 0g
        assertEquals(50.0, UnitConverter.calculateShortfall(150.0, "g", 200.0, "g", "spinach"), 0.001);
        assertEquals(0.0, UnitConverter.calculateLeftover(150.0, "g", 200.0, "g", "spinach"), 0.001);

        // Need 200g, have 300g -> shortfall is 0g, leftover is 100g
        assertEquals(0.0, UnitConverter.calculateShortfall(300.0, "g", 200.0, "g", "tomato"), 0.001);
        assertEquals(100.0, UnitConverter.calculateLeftover(300.0, "g", 200.0, "g", "tomato"), 0.001);

        // Cross unit: Need 100g, have 0.5kg (500g) -> leftover is 400g
        assertEquals(400.0, UnitConverter.calculateLeftover(0.5, "kg", 100.0, "g", "potato"), 0.001);
    }
}
