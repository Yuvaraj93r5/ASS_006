package com.system.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Challan {
    private String challanId;
    private String vehicleNumber;
    private ViolationType violationType;
    private String location;
    private LocalDateTime timestamp;
    private double speed;
    private double permittedSpeed;
    private double fineAmount;
    private boolean isPaid;

    public Challan(String challanId, String vehicleNumber, ViolationType violationType, 
                   String location, LocalDateTime timestamp, double speed, double permittedSpeed, double fineAmount) {
        this.challanId = challanId;
        this.vehicleNumber = vehicleNumber;
        this.violationType = violationType;
        this.location = location;
        this.timestamp = timestamp;
        this.speed = speed;
        this.permittedSpeed = permittedSpeed;
        this.fineAmount = fineAmount;
        this.isPaid = false;
    }

    // Getters and Setters
    public String getChallanId() { return challanId; }
    public String getVehicleNumber() { return vehicleNumber; }
    public ViolationType getViolationType() { return violationType; }
    public String getLocation() { return location; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public double getSpeed() { return speed; }
    public double getPermittedSpeed() { return permittedSpeed; }
    public double getFineAmount() { return fineAmount; }
    public boolean isPaid() { return isPaid; }
    public void setPaid(boolean paid) { isPaid = paid; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Challan challan = (Challan) o;
        return Objects.equals(vehicleNumber, challan.vehicleNumber) &&
               violationType == challan.violationType &&
               Objects.equals(location, challan.location) &&
               Objects.equals(timestamp, challan.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vehicleNumber, violationType, location, timestamp);
    }
}
