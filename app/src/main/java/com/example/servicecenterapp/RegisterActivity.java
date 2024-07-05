package com.example.servicecenterapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RegisterActivity extends AppCompatActivity {

    private EditText inputFirstName, inputLastName, inputPhoneNumber, inputEmail, inputPassword, inputOtp;
    private Button btnSignUp, btnLogin, btnVerifyOtp;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private TextInputLayout inputOtpLayout;
    private static final String TAG = "RegisterActivity";

    private String verificationId;
    private PhoneAuthProvider.ForceResendingToken resendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_register);

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnSignUp = findViewById(R.id.sign_up_button);
        btnLogin = findViewById(R.id.login_button);
        inputFirstName = findViewById(R.id.first_name);
        inputLastName = findViewById(R.id.last_name);
        inputPhoneNumber = findViewById(R.id.phone_number);
        inputEmail = findViewById(R.id.email);
        inputPassword = findViewById(R.id.password);
        inputOtp = findViewById(R.id.otp_code);
        inputOtpLayout = findViewById(R.id.otp_code_layout);
        btnVerifyOtp = findViewById(R.id.verify_otp_button);
        progressBar = findViewById(R.id.progressBar);

        btnSignUp.setOnClickListener(v -> {
            String firstName = inputFirstName.getText().toString().trim();
            String lastName = inputLastName.getText().toString().trim();
            String phoneNumber = inputPhoneNumber.getText().toString().trim();
            String email = inputEmail.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();

            if (TextUtils.isEmpty(firstName)) {
                Toast.makeText(getApplicationContext(), "Enter first name!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(lastName)) {
                Toast.makeText(getApplicationContext(), "Enter last name!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(phoneNumber)) {
                Toast.makeText(getApplicationContext(), "Enter phone number!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(getApplicationContext(), "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);

            // Create user
            auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(RegisterActivity.this, task -> {
                progressBar.setVisibility(View.GONE);
                if (!task.isSuccessful()) {
                    String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                    Toast.makeText(RegisterActivity.this, "Authentication Failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Authentication Failed", task.getException());
                } else {
                    FirebaseUser user = auth.getCurrentUser();
                    if (user != null) {
                        String uid = user.getUid();
                        Map<String, Object> userDetails = new HashMap<>();
                        userDetails.put("firstName", firstName);
                        userDetails.put("lastName", lastName);
                        userDetails.put("phoneNumber", "+94"+phoneNumber);
                        userDetails.put("email", email);
                        userDetails.put("customer_id", "");

                        db.collection("users").document(uid)
                        .set(userDetails)
                        .addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                sendVerificationEmail();
                                //Verify Phone Number
                                //sendVerificationSms(phoneNumber);
                                //inputOtpLayout.setVisibility(View.VISIBLE);
                                //btnVerifyOtp.setVisibility(View.VISIBLE);
                            } else {
                                Toast.makeText(RegisterActivity.this, "Failed to save user details: " + task1.getException(), Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Failed to save user details", task1.getException());
                            }
                        });
                    }
                }
            });
        });

        btnVerifyOtp.setOnClickListener(v -> {
            String code = inputOtp.getText().toString().trim();
            if (TextUtils.isEmpty(code)) {
                Toast.makeText(RegisterActivity.this, "Enter OTP!", Toast.LENGTH_SHORT).show();
                return;
            }
            verifyOtp(code);
        });

        btnLogin.setOnClickListener(v -> startActivity(new Intent(RegisterActivity.this, LoginActivity.class)));
    }

    private void sendVerificationEmail() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this, "Verification email sent to " + user.getEmail(), Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    finish();
                } else {
                    Toast.makeText(RegisterActivity.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void sendVerificationSms(String phoneNumber) {
    PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
        .setPhoneNumber(phoneNumber)
        .setTimeout(60L, TimeUnit.SECONDS) // Adjust the timeout as needed
        .setActivity(this)
        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                String code = phoneAuthCredential.getSmsCode();
                if (code != null) {
                    inputOtp.setText(code);
                    verifyOtp(code);
                }
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Toast.makeText(RegisterActivity.this, "Failed to send SMS verification: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "SMS Verification Failed", e);
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                RegisterActivity.this.verificationId = verificationId;
                RegisterActivity.this.resendToken = token;
                Toast.makeText(RegisterActivity.this, "OTP sent to phone number.", Toast.LENGTH_SHORT).show();
            }
        })
        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void verifyOtp(String code) {
        if (verificationId != null) {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
            signInWithPhoneAuthCredential(credential);
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
    auth.signInWithCredential(credential)
        .addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                Toast.makeText(RegisterActivity.this, "Phone number verified successfully", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            } else {
                Toast.makeText(RegisterActivity.this, "OTP verification failed: " + task.getException(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "OTP Verification Failed", task.getException());
            }
        });
    }
}
