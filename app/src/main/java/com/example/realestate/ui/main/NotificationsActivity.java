package com.example.realestate.ui.main;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.realestate.data.model.CommonResponse;
import com.example.realestate.data.model.Notification;
import com.example.realestate.data.remote.ApiService;
import com.example.realestate.data.remote.RetrofitClient;
import com.example.realestate.databinding.ActivityNotificationsBinding;
import com.example.realestate.ui.adapters.NotificationsAdapter;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsActivity extends AppCompatActivity {
    private ActivityNotificationsBinding binding;
    private ApiService apiService;
    private NotificationsAdapter adapter;
    private final List<Notification> notificationsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = RetrofitClient.getApiService(this);

        setupRecycler();
        setupListeners();
        loadNotifications(true);
    }

    private void setupRecycler() {
        adapter = new NotificationsAdapter(notificationsList, notification -> {
            if (!notification.isRead()) {
                markAsRead(notification);
            }
        });
        binding.rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        binding.rvNotifications.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.btnNotifBack.setOnClickListener(v -> finish());
        binding.swipeRefreshNotifs.setOnRefreshListener(() -> loadNotifications(false));
        binding.btnMarkAllRead.setOnClickListener(v -> markAllAsRead());
    }

    private void loadNotifications(boolean showProgress) {
        if (showProgress) {
            binding.notifProgress.setVisibility(View.VISIBLE);
        }
        binding.tvNotifEmpty.setVisibility(View.GONE);

        apiService.getNotifications().enqueue(new Callback<List<Notification>>() {
            @Override
            public void onResponse(Call<List<Notification>> call, Response<List<Notification>> response) {
                binding.notifProgress.setVisibility(View.GONE);
                binding.swipeRefreshNotifs.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    notificationsList.clear();
                    notificationsList.addAll(response.body());
                    adapter.notifyDataSetChanged();

                    if (notificationsList.isEmpty()) {
                        binding.tvNotifEmpty.setVisibility(View.VISIBLE);
                    }
                } else {
                    binding.tvNotifEmpty.setVisibility(View.VISIBLE);
                    Toast.makeText(NotificationsActivity.this, "Không thể tải danh sách thông báo", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Notification>> call, Throwable t) {
                binding.notifProgress.setVisibility(View.GONE);
                binding.swipeRefreshNotifs.setRefreshing(false);
                binding.tvNotifEmpty.setVisibility(View.VISIBLE);
                Toast.makeText(NotificationsActivity.this, "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void markAsRead(Notification notification) {
        apiService.markNotificationRead(notification.getNotificationId()).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                if (response.isSuccessful()) {
                    notification.setRead(true);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {}
        });
    }

    private void markAllAsRead() {
        if (notificationsList.isEmpty()) return;

        apiService.markAllNotificationsRead().enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(NotificationsActivity.this, "Đã đánh dấu đọc tất cả", Toast.LENGTH_SHORT).show();
                    for (Notification n : notificationsList) {
                        n.setRead(true);
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(NotificationsActivity.this, "Thao tác thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                Toast.makeText(NotificationsActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
