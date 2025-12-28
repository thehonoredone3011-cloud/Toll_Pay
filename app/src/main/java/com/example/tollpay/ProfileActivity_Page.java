package com.example.tollpay;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.regex.Pattern;

public class ProfileActivity_Page extends AppCompatActivity {

    private EditText nameField, phoneField, aadharField, drivingLicenseField;
    private Button clearButton, verifyButton, setUpPImageButton;
    private ProgressBar verificationProgressBar;
    private TextView helpLabel;
    private boolean verified = false;
    private FirebaseFirestore db;
    private String email; // Declare email variable here

    // Pattern for driving license format
    private static final Pattern DRIVING_LICENSE_PATTERN = Pattern.compile("^[A-Z]{2}[0-9]{2}/[0-9]{4}/[0-9]{4}$");

    // Declare the gender variable
    private String selectedGender = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        // Retrieve email from intent
        email = getIntent().getStringExtra("email");

        // Initialize UI components
        nameField = findViewById(R.id.nameField);
        phoneField = findViewById(R.id.phoneField);
        aadharField = findViewById(R.id.aadharField);
        drivingLicenseField = findViewById(R.id.drivingLicenseField);
        clearButton = findViewById(R.id.clearButton);
        verifyButton = findViewById(R.id.verifyButton);
        setUpPImageButton = findViewById(R.id.setupPImageButton);
        verificationProgressBar = findViewById(R.id.verificationProgressBar);
        helpLabel = findViewById(R.id.helpLabel);
        RadioGroup genderGroup = findViewById(R.id.genderGroup);

        // Set listener for gender selection
        genderGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // Check which radio button was clicked
                RadioButton selectedRadioButton = findViewById(checkedId);
                selectedGender = selectedRadioButton.getText().toString(); // Store the selected gender
            }
        });

        db = FirebaseFirestore.getInstance();

        // Set up listeners
        clearButton.setOnClickListener(v -> clearFields());

        verifyButton.setOnClickListener(v -> verifyUser());
    }

    private void clearFields() {
        nameField.setText("");
        phoneField.setText("");
        aadharField.setText("");
        drivingLicenseField.setText("");
        Toast.makeText(this, "Fields cleared", Toast.LENGTH_SHORT).show();
    }

    private void verifyUser() {
        final String name = nameField.getText().toString().trim();
        final String phone = phoneField.getText().toString().trim();
        final String aadhar = aadharField.getText().toString().trim();
        final String drivingLicense = drivingLicenseField.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            nameField.setError("Name is required");
            return;
        }

        if (!isValidPhone(phone)) {
            phoneField.setError("Phone number must be 10 digits");
            return;
        }

        if (!isValidAadhar(aadhar)) {
            aadharField.setError("Aadhar number must be 12 digits");
            return;
        }

        if (!isValidDrivingLicense(drivingLicense)) {
            drivingLicenseField.setError("Invalid driving license format (e.g., DL06/2024/2345)");
            return;
        }

        verificationProgressBar.setVisibility(View.VISIBLE);

        // Fetch data from Firebase Firestore collection "Aadhar_and_Driving" using the name as document ID
        db.collection("Aadhar_and_Driving").document(name)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        verificationProgressBar.setVisibility(View.GONE);

                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                String dbName = document.getString("Name");
                                Long dbPhone = document.getLong("Phone");
                                Long dbAadhar = document.getLong("Aadhar_Number");
                                String dbDrivingLicense = document.getString("DrivingLicense_Number");
                                String dbGender = document.getString("Gender"); // Fetch Gender from Firestore

                                if (name.equals(dbName) && phone.equals(String.valueOf(dbPhone)) && aadhar.equals(String.valueOf(dbAadhar)) && drivingLicense.equals(dbDrivingLicense)) {
                                    verified = true;
                                    Toast.makeText(ProfileActivity_Page.this, "User verified successfully!", Toast.LENGTH_SHORT).show();
                                    setUpPImageButton.setVisibility(View.VISIBLE);
                                    helpLabel.setVisibility(View.GONE);
                                    setUpPImageButton.setOnClickListener(view -> {
                                        // Pass user data, including email, to setProfileImage activity
                                        Intent intent = new Intent(ProfileActivity_Page.this, setProfileImage.class);
                                        intent.putExtra("name", name);
                                        intent.putExtra("phone", phone);
                                        intent.putExtra("aadhar", aadhar);
                                        intent.putExtra("drivingLicense", drivingLicense);
                                        intent.putExtra("gender", selectedGender);
                                        intent.putExtra("email", email); // Include email in intent
                                        startActivity(intent);
                                    });

                                } else {
                                    Toast.makeText(ProfileActivity_Page.this, "Verification failed: Details do not match", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(ProfileActivity_Page.this, "Verification failed: No record found", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(ProfileActivity_Page.this, "Error accessing database: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private boolean isValidPhone(String phone) {
        return phone.length() == 10 && TextUtils.isDigitsOnly(phone);
    }

    private boolean isValidAadhar(String aadhar) {
        return aadhar.length() == 12 && TextUtils.isDigitsOnly(aadhar);
    }

    private boolean isValidDrivingLicense(String drivingLicense) {
        return DRIVING_LICENSE_PATTERN.matcher(drivingLicense).matches();
    }
}
