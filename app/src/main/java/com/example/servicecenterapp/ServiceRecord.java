package com.example.servicecenterapp;

public class ServiceRecord {
    private String inno;
    private String date;


    public ServiceRecord(String inno, String date) {

        this.inno = inno;
        this.date=date;
    }

    public String getInno() {
        return inno;
    }

    public void setInno(String inno) {
        this.inno = inno;
    }

    public String getDate() {
        return date;
    }
}