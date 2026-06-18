package com.example.realestate.ui.subscription;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.realestate.databinding.ActivityPricingBinding;

public class PricingActivity extends AppCompatActivity {
    private ActivityPricingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPricingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnPricingBack.setOnClickListener(v -> finish());

        // The package activation is initiated directly inside the My Properties listing manager on the Profile tab.
        binding.btnBuySilver.setOnClickListener(v -> finish());
        binding.btnBuyGold.setOnClickListener(v -> finish());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
