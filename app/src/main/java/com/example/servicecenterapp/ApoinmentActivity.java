package com.example.servicecenterapp;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.MessagingException;

public class ApoinmentActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String customerId, phoneNumber, firstName, lastName;
    private ImageView img_logout_button;
    private TextView title_text_username;
    private EditText datePicker, timePicker, commentEditText;
    private Button makeAppointmentButton;
    private Spinner vehicleSpinner;
    private static final String TAG = "ApoinmentActivity";
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private ProgressDialog progressDialog, dataLoadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apoinment);

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        img_logout_button = findViewById(R.id.img_logout_button);
        title_text_username = findViewById(R.id.title_text_username);
        datePicker = findViewById(R.id.date_picker);
        timePicker = findViewById(R.id.time_picker);
        commentEditText = findViewById(R.id.comment_box);
        makeAppointmentButton = findViewById(R.id.make_appointment_button);
        vehicleSpinner = findViewById(R.id.vehicle_spinner);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending email...");
        progressDialog.setCancelable(false);

        dataLoadingDialog = new ProgressDialog(this);
        dataLoadingDialog.setMessage("Loading data...");
        dataLoadingDialog.setCancelable(false);

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            dataLoadingDialog.show(); // Show data loading dialog
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
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        auth.signOut();
                        startActivity(new Intent(ApoinmentActivity.this, LoginActivity.class));
                        finish();
                    })
                    .setNegativeButton(android.R.string.no, null);

            builder.show();
        });

        datePicker.setOnClickListener(v -> showDatePickerDialog());
        timePicker.setOnClickListener(v -> showTimePickerDialog());

        makeAppointmentButton.setOnClickListener(v -> makeAppointment());
    }

    private void loadUserInfo(String uid) {
        db.collection("users").document(uid).get()
        .addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    firstName = document.getString("firstName");
                    lastName = document.getString("lastName");
                    phoneNumber = document.getString("phoneNumber");
                    customerId = document.getString("customer_id");

                    title_text_username.setText("Welcome, " + firstName);
                    loadUserVehicles(customerId);
                } else {
                    Log.d(TAG, "No such document");
                    dataLoadingDialog.dismiss(); // Dismiss data loading dialog
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
                Toast.makeText(ApoinmentActivity.this, "Failed to load user details.", Toast.LENGTH_SHORT).show();
                dataLoadingDialog.dismiss(); // Dismiss data loading dialog
            }
        });
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
            String selectedDate = (month1 + 1) + "/" + dayOfMonth + "/" + year1;
            datePicker.setText(selectedDate);
        }, year, month, day);

        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        datePickerDialog.show();
    }

    private void showTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute1) -> {
            String selectedTime = formatTime(hourOfDay, minute1);
            timePicker.setText(selectedTime);
        }, hour, minute, false);

        timePickerDialog.show();
    }

    private String formatTime(int hourOfDay, int minute) {
        Calendar datetime = Calendar.getInstance();
        datetime.set(Calendar.HOUR_OF_DAY, hourOfDay);
        datetime.set(Calendar.MINUTE, minute);

        String amPm = (datetime.get(Calendar.AM_PM) == Calendar.AM) ? "AM" : "PM";
        int hour = datetime.get(Calendar.HOUR);
        if (hour == 0) hour = 12;

        return String.format("%02d:%02d %s", hour, minute, amPm);
    }

    private void loadUserVehicles(String customerId) {
        dataLoadingDialog.show(); // Show data loading dialog
        List<String> vehicles = new ArrayList<>();
        vehicles.add("Select Vehicle");

        ConnectionHelper connectionHelper = new ConnectionHelper();
        Connection connect = connectionHelper.connectionClass();
        if (connect != null) {
            String query = "SELECT DISTINCT vehino FROM tblmain WHERE cusid = '" + customerId + "'";
            try {
                Statement st = connect.createStatement();
                ResultSet rs = st.executeQuery(query);
                while (rs.next()) {
                    vehicles.add(rs.getString("vehino"));
                }
                connect.close();
            } catch (Exception ex) {
                Log.e(TAG, "Error fetching vehicles: " + ex.getMessage());
            }
        } else {
            Log.e(TAG, "Connection Error");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, vehicles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        vehicleSpinner.setAdapter(adapter);

        dataLoadingDialog.dismiss(); // Dismiss data loading dialog
    }

    private void makeAppointment() {
        String date = datePicker.getText().toString();
        String time = timePicker.getText().toString();
        String comment = commentEditText.getText().toString();
        String vehicleNumber = vehicleSpinner.getSelectedItem().toString();
        if (TextUtils.isEmpty(date) || TextUtils.isEmpty(time) || TextUtils.isEmpty(comment) || TextUtils.isEmpty(vehicleNumber)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userEmail = user.getEmail();

            progressDialog.show(); // Show the progress dialog

            executorService.execute(() -> {
                try {
                    // Sending email
                    MailSender mailSender = new MailSender("savontainfo@gmail.com", "rbxe bkzt zkhr syps");
                    mailSender.sendMail("savontaservice15@gmail.com", "New Appointment",
                            "Customer: " + firstName + " " + lastName + "\nContact Number: " + phoneNumber + "\nDate: " + date + "\nTime: " + time + "\nVehicle Number: " + vehicleNumber + "\nService: " + comment);

                    // After sending email, prepare WhatsApp message
                    String whatsappMessage = "New Appointment\n" +
                            "Customer: " + firstName + " " + lastName + "\n" +
                            "Contact Number: " + phoneNumber + "\n" +
                            "Date: " + date + "\n" +
                            "Time: " + time + "\n" +
                            "Vehicle Number: " + vehicleNumber + "\n" +
                            "Service: " + comment;

                    // Check if WhatsApp Business is installed
                    if (isWhatsAppBusinessInstalled()) {
                        // Launch WhatsApp Business
                        Intent whatsappIntent = new Intent(Intent.ACTION_VIEW);
                        whatsappIntent.setPackage("com.whatsapp.w4b");
                        String url = "https://wa.me/+94706222111?text=" + Uri.encode(whatsappMessage);
                        whatsappIntent.setData(Uri.parse(url));
                        startActivity(whatsappIntent);
                    } else if (isWhatsAppInstalled()) {
                        // Launch regular WhatsApp
                        Intent whatsappIntent = new Intent(Intent.ACTION_VIEW);
                        whatsappIntent.setPackage("com.whatsapp");
                        String url = "https://wa.me/+94706222111?text=" + Uri.encode(whatsappMessage);
                        whatsappIntent.setData(Uri.parse(url));
                        startActivity(whatsappIntent);
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(ApoinmentActivity.this, "WhatsApp or WhatsApp Business is not installed", Toast.LENGTH_SHORT).show();
                        });
                    }

                    runOnUiThread(() -> {
                        progressDialog.dismiss(); // Dismiss the progress dialog
                        Toast.makeText(ApoinmentActivity.this, "Email sent successfully", Toast.LENGTH_SHORT).show();
                    });
                } catch (MessagingException e) {
                    runOnUiThread(() -> {
                        progressDialog.dismiss(); // Dismiss the progress dialog
                        Toast.makeText(ApoinmentActivity.this, "Failed to send email: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                    Log.e(TAG, "Failed to send email", e);
                }
            });
        }
    }

    private boolean isWhatsAppInstalled() {
        // Check if regular WhatsApp is installed on the device
        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private boolean isWhatsAppBusinessInstalled() {
        // Check if WhatsApp Business is installed on the device
        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo("com.whatsapp.w4b", PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
