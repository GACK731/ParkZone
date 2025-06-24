package com.s23010344.parkzone;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;


import androidx.appcompat.app.AppCompatActivity;

public class NavigationMenuActivity extends AppCompatActivity {

    private ImageView btnBack;
    private LinearLayout btnAccountSetting, btnFavouriteParks, btnAddPark, btnAboutUs, btnCountDown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigation_menu);

        btnBack = findViewById(R.id.btnBack);
        btnAccountSetting = findViewById(R.id.btnAccountSetting);
        btnFavouriteParks = findViewById(R.id.btnFavouriteParks);
        btnAddPark = findViewById(R.id.btnAddPark);
        btnAboutUs = findViewById(R.id.btnAboutUs);
        btnCountDown = findViewById(R.id.btnCountDown);


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

        btnAboutUs.setOnClickListener(v -> {
            startActivity(new Intent(NavigationMenuActivity.this, AboutUsActivity.class));
            finish();
        });

        btnCountDown.setOnClickListener(v -> {
            startActivity(new Intent(NavigationMenuActivity.this, CountdownActivity.class));
            finish();
        });




    }

}
