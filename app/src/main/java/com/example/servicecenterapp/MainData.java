package com.example.servicecenterapp;

import java.util.List;

import java.util.List;

public class MainData {
    private String vehicleNo, vehicleBrand, odoMeter;
    private List<ServiceRecord> serviceRecords; // Add this

    public MainData(String vehicleNo, String vehicleBrand, String odoMeter) {
        this.vehicleNo = vehicleNo;
        this.vehicleBrand = vehicleBrand;
        this.odoMeter = odoMeter;
    }

    public String getVehicleNo() {
        return vehicleNo;
    }

    public void setVehicleNo(String vehicleNo) {
        this.vehicleNo = vehicleNo;
    }

    public String getVehicleBrand() {
        return vehicleBrand;
    }

    public void setVehicleBrand(String vehicleBrand) {
        this.vehicleBrand = vehicleBrand;
    }

    public String getOdoMeter() {
        return odoMeter;
    }

    public void setOdoMeter(String odoMeter) {
        this.odoMeter = odoMeter;
    }

    public List<ServiceRecord> getServiceRecords() {
        return serviceRecords;
    }

    public void setServiceRecords(List<ServiceRecord> serviceRecords) {
        this.serviceRecords = serviceRecords;
    }
}
