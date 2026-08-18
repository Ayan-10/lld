package models;

import enums.VehicleType;

public abstract class Vehicle {
  protected final String licensePlate;
  protected final VehicleType vehicleType;

  protected Vehicle(String licensePlate, VehicleType vehicleType) {
    this.licensePlate = licensePlate;
    this.vehicleType = vehicleType;
  }

  public String getLicensePlate() {
    return licensePlate;
  }

  public VehicleType getVehicleType() {
    return vehicleType;
  }
}
