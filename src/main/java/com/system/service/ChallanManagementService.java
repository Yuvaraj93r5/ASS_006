package com.system.service;

import com.system.exception.DuplicateChallanException;
import com.system.exception.InvalidVehicleException;
import com.system.model.*;

import java.time.LocalDateTime;
import java.util.*;

public class ChallanManagementService {
    private final Map<String, Vehicle> vehicleRegistry = new HashMap<>();
    private final List<Challan> challanList = new ArrayList<>();
    private int challanCounter = 1;

    public void registerVehicle(Vehicle vehicle) throws InvalidVehicleException {
        if (vehicle == null || vehicle.getVehicleNumber() == null || vehicle.getVehicleNumber().trim().isEmpty()) {
            throw new InvalidVehicleException("Invalid vehicle information provided.");
        }
        if (vehicle.getOwnerName() == null || vehicle.getOwnerName().trim().isEmpty()) {
            throw new InvalidVehicleException("Owner details cannot be empty.");
        }
        vehicleRegistry.put(vehicle.getVehicleNumber(), vehicle);
    }

    public Challan issueChallan(String vehicleNumber, ViolationType violationType, String location, 
                                 LocalDateTime timestamp, double speed, double permittedSpeed) 
                                 throws InvalidVehicleException, DuplicateChallanException {
        
        if (!vehicleRegistry.containsKey(vehicleNumber)) {
            throw new InvalidVehicleException("Vehicle number " + vehicleNumber + " is not registered in the system.");
        }

        // Detect Over-Speeding violation validity
        if (violationType == ViolationType.OVER_SPEEDING && speed <= permittedSpeed) {
            return null; // No violation occurred
        }

        // Prevent Duplicate Challans for the exact same event parameters
        for (Challan existing : challanList) {
            if (existing.getVehicleNumber().equals(vehicleNumber) &&
                existing.getViolationType() == violationType &&
                existing.getLocation().equalsIgnoreCase(location) &&
                existing.getTimestamp().equals(timestamp)) {
                throw new DuplicateChallanException("Challan already exists for this exact violation event.");
            }
        }

        long historicalViolationsCount = challanList.stream()
                .filter(c -> c.getVehicleNumber().equals(vehicleNumber))
                .count();

        // Base Fine logic execution
        double finalFine = violationType.getBaseFine();

        // Apply progressive higher penalty rules for repeat behavior
        if (historicalViolationsCount > 0) {
            finalFine += (historicalViolationsCount * 500.0);
        }

        String challanId = "CH-" + String.format("%05d", challanCounter++);
        Challan newChallan = new Challan(challanId, vehicleNumber, violationType, location, timestamp, speed, permittedSpeed, finalFine);
        challanList.add(newChallan);

        // Re-classify risk profile metrics dynamically
        updateVehicleRiskClassification(vehicleNumber);

        return newChallan;
    }

    public void payChallan(String challanId) {
        challanList.stream()
                .filter(c -> c.getChallanId().equals(challanId))
                .findFirst()
                .ifPresent(challan -> challan.setPaid(true));
    }

    public double calculateTotalOutstandingFines(String vehicleNumber) {
        return challanList.stream()
                .filter(c -> c.getVehicleNumber().equals(vehicleNumber) && !c.isPaid())
                .mapToDouble(Challan::getFineAmount)
                .sum();
    }

    private void updateVehicleRiskClassification(String vehicleNumber) {
        long violations = challanList.stream()
                .filter(c -> c.getVehicleNumber().equals(vehicleNumber))
                .count();

        Vehicle vehicle = vehicleRegistry.get(vehicleNumber);
        if (vehicle != null) {
            if (violations >= 4) {
                vehicle.setRiskClassification("Chronic Offender");
            } else if (violations >= 2) {
                vehicle.setRiskClassification("Moderate");
            } else {
                vehicle.setRiskClassification("Safe");
            }
        }
    }

    public List<Challan> getChallansByVehicle(String vehicleNumber) {
        return challanList.stream()
                .filter(c -> c.getVehicleNumber().equals(vehicleNumber))
                .toList();
    }

    public Vehicle getVehicle(String vehicleNumber) {
        return vehicleRegistry.get(vehicleNumber);
    }
}
