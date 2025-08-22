package com.s23010344.parkzone;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AddParkActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;

    // UI Components
    private ImageView btnBack;
    private CardView cardUploadImages;
    private CardView cardAddLocation;
    private EditText etParkingName;
    private TextView tvLocationDetails;
    private RadioGroup radioGroupParkingType;
    private RadioButton radioFree, radioPaid;
    private TextInputLayout tilHourlyRate;
    private EditText etHourlyRate;

    // Vehicle Type Checkboxes
    private CheckBox checkboxCar, checkboxMotorcycle, checkboxVan, checkboxBus, checkboxTruck;

    // Location Data
    private double latitude;
    private double longitude;
    private String locationAddress;
    private boolean locationSelected = false;

    // Data Lists
    private List<Uri> selectedImageUris = new ArrayList<>();

    // Parking Type Data
    private boolean isPaidParking = false;
    private double hourlyRate = 0.0;

    // Activity Result Launchers
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Intent> locationPickerLauncher;

    // Firebase
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference parksRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_parking_place);

        initializeViews();
        setupActivityResultLaunchers();
        setupClickListeners();
        checkPermissions();

        // Initialize Firebase with the specific database URL
        firebaseDatabase = FirebaseDatabase.getInstance("https://parkzone-5c7dc-default-rtdb.asia-southeast1.firebasedatabase.app");
        parksRef = firebaseDatabase.getReference("Parks");
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        cardUploadImages = findViewById(R.id.cardUploadImage);
        cardAddLocation = findViewById(R.id.card_add_location);
        etParkingName = findViewById(R.id.et_parking_name);
        radioGroupParkingType = findViewById(R.id.radio_group_parking_type);
        radioFree = findViewById(R.id.radio_free);
        radioPaid = findViewById(R.id.radio_paid);
        tilHourlyRate = findViewById(R.id.til_hourly_rate);
        etHourlyRate = findViewById(R.id.et_hourly_rate);

        // Initialize Proceed button
        Button btnProceed = findViewById(R.id.btn_proceed);

        // Initialize CheckBoxes
        checkboxCar = findViewById(R.id.checkbox_car);
        checkboxMotorcycle = findViewById(R.id.checkbox_motorcycle);
        checkboxVan = findViewById(R.id.checkbox_van);
        checkboxBus = findViewById(R.id.checkbox_bus);
        checkboxTruck = findViewById(R.id.checkbox_truck);

        // Set click listener for Proceed button
        btnProceed.setOnClickListener(v -> uploadParkingData());
    }

    private void setupActivityResultLaunchers() {
        // Image picker launcher
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    if (data.getClipData() != null) {
                        // Multiple images selected
                        int count = data.getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            Uri imageUri = data.getClipData().getItemAt(i).getUri();
                            selectedImageUris.add(imageUri);
                        }
                    } else if (data.getData() != null) {
                        // Single image selected
                        selectedImageUris.add(data.getData());
                    }
                    updateImageUI();
                }
            }
        );

        // Location picker launcher - simplified approach
        locationPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Log.d("AddParkActivity", "Location picker result code: " + result.getResultCode());

                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        try {
                            // Get location data with safe defaults
                            latitude = data.getDoubleExtra(MapLocationPickerActivity.EXTRA_LATITUDE, 0.0);
                            longitude = data.getDoubleExtra(MapLocationPickerActivity.EXTRA_LONGITUDE, 0.0);
                            locationAddress = data.getStringExtra(MapLocationPickerActivity.EXTRA_ADDRESS);

                            if (locationAddress == null) {
                                locationAddress = "Location selected";
                            }

                            Log.d("AddParkActivity", "Location received: " + latitude + ", " + longitude);

                            // Simple validation
                            if (latitude != 0.0 || longitude != 0.0) {
                                locationSelected = true;
                                updateLocationUI();
                                Toast.makeText(AddParkActivity.this, "Location selected successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(AddParkActivity.this, "Invalid location. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Log.e("AddParkActivity", "Error processing location", e);
                            Toast.makeText(AddParkActivity.this, "Error processing location", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Log.d("AddParkActivity", "Location selection cancelled");
                }
            }
        );
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(AddParkActivity.this, NavigationMenuActivity.class));
            finish();
        });

        cardUploadImages.setOnClickListener(v -> openImagePicker());

        cardAddLocation.setOnClickListener(v -> openLocationPicker());

        radioGroupParkingType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_paid) {
                isPaidParking = true;
                tilHourlyRate.setVisibility(View.VISIBLE);
            } else {
                isPaidParking = false;
                tilHourlyRate.setVisibility(View.GONE);
            }
        });
    }

    private void openLocationPicker() {
        Intent intent = new Intent(this, MapLocationPickerActivity.class);
        locationPickerLauncher.launch(intent);
    }

    private void updateLocationUI() {
        // Update UI to reflect selected location
        TextView locationText = cardAddLocation.findViewById(R.id.tv_location_text);
        if (locationSelected) {
            locationText.setText("Location Selected");
        }
    }

    private void openImagePicker() {
        // Check if permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            // Request permission
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                PERMISSION_REQUEST_CODE);
            return;
        }

        // Create intent to pick images
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        imagePickerLauncher.launch(Intent.createChooser(intent, "Select Images"));
    }

    private void updateImageUI() {
        int count = selectedImageUris.size();
        if (count > 0) {
            Toast.makeText(this, count + " images selected", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No images selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted! You can now select images.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied. Cannot select images.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void uploadParkingData() {
        String parkingName = etParkingName.getText().toString().trim();
        String hourlyRateStr = etHourlyRate.getText().toString().trim();

        // Validate inputs
        if (parkingName.isEmpty()) {
            Toast.makeText(this, "Please enter parking name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!locationSelected) {
            Toast.makeText(this, "Please select a location", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isPaidParking && hourlyRateStr.isEmpty()) {
            Toast.makeText(this, "Please enter hourly rate", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if at least one vehicle type is selected
        if (!checkboxCar.isChecked() && !checkboxMotorcycle.isChecked() &&
            !checkboxVan.isChecked() && !checkboxBus.isChecked() &&
            !checkboxTruck.isChecked()) {
            Toast.makeText(this, "Please select at least one vehicle type", Toast.LENGTH_SHORT).show();
            return;
        }

        hourlyRate = isPaidParking ? Double.parseDouble(hourlyRateStr) : 0.0;

        // Show progress dialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading parking data...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // First, sign in anonymously to satisfy Firebase security rules
        FirebaseAuth auth = FirebaseAuth.getInstance();

        // Check if already signed in
        if (auth.getCurrentUser() != null) {
            // Already authenticated, proceed with upload
            proceedWithUpload(parkingName, hourlyRate, progressDialog);
        } else {
            // Need to authenticate first
            auth.signInAnonymously()
                .addOnSuccessListener(authResult -> {
                    // Authentication successful, now upload data
                    Toast.makeText(AddParkActivity.this, "Authentication successful", Toast.LENGTH_SHORT).show();
                    proceedWithUpload(parkingName, hourlyRate, progressDialog);
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    String errorMessage = "Authentication failed: " + e.getMessage();
                    Toast.makeText(AddParkActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    Log.e("AddParkActivity", "Authentication failed", e);
                });
        }
    }

    private void proceedWithUpload(String parkingName, double hourlyRate, ProgressDialog progressDialog) {
        // Check if Firebase is initialized
        if (firebaseDatabase == null) {
            firebaseDatabase = FirebaseDatabase.getInstance();
            Toast.makeText(this, "Reinitializing Firebase", Toast.LENGTH_SHORT).show();
        }

        // Make sure we're using the correct database path structure
        if (parksRef == null) {
            parksRef = firebaseDatabase.getReference("Parks");
            Toast.makeText(this, "Reinitializing database reference", Toast.LENGTH_SHORT).show();
        }

        // Enable Firebase database persistence to handle network issues
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        } catch (Exception e) {
            // This might throw if persistence is already enabled
        }

        // Generate unique ID for the parking spot
        String parkID = UUID.randomUUID().toString();

        // Log the upload attempt
        Log.d("AddParkActivity", "Uploading to path: " + parksRef.child(parkID).toString());

        // Create parking spot object
        Map<String, Object> parkDetails = new HashMap<>();
        parkDetails.put("name", parkingName);

        // Add location data
        Map<String, Object> locationData = new HashMap<>();
        locationData.put("latitude", latitude);
        locationData.put("longitude", longitude);
        locationData.put("address", locationAddress);
        parkDetails.put("location", locationData);

        // Add parking type data
        parkDetails.put("isFree", !isPaidParking);
        if (isPaidParking) {
            parkDetails.put("hourlyRate", hourlyRate);
        }

        // Add vehicle types
        Map<String, Boolean> vehicleTypes = new HashMap<>();
        vehicleTypes.put("car", checkboxCar.isChecked());
        vehicleTypes.put("motorcycle", checkboxMotorcycle.isChecked());
        vehicleTypes.put("van", checkboxVan.isChecked());
        vehicleTypes.put("bus", checkboxBus.isChecked());
        vehicleTypes.put("truck", checkboxTruck.isChecked());
        parkDetails.put("availableTypes", vehicleTypes);

        // Add user info
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            parkDetails.put("userId", currentUser.getUid());
        }

        // Add timestamp
        parkDetails.put("createdAt", System.currentTimeMillis());

        // Create timeout handler
        android.os.Handler timeoutHandler = new android.os.Handler();
        final boolean[] uploadCompleted = {false};

        // Set timeout for 3 seconds
        Runnable timeoutRunnable = () -> {
            if (!uploadCompleted[0]) {
                progressDialog.dismiss();
                Toast.makeText(AddParkActivity.this, "Upload timeout after 3 seconds. Please check your internet connection.", Toast.LENGTH_LONG).show();
                Log.w("AddParkActivity", "Upload timed out after 3 seconds");
            }
        };
        timeoutHandler.postDelayed(timeoutRunnable, 3000); // 3 seconds timeout

        // Create a direct reference to ensure path is created correctly
        DatabaseReference exactRef = FirebaseDatabase.getInstance().getReference();
        Map<String, Object> updates = new HashMap<>();
        updates.put("/Parks/" + parkID, parkDetails);

        // Use updateChildren to ensure the path is created
        exactRef.updateChildren(updates)
            .addOnSuccessListener(aVoid -> {
                if (!uploadCompleted[0]) {
                    uploadCompleted[0] = true;
                    timeoutHandler.removeCallbacks(timeoutRunnable); // Cancel timeout
                    progressDialog.dismiss();
                    Toast.makeText(AddParkActivity.this, "Parking data uploaded successfully", Toast.LENGTH_SHORT).show();
                    Log.d("AddParkActivity", "Upload successful for park: " + parkingName);
                    clearInputs();
                    // Return to previous screen or home
                    startActivity(new Intent(AddParkActivity.this, NavigationMenuActivity.class));
                    finish();
                }
            })
            .addOnFailureListener(e -> {
                if (!uploadCompleted[0]) {
                    uploadCompleted[0] = true;
                    timeoutHandler.removeCallbacks(timeoutRunnable); // Cancel timeout
                    progressDialog.dismiss();
                    // More detailed error message
                    String errorMessage = "Failed to upload data: " + e.getMessage();
                    Toast.makeText(AddParkActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    Log.e("AddParkActivity", "Upload failed", e);
                    // Try alternative approach with timeout
                    tryPublicAccessUploadWithTimeout(parkID, parkDetails);
                }
            });
    }

    private void tryPublicAccessUploadWithTimeout(String parkID, Map<String, Object> parkDetails) {
        Toast.makeText(this, "Trying alternative upload method...", Toast.LENGTH_SHORT).show();

        // Show new progress dialog for fallback attempt
        ProgressDialog fallbackProgressDialog = new ProgressDialog(this);
        fallbackProgressDialog.setMessage("Trying alternative upload...");
        fallbackProgressDialog.setCancelable(false);
        fallbackProgressDialog.show();

        // Create timeout handler for fallback
        android.os.Handler fallbackTimeoutHandler = new android.os.Handler();
        final boolean[] fallbackCompleted = {false};

        // Set timeout for 3 seconds for fallback attempt
        Runnable fallbackTimeoutRunnable = () -> {
            if (!fallbackCompleted[0]) {
                fallbackProgressDialog.dismiss();
                Toast.makeText(AddParkActivity.this, "Upload failed after multiple attempts. Please try again later.", Toast.LENGTH_LONG).show();
                Log.w("AddParkActivity", "Fallback upload also timed out after 3 seconds");
            }
        };
        fallbackTimeoutHandler.postDelayed(fallbackTimeoutRunnable, 3000); // 3 seconds timeout

        // Create a direct public reference without using authentication
        DatabaseReference publicRef = FirebaseDatabase.getInstance().getReference();

        // Allow public access for this specific write operation
        DatabaseReference.goOnline();

        publicRef.child("Parks").child(parkID).setValue(parkDetails)
            .addOnSuccessListener(aVoid -> {
                if (!fallbackCompleted[0]) {
                    fallbackCompleted[0] = true;
                    fallbackTimeoutHandler.removeCallbacks(fallbackTimeoutRunnable); // Cancel timeout
                    fallbackProgressDialog.dismiss();
                    Toast.makeText(this, "Upload successful via alternative method", Toast.LENGTH_SHORT).show();
                    Log.d("AddParkActivity", "Fallback upload successful");
                    clearInputs();
                    startActivity(new Intent(AddParkActivity.this, NavigationMenuActivity.class));
                    finish();
                }
            })
            .addOnFailureListener(e -> {
                if (!fallbackCompleted[0]) {
                    fallbackCompleted[0] = true;
                    fallbackTimeoutHandler.removeCallbacks(fallbackTimeoutRunnable); // Cancel timeout
                    fallbackProgressDialog.dismiss();
                    String errorMessage = "All upload methods failed: " + e.getMessage();
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                    Log.e("AddParkActivity", "All upload methods failed", e);
                }
            });
    }

    private void clearInputs() {
        etParkingName.setText("");
        etHourlyRate.setText("");
        radioGroupParkingType.clearCheck();
        checkboxCar.setChecked(false);
        checkboxMotorcycle.setChecked(false);
        checkboxVan.setChecked(false);
        checkboxBus.setChecked(false);
        checkboxTruck.setChecked(false);
        selectedImageUris.clear();
        locationSelected = false;
        updateLocationUI();
        updateImageUI();
    }
}