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

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class PaymentSuccessfullPage extends AppCompatActivity {

    private TextView collectionIdValue, vehicleValue, tollFeesValue, accountNameValue, updatedAmountValue, timestampValue;
    private Button completeButton;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_successfull);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        }

        // Optional: Make status bar icons dark for better visibility on light backgrounds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        // Initialize Firestore and Firebase Storage
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // Initialize views
        collectionIdValue = findViewById(R.id.collectionIdValue);
        vehicleValue = findViewById(R.id.vehicleValue);
        tollFeesValue = findViewById(R.id.tollFeesValue);
        accountNameValue = findViewById(R.id.accountNameValue);
        updatedAmountValue = findViewById(R.id.updatedAmountValue);
        timestampValue = findViewById(R.id.timestampValue);
        completeButton = findViewById(R.id.completeButton);

        // Get the passed data
        String selectedCollectionId = getIntent().getStringExtra("selected_collection_id");
        String selectedVehicle = getIntent().getStringExtra("selected_vehicle");
        double tollFees = getIntent().getDoubleExtra("Toll_Fees", 0.0);
        String username = getIntent().getStringExtra("accountName");
        double updatedAmount = getIntent().getDoubleExtra("updatedTotalAmount", 0.0);
        String currentTimestamp = getIntent().getStringExtra("currentTimestamp");

        // Set the received data to the TextViews
        collectionIdValue.setText(selectedCollectionId);
        vehicleValue.setText(selectedVehicle);
        tollFeesValue.setText("₹" + tollFees);
        accountNameValue.setText(username);
        updatedAmountValue.setText("₹" + updatedAmount);
        timestampValue.setText(currentTimestamp);

        // Handle complete button click to add data to Firestore
        completeButton.setOnClickListener(v -> {
            // Add the payment data to Firestore and redirect to MainActivity
            addDataToFirestoreAndNavigate(username, selectedCollectionId, selectedVehicle, tollFees, updatedAmount, currentTimestamp);
        });
    }

    // Function to add data to Firestore under Transactions collection and redirect to MainActivity
    private void addDataToFirestoreAndNavigate(String username, String selectedCollectionId, String selectedVehicle, double tollFees, double updatedAmount, String currentTimestamp) {
        // Create a map to store the data
        Map<String, Object> transactionData = new HashMap<>();
        transactionData.put("selected_collection_id", selectedCollectionId);
        transactionData.put("selected_vehicle", selectedVehicle);
        transactionData.put("Toll_Fees", tollFees);
        transactionData.put("accountName", username);
        transactionData.put("updatedTotalAmount", updatedAmount);
        transactionData.put("currentTimestamp", currentTimestamp);

        // Add the data to Firestore under the "Transactions" collection
        db.collection("Transactions")
                .add(transactionData)  // Automatically generates a new document ID
                .addOnSuccessListener(documentReference -> {
                    // Successfully added transaction data, navigate to MainActivity
                    Toast.makeText(this, "Payment Recorded Successfully!", Toast.LENGTH_SHORT).show();
                    navigateToMainActivity();  // Navigate to MainActivity
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to record transaction: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Function to navigate to MainActivity after payment completion
    private void navigateToMainActivity() {
        Intent intent = new Intent(PaymentSuccessfullPage.this, MainActivity.class);
        intent.putExtra("username", accountNameValue.getText().toString()); // Pass the username as an extra
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Clear the activity stack
        startActivity(intent);
        finish(); // Finish the current activity to prevent the user from returning to it
    }
}
