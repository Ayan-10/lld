package models;

import enums.VehicleType;

public class Car extends Vehicle {
  public Car(String licensePlate) {
    super(licensePlate, VehicleType.CAR);
  }
}
