package com.example.servicecenterapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;

public class VerificationActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private static final String TAG = "VerificationActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, pendingDynamicLinkData -> {
                    Uri deepLink = null;
                    if (pendingDynamicLinkData != null) {
                        deepLink = pendingDynamicLinkData.getLink();
                        handleEmailVerification();
                    }
                })
                .addOnFailureListener(this, e -> {
                    Log.w(TAG, "getDynamicLink:onFailure", e);
                    Toast.makeText(VerificationActivity.this, "Failed to handle the link.", Toast.LENGTH_SHORT).show();
                });
    }

    private void handleEmailVerification() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            user.reload().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (user.isEmailVerified()) {
                        db.collection("users").document(user.getUid())
                                .update("isVerified", true)
                                .addOnCompleteListener(updateTask -> {
                                    if (updateTask.isSuccessful()) {
                                        Toast.makeText(VerificationActivity.this, "Email verified successfully!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(VerificationActivity.this, LoginActivity.class));
                                        finish();
                                    } else {
                                        Toast.makeText(VerificationActivity.this, "Failed to update verification status: " + updateTask.getException(), Toast.LENGTH_SHORT).show();
                                        Log.e(TAG, "Failed to update verification status", updateTask.getException());
                                    }
                                });
                    } else {
                        Toast.makeText(this, "Email not verified. Please check your email.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, LoginActivity.class));
                    }
                } else {
                    Toast.makeText(this, "Failed to reload user.", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Failed to reload user", task.getException());
                }
            });
        } else {
            Toast.makeText(this, "User not found. Please log in.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
        }
    }
}
