package com.example.realestate;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.example.realestate.databinding.ActivityMainBinding;
import com.example.realestate.ui.main.*;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    
    private final Fragment homeFragment = new HomeFragment();
    private final SearchFragment searchFragment = new SearchFragment();
    private final Fragment favoritesFragment = new FavoritesFragment();
    private final Fragment inboxFragment = new InboxFragment();
    private final Fragment profileFragment = new ProfileFragment();
    private Fragment activeFragment = homeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize and add fragments (hide all except home)
        final FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().add(R.id.content_frame, profileFragment, "5").hide(profileFragment).commit();
        fm.beginTransaction().add(R.id.content_frame, inboxFragment, "4").hide(inboxFragment).commit();
        fm.beginTransaction().add(R.id.content_frame, favoritesFragment, "3").hide(favoritesFragment).commit();
        fm.beginTransaction().add(R.id.content_frame, searchFragment, "2").hide(searchFragment).commit();
        fm.beginTransaction().add(R.id.content_frame, homeFragment, "1").commit();

        setupNavigation();
    }

    private void setupNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                getSupportFragmentManager().beginTransaction().hide(activeFragment).show(homeFragment).commit();
                activeFragment = homeFragment;
                return true;
            } else if (id == R.id.nav_search) {
                getSupportFragmentManager().beginTransaction().hide(activeFragment).show(searchFragment).commit();
                activeFragment = searchFragment;
                return true;
            } else if (id == R.id.nav_favorites) {
                getSupportFragmentManager().beginTransaction().hide(activeFragment).show(favoritesFragment).commit();
                activeFragment = favoritesFragment;
                return true;
            } else if (id == R.id.nav_inbox) {
                getSupportFragmentManager().beginTransaction().hide(activeFragment).show(inboxFragment).commit();
                activeFragment = inboxFragment;
                return true;
            } else if (id == R.id.nav_profile) {
                getSupportFragmentManager().beginTransaction().hide(activeFragment).show(profileFragment).commit();
                activeFragment = profileFragment;
                return true;
            }
            return false;
        });
    }

    /**
     * Inter-fragment navigation method: routes to Search tab from Home search queries.
     */
    public void navigateToSearchTab(String keyword, Integer typeId, String listingType) {
        searchFragment.setInitialFilters(keyword, typeId, listingType);
        binding.bottomNavigation.setSelectedItemId(R.id.nav_search);
    }
}
