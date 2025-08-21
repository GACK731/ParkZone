package com.s23010344.parkzone;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.util.Log;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import android.location.Location;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ImageView;
import android.hardware.Sensor;
import android.hardware.SensorManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;


public class HomePageActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private ImageButton btnNavigationMenu;

    private String selectedVehicleType = null;
    private Boolean filterPaid = null;

    private LatLng selectedParkLocation = null;

    private MaterialButton btnNavigate;
    private EditText searchEditText;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private ShakeDitectorActivity shakeDetector;

    private Park selectedPark; // Add this field to keep track of selected park

    private SessionManager sessionManager;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize session manager
        sessionManager = new SessionManager(this);

        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            // Redirect to login activity
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.home_page);

        btnNavigationMenu = findViewById(R.id.btnNavigationMenu);
        btnNavigate = findViewById(R.id.btnNavigate);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        searchEditText = findViewById(R.id.searchEditText); // FIX: remove duplicate declaration
        ImageView searchIcon = findViewById(R.id.search_icon);


        //filters
        findViewById(R.id.bike_option).setOnClickListener(v -> {
            selectedVehicleType = "Bike";
            loadParksFromFirebase();
        });

        findViewById(R.id.car_option).setOnClickListener(v -> {
            selectedVehicleType = "Car";
            loadParksFromFirebase();
        });

        findViewById(R.id.three_wheel_option).setOnClickListener(v -> {
            selectedVehicleType = "Three-Wheel";
            loadParksFromFirebase();
        });

        findViewById(R.id.bus_option).setOnClickListener(v -> {
            selectedVehicleType = "Bus";
            loadParksFromFirebase();
        });

        findViewById(R.id.btn_paid).setOnClickListener(v -> {
            filterPaid = true;
            loadParksFromFirebase();
        });

        findViewById(R.id.btn_free).setOnClickListener(v -> {
            filterPaid = false;
            loadParksFromFirebase();
        });

        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnNavigationMenu.setOnClickListener(v -> {
            startActivity(new Intent(HomePageActivity.this, NavigationMenuActivity.class));
            finish();
        });


        MaterialButton btnNavigate = findViewById(R.id.btnNavigate);
        btnNavigate.setOnClickListener(v -> {
            if (selectedParkLocation != null) {
                Uri gmmIntentUri = Uri.parse("google.navigation:q="
                        + selectedParkLocation.latitude + "," + selectedParkLocation.longitude);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");

                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent); // Launch Google Maps
                } else {
                    Toast.makeText(this, "Google Maps not installed", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Select a park first", Toast.LENGTH_SHORT).show();
            }
        });

        //search
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            String query = searchEditText.getText().toString().trim();

            // Check if user pressed Enter
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH ||
                    actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == android.view.KeyEvent.KEYCODE_ENTER)) {

                if (query.isEmpty()) {
                    // Show all parks
                    loadParksFromFirebase();
                } else {
                    // Search parks by name
                    searchParkByName(query);
                }
                return true;
            }
            return false;
        });

        //search icon
        searchIcon.setOnClickListener(v -> {
            String query = searchEditText.getText().toString();
            searchParkByName(query);
        });

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            shakeDetector = new ShakeDitectorActivity();
            shakeDetector.setOnShakeListener(count -> {
                // Handle shake event
                Intent intent = new Intent(HomePageActivity.this, FeedbackActivity.class);
                startActivity(intent);
            });
        }




    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Check permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
            //loading park from firebase
            loadParksFromFirebase();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }

        // Set custom info window adapter
        mMap.setInfoWindowAdapter(new InfoWindowAdapter() {
            @Override
            public View getInfoWindow(com.google.android.gms.maps.model.Marker marker) {
                return null; // Use default frame
            }

            @Override
            public View getInfoContents(com.google.android.gms.maps.model.Marker marker) {
                LayoutInflater inflater = LayoutInflater.from(HomePageActivity.this);
                View view = inflater.inflate(R.layout.custom_info_window, null);

                TextView title = view.findViewById(R.id.info_title);
                TextView snippet = view.findViewById(R.id.info_snippet);
                TextView showMore = view.findViewById(R.id.info_show_more);

                title.setText(marker.getTitle());
                snippet.setText(marker.getSnippet());
                showMore.setText("Show More");

                // Save park info for navigation
                selectedPark = (Park) marker.getTag();

                return view;
            }
        });

        mMap.setOnMarkerClickListener(marker -> {
            selectedParkLocation = marker.getPosition();
            selectedPark = (Park) marker.getTag();
            marker.showInfoWindow();
            return true; // Consume event
        });

        mMap.setOnInfoWindowClickListener(marker -> {
            Park park = (Park) marker.getTag();
            if (park != null) {
                Intent intent = new Intent(HomePageActivity.this, ParkDetailActivity.class);
                intent.putExtra("park_id", park.firebaseKey); // Use firebase key instead of park.id
                startActivity(intent);
            }
        });



    }

    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true); // Shows blue dot

        // Move camera to current location
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f));

                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
                grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        } else {
            // Permission denied
        }
    }


    private void loadParksFromFirebase() {
        mMap.clear(); // Clear existing markers
        DatabaseReference ref = FirebaseDatabase.getInstance("https://parkzone-5c7dc-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Parks");//data base path

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("FIREBASE", "Data received: " + snapshot.getChildrenCount());
                for (DataSnapshot parkSnapshot : snapshot.getChildren()) {
                    Park park = parkSnapshot.getValue(Park.class);
                    if (park == null) continue;

                    // Set the Firebase key
                    park.firebaseKey = parkSnapshot.getKey();

                    // Apply filters
                    if (filterPaid != null && park.paid != filterPaid) continue;
                    if (selectedVehicleType != null && !park.vehicleTypes.contains(selectedVehicleType)) continue;

                    LatLng position = new LatLng(park.latitude, park.longitude);
                    String title = park.name + " (" + (park.paid ? "Paid" : "Free") + ")";
                    String snippet = "Allowed: " + String.join(", ", park.vehicleTypes);

                    MarkerOptions options = new MarkerOptions()
                            .position(position)
                            .title(title)
                            .snippet(snippet);

                    com.google.android.gms.maps.model.Marker marker = mMap.addMarker(options);
                    marker.setTag(park); // Attach park object
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FIREBASE", "Database error: " + error.getMessage());
                // Handle error if needed
            }
        });
    }


    private void searchParkByName(String query) {
        mMap.clear();
        DatabaseReference ref = FirebaseDatabase.getInstance("https://parkzone-5c7dc-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("Parks");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean found = false;
                for (DataSnapshot parkSnapshot : snapshot.getChildren()) {
                    Park park = parkSnapshot.getValue(Park.class);
                    if (park == null) continue;

                    // Set the Firebase key
                    park.firebaseKey = parkSnapshot.getKey();

                    // Check if the park name contains the search text (case-insensitive)
                    if (park.name.toLowerCase().contains(query.toLowerCase())) {
                        LatLng position = new LatLng(park.latitude, park.longitude);
                        String title = park.name + " (" + (park.paid ? "Paid" : "Free") + ")";
                        String snippet = "Allowed: " + String.join(", ", park.vehicleTypes);

                        MarkerOptions options = new MarkerOptions()
                                .position(position)
                                .title(title)
                                .snippet(snippet);

                        com.google.android.gms.maps.model.Marker marker = mMap.addMarker(options);
                        marker.setTag(park); // Attach park object

                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 17f));
                        found = true;
                        selectedParkLocation = position;
                        break; // only focus on the first match
                    }
                }
                if (!found) {
                    Toast.makeText(HomePageActivity.this, "No matching park found", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FIREBASE", "Search error: " + error.getMessage());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sensorManager != null && accelerometer != null && shakeDetector != null) {
            sensorManager.registerListener(shakeDetector, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null && shakeDetector != null) {
            sensorManager.unregisterListener(shakeDetector);
        }
    }




}
