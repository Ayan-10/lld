package utils;

import enums.ParkingSpotType;
import models.ParkingLot;
import models.ParkingSpot;
import observers.SpotObserver;

// FIX #3 + #4: DisplayBoard now implements the single-method SpotObserver interface,
// and instead of tracking a delta-from-zero (which never reflected the real inventory
// and clamped at 0), it reads the AUTHORITATIVE live count from ParkingLot on demand.
public class DisplayBoard implements SpotObserver {

  private final ParkingLot lot; // FIX #3: source of truth for availability

  public DisplayBoard(ParkingLot lot) {
    this.lot = lot;
  }

  @Override
  public void onSpotStateChanged(ParkingSpot spot) {
    // FIX #3: a state change just triggers a re-render; the counts come from the lot,
    // so there is no local counter to drift.
    show();
  }

  public void show() {
    System.out.println("===== Parking Availability =====");

    for (ParkingSpotType type : ParkingSpotType.values()) {
      int count = lot.getAvailableCount(type); // FIX #3: authoritative live count

      System.out.println(
              type + ": " + count + " available"
      );
    }

    System.out.println("================================");
  }
}
