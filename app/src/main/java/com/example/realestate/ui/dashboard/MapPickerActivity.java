package com.example.realestate.ui.dashboard;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.realestate.databinding.ActivityMapPickerBinding;

/**
 * MapPickerActivity
 *
 * Hosts a Leaflet map (loaded from assets/map_picker.html) inside a WebView.
 * The user taps on the map to drop/move a pin; the picked coordinates are
 * passed back to the calling Activity via setResult().
 *
 * Intent extras (input):
 *   "initial_lat" (double) – optional, pre-places the pin on an existing lat
 *   "initial_lng" (double) – optional, pre-places the pin on an existing lng
 *
 * Intent extras (output on RESULT_OK):
 *   "picked_lat" (double) – latitude chosen by the user
 *   "picked_lng" (double) – longitude chosen by the user
 */
public class MapPickerActivity extends AppCompatActivity {

    public static final String EXTRA_INITIAL_LAT = "initial_lat";
    public static final String EXTRA_INITIAL_LNG = "initial_lng";
    public static final String EXTRA_PICKED_LAT  = "picked_lat";
    public static final String EXTRA_PICKED_LNG  = "picked_lng";

    private ActivityMapPickerBinding binding;

    // Last coordinates received from the JavaScript bridge
    private double pickedLat = Double.NaN;
    private double pickedLng = Double.NaN;

    // ─── JavaScript Bridge ─────────────────────────────────────────────────────

    private class MapJsBridge {

        /**
         * Called by map_picker.html whenever the user taps the map or drags the pin.
         * Runs on a background thread – post UI work to the main thread if needed.
         */
        @JavascriptInterface
        public void onLocationPicked(double lat, double lng) {
            pickedLat = lat;
            pickedLng = lng;
        }
    }

    // ─── Activity Lifecycle ────────────────────────────────────────────────────

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapPickerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupWebView();
        setupListeners();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings settings = binding.webViewMap.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);

        // Expose the Java bridge as the global object "Android" in JS
        binding.webViewMap.addJavascriptInterface(new MapJsBridge(), "Android");

        binding.webViewMap.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                // Keep all navigation inside the WebView
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // If the caller passed existing coordinates, pre-pin them on the map
                double initialLat = getIntent().getDoubleExtra(EXTRA_INITIAL_LAT, Double.NaN);
                double initialLng = getIntent().getDoubleExtra(EXTRA_INITIAL_LNG, Double.NaN);
                if (!Double.isNaN(initialLat) && !Double.isNaN(initialLng)) {
                    pickedLat = initialLat;
                    pickedLng = initialLng;
                    String js = "javascript:window.setInitialLocation(" + initialLat + "," + initialLng + ");";
                    binding.webViewMap.loadUrl(js);
                }
            }
        });

        // Load the bundled HTML from the assets folder
        binding.webViewMap.loadUrl("file:///android_asset/map_picker.html");
    }

    private void setupListeners() {
        binding.btnMapBack.setOnClickListener(v -> finish());

        binding.btnMapConfirm.setOnClickListener(v -> {
            if (Double.isNaN(pickedLat) || Double.isNaN(pickedLng)) {
                Toast.makeText(this, "Vui lòng nhấn vào bản đồ để chọn vị trí", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent result = new Intent();
            result.putExtra(EXTRA_PICKED_LAT, pickedLat);
            result.putExtra(EXTRA_PICKED_LNG, pickedLng);
            setResult(RESULT_OK, result);
            finish();
        });
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onBackPressed() {
        // If the WebView can go back in history, let it; otherwise close the Activity
        if (binding.webViewMap.canGoBack()) {
            binding.webViewMap.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (binding != null) {
            binding.webViewMap.destroy();
        }
        binding = null;
    }
}
