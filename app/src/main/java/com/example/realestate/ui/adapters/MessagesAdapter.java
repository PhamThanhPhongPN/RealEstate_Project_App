package com.example.realestate.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.realestate.data.model.Message;
import com.example.realestate.databinding.ItemMessageBinding;
import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> {
    private final List<Message> messages;
    private final int currentUserId;

    public MessagesAdapter(List<Message> messages, int currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMessageBinding binding = ItemMessageBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message msg = messages.get(position);
        Context context = holder.itemView.getContext();

        final boolean isMine = currentUserId == msg.getSenderId();

        if (isMine) {
            holder.binding.layoutMessageLeft.setVisibility(View.GONE);
            holder.binding.tvMessageRight.setText(msg.getBody());
            holder.binding.tvMessageRight.setVisibility(View.VISIBLE);
        } else {
            holder.binding.layoutMessageLeft.setVisibility(View.VISIBLE);
            holder.binding.tvMessageLeft.setText(msg.getBody());
            holder.binding.tvMessageRight.setVisibility(View.GONE);

            // Load contact avatar
            String avatarUrl = msg.getSenderAvatar();
            if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
                avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=150&q=80";
            }
            Glide.with(context)
                    .load(avatarUrl)
                    .circleCrop()
                    .placeholder(android.R.drawable.sym_def_app_icon)
                    .into(holder.binding.ivMessageAvatar);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemMessageBinding binding;

        public ViewHolder(ItemMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
