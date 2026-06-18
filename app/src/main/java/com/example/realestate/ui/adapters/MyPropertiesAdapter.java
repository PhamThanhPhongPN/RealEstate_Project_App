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
import com.example.realestate.databinding.ItemMyPropertyBinding;
import java.util.List;
import java.util.Locale;

public class MyPropertiesAdapter extends RecyclerView.Adapter<MyPropertiesAdapter.ViewHolder> {
    private final List<Property> properties;
    private final OnMyPropertyActionListener listener;

    public interface OnMyPropertyActionListener {
        void onEdit(Property property);
        void onDelete(Property property);
        void onBoost(Property property);
    }

    public MyPropertiesAdapter(List<Property> properties, OnMyPropertyActionListener listener) {
        this.properties = properties;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMyPropertyBinding binding = ItemMyPropertyBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Property property = properties.get(position);
        Context context = holder.itemView.getContext();

        holder.binding.tvMyTitle.setText(property.getTitle());
        holder.binding.tvMyPrice.setText(String.format(Locale.US, "$%,.0f", property.getPriceUsd()));

        // VIP Badge visibility
        if (property.getVipTier() != null && !"none".equalsIgnoreCase(property.getVipTier())) {
            holder.binding.tvMyVipBadge.setText("VIP: " + property.getVipTier().toUpperCase());
            holder.binding.tvMyVipBadge.setVisibility(View.VISIBLE);
            holder.binding.btnMyBoost.setVisibility(View.GONE); // Hide boost if already VIP
        } else {
            holder.binding.tvMyVipBadge.setVisibility(View.GONE);
            holder.binding.btnMyBoost.setVisibility(View.VISIBLE);
        }

        // Listing Status Badge
        if (property.getModStatus() != null) {
            holder.binding.tvMyStatusBadge.setText(property.getModStatus().toUpperCase());
            if ("approved".equalsIgnoreCase(property.getModStatus())) {
                holder.binding.tvMyStatusBadge.setBackgroundColor(context.getResources().getColor(R.color.accent_green));
                holder.binding.tvMyStatusBadge.setTextColor(context.getResources().getColor(R.color.white));
            } else if ("rejected".equalsIgnoreCase(property.getModStatus())) {
                holder.binding.tvMyStatusBadge.setBackgroundColor(context.getResources().getColor(R.color.secondary_pink));
                holder.binding.tvMyStatusBadge.setTextColor(context.getResources().getColor(R.color.white));
            } else {
                holder.binding.tvMyStatusBadge.setBackgroundColor(context.getResources().getColor(R.color.gray_300));
                holder.binding.tvMyStatusBadge.setTextColor(context.getResources().getColor(R.color.gray_700));
            }
        }

        holder.binding.btnMyEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(property);
        });

        holder.binding.btnMyDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(property);
        });

        holder.binding.btnMyBoost.setOnClickListener(v -> {
            if (listener != null) listener.onBoost(property);
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
                .into(holder.binding.ivMyImg);
    }

    @Override
    public int getItemCount() {
        return properties.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemMyPropertyBinding binding;

        public ViewHolder(ItemMyPropertyBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
