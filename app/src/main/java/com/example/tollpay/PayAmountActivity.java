package com.example.tollpay;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.regex.Pattern;

public class PayAmountActivity extends AppCompatActivity {

    private EditText rcBookField, accountNameField, epinField;
    private TextView profileTitle, rcBookLabel, accountNameLabel, pinfield;
    private Button clearButton, verifyButton, nameVerifyButton, submitButton;
    private FirebaseFirestore db;

    // Declare the variables to hold the passed intent values
    private String selectedCollectionId, selectedVehicle, tollId, tollLocation;
    private int vehicleId;
    private double tollFees;

    // Flag to track whether RC book has been verified
    private boolean isRcBookVerified = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        }

        // Optional: Make status bar icons dark for better visibility on light backgrounds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        // Initialize views
        profileTitle = findViewById(R.id.profileTitle);
        pinfield = findViewById(R.id.pinField);
        rcBookField = findViewById(R.id.rcBookField);
        accountNameField = findViewById(R.id.accountNameField);
        accountNameLabel = findViewById(R.id.accountNameLabel);
        epinField = findViewById(R.id.EpinField);
        clearButton = findViewById(R.id.clearButton);
        verifyButton = findViewById(R.id.verifyButton);
        nameVerifyButton = findViewById(R.id.NameVerifyButton);
        submitButton = findViewById(R.id.submitButton);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Fetch the intent values
        Intent intent = getIntent();
        selectedCollectionId = intent.getStringExtra("selected_collection_id");
        selectedVehicle = intent.getStringExtra("selected_vehicle");
        tollFees = intent.getDoubleExtra("Toll_Fees", 0.0);
        tollId = intent.getStringExtra("Toll_Id");
        tollLocation = intent.getStringExtra("Toll_Location");
        vehicleId = intent.getIntExtra("Vehicle_Id", -1);

        // Show the values in the UI (optional)
        profileTitle.setText("Toll Fee: ₹" + tollFees);

        // Set up listeners
        verifyButton.setOnClickListener(v -> verifyRcBookNumber());
        clearButton.setOnClickListener(v -> clearFields());
        nameVerifyButton.setOnClickListener(v -> verifyEPay());
        submitButton.setOnClickListener(view -> CompletePayment());
    }

    private void CompletePayment() {
        String username = accountNameField.getText().toString();
        String pin = epinField.getText().toString().trim();

        // Check if Pin is a 4-digit number
        if (pin.length() != 4 || !pin.matches("\\d{4}")) {
            Toast.makeText(this, "Invalid Pin, must be a 4-digit number", Toast.LENGTH_SHORT).show();
            return;
        }

        // Fetch the account from Firestore
        db.collection("E_Pay").document(username).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Check if the Pin matches
                        String storedPin = documentSnapshot.getString("PIN"); // Assuming pin is stored under 'pin' field

                        if (storedPin != null && storedPin.equals(pin)) {
                            // Perform subtraction if Total_Amount > 1000
                            Double totalAmount = documentSnapshot.getDouble("Total_Amount"); // Assuming 'Total_Amount' is a field in the document
                            if (totalAmount != null && totalAmount > 1000) {
                                double updatedAmount = totalAmount - tollFees;

                                // Update Firestore with the new Total_Amount
                                documentSnapshot.getReference().update("Total_Amount", updatedAmount)
                                        .addOnSuccessListener(aVoid -> {
                                            // Get current timestamp
                                            String currentTimestamp = String.valueOf(System.currentTimeMillis());

                                            // Pass the data to the success page
                                            Intent intent = new Intent(PayAmountActivity.this, PaymentSuccessfullPage.class);
                                            intent.putExtra("selected_collection_id", selectedCollectionId);
                                            intent.putExtra("selected_vehicle", selectedVehicle);
                                            intent.putExtra("Toll_Fees", tollFees);
                                            intent.putExtra("Toll_Id", tollId);
                                            intent.putExtra("Toll_Location", tollLocation);
                                            intent.putExtra("Vehicle_Id", vehicleId);
                                            intent.putExtra("updatedTotalAmount", updatedAmount); // pass updated total amount
                                            intent.putExtra("accountName", username); // pass account name
                                            intent.putExtra("currentTimestamp", currentTimestamp); // pass current timestamp
                                            startActivity(intent);
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(this, "Error updating total amount", Toast.LENGTH_SHORT).show();
                                        });
                            } else {
                                Toast.makeText(this, "Total Amount is less than 1000", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "Invalid Pin", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Account not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching account data", Toast.LENGTH_SHORT).show();
                });
    }


    private void verifyEPay() {
        String username = accountNameField.getText().toString();
        db.collection("E_Pay").document(username).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Toast.makeText(this, "E_Pay is verified", Toast.LENGTH_SHORT).show();

                        // Make account fields visible and verifyButton gone
                        pinfield.setVisibility(View.VISIBLE);
                        epinField.setVisibility(View.VISIBLE);
                        submitButton.setVisibility(View.VISIBLE);
                        nameVerifyButton.setVisibility(View.GONE);
                    } else {
                        Toast.makeText(this, "RC Book Number not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching data", Toast.LENGTH_SHORT).show();
                });
    }

    // Method to verify RC Book Number format and check in Firestore
      // Declare this variable globally to track RC Book verification status

    private void verifyRcBookNumber() {
        // Check if the RC book is already verified
        if (isRcBookVerified) {
            Toast.makeText(this, "RC Book already verified", Toast.LENGTH_SHORT).show();
            return; // Exit method if already verified
        }

        String rcBookNumber = rcBookField.getText().toString().trim();

        // Regular expression for RC Book Number format GJ06-EU-2207
        String rcPattern = "^[A-Z]{2}\\d{2}-[A-Z]{2}-\\d{4}$";

        // Check if the RC Book Number is valid using the regex pattern
        if (!Pattern.matches(rcPattern, rcBookNumber)) {
            Toast.makeText(this, "Invalid RC Book Number format", Toast.LENGTH_SHORT).show();
            return; // Exit method if format is invalid
        }

        // Check in Firestore if the RC Book Number exists
        db.collection("Vehicle_details").document(rcBookNumber).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Toast.makeText(this, "RC Book Number found", Toast.LENGTH_SHORT).show();
                        String storedVehicle = documentSnapshot.getString("Vehicle_Type"); // Fetch the vehicle type from Firestore

                        // Compare the selected vehicle type with the stored vehicle type
                        if (selectedVehicle.equals(storedVehicle)) {
                            // Vehicle type matches, show the next fields
                            accountNameField.setVisibility(View.VISIBLE);
                            accountNameLabel.setVisibility(View.VISIBLE);
                            nameVerifyButton.setVisibility(View.VISIBLE);
                            verifyButton.setVisibility(View.GONE);

                            // Set RC Book verified flag to true
                            isRcBookVerified = true;
                        } else {
                            // If vehicle types don't match
                            Toast.makeText(this, "Vehicle Not Matched", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // If the RC book number does not exist in Firestore
                        Toast.makeText(this, "RC Book Number not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(this, "Error fetching data", Toast.LENGTH_SHORT).show();
                });
    }



    // Method to clear input fields
    private void clearFields() {
        rcBookField.setText("");
        accountNameField.setText("");
        epinField.setText("");
    }
}
