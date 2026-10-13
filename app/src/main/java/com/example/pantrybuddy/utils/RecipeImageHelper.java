package com.example.pantrybuddy.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
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

// Utility for loading recipe photography with Glide and managing fallback placeholders cleanly
public class RecipeImageHelper {

    public static final int DEFAULT_CARD_PADDING_DP = 16;
    public static final int HERO_BANNER_PADDING_DP = 32;

    public static void loadRecipeImage(@Nullable ImageView imageView, @Nullable String imageUrl) {
        loadRecipeImage(imageView, imageUrl, DEFAULT_CARD_PADDING_DP);
    }

    public static void loadRecipeImage(@Nullable ImageView imageView, @Nullable String imageUrl, int fallbackPaddingDp) {
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

        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            applyFallback(imageView, fallbackPaddingDp);
            return;
        }

        String targetUrl = imageUrl.trim();

        // Reset image properties so photography fills the view without color filter or padding
        imageView.clearColorFilter();
        imageView.setImageTintList(null);
        imageView.setPadding(0, 0, 0, 0);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        try {
            Glide.with(context)
                    .load(targetUrl)
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
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imageView.setColorFilter(
                ContextCompat.getColor(context, R.color.colorTextSecondary),
                PorterDuff.Mode.SRC_IN
        );
        imageView.setImageResource(R.drawable.ic_restaurant);
    }
}
