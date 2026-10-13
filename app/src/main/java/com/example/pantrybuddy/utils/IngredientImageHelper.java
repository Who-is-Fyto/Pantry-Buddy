package com.example.pantrybuddy.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.pantrybuddy.R;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

// Utility for providing verified ingredient photography URLs and loading them with Glide
public class IngredientImageHelper {

    private static final String BASE_URL = "https://img.spoonacular.com/ingredients_100x100/";

    private static final Map<String, String> INGREDIENT_FILENAMES = new HashMap<>();

    static {
        // Eggs & Dairy
        INGREDIENT_FILENAMES.put("egg", "egg.png");
        INGREDIENT_FILENAMES.put("eggs", "egg.png");
        INGREDIENT_FILENAMES.put("butter", "butter-sliced.jpg");
        INGREDIENT_FILENAMES.put("milk", "milk.png");
        INGREDIENT_FILENAMES.put("cheese", "cheddar-cheese.png");
        INGREDIENT_FILENAMES.put("cheddar cheese", "cheddar-cheese.png");
        INGREDIENT_FILENAMES.put("cheddar", "cheddar-cheese.png");
        INGREDIENT_FILENAMES.put("mozzarella", "mozzarella.png");
        INGREDIENT_FILENAMES.put("parmesan", "parmesan.png");
        INGREDIENT_FILENAMES.put("yogurt", "plain-yogurt.jpg");
        INGREDIENT_FILENAMES.put("sour cream", "sour-cream.jpg");

        // Produce & Vegetables
        INGREDIENT_FILENAMES.put("tomato", "tomato.png");
        INGREDIENT_FILENAMES.put("tomatoes", "tomato.png");
        INGREDIENT_FILENAMES.put("spinach", "spinach.jpg");
        INGREDIENT_FILENAMES.put("potato", "potatoes-yukon-gold.png");
        INGREDIENT_FILENAMES.put("potatoes", "potatoes-yukon-gold.png");
        INGREDIENT_FILENAMES.put("onion", "brown-onion.png");
        INGREDIENT_FILENAMES.put("onions", "brown-onion.png");
        INGREDIENT_FILENAMES.put("garlic", "garlic.png");
        INGREDIENT_FILENAMES.put("carrot", "carrots.jpg");
        INGREDIENT_FILENAMES.put("carrots", "carrots.jpg");
        INGREDIENT_FILENAMES.put("cabbage", "cabbage.jpg");
        INGREDIENT_FILENAMES.put("cucumber", "cucumber.jpg");
        INGREDIENT_FILENAMES.put("mushroom", "mushrooms.png");
        INGREDIENT_FILENAMES.put("mushrooms", "mushrooms.png");
        INGREDIENT_FILENAMES.put("sweetcorn", "corn.png");
        INGREDIENT_FILENAMES.put("corn", "corn.png");
        INGREDIENT_FILENAMES.put("bell pepper", "bell-pepper-orange.png");
        INGREDIENT_FILENAMES.put("avocado", "avocado.jpg");
        INGREDIENT_FILENAMES.put("peas", "peas.jpg");
        INGREDIENT_FILENAMES.put("broccoli", "broccoli.jpg");
        INGREDIENT_FILENAMES.put("ginger", "ginger.png");
        INGREDIENT_FILENAMES.put("lemon", "lemon.png");
        INGREDIENT_FILENAMES.put("lemon juice", "lemon-juice.jpg");
        INGREDIENT_FILENAMES.put("lime", "lime.jpg");
        INGREDIENT_FILENAMES.put("lime juice", "lime.jpg");
        INGREDIENT_FILENAMES.put("apple", "apple.jpg");
        INGREDIENT_FILENAMES.put("banana", "bananas.jpg");

        // Pantry Staples & Grains
        INGREDIENT_FILENAMES.put("rice", "uncooked-white-rice.png");
        INGREDIENT_FILENAMES.put("cooked rice", "uncooked-white-rice.png");
        INGREDIENT_FILENAMES.put("bread", "white-bread.jpg");
        INGREDIENT_FILENAMES.put("pasta", "spaghetti.jpg");
        INGREDIENT_FILENAMES.put("spaghetti", "spaghetti.jpg");
        INGREDIENT_FILENAMES.put("noodles", "egg-noodles.jpg");
        INGREDIENT_FILENAMES.put("egg noodles", "egg-noodles.jpg");
        INGREDIENT_FILENAMES.put("flour", "flour.png");
        INGREDIENT_FILENAMES.put("sugar", "sugar-in-bowl.png");
        INGREDIENT_FILENAMES.put("oats", "rolled-oats.jpg");
        INGREDIENT_FILENAMES.put("oatmeal", "rolled-oats.jpg");

        // Oils & Condiments
        INGREDIENT_FILENAMES.put("oil", "olive-oil.jpg");
        INGREDIENT_FILENAMES.put("olive oil", "olive-oil.jpg");
        INGREDIENT_FILENAMES.put("cooking oil", "vegetable-oil.jpg");
        INGREDIENT_FILENAMES.put("vegetable oil", "vegetable-oil.jpg");
        INGREDIENT_FILENAMES.put("mayonnaise", "mayonnaise.png");
        INGREDIENT_FILENAMES.put("soy sauce", "soy-sauce.jpg");
        INGREDIENT_FILENAMES.put("vinegar", "vinegar-(white).jpg");
        INGREDIENT_FILENAMES.put("honey", "honey.png");

        // Legumes & Beans
        INGREDIENT_FILENAMES.put("chickpea", "chickpeas.png");
        INGREDIENT_FILENAMES.put("chickpeas", "chickpeas.png");
        INGREDIENT_FILENAMES.put("lentil", "lentils-brown.jpg");
        INGREDIENT_FILENAMES.put("lentils", "lentils-brown.jpg");
        INGREDIENT_FILENAMES.put("red lentils", "lentils-brown.jpg");
        INGREDIENT_FILENAMES.put("white beans", "cannellini-beans.jpg");
        INGREDIENT_FILENAMES.put("white bean", "cannellini-beans.jpg");
        INGREDIENT_FILENAMES.put("cannellini beans", "cannellini-beans.jpg");
        INGREDIENT_FILENAMES.put("beans", "kidney-beans.jpg");
        INGREDIENT_FILENAMES.put("black beans", "black-beans.jpg");

        // Meats & Seafood
        INGREDIENT_FILENAMES.put("chicken", "chicken-breasts.png");
        INGREDIENT_FILENAMES.put("chicken breast", "chicken-breasts.png");
        INGREDIENT_FILENAMES.put("chicken breasts", "chicken-breasts.png");
        INGREDIENT_FILENAMES.put("beef", "beef-cubes-raw.png");
        INGREDIENT_FILENAMES.put("ground beef", "beef-cubes-raw.png");
        INGREDIENT_FILENAMES.put("pork", "pork-chops.jpg");
        INGREDIENT_FILENAMES.put("tuna", "canned-tuna.png");
        INGREDIENT_FILENAMES.put("canned tuna", "canned-tuna.png");
        INGREDIENT_FILENAMES.put("salmon", "salmon.png");
        INGREDIENT_FILENAMES.put("shrimp", "shrimp.png");

        // Spices & Herbs
        INGREDIENT_FILENAMES.put("salt", "salt.jpg");
        INGREDIENT_FILENAMES.put("black pepper", "black-pepper.png");
        INGREDIENT_FILENAMES.put("pepper", "black-pepper.png");
        INGREDIENT_FILENAMES.put("cinnamon", "cinnamon.jpg");
        INGREDIENT_FILENAMES.put("cumin", "cumin.jpg");
        INGREDIENT_FILENAMES.put("curry powder", "curry-powder.jpg");
        INGREDIENT_FILENAMES.put("chili", "red-pepper-flakes.jpg");
        INGREDIENT_FILENAMES.put("chili flakes", "red-pepper-flakes.jpg");
        INGREDIENT_FILENAMES.put("red pepper flakes", "red-pepper-flakes.jpg");
        INGREDIENT_FILENAMES.put("rosemary", "rosemary.jpg");
        INGREDIENT_FILENAMES.put("basil", "basil.jpg");
        INGREDIENT_FILENAMES.put("oregano", "oregano.jpg");
        INGREDIENT_FILENAMES.put("parsley", "parsley.jpg");
        INGREDIENT_FILENAMES.put("thyme", "thyme.jpg");
        INGREDIENT_FILENAMES.put("paprika", "paprika.jpg");
    }

