package com.example.servicecenterapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.text.DecimalFormat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity implements MainAdapter.OnServiceRecordsClickListener {

    private FirebaseAuth auth;
    private TextView txtFirstname, txtLastname, txtPhoneNumber, txtEmail, txtCustomerId,txtCreditBal;
    private RecyclerView recyclerView;
    private MainAdapter mainAdapter;
    private List<MainData> mainDataList;
    private Connection connect;
    private FirebaseFirestore db;
    private ImageView img_btn_logout;
    private ProgressBar progressBar;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
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
        progressBar = findViewById(R.id.progress_bar);  // Initialize the ProgressBar
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mainDataList = new ArrayList<>();
        mainAdapter = new MainAdapter(mainDataList, this);
        recyclerView.setAdapter(mainAdapter);
        img_btn_logout = findViewById(R.id.img_logout_button);
        txtCreditBal = findViewById(R.id.txtCreditBal);

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            progressBar.setVisibility(View.VISIBLE);  // Show progress bar
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
                            // Fetch balance from tblclist
                            fetchBalance(customerId);
                        } else {
                            Log.d(TAG, "No such document");
                            progressBar.setVisibility(View.GONE);  // Hide progress bar on failure
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                        Toast.makeText(MainActivity.this, "Failed to load user details.", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);  // Hide progress bar on failure
                    }
                });
    }

    private void fetchDataFromSql(String customerId) {
        try {
            ConnectionHelper connectionHelper = new ConnectionHelper();
            connect = connectionHelper.connectionClass();
            if (connect != null) {
                String query = "SELECT tm.vehino, tm.vehibrand, tm.odimeter, tm.date FROM tblmain tm " +
                        "INNER JOIN (SELECT vehino, MAX(CONVERT(datetime, date, 105)) AS max_date FROM tblmain " +
                        "WHERE cusid = '" + customerId + "' GROUP BY vehino) subq " +
                        "ON tm.vehino = subq.vehino AND CONVERT(datetime, tm.date, 105) = subq.max_date " +
                        "WHERE tm.cusid = '" + customerId + "' ORDER BY tm.vehino;";
                Statement st = connect.createStatement();
                ResultSet rs = st.executeQuery(query);

                mainDataList.clear();
                while (rs.next()) {
                    String vehicleBrand = rs.getString("vehibrand");
                    String vehicleNo = rs.getString("vehino");
                    String odoMeter = rs.getString("odimeter");
                    MainData mainData = new MainData(vehicleNo, vehicleBrand, odoMeter);

                    // Fetch service records for the current vehicle
                    List<ServiceRecord> serviceRecords = fetchServiceRecords(vehicleNo);
                    mainData.setServiceRecords(serviceRecords);

                    mainDataList.add(mainData);
                }
                mainAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);  // Hide progress bar when data is loaded
                connect.close();
            } else {
                Toast.makeText(this, "Connection Error", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);  // Hide progress bar on failure
            }
        } catch (Exception ex) {
            Log.e("Error", ex.getMessage());
            progressBar.setVisibility(View.GONE);  // Hide progress bar on failure
        }
    }

    private List<ServiceRecord> fetchServiceRecords(String vehicleNo) {
        List<ServiceRecord> serviceRecords = new ArrayList<>();
        try {
            ConnectionHelper connectionHelper = new ConnectionHelper();
            connect = connectionHelper.connectionClass();
            if (connect != null) {
                String query = "SELECT top 5 inno FROM tblitemlist WHERE vehino = '" + vehicleNo + "' group by inno ORDER BY inno Desc";
                Statement st = connect.createStatement();
                ResultSet rs = st.executeQuery(query);

                while (rs.next()) {
                    String inno = rs.getString("inno");
                    serviceRecords.add(new ServiceRecord(inno));
                }
                connect.close();
            } else {
                Toast.makeText(this, "Connection Error", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception ex) {
            Log.e("Error", ex.getMessage());
        }
        return serviceRecords;
    }

    @Override
    public void onServiceRecordsClick(String vehicleNo, int position) {
        // This method can be left empty if service records are loaded on activity load
    }

    private void fetchBalance(String customerId) {
        try {
            ConnectionHelper connectionHelper = new ConnectionHelper();
            connect = connectionHelper.connectionClass();
            if (connect != null) {
                String query = "SELECT balance FROM tblclist WHERE ownerid = '" + customerId + "'";
                Statement st = connect.createStatement();
                ResultSet rs = st.executeQuery(query);

                if (rs.next()) {
                    double balance = rs.getDouble("balance");
                    DecimalFormat df = new DecimalFormat("#.00");  // formatter with two decimal places
                    String formattedBalance = df.format(balance);
                    txtCreditBal.setText("Credit Balance Rs: "+formattedBalance);
                    txtCreditBal.setVisibility(View.VISIBLE);
                } else {
                    txtCreditBal.setVisibility(View.GONE); // Hide if no records
                }
                connect.close();
            } else {
                Toast.makeText(this, "Connection Error", Toast.LENGTH_SHORT).show();
                txtCreditBal.setVisibility(View.GONE);
            }
        } catch (Exception ex) {
            Log.e("Error", ex.getMessage());
            txtCreditBal.setVisibility(View.GONE);
        }
    }
}
