package com.example.pantrybuddy;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

// Main host activity for bottom navigation and fragments
public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_NAV_TAB = "extra_nav_tab";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.container), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
            NavigationUI.setupWithNavController(bottomNav, navController);

            handleNavTabIntent(getIntent(), bottomNav);
        }

        com.example.pantrybuddy.utils.NotificationHelper.createNotificationChannel(this);
        if (com.example.pantrybuddy.utils.PreferenceHelper.isExpiryAlertsEnabled(this)) {
            com.example.pantrybuddy.utils.ExpiryReminderWorker.scheduleDailyReminders(this);
        }
    }

    @Override
    protected void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            handleNavTabIntent(intent, bottomNav);
        }
    }

    private void handleNavTabIntent(android.content.Intent intent, BottomNavigationView bottomNav) {
        if (intent != null && intent.hasExtra(EXTRA_NAV_TAB)) {
            int tabId = intent.getIntExtra(EXTRA_NAV_TAB, -1);
            if (tabId != -1) {
                bottomNav.setSelectedItemId(tabId);
            }
        }
    }
}