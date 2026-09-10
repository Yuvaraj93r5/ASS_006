package com.system.model;

public class Vehicle {
    private String vehicleNumber;
    private String ownerName;
    private VehicleType vehicleType;
    private String riskClassification; // Safe, Moderate, Chronic Offender

    public Vehicle(String vehicleNumber, String ownerName, VehicleType vehicleType) {
        this.vehicleNumber = vehicleNumber;
        this.ownerName = ownerName;
        this.vehicleType = vehicleType;
        this.riskClassification = "Safe";
    }

    // Getters and Setters
    public String getVehicleNumber() { return vehicleNumber; }
    public String getOwnerName() { return ownerName; }
    public VehicleType getVehicleType() { return vehicleType; }
    public String getRiskClassification() { return riskClassification; }
    public void setRiskClassification(String riskClassification) { this.riskClassification = riskClassification; }
}
