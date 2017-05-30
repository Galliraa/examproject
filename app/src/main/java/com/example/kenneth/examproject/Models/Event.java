package com.example.kenneth.examproject.Models;

public class Event {

    private String Name;
    private String StartTime;
    private String EndTime;
    private String Descrition;
    private String Address;
    private double Longitude;
    private double Latitude;
    private String EventImage;

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getStartTime() {
        return StartTime;
    }

    public void setStartTime(String startTime) {
        StartTime = startTime;
    }

    public String getDescrition() {
        return Descrition;
    }

    public String getEndTime() {return EndTime;}

    public void setEndTime(String endTime) {EndTime = endTime;}

    public void setDescrition(String descrition) {
        Descrition = descrition;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public double getLongitude() {
        return Longitude;
    }

    public void setLongitude(double longitude) {
        Longitude = longitude;
    }

    public double getLatitude() {
        return Latitude;
    }

    public void setLatitude(double latitude) {
        Latitude = latitude;
    }

    public String getEventImage() {
        return EventImage;
    }

    public void setEventImage(String eventImage) {
        EventImage = eventImage;
    }
}
