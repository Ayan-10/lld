package models;

import enums.VehicleType;

public class MotorCycle extends Vehicle {
  public MotorCycle(String licensePlate) {
    super(licensePlate, VehicleType.MOTORCYCLE);
  }
}
