package com.example.realestate.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.realestate.R;
import com.example.realestate.data.model.Notification;
import com.example.realestate.databinding.ItemNotificationBinding;
import java.util.List;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder> {
    private final List<Notification> notifications;
    private final OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    public NotificationsAdapter(List<Notification> notifications, OnNotificationClickListener listener) {
        this.notifications = notifications;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNotificationBinding binding = ItemNotificationBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.binding.tvNotifTitle.setText(notification.getTitle());
        holder.binding.tvNotifBody.setText(notification.getBody());
        holder.binding.tvNotifDate.setText(notification.getCreatedAt());

        // Dynamic styles based on read status
        if (notification.isRead()) {
            holder.binding.viewUnreadDot.setVisibility(View.GONE);
            holder.binding.layoutNotificationItem.setBackgroundColor(0); // Transparent default
            holder.binding.tvNotifTitle.setAlpha(0.7f);
            holder.binding.tvNotifBody.setAlpha(0.7f);
        } else {
            holder.binding.viewUnreadDot.setVisibility(View.VISIBLE);
            holder.binding.layoutNotificationItem.setBackgroundColor(
                    holder.itemView.getContext().getResources().getColor(R.color.brand_blue_50));
            holder.binding.tvNotifTitle.setAlpha(1.0f);
            holder.binding.tvNotifBody.setAlpha(1.0f);
        }

        // Apply distinct icons based on notification type
        if ("listing_approved".equalsIgnoreCase(notification.getType())) {
            holder.binding.ivNotifIcon.setImageResource(android.R.drawable.stat_sys_upload_done);
            holder.binding.ivNotifIcon.setColorFilter(holder.itemView.getContext().getResources().getColor(R.color.accent_green));
        } else if ("listing_rejected".equalsIgnoreCase(notification.getType())) {
            holder.binding.ivNotifIcon.setImageResource(android.R.drawable.ic_dialog_alert);
            holder.binding.ivNotifIcon.setColorFilter(holder.itemView.getContext().getResources().getColor(R.color.secondary_pink));
        } else {
            holder.binding.ivNotifIcon.setImageResource(android.R.drawable.ic_dialog_info);
            holder.binding.ivNotifIcon.setColorFilter(holder.itemView.getContext().getResources().getColor(R.color.brand_blue_600));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificationClick(notification);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemNotificationBinding binding;
        ViewHolder(ItemNotificationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
