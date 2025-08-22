package com.s23010344.parkzone;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapLocationPickerActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private TextView tvLocationAddress, tvLocationCoordinates;
    private Button btnConfirmLocation;
    private ImageView btnBackMap;
    private FloatingActionButton btnCurrentLocation;

    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private LatLng selectedLocation;

    public static final String EXTRA_LATITUDE = "extra_latitude";
    public static final String EXTRA_LONGITUDE = "extra_longitude";
    public static final String EXTRA_ADDRESS = "extra_address";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_location_picker);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        initializeViews();
        setupMap();
        setupClickListeners();
    }

    private void initializeViews() {
        tvLocationAddress = findViewById(R.id.tvLocationAddress);
        tvLocationCoordinates = findViewById(R.id.tvLocationCoordinates);
        btnConfirmLocation = findViewById(R.id.btnConfirmLocation);
        btnBackMap = findViewById(R.id.btnBackMap);
        btnCurrentLocation = findViewById(R.id.btnCurrentLocation);
    }

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void setupClickListeners() {
        btnBackMap.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        btnCurrentLocation.setOnClickListener(v -> getCurrentLocation());

        btnConfirmLocation.setOnClickListener(v -> confirmLocation());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Set default location (Colombo, Sri Lanka)
        LatLng defaultLocation = new LatLng(6.9271, 79.8612);
        selectedLocation = defaultLocation;

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15f));
        updateLocationInfo(defaultLocation);

        // Enable location if permission granted
        enableMyLocation();

        // Set camera move listener
        mMap.setOnCameraIdleListener(() -> {
            selectedLocation = mMap.getCameraPosition().target;
            updateLocationInfo(selectedLocation);
        });

        // Try to get current location
        getCurrentLocation();
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            selectedLocation = currentLocation;
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 18f));
                            updateLocationInfo(currentLocation);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("MapLocationPicker", "Error getting location", e);
                        Toast.makeText(this, "Error getting current location", Toast.LENGTH_SHORT).show();
                    });
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && mMap != null) {
            mMap.setMyLocationEnabled(true);
        }
    }

    private void confirmLocation() {
        if (selectedLocation != null) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra(EXTRA_LATITUDE, selectedLocation.latitude);
            resultIntent.putExtra(EXTRA_LONGITUDE, selectedLocation.longitude);
            resultIntent.putExtra(EXTRA_ADDRESS, tvLocationAddress.getText().toString());

            Log.d("MapLocationPicker", "Returning location: " + selectedLocation.latitude + ", " + selectedLocation.longitude);

            setResult(RESULT_OK, resultIntent);
            finish();
        } else {
            Toast.makeText(this, "Please select a location", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateLocationInfo(LatLng location) {
        if (location == null) return;

        // Update coordinates
        String coordinates = String.format(Locale.getDefault(), "%.6f, %.6f", location.latitude, location.longitude);
        tvLocationCoordinates.setText(coordinates);

        // Get address
        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1);

                String addressText = "Address not found";
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    StringBuilder sb = new StringBuilder();

                    for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                        if (address.getAddressLine(i) != null) {
                            sb.append(address.getAddressLine(i));
                            if (i < address.getMaxAddressLineIndex()) sb.append(", ");
                        }
                    }

                    if (sb.length() > 0) {
                        addressText = sb.toString();
                    }
                }

                final String finalAddress = addressText;
                runOnUiThread(() -> tvLocationAddress.setText(finalAddress));

            } catch (Exception e) {
                Log.e("MapLocationPicker", "Error getting address", e);
                runOnUiThread(() -> tvLocationAddress.setText("Could not get address"));
            }
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
