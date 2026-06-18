package com.example.realestate.ui.dashboard;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.realestate.R;
import com.example.realestate.data.model.*;
import com.example.realestate.data.remote.ApiService;
import com.example.realestate.data.remote.RetrofitClient;
import com.example.realestate.databinding.ActivityEditPropertyBinding;
import com.example.realestate.ui.adapters.FeatureCheckboxAdapter;
import com.example.realestate.ui.adapters.ImagePreviewsAdapter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.*;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditPropertyActivity extends AppCompatActivity {
    private ActivityEditPropertyBinding binding;
    private ApiService apiService;
    private int propertyId;

    private List<City> citiesList = new ArrayList<>();
    private List<District> districtsList = new ArrayList<>();
    private List<District> filteredDistricts = new ArrayList<>();
    private List<Category> categoriesList = new ArrayList<>();
    private List<Feature> featuresList = new ArrayList<>();

    private FeatureCheckboxAdapter featuresAdapter;
    private ImagePreviewsAdapter imagesAdapter;
    private final List<Uri> selectedImageUris = new ArrayList<>();

    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetMultipleContents(),
            uris -> {
                if (uris != null) {
                    selectedImageUris.addAll(uris);
                    imagesAdapter.notifyDataSetChanged();
                    binding.tvEditImagesCount.setText("Đã chọn: " + selectedImageUris.size() + " ảnh mới");
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditPropertyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = RetrofitClient.getApiService(this);
        propertyId = getIntent().getIntExtra("property_id", -1);

        if (propertyId == -1) {
            Toast.makeText(this, "Không tìm thấy tin đăng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupAdapters();
        setupListeners();
        loadMetadataAndDetails();
    }

    private void setupAdapters() {
        featuresAdapter = new FeatureCheckboxAdapter(featuresList);
        binding.rvEditFeatures.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvEditFeatures.setAdapter(featuresAdapter);

        imagesAdapter = new ImagePreviewsAdapter(selectedImageUris, position -> {
            selectedImageUris.remove(position);
            imagesAdapter.notifyDataSetChanged();
            binding.tvEditImagesCount.setText("Đã chọn: " + selectedImageUris.size() + " ảnh mới");
        });
        binding.rvEditImagePreviews.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvEditImagePreviews.setAdapter(imagesAdapter);
    }

    private void setupListeners() {
        binding.btnEditBack.setOnClickListener(v -> finish());
        binding.btnEditSelectImages.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        binding.btnEditSubmit.setOnClickListener(v -> submitEdits());

        binding.spinnerEditCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && !citiesList.isEmpty()) {
                    City city = citiesList.get(position);
                    filterDistricts(city.getCityId());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        binding.etEditPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePriceHint();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.spinnerEditCurrency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updatePriceHint();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void updatePriceHint() {
        String priceStr = binding.etEditPrice.getText().toString().trim();
        String currency = binding.spinnerEditCurrency.getSelectedItem() != null ?
                binding.spinnerEditCurrency.getSelectedItem().toString() : "USD";

        if (TextUtils.isEmpty(priceStr) || "USD".equalsIgnoreCase(currency)) {
            binding.tvEditPriceHint.setVisibility(View.GONE);
            return;
        }

        try {
            double amount = Double.parseDouble(priceStr);
            double usdAmount = amount / 25000.0;
            binding.tvEditPriceHint.setText(String.format(Locale.US, "≈ $%,.2f USD (Saved in USD backend)", usdAmount));
            binding.tvEditPriceHint.setVisibility(View.VISIBLE);
        } catch (NumberFormatException e) {
            binding.tvEditPriceHint.setVisibility(View.GONE);
        }
    }

    private void loadMetadataAndDetails() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.scrollView.setVisibility(View.GONE);

        apiService.getMetadata().enqueue(new Callback<SearchMetadata>() {
            @Override
            public void onResponse(Call<SearchMetadata> call, Response<SearchMetadata> response) {
                if (response.isSuccessful() && response.body() != null) {
                    citiesList.clear();
                    citiesList.addAll(response.body().getCities());

                    districtsList.clear();
                    districtsList.addAll(response.body().getDistricts());

                    categoriesList.clear();
                    categoriesList.addAll(response.body().getCategories());

                    featuresList.clear();
                    featuresList.addAll(response.body().getFeatures());
                    featuresAdapter.notifyDataSetChanged();

                    populateSpinners();
                    loadPropertyDetails();
                }
            }

            @Override
            public void onFailure(Call<SearchMetadata> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(EditPropertyActivity.this, "Không thể tải cấu hình", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateSpinners() {
        ArrayAdapter<City> cityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, citiesList);
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerEditCity.setAdapter(cityAdapter);

        List<String> typeNames = new ArrayList<>();
        for (Category cat : categoriesList) {
            typeNames.add(cat.getName());
        }
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, typeNames);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerEditType.setAdapter(typeAdapter);

        List<String> directions = Arrays.asList("Đông", "Tây", "Nam", "Bắc", "Đông Bắc", "Đông Nam", "Tây Bắc", "Tây Nam");
        ArrayAdapter<String> directionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, directions);
        directionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerEditDirection.setAdapter(directionAdapter);

        List<String> currencies = Arrays.asList("USD", "VND");
        ArrayAdapter<String> currencyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, currencies);
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerEditCurrency.setAdapter(currencyAdapter);
    }

    private void filterDistricts(int cityId) {
        filteredDistricts.clear();
        for (District d : districtsList) {
            if (d.getCityId() == cityId) {
                filteredDistricts.add(d);
            }
        }
        ArrayAdapter<District> districtAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, filteredDistricts);
        districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerEditDistrict.setAdapter(districtAdapter);
    }

    private void loadPropertyDetails() {
        apiService.getPropertyById(propertyId).enqueue(new Callback<Property>() {
            @Override
            public void onResponse(Call<Property> call, Response<Property> response) {
                binding.progressBar.setVisibility(View.GONE);
                binding.scrollView.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null) {
                    Property prop = response.body();
                    
                    binding.etEditTitle.setText(prop.getTitle());
                    binding.etEditDescription.setText(prop.getDescription() != null ? prop.getDescription() : "");
                    binding.etEditPrice.setText(String.valueOf((int) prop.getPriceUsd()));
                    binding.etEditArea.setText(prop.getAreaM2() != null ? String.valueOf(prop.getAreaM2()) : "");
                    binding.etEditBedrooms.setText(prop.getBedrooms() != null ? String.valueOf(prop.getBedrooms()) : "");
                    binding.etEditBathrooms.setText(prop.getBathrooms() != null ? String.valueOf(prop.getBathrooms()) : "");
                    
                    String fullAddress = prop.getAddress() != null ? prop.getAddress() : "";
                    String shortAddress = fullAddress.split(",")[0].trim();
                    binding.etEditAddress.setText(shortAddress);
                    
                    binding.etEditVideoUrl.setText(prop.getVideoUrl() != null ? prop.getVideoUrl() : "");

                    if ("rent".equalsIgnoreCase(prop.getListingType())) {
                        binding.rbEditRent.setChecked(true);
                    } else {
                        binding.rbEditSale.setChecked(true);
                    }

                    // Pre-select category type spinner
                    for (int i = 0; i < categoriesList.size(); i++) {
                        if (categoriesList.get(i).getTypeId() == prop.getTypeId()) {
                            binding.spinnerEditType.setSelection(i);
                            break;
                        }
                    }

                    // Pre-select direction spinner
                    if (prop.getDirection() != null) {
                        List<String> englishDirections = Arrays.asList("east", "west", "south", "north", "northeast", "southeast", "northwest", "southwest");
                        int idx = englishDirections.indexOf(prop.getDirection().toLowerCase());
                        if (idx >= 0) binding.spinnerEditDirection.setSelection(idx);
                    }

                    // Pre-select features checkbox
                    List<Integer> selectedFeatureIds = new ArrayList<>();
                    for (Feature f : prop.getFeatures()) {
                        selectedFeatureIds.add(f.getFeatureId());
                    }
                    featuresAdapter.setSelectedFeatureIds(selectedFeatureIds);
                }
            }

            @Override
            public void onFailure(Call<Property> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(EditPropertyActivity.this, "Không thể tải chi tiết tin đăng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitEdits() {
        String title = binding.etEditTitle.getText().toString().trim();
        String description = binding.etEditDescription.getText().toString().trim();
        String priceStr = binding.etEditPrice.getText().toString().trim();
        String areaStr = binding.etEditArea.getText().toString().trim();
        String address = binding.etEditAddress.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            binding.etEditTitle.setError("Không được bỏ trống tiêu đề");
            return;
        }
        if (TextUtils.isEmpty(priceStr)) {
            binding.etEditPrice.setError("Không được bỏ trống giá");
            return;
        }
        if (TextUtils.isEmpty(areaStr)) {
            binding.etEditArea.setError("Không được bỏ trống diện tích");
            return;
        }
        if (TextUtils.isEmpty(address)) {
            binding.etEditAddress.setError("Không được bỏ trống địa chỉ");
            return;
        }

        binding.btnEditSubmit.setEnabled(false);
        binding.tvEditError.setVisibility(View.GONE);

        double enteredPrice = Double.parseDouble(priceStr);
        String currency = binding.spinnerEditCurrency.getSelectedItem().toString();
        double finalPriceUsd = enteredPrice;
        if ("VND".equalsIgnoreCase(currency)) {
            finalPriceUsd = enteredPrice / 25000.0;
        }

        String listingType = binding.rbEditSale.isChecked() ? "sale" : "rent";
        
        String directionSelected = binding.spinnerEditDirection.getSelectedItem().toString();
        String direction = getDirectionValue(directionSelected);

        Integer districtId = null;
        if (binding.spinnerEditDistrict.getSelectedItem() != null) {
            districtId = ((District) binding.spinnerEditDistrict.getSelectedItem()).getDistrictId();
        }

        Map<String, Object> body = new HashMap<>();
        body.put("title", title);
        body.put("description", description);
        body.put("listing_type", listingType);
        body.put("price_usd", finalPriceUsd);
        body.put("area_sqm", Double.parseDouble(areaStr));
        
        String beds = binding.etEditBedrooms.getText().toString().trim();
        body.put("bedrooms", TextUtils.isEmpty(beds) ? 0 : Integer.parseInt(beds));
        
        String baths = binding.etEditBathrooms.getText().toString().trim();
        body.put("bathrooms", TextUtils.isEmpty(baths) ? 0 : Integer.parseInt(baths));
        
        body.put("direction", direction);
        body.put("address", address);
        
        if (districtId != null) {
            body.put("district_id", districtId);
        }

        String video = binding.etEditVideoUrl.getText().toString().trim();
        body.put("video_url", TextUtils.isEmpty(video) ? null : video);

        apiService.updateProperty(propertyId, body).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                if (response.isSuccessful()) {
                    if (!selectedImageUris.isEmpty()) {
                        uploadListingImages(propertyId);
                    } else {
                        Toast.makeText(EditPropertyActivity.this, "Cập nhật tin đăng thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    binding.btnEditSubmit.setEnabled(true);
                    showError("Cập nhật tin đăng lỗi: máy chủ");
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                binding.btnEditSubmit.setEnabled(true);
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void uploadListingImages(int propertyId) {
        binding.tvEditError.setText("Đang tải ảnh bổ sung...");
        binding.tvEditError.setVisibility(View.VISIBLE);

        List<MultipartBody.Part> imageParts = new ArrayList<>();
        for (int i = 0; i < selectedImageUris.size(); i++) {
            File file = getFileFromUri(selectedImageUris.get(i), "edit_img_" + i);
            if (file != null) {
                RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
                MultipartBody.Part body = MultipartBody.Part.createFormData("images", file.getName(), requestFile);
                imageParts.add(body);
            }
        }

        RequestBody propertyIdPart = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(propertyId));

        apiService.uploadImages(propertyIdPart, imageParts).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                Toast.makeText(EditPropertyActivity.this, "Cập nhật tin đăng và tải ảnh mới thành công!", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                Toast.makeText(EditPropertyActivity.this, "Đã cập nhật tin đăng nhưng lỗi tải ảnh mới.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private File getFileFromUri(Uri uri, String name) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;
            File file = new File(getCacheDir(), name + ".jpg");
            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[4 * 1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
            return file;
        } catch (Exception e) {
            return null;
        }
    }

    private void showError(String msg) {
        binding.tvEditError.setText(msg);
        binding.tvEditError.setVisibility(View.VISIBLE);
    }

    private String getDirectionValue(String dir) {
        if (dir == null) return null;
        switch (dir.trim().toLowerCase()) {
            case "đông":
            case "dông":
                return "east";
            case "tây":
            case "tay":
                return "west";
            case "nam":
                return "south";
            case "bắc":
            case "bac":
                return "north";
            case "đông bắc":
            case "dông bắc":
                return "northeast";
            case "đông nam":
            case "dông nam":
                return "southeast";
            case "tây bắc":
            case "tay bac":
                return "northwest";
            case "tây nam":
            case "tay nam":
                return "southwest";
            default:
                return dir;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
