package com.s23010344.parkzone;
import android.util.Log;

import android.os.Bundle;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.appcompat.app.AppCompatActivity;

public class CreateAccountActivity extends AppCompatActivity {
    private EditText txtEmail, txtUsername, txtPassword, txtRePassword;
    private CheckBox checkBoxTerms;
    private MaterialButton btnCreateAccount;
    private TextView txtLogin;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.creat_account);

        mAuth = FirebaseAuth.getInstance();

        txtEmail = findViewById(R.id.txtEmail);
        txtUsername = findViewById(R.id.txtUsername);
        txtPassword = findViewById(R.id.txtPassword);
        txtRePassword = findViewById(R.id.txtRePassword);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        checkBoxTerms = findViewById(R.id.checkBoxTerms);
        txtLogin = findViewById(R.id.txtLogin);

        btnCreateAccount.setOnClickListener(v -> registerUser());

        // Add click listener for login navigation
        txtLogin.setOnClickListener(v -> {
            Intent intent = new Intent(CreateAccountActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Optional: finish current activity so user can't go back with back button
        });
    }

    private void registerUser(){
        String email = txtEmail.getText().toString().trim();
        String password = txtPassword.getText().toString().trim();
        String confirmPassword = txtRePassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Email and password are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!checkBoxTerms.isChecked()) {
            Toast.makeText(this, "You must agree to the terms", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    Log.d("Email", "Email is: " + email);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show();
                        // Redirect to main or login page
                        startActivity(new Intent(CreateAccountActivity.this, HomePageActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to register: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }


}