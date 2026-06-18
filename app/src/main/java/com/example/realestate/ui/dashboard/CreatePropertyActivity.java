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
import com.example.realestate.databinding.ActivityCreatePropertyBinding;
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

public class CreatePropertyActivity extends AppCompatActivity {
    private ActivityCreatePropertyBinding binding;
    private ApiService apiService;

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
                    if (selectedImageUris.size() + uris.size() > 10) {
                        Toast.makeText(this, "Chỉ được chọn tối đa 10 ảnh", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    selectedImageUris.addAll(uris);
                    imagesAdapter.notifyDataSetChanged();
                    binding.tvImagesSelectedCount.setText("Đã chọn: " + selectedImageUris.size() + " ảnh");
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreatePropertyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = RetrofitClient.getApiService(this);

        setupAdapters();
        setupListeners();
        loadMetadata();
    }

    private void setupAdapters() {
        // Features list checkboxes
        featuresAdapter = new FeatureCheckboxAdapter(featuresList);
        binding.rvCreateFeatures.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvCreateFeatures.setAdapter(featuresAdapter);

        // Images preview list
        imagesAdapter = new ImagePreviewsAdapter(selectedImageUris, position -> {
            selectedImageUris.remove(position);
            imagesAdapter.notifyDataSetChanged();
            binding.tvImagesSelectedCount.setText("Đã chọn: " + selectedImageUris.size() + " ảnh");
        });
        binding.rvImagePreviews.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvImagePreviews.setAdapter(imagesAdapter);
    }

    private void setupListeners() {
        binding.btnCreateBack.setOnClickListener(v -> finish());
        binding.btnSelectImages.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        binding.btnCreateSubmit.setOnClickListener(v -> submitProperty());

        // City selection cascading trigger
        binding.spinnerCreateCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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

        // Price text converter watch (VND conversion display)
        binding.etCreatePrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePriceHint();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.spinnerCreateCurrency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updatePriceHint();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void updatePriceHint() {
        String priceStr = binding.etCreatePrice.getText().toString().trim();
        String currency = binding.spinnerCreateCurrency.getSelectedItem() != null ?
                binding.spinnerCreateCurrency.getSelectedItem().toString() : "USD";

        if (TextUtils.isEmpty(priceStr) || "USD".equalsIgnoreCase(currency)) {
            binding.tvCreatePriceHint.setVisibility(View.GONE);
            return;
        }

        try {
            double amount = Double.parseDouble(priceStr);
            double usdAmount = amount / 25000.0; // Fallback conversion rate
            binding.tvCreatePriceHint.setText(String.format(Locale.US, "≈ $%,.2f USD (Saved in USD backend)", usdAmount));
            binding.tvCreatePriceHint.setVisibility(View.VISIBLE);
        } catch (NumberFormatException e) {
            binding.tvCreatePriceHint.setVisibility(View.GONE);
        }
    }

    private void loadMetadata() {
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
                }
            }

            @Override
            public void onFailure(Call<SearchMetadata> call, Throwable t) {
                Toast.makeText(CreatePropertyActivity.this, "Không thể tải cấu hình danh mục", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateSpinners() {
        // 1. City spinner
        ArrayAdapter<City> cityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, citiesList);
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCreateCity.setAdapter(cityAdapter);

        // 2. Type spinner
        List<String> typeNames = new ArrayList<>();
        for (Category cat : categoriesList) {
            typeNames.add(cat.getName());
        }
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, typeNames);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCreateType.setAdapter(typeAdapter);

        // 3. Direction spinner
        List<String> directions = Arrays.asList("Đông", "Tây", "Nam", "Bắc", "Đông Bắc", "Đông Nam", "Tây Bắc", "Tây Nam");
        ArrayAdapter<String> directionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, directions);
        directionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCreateDirection.setAdapter(directionAdapter);

        // 4. Currency spinner
        List<String> currencies = Arrays.asList("USD", "VND");
        ArrayAdapter<String> currencyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, currencies);
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCreateCurrency.setAdapter(currencyAdapter);
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
        binding.spinnerCreateDistrict.setAdapter(districtAdapter);
    }

    private void submitProperty() {
        String title = binding.etCreateTitle.getText().toString().trim();
        String description = binding.etCreateDescription.getText().toString().trim();
        String priceStr = binding.etCreatePrice.getText().toString().trim();
        String areaStr = binding.etCreateArea.getText().toString().trim();
        String address = binding.etCreateAddress.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            binding.etCreateTitle.setError("Không được bỏ trống tiêu đề");
            return;
        }
        if (TextUtils.isEmpty(priceStr)) {
            binding.etCreatePrice.setError("Không được bỏ trống giá");
            return;
        }
        if (TextUtils.isEmpty(areaStr)) {
            binding.etCreateArea.setError("Không được bỏ trống diện tích");
            return;
        }
        if (TextUtils.isEmpty(address)) {
            binding.etCreateAddress.setError("Không được bỏ trống địa chỉ");
            return;
        }

        binding.btnCreateSubmit.setEnabled(false);
        binding.tvCreateError.setVisibility(View.GONE);

        // Currency calculations: scale price to USD backend
        double enteredPrice = Double.parseDouble(priceStr);
        String currency = binding.spinnerCreateCurrency.getSelectedItem().toString();
        double finalPriceUsd = enteredPrice;
        if ("VND".equalsIgnoreCase(currency)) {
            finalPriceUsd = enteredPrice / 25000.0;
        }

        String listingType = binding.rbCreateSale.isChecked() ? "sale" : "rent";
        
        String typeName = binding.spinnerCreateType.getSelectedItem().toString();
        String directionSelected = binding.spinnerCreateDirection.getSelectedItem().toString();
        String direction = getDirectionValue(directionSelected);

        Integer districtId = null;
        if (binding.spinnerCreateDistrict.getSelectedItem() != null) {
            districtId = ((District) binding.spinnerCreateDistrict.getSelectedItem()).getDistrictId();
        }

        Map<String, Object> body = new HashMap<>();
        body.put("title", title);
        body.put("description", description);
        body.put("property_type", typeName);
        body.put("listing_type", listingType);
        body.put("price_usd", finalPriceUsd);
        body.put("area_sqm", Double.parseDouble(areaStr));
        
        String beds = binding.etCreateBedrooms.getText().toString().trim();
        body.put("bedrooms", TextUtils.isEmpty(beds) ? 0 : Integer.parseInt(beds));
        
        String baths = binding.etCreateBathrooms.getText().toString().trim();
        body.put("bathrooms", TextUtils.isEmpty(baths) ? 0 : Integer.parseInt(baths));
        
        body.put("direction", direction);
        body.put("address", address);
        
        String video = binding.etCreateVideoUrl.getText().toString().trim();
        if (!TextUtils.isEmpty(video)) {
            body.put("video_url", video);
        }

        // Feature IDs selected
        Set<Integer> fIds = featuresAdapter.getSelectedFeatureIds();
        body.put("features", fIds);

        apiService.createProperty(body).enqueue(new Callback<Property>() {
            @Override
            public void onResponse(Call<Property> call, Response<Property> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int createdPropertyId = response.body().getPropertyId();
                    if (!selectedImageUris.isEmpty()) {
                        uploadListingImages(createdPropertyId);
                    } else {
                        Toast.makeText(CreatePropertyActivity.this, "Đăng tin thành công! Chờ Admin duyệt.", Toast.LENGTH_LONG).show();
                        finish();
                    }
                } else {
                    binding.btnCreateSubmit.setEnabled(true);
                    showError("Đăng tin thất bại: Lỗi máy chủ");
                }
            }

            @Override
            public void onFailure(Call<Property> call, Throwable t) {
                binding.btnCreateSubmit.setEnabled(true);
                showError("Lỗi kết nối mạng: " + t.getMessage());
            }
        });
    }

    private void uploadListingImages(int propertyId) {
        binding.tvCreateError.setText("Đang tải ảnh lên Cloudinary...");
        binding.tvCreateError.setVisibility(View.VISIBLE);

        List<MultipartBody.Part> imageParts = new ArrayList<>();
        for (int i = 0; i < selectedImageUris.size(); i++) {
            File file = getFileFromUri(selectedImageUris.get(i), "img_" + i);
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
                if (response.isSuccessful()) {
                    Toast.makeText(CreatePropertyActivity.this, "Đăng tin và tải ảnh thành công! Chờ duyệt.", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(CreatePropertyActivity.this, "Đăng tin thành công nhưng tải ảnh lên Cloudinary lỗi.", Toast.LENGTH_LONG).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                Toast.makeText(CreatePropertyActivity.this, "Đăng tin thành công nhưng lỗi mạng khi tải ảnh.", Toast.LENGTH_LONG).show();
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
        binding.tvCreateError.setText(msg);
        binding.tvCreateError.setVisibility(View.VISIBLE);
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
