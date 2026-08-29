package strategy;

import enums.SpotStatus;
import enums.VehicleType;
import models.ParkingFloor;
import models.ParkingSpot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

// Strategy (pluggable placement): picks any AVAILABLE fitting spot at random, spreading
// load across the lot instead of always filling from the entrance. Swappable with
// NearestEntranceStrategy without touching ParkingLot (Open/Closed).
public class RandomStrategy implements SpotAssignmentStrategy {

  @Override
  public Optional<ParkingSpot> findSpot(
          List<ParkingFloor> floors,
          VehicleType vehicleType) {

    List<ParkingSpot> available = new ArrayList<>();

    for (ParkingFloor floor : floors) {
      for (ParkingSpot spot : floor.candidateSpots(vehicleType)) {
        if (spot.getStatus() == SpotStatus.AVAILABLE && spot.canFit(vehicleType)) {
          available.add(spot);
        }
      }
    }

    if (available.isEmpty()) {
      return Optional.empty();
    }

    Collections.shuffle(available);
    return Optional.of(available.get(0));
  }
}
