package com.example.realestate.ui.main;

import android.content.Context;
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
import com.example.realestate.MainActivity;
import com.example.realestate.R;
import com.example.realestate.data.model.Property;
import com.example.realestate.data.model.SearchResponse;
import com.example.realestate.data.remote.ApiService;
import com.example.realestate.data.remote.RetrofitClient;
import com.example.realestate.databinding.FragmentHomeBinding;
import com.example.realestate.ui.adapters.FeaturedPropertiesAdapter;
import com.example.realestate.ui.properties.PropertyDetailActivity;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private ApiService apiService;
    private List<Property> featuredList = new ArrayList<>();
    private FeaturedPropertiesAdapter adapter;
    private String activeListingType = "sale"; // "sale" or "rent"
    private Integer selectedTypeId = null; // Filter type: Apartment(1), Villa(4)...

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        apiService = RetrofitClient.getApiService(requireContext());

        setupRecycler();
        setupListeners();
        
        loadFeaturedProperties();
    }

    private void setupRecycler() {
        adapter = new FeaturedPropertiesAdapter(featuredList, property -> {
            Intent intent = new Intent(requireContext(), PropertyDetailActivity.class);
            intent.putExtra("property_id", property.getPropertyId());
            startActivity(intent);
        });
        binding.rvFeatured.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvFeatured.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.swipeRefresh.setOnRefreshListener(this::loadFeaturedProperties);

        // Buy / Rent toggles
        binding.btnBuyToggle.setOnClickListener(v -> {
            activeListingType = "sale";
            binding.btnBuyToggle.setBackgroundTintList(requireContext().getColorStateList(R.color.white));
            binding.btnBuyToggle.setTextColor(requireContext().getColor(R.color.brand_blue_600));
            binding.btnRentToggle.setBackgroundTintList(requireContext().getColorStateList(android.R.color.transparent));
            binding.btnRentToggle.setTextColor(requireContext().getColor(R.color.white));
            loadFeaturedProperties();
        });

        binding.btnRentToggle.setOnClickListener(v -> {
            activeListingType = "rent";
            binding.btnRentToggle.setBackgroundTintList(requireContext().getColorStateList(R.color.white));
            binding.btnRentToggle.setTextColor(requireContext().getColor(R.color.brand_blue_600));
            binding.btnBuyToggle.setBackgroundTintList(requireContext().getColorStateList(android.R.color.transparent));
            binding.btnBuyToggle.setTextColor(requireContext().getColor(R.color.white));
            loadFeaturedProperties();
        });

        // Search action
        binding.btnQuickSearch.setOnClickListener(v -> triggerSearchTab());
        binding.etSearchQuery.setOnEditorActionListener((v, actionId, event) -> {
            triggerSearchTab();
            return true;
        });

        binding.tvViewAllFeatured.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToSearchTab(null, null, activeListingType);
            }
        });

        // Categories quick tags click listeners
        setupCategoryClicks();
    }

    private void setupCategoryClicks() {
        binding.catApartment.setOnClickListener(v -> toggleCategory(1, binding.catApartment));
        binding.catVilla.setOnClickListener(v -> toggleCategory(4, binding.catVilla));
        binding.catPenthouse.setOnClickListener(v -> toggleCategory(2, binding.catPenthouse));
        binding.catHouse.setOnClickListener(v -> toggleCategory(5, binding.catHouse));
        binding.catLand.setOnClickListener(v -> toggleCategory(3, binding.catLand)); // Map Land to ID 3
    }

    private void toggleCategory(int typeId, View view) {
        // Reset backgrounds of all category views
        binding.catApartment.setBackgroundResource(R.drawable.bg_category_inactive);
        binding.catApartment.setTextColor(requireContext().getColor(R.color.brand_blue_600));
        binding.catVilla.setBackgroundResource(R.drawable.bg_category_inactive);
        binding.catVilla.setTextColor(requireContext().getColor(R.color.brand_blue_600));
        binding.catPenthouse.setBackgroundResource(R.drawable.bg_category_inactive);
        binding.catPenthouse.setTextColor(requireContext().getColor(R.color.brand_blue_600));
        binding.catHouse.setBackgroundResource(R.drawable.bg_category_inactive);
        binding.catHouse.setTextColor(requireContext().getColor(R.color.brand_blue_600));
        binding.catLand.setBackgroundResource(R.drawable.bg_category_inactive);
        binding.catLand.setTextColor(requireContext().getColor(R.color.brand_blue_600));

        if (selectedTypeId != null && selectedTypeId == typeId) {
            selectedTypeId = null; // Unselect
        } else {
            selectedTypeId = typeId; // Select
            view.setBackgroundResource(R.drawable.bg_category_active);
            ((android.widget.TextView) view).setTextColor(requireContext().getColor(R.color.white));
        }
        loadFeaturedProperties();
    }

    private void triggerSearchTab() {
        String query = binding.etSearchQuery.getText().toString().trim();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateToSearchTab(query, selectedTypeId, activeListingType);
        }
    }

    private void loadFeaturedProperties() {
        binding.swipeRefresh.setRefreshing(true);
        // We call search API specifically with approved active listings, sorting by newest
        apiService.searchProperties(
                null,
                selectedTypeId,
                null,
                null,
                activeListingType,
                null,
                null,
                null,
                null,
                null,
                "newest",
                6,
                1
        ).enqueue(new Callback<SearchResponse>() {
            @Override
            public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {
                binding.swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    featuredList.clear();
                    featuredList.addAll(response.body().getProperties());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(requireContext(), "Không thể tải tin nổi bật", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SearchResponse> call, Throwable t) {
                binding.swipeRefresh.setRefreshing(false);
                Toast.makeText(requireContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
