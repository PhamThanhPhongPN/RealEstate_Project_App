package com.example.realestate.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.realestate.data.model.CommonResponse;
import com.example.realestate.data.remote.ApiService;
import com.example.realestate.data.remote.RetrofitClient;
import com.example.realestate.databinding.ActivityResetPasswordBinding;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPasswordActivity extends AppCompatActivity {
    private ActivityResetPasswordBinding binding;
    private ApiService apiService;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResetPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = RetrofitClient.getApiService(this);
        email = getIntent().getStringExtra("email");

        binding.btnResetSubmit.setOnClickListener(v -> resetPassword());
        binding.tvResetBackToLogin.setOnClickListener(v -> finish());
    }

    private void resetPassword() {
        String token = binding.etResetOtp.getText().toString().trim();
        String newPassword = binding.etResetNewPassword.getText().toString().trim();

        if (TextUtils.isEmpty(token)) {
            binding.etResetOtp.setError("Nhập mã OTP");
            return;
        }
        if (TextUtils.isEmpty(newPassword)) {
            binding.etResetNewPassword.setError("Nhập mật khẩu mới");
            return;
        }

        binding.btnResetSubmit.setEnabled(false);
        binding.tvResetError.setVisibility(View.GONE);

        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("token", token);
        body.put("newPassword", newPassword);

        apiService.resetPassword(body).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                binding.btnResetSubmit.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    CommonResponse res = response.body();
                    if (res.isSuccess()) {
                        Toast.makeText(ResetPasswordActivity.this, "Đặt lại mật khẩu thành công! Hãy đăng nhập lại.", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        showError(res.getError() != null ? res.getError() : "Mã OTP không đúng hoặc hết hạn");
                    }
                } else {
                    showError("Mã OTP không đúng hoặc hết hạn");
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                binding.btnResetSubmit.setEnabled(true);
                showError("Lỗi kết nối mạng: " + t.getMessage());
            }
        });
    }

    private void showError(String msg) {
        binding.tvResetError.setText(msg);
        binding.tvResetError.setVisibility(View.VISIBLE);
    }
}
