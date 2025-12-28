package com.example.tollpay;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText emailField, passwordField, confirmPasswordField;
    private TextView passwordLabel, confirmPasswordLabel;
    private Button verifyEmailButton, updatePasswordButton;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgotpassword);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        }

        // Optional: Make status bar icons dark for better visibility on light backgrounds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // UI Elements
        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);
        confirmPasswordField = findViewById(R.id.confirmPasswordField);
        passwordLabel = findViewById(R.id.passwordLabel);
        confirmPasswordLabel = findViewById(R.id.confirmPasswordLabel);
        verifyEmailButton = findViewById(R.id.verifyEmailButton);
        updatePasswordButton = findViewById(R.id.updatePasswordButton);
        progressBar = findViewById(R.id.progressBar);

        // Set visibility for new password fields
        setPasswordFieldsVisibility(View.GONE);

        // Set up Verify Email button click listener
        verifyEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyEmail();
            }
        });

        // Set up Update Password button click listener
        updatePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePassword();
            }
        });
    }

    private void setPasswordFieldsVisibility(int visibility) {
        passwordLabel.setVisibility(visibility);
        passwordField.setVisibility(visibility);
        confirmPasswordLabel.setVisibility(visibility);
        confirmPasswordField.setVisibility(visibility);
        updatePasswordButton.setVisibility(visibility);
    }

    private void verifyEmail() {
        String email = emailField.getText().toString().trim();

        if (email.isEmpty()) {
            emailField.setError("Please enter your email");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Query Firestore for the user's email
        db.collection("UserInfo")
                .whereEqualTo("User_Email", email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            // If the query finds a document with the email
                            Toast.makeText(ForgotPasswordActivity.this, "Email verified", Toast.LENGTH_SHORT).show();

                            // Show password fields if email matches
                            setPasswordFieldsVisibility(View.VISIBLE);
                        } else {
                            // Email was not found
                            Toast.makeText(ForgotPasswordActivity.this, "Email not found", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void updatePassword() {
        String newPassword = passwordField.getText().toString().trim();
        String confirmPassword = confirmPasswordField.getText().toString().trim();

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all password fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Fetch the document ID for the email that was verified
        db.collection("UserInfo")
                .whereEqualTo("User_Email", emailField.getText().toString().trim())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                DocumentReference docRef = document.getReference();
                                // Update the password in Firestore
                                docRef.update("User_Pass", newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        progressBar.setVisibility(View.GONE);
                                        if (task.isSuccessful()) {
                                            Toast.makeText(ForgotPasswordActivity.this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                                            // Redirect to login page
                                            Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Toast.makeText(ForgotPasswordActivity.this, "Failed to update password", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        } else {
                            Toast.makeText(ForgotPasswordActivity.this, "Email not found", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
    }
}
