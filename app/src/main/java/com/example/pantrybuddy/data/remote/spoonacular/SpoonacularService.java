package com.example.pantrybuddy.data.remote.spoonacular;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.pantrybuddy.BuildConfig;
import com.example.pantrybuddy.domain.model.RecipeWithIngredients;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// REST client communicating with Spoonacular Recipe API endpoints
public class SpoonacularService {

    private static final String BASE_URL = "https://api.spoonacular.com/recipes/";
    private static final int TIMEOUT_MS = 10000;

    private static volatile SpoonacularService instance;
    private final ExecutorService executor;
    private final Handler mainHandler;
    private final String apiKey;

    public interface RecipesCallback {
        void onSuccess(@NonNull List<RecipeWithIngredients> recipes);
        void onError(@NonNull String errorMessage);
    }

    public interface RecipeDetailCallback {
        void onSuccess(@NonNull RecipeWithIngredients recipe);
        void onError(@NonNull String errorMessage);
    }

    public SpoonacularService() {
        this(BuildConfig.SPOONACULAR_API_KEY);
    }

    public SpoonacularService(@NonNull String apiKey) {
        this.apiKey = apiKey.trim();
        this.executor = Executors.newCachedThreadPool();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public static SpoonacularService getInstance() {
        if (instance == null) {
            synchronized (SpoonacularService.class) {
                if (instance == null) {
                    instance = new SpoonacularService();
                }
            }
        }
        return instance;
    }

    public boolean hasValidApiKey() {
        return apiKey != null && !apiKey.isEmpty() && !apiKey.equals("DEMO_KEY");
    }

    // Searches recipes by ingredients list with comprehensive details in a single query
    public void searchRecipesByIngredients(@NonNull List<String> ingredientNames, @NonNull RecipesCallback callback) {
        if (!hasValidApiKey()) {
            callback.onError("Missing Spoonacular API key");
            return;
        }

        executor.execute(() -> {
            try {
                List<RecipeWithIngredients> results = searchRecipesByIngredientsSync(ingredientNames);
                mainHandler.post(() -> callback.onSuccess(results));
            } catch (Exception e) {
                String msg = (e.getMessage() != null) ? e.getMessage() : "Error communicating with Spoonacular";
                mainHandler.post(() -> callback.onError(msg));
            }
        });
    }

    @NonNull
    public List<RecipeWithIngredients> searchRecipesByIngredientsSync(@NonNull List<String> ingredientNames) throws Exception {
        if (ingredientNames.isEmpty()) {
            return Collections.emptyList();
        }

        StringBuilder ingredientsQuery = new StringBuilder();
        for (int i = 0; i < ingredientNames.size(); i++) {
            ingredientsQuery.append(ingredientNames.get(i));
            if (i < ingredientNames.size() - 1) {
                ingredientsQuery.append(",");
            }
        }

        String encodedIngredients = URLEncoder.encode(ingredientsQuery.toString(), StandardCharsets.UTF_8.name());
        String urlString = BASE_URL + "complexSearch"
                + "?includeIngredients=" + encodedIngredients
                + "&fillIngredients=true"
                + "&addRecipeInformation=true"
                + "&addRecipeInstructions=true"
                + "&sort=min-missing-ingredients"
                + "&ranking=2"
                + "&number=15"
                + "&apiKey=" + apiKey;

        String responseBody = executeHttpGet(urlString);
        return SpoonacularParser.parseComplexSearchJson(responseBody);
    }

    // Fetches full detailed instructions for a specific recipe
    public void fetchRecipeInformation(long recipeId, @NonNull RecipeDetailCallback callback) {
        if (!hasValidApiKey()) {
            callback.onError("Missing Spoonacular API key");
            return;
        }

        executor.execute(() -> {
            try {
                RecipeWithIngredients result = fetchRecipeInformationSync(recipeId);
                if (result != null) {
                    mainHandler.post(() -> callback.onSuccess(result));
                } else {
                    mainHandler.post(() -> callback.onError("Recipe not found"));
                }
            } catch (Exception e) {
                String msg = (e.getMessage() != null) ? e.getMessage() : "Error fetching recipe details";
                mainHandler.post(() -> callback.onError(msg));
            }
        });
    }

    @Nullable
    public RecipeWithIngredients fetchRecipeInformationSync(long recipeId) throws Exception {
        String urlString = BASE_URL + recipeId + "/information"
                + "?includeNutrition=false"
                + "&apiKey=" + apiKey;

        String responseBody = executeHttpGet(urlString);
        return SpoonacularParser.parseRecipeInformationJson(responseBody);
    }

    @NonNull
    private String executeHttpGet(@NonNull String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        try {
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);
            conn.setRequestProperty("Accept", "application/json");

            int code = conn.getResponseCode();
            if (code == 402 || code == 429) {
                throw new Exception("Spoonacular API quota reached (HTTP " + code + ")");
            }
            if (code != HttpURLConnection.HTTP_OK) {
                throw new Exception("Spoonacular returned HTTP " + code);
            }

            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();

            return sb.toString();
        } finally {
            conn.disconnect();
        }
    }
}
