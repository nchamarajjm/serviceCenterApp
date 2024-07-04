package com.example.servicecenterapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.sql.Connection;

public class ApoinmentActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private Connection connect;
    private String customerId;
    private ImageView img_logout_button;
    private TextView textView2;
    private static final String TAG = "ApoinmentActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apoinment);

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        img_logout_button = findViewById(R.id.img_logout_button);
        textView2 = findViewById(R.id.textView2);

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            loadUserInfo(user.getUid());
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(ApoinmentActivity.this, LoginActivity.class));
            finish();
        }

        img_logout_button.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(ApoinmentActivity.this)
                    .setTitle("Sign Out")
                    .setMessage("Are you sure you want to sign out?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Sign out
                            auth.signOut();
                            startActivity(new Intent(ApoinmentActivity.this, LoginActivity.class));
                            finish();
                        }
                    })
                    .setNegativeButton(android.R.string.no, null);

            Drawable alertIcon = ContextCompat.getDrawable(ApoinmentActivity.this, android.R.drawable.ic_dialog_alert);
            if (alertIcon != null) {
                alertIcon = DrawableCompat.wrap(alertIcon);
                DrawableCompat.setTint(alertIcon, ContextCompat.getColor(ApoinmentActivity.this, R.color.orange));
            }
            builder.setIcon(alertIcon);
            builder.show();
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

                    textView2.setText("Welcome, "+firstName+ " " +lastName);
                    //fetchBalance(customerId);
                } else {
                    Log.d(TAG, "No such document");
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
                Toast.makeText(ApoinmentActivity.this, "Failed to load user details.", Toast.LENGTH_SHORT).show();
            }
        });
    }

}