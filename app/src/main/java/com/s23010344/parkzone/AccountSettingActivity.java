package com.s23010344.parkzone;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AccountSettingActivity extends AppCompatActivity {
    private static final String TAG = "AccountSettingActivity";

    private ImageView btnBack;
    private TextInputEditText txtFirstName, txtLastName, txtEmail;
    private MaterialButton btnSave;
    private TextView tvName, tvEmail;
    private ProgressBar progressBar;

    private SessionManager sessionManager;
    private DatabaseReference userRef;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_setting);

        // Initialize session manager
        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        initializeViews();
        setupFirebaseReferences();
        loadUserData();
        setupClickListeners();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        txtFirstName = findViewById(R.id.txtFirstName);
        txtLastName = findViewById(R.id.txtLastName);
        txtEmail = findViewById(R.id.txtEmail);
        btnSave = findViewById(R.id.btnSave);
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);

        // Add progress bar if not in layout
        progressBar = new ProgressBar(this);
        progressBar.setVisibility(View.GONE);
    }

    private void setupFirebaseReferences() {
        currentUserId = sessionManager.getUserId();
        userRef = FirebaseDatabase.getInstance("https://parkzone-5c7dc-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users")
                .child(currentUserId);

        Log.d(TAG, "Firebase reference setup for user: " + currentUserId);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(AccountSettingActivity.this, NavigationMenuActivity.class);
            startActivity(intent);
            finish();
        });

        btnSave.setOnClickListener(v -> saveUserData());
    }

    private void loadUserData() {
        Log.d(TAG, "Loading user data from Firebase");
        showLoading(true);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "User data loaded successfully");
                showLoading(false);

                if (snapshot.exists()) {
                    try {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            populateUserData(user);
                        } else {
                            // Create user data structure if it doesn't exist
                            createDefaultUserData();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing user data: " + e.getMessage());
                        createDefaultUserData();
                    }
                } else {
                    Log.d(TAG, "No user data found, creating default");
                    createDefaultUserData();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load user data: " + error.getMessage());
                showLoading(false);
                Toast.makeText(AccountSettingActivity.this,
                    "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateUserData(User user) {
        Log.d(TAG, "Populating user data in UI");

        // Extract first and last name from username if they don't exist separately
        String firstName = "";
        String lastName = "";

        if (user.username != null && !user.username.isEmpty()) {
            String[] nameParts = user.username.split(" ");
            firstName = nameParts[0];
            if (nameParts.length > 1) {
                lastName = nameParts[1];
            }
        }

        // Fill the form fields
        txtFirstName.setText(firstName);
        txtLastName.setText(lastName);
        txtEmail.setText(user.email != null ? user.email : sessionManager.getUserEmail());

        // Update display info
        updateDisplayInfo(firstName, lastName, user.email);
    }

    private void createDefaultUserData() {
        Log.d(TAG, "Creating default user data");

        String userEmail = sessionManager.getUserEmail();
        String username = extractUsernameFromEmail(userEmail);

        txtEmail.setText(userEmail);
        updateDisplayInfo("", "", userEmail);

        // Create basic user structure in Firebase
        User newUser = new User();
        newUser.email = userEmail;
        newUser.username = username;

        userRef.setValue(newUser).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Default user data created successfully");
            } else {
                Log.e(TAG, "Failed to create default user data");
            }
        });
    }

    private void saveUserData() {
        Log.d(TAG, "Saving user data");

        String firstName = txtFirstName.getText().toString().trim();
        String lastName = txtLastName.getText().toString().trim();
        String email = txtEmail.getText().toString().trim();

        // Validate input
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            txtEmail.setError("Enter a valid email address");
            txtEmail.requestFocus();
            return;
        }

        showLoading(true);

        // Load existing user data and update it
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user;

                if (snapshot.exists()) {
                    user = snapshot.getValue(User.class);
                    if (user == null) {
                        user = new User();
                    }
                } else {
                    user = new User();
                }

                // Update user data
                user.email = email;
                user.username = (firstName + " " + lastName).trim();

                // Preserve existing favorite parks if they exist
                if (user.favourite_parks == null) {
                    user.favourite_parks = new java.util.ArrayList<>();
                }

                // Save to Firebase
                userRef.setValue(user).addOnCompleteListener(task -> {
                    showLoading(false);

                    if (task.isSuccessful()) {
                        Log.d(TAG, "User data saved successfully");

                        // Update session manager with new email
                        sessionManager.createLoginSession(currentUserId, email);

                        // Update display info
                        updateDisplayInfo(firstName, lastName, email);

                        Toast.makeText(AccountSettingActivity.this,
                            "Account updated successfully", Toast.LENGTH_SHORT).show();

                        // Navigate back
                        Intent intent = new Intent(AccountSettingActivity.this, NavigationMenuActivity.class);
                        startActivity(intent);
                        finish();

                    } else {
                        Log.e(TAG, "Failed to save user data: " + task.getException());
                        Toast.makeText(AccountSettingActivity.this,
                            "Failed to save changes", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showLoading(false);
                Log.e(TAG, "Database error: " + error.getMessage());
                Toast.makeText(AccountSettingActivity.this,
                    "Database error occurred", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateDisplayInfo(String firstName, String lastName, String email) {
        String fullName = (firstName + " " + lastName).trim();
        if (fullName.isEmpty()) {
            fullName = extractUsernameFromEmail(email);
        }

        tvName.setText(fullName);
        tvEmail.setText(email);
    }

    private String extractUsernameFromEmail(String email) {
        if (email != null && email.contains("@")) {
            return email.substring(0, email.indexOf("@"));
        }
        return "User";
    }

    private void showLoading(boolean show) {
        if (show) {
            btnSave.setEnabled(false);
            btnSave.setText("Saving...");
        } else {
            btnSave.setEnabled(true);
            btnSave.setText("Save");
        }
    }
}
