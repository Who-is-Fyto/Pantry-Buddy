package com.example.pantrybuddy.data.remote.openfoodfacts;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// HTTP client service for querying the Open Food Facts API v2
public class OpenFoodFactsService {

    private static final String API_BASE_URL = "https://world.openfoodfacts.org/api/v2/product/";
    private static final String USER_AGENT = "PantryBuddy - Android - Version 1.0 (pantrybuddy-app@example.com)";
    private static final int TIMEOUT_MS = 8000;

    private static volatile OpenFoodFactsService instance;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public interface ProductCallback {
        void onSuccess(@NonNull ScannedProduct product);
        void onNotFound(@NonNull String barcode);
        void onError(@NonNull String errorMessage);
    }

    public OpenFoodFactsService() {
        this.executor = Executors.newCachedThreadPool();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public static OpenFoodFactsService getInstance() {
        if (instance == null) {
            synchronized (OpenFoodFactsService.class) {
                if (instance == null) {
                    instance = new OpenFoodFactsService();
                }
            }
        }
        return instance;
    }

    // Asynchronously queries Open Food Facts API on background thread
    public void fetchProduct(@NonNull String barcode, @NonNull ProductCallback callback) {
        final String cleanBarcode = barcode.trim();
        if (cleanBarcode.isEmpty()) {
            callback.onError("Invalid empty barcode");
            return;
        }

        executor.execute(() -> {
            try {
                ScannedProduct product = fetchProductSync(cleanBarcode);
                if (product != null) {
                    mainHandler.post(() -> callback.onSuccess(product));
                } else {
                    mainHandler.post(() -> callback.onNotFound(cleanBarcode));
                }
            } catch (Exception e) {
                String errorMsg = e.getMessage() != null ? e.getMessage() : "Network error";
                mainHandler.post(() -> callback.onError(errorMsg));
            }
        });
    }

    // Synchronous HTTP GET request for background tasks and testing
    @Nullable
    public ScannedProduct fetchProductSync(@NonNull String barcode) throws Exception {
        String urlString = API_BASE_URL + barcode.trim() + ".json";
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        try {
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);
            conn.setRequestProperty("User-Agent", USER_AGENT);
            conn.setRequestProperty("Accept", "application/json");

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                return null;
            }

            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new Exception("Server returned HTTP " + responseCode);
            }

            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();

            return OpenFoodFactsParser.parseJson(sb.toString());
        } finally {
            conn.disconnect();
        }
    }
}
