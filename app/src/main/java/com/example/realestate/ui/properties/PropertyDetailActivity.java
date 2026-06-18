package com.example.realestate.ui.properties;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.bumptech.glide.Glide;
import com.example.realestate.R;
import com.example.realestate.data.model.*;
import com.example.realestate.data.remote.ApiService;
import com.example.realestate.data.remote.RetrofitClient;
import com.example.realestate.databinding.ActivityPropertyDetailBinding;
import com.example.realestate.databinding.ItemDetailImageBinding;
import com.example.realestate.databinding.ItemDetailFeatureBinding;
import com.example.realestate.ui.adapters.FeaturedPropertiesAdapter;
import com.example.realestate.ui.auth.LoginActivity;
import com.example.realestate.ui.chat.ChatActivity;
import java.util.*;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PropertyDetailActivity extends AppCompatActivity {
    private ActivityPropertyDetailBinding binding;
    private ApiService apiService;
    private int propertyId;
    private Property property;

    private boolean isVndMode = false; // Toggle currency display mode
    private final List<Property> similarList = new ArrayList<>();
    private FeaturedPropertiesAdapter similarAdapter;

    // Mortgage calculator params
    private double downPaymentPercent = 20.0;
    private double annualInterestRate = 6.5;
    private double loanTermYears = 25.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPropertyDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = RetrofitClient.getApiService(this);
        propertyId = getIntent().getIntExtra("property_id", -1);

        if (propertyId == -1) {
            Toast.makeText(this, "Không tìm thấy bất động sản", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupSimilarRecycler();
        setupListeners();
        loadPropertyDetails();
        syncFavoriteState();
    }

    private void setupSimilarRecycler() {
        similarAdapter = new FeaturedPropertiesAdapter(similarList, prop -> {
            Intent intent = new Intent(PropertyDetailActivity.this, PropertyDetailActivity.class);
            intent.putExtra("property_id", prop.getPropertyId());
            startActivity(intent);
        });
        binding.rvSimilarProperties.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvSimilarProperties.setAdapter(similarAdapter);
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnFavorite.setOnClickListener(v -> toggleFavorite());
        binding.btnReport.setOnClickListener(v -> showReportDialog());

        binding.btnCurrencyToggle.setOnClickListener(v -> {
            isVndMode = !isVndMode;
            binding.btnCurrencyToggle.setText(isVndMode ? "Xem giá USD" : "Xem giá VND");
            updatePrices();
            calculateMortgage();
        });

        // Contact Seller
        binding.btnContactAgent.setOnClickListener(v -> initiateContact());

        // Mortgage Sliders
        binding.sliderDownPayment.addOnChangeListener((slider, value, fromUser) -> {
            downPaymentPercent = value;
            binding.tvLabelDownPayment.setText(String.format(Locale.US, "Trả trước (Down Payment): %.0f%%", downPaymentPercent));
            calculateMortgage();
        });

        binding.sliderInterestRate.addOnChangeListener((slider, value, fromUser) -> {
            annualInterestRate = value;
            binding.tvLabelInterestRate.setText(String.format(Locale.US, "Lãi suất hàng năm: %.1f%%", annualInterestRate));
            calculateMortgage();
        });

        binding.sliderLoanTerm.addOnChangeListener((slider, value, fromUser) -> {
            loanTermYears = value;
            binding.tvLabelLoanTerm.setText(String.format(Locale.US, "Kỳ hạn vay: %.0f năm", loanTermYears));
            calculateMortgage();
        });
    }

    private void loadPropertyDetails() {
        apiService.getPropertyById(propertyId).enqueue(new Callback<Property>() {
            @Override
            public void onResponse(Call<Property> call, Response<Property> response) {
                if (response.isSuccessful() && response.body() != null) {
                    property = response.body();
                    populateDetails();
                    loadSimilarProperties();
                } else {
                    Toast.makeText(PropertyDetailActivity.this, "Không tìm thấy dữ liệu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Property> call, Throwable t) {
                Toast.makeText(PropertyDetailActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateDetails() {
        binding.tvDetailTitle.setText(property.getTitle());
        binding.tvDetailAddress.setText(property.getAddress() != null ? property.getAddress() : "Khu vực");
        binding.tvDetailDescription.setText(property.getDescription() != null ? property.getDescription() : "Không có mô tả.");

        binding.tvDetailBeds.setText(property.getBedrooms() != null ? property.getBedrooms() + " PN" : "0 PN");
        binding.tvDetailBaths.setText(property.getBathrooms() != null ? property.getBathrooms() + " PT" : "0 PT");
        binding.tvDetailArea.setText(property.getAreaM2() != null ? property.getAreaM2() + " m²" : "0 m²");
        binding.tvDetailDirection.setText(getDirectionLabel(property.getDirection()));

        updatePrices();

        // 1. Photo Slider (ViewPager2)
        List<PropertyImage> images = property.getImages();
        if (images.isEmpty()) {
            PropertyImage dImg = new PropertyImage();
            dImg.setImageUrl("https://images.unsplash.com/photo-1580587771525-78b9dba3b914?auto=format&fit=crop&w=600&q=80");
            images = Collections.singletonList(dImg);
        }

        ImageSliderAdapter sliderAdapter = new ImageSliderAdapter(images);
        binding.viewPagerImages.setAdapter(sliderAdapter);
        
        final List<PropertyImage> finalImages = images;
        binding.tvImageIndicator.setText("1/" + finalImages.size());
        binding.viewPagerImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                binding.tvImageIndicator.setText((position + 1) + "/" + finalImages.size());
            }
        });

        // 2. Features Grid
        if (property.getFeatures() != null && !property.getFeatures().isEmpty()) {
            binding.tvDetailFeaturesHeader.setVisibility(View.VISIBLE);
            binding.rvDetailFeatures.setVisibility(View.VISIBLE);
            binding.rvDetailFeatures.setLayoutManager(new LinearLayoutManager(this));
            binding.rvDetailFeatures.setAdapter(new DetailFeaturesAdapter(property.getFeatures()));
        } else {
            binding.tvDetailFeaturesHeader.setVisibility(View.GONE);
            binding.rvDetailFeatures.setVisibility(View.GONE);
        }

        // 3. Seller Card Info
        User seller = property.getOwner();
        if (seller != null) {
            binding.tvSellerName.setText(seller.getFullName() != null ? seller.getFullName() : "Chủ nhà");
            binding.tvSellerPhone.setText(seller.getPhone() != null ? seller.getPhone() : "Liên hệ để nhận số điện thoại");
            String sellerAvatar = seller.getAvatarUrl();
            if (sellerAvatar == null || sellerAvatar.trim().isEmpty()) {
                sellerAvatar = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=150&q=80";
            }
            Glide.with(this)
                    .load(sellerAvatar)
                    .circleCrop()
                    .placeholder(android.R.drawable.sym_def_app_icon)
                    .into(binding.ivSellerAvatar);
        }

        // Calculate mortgage principal initial
        calculateMortgage();
    }

    private void updatePrices() {
        if (property == null) return;
        double price = property.getPriceUsd();
        String priceText;

        if (isVndMode) {
            double vndPrice = price * 25000.0;
            priceText = String.format(Locale.US, "%,.0f VND", vndPrice);
            if ("rent".equalsIgnoreCase(property.getListingType())) {
                priceText += "/tháng";
            }
        } else {
            priceText = String.format(Locale.US, "$%,.0f", price);
            if ("rent".equalsIgnoreCase(property.getListingType())) {
                priceText += "/tháng";
            }
        }

        binding.tvDetailPrice.setText(priceText);
        binding.tvBottomPrice.setText(priceText);
    }

    private void calculateMortgage() {
        if (property == null) return;

        double priceUsd = property.getPriceUsd();
        double displayPrice = priceUsd;
        String curSymbol = "$";

        if (isVndMode) {
            displayPrice = priceUsd * 25000.0;
            curSymbol = "VND ";
        }

        // 1. Calculate down payment and principal amount
        double downPayment = displayPrice * (downPaymentPercent / 100.0);
        double principal = displayPrice - downPayment;

        binding.tvMortgagePrincipal.setText(String.format(Locale.US, "Số tiền cần vay: %,.0f %s", principal, isVndMode ? "VND" : "USD"));

        // 2. Solve mortgage payment formula:
        // M = P * [ i * (1 + i)^n ] / [ (1 + i)^n - 1 ]
        double monthlyInterestRate = (annualInterestRate / 12.0) / 100.0;
        double numberOfPayments = loanTermYears * 12.0;

        double monthlyPayment = 0.0;
        if (monthlyInterestRate > 0) {
            double factor = Math.pow(1 + monthlyInterestRate, numberOfPayments);
            monthlyPayment = principal * (monthlyInterestRate * factor) / (factor - 1);
        } else if (numberOfPayments > 0) {
            monthlyPayment = principal / numberOfPayments;
        }

        if (isVndMode) {
            binding.tvMortgageMonthlyPayment.setText(String.format(Locale.US, "%,.0f VND / tháng", monthlyPayment));
        } else {
            binding.tvMortgageMonthlyPayment.setText(String.format(Locale.US, "$%,.2f / tháng", monthlyPayment));
        }
    }

    private void loadSimilarProperties() {
        apiService.getSimilarProperties(propertyId).enqueue(new Callback<List<Property>>() {
            @Override
            public void onResponse(Call<List<Property>> call, Response<List<Property>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    similarList.clear();
                    similarList.addAll(response.body());
                    similarAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Property>> call, Throwable t) {}
        });
    }

    private void syncFavoriteState() {
        apiService.getFavoriteIds().enqueue(new Callback<List<Integer>>() {
            @Override
            public void onResponse(Call<List<Integer>> call, Response<List<Integer>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().contains(propertyId)) {
                        binding.btnFavorite.setColorFilter(getResources().getColor(R.color.secondary_pink));
                    } else {
                        binding.btnFavorite.setColorFilter(getResources().getColor(R.color.white));
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Integer>> call, Throwable t) {}
        });
    }

    private void toggleFavorite() {
        apiService.toggleFavorite(propertyId).enqueue(new Callback<Map<String, Boolean>>() {
            @Override
            public void onResponse(Call<Map<String, Boolean>> call, Response<Map<String, Boolean>> response) {
                if (response.isSuccessful()) {
                    syncFavoriteState();
                } else {
                    Toast.makeText(PropertyDetailActivity.this, "Vui lòng đăng nhập để lưu tin đăng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Boolean>> call, Throwable t) {
                Toast.makeText(PropertyDetailActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initiateContact() {
        if (property == null) return;

        // Verify session first
        apiService.getMe().enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    // Start or create conversation
                    Map<String, String> body = new HashMap<>();
                    body.put("propertyId", String.valueOf(propertyId));
                    body.put("receiverId", String.valueOf(property.getOwnerId()));

                    apiService.createConversation(body).enqueue(new Callback<Map<String, String>>() {
                        @Override
                        public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                String convIdStr = response.body().get("conversationId");
                                int convId = convIdStr != null ? Integer.parseInt(convIdStr) : -1;
                                
                                User owner = property.getOwner();
                                Intent intent = new Intent(PropertyDetailActivity.this, ChatActivity.class);
                                intent.putExtra("conversation_id", convId);
                                intent.putExtra("receiver_id", property.getOwnerId());
                                intent.putExtra("receiver_name", owner != null ? owner.getFullName() : "Chủ nhà");
                                intent.putExtra("receiver_avatar", owner != null ? owner.getAvatarUrl() : "");
                                intent.putExtra("property_title", property.getTitle());
                                intent.putExtra("property_id", property.getPropertyId());
                                startActivity(intent);
                            }
                        }

                        @Override
                        public void onFailure(Call<Map<String, String>> call, Throwable t) {
                            Toast.makeText(PropertyDetailActivity.this, "Lỗi tạo hội thoại", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    startActivity(new Intent(PropertyDetailActivity.this, LoginActivity.class));
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                startActivity(new Intent(PropertyDetailActivity.this, LoginActivity.class));
            }
        });
    }

    private void showReportDialog() {
        apiService.getMe().enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    showReportDialogForm();
                } else {
                    Toast.makeText(PropertyDetailActivity.this, "Vui lòng đăng nhập để báo cáo tin đăng", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(PropertyDetailActivity.this, LoginActivity.class));
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                Toast.makeText(PropertyDetailActivity.this, "Vui lòng đăng nhập để báo cáo tin đăng", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(PropertyDetailActivity.this, LoginActivity.class));
            }
        });
    }

    private void showReportDialogForm() {
        String[] reasons = {"Spam hoặc trùng lặp (Spam/Duplicate)", "Lừa đảo (Fraud/Scam)", "Thông tin sai lệch (Wrong Info)", "Nội dung phản cảm (Offensive Content)", "Khác (Other)"};
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_report_property, null);
        android.widget.Spinner spinner = dialogView.findViewById(R.id.spinner_report_reason);
        android.widget.EditText etDetails = dialogView.findViewById(R.id.et_report_details);

        android.widget.ArrayAdapter<String> spinnerAdapter = new android.widget.ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, reasons);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Báo cáo tin đăng")
                .setView(dialogView)
                .setPositiveButton("Gửi báo cáo", (dialog, which) -> {
                    String reason = spinner.getSelectedItem().toString();
                    String details = etDetails.getText().toString().trim();
                    submitReport(reason, details);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void submitReport(String reason, String details) {
        Map<String, Object> body = new HashMap<>();
        body.put("property_id", propertyId);
        body.put("reason", reason);
        body.put("details", details);

        apiService.submitReport(body).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(PropertyDetailActivity.this, "Gửi báo cáo thành công! Cảm ơn đóng góp của bạn.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(PropertyDetailActivity.this, "Không thể gửi báo cáo lúc này.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                Toast.makeText(PropertyDetailActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    // ── Image Slider Adapter (ViewPager2) ──
    private static class ImageSliderAdapter extends RecyclerView.Adapter<ImageSliderAdapter.ViewHolder> {
        private final List<PropertyImage> images;

        ImageSliderAdapter(List<PropertyImage> images) {
            this.images = images;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemDetailImageBinding b = ItemDetailImageBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(b);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            PropertyImage img = images.get(position);
            Glide.with(holder.itemView.getContext())
                    .load(img.getImageUrl())
                    .centerCrop()
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(holder.binding.ivDetailImg);
        }

        @Override
        public int getItemCount() {
            return images.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            final ItemDetailImageBinding binding;
            ViewHolder(ItemDetailImageBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }

    // ── Features Grid Adapter (RecyclerView) ──
    private static class DetailFeaturesAdapter extends RecyclerView.Adapter<DetailFeaturesAdapter.ViewHolder> {
        private final List<Feature> features;

        DetailFeaturesAdapter(List<Feature> features) {
            this.features = features;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemDetailFeatureBinding b = ItemDetailFeatureBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(b);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Feature f = features.get(position);
            holder.binding.tvFeatureLabel.setText(f.getName());
        }

        @Override
        public int getItemCount() {
            return features.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            final ItemDetailFeatureBinding binding;
            ViewHolder(ItemDetailFeatureBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }

    private String getDirectionLabel(String dir) {
        if (dir == null) return "N/A";
        switch (dir.trim().toLowerCase()) {
            case "east": return "Đông";
            case "west": return "Tây";
            case "south": return "Nam";
            case "north": return "Bắc";
            case "northeast": return "Đông Bắc";
            case "southeast": return "Đông Nam";
            case "northwest": return "Tây Bắc";
            case "southwest": return "Tây Nam";
            default: return dir.toUpperCase();
        }
    }
}
