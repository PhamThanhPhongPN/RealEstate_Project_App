package com.example.realestate.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.realestate.data.model.CommonResponse;
import com.example.realestate.data.remote.ApiService;
import com.example.realestate.data.remote.RetrofitClient;
import com.example.realestate.databinding.ActivityForgotPasswordBinding;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity {
    private ActivityForgotPasswordBinding binding;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = RetrofitClient.getApiService(this);

        binding.btnForgotSubmit.setOnClickListener(v -> requestOtp());
        binding.tvForgotBackToLogin.setOnClickListener(v -> finish());
    }

    private void requestOtp() {
        String email = binding.etForgotEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            binding.etForgotEmail.setError("Email không được để trống");
            return;
        }

        binding.btnForgotSubmit.setEnabled(false);
        binding.tvForgotError.setVisibility(View.GONE);

        Map<String, String> body = new HashMap<>();
        body.put("email", email);

        final String finalEmail = email;
        apiService.forgotPassword(body).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                binding.btnForgotSubmit.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    CommonResponse res = response.body();
                    if (res.isSuccess()) {
                        Toast.makeText(ForgotPasswordActivity.this, "Mã OTP khôi phục đã được gửi!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ForgotPasswordActivity.this, ResetPasswordActivity.class);
                        intent.putExtra("email", finalEmail);
                        startActivity(intent);
                        finish();
                    } else {
                        showError(res.getError() != null ? res.getError() : "Không tìm thấy tài khoản");
                    }
                } else {
                    showError("Địa chỉ email không hợp lệ hoặc không tồn tại");
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                binding.btnForgotSubmit.setEnabled(true);
                showError("Lỗi mạng: " + t.getMessage());
            }
        });
    }

    private void showError(String msg) {
        binding.tvForgotError.setText(msg);
        binding.tvForgotError.setVisibility(View.VISIBLE);
    }
}
