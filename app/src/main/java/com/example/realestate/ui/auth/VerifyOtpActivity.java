package com.example.realestate.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.realestate.data.model.AuthResponse;
import com.example.realestate.data.remote.ApiService;
import com.example.realestate.data.remote.RetrofitClient;
import com.example.realestate.databinding.ActivityVerifyOtpBinding;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerifyOtpActivity extends AppCompatActivity {
    private ActivityVerifyOtpBinding binding;
    private ApiService apiService;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVerifyOtpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = RetrofitClient.getApiService(this);
        email = getIntent().getStringExtra("email");

        if (email != null) {
            binding.tvOtpSentHint.setText("Mã OTP đã được gửi đến:\n" + email);
        }

        binding.btnVerifyOtp.setOnClickListener(v -> verifyOtp());
        binding.tvBackToLogin.setOnClickListener(v -> finish());
    }

    private void verifyOtp() {
        String token = binding.etOtpToken.getText().toString().trim();

        if (TextUtils.isEmpty(token)) {
            binding.etOtpToken.setError("Vui lòng nhập mã OTP");
            return;
        }

        binding.btnVerifyOtp.setEnabled(false);
        binding.tvOtpError.setVisibility(View.GONE);

        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("token", token);

        apiService.verifyOtp(body).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                binding.btnVerifyOtp.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse res = response.body();
                    if (res.isSuccess()) {
                        Toast.makeText(VerifyOtpActivity.this, "Xác thực tài khoản thành công! Hãy đăng nhập lại.", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        showError(res.getError() != null ? res.getError() : "Mã xác thực không hợp lệ");
                    }
                } else {
                    showError("Mã xác thực không đúng hoặc đã hết hạn");
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                binding.btnVerifyOtp.setEnabled(true);
                showError("Lỗi kết nối mạng: " + t.getMessage());
            }
        });
    }

    private void showError(String msg) {
        binding.tvOtpError.setText(msg);
        binding.tvOtpError.setVisibility(View.VISIBLE);
    }
}
