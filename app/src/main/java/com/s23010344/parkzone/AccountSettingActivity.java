package com.s23010344.parkzone;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.appcompat.app.AppCompatActivity;

public class AccountSettingActivity extends AppCompatActivity {
    private ImageView btnBack;
    private EditText txtFirstName, txtLastName, txtEmail;
    private Button btnSave;
    private FirebaseUser currentUser;
    private DatabaseReference userRef;
    private TextView tvName, tvEmail;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_setting);

        btnBack = findViewById(R.id.btnBack);
        txtFirstName = findViewById(R.id.txtFirstName);
        txtLastName = findViewById(R.id.txtLastName);
        txtEmail = findViewById(R.id.txtEmail);
        btnSave = findViewById(R.id.btnSave);
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);


        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(AccountSettingActivity.this, NavigationMenuActivity.class));
            finish();
        });

        if (currentUser != null) {
            userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());

            // Optional: Load existing data
            userRef.get().addOnSuccessListener(dataSnapshot -> {
                if (dataSnapshot.exists()) {
                    txtFirstName.setText(dataSnapshot.child("firstName").getValue(String.class));
                    txtLastName.setText(dataSnapshot.child("lastName").getValue(String.class));
                    txtEmail.setText(dataSnapshot.child("email").getValue(String.class));
                }
            });
        }

        btnSave.setOnClickListener(v -> {
            String firstName = txtFirstName.getText().toString().trim();
            String lastName = txtLastName.getText().toString().trim();
            String email = txtEmail.getText().toString().trim();

            userRef.child("firstName").setValue(firstName);
            userRef.child("lastName").setValue(lastName);
            userRef.child("email").setValue(email);

            tvName.setText(firstName + " " + lastName);
            tvEmail.setText(email);


            // Go back to navigation menu

        });

    }

}
