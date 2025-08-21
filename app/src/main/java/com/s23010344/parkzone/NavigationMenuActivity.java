package com.s23010344.parkzone;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class NavigationMenuActivity extends AppCompatActivity {

    private ImageView btnBack;
    private LinearLayout btnAccountSetting, btnFavouriteParks, btnAddPark, btnFeedback, btnCountDown;
    private MaterialButton btnLogout;
    private TextView tvName, tvEmail;

    private FirebaseUser currentUser;
    private DatabaseReference userRef;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigation_menu);

        btnBack = findViewById(R.id.btnBack);
        btnAccountSetting = findViewById(R.id.btnAccountSetting);
        btnFavouriteParks = findViewById(R.id.btnFavouriteParks);
        btnAddPark = findViewById(R.id.btnAddPark);
        btnFeedback = findViewById(R.id.btnFeedback);
        btnCountDown = findViewById(R.id.btnCountDown);
        btnLogout = findViewById(R.id.btnLogout);
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);

        // Initialize session manager
        sessionManager = new SessionManager(this);

        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(NavigationMenuActivity.this, HomePageActivity.class));
            finish();
        });

        btnAccountSetting.setOnClickListener(v -> {
            startActivity(new Intent(NavigationMenuActivity.this, AccountSettingActivity.class));
            finish();
        });

        btnFavouriteParks.setOnClickListener(v -> {
            startActivity(new Intent(NavigationMenuActivity.this, FavouriteParkActivity.class));
            finish();
        });

        btnAddPark.setOnClickListener(v -> {
            startActivity(new Intent(NavigationMenuActivity.this, AddParkActivity.class));
            finish();
        });

        btnFeedback.setOnClickListener(v -> {
            startActivity(new Intent(NavigationMenuActivity.this, FeedbackActivity.class));
            finish();
        });

        btnCountDown.setOnClickListener(v -> {
            startActivity(new Intent(NavigationMenuActivity.this, CountdownActivity.class));
            finish();
        });

        // Logout functionality
        btnLogout.setOnClickListener(v -> {
            // Clear session
            sessionManager.logoutUser();

            // Redirect to login
            Intent intent = new Intent(NavigationMenuActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
