package com.example.realestate.ui.dashboard;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.realestate.databinding.ActivityGuidelinesBinding;

public class GuidelinesActivity extends AppCompatActivity {
    private ActivityGuidelinesBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGuidelinesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnGuidelinesBack.setOnClickListener(v -> finish());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
