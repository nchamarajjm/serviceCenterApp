package com.example.servicecenterapp;

import android.annotation.SuppressLint;
import android.os.StrictMode;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConnectionHelper {

    Connection con;
    String username,password,ip,port,database;
    @SuppressLint("NewApi")
    public  Connection connectionClass(){
        ip ="139.99.90.229";
        database="TestAppDB";
        username="sa";
        password="sa@SQL24";
        port="1433";

        StrictMode.ThreadPolicy policy =new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Connection connection =null;
        String ConnectionURL= null;

        try{

            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            ConnectionURL= "jdbc:jtds:sqlserver://"+ ip + ":"+ port+";"+ "databasename="+ database+";user="+username+";password="+password+";";
            connection = DriverManager.getConnection(ConnectionURL);
        }catch (Exception ex){

            Log.e("Error", ex.getMessage());

        }
        return connection;

    }
}