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

public class ParkDetailActivity extends AppCompatActivity {

    private TextView parkNameText, parkLocationText, parkTypeText, parkVehicleTypesText;
    private ImageView btnBack;
    private MaterialButton btnShowDirection;
    private Park currentPark;
    private SessionManager sessionManager;

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

        ImageView btnFavourite = findViewById(R.id.btnFavourite);

// Flag to track favourite state
        final boolean[] isFavourite = {false};

        btnFavourite.setOnClickListener(v -> {
            if (isFavourite[0]) {
                // Currently favourite → set back to stroke
                btnFavourite.setImageResource(R.drawable.favourite_icon_stroke);
                isFavourite[0] = false;
            } else {
                // Currently not favourite → set to filled
                btnFavourite.setImageResource(R.drawable.favourite_icon);
                isFavourite[0] = true;
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

    private void loadParkDetails(String parkId) {
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
