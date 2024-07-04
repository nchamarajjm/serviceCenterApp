package com.example.servicecenterapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.List;

public class LandingActivity extends AppCompatActivity {
    private CardView cardMyVehicles,card_apoinment,card_transfers,card_myCredit,card_myProfile,card_contactUs;
    private FirebaseAuth auth;
    private TextView title_view;
    private FirebaseFirestore db;
    private ImageView img_logout_button;
    private Connection connect;
    private String customerId;
    private static final String TAG = "LandingActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        cardMyVehicles = findViewById(R.id.card_myVehicles);
        card_apoinment = findViewById(R.id.card_apoinment);
        card_transfers = findViewById(R.id.card_transfers);
        card_myCredit = findViewById(R.id.card_myCredit);
        card_myProfile = findViewById(R.id.card_myProfile);
        card_contactUs = findViewById(R.id.card_contactUs);
        img_logout_button =findViewById(R.id.img_logout_button);
        title_view = findViewById(R.id.title_view);

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            loadUserInfo(user.getUid());
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(LandingActivity.this, LoginActivity.class));
            finish();
        }

        img_logout_button.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(LandingActivity.this)
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Sign out
                        auth.signOut();
                        startActivity(new Intent(LandingActivity.this, LoginActivity.class));
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.no, null);

            Drawable alertIcon = ContextCompat.getDrawable(LandingActivity.this, android.R.drawable.ic_dialog_alert);
            if (alertIcon != null) {
                alertIcon = DrawableCompat.wrap(alertIcon);
                DrawableCompat.setTint(alertIcon, ContextCompat.getColor(LandingActivity.this, R.color.orange));
            }
            builder.setIcon(alertIcon);
            builder.show();
        });

        cardMyVehicles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create an Intent to start MainActivity
                Intent intent = new Intent(LandingActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        card_apoinment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create an Intent to start MainActivity
                Intent intent = new Intent(LandingActivity.this, ApoinmentActivity.class);
                startActivity(intent);
            }
        });
        card_transfers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LandingActivity.this, "This feature will be available soon", Toast.LENGTH_SHORT).show();
            }
        });
        card_myCredit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchBalance(customerId);
            }
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
                    customerId = document.getString("customer_id");

                    title_view.setText("Welcome, "+firstName+ " " +lastName);
                    //fetchBalance(customerId);
                } else {
                    Log.d(TAG, "No such document");
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
                Toast.makeText(LandingActivity.this, "Failed to load user details.", Toast.LENGTH_SHORT).show();
            }
        });
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

                    // Show dialog with balance
                    showBalanceDialog(formattedBalance);
                } else {
                    // Handle case where no balance found
                    Toast.makeText(this, "No balance found for this customer", Toast.LENGTH_SHORT).show();
                    Log.i("UserId",customerId);
                }
                connect.close();
            } else {
                Toast.makeText(this, "Connection Error", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception ex) {
            Log.e("Error", ex.getMessage());
        }
    }
    private void showBalanceDialog(String balance) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Credit Balance");
        builder.setMessage("Your current balance is Rs: " + balance);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}