package com.example.realestate.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.realestate.R;
import com.example.realestate.data.model.*;
import com.example.realestate.data.remote.ApiService;
import com.example.realestate.data.remote.RetrofitClient;
import com.example.realestate.databinding.FragmentSearchBinding;
import com.example.realestate.ui.adapters.PropertyListAdapter;
import com.example.realestate.ui.properties.PropertyDetailActivity;
import java.util.*;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment {
    private FragmentSearchBinding binding;
    private ApiService apiService;
    private PropertyListAdapter adapter;
    private List<Property> propertiesList = new ArrayList<>();
    
    // Metadata states
    private List<City> cities = new ArrayList<>();
    private List<District> allDistricts = new ArrayList<>();
    private List<District> filteredDistricts = new ArrayList<>();
    private List<Category> categories = new ArrayList<>();
    
    private String activeListingType = "sale"; // "sale" or "rent"
    private Integer initialTypeId = null;
    private String initialKeyword = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        apiService = RetrofitClient.getApiService(requireContext());

        setupRecycler();
        setupListeners();
        loadMetadata();
    }

    public void setInitialFilters(String keyword, Integer typeId, String listingType) {
        this.initialKeyword = keyword;
        this.initialTypeId = typeId;
        if (listingType != null) {
            this.activeListingType = listingType;
        }
    }

    private void setupRecycler() {
        adapter = new PropertyListAdapter(propertiesList, new PropertyListAdapter.OnPropertyClickListener() {
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
                            syncFavoriteIds();
                        } else {
                            Toast.makeText(requireContext(), "Yêu cầu đăng nhập để lưu", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Boolean>> call, Throwable t) {
                        Toast.makeText(requireContext(), "Lỗi mạng", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        binding.rvSearchResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvSearchResults.setAdapter(adapter);
    }

    private void setupListeners() {
        // Toggle advanced filters drawer
        binding.btnFilterToggle.setOnClickListener(v -> {
            int vis = binding.layoutAdvancedFilters.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE;
            binding.layoutAdvancedFilters.setVisibility(vis);
        });

        // Quick listing type toggles
        binding.btnSearchBuy.setOnClickListener(v -> {
            activeListingType = "sale";
            binding.btnSearchBuy.setBackgroundTintList(requireContext().getColorStateList(R.color.brand_blue_600));
            binding.btnSearchBuy.setTextColor(requireContext().getColor(R.color.white));
            binding.btnSearchRent.setBackgroundTintList(requireContext().getColorStateList(android.R.color.transparent));
            binding.btnSearchRent.setTextColor(requireContext().getColor(R.color.brand_blue_600));
            performSearch();
        });

        binding.btnSearchRent.setOnClickListener(v -> {
            activeListingType = "rent";
            binding.btnSearchRent.setBackgroundTintList(requireContext().getColorStateList(R.color.brand_blue_600));
            binding.btnSearchRent.setTextColor(requireContext().getColor(R.color.white));
            binding.btnSearchBuy.setBackgroundTintList(requireContext().getColorStateList(android.R.color.transparent));
            binding.btnSearchBuy.setTextColor(requireContext().getColor(R.color.brand_blue_600));
            performSearch();
        });

        binding.btnApplyFilters.setOnClickListener(v -> {
            binding.layoutAdvancedFilters.setVisibility(View.GONE);
            performSearch();
        });

        // Trigger search on keyboard Search action
        binding.etKeyword.setOnEditorActionListener((v, actionId, event) -> {
            performSearch();
            return true;
        });

        // Cascading spinner listener
        binding.spinnerCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    filteredDistricts.clear();
                    setupDistrictSpinner();
                } else {
                    City selectedCity = cities.get(position - 1);
                    filterDistrictsByCity(selectedCity.getCityId());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadMetadata() {
        apiService.getMetadata().enqueue(new Callback<SearchMetadata>() {
            @Override
            public void onResponse(Call<SearchMetadata> call, Response<SearchMetadata> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cities.clear();
                    cities.addAll(response.body().getCities());

                    allDistricts.clear();
                    allDistricts.addAll(response.body().getDistricts());

                    categories.clear();
                    categories.addAll(response.body().getCategories());

                    setupSpinners();
                    
                    // Set initial filters from Home tab if passed
                    if (initialKeyword != null) {
                        binding.etKeyword.setText(initialKeyword);
                        initialKeyword = null;
                    }
                    // Apply listingType segment highlight
                    if ("rent".equalsIgnoreCase(activeListingType)) {
                        binding.btnSearchRent.performClick();
                    } else {
                        binding.btnSearchBuy.performClick();
                    }
                }
            }

            @Override
            public void onFailure(Call<SearchMetadata> call, Throwable t) {
                Toast.makeText(requireContext(), "Lỗi tải danh mục từ server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSpinners() {
        // 1. City Spinner
        List<String> cityNames = new ArrayList<>();
        cityNames.add("Tất cả Thành phố");
        for (City c : cities) {
            cityNames.add(c.getName());
        }
        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, cityNames);
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCity.setAdapter(cityAdapter);

        // 2. Type Spinner
        List<String> typeNames = new ArrayList<>();
        typeNames.add("Tất cả Loại nhà");
        for (Category cat : categories) {
            typeNames.add(cat.getName());
        }
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, typeNames);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerType.setAdapter(typeAdapter);

        if (initialTypeId != null) {
            // Find and preselect type spinner
            for (int i = 0; i < categories.size(); i++) {
                if (categories.get(i).getTypeId() == initialTypeId) {
                    binding.spinnerType.setSelection(i + 1);
                    break;
                }
            }
            initialTypeId = null;
        }

        // 3. Sort Spinner
        List<String> sortNames = Arrays.asList("Mới nhất", "Cũ nhất", "Giá: Thấp đến Cao", "Giá: Cao đến Thấp", "Diện tích: Bé đến Lớn", "Diện tích: Lớn đến Bé");
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, sortNames);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerSort.setAdapter(sortAdapter);

        // 4. Trigger empty districts
        setupDistrictSpinner();
    }

    private void filterDistrictsByCity(int cityId) {
        filteredDistricts.clear();
        for (District d : allDistricts) {
            if (d.getCityId() == cityId) {
                filteredDistricts.add(d);
            }
        }
        setupDistrictSpinner();
    }

    private void setupDistrictSpinner() {
        List<String> districtNames = new ArrayList<>();
        districtNames.add("Tất cả Quận/Huyện");
        for (District d : filteredDistricts) {
            districtNames.add(d.getName());
        }
        ArrayAdapter<String> districtAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, districtNames);
        districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerDistrict.setAdapter(districtAdapter);
    }

    private void performSearch() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.tvEmptyState.setVisibility(View.GONE);

        String keyword = binding.etKeyword.getText().toString().trim();
        if (TextUtils.isEmpty(keyword)) keyword = null;

        Integer cityId = null;
        int citySelPos = binding.spinnerCity.getSelectedItemPosition();
        if (citySelPos > 0) {
            cityId = cities.get(citySelPos - 1).getCityId();
        }

        Integer districtId = null;
        int distSelPos = binding.spinnerDistrict.getSelectedItemPosition();
        if (distSelPos > 0 && !filteredDistricts.isEmpty()) {
            districtId = filteredDistricts.get(distSelPos - 1).getDistrictId();
        }

        Integer typeId = null;
        int typeSelPos = binding.spinnerType.getSelectedItemPosition();
        if (typeSelPos > 0) {
            typeId = categories.get(typeSelPos - 1).getTypeId();
        }

        String minPrice = binding.etMinPrice.getText().toString().trim();
        if (TextUtils.isEmpty(minPrice)) minPrice = null;

        String maxPrice = binding.etMaxPrice.getText().toString().trim();
        if (TextUtils.isEmpty(maxPrice)) maxPrice = null;

        Integer bedrooms = null;
        String bedsStr = binding.etBedrooms.getText().toString().trim();
        if (!TextUtils.isEmpty(bedsStr)) bedrooms = Integer.parseInt(bedsStr);

        Integer bathrooms = null;
        String bathsStr = binding.etBathrooms.getText().toString().trim();
        if (!TextUtils.isEmpty(bathsStr)) bathrooms = Integer.parseInt(bathsStr);

        // Sorting mapping
        String sortVal = "newest";
        int sortPos = binding.spinnerSort.getSelectedItemPosition();
        if (sortPos == 1) sortVal = "oldest";
        else if (sortPos == 2) sortVal = "price_asc";
        else if (sortPos == 3) sortVal = "price_desc";
        else if (sortPos == 4) sortVal = "area_asc";
        else if (sortPos == 5) sortVal = "area_desc";

        apiService.searchProperties(
                keyword,
                typeId,
                cityId,
                districtId,
                activeListingType,
                minPrice,
                maxPrice,
                bedrooms,
                bathrooms,
                null, // direction
                sortVal,
                20,
                1
        ).enqueue(new Callback<SearchResponse>() {
            @Override
            public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    propertiesList.clear();
                    propertiesList.addAll(response.body().getProperties());
                    adapter.notifyDataSetChanged();

                    binding.tvSearchResultsCount.setText(propertiesList.size() + " kết quả");
                    if (propertiesList.isEmpty()) {
                        binding.tvEmptyState.setVisibility(View.VISIBLE);
                    }
                    syncFavoriteIds();
                }
            }

            @Override
            public void onFailure(Call<SearchResponse> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Lỗi mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void syncFavoriteIds() {
        apiService.getFavoriteIds().enqueue(new Callback<List<Integer>>() {
            @Override
            public void onResponse(Call<List<Integer>> call, Response<List<Integer>> response) {
                if (response.isSuccessful()) {
                    adapter.setFavoriteIds(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<Integer>> call, Throwable t) {}
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
