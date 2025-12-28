package com.example.tollpay;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;

    private EditText emailField, passwordField;
    private FirebaseFirestore db;
    private ProgressBar loginProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        }

        // Optional: Make status bar icons dark for better visibility on light backgrounds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);
        loginProgressBar = findViewById(R.id.loginProgressBar); // Initialize the ProgressBar

        // Check permissions
        if (!hasPermissions()) {
            requestPermissions();
        }
    }

    private boolean hasPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.RECEIVE_SMS,
                        Manifest.permission.ACCESS_FINE_LOCATION
                },
                PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permissions denied. Can't proceed.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Handles the "Clear" button action
    public void onClearClicked(View view) {
        emailField.setText("");
        passwordField.setText("");
    }

    // Handles the "Login" button action
    public void onLoginClicked(View view) {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show the ProgressBar while checking credentials
        loginProgressBar.setVisibility(View.VISIBLE);

        // Check the credentials in the Firestore database
        checkUserCredentials(email, password);
    }

    private void checkUserCredentials(String email, String password) {
        DocumentReference userRef = db.collection("UserInfo").document(email);
        userRef.get().addOnCompleteListener(task -> {
            // Hide the ProgressBar after the operation is complete
            loginProgressBar.setVisibility(View.GONE);

            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                // User document exists, now check the password
                String storedPassword = task.getResult().getString("User_Pass");
                if (storedPassword != null && storedPassword.equals(password)) {
                    // Fetch the profile completion status
                    Boolean profileCompletion = task.getResult().getBoolean("Profile_Completion");

                    if (profileCompletion != null && profileCompletion) {
                        // If profile is complete, proceed to MainActivity
                        String username = task.getResult().getString("User_Name");
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("username", username);
                        startActivity(intent);
                    } else {
                        Toast.makeText(LoginActivity.this, "Profile Not Completed", Toast.LENGTH_SHORT).show();
                        // If profile is not complete, redirect to ProfileActivity_Page
                        Intent intent = new Intent(LoginActivity.this, ProfileActivity_Page.class);
                        intent.putExtra("email", email);
                        startActivity(intent);
                    }

                    finish(); // Close the login activity
                } else {
                    Toast.makeText(LoginActivity.this, "Incorrect password", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(LoginActivity.this, "User not registered. Please sign up.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            // Hide the ProgressBar in case of failure
            loginProgressBar.setVisibility(View.GONE);
            Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    // Handles the "Forgot Password" text click action
    public void onForgotPasswordClicked(View view) {
        // Navigate to ForgotPasswordActivity
        startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
    }

    // Handles the "Sign Up" button action
    public void onSignUpClicked(View view) {
        // Navigate to SignUpActivity
        startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
    }
}
