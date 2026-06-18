package com.example.realestate.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.example.realestate.data.model.AuthResponse;
import com.example.realestate.data.remote.ApiService;
import com.example.realestate.data.remote.RetrofitClient;
import com.example.realestate.databinding.ActivityRegisterBinding;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = RetrofitClient.getApiService(this);

        binding.btnRegister.setOnClickListener(v -> registerUser());
        binding.tvNavigateLogin.setOnClickListener(v -> finish());
    }

    private void registerUser() {
        String fullName = binding.etRegisterFullname.getText().toString().trim();
        String email = binding.etRegisterEmail.getText().toString().trim();
        String phone = binding.etRegisterPhone.getText().toString().trim();
        String password = binding.etRegisterPassword.getText().toString().trim();
        
        String role = "user";
        if (binding.rbAgent.isChecked()) {
            role = "agent";
        }

        if (TextUtils.isEmpty(fullName)) {
            binding.etRegisterFullname.setError("Họ và tên không được để trống");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            binding.etRegisterEmail.setError("Email không được để trống");
            return;
        }
        if (TextUtils.isEmpty(phone)) {
            binding.etRegisterPhone.setError("Số điện thoại không được để trống");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            binding.etRegisterPassword.setError("Mật khẩu không được để trống");
            return;
        }

        binding.btnRegister.setEnabled(false);
        binding.tvRegisterError.setVisibility(View.GONE);

        Map<String, String> body = new HashMap<>();
        body.put("full_name", fullName);
        body.put("email", email);
        body.put("phone", phone);
        body.put("password", password);
        body.put("role", role);

        final String finalEmail = email;
        apiService.register(body).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                binding.btnRegister.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse res = response.body();
                    if (res.isSuccess()) {
                        // Registration success, navigate to OTP verify
                        Intent intent = new Intent(RegisterActivity.this, VerifyOtpActivity.class);
                        intent.putExtra("email", finalEmail);
                        startActivity(intent);
                        finish();
                    } else {
                        showError(res.getError() != null ? res.getError() : "Đăng ký thất bại");
                    }
                } else {
                    String errorMsg = "Email đã được sử dụng hoặc thông tin không hợp lệ";
                    try {
                        if (response.errorBody() != null) {
                            String errStr = response.errorBody().string();
                            com.google.gson.JsonObject json = com.google.gson.JsonParser.parseString(errStr).getAsJsonObject();
                            if (json.has("error")) {
                                errorMsg = json.get("error").getAsString();
                            }
                        }
                    } catch (Exception e) {
                        // Ignore
                    }
                    showError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                binding.btnRegister.setEnabled(true);
                showError("Lỗi kết nối mạng: " + t.getMessage());
            }
        });
    }

    private void showError(String msg) {
        binding.tvRegisterError.setText(msg);
        binding.tvRegisterError.setVisibility(View.VISIBLE);
    }
}
