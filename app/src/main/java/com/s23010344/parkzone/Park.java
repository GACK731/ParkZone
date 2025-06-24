package com.s23010344.parkzone;
import java.util.List;

public class Park {
    public String name;
    public double latitude;
    public double longitude;
    public boolean paid;
    public List<String> vehicleTypes;

    public Park() {} // Firebase requires empty constructor

    public Park(String name, double latitude, double longitude, boolean paid, List<String> vehicleTypes) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.paid = paid;
        this.vehicleTypes = vehicleTypes;
    }
}

