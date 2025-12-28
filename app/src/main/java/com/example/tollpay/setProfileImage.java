package com.example.tollpay;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;
public class setProfileImage extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView profileImageView;
    private Button uploadImageButton, saveProfileButton, EPayButton;
    private ProgressBar uploadProgressBar;
    private Uri imageUri;

    private FirebaseFirestore db;
    private String name, phone, aadhar, drivingLicense, gender, email; // Add email variable

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_image);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize views
        profileImageView = findViewById(R.id.profileImageView);
        uploadImageButton = findViewById(R.id.uploadImageButton);
        saveProfileButton = findViewById(R.id.saveProfileButton);
        EPayButton = findViewById(R.id.EPayButton);
        uploadProgressBar = findViewById(R.id.uploadProgressBar);

        // Retrieve intent data
        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        phone = intent.getStringExtra("phone");
        aadhar = intent.getStringExtra("aadhar");
        drivingLicense = intent.getStringExtra("drivingLicense");
        gender = intent.getStringExtra("gender");
        email = intent.getStringExtra("email"); // Retrieve email from intent

        // Set up button click listeners
        uploadImageButton.setOnClickListener(v -> openImageChooser());

        saveProfileButton.setOnClickListener(v -> saveProfileData());
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profileImageView.setImageURI(imageUri);
        }
    }

    private void saveProfileData() {
        if (imageUri != null) {
            uploadProgressBar.setVisibility(View.VISIBLE);

            // Prepare data to store in Firestore
            Map<String, Object> userData = new HashMap<>();
            userData.put("Name", name);
            userData.put("Phone_Number", Long.parseLong(phone));
            userData.put("Aadhar_Number", Long.parseLong(aadhar));
            userData.put("DrivingLicense_Number", drivingLicense);
            userData.put("Gender", gender);
            userData.put("Profile_Photo", imageUri.toString());
            userData.put("Verified", true);

            // Add user data to Firestore in the "User_Profile" collection
            db.collection("User_Profile")
                    .document(name)
                    .set(userData, SetOptions.merge())
                    .addOnCompleteListener(task -> {
                        uploadProgressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Toast.makeText(setProfileImage.this, "Profile saved successfully!", Toast.LENGTH_SHORT).show();
                            checkAndSetProfileCompletion();
                        } else {
                            Toast.makeText(setProfileImage.this, "Failed to save profile: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show();
        }
    }

    // Check if email exists in UserInfo collection and update Profile_Completion
    private void checkAndSetProfileCompletion() {
        db.collection("UserInfo").document(email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        // If document with email ID exists, update Profile_Completion field
                        Map<String, Object> update = new HashMap<>();
                        update.put("Profile_Completion", true);

                        db.collection("UserInfo").document(email)
                                .set(update, SetOptions.merge())
                                .addOnCompleteListener(updateTask -> {
                                    if (updateTask.isSuccessful()) {
                                        Toast.makeText(setProfileImage.this, "Profile Completion updated!", Toast.LENGTH_SHORT).show();
                                        uploadImageButton.setVisibility(View.GONE);
                                        saveProfileButton.setVisibility(View.GONE);
                                        EPayButton.setVisibility(View.VISIBLE);
                                        EPayButton.setOnClickListener(view -> {
                                            Intent intent = new Intent(setProfileImage.this, LoginActivity.class);
                                            startActivity(intent);
                                        });
                                    } else {
                                        Toast.makeText(setProfileImage.this, "Failed to update Profile Completion: " + updateTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                    } else {
                        Toast.makeText(setProfileImage.this, "Email not found in UserInfo collection", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
