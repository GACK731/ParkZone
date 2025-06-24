package com.s23010344.parkzone;
import android.util.Log;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private EditText txtEmail, txtPassword;
    private MaterialButton btnLogin;
    private FirebaseAuth mAuth;
    private View txtSignUp;

    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        Log.d(TAG, "onCreate called");

        mAuth = FirebaseAuth.getInstance();

        txtEmail = findViewById(R.id.txtEmail);
        txtPassword = findViewById(R.id.txtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtSignUp = findViewById(R.id.txtSignUp);

        if (btnLogin == null) {
            Log.e(TAG, "btnLogin is null — Check R.id.btnLogin in login.xml");
        } else {
            Log.d(TAG, "btnLogin found — setting click listener");
        }


        btnLogin.setOnClickListener(v -> {
            Log.d(TAG, "Login button clicked");
            loginUser();
        });

        txtSignUp.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, CreateAccountActivity.class));
            finish();
        });

    }


    private void loginUser(){
        String email = txtEmail.getText().toString().trim();
        String password = txtPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task ->{
                    if (task.isSuccessful()){
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();

                        // Go to next activity
                        startActivity(new Intent(LoginActivity.this, HomePageActivity.class));
                        finish();

                    }else {
                        Toast.makeText(LoginActivity.this, "Login failed: " +
                                task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

    }
}
