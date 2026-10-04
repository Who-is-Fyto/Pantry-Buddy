package com.example.pantrybuddy.ui.recipes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pantrybuddy.R;
import com.example.pantrybuddy.data.local.entity.Recipe;
import com.example.pantrybuddy.data.local.entity.RecipeIngredient;
import com.example.pantrybuddy.databinding.ItemAlmostThereCardBinding;
import com.example.pantrybuddy.databinding.ItemRecipeCardBinding;
import com.example.pantrybuddy.databinding.ItemRecipesNoticeBinding;
import com.example.pantrybuddy.databinding.ItemRecipesSectionHeaderBinding;
import com.example.pantrybuddy.domain.model.MatchResult;
import com.example.pantrybuddy.domain.model.RecipeWithIngredients;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Multi-view type adapter rendering strictly cookable and quarantined almost there cards
public class RecipesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int VIEW_TYPE_HEADER = 0;
    public static final int VIEW_TYPE_COOKABLE = 1;
    public static final int VIEW_TYPE_ALMOST_THERE = 2;
    public static final int VIEW_TYPE_NOTICE = 3;

    public interface OnRecipeClickListener {
        void onRecipeClick(MatchResult matchResult);
    }

    private final List<RecipeListItem> items = new ArrayList<>();
    private OnRecipeClickListener listener;

    public void setOnRecipeClickListener(OnRecipeClickListener listener) {
        this.listener = listener;
    }

    public void setAlmostThereOnly(List<MatchResult> almostThereList) {
        items.clear();
        if (almostThereList != null) {
            for (MatchResult mr : almostThereList) {
                items.add(new AlmostThereItem(mr));
            }
        }
        notifyDataSetChanged();
    }

    public void setData(List<MatchResult> cookableList, List<MatchResult> almostThereList) {
        items.clear();

        boolean hasCookable = (cookableList != null && !cookableList.isEmpty());
        boolean hasAlmost = (almostThereList != null && !almostThereList.isEmpty());

        if (hasCookable) {
            items.add(new HeaderItem(
                    "Ready to cook right now",
                    "100% of required ingredients are on hand",
                    cookableList.size(),
                    null,
                    false
            ));
            for (MatchResult mr : cookableList) {
                items.add(new CookableItem(mr));
            }
        } else if (hasAlmost) {
            items.add(new HeaderItem(
                    "Ready to cook right now",
                    "100% of required ingredients are on hand",
                    0,
                    null,
                    false
            ));
            items.add(new NoticeItem(
                    "No 100% cookable recipes match your current filters. See \"Almost There\" below or check your pantry stock."
            ));
        }

        if (hasAlmost) {
            items.add(new HeaderItem(
                    "Almost There",
                    "You have almost everything. Only 1 ingredient is missing or short.",
                    almostThereList.size(),
                    "MISSING 1 INGREDIENT — SHOPPING REQUIRED",
                    hasCookable || !items.isEmpty()
            ));
            for (MatchResult mr : almostThereList) {
                items.add(new AlmostThereItem(mr));
            }
        }

        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getItemType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case VIEW_TYPE_HEADER:
                return new HeaderViewHolder(ItemRecipesSectionHeaderBinding.inflate(inflater, parent, false));
            case VIEW_TYPE_ALMOST_THERE:
                return new AlmostThereViewHolder(ItemAlmostThereCardBinding.inflate(inflater, parent, false));
            case VIEW_TYPE_NOTICE:
                return new NoticeViewHolder(ItemRecipesNoticeBinding.inflate(inflater, parent, false));
            case VIEW_TYPE_COOKABLE:
            default:
                return new CookableViewHolder(ItemRecipeCardBinding.inflate(inflater, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        RecipeListItem item = items.get(position);
        if (holder instanceof CookableViewHolder) {
            ((CookableViewHolder) holder).bind(((CookableItem) item).matchResult, listener);
        } else if (holder instanceof AlmostThereViewHolder) {
            ((AlmostThereViewHolder) holder).bind(((AlmostThereItem) item).matchResult, listener);
        } else if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind((HeaderItem) item);
        } else if (holder instanceof NoticeViewHolder) {
            ((NoticeViewHolder) holder).bind((NoticeItem) item);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ViewHolders
    static class CookableViewHolder extends RecyclerView.ViewHolder {
        private final ItemRecipeCardBinding binding;

        CookableViewHolder(ItemRecipeCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(MatchResult result, OnRecipeClickListener listener) {
            RecipeWithIngredients rwi = result.getRecipeWithIngredients();
            Recipe recipe = result.getRecipe();
            if (recipe == null) return;

            binding.tvRecipeTitle.setText(recipe.getTitle());
            binding.tvRecipeDescription.setText(recipe.getDescription());
            binding.tvRecipeCookTime.setText(String.format(Locale.US, "%d min", recipe.getCookTimeMinutes()));
            binding.tvRecipeServings.setText(String.format(Locale.US, "%d servings", result.getMaxServings()));
            binding.tvRecipeDifficulty.setText(recipe.getDifficulty() != null ? recipe.getDifficulty() : "Easy");

            int totalIngredients = (rwi != null && rwi.getIngredients() != null) ? rwi.getIngredients().size() : 0;
            binding.tvRecipeMatchBadge.setText(String.format(Locale.US, "%d of %d ingredients at home", totalIngredients, totalIngredients));

            if (result.isUsesExpiringSoonItem()) {
                binding.tvRecipeExpiringTag.setVisibility(View.VISIBLE);
                binding.tvRecipeExpiringTag.setText("Uses expiring item");
            } else {
                binding.tvRecipeExpiringTag.setVisibility(View.GONE);
            }

            if (rwi != null && rwi.getIngredients() != null && !rwi.getIngredients().isEmpty()) {
                StringBuilder preview = new StringBuilder("Includes: ");
                for (int i = 0; i < rwi.getIngredients().size(); i++) {
                    preview.append(rwi.getIngredients().get(i).getIngredientName());
                    if (i < rwi.getIngredients().size() - 1) {
                        preview.append(", ");
                    }
                }
                binding.tvRecipeIngredientsPreview.setText(preview.toString());
                binding.tvRecipeIngredientsPreview.setVisibility(View.VISIBLE);
            } else {
                binding.tvRecipeIngredientsPreview.setVisibility(View.GONE);
            }

            if (recipe.getImageUrl() != null && !recipe.getImageUrl().trim().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(recipe.getImageUrl())
                        .placeholder(R.drawable.ic_restaurant)
                        .error(R.drawable.ic_restaurant)
                        .into(binding.ivRecipeThumbnail);
            } else {
                binding.ivRecipeThumbnail.setImageResource(R.drawable.ic_restaurant);
            }

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRecipeClick(result);
                }
            });
        }
    }

    static class AlmostThereViewHolder extends RecyclerView.ViewHolder {
        private final ItemAlmostThereCardBinding binding;

        AlmostThereViewHolder(ItemAlmostThereCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(MatchResult result, OnRecipeClickListener listener) {
            RecipeWithIngredients rwi = result.getRecipeWithIngredients();
            Recipe recipe = result.getRecipe();
            if (recipe == null) return;

            binding.tvAlmostThereTitle.setText(recipe.getTitle());
            binding.tvAlmostThereCookTime.setText(String.format(Locale.US, "%d min", recipe.getCookTimeMinutes()));
            binding.tvAlmostThereServings.setText(String.format(Locale.US, "%d servings", recipe.getDefaultServings()));

            int total = (rwi != null && rwi.getIngredients() != null) ? rwi.getIngredients().size() : 0;
            int onHand = Math.max(0, total - result.getMissingCount());
            binding.tvAlmostThereStockBadge.setText(String.format(Locale.US, "%d of %d ingredients at home", onHand, total));

            if (result.getMissingIngredients() != null && !result.getMissingIngredients().isEmpty()) {
                MatchResult.MissingIngredientInfo missing = result.getMissingIngredients().get(0);
                String missingText = missing.getDisplayText();
                if (missingText.startsWith("Missing: ")) {
                    missingText = missingText.substring("Missing: ".length());
                }
                binding.tvAlmostThereMissingBanner.setText(String.format("Missing 1 Ingredient: %s", missingText));
            } else {
                binding.tvAlmostThereMissingBanner.setText("Missing 1 Ingredient");
            }

            if (rwi != null && rwi.getIngredients() != null) {
                StringBuilder have = new StringBuilder("Have: ");
                List<String> missingNames = new ArrayList<>();
                if (result.getMissingIngredients() != null) {
                    for (MatchResult.MissingIngredientInfo m : result.getMissingIngredients()) {
                        missingNames.add(m.getIngredientName().toLowerCase(Locale.US));
                    }
                }

                boolean first = true;
                for (RecipeIngredient ing : rwi.getIngredients()) {
                    if (!missingNames.contains(ing.getIngredientName().toLowerCase(Locale.US))) {
                        if (!first) have.append(", ");
                        have.append(ing.getIngredientName());
                        first = false;
                    }
                }
                binding.tvAlmostThereAtHomePreview.setText(have.toString());
            }

            if (recipe.getImageUrl() != null && !recipe.getImageUrl().trim().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(recipe.getImageUrl())
                        .placeholder(R.drawable.ic_restaurant)
                        .error(R.drawable.ic_restaurant)
                        .into(binding.ivAlmostThereThumbnail);
            } else {
                binding.ivAlmostThereThumbnail.setImageResource(R.drawable.ic_restaurant);
            }

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRecipeClick(result);
                }
            });
        }
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final ItemRecipesSectionHeaderBinding binding;

        HeaderViewHolder(ItemRecipesSectionHeaderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(HeaderItem item) {
            binding.viewSectionDivider.setVisibility(item.showDivider ? View.VISIBLE : View.GONE);

            if (item.badgeText != null) {
                binding.layoutAmberSectionBadge.setVisibility(View.VISIBLE);
                binding.tvAmberSectionBadgeText.setText(item.badgeText);
            } else {
                binding.layoutAmberSectionBadge.setVisibility(View.GONE);
            }

            binding.tvSectionHeaderTitle.setText(item.title);
            binding.tvSectionHeaderSubtitle.setText(item.subtitle);

            String countStr = (item.count == 1) ? "1 recipe" : item.count + " recipes";
            binding.tvSectionHeaderCount.setText(countStr);
        }
    }

    static class NoticeViewHolder extends RecyclerView.ViewHolder {
        private final ItemRecipesNoticeBinding binding;

        NoticeViewHolder(ItemRecipesNoticeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(NoticeItem item) {
            binding.tvSectionNoticeText.setText(item.message);
        }
    }

    // Data wrappers
    public abstract static class RecipeListItem {
        public abstract int getItemType();
    }

    public static class HeaderItem extends RecipeListItem {
        final String title;
        final String subtitle;
        final int count;
        final String badgeText;
        final boolean showDivider;

        public HeaderItem(String title, String subtitle, int count, String badgeText, boolean showDivider) {
            this.title = title;
            this.subtitle = subtitle;
            this.count = count;
            this.badgeText = badgeText;
            this.showDivider = showDivider;
        }

        @Override
        public int getItemType() {
            return VIEW_TYPE_HEADER;
        }
    }

    public static class CookableItem extends RecipeListItem {
        final MatchResult matchResult;

        public CookableItem(MatchResult matchResult) {
            this.matchResult = matchResult;
        }

        @Override
        public int getItemType() {
            return VIEW_TYPE_COOKABLE;
        }
    }

    public static class AlmostThereItem extends RecipeListItem {
        final MatchResult matchResult;

        public AlmostThereItem(MatchResult matchResult) {
            this.matchResult = matchResult;
        }

        @Override
        public int getItemType() {
            return VIEW_TYPE_ALMOST_THERE;
        }
    }

    public static class NoticeItem extends RecipeListItem {
        final String message;

        public NoticeItem(String message) {
            this.message = message;
        }

        @Override
        public int getItemType() {
            return VIEW_TYPE_NOTICE;
        }
    }
}
