package com.system.model;

public enum ViolationType {
    OVER_SPEEDING(2000.0),
    SIGNAL_VIOLATION(1500.0),
    ILLEGAL_PARKING(1000.0);

    private final double baseFine;

    ViolationType(double baseFine) {
        this.baseFine = baseFine;
    }

    public double getBaseFine() {
        return baseFine;
    }
}
