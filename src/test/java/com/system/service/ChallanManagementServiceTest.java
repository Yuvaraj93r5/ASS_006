package com.system.service;

import com.system.exception.DuplicateChallanException;
import com.system.exception.InvalidVehicleException;
import com.system.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ChallanManagementServiceTest {
    private ChallanManagementService service;
    private LocalDateTime sampleTime;

    @BeforeEach
    public void setUp() {
        service = new ChallanManagementService();
        sampleTime = LocalDateTime.of(2026, 9, 10, 10, 0);
    }

    // --- NORMAL SCENARIOS ---
    @Test
    public void testRegisterAndVerifyVehicleNormal() throws InvalidVehicleException {
        Vehicle vehicle = new Vehicle("KA-01-1234", "John Doe", VehicleType.FOUR_WHEELER);
        service.registerVehicle(vehicle);
        assertNotNull(service.getVehicle("KA-01-1234"));
    }

    @Test
    public void testIssueAndPayChallanNormal() throws Exception {
        Vehicle vehicle = new Vehicle("KA-01-1234", "John Doe", VehicleType.FOUR_WHEELER);
        service.registerVehicle(vehicle);

        Challan challan = service.issueChallan("KA-01-1234", ViolationType.SIGNAL_VIOLATION, "Intersection A", sampleTime, 0, 0);
        assertNotNull(challan);
        assertEquals(1500.0, challan.getFineAmount());
        assertFalse(challan.isPaid());

        service.payChallan(challan.getChallanId());
        assertTrue(challan.isPaid());
    }

    // --- BOUNDARY SCENARIOS ---
    @Test
    public void testOverSpeedingExactlyAtPermittedSpeed() throws Exception {
        Vehicle vehicle = new Vehicle("KA-01-5555", "Alice Smith", VehicleType.TWO_WHEELER);
        service.registerVehicle(vehicle);

        // Speed is exactly equal to permitted speed (Boundary check: should not result in a violation)
        Challan challan = service.issueChallan("KA-01-5555", ViolationType.OVER_SPEEDING, "Highway 1", sampleTime, 80.0, 80.0);
        assertNull(challan);
    }

    @Test
    public void testOverSpeedingJustAbovePermittedSpeed() throws Exception {
        Vehicle vehicle = new Vehicle("KA-01-5555", "Alice Smith", VehicleType.TWO_WHEELER);
        service.registerVehicle(vehicle);

        // 80.1 km/h vs 80.0 km/h baseline violation limit
        Challan challan = service.issueChallan("KA-01-5555", ViolationType.OVER_SPEEDING, "Highway 1", sampleTime, 80.1, 80.0);
        assertNotNull(challan);
        assertEquals(2000.0, challan.getFineAmount());
    }

    // --- INVALID INPUT SCENARIOS ---
    @Test
    public void testRegisterVehicleInvalidInput() {
        Vehicle invalidVehicle = new Vehicle("", "", VehicleType.COMMERCIAL);
        assertThrows(InvalidVehicleException.class, () -> service.registerVehicle(invalidVehicle));
    }

    @Test
    public void testIssueChallanForUnregisteredVehicle() {
        assertThrows(InvalidVehicleException.class, () -> 
            service.issueChallan("UNREGISTERED-123", ViolationType.ILLEGAL_PARKING, "Down Town", sampleTime, 0, 0)
        );
    }

    // --- MULTIPLE FAILURE SCENARIOS ---
    @Test
    public void testDuplicateChallanPrevention() throws Exception {
        Vehicle vehicle = new Vehicle("KA-01-9999", "Bob Johnson", VehicleType.COMMERCIAL);
        service.registerVehicle(vehicle);

        service.issueChallan("KA-01-9999", ViolationType.ILLEGAL_PARKING, "Main St", sampleTime, 0, 0);

        // Re-issuing the exact duplicate context should trigger a system failure catch block
        assertThrows(DuplicateChallanException.class, () -> 
            service.issueChallan("KA-01-9999", ViolationType.ILLEGAL_PARKING, "Main St", sampleTime, 0, 0)
        );
    }

    @Test
    public void testRepeatedViolationsAndRiskReclassification() throws Exception {
        Vehicle vehicle = new Vehicle("KA-02-7777", "Charlie Brown", VehicleType.FOUR_WHEELER);
        service.registerVehicle(vehicle);

        // 1st offense
        Challan c1 = service.issueChallan("KA-02-7777", ViolationType.SIGNAL_VIOLATION, "Loc A", sampleTime, 0, 0);
        assertEquals(1500.0, c1.getFineAmount());
        assertEquals("Safe", service.getVehicle("KA-02-7777").getRiskClassification());

        // 2nd offense (+500 penalty modifier escalation)
        Challan c2 = service.issueChallan("KA-02-7777", ViolationType.ILLEGAL_PARKING, "Loc B", sampleTime.plusHours(1), 0, 0);
        assertEquals(1500.0, c2.getFineAmount()); // Base 1000 + 500 penalty step
        assertEquals("Moderate", service.getVehicle("KA-02-7777").getRiskClassification());

        // Calculate cumulative unpaid tracking
        double totalOutstanding = service.calculateTotalOutstandingFines("KA-02-7777");
        assertEquals(3000.0, totalOutstanding);
    }
}
