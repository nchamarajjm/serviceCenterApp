package com.example.servicecenterapp;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private TextView textViewCustomerId, textViewEmail, textViewFirstName, textViewLastName, textViewPhoneNumber;
    private static final String TAG = "ProfileActivity";
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        textViewCustomerId = findViewById(R.id.textViewCustomerId);
        textViewEmail = findViewById(R.id.textViewEmail);
        textViewFirstName = findViewById(R.id.textViewFirstName);
        textViewLastName = findViewById(R.id.textViewLastName);
        textViewPhoneNumber = findViewById(R.id.textViewPhoneNumber);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            progressDialog.show();
            loadUserInfo(user.getUid());
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadUserInfo(String uid) {
        db.collection("users").document(uid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            String customerId = document.getString("customer_id");
                            String email = document.getString("email");
                            String firstName = document.getString("firstName");
                            String lastName = document.getString("lastName");
                            String phoneNumber = document.getString("phoneNumber");

                            textViewCustomerId.setText("Customer ID: " + customerId);
                            textViewEmail.setText("Email: " + email);
                            textViewFirstName.setText("First Name: " + firstName);
                            textViewLastName.setText("Last Name: " + lastName);
                            textViewPhoneNumber.setText("Phone Number: " + phoneNumber);
                        } else {
                            Log.d(TAG, "No such document");
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                        Toast.makeText(ProfileActivity.this, "Failed to load user details.", Toast.LENGTH_SHORT).show();
                    }
                    progressDialog.dismiss();
                });
    }
}
