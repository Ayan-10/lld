package models;

import java.time.Instant;
import java.util.UUID;

public class Ticket {
  private final String id;
  private final Vehicle vehicle;
  private final ParkingSpot parkingSpot;
  private final Instant entryTime;
  private Instant exitTime;
  private boolean paid;

  public Ticket(Vehicle vehicle, ParkingSpot parkingSpot, Instant entryTime) {
    this.id = UUID.randomUUID().toString();
    this.vehicle = vehicle;
    this.parkingSpot = parkingSpot;
    this.entryTime = entryTime;
  }

  public String getId() {
    return id;
  }

  public Vehicle getVehicle() {
    return vehicle;
  }

  public ParkingSpot getParkingSpot() {
    return parkingSpot;
  }

  public Instant getEntryTime() {
    return entryTime;
  }

  public Instant getExitTime() {
    return exitTime;
  }

  public boolean isPaid() {
    return paid;
  }

  public void markPaid(boolean paid) {
    this.paid = paid;
  }

  public void setExitTime(Instant exitTime) {
    this.exitTime = exitTime;
  }
}
