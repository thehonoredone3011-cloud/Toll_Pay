package com.example.tollpay;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class PayTollActivity extends AppCompatActivity {

    private Spinner tollPlazaSpinner, vehicleSpinner;
    private TextView tollFeeDisplay;
    private Button proceedButton;

    private FirebaseFirestore db;
    private List<String> tollPlazaList = new ArrayList<>();
    private List<String> vehicleList = new ArrayList<>();
    private List<String> vehicleDocIdList = new ArrayList<>();
    private double tollFee;
    private String tollPlazaId, selectedVehicleId, tollId, tollLocation;
    private int vehicleId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_toll);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        }

        // Optional: Make status bar icons dark for better visibility on light backgrounds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        // UI References
        tollPlazaSpinner = findViewById(R.id.tollPlazaSpinner);
        vehicleSpinner = findViewById(R.id.vehicleSpinner);
        tollFeeDisplay = findViewById(R.id.tollFeeDisplay);
        proceedButton = findViewById(R.id.proceedButton);

        loadTollPlazaData();

        tollPlazaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != AdapterView.INVALID_POSITION) {
                    tollPlazaId = tollPlazaList.get(position);
                    loadVehicleData(tollPlazaId);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                tollPlazaId = null;
            }
        });

        vehicleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != AdapterView.INVALID_POSITION && !vehicleDocIdList.isEmpty()) {
                    selectedVehicleId = vehicleDocIdList.get(position);
                    loadTollFeeAndDetails(selectedVehicleId);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedVehicleId = null;
            }
        });

        proceedButton.setOnClickListener(v -> {
            if (tollFee > 0 && tollPlazaId != null && selectedVehicleId != null) {
                Intent intent = new Intent(PayTollActivity.this, PayAmountActivity.class);
                intent.putExtra("selected_collection_id", tollPlazaId);
                intent.putExtra("selected_vehicle", selectedVehicleId);
                intent.putExtra("Toll_Fees", tollFee);
                intent.putExtra("Toll_Id", tollId);
                intent.putExtra("Toll_Location", tollLocation);
                intent.putExtra("Vehicle_Id", vehicleId);

                startActivity(intent);
            } else {
                Toast.makeText(PayTollActivity.this, "Please select a vehicle and toll plaza to continue.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadTollPlazaData() {
        db.collection("Toll_Data")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        tollPlazaList.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            tollPlazaList.add(document.getId());
                        }
                        ArrayAdapter<String> tollPlazaAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tollPlazaList);
                        tollPlazaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        tollPlazaSpinner.setAdapter(tollPlazaAdapter);
                    } else {
                        Toast.makeText(this, "Failed to load toll plazas", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadVehicleData(String tollPlazaId) {
        db.collection("Toll_Data")
                .document(tollPlazaId)
                .collection("Toll_Amount")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        vehicleList.clear();
                        vehicleDocIdList.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            vehicleList.add(document.getId());
                            vehicleDocIdList.add(document.getId());
                        }
                        ArrayAdapter<String> vehicleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, vehicleList);
                        vehicleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        vehicleSpinner.setAdapter(vehicleAdapter);
                    } else {
                        Toast.makeText(this, "Failed to load vehicles", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadTollFeeAndDetails(String vehicleId) {
        if (vehicleId == null || tollPlazaId == null) {
            Toast.makeText(this, "Please select a valid vehicle and toll plaza", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("Toll_Data")
                .document(tollPlazaId)
                .collection("Toll_Amount")
                .document(vehicleId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            tollFee = document.getDouble("Toll_Fees") != null ? document.getDouble("Toll_Fees") : 0.0;
                            tollId = document.getString("Toll_Id");
                            tollLocation = document.getString("Toll_Location");
                            if (document.getLong("Vehicle_Id") != null) {
                                document.getLong("Vehicle_Id").intValue();
                            }
                            tollFeeDisplay.setText("Total Toll Amount: ₹" + tollFee);
                        } else {
                            Toast.makeText(this, "Failed to load toll fee details", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Failed to load toll fee details", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
