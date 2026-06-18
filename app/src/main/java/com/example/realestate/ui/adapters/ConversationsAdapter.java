package com.example.realestate.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.realestate.data.model.Conversation;
import com.example.realestate.databinding.ItemConversationBinding;
import java.util.List;

public class ConversationsAdapter extends RecyclerView.Adapter<ConversationsAdapter.ViewHolder> {
    private final List<Conversation> conversations;
    private final int currentUserId;
    private final OnConversationClickListener listener;

    public interface OnConversationClickListener {
        void onConversationClick(Conversation conversation, int otherUserId, String otherUserName, String otherUserAvatar);
    }

    public ConversationsAdapter(List<Conversation> conversations, int currentUserId, OnConversationClickListener listener) {
        this.conversations = conversations;
        this.currentUserId = currentUserId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemConversationBinding binding = ItemConversationBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Conversation conv = conversations.get(position);
        Context context = holder.itemView.getContext();

        final boolean isBuyer = currentUserId == conv.getBuyerId();
        final String otherPartyName = isBuyer ? conv.getSellerName() : conv.getBuyerName();
        final String otherPartyAvatar = isBuyer ? conv.getSellerAvatar() : conv.getBuyerAvatar();
        final int otherPartyId = isBuyer ? conv.getSellerId() : conv.getBuyerId();

        holder.binding.tvConvName.setText(otherPartyName != null ? otherPartyName : "User #" + otherPartyId);

        // Unread Badge
        if (conv.getUnreadCount() > 0) {
            holder.binding.tvConvUnread.setText(String.valueOf(conv.getUnreadCount()));
            holder.binding.tvConvUnread.setVisibility(View.VISIBLE);
        } else {
            holder.binding.tvConvUnread.setVisibility(View.GONE);
        }

        // Property title tag
        if (conv.getPropertyTitle() != null && !conv.getPropertyTitle().trim().isEmpty()) {
            holder.binding.tvConvProperty.setText(conv.getPropertyTitle());
            holder.binding.tvConvProperty.setVisibility(View.VISIBLE);
        } else {
            holder.binding.tvConvProperty.setVisibility(View.GONE);
        }

        // Last Message
        holder.binding.tvConvLastMessage.setText(conv.getLastMessage() != null ? conv.getLastMessage() : "Chưa có tin nhắn nào.");

        // Avatar
        String avatarUrl = otherPartyAvatar;
        if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
            avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=150&q=80";
        }

        Glide.with(context)
                .load(avatarUrl)
                .circleCrop()
                .placeholder(android.R.drawable.sym_def_app_icon)
                .into(holder.binding.ivConvAvatar);

        final String finalAvatar = avatarUrl;
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onConversationClick(conv, otherPartyId, otherPartyName, finalAvatar);
            }
        });
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemConversationBinding binding;

        public ViewHolder(ItemConversationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
