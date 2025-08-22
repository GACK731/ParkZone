package com.s23010344.parkzone;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ParkDetailActivity extends AppCompatActivity {

    private TextView parkNameText, parkLocationText, parkTypeText, parkVehicleTypesText;
    private ImageView btnBack, btnFavourite;
    private MaterialButton btnShowDirection;
    private Park currentPark;
    private SessionManager sessionManager;
    private DatabaseReference userRef;
    private String currentParkId;
    private boolean isFavourite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_park_detail);

        // Initialize session manager and check login
        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Initialize Firebase reference for current user
        String userId = sessionManager.getUserId();
        userRef = FirebaseDatabase.getInstance("https://parkzone-5c7dc-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users")
                .child(userId);

        btnFavourite = findViewById(R.id.btnFavourite);

        btnFavourite.setOnClickListener(v -> {
            if (currentParkId != null) {
                toggleFavorite();
            }
        });

        // Initialize views
        parkNameText = findViewById(R.id.park_name);
        parkLocationText = findViewById(R.id.park_location);
        parkTypeText = findViewById(R.id.park_type);
        parkVehicleTypesText = findViewById(R.id.park_vehicle_types);
        btnBack = findViewById(R.id.btnBack);
        btnShowDirection = findViewById(R.id.btnShowDirection);

        // Back button functionality
        btnBack.setOnClickListener(v -> finish());

        // Show direction button functionality
        btnShowDirection.setOnClickListener(v -> {
            if (currentPark != null) {
                Uri gmmIntentUri = Uri.parse("google.navigation:q="
                        + currentPark.latitude + "," + currentPark.longitude);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");

                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                } else {
                    Toast.makeText(this, "Google Maps not installed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Get park ID from intent
        String parkId = getIntent().getStringExtra("park_id");

        if (parkId != null && !parkId.isEmpty()) {
            loadParkDetails(parkId);
        } else {
            Toast.makeText(this, "Invalid park ID", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void toggleFavorite() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);

                if (user == null) {
                    // Create new user data if it doesn't exist
                    user = new User();
                    user.email = sessionManager.getUserEmail();
                    user.username = extractUsernameFromEmail(user.email);
                    user.favourite_parks = new ArrayList<>();
                }

                // Ensure favourite_parks list exists
                if (user.favourite_parks == null) {
                    user.favourite_parks = new ArrayList<>();
                }

                if (user.favourite_parks.contains(currentParkId)) {
                    // Remove from favorites
                    user.favourite_parks.remove(currentParkId);
                    btnFavourite.setImageResource(R.drawable.favourite_icon_stroke);
                    isFavourite = false;
                    Toast.makeText(ParkDetailActivity.this, "Removed from favorites", Toast.LENGTH_SHORT).show();
                } else {
                    // Add to favorites
                    user.favourite_parks.add(currentParkId);
                    btnFavourite.setImageResource(R.drawable.favourite_icon);
                    isFavourite = true;
                    Toast.makeText(ParkDetailActivity.this, "Added to favorites", Toast.LENGTH_SHORT).show();
                }

                // Save updated user data to Firebase
                userRef.setValue(user);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ParkDetailActivity.this, "Error updating favorites: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String extractUsernameFromEmail(String email) {
        if (email != null && email.contains("@")) {
            return email.substring(0, email.indexOf("@"));
        }
        return "Unknown";
    }

    private void checkIfFavorite() {
        if (currentParkId == null) return;

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null && user.favourite_parks != null &&
                        user.favourite_parks.contains(currentParkId)) {
                    btnFavourite.setImageResource(R.drawable.favourite_icon);
                    isFavourite = true;
                } else {
                    btnFavourite.setImageResource(R.drawable.favourite_icon_stroke);
                    isFavourite = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error silently for favorite check
            }
        });
    }

    private void loadParkDetails(String parkId) {
        currentParkId = parkId;
        DatabaseReference ref = FirebaseDatabase.getInstance("https://parkzone-5c7dc-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("Parks")
                .child(parkId);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Park park = snapshot.getValue(Park.class);
                if (park != null) {
                    currentPark = park;

                    parkNameText.setText(park.name != null ? park.name : "Unnamed Park");
                    parkLocationText.setText("Lat: " + park.latitude + ", Lng: " + park.longitude);
                    parkTypeText.setText(park.paid ? "Paid Parking" : "Free Parking");

                    if (park.vehicleTypes != null && !park.vehicleTypes.isEmpty()) {
                        parkVehicleTypesText.setText("Allowed: " + TextUtils.join(", ", park.vehicleTypes));
                    } else {
                        parkVehicleTypesText.setText("Allowed: Not specified");
                    }

                    // Check if this park is already favorited
                    checkIfFavorite();
                } else {
                    Toast.makeText(ParkDetailActivity.this, "Park not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ParkDetailActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
