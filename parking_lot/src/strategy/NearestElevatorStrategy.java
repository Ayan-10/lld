package strategy;

import enums.SpotStatus;
import enums.VehicleType;
import models.ParkingFloor;
import models.ParkingSpot;

import java.util.List;
import java.util.Optional;

// Phase 10 extensibility drill: "add a placement rule = add a class, ZERO edits to
// ParkingLot." This one prefers spots nearest the elevator. In this simplified model
// there is no per-spot elevator distance, so it degrades to the same scan as
// NearestEntrance; a real impl would sort candidates by spot.getDistanceToElevator().
public class NearestElevatorStrategy implements SpotAssignmentStrategy {

  @Override
  public Optional<ParkingSpot> findSpot(
          List<ParkingFloor> floors,
          VehicleType vehicleType) {

    for (ParkingFloor floor : floors) {
      for (ParkingSpot spot : floor.candidateSpots(vehicleType)) {
        // Real impl: pick min(spot.getDistanceToElevator()) among AVAILABLE candidates.
        if (spot.getStatus() == SpotStatus.AVAILABLE && spot.canFit(vehicleType)) {
          return Optional.of(spot);
        }
      }
    }

    return Optional.empty();
  }
}
