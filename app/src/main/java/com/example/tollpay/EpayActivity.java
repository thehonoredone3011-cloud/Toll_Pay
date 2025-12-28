package com.example.tollpay;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EpayActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private TextView profileTitle, accountNumberLabel, accountNameLabel, bankLabel, epinLabel;
    private EditText accountNumberField, accountNameField, pinField;
    private Spinner bankSpinner;
    private Button clearButton, verifyButton, submitButton;
    private ProgressBar verificationProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_epay);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        }

        // Optional: Make status bar icons dark for better visibility on light backgrounds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        // Initialize the views
        profileTitle = findViewById(R.id.profileTitle);
        accountNumberLabel = findViewById(R.id.accountNumberLabel);
        accountNumberField = findViewById(R.id.accountNumberField);
        accountNameLabel = findViewById(R.id.accountNameLabel);
        accountNameField = findViewById(R.id.accountNameField);
        bankLabel = findViewById(R.id.bankLabel);
        bankSpinner = findViewById(R.id.bankSpinner);
        epinLabel = findViewById(R.id.epin);
        pinField = findViewById(R.id.pinfield);

        clearButton = findViewById(R.id.clearButton);
        verifyButton = findViewById(R.id.verifyButton);
        submitButton = findViewById(R.id.submitButton);
        verificationProgressBar = findViewById(R.id.verificationProgressBar);

        // Create an ArrayAdapter using the bank names array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.bank_names, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        bankSpinner.setAdapter(adapter);

        // Set up button listeners
        setUpButtonListeners();

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
    }

    private void setUpButtonListeners() {
        // Handle Clear Button Click
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearFields();
            }
        });

        // Handle Verify Button Click
        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startVerification();
            }
        });

        // Handle Submit Button Click

    }

    private void clearFields() {
        // Clear all fields
        accountNumberField.setText("");
        accountNameField.setText("");
        pinField.setText("");
        bankSpinner.setSelection(0); // Reset spinner to first item
    }

    private void startVerification() {
        // Show progress bar when verification starts
        verificationProgressBar.setVisibility(View.VISIBLE);

        String accountNumber = accountNumberField.getText().toString();
        String accountName = accountNameField.getText().toString();
        String selectedBank = bankSpinner.getSelectedItem().toString();

        // Validate inputs
        if (accountNumber.isEmpty() || accountName.isEmpty() || selectedBank.equals("Select your bank")) {
            // Show error if any field is missing
            Toast.makeText(EpayActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            verificationProgressBar.setVisibility(View.GONE);
            return;
        }

        // Verify the bank and user details
        verifyData(selectedBank, accountName, accountNumber);
    }

    private void verifyData(String selectedBank, String accountName, String accountNumber) {
        db.collection("Bank_Of_Baroda_Users").document(accountNumber)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        // Check if the bank document exists
                        if (documentSnapshot.exists()) {
                            // Get the Bank Name from the document
                            String bankName = documentSnapshot.getString("Bank_Name");
                            String accountType = documentSnapshot.getString("Account_Type");
                            String storedAccountName = documentSnapshot.getString("Account_Name");
                            String storedAccountNumber = documentSnapshot.getString("Account_Number");
                            double totalAmount = documentSnapshot.getDouble("Total_Amount");
                            // Show a success toast
                            Toast.makeText(EpayActivity.this, "Your Bank is Verified: Bank Name: " + bankName, Toast.LENGTH_SHORT).show();

                            // Hide the verify button and show the pin field and submit button
                            verifyButton.setVisibility(View.GONE);
                            epinLabel.setVisibility(View.VISIBLE);
                            pinField.setVisibility(View.VISIBLE);
                            submitButton.setVisibility(View.VISIBLE);
                            submitButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    submitForm(bankName,accountType,storedAccountName,storedAccountNumber,totalAmount);
                                }
                            });

                        } else {
                            // Show a failure toast
                            Toast.makeText(EpayActivity.this, "Bank not found", Toast.LENGTH_SHORT).show();

                            // Hide the pin field and submit button in case of failure
                            pinField.setVisibility(View.GONE);
                            submitButton.setVisibility(View.GONE);

                            // Keep the verify button visible
                            verifyButton.setVisibility(View.VISIBLE);
                        }

                        // Hide the progress bar after the operation completes
                        verificationProgressBar.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        // Handle failure (e.g., network error)
                        Toast.makeText(EpayActivity.this, "Failed to verify bank", Toast.LENGTH_SHORT).show();

                        // Hide the pin field and submit button in case of failure
                        pinField.setVisibility(View.GONE);
                        submitButton.setVisibility(View.GONE);

                        // Keep the verify button visible
                        verifyButton.setVisibility(View.VISIBLE);

                        // Hide the progress bar after the operation completes
                        verificationProgressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void submitForm(String bankName, String accountType, String storedAccountName, String storedAccountNumber, double totalAmount) {
        String accountNumber = accountNumberField.getText().toString();
        String accountName = accountNameField.getText().toString();
        String selectedBank = bankSpinner.getSelectedItem().toString();
        String pinString = pinField.getText().toString();  // Get the PIN entered by the user

        // Validate inputs
        if (accountNumber.isEmpty() || accountName.isEmpty() || selectedBank.equals("Select your bank") || pinString.isEmpty()) {
            // Show error if any field is missing
            Toast.makeText(EpayActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the PIN is exactly 4 digits long
        if (pinString.length() != 4) {
            // Show error if the PIN is not 4 digits long
            Toast.makeText(EpayActivity.this, "PIN must be 4 digits", Toast.LENGTH_SHORT).show();
            return;
        }

        // Log the data (for debugging purposes)
        Log.d("EpayActivity", "Account Number: " + accountNumber);
        Log.d("EpayActivity", "Account Name: " + accountName);
        Log.d("EpayActivity", "Selected Bank: " + selectedBank);
        Log.d("EpayActivity", "PIN: " + pinString);

        // Create a map to store the user data
        Map<String, Object> userData = new HashMap<>();
        userData.put("Bank_Name", bankName);
        userData.put("Account_Type", accountType);
        userData.put("Account_Name", accountName);
        userData.put("Account_Number", accountNumber);
        userData.put("Total_Amount", totalAmount);
        userData.put("PIN", pinString);  // Store the PIN as part of the user data

        // Store user data into Firestore under the 'E_Pay' collection and create a document with the account name as the document ID
        db.collection("E_Pay")
                .document(accountName)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            // Account already exists
                            Toast.makeText(EpayActivity.this, "User already exists", Toast.LENGTH_SHORT).show();
                            clearFields();
                            // Redirect to MainActivity
                            finish();  // Close current activity
                        } else {
                            // Account does not exist, proceed to store data
                            db.collection("E_Pay")
                                    .document(accountName)  // Use account name as document ID
                                    .set(userData)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // Data was successfully stored
                                            Toast.makeText(EpayActivity.this, "Data stored successfully for: " + accountName, Toast.LENGTH_SHORT).show();

                                            // Redirect to MainActivity
                                            clearFields();  // Close current activity
                                            finish();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(Exception e) {
                                            // Handle failure to store data
                                            Toast.makeText(EpayActivity.this, "Failed to store data", Toast.LENGTH_SHORT).show();
                                            Log.e("EpayActivity", "Error storing data: ", e);
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        // Handle failure to retrieve document
                        Toast.makeText(EpayActivity.this, "Failed to check user existence", Toast.LENGTH_SHORT).show();
                        Log.e("EpayActivity", "Error checking user existence: ", e);
                    }
                });
    }


}
