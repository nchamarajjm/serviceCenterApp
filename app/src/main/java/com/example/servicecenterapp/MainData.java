package com.example.servicecenterapp;

public class MainData {
    private String date;
    private String vehicleNo;

    public MainData(String date, String vehicleNo) {
        this.date = date;
        this.vehicleNo = vehicleNo;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getVehicleNo() {
        return vehicleNo;
    }

    public void setVehicleNo(String vehicleNo) {
        this.vehicleNo = vehicleNo;
    }
}
