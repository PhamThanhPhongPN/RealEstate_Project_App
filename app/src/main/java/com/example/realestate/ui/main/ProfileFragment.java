package com.example.realestate.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.bumptech.glide.Glide;
import com.example.realestate.R;
import com.example.realestate.data.model.AuthResponse;
import com.example.realestate.data.model.CommonResponse;
import com.example.realestate.data.model.Property;
import com.example.realestate.data.model.User;
import com.example.realestate.data.remote.ApiService;
import com.example.realestate.data.remote.RetrofitClient;
import com.example.realestate.databinding.FragmentProfileBinding;
import com.example.realestate.ui.adapters.MyPropertiesAdapter;
import com.example.realestate.ui.auth.LoginActivity;
import com.example.realestate.ui.dashboard.CreatePropertyActivity;
import com.example.realestate.ui.dashboard.EditPropertyActivity;
import com.example.realestate.ui.dashboard.GuidelinesActivity;
import com.example.realestate.ui.dashboard.ActivityHistoryActivity;
import com.example.realestate.ui.subscription.PricingActivity;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import android.net.Uri;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    private ApiService apiService;
    private MyPropertiesAdapter adapter;
    private List<Property> myPropertiesList = new ArrayList<>();
    private String userPhone = "";

    private final androidx.activity.result.ActivityResultLauncher<String> avatarPickerLauncher = registerForActivityResult(
            new androidx.activity.result.contract.ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    uploadAvatar(uri);
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        apiService = RetrofitClient.getApiService(requireContext());

        setupRecycler();
        setupListeners();
        loadProfile();
    }

    private void setupRecycler() {
        adapter = new MyPropertiesAdapter(myPropertiesList, new MyPropertiesAdapter.OnMyPropertyActionListener() {
            @Override
            public void onEdit(Property property) {
                Intent intent = new Intent(requireContext(), EditPropertyActivity.class);
                intent.putExtra("property_id", property.getPropertyId());
                startActivity(intent);
            }

            @Override
            public void onDelete(Property property) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Xóa tin đăng")
                        .setMessage("Bạn có chắc chắn muốn xóa tin đăng này không?")
                        .setPositiveButton("Xóa", (dialog, which) -> deleteListing(property.getPropertyId()))
                        .setNegativeButton("Hủy", null)
                        .show();
            }

            @Override
            public void onBoost(Property property) {
                // Simulate boosting property to Silver VIP
                Map<String, String> body = new HashMap<>();
                body.put("propertyId", String.valueOf(property.getPropertyId()));
                body.put("vipTier", "silver");
                
                apiService.simulateSubscription(body).enqueue(new Callback<CommonResponse>() {
                    @Override
                    public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(requireContext(), "Kích hoạt Silver VIP thành công!", Toast.LENGTH_SHORT).show();
                            loadMyProperties();
                        } else {
                            Toast.makeText(requireContext(), "Kích hoạt VIP thất bại: Tin đăng cần được Duyệt trước", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<CommonResponse> call, Throwable t) {
                        Toast.makeText(requireContext(), "Lỗi mạng", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        binding.rvMyProperties.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvMyProperties.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.btnEditProfileTrigger.setOnClickListener(v -> showEditProfileDialog());
        binding.btnCreateListing.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), CreatePropertyActivity.class));
        });
        binding.cardVipPromo.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), PricingActivity.class));
        });
        binding.btnLogout.setOnClickListener(v -> performLogout());

        binding.btnNotifications.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), NotificationsActivity.class));
        });

        binding.btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());

        binding.btnActivityHistory.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), ActivityHistoryActivity.class));
        });

        binding.btnGuidelines.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), GuidelinesActivity.class));
        });

        binding.ivAvatar.setOnClickListener(v -> avatarPickerLauncher.launch("image/*"));
    }

    private void loadProfile() {
        apiService.getMe().enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body().getUser();
                    if (user != null) {
                        binding.tvUsername.setText(user.getFullName() != null ? user.getFullName() : "Chưa cập nhật tên");
                        binding.tvEmail.setText(user.getEmail());
                        userPhone = user.getPhone() != null ? user.getPhone() : "";

                        if ("admin".equalsIgnoreCase(user.getRole())) {
                            binding.tvRoleBadge.setText("Quản trị viên");
                            binding.tvRoleBadge.setBackgroundTintList(requireContext().getColorStateList(R.color.secondary_pink));
                        } else {
                            binding.tvRoleBadge.setText("Thành viên");
                            binding.tvRoleBadge.setBackgroundTintList(requireContext().getColorStateList(R.color.brand_blue_600));
                        }

                        String avatar = user.getAvatarUrl();
                        if (avatar == null || avatar.trim().isEmpty()) {
                            avatar = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=150&q=80";
                        }
                        Glide.with(requireContext())
                                .load(avatar)
                                .circleCrop()
                                .placeholder(android.R.drawable.sym_def_app_icon)
                                .into(binding.ivAvatar);
                    }
                    loadMyProperties();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {}
        });
    }

    private void loadMyProperties() {
        binding.myPropertiesProgress.setVisibility(View.VISIBLE);
        binding.tvMyPropertiesEmpty.setVisibility(View.GONE);

        apiService.getMyProperties().enqueue(new Callback<List<Property>>() {
            @Override
            public void onResponse(Call<List<Property>> call, Response<List<Property>> response) {
                binding.myPropertiesProgress.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    myPropertiesList.clear();
                    myPropertiesList.addAll(response.body());
                    adapter.notifyDataSetChanged();

                    if (myPropertiesList.isEmpty()) {
                        binding.tvMyPropertiesEmpty.setVisibility(View.VISIBLE);
                    }
                } else {
                    binding.tvMyPropertiesEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<List<Property>> call, Throwable t) {
                binding.myPropertiesProgress.setVisibility(View.GONE);
                binding.tvMyPropertiesEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showEditProfileDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_profile, null);
        android.widget.EditText etName = dialogView.findViewById(R.id.et_dialog_name);
        android.widget.EditText etPhone = dialogView.findViewById(R.id.et_dialog_phone);

        // Prepopulate
        String currentName = binding.tvUsername.getText().toString();
        etName.setText(currentName.equals("Chưa cập nhật tên") ? "" : currentName);
        etPhone.setText(userPhone);

        new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String phone = etPhone.getText().toString().trim();
                    if (!TextUtils.isEmpty(name)) {
                        saveProfileInfo(name, phone);
                    } else {
                        Toast.makeText(requireContext(), "Họ tên không được trống", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void saveProfileInfo(String name, String phone) {
        Map<String, String> body = new HashMap<>();
        body.put("full_name", name);
        body.put("phone", phone);

        apiService.updateProfile(body).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show();
                    loadProfile();
                } else {
                    Toast.makeText(requireContext(), "Không thể cập nhật hồ sơ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                Toast.makeText(requireContext(), "Lỗi mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteListing(int propertyId) {
        apiService.deleteProperty(propertyId).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Xóa tin đăng thành công", Toast.LENGTH_SHORT).show();
                    loadMyProperties();
                } else {
                    Toast.makeText(requireContext(), "Không thể xóa tin đăng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                Toast.makeText(requireContext(), "Lỗi mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performLogout() {
        apiService.logout().enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                // Irrespective of server response, clear local session cookies and go to Login
                requireContext().getSharedPreferences("app_cookies", 0).edit().clear().apply();
                Intent intent = new Intent(requireContext(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                if (getActivity() != null) getActivity().finish();
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                requireContext().getSharedPreferences("app_cookies", 0).edit().clear().apply();
                Intent intent = new Intent(requireContext(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                if (getActivity() != null) getActivity().finish();
            }
        });
    }

    private void showChangePasswordDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_change_password, null);
        android.widget.EditText etCurrentPassword = dialogView.findViewById(R.id.et_dialog_current_password);
        android.widget.EditText etNewPassword = dialogView.findViewById(R.id.et_dialog_new_password);
        android.widget.EditText etConfirmPassword = dialogView.findViewById(R.id.et_dialog_confirm_password);

        new AlertDialog.Builder(requireContext())
                .setTitle("Đổi mật khẩu")
                .setView(dialogView)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String currentPw = etCurrentPassword.getText().toString();
                    String newPw = etNewPassword.getText().toString();
                    String confirmPw = etConfirmPassword.getText().toString();

                    if (TextUtils.isEmpty(currentPw) || TextUtils.isEmpty(newPw) || TextUtils.isEmpty(confirmPw)) {
                        Toast.makeText(requireContext(), "Vui lòng nhập đầy đủ các trường", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (newPw.length() < 6) {
                        Toast.makeText(requireContext(), "Mật khẩu mới phải từ 6 ký tự", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!newPw.equals(confirmPw)) {
                        Toast.makeText(requireContext(), "Mật khẩu mới không trùng khớp", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    updatePassword(currentPw, newPw);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void updatePassword(String currentPw, String newPw) {
        Map<String, String> body = new HashMap<>();
        body.put("current_password", currentPw);
        body.put("new_password", newPw);

        apiService.changePassword(body).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Mật khẩu hiện tại không đúng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                Toast.makeText(requireContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadAvatar(Uri uri) {
        File file = getFileFromUri(uri, "avatar_upload");
        if (file == null) {
            Toast.makeText(requireContext(), "Lỗi đọc file hình ảnh", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        Toast.makeText(requireContext(), "Đang tải ảnh đại diện lên...", Toast.LENGTH_SHORT).show();

        apiService.uploadAvatar(body).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(requireContext(), "Cập nhật ảnh đại diện thành công!", Toast.LENGTH_SHORT).show();
                    loadProfile();
                } else {
                    Toast.makeText(requireContext(), "Không thể tải ảnh đại diện", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                Toast.makeText(requireContext(), "Lỗi mạng khi tải ảnh", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private File getFileFromUri(Uri uri, String name) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;
            File file = new File(requireContext().getCacheDir(), name + ".jpg");
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

    @Override
    public void onResume() {
        super.onResume();
        loadMyProperties();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
