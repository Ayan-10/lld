package observers;

import models.ParkingSpot;

// FIX #4: collapsed from a confused abstract class (two methods) into a single-method
// interface per spec. One callback: onSpotStateChanged.
public interface SpotObserver {
  void onSpotStateChanged(ParkingSpot spot);
}
