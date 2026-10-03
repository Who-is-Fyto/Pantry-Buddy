package com.example.pantrybuddy.ui.pantry;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pantrybuddy.R;
import com.example.pantrybuddy.data.local.entity.PantryItem;
import com.example.pantrybuddy.databinding.ItemPantryIngredientBinding;
import com.example.pantrybuddy.domain.model.ExpiryStatus;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

// Adapter for displaying pantry ingredients with quantities and freshness badges
public class PantryAdapter extends ListAdapter<PantryItem, PantryAdapter.ViewHolder> {

    public interface OnPantryItemClickListener {
        void onPantryItemClick(PantryItem item);
    }

    private static final DiffUtil.ItemCallback<PantryItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<PantryItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull PantryItem oldItem, @NonNull PantryItem newItem) {
            return oldItem.getItemId() == newItem.getItemId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull PantryItem oldItem, @NonNull PantryItem newItem) {
            return Double.compare(oldItem.getQuantity(), newItem.getQuantity()) == 0
                    && Objects.equals(oldItem.getName(), newItem.getName())
                    && Objects.equals(oldItem.getUnit(), newItem.getUnit())
                    && Objects.equals(oldItem.getExpiryDate(), newItem.getExpiryDate())
                    && Objects.equals(oldItem.getCategory(), newItem.getCategory());
        }
    };

    private final OnPantryItemClickListener listener;
    private final SimpleDateFormat shortDateFormat = new SimpleDateFormat("d MMM", Locale.US);

    public PantryAdapter(OnPantryItemClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPantryIngredientBinding binding = ItemPantryIngredientBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemPantryIngredientBinding binding;

        ViewHolder(ItemPantryIngredientBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(PantryItem item) {
            Context context = binding.getRoot().getContext();

            // Name
            binding.tvIngredientName.setText(item.getName());

            // Quantity formatting: "4 pcs" or "0.5 kg"
            String qtyStr = (item.getQuantity() == (long) item.getQuantity())
                    ? String.valueOf((long) item.getQuantity())
                    : String.format(Locale.US, "%.1f", item.getQuantity());
            binding.tvIngredientQuantity.setText(String.format("%s %s", qtyStr, item.getUnit()));

            // Category
            binding.tvIngredientCategory.setText(
                    item.getCategory() != null ? item.getCategory() : "Other"
            );

            // Expiry pill status
            if (item.getExpiryDate() == null) {
                binding.tvExpiryPill.setText("Non-perishable");
                binding.tvExpiryPill.setBackgroundResource(R.drawable.bg_pill_neutral);
                binding.tvExpiryPill.setTextColor(ContextCompat.getColor(context, R.color.colorTextSecondary));
            } else {
                ExpiryStatus status = ExpiryStatus.fromTimestamp(item.getExpiryDate());
                String dateStr = shortDateFormat.format(new Date(item.getExpiryDate()));

                switch (status) {
                    case EXPIRED:
                        binding.tvExpiryPill.setText("Expired");
                        binding.tvExpiryPill.setBackgroundResource(R.drawable.bg_pill_danger);
                        binding.tvExpiryPill.setTextColor(ContextCompat.getColor(context, R.color.colorDangerRose));
                        break;
                    case TODAY:
                        binding.tvExpiryPill.setText(String.format("Use today · %s", dateStr));
                        binding.tvExpiryPill.setBackgroundResource(R.drawable.bg_pill_green);
                        binding.tvExpiryPill.setTextColor(ContextCompat.getColor(context, R.color.colorAccentGreen));
                        break;
                    case TOMORROW:
                        binding.tvExpiryPill.setText("Use tomorrow");
                        binding.tvExpiryPill.setBackgroundResource(R.drawable.bg_pill_amber);
                        binding.tvExpiryPill.setTextColor(ContextCompat.getColor(context, R.color.colorAccentAmber));
                        break;
                    case SOON:
                        binding.tvExpiryPill.setText(String.format("Use soon · %s", dateStr));
                        binding.tvExpiryPill.setBackgroundResource(R.drawable.bg_pill_amber);
                        binding.tvExpiryPill.setTextColor(ContextCompat.getColor(context, R.color.colorAccentAmber));
                        break;
                    case SAFE:
                    default:
                        binding.tvExpiryPill.setText(String.format("Best before %s", dateStr));
                        binding.tvExpiryPill.setBackgroundResource(R.drawable.bg_pill_neutral);
                        binding.tvExpiryPill.setTextColor(ContextCompat.getColor(context, R.color.colorTextSecondary));
                        break;
                }
            }

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPantryItemClick(item);
                }
            });
        }
    }
}
