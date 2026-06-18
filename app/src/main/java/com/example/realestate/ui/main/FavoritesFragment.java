package com.example.realestate.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.realestate.data.model.Property;
import com.example.realestate.data.remote.ApiService;
import com.example.realestate.data.remote.RetrofitClient;
import com.example.realestate.databinding.FragmentFavoritesBinding;
import com.example.realestate.ui.adapters.PropertyListAdapter;
import com.example.realestate.ui.properties.PropertyDetailActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoritesFragment extends Fragment {
    private FragmentFavoritesBinding binding;
    private ApiService apiService;
    private PropertyListAdapter adapter;
    private List<Property> favoritesList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFavoritesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        apiService = RetrofitClient.getApiService(requireContext());

        setupRecycler();
        loadFavorites();
    }

    private void setupRecycler() {
        adapter = new PropertyListAdapter(favoritesList, new PropertyListAdapter.OnPropertyClickListener() {
            @Override
            public void onPropertyClick(Property property) {
                Intent intent = new Intent(requireContext(), PropertyDetailActivity.class);
                intent.putExtra("property_id", property.getPropertyId());
                startActivity(intent);
            }

            @Override
            public void onFavoriteClick(Property property, boolean isFavorite) {
                apiService.toggleFavorite(property.getPropertyId()).enqueue(new Callback<Map<String, Boolean>>() {
                    @Override
                    public void onResponse(Call<Map<String, Boolean>> call, Response<Map<String, Boolean>> response) {
                        if (response.isSuccessful()) {
                            loadFavorites(); // Reload list to remove item
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Boolean>> call, Throwable t) {
                        Toast.makeText(requireContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        binding.rvFavorites.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvFavorites.setAdapter(adapter);
    }

    private void loadFavorites() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.layoutEmptyState.setVisibility(View.GONE);

        apiService.getFavorites().enqueue(new Callback<List<Property>>() {
            @Override
            public void onResponse(Call<List<Property>> call, Response<List<Property>> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    favoritesList.clear();
                    favoritesList.addAll(response.body());
                    adapter.notifyDataSetChanged();

                    if (favoritesList.isEmpty()) {
                        binding.layoutEmptyState.setVisibility(View.VISIBLE);
                    } else {
                        // Populate favorite IDs
                        List<Integer> ids = new ArrayList<>();
                        for (Property p : favoritesList) {
                            ids.add(p.getPropertyId());
                        }
                        adapter.setFavoriteIds(ids);
                    }
                } else {
                    binding.layoutEmptyState.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<List<Property>> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                binding.layoutEmptyState.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFavorites(); // Reload favorite items when returning to the tab
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
