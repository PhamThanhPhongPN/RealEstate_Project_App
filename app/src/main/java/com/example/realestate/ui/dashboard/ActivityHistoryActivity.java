package com.example.realestate.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.realestate.data.model.Property;
import com.example.realestate.data.remote.ApiService;
import com.example.realestate.data.remote.RetrofitClient;
import com.example.realestate.databinding.ActivityHistoryBinding;
import com.example.realestate.ui.adapters.PropertyListAdapter;
import com.example.realestate.ui.properties.PropertyDetailActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityHistoryActivity extends AppCompatActivity {
    private ActivityHistoryBinding binding;
    private ApiService apiService;
    private PropertyListAdapter adapter;
    private final List<Property> historyList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = RetrofitClient.getApiService(this);

        setupRecycler();
        setupListeners();
        loadHistory(true);
    }

    private void setupRecycler() {
        adapter = new PropertyListAdapter(historyList, new PropertyListAdapter.OnPropertyClickListener() {
            @Override
            public void onPropertyClick(Property property) {
                Intent intent = new Intent(ActivityHistoryActivity.this, PropertyDetailActivity.class);
                intent.putExtra("property_id", property.getPropertyId());
                startActivity(intent);
            }

            @Override
            public void onFavoriteClick(Property property, boolean isFavorite) {
                toggleFavorite(property);
            }
        });
        binding.rvHistoryProperties.setLayoutManager(new LinearLayoutManager(this));
        binding.rvHistoryProperties.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.btnHistoryBack.setOnClickListener(v -> finish());
        binding.swipeRefreshHistory.setOnRefreshListener(() -> loadHistory(false));
    }

    private void loadHistory(boolean showProgress) {
        if (showProgress) {
            binding.historyProgress.setVisibility(View.VISIBLE);
        }
        binding.tvHistoryEmpty.setVisibility(View.GONE);

        apiService.getRecentlyViewed().enqueue(new Callback<List<Property>>() {
            @Override
            public void onResponse(Call<List<Property>> call, Response<List<Property>> response) {
                binding.historyProgress.setVisibility(View.GONE);
                binding.swipeRefreshHistory.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    historyList.clear();
                    historyList.addAll(response.body());
                    adapter.notifyDataSetChanged();

                    if (historyList.isEmpty()) {
                        binding.tvHistoryEmpty.setVisibility(View.VISIBLE);
                    } else {
                        syncFavoriteState();
                    }
                } else {
                    binding.tvHistoryEmpty.setVisibility(View.VISIBLE);
                    Toast.makeText(ActivityHistoryActivity.this, "Không thể tải lịch sử", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Property>> call, Throwable t) {
                binding.historyProgress.setVisibility(View.GONE);
                binding.swipeRefreshHistory.setRefreshing(false);
                binding.tvHistoryEmpty.setVisibility(View.VISIBLE);
                Toast.makeText(ActivityHistoryActivity.this, "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void syncFavoriteState() {
        apiService.getFavoriteIds().enqueue(new Callback<List<Integer>>() {
            @Override
            public void onResponse(Call<List<Integer>> call, Response<List<Integer>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setFavoriteIds(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<Integer>> call, Throwable t) {}
        });
    }

    private void toggleFavorite(Property property) {
        apiService.toggleFavorite(property.getPropertyId()).enqueue(new Callback<Map<String, Boolean>>() {
            @Override
            public void onResponse(Call<Map<String, Boolean>> call, Response<Map<String, Boolean>> response) {
                if (response.isSuccessful()) {
                    syncFavoriteState();
                } else {
                    Toast.makeText(ActivityHistoryActivity.this, "Lỗi cập nhật tin yêu thích", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Boolean>> call, Throwable t) {
                Toast.makeText(ActivityHistoryActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHistory(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
