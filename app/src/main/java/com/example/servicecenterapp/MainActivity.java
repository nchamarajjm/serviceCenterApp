package com.example.servicecenterapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private Button btnLogout;
    TextView txt1,txt2;
    Connection connect;
    String ConnectionResult="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        btnLogout = findViewById(R.id.logout_button);

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signOut();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
        });
    }
    public void GetTextFromSQL(View v){
        txt1= findViewById(R.id.txt_1);
        txt2 =findViewById(R.id.txt_2);

        try {
            ConnectionHelper connectionHelper = new ConnectionHelper();
            connect = connectionHelper.connectionClass();
            if(connect !=null){
                String query = "Select * from users";
                Statement st =connect.createStatement();
                ResultSet rs = st.executeQuery(query);

                while (rs.next()){
                    txt1.setText(rs.getString(2));
                    txt2.setText(rs.getString(3));
                }

            }
            else {
                ConnectionResult = "Connection Error";
            }

        }catch (Exception ex){

            Log.e("Error", ex.getMessage());

        }
    }
}