package strategy;

import enums.SpotStatus;
import enums.VehicleType;
import models.ParkingFloor;
import models.ParkingSpot;

import java.util.List;
import java.util.Optional;

public class NearestEntranceStrategy implements SpotAssignmentStrategy {

  @Override
  public Optional<ParkingSpot> findSpot(
          List<ParkingFloor> floors,
          VehicleType vehicleType) {

    for (ParkingFloor floor : floors) {
      for (ParkingSpot spot : floor.candidateSpots(vehicleType)) {

        if (spot.getStatus() == SpotStatus.AVAILABLE
                && spot.canFit(vehicleType)) {
          return Optional.of(spot);
        }
      }
    }

    return Optional.empty();
  }
}