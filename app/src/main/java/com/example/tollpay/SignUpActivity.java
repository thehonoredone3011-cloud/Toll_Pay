package com.example.tollpay;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class SignUpActivity extends AppCompatActivity {

    private EditText usernameField, emailField, phoneField;
    private Button clearButton, submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        }

        // Optional: Make status bar icons dark for better visibility on light backgrounds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        // Initialize UI elements
        usernameField = findViewById(R.id.usernameField);
        emailField = findViewById(R.id.emailField);
        phoneField = findViewById(R.id.phoneField);
        clearButton = findViewById(R.id.clearButton);
        submitButton = findViewById(R.id.submitButton);

        // Set clear button action
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearFields();
            }
        });

        // Set submit button action
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitSignup();
            }
        });
    }

    // Clears all input fields
    private void clearFields() {
        usernameField.setText("");
        emailField.setText("");
        phoneField.setText("");
    }

    // Handles signup submission
    private void submitSignup() {
        String username = usernameField.getText().toString().trim();
        String email = emailField.getText().toString().trim();
        String phoneNumber = phoneField.getText().toString().trim();

        if (!isValidInput(username, email, phoneNumber)) {
            return; // Stop if any input is invalid
        }

        // Pass data to MakePasswordActivity
        navigateToPasswordActivity(username, email, phoneNumber);
    }

    // Validates user input
    private boolean isValidInput(String username, String email, String phoneNumber) {
        if (username.isEmpty() || email.isEmpty() || phoneNumber.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!isValidPhoneNumber(phoneNumber)) {
            Toast.makeText(this, "Please enter a valid 10-digit phone number", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // Checks if the phone number is valid (10 digits)
    private boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber.length() == 10 && Patterns.PHONE.matcher(phoneNumber).matches();
    }

    // Navigates to MakePasswordActivity and passes user data
    private void navigateToPasswordActivity(String username, String email, String phoneNumber) {
        Intent intent = new Intent(SignUpActivity.this, MakePasswordActivity.class);
        intent.putExtra("username", username);
        intent.putExtra("email", email);
        intent.putExtra("phone", phoneNumber);
        startActivity(intent);
        finish();
    }
}
