package com.example.tollpay;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.core.view.GravityCompat;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_home);

        // Set the status bar color and icons visibility for Lollipop and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        // Initialize DrawerLayout and other UI components
        drawerLayout = findViewById(R.id.drawer_layout);
        ImageView navigationIcon = findViewById(R.id.navigation_icon);
        TextView myProfile = findViewById(R.id.my_profile);
        TextView changePayment = findViewById(R.id.change_payment);
        TextView termsConditions = findViewById(R.id.terms_conditions);
        TextView aboutUs = findViewById(R.id.about_us);
        TextView logout = findViewById(R.id.logout);
        ImageView icon1 = findViewById(R.id.icon1);
        ImageView icon2 = findViewById(R.id.icon2);
        ImageView icon3 = findViewById(R.id.icon3);
        ImageView icon4 = findViewById(R.id.icon4);
        TextView usernameText = findViewById(R.id.username_text);

        // Get the username from the intent
        Intent intent = getIntent();
        username = intent.getStringExtra("username");

        // Check if username is null and display a message or set it to the TextView
        if (username == null) {
            Toast.makeText(MainActivity.this, "User Name not Found", Toast.LENGTH_SHORT).show();
        } else {
            usernameText.setText("Welcome, " + username);
        }

        // Set navigation icon click listener
        navigationIcon.setOnClickListener(view -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END);
            } else {
                drawerLayout.openDrawer(GravityCompat.END);
            }
        });

        // Set click listeners for drawer items
        setClickListeners(myProfile, changePayment, termsConditions, aboutUs, logout, icon1, icon2, icon3, icon4);
    }

    private void setClickListeners(TextView myProfile, TextView changePayment, TextView termsConditions,
                                   TextView aboutUs, TextView logout,
                                   ImageView icon1, ImageView icon2, ImageView icon3, ImageView icon4) {

        // My Profile - Navigate to ProfileActivity
        myProfile.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
        });

        // Transaction History - Navigate to ShowTransactions
        changePayment.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, ShowTransactions.class);
            intent.putExtra("username", username);
            startActivity(intent);
        });

        // Terms and Conditions
        termsConditions.setOnClickListener(view -> showToast("Terms and Conditions Clicked"));

        // About Us
        aboutUs.setOnClickListener(view -> showToast("About Us Clicked"));

        // Log Out - Quit the app
        logout.setOnClickListener(view -> {
            showToast("Log Out Clicked");
            finishAffinity(); // Close all activities and quit the app
        });

        // Icon1 - Navigate to PayTollActivity
        icon1.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, PayTollActivity.class);
            startActivity(intent);
        });

        // Icon2 - Navigate to EpayActivity and pass username
        icon2.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, EpayActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
        });

        // Icon3 - Navigate to AddVehicle
        icon3.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AddVehicle.class);
            startActivity(intent);
        });

        // Icon4 - Navigate to MyVehicles and pass username
        icon4.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, MyVehicles.class);
            intent.putExtra("username", username);
            startActivity(intent);
        });
    }

    // Helper method to show Toast messages
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
