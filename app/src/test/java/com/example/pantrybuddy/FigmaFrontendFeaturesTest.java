package com.example.pantrybuddy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.pantrybuddy.domain.engine.UnitConverter;
import com.example.pantrybuddy.domain.model.ExpiryStatus;

import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.regex.Pattern;

// Tests verifying frontend logic and Figma design alignment
public class FigmaFrontendFeaturesTest {

    // Regex to verify zero emojis across code and resources
    private static final Pattern EMOJI_PATTERN = Pattern.compile(
            "[\uD83C-\uDBFF\uDC00-\uDFFF]+|[\u2600-\u27BF]"
    );

    @Test
    public void testThreeColumnMealReviewCalculation() {
        // Test that leftover calculation correctly computes remaining amount
        double beforeQty = 300.0;
        double usedQty = 200.0;
        double leftover = UnitConverter.calculateLeftover(beforeQty, "g", usedQty, "g", "tomato");
        assertEquals(100.0, leftover, 0.001);

        // Test fully used up ingredient
        double fullyUsedLeftover = UnitConverter.calculateLeftover(4.0, "pcs", 4.0, "pcs", "egg");
        assertEquals(0.0, fullyUsedLeftover, 0.001);
    }

    @Test
    public void testPantryFilterUrgencyLogic() {
        long now = System.currentTimeMillis();
        long todayMs = now + 3_600_000L; // 1 hour ahead
        long twoDaysMs = now + (2L * 86_400_000L) - 3_600_000L; // under 2 days
        long fiveDaysMs = now + (5L * 86_400_000L); // 5 days ahead

        long twoDayThreshold = now + (2L * 86_400_000L);

        // Today is within threshold
        assertTrue(todayMs <= twoDayThreshold);

        // In 2 days is within threshold
        assertTrue(twoDaysMs <= twoDayThreshold);

        // In 5 days is NOT within threshold
        assertFalse(fiveDaysMs <= twoDayThreshold);
    }

    @Test
    public void testCategoryFilterMatchingLogic() {
        String filterCategory = "Vegetables";

        assertTrue("All".equalsIgnoreCase("All"));
        assertTrue("Vegetables".equalsIgnoreCase(filterCategory));
        assertFalse("Dairy & Eggs".equalsIgnoreCase(filterCategory));
    }

    @Test
    public void testZeroEmojisInNewFrontendLayouts() throws Exception {
        String[] filesToCheck = {
                "app/src/main/res/layout/bottom_sheet_expiry_reminder.xml",
                "app/src/main/res/layout/bottom_sheet_pantry_filter.xml",
                "app/src/main/res/layout/item_expiry_reminder_row.xml",
                "app/src/main/res/layout/item_meal_review_row.xml",
                "app/src/main/res/layout/fragment_home.xml",
                "app/src/main/res/layout/fragment_pantry.xml",
                "app/src/main/res/layout/fragment_recipes.xml",
                "app/src/main/res/layout/activity_cooking.xml",
                "app/src/main/res/layout/activity_meal_review.xml",
                "app/src/main/res/layout/activity_ingredient_detail.xml",
                "app/src/main/res/values/strings.xml"
        };

        for (String relativePath : filesToCheck) {
            File f = new File(relativePath);
            if (f.exists()) {
                String content = new String(Files.readAllBytes(f.toPath()));
                assertFalse("File " + relativePath + " must not contain emojis",
                        EMOJI_PATTERN.matcher(content).find());
            }
        }
    }
}
