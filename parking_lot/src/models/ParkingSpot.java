package models;

import enums.ParkingSpotType;
import enums.SpotStatus;
import enums.VehicleType;
import observers.SpotObserver;

import java.util.List;

public class ParkingSpot {
  private final String id;
  private final ParkingSpotType type;
  private SpotStatus status; // FIX #6: was public; now private (use getStatus()) for encapsulation
  private Vehicle currentVehicle;
  private final List<SpotObserver> spotObserverList;

  public ParkingSpot(String id, ParkingSpotType type, List<SpotObserver> spotObserverList) {
    this.id = id;
    this.type = type;
    this.status = SpotStatus.AVAILABLE;
    this.spotObserverList = spotObserverList;
  }

  public boolean canFit(VehicleType v) {
    if (type == ParkingSpotType.MOTORCYCLE) {
      return v == VehicleType.MOTORCYCLE;
    }

    if (type == ParkingSpotType.COMPACT) {
      return v == VehicleType.MOTORCYCLE
              || v == VehicleType.CAR;
    }

    if (type == ParkingSpotType.LARGE) {
      return true;
    }

    return false;
  }

  synchronized boolean assignSpot(Vehicle vehicle){
    if (status != SpotStatus.AVAILABLE) {
      return false;
    }

    status = SpotStatus.OCCUPIED;
    currentVehicle = vehicle;
    notifyObservers();
    return true;
  }

  private void notifyObservers() {
    for (SpotObserver observer : spotObserverList) {
      // FIX #4: call the single interface method onSpotStateChanged (was onSpotChanged,
      // which DisplayBoard never overrode, so board logic never ran).
      observer.onSpotStateChanged(this);
    }
  }

  void freeSpot() {
    status = SpotStatus.AVAILABLE;
    currentVehicle = null;

    notifyObservers();
  }


  boolean isAvailable() {
    return status == SpotStatus.AVAILABLE;
  }

  String getId() {
    return id;
  }

  public ParkingSpotType getType() {
    return type;
  }

  Vehicle getCurrentVehicle() {
    return currentVehicle;
  }

  void markOutOfService() {
    status = SpotStatus.OUT_OF_SERVICE;
    currentVehicle = null;

    notifyObservers();
  }

  void markInService() {
    status = SpotStatus.AVAILABLE;

    notifyObservers();
  }

  public SpotStatus getStatus() {
    return status;
  }
}
