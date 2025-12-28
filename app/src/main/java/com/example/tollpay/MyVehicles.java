package com.example.tollpay;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class MyVehicles extends AppCompatActivity {

    private TextView rcBookNoValue, vehicleFastagValue, vehicleNameValue, vehicleNumberValue, vehicleOwnerValue, vehicleTypeValue, vehicleVerifiedValue;
    private FirebaseFirestore db;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_vehicle);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Bind TextViews
        rcBookNoValue = findViewById(R.id.rcBookNoValue);
        vehicleFastagValue = findViewById(R.id.vehicleFastagValue);
        vehicleNameValue = findViewById(R.id.vehicleNameValue);
        vehicleNumberValue = findViewById(R.id.vehicleNumberValue);
        vehicleOwnerValue = findViewById(R.id.vehicleOwnerValue);
        vehicleTypeValue = findViewById(R.id.vehicleTypeValue);
        vehicleVerifiedValue = findViewById(R.id.vehicleVerifiedValue);

        // Get username from intent
        Intent intent = getIntent();
        username = intent.getStringExtra("username");

        if (username != null) {
            // Fetch vehicle data
            fetchVehicleData(username);
        } else {
            Toast.makeText(this, "Username is missing.", Toast.LENGTH_SHORT).show();
        }

        // Back button functionality
        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> onBackPressed());
    }

    private void fetchVehicleData(String username) {
        db.collection("Vehicle_details")
                .whereEqualTo("Vehicle_Owner", username)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            displayVehicleData(document);
                        }
                    } else {
                        Toast.makeText(MyVehicles.this, "No vehicles found for this user.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MyVehicles.this, "Error fetching data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void displayVehicleData(DocumentSnapshot document) {
        rcBookNoValue.setText(document.getString("RC_Book_No") != null ? document.getString("RC_Book_No") : "N/A");
        vehicleFastagValue.setText(document.getBoolean("Vehicle_Fastag") != null && document.getBoolean("Vehicle_Fastag") ? "Yes" : "No");
        vehicleNameValue.setText(document.getString("Vehicle_Name") != null ? document.getString("Vehicle_Name") : "N/A");
        vehicleNumberValue.setText(document.getString("Vehicle_Number") != null ? document.getString("Vehicle_Number") : "N/A");
        vehicleOwnerValue.setText(document.getString("Vehicle_Owner") != null ? document.getString("Vehicle_Owner") : "N/A");
        vehicleTypeValue.setText(document.getString("Vehicle_Type") != null ? document.getString("Vehicle_Type") : "N/A");
        vehicleVerifiedValue.setText(document.getBoolean("Vehicle_Verified") != null && document.getBoolean("Vehicle_Verified") ? "Yes" : "No");
    }
}
