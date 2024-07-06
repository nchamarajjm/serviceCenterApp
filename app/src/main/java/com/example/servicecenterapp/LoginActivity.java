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

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private EditText inputEmail, inputPassword, inputPhoneNumber, inputOtp;
    private Button btnLogin, btnRegister, btnSendOtp, btnVerifyOtp;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private static final String TAG = "LoginActivity";

    private String verificationId;
    private PhoneAuthProvider.ForceResendingToken resendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        inputEmail = findViewById(R.id.email);
        inputPassword = findViewById(R.id.password);
        inputPhoneNumber = findViewById(R.id.phone_number);
        inputOtp = findViewById(R.id.otp_code);
        btnLogin = findViewById(R.id.login_button);
        btnRegister = findViewById(R.id.sign_up_button);
        btnSendOtp = findViewById(R.id.send_otp_button);
        btnVerifyOtp = findViewById(R.id.verify_otp_button);
        progressBar = findViewById(R.id.progressBar);

        btnLogin.setOnClickListener(v -> {
            String email = inputEmail.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);

            // Authenticate user with email and password
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(LoginActivity.this, task -> {
                        progressBar.setVisibility(View.GONE);
                        if (!task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Authentication Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        } else {
                            FirebaseUser user = auth.getCurrentUser();
                            if (user != null) {
                                if (user.isEmailVerified()) {
                                    navigateBasedOnUserType(user.getUid());
                                } else {
                                    Toast.makeText(LoginActivity.this, "Please verify your email before logging in.", Toast.LENGTH_SHORT).show();
                                    auth.signOut();
                                }
                            }
                        }
                    });
        });

        btnSendOtp.setOnClickListener(v -> {
            String phoneNumber = inputPhoneNumber.getText().toString().trim();
            if (TextUtils.isEmpty(phoneNumber)) {
                Toast.makeText(getApplicationContext(), "Enter phone number!", Toast.LENGTH_SHORT).show();
                return;
            }
            sendVerificationSms(phoneNumber);
        });

        btnVerifyOtp.setOnClickListener(v -> {
            String code = inputOtp.getText().toString().trim();
            if (TextUtils.isEmpty(code)) {
                Toast.makeText(getApplicationContext(), "Enter OTP!", Toast.LENGTH_SHORT).show();
                return;
            }
            verifyOtp(code);
        });

        btnRegister.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
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
                        Toast.makeText(LoginActivity.this, "Failed to send SMS verification: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "SMS Verification Failed", e);
                    }

                    @Override
                    public void onCodeSent(@NonNull String verificationId,
                                           @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        LoginActivity.this.verificationId = verificationId;
                        LoginActivity.this.resendToken = token;
                        Toast.makeText(LoginActivity.this, "OTP sent to phone number.", Toast.LENGTH_SHORT).show();
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
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            navigateBasedOnUserType(user.getUid());
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "OTP verification failed: " + task.getException(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "OTP Verification Failed", task.getException());
                    }
                });
    }

    private void navigateBasedOnUserType(String uid) {
        db.collection("users").document(uid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            Long userType = document.getLong("user_type");
                            if (userType != null) {
                                if (userType == 1) {
                                    startActivity(new Intent(LoginActivity.this, UserManageActivity.class));
                                } else {
                                    startActivity(new Intent(LoginActivity.this, LandingActivity.class));
                                }
                                finish();
                            } else {
                                Toast.makeText(LoginActivity.this, "User type is undefined.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "User data not found.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Failed to fetch user data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Failed to fetch user data", task.getException());
                    }
                });
    }
}
