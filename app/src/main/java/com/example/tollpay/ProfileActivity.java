package com.example.tollpay;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private TextView aadharValue, drivingLicenseValue, genderValue, nameValue, phoneValue, verifiedValue;
    private FirebaseFirestore db;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showprofile);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Bind TextViews
        aadharValue = findViewById(R.id.aadharValue);
        drivingLicenseValue = findViewById(R.id.drivingLicenseValue);
        genderValue = findViewById(R.id.genderValue);
        nameValue = findViewById(R.id.nameValue);
        phoneValue = findViewById(R.id.phoneValue);
        verifiedValue = findViewById(R.id.verifiedValue);

        // Get username from intent
        Intent intent = getIntent();
        username = intent.getStringExtra("username");

        if (username != null) {
            // Fetch user data
            fetchUserData(username);
        } else {
            Toast.makeText(this, "Username is missing.", Toast.LENGTH_SHORT).show();
        }

        // Back button functionality
        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> onBackPressed());
    }

    private void fetchUserData(String username) {
        db.collection("User_Profile").document(username)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Safely retrieve each field, converting numeric fields as needed
                        Long aadharNumber = documentSnapshot.getLong("Aadhar_Number");
                        Long phoneNumber = documentSnapshot.getLong("Phone_Number");

                        aadharValue.setText(aadharNumber != null ? String.valueOf(aadharNumber) : "N/A");
                        drivingLicenseValue.setText(documentSnapshot.getString("DrivingLicense_Number") != null ? documentSnapshot.getString("DrivingLicense_Number") : "N/A");
                        genderValue.setText(documentSnapshot.getString("Gender") != null ? documentSnapshot.getString("Gender") : "N/A");
                        nameValue.setText(documentSnapshot.getString("Name") != null ? documentSnapshot.getString("Name") : "N/A");
                        phoneValue.setText(phoneNumber != null ? String.valueOf(phoneNumber) : "N/A");
                        verifiedValue.setText(documentSnapshot.getBoolean("Verified") != null && documentSnapshot.getBoolean("Verified") ? "Yes" : "No");
                    } else {
                        Toast.makeText(ProfileActivity.this, "User data not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ProfileActivity.this, "Error fetching data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}
