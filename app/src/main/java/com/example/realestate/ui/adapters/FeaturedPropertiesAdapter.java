package com.example.realestate.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.realestate.data.model.Property;
import com.example.realestate.databinding.ItemPropertyFeaturedBinding;
import java.util.List;
import java.util.Locale;

public class FeaturedPropertiesAdapter extends RecyclerView.Adapter<FeaturedPropertiesAdapter.ViewHolder> {
    private final List<Property> properties;
    private final OnPropertyClickListener listener;

    public interface OnPropertyClickListener {
        void onPropertyClick(Property property);
    }

    public FeaturedPropertiesAdapter(List<Property> properties, OnPropertyClickListener listener) {
        this.properties = properties;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPropertyFeaturedBinding binding = ItemPropertyFeaturedBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Property property = properties.get(position);
        Context context = holder.itemView.getContext();

        holder.binding.tvFeaturedTitle.setText(property.getTitle());
        holder.binding.tvFeaturedPrice.setText(String.format(Locale.US, "$%,.0f", property.getPriceUsd()));
        
        String address = property.getAddress();
        if (address == null || address.trim().isEmpty()) {
            address = (property.getDistrictName() != null ? property.getDistrictName() : "District") + ", " +
                      (property.getCityName() != null ? property.getCityName() : "City");
        }
        holder.binding.tvFeaturedAddress.setText(address);

        holder.binding.tvFeaturedBeds.setText("🛏️ " + (property.getBedrooms() != null ? property.getBedrooms() : 0) + " PN");
        holder.binding.tvFeaturedBaths.setText("🚿 " + (property.getBathrooms() != null ? property.getBathrooms() : 0) + " PT");
        holder.binding.tvFeaturedArea.setText("📐 " + (property.getAreaM2() != null ? property.getAreaM2() : 0.0) + " m²");

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
                .into(holder.binding.ivFeaturedImg);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPropertyClick(property);
            }
        });
    }

    @Override
    public int getItemCount() {
        return properties.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemPropertyFeaturedBinding binding;

        public ViewHolder(ItemPropertyFeaturedBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
