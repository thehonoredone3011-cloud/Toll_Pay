package com.example.tollpay;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MakePasswordActivity extends AppCompatActivity {

    private EditText passwordField, confirmPasswordField;
    private Button clearButton, submitButton;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        db = FirebaseFirestore.getInstance();

        // Show a toast message indicating that Firestore is connected
        Toast.makeText(this, "Database connected", Toast.LENGTH_SHORT).show();

        // Initialize UI elements
        passwordField = findViewById(R.id.passwordField);
        confirmPasswordField = findViewById(R.id.confirmPasswordField);
        clearButton = findViewById(R.id.clearButton);
        submitButton = findViewById(R.id.submitButton);

        // Set button actions
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearFields();
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitData();
            }
        });

        // Retrieve user data from intent
        Intent intent = getIntent();
        String username = intent.getStringExtra("username");
        String email = intent.getStringExtra("email");
        String phone = intent.getStringExtra("phone"); // Get phone as a String

        // Log user data for debugging
        Log.d("MakePasswordActivity", "Username: " + username + ", Email: " + email + ", Phone: " + phone);
    }

    // Clear input fields
    private void clearFields() {
        passwordField.setText("");
        confirmPasswordField.setText("");
    }

    // Submit user data to Firestore
    private void submitData() {
        String password = passwordField.getText().toString().trim();
        String confirmPassword = confirmPasswordField.getText().toString().trim();

        // Validate passwords
        if (password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prepare user data
        Intent intent = getIntent();
        String username = intent.getStringExtra("username");
        String email = intent.getStringExtra("email");
        String phone = intent.getStringExtra("phone"); // Get phone as a String
        Boolean profileCompletion = false;

        // Check if user already exists
        if (isNetworkAvailable()) {
            db.collection("UserInfo").document(email)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // User already exists
                                Toast.makeText(MakePasswordActivity.this, "User is already registered. Please log in.", Toast.LENGTH_SHORT).show();
                                navigateToLogin();
                            } else {
                                // Create new user
                                User user = new User(username, email, password, phone, profileCompletion);
                                db.collection("UserInfo").document(email)
                                        .set(user)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(MakePasswordActivity.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                                            navigateToLogin();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(MakePasswordActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            }
                        } else {
                            Toast.makeText(MakePasswordActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    // User class for Firestore
    public static class User {
        public String User_Email;
        public String User_Name;
        public String User_Pass;
        public String User_Phone;
        public Boolean Profile_Completion;

        public User() {
            // Public no-argument constructor required for Firestore serialization
        }

        public User(String username, String email, String password, String phone, Boolean profileCompletion) {
            this.User_Name = username;
            this.User_Email = email;
            this.User_Pass = password;
            this.User_Phone = phone; // Store phone as a String
            this.Profile_Completion = profileCompletion;
        }
    }

    // Check if network is available
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    // Navigate to LoginActivity
    private void navigateToLogin() {
        Intent intent = new Intent(MakePasswordActivity.this, LoginActivity.class);
        startActivity(intent);
        finish(); // Close the current activity
    }
}
