package models;

import enums.ParkingSpotType;
import enums.SpotStatus;
import enums.VehicleType;
import observers.SpotObserver;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class ParkingSpot {
  private final String id;
  private final ParkingSpotType type;
  private SpotStatus status; // FIX #6: was public; now private (use getStatus()) for encapsulation
  private Vehicle currentVehicle;
  private final List<SpotObserver> spotObserverList;

  // ⬆ HARDEN (Pass 2, H1): per-spot lock guards the check-and-occupy so two gates can't
  // both take this spot. Per-spot (not one lot-wide lock) => cars heading for DIFFERENT
  // spots never contend => maximum concurrency.
  private final ReentrantLock lock = new ReentrantLock();

  // ⬆ HARDEN (Pass 2, H1): the floor's live per-type free-count, SHARED with this spot so
  // the counter moves in lockstep with the status flip, inside the same lock. Injected by
  // the floor at construction. Replaces the orchestrator-driven decrement/increment.
  private final AtomicInteger floorAvailableCount;

  // ⬆ HARDEN (Pass 2, H1): ctor now takes the shared AtomicInteger. The floor hands each
  // spot the very counter it will report from getAvailableCount().
  public ParkingSpot(String id,
                     ParkingSpotType type,
                     AtomicInteger floorAvailableCount,
                     List<SpotObserver> spotObserverList) {
    this.id = id;
    this.type = type;
    this.status = SpotStatus.AVAILABLE;
    this.floorAvailableCount = floorAvailableCount;
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

  // ⬆ HARDEN (Pass 2, H2): assignSpot -> tryAssign. The naive Pass-1 check-then-set had a
  // gap between reading AVAILABLE and writing OCCUPIED where a second thread could slip in
  // (two cars, one spot). Now the check AND the set happen as ONE atomic step under the
  // lock. Returns false if we lost the race, so the caller just tries the next candidate.
  public boolean tryAssign(Vehicle vehicle) {
    if (!canFit(vehicle.getVehicleType())) {
      return false;
    }

    lock.lock();
    try {
      if (status != SpotStatus.AVAILABLE) {
        return false; // someone grabbed it first -> caller tries the next spot
      }

      status = SpotStatus.OCCUPIED;
      currentVehicle = vehicle;
      floorAvailableCount.decrementAndGet(); // counter moves IN LOCKSTEP with the status
      notifyObservers();
      return true;
    } finally {
      lock.unlock();
    }
  }

  private void notifyObservers() {
    for (SpotObserver observer : spotObserverList) {
      // FIX #4: call the single interface method onSpotStateChanged (was onSpotChanged,
      // which DisplayBoard never overrode, so board logic never ran).
      observer.onSpotStateChanged(this);
    }
  }

  // ⬆ HARDEN (Pass 2, H2): idempotent + counter-consistent. Freeing an already-free spot is
  // a no-op (protects against an exit-gate double-scan), and the increment happens inside
  // the same lock so the count can never disagree with the status.
  public void freeSpot() {
    lock.lock();
    try {
      if (status != SpotStatus.OCCUPIED) {
        return; // idempotent: nothing to free
      }
      status = SpotStatus.AVAILABLE;
      currentVehicle = null;
      floorAvailableCount.incrementAndGet();
      notifyObservers();
    } finally {
      lock.unlock();
    }
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
    lock.lock();
    try {
      status = SpotStatus.OUT_OF_SERVICE;
      currentVehicle = null;
      notifyObservers();
    } finally {
      lock.unlock();
    }
  }

  void markInService() {
    lock.lock();
    try {
      status = SpotStatus.AVAILABLE;
      notifyObservers();
    } finally {
      lock.unlock();
    }
  }

  public SpotStatus getStatus() {
    return status;
  }
}
