package com.example.tollpay;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class AddVehicle extends AppCompatActivity {

    private EditText rcBookField, vehicleNameField, vehicleNumberField, vehicleOwnerField;
    private CheckBox fastagCheckbox;
    private Spinner vehicleTypeSpinner;
    private Button verifyButton, submitButton, clearButton;

    private static final String VEHICLE_NUMBER_PATTERN = "^[A-Z]{2}\\d{2}-[A-Z]{2}-\\d{4}$";
    private FirebaseFirestore db;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addvehicle); // Replace with your layout name
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        }

        // Optional: Make status bar icons dark for better visibility on light backgrounds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize UI components
        rcBookField = findViewById(R.id.rcBookField);
        fastagCheckbox = findViewById(R.id.fastagCheckbox);
        vehicleNameField = findViewById(R.id.vehicleNameField);
        vehicleNumberField = findViewById(R.id.vehicleNumberField);
        vehicleOwnerField = findViewById(R.id.vehicleOwnerField);
        vehicleTypeSpinner = findViewById(R.id.vehicleTypeSpinner);
        verifyButton = findViewById(R.id.verifyButton);
        submitButton = findViewById(R.id.submitButton);
        clearButton = findViewById(R.id.clearButton);

        // Set initial visibility
        submitButton.setVisibility(View.GONE);

        // Set up listeners
        clearButton.setOnClickListener(view -> clearFields());
        verifyButton.setOnClickListener(view -> checkVehicleInDatabaseAndVerify());
        submitButton.setOnClickListener(view -> checkVehicleExistsAndAdd());
    }

    private void clearFields() {
        rcBookField.setText("");
        fastagCheckbox.setChecked(false);
        vehicleNameField.setText("");
        vehicleNumberField.setText("");
        vehicleOwnerField.setText("");
        vehicleTypeSpinner.setSelection(0); // Assuming first item is a placeholder like "Select Type"
        Log.d("AddVehicle", "All fields cleared");
    }

    // Method to check if vehicle exists in Vehicle_Database and verify details
    private void checkVehicleInDatabaseAndVerify() {
        String rcBookNumber = rcBookField.getText().toString().trim();
        String vehicleName = vehicleNameField.getText().toString().trim();
        String vehicleNumber = vehicleNumberField.getText().toString().trim();
        String vehicleOwner = vehicleOwnerField.getText().toString().trim();

        // Validate format using the pattern
        if (!Pattern.matches(VEHICLE_NUMBER_PATTERN, vehicleNumber)) {
            Toast.makeText(this, "Invalid vehicle number format. Please use format: GJ06-EU-2024", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the RC Book Number exists in the Vehicle_Database
        db.collection("Vehicle_Database").document(rcBookNumber).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            // If vehicle found in the database, verify the details
                            String dbVehicleName = documentSnapshot.getString("Vehicle_Name");
                            String dbVehicleNumber = documentSnapshot.getString("Vehicle_Number");
                            String dbVehicleOwner = documentSnapshot.getString("Vehicle_Owner");

                            // Check if the entered details match with the database values
                            if (vehicleName.equals(dbVehicleName) && vehicleNumber.equals(dbVehicleNumber) && vehicleOwner.equals(dbVehicleOwner)) {
                                Toast.makeText(AddVehicle.this, "Vehicle details verified successfully!", Toast.LENGTH_SHORT).show();
                                verifyButton.setVisibility(View.GONE);
                                submitButton.setVisibility(View.VISIBLE);
                            } else {
                                Toast.makeText(AddVehicle.this, "Vehicle details do not match database records!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(AddVehicle.this, "Vehicle not found in Vehicle_Database.", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Log.d("Firestore", "Error fetching details from Vehicle_Database", e);
                        Toast.makeText(AddVehicle.this, "Error fetching vehicle details.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkVehicleExistsAndAdd() {
        String rcBookNumber = rcBookField.getText().toString().trim();
        String vehicleNumber = vehicleNumberField.getText().toString().trim();

        // Check if RC Book Number and Vehicle Number match
        if (!rcBookNumber.equals(vehicleNumber)) {
            Toast.makeText(this, "RC Book Number and Vehicle Number must be the same.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate format using the pattern
        if (!Pattern.matches(VEHICLE_NUMBER_PATTERN, rcBookNumber)) {
            Toast.makeText(this, "Invalid format. Please use format: GJ06-EU-2024", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the vehicle is already registered
        db.collection("Vehicle_details").document(rcBookNumber).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            // Vehicle already exists
                            Toast.makeText(AddVehicle.this, "Vehicle is already registered.", Toast.LENGTH_SHORT).show();
                        } else {
                            // Vehicle does not exist, add it
                            addVehicleToFirestore(rcBookNumber);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Log.d("Firestore", "Error checking document", e);
                        Toast.makeText(AddVehicle.this, "Failed to check vehicle data.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Method to add vehicle details to Firestore
    private void addVehicleToFirestore(String rcBookNumber) {
        boolean isFastagEnabled = fastagCheckbox.isChecked();
        String vehicleName = vehicleNameField.getText().toString().trim();
        String vehicleOwner = vehicleOwnerField.getText().toString().trim();
        String vehicleType = vehicleTypeSpinner.getSelectedItem().toString();

        // Prepare data to store in Firestore
        Map<String, Object> vehicleData = new HashMap<>();
        vehicleData.put("RC_Book_No", rcBookNumber);
        vehicleData.put("Vehicle_Name", vehicleName);
        vehicleData.put("Vehicle_Number", rcBookNumber);
        vehicleData.put("Vehicle_Owner", vehicleOwner);
        vehicleData.put("Vehicle_Type", vehicleType);
        vehicleData.put("Vehicle_Fastag", isFastagEnabled);
        vehicleData.put("Vehicle_Verified", true);  // Set verification to true

        // Add data to Firestore with document ID as RC Book Number
        db.collection("Vehicle_details").document(rcBookNumber)
                .set(vehicleData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Firestore", "Vehicle data added successfully with ID: " + rcBookNumber);
                        Toast.makeText(AddVehicle.this, "Vehicle data added successfully!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Log.d("Firestore", "Error adding document", e);
                        Toast.makeText(AddVehicle.this, "Failed to add vehicle data.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
