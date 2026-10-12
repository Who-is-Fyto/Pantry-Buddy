package com.example.pantrybuddy.data.remote.openfoodfacts;

import com.google.gson.annotations.SerializedName;
import java.util.List;

// Data transfer objects representing responses from the Open Food Facts API v2
public class OpenFoodFactsDto {

    public static class Response {
        @SerializedName("code")
        public String code;

        @SerializedName("status")
        public int status;

        @SerializedName("status_verbose")
        public String statusVerbose;

        @SerializedName("product")
        public Product product;
    }

    public static class Product {
        @SerializedName("product_name")
        public String productName;

        @SerializedName("product_name_en")
        public String productNameEn;

        @SerializedName("generic_name")
        public String genericName;

        @SerializedName("brands")
        public String brands;

        @SerializedName("quantity")
        public String quantity;

        @SerializedName("categories")
        public String categories;

        @SerializedName("categories_tags")
        public List<String> categoriesTags;

        @SerializedName("image_front_url")
        public String imageFrontUrl;

        @SerializedName("image_url")
        public String imageUrl;
    }
}
