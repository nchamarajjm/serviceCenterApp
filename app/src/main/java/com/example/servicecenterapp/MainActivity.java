package com.example.servicecenterapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity implements MainAdapter.OnServiceRecordsClickListener {

    private FirebaseAuth auth;
    private TextView txtFirstname, txtLastname, txtPhoneNumber, txtEmail, txtCustomerId;
    private RecyclerView recyclerView;
    private MainAdapter mainAdapter;
    private List<MainData> mainDataList;
    private Connection connect;
    private FirebaseFirestore db;
    private ImageView img_btn_logout;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        txtFirstname = findViewById(R.id.txt_firstname);
        txtLastname = findViewById(R.id.txt_lastname);
        txtPhoneNumber = findViewById(R.id.txt_phone_number);
        txtEmail = findViewById(R.id.txt_email);
        txtCustomerId = findViewById(R.id.txt_customer_id);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mainDataList = new ArrayList<>();
        mainAdapter = new MainAdapter(mainDataList, this);
        recyclerView.setAdapter(mainAdapter);
        img_btn_logout = findViewById(R.id.img_logout_button);

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            loadUserInfo(user.getUid());
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }

        img_btn_logout.setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void loadUserInfo(String uid) {
        db.collection("users").document(uid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            String firstName = document.getString("firstName");
                            String lastName = document.getString("lastName");
                            String phoneNumber = document.getString("phoneNumber");
                            String email = document.getString("email");
                            String customerId = document.getString("customer_id");

                            txtFirstname.setText(firstName);
                            txtLastname.setText(lastName);
                            txtPhoneNumber.setText(phoneNumber);
                            txtEmail.setText(email);
                            txtCustomerId.setText(customerId);

                            // Fetch data from tblmain
                            fetchDataFromSql(customerId);
                        } else {
                            Log.d(TAG, "No such document");
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                        Toast.makeText(MainActivity.this, "Failed to load user details.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchDataFromSql(String customerId) {
        try {
            ConnectionHelper connectionHelper = new ConnectionHelper();
            connect = connectionHelper.connectionClass();
            if (connect != null) {
                String query = "SELECT tm.vehino,tm.vehibrand,tm.odimeter,tm.date FROM tblmain tm INNER JOIN (SELECT vehino, MAX(CONVERT(datetime, date, 105)) AS max_date FROM tblmain WHERE cusid = '" + customerId + "' GROUP BY vehino) subq ON tm.vehino = subq.vehino AND CONVERT(datetime, tm.date, 105) = subq.max_date WHERE tm.cusid = '" + customerId + "' ORDER BY tm.vehino;";
                Statement st = connect.createStatement();
                ResultSet rs = st.executeQuery(query);

                mainDataList.clear();
                while (rs.next()) {
                    String vehicleBrand = rs.getString("vehibrand");
                    String vehicleNo = rs.getString("vehino");
                    String odoMeter = rs.getString("odimeter");
                    mainDataList.add(new MainData(vehicleNo, vehicleBrand, odoMeter));
                }
                mainAdapter.notifyDataSetChanged();
                connect.close();
            } else {
                Toast.makeText(this, "Connection Error", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception ex) {
            Log.e("Error", ex.getMessage());
        }
    }

    @Override
    public void onServiceRecordsClick(String vehicleNo) {
        fetchServiceRecords(vehicleNo);
    }

    private void fetchServiceRecords(String vehicleNo) {
        try {
            ConnectionHelper connectionHelper = new ConnectionHelper();
            connect = connectionHelper.connectionClass();
            if (connect != null) {
                String query = "SELECT top 5 inno FROM tblitemlist WHERE vehino = '" + vehicleNo + "' group by inno ORDER BY inno Desc";

                Statement st = connect.createStatement();
                ResultSet rs = st.executeQuery(query);

                List<ServiceRecord> serviceRecords = new ArrayList<>();
                while (rs.next()) {
                    String inno = rs.getString("inno");
                    serviceRecords.add(new ServiceRecord(inno));
                }

                // Display service records (this can be done through a dialog or a new activity)
                showServiceRecordsDialog(serviceRecords);

                connect.close();
            } else {
                Toast.makeText(this, "Connection Error", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception ex) {
            Log.e("Error", ex.getMessage());
        }
    }


    private void showServiceRecordsDialog(List<ServiceRecord> serviceRecords) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.dialog_service_records, null);
        builder.setView(dialogView);

        TextView txtServiceRecords = dialogView.findViewById(R.id.txt_service_records);
        StringBuilder recordsBuilder = new StringBuilder();
        for (ServiceRecord record : serviceRecords) {
            recordsBuilder.append("Inno: ").append(record.getInno()).append("\n");
        }
        txtServiceRecords.setText(recordsBuilder.toString());

        builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
