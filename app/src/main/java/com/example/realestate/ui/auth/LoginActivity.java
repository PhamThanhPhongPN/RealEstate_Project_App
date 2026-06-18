package com.example.realestate.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.example.realestate.MainActivity;
import com.example.realestate.data.model.AuthResponse;
import com.example.realestate.data.remote.ApiService;
import com.example.realestate.data.remote.RetrofitClient;
import com.example.realestate.databinding.ActivityLoginBinding;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = RetrofitClient.getApiService(this);

        // Check active session (auto login)
        checkSession();

        binding.btnLogin.setOnClickListener(v -> loginUser());
        
        binding.tvNavigateRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        binding.tvForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
        });
    }

    private void checkSession() {
        binding.tvErrorMessage.setVisibility(View.GONE);
        apiService.getMe().enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    // Already logged in, go to main dashboard
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                // Fail silently (not logged in)
            }
        });
    }

    private void loginUser() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            binding.etEmail.setError("Email không được để trống");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            binding.etPassword.setError("Mật khẩu không được để trống");
            return;
        }

        binding.btnLogin.setEnabled(false);
        binding.tvErrorMessage.setVisibility(View.GONE);

        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);

        apiService.login(body).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                binding.btnLogin.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse res = response.body();
                    if (res.isSuccess()) {
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else if (res.getRequiresVerification() != null && res.getRequiresVerification()) {
                        // Redirect to OTP verification screen
                        Intent intent = new Intent(LoginActivity.this, VerifyOtpActivity.class);
                        intent.putExtra("email", email);
                        startActivity(intent);
                    } else {
                        showError(res.getError() != null ? res.getError() : "Đăng nhập thất bại");
                    }
                } else {
                    String errorMsg = "Email hoặc mật khẩu không hợp lệ";
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
                binding.btnLogin.setEnabled(true);
                showError("Lỗi kết nối mạng: " + t.getMessage());
            }
        });
    }

    private void showError(String msg) {
        binding.tvErrorMessage.setText(msg);
        binding.tvErrorMessage.setVisibility(View.VISIBLE);
    }
}
