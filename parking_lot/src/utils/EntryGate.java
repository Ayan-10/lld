package utils;

import enums.VehicleType;
import exceptions.LotFullException;
import models.ParkingLot;
import models.Ticket;
import models.Vehicle;

public class EntryGate {

  private final ParkingLot lot;

  public EntryGate(ParkingLot lot) {
    this.lot = lot;
  }

  public Ticket admit(VehicleType type, String plate) {
    Vehicle vehicle = VehicleFactory.create(type, plate);

    try {
      return lot.parkVehicle(vehicle);
    } catch (LotFullException e) {
      System.out.println("FULL");
      return null;
    }
  }
}
