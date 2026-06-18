package com.example.realestate.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.realestate.R;
import com.example.realestate.data.model.Property;
import com.example.realestate.databinding.ItemPropertyListBinding;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class PropertyListAdapter extends RecyclerView.Adapter<PropertyListAdapter.ViewHolder> {
    private final List<Property> properties;
    private final Set<Integer> favoriteIds = new HashSet<>();
    private final OnPropertyClickListener listener;

    public interface OnPropertyClickListener {
        void onPropertyClick(Property property);
        void onFavoriteClick(Property property, boolean isFavorite);
    }

    public PropertyListAdapter(List<Property> properties, OnPropertyClickListener listener) {
        this.properties = properties;
        this.listener = listener;
    }

    public void setFavoriteIds(List<Integer> ids) {
        this.favoriteIds.clear();
        if (ids != null) {
            this.favoriteIds.addAll(ids);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPropertyListBinding binding = ItemPropertyListBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Property property = properties.get(position);
        Context context = holder.itemView.getContext();

        holder.binding.tvListTitle.setText(property.getTitle());
        
        String priceText = String.format(Locale.US, "$%,.0f", property.getPriceUsd());
        if ("rent".equalsIgnoreCase(property.getListingType())) {
            priceText += "/th";
        }
        holder.binding.tvListPrice.setText(priceText);

        holder.binding.tvListType.setText(property.getTypeName() != null ? property.getTypeName() : "Bất động sản");
        
        String address = property.getAddress();
        if (address == null || address.trim().isEmpty()) {
            address = (property.getDistrictName() != null ? property.getDistrictName() : "District") + ", " +
                      (property.getCityName() != null ? property.getCityName() : "City");
        }
        holder.binding.tvListAddress.setText(address);
        holder.binding.tvListArea.setText("📐 " + (property.getAreaM2() != null ? property.getAreaM2() : 0.0) + " m²");

        // VIP Badge visibility
        if (property.getVipTier() != null && !"none".equalsIgnoreCase(property.getVipTier())) {
            holder.binding.tvListVipBadge.setText(property.getVipTier().toUpperCase());
            holder.binding.tvListVipBadge.setVisibility(View.VISIBLE);
            if ("gold".equalsIgnoreCase(property.getVipTier())) {
                holder.binding.tvListVipBadge.setBackgroundColor(context.getResources().getColor(R.color.gold));
            } else {
                holder.binding.tvListVipBadge.setBackgroundColor(context.getResources().getColor(R.color.silver));
            }
        } else {
            holder.binding.tvListVipBadge.setVisibility(View.GONE);
        }

        // Favorite Heart State
        final boolean isFavorited = favoriteIds.contains(property.getPropertyId());
        if (isFavorited) {
            holder.binding.ivListFavorite.setImageResource(R.drawable.ic_favorite);
            holder.binding.ivListFavorite.setColorFilter(context.getResources().getColor(R.color.secondary_pink));
        } else {
            holder.binding.ivListFavorite.setImageResource(R.drawable.ic_favorite);
            holder.binding.ivListFavorite.setColorFilter(context.getResources().getColor(R.color.gray_500));
        }

        holder.binding.ivListFavorite.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFavoriteClick(property, isFavorited);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPropertyClick(property);
            }
        });

        String imgUrl = property.getPrimaryImage();
        if (imgUrl == null && !property.getImages().isEmpty()) {
            imgUrl = property.getImages().get(0).getImageUrl();
        }
        if (imgUrl == null) {
            imgUrl = "https://images.unsplash.com/photo-1580587771525-78b9dba3b914?auto=format&fit=crop&w=600&q=80";
        }

        Glide.with(context)
                .load(imgUrl)
                .centerCrop()
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(holder.binding.ivListImg);
    }

    @Override
    public int getItemCount() {
        return properties.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemPropertyListBinding binding;

        public ViewHolder(ItemPropertyListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
