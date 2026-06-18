package com.example.realestate.ui.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.realestate.databinding.ItemImagePreviewBinding;
import java.util.List;

public class ImagePreviewsAdapter extends RecyclerView.Adapter<ImagePreviewsAdapter.ViewHolder> {
    private final List<Uri> imageUris;
    private final OnImageDeleteListener listener;

    public interface OnImageDeleteListener {
        void onDeleteClick(int position);
    }

    public ImagePreviewsAdapter(List<Uri> imageUris, OnImageDeleteListener listener) {
        this.imageUris = imageUris;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemImagePreviewBinding binding = ItemImagePreviewBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Uri uri = imageUris.get(position);
        Context context = holder.itemView.getContext();

        Glide.with(context)
                .load(uri)
                .centerCrop()
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(holder.binding.ivPreviewImage);

        holder.binding.btnDeletePreview.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageUris.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemImagePreviewBinding binding;

        public ViewHolder(ItemImagePreviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