    // Resolves an ingredient name to a verified Spoonacular ingredient image URL
    public static String getImageUrlForIngredient(@Nullable String ingredientName, @Nullable String normalizedName) {
        String key = null;
        if (normalizedName != null && !normalizedName.trim().isEmpty()) {
            key = normalizedName.trim().toLowerCase(Locale.US);
        } else if (ingredientName != null && !ingredientName.trim().isEmpty()) {
            key = ingredientName.trim().toLowerCase(Locale.US);
        }

        if (key == null || key.isEmpty()) {
            return null;
        }

        // Direct lookup
        if (INGREDIENT_FILENAMES.containsKey(key)) {
            return BASE_URL + INGREDIENT_FILENAMES.get(key);
        }

        // Partial match lookup
        for (Map.Entry<String, String> entry : INGREDIENT_FILENAMES.entrySet()) {
            String candidate = entry.getKey();
            if (key.contains(candidate) || candidate.contains(key)) {
                return BASE_URL + entry.getValue();
            }
        }

        // Heuristic fallback using sanitized ingredient name
        String sanitized = key.replaceAll("[^a-zA-Z0-9]+", "-").replaceAll("^-+|-+$", "");
        if (!sanitized.isEmpty()) {
            return BASE_URL + sanitized + ".jpg";
        }

        return null;
    }

