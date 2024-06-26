package com.example.servicecenterapp;

public class MainData {
    private String vehicleNo,vehicleBrand;


    public MainData(String vehicleNo, String vehicleBrand) {
        this.vehicleNo = vehicleNo;
        this.vehicleBrand = vehicleBrand;
    }

    public String getVehicleNo() {
        return vehicleNo;
    }

    public void setVehicleNo(String vehicleNo) {
        this.vehicleNo = vehicleNo;
    }

    public String getvehicleBrand() {
        return vehicleBrand;
    }

    public void setvehicleBrand(String vehicleBrand) {
        this.vehicleBrand = vehicleBrand;
    }
}
