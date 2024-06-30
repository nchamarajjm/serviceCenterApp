package com.example.servicecenterapp;

public class MainData {
    private String vehicleNo,vehicleBrand,odoMeter;


    public MainData(String vehicleNo, String vehicleBrand, String odoMeter) {
        this.vehicleNo = vehicleNo;
        this.vehicleBrand = vehicleBrand;
        this.odoMeter =odoMeter;
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

    public String getodoMeter() {
        return odoMeter;
    }

    public void setodoMeter(String odoMeter) {
        this.odoMeter = odoMeter;
    }
}