    public static void loadIngredientImage(@Nullable ImageView imageView, @Nullable String ingredientName) {
        loadIngredientImage(imageView, ingredientName, null, 8);
    }

    public static void loadIngredientImage(@Nullable ImageView imageView, @Nullable String ingredientName, @Nullable String normalizedName) {
        loadIngredientImage(imageView, ingredientName, normalizedName, 8);
    }

    public static void loadIngredientImage(@Nullable ImageView imageView,
                                           @Nullable String ingredientName,
                                           @Nullable String normalizedName,
                                           int fallbackPaddingDp) {
        if (imageView == null) {
            return;
        }

        Context context = imageView.getContext();
        if (context == null) {
            return;
        }

        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            if (activity.isFinishing() || activity.isDestroyed()) {
                return;
            }
        }

        String url = getImageUrlForIngredient(ingredientName, normalizedName);
        if (url == null || url.trim().isEmpty()) {
            applyFallback(imageView, fallbackPaddingDp);
            return;
        }

        imageView.clearColorFilter();
        imageView.setImageTintList(null);
        imageView.setPadding(0, 0, 0, 0);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        try {
            Glide.with(context)
                    .load(url)
                    .centerCrop()
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                     Target<Drawable> target, boolean isFirstResource) {
                            applyFallback(imageView, fallbackPaddingDp);
                            return true;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model,
                                                       Target<Drawable> target, DataSource dataSource,
                                                       boolean isFirstResource) {
                            imageView.clearColorFilter();
                            imageView.setImageTintList(null);
                            imageView.setPadding(0, 0, 0, 0);
                            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            return false;
                        }
                    })
                    .into(imageView);
        } catch (Exception e) {
            applyFallback(imageView, fallbackPaddingDp);
        }
    }

    public static void applyFallback(@Nullable ImageView imageView, int paddingDp) {
        if (imageView == null) {
            return;
        }

        Context context = imageView.getContext();
        if (context == null) {
            return;
        }

        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            if (activity.isFinishing() || activity.isDestroyed()) {
                return;
            }
        }

        try {
            Glide.with(context).clear(imageView);
        } catch (Exception ignored) {
        }

        int paddingPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                paddingDp,
                context.getResources().getDisplayMetrics()
        );

        imageView.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setImageResource(R.drawable.ic_restaurant);
        imageView.setColorFilter(
                ContextCompat.getColor(context, R.color.colorTextTertiary),
                android.graphics.PorterDuff.Mode.SRC_IN
        );
    }
}
