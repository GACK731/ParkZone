package com.s23010344.parkzone;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FavoriteParksActivity extends AppCompatActivity {

    private static final String TAG = "FavoriteParksActivity";

    private LinearLayout favoriteParksList;
    private ProgressBar progressBar;
    private LinearLayout layoutNoFavorites;
    private ImageView btnBack;

    private SessionManager sessionManager;
    private DatabaseReference userRef, parksRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favourite_parks);

        Log.d(TAG, "onCreate: Starting FavoriteParksActivity");

        // Initialize session manager and check login
        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            Log.d(TAG, "User not logged in, redirecting to login");
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        String userId = sessionManager.getUserId();
        Log.d(TAG, "User ID: " + userId);

        initializeViews();
        setupFirebaseReferences();

        // Simple approach - immediately try to load data
        loadFavoriteParks();
    }

    private void initializeViews() {
        favoriteParksList = findViewById(R.id.favoriteParksList);
        progressBar = findViewById(R.id.progressBar);
        layoutNoFavorites = findViewById(R.id.layoutNoFavorites);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> {
            Log.d(TAG, "Back button clicked");
            finish();
        });
    }

    private void setupFirebaseReferences() {
        String userId = sessionManager.getUserId();
        userRef = FirebaseDatabase.getInstance("https://parkzone-5c7dc-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users")
                .child(userId);

        parksRef = FirebaseDatabase.getInstance("https://parkzone-5c7dc-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("Parks");

        Log.d(TAG, "Firebase references setup completed");
    }

    private void loadFavoriteParks() {
        Log.d(TAG, "=== DEBUGGING FAVORITE PARKS ===");
        Log.d(TAG, "Starting to load favorite parks");

        // Force show something immediately for debugging
        showDebugInfo();

        // Always hide loading and show something within 3 seconds maximum
        new android.os.Handler().postDelayed(() -> {
            Log.w(TAG, "Timeout reached - forcing display");
            if (progressBar.getVisibility() == View.VISIBLE) {
                progressBar.setVisibility(View.GONE);
                showNoFavorites();
                Toast.makeText(this, "Loading timeout - check logs", Toast.LENGTH_LONG).show();
            }
        }, 5000); // Extended to 5 seconds for debugging

        String userId = sessionManager.getUserId();
        String userEmail = sessionManager.getUserEmail();
        Log.d(TAG, "DEBUG - User ID: " + userId);
        Log.d(TAG, "DEBUG - User Email: " + userEmail);
        Log.d(TAG, "DEBUG - User Reference Path: users/" + userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "=== FIREBASE CALLBACK RECEIVED ===");
                Log.d(TAG, "Snapshot exists: " + snapshot.exists());
                Log.d(TAG, "Snapshot key: " + snapshot.getKey());
                Log.d(TAG, "Snapshot children count: " + snapshot.getChildrenCount());
                Log.d(TAG, "Raw snapshot value: " + snapshot.getValue());

                progressBar.setVisibility(View.GONE);

                if (!snapshot.exists()) {
                    Log.d(TAG, "ISSUE: No user data found in Firebase");
                    createTestData(); // Create test data for debugging
                    return;
                }

                try {
                    // Try to parse as different types to debug
                    Object rawData = snapshot.getValue();
                    Log.d(TAG, "Raw data type: " + (rawData != null ? rawData.getClass().getName() : "null"));

                    if (rawData instanceof java.util.Map) {
                        java.util.Map<String, Object> dataMap = (java.util.Map<String, Object>) rawData;
                        Log.d(TAG, "Data as map: " + dataMap.toString());

                        for (String key : dataMap.keySet()) {
                            Log.d(TAG, "Map key: " + key + " = " + dataMap.get(key));
                        }
                    }

                    User user = snapshot.getValue(User.class);
                    Log.d(TAG, "User object parsed: " + (user != null));

                    if (user != null) {
                        Log.d(TAG, "User email: " + user.email);
                        Log.d(TAG, "User username: " + user.username);
                        Log.d(TAG, "Favorite parks object: " + user.favourite_parks);
                        Log.d(TAG, "Favorite parks type: " + (user.favourite_parks != null ? user.favourite_parks.getClass().getName() : "null"));

                        if (user.favourite_parks != null) {
                            Log.d(TAG, "Favorite parks size: " + user.favourite_parks.size());
                            for (int i = 0; i < user.favourite_parks.size(); i++) {
                                Log.d(TAG, "Favorite park " + i + ": " + user.favourite_parks.get(i));
                            }
                        }
                    }

                    if (user == null || user.favourite_parks == null || user.favourite_parks.isEmpty()) {
                        Log.d(TAG, "ISSUE: No favorite parks found - creating test data");
                        createTestData();
                        return;
                    }

                    Log.d(TAG, "SUCCESS: Found " + user.favourite_parks.size() + " favorite parks");
                    createFavoriteParkViews(user.favourite_parks);

                } catch (Exception e) {
                    Log.e(TAG, "ERROR parsing user data: " + e.getMessage());
                    e.printStackTrace();
                    createTestData();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Firebase error: " + error.getMessage());
                Log.e(TAG, "Error code: " + error.getCode());
                Log.e(TAG, "Error details: " + error.getDetails());
                progressBar.setVisibility(View.GONE);
                createTestData();
            }
        });
    }

    private void showDebugInfo() {
        // Show immediate debug info
        favoriteParksList.removeAllViews();

        TextView debugInfo = new TextView(this);
        debugInfo.setText("🔍 DEBUGGING MODE\n\nChecking Firebase data...\nUser ID: " + sessionManager.getUserId() + "\n\nCheck Android Studio Logcat for detailed logs with tag 'FavoriteParksActivity'");
        debugInfo.setTextSize(16);
        debugInfo.setTextColor(getResources().getColor(android.R.color.black));
        debugInfo.setPadding(32, 32, 32, 32);
        debugInfo.setBackgroundColor(getResources().getColor(R.color.lightblue));

        LinearLayout.LayoutParams debugParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        debugParams.setMargins(0, 0, 0, 24);
        debugInfo.setLayoutParams(debugParams);

        favoriteParksList.addView(debugInfo);
        showFavoriteParks();
    }

    private void createTestData() {
        Log.d(TAG, "Creating test data to verify UI works");

        favoriteParksList.removeAllViews();

        // Create test park views
        for (int i = 1; i <= 2; i++) {
            View testView = createParkPlaceholder("test" + i, "Test Park " + i, "Test Location " + i, false);
            favoriteParksList.addView(testView);
        }

        TextView testInfo = new TextView(this);
        testInfo.setText("⚠️ TEST DATA SHOWN\n\nThis means:\n1. UI is working ✓\n2. No real favorites found in Firebase\n3. Check logs for Firebase data issues");
        testInfo.setTextSize(14);
        testInfo.setTextColor(getResources().getColor(android.R.color.black));
        testInfo.setPadding(24, 24, 24, 24);
        testInfo.setBackgroundColor(getResources().getColor(R.color.gray));

        LinearLayout.LayoutParams testParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        testParams.setMargins(0, 0, 0, 24);
        testInfo.setLayoutParams(testParams);

        favoriteParksList.addView(testInfo, 0); // Add at top

        showFavoriteParks();
    }

    private void createFavoriteParkViews(List<String> favoriteParkIds) {
        Log.d(TAG, "Creating views for " + favoriteParkIds.size() + " parks");

        // Clear existing views
        favoriteParksList.removeAllViews();

        // For each park ID, create a view and load data
        for (int i = 0; i < favoriteParkIds.size(); i++) {
            String parkId = favoriteParkIds.get(i);
            createParkViewForId(parkId, i + 1);
        }

        showFavoriteParks();
    }

    private void createParkViewForId(String parkId, int parkNumber) {
        // Create a park view immediately with placeholder data
        View parkView = createParkPlaceholder(parkId, "Loading Park " + parkNumber + "...", "Loading location...", false);
        favoriteParksList.addView(parkView);

        // Then load the actual data
        parksRef.child(parkId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Park park = snapshot.getValue(Park.class);
                    if (park != null) {
                        park.id = parkId;
                        updateParkView(parkView, park);
                        Log.d(TAG, "Updated park view for: " + park.name);
                    }
                } else {
                    Log.w(TAG, "Park not found: " + parkId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading park " + parkId);
            }
        });
    }

    private View createParkPlaceholder(String parkId, String name, String location, boolean isPaid) {
        // Create a simple park view
        LinearLayout parkItem = new LinearLayout(this);
        parkItem.setOrientation(LinearLayout.HORIZONTAL);
        parkItem.setPadding(32, 32, 32, 32);
        parkItem.setBackgroundColor(getResources().getColor(android.R.color.white));

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, 0, 0, 24);
        parkItem.setLayoutParams(layoutParams);

        // Park info container
        LinearLayout textContainer = new LinearLayout(this);
        textContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        textContainer.setLayoutParams(textParams);

        // Park name
        TextView tvParkName = new TextView(this);
        tvParkName.setTag("parkName"); // Add tag for easy finding
        tvParkName.setText(name);
        tvParkName.setTextSize(20);
        tvParkName.setTextColor(getResources().getColor(android.R.color.black));
        tvParkName.setTypeface(null, android.graphics.Typeface.BOLD);

        // Park location
        TextView tvParkLocation = new TextView(this);
        tvParkLocation.setTag("parkLocation"); // Add tag for easy finding
        tvParkLocation.setText(location);
        tvParkLocation.setTextSize(16);
        tvParkLocation.setTextColor(getResources().getColor(android.R.color.darker_gray));

        LinearLayout.LayoutParams locationParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        locationParams.setMargins(0, 16, 0, 0);
        tvParkLocation.setLayoutParams(locationParams);

        // Park type
        TextView tvParkType = new TextView(this);
        tvParkType.setTag("parkType"); // Add tag for easy finding
        tvParkType.setText(isPaid ? "Paid Parking" : "Free Parking");
        tvParkType.setTextSize(14);
        tvParkType.setTextColor(getResources().getColor(R.color.theme_color));

        LinearLayout.LayoutParams typeParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        typeParams.setMargins(0, 8, 0, 0);
        tvParkType.setLayoutParams(typeParams);

        // Remove button
        TextView btnRemove = new TextView(this);
        btnRemove.setText("❤️ Remove");
        btnRemove.setTextSize(14);
        btnRemove.setTextColor(getResources().getColor(R.color.theme_color));
        btnRemove.setPadding(16, 16, 16, 16);
        btnRemove.setClickable(true);
        btnRemove.setFocusable(true);

        // Set click listeners
        parkItem.setOnClickListener(v -> {
            Intent intent = new Intent(FavoriteParksActivity.this, ParkDetailActivity.class);
            intent.putExtra("park_id", parkId);
            startActivity(intent);
        });

        btnRemove.setOnClickListener(v -> removeParkFromFavorites(parkId, parkItem));

        // Add views to container
        textContainer.addView(tvParkName);
        textContainer.addView(tvParkLocation);
        textContainer.addView(tvParkType);

        parkItem.addView(textContainer);
        parkItem.addView(btnRemove);

        return parkItem;
    }

    private void updateParkView(View parkView, Park park) {
        TextView nameView = parkView.findViewWithTag("parkName");
        TextView locationView = parkView.findViewWithTag("parkLocation");
        TextView typeView = parkView.findViewWithTag("parkType");

        if (nameView != null) {
            nameView.setText(park.name != null ? park.name : "Unnamed Park");
        }
        if (locationView != null) {
            locationView.setText("Lat: " + park.latitude + ", Lng: " + park.longitude);
        }
        if (typeView != null) {
            typeView.setText(park.paid ? "Paid Parking" : "Free Parking");
        }
    }

    private void removeParkFromFavorites(String parkId, View parkView) {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null && user.favourite_parks != null) {
                    user.favourite_parks.remove(parkId);
                    userRef.setValue(user).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            favoriteParksList.removeView(parkView);
                            Toast.makeText(FavoriteParksActivity.this,
                                "Removed from favorites", Toast.LENGTH_SHORT).show();

                            if (favoriteParksList.getChildCount() == 0) {
                                showNoFavorites();
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FavoriteParksActivity.this,
                    "Error removing favorite", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showNoFavorites() {
        progressBar.setVisibility(View.GONE);
        favoriteParksList.setVisibility(View.GONE);
        layoutNoFavorites.setVisibility(View.VISIBLE);
    }

    private void showFavoriteParks() {
        progressBar.setVisibility(View.GONE);
        layoutNoFavorites.setVisibility(View.GONE);
        favoriteParksList.setVisibility(View.VISIBLE);
    }
}
