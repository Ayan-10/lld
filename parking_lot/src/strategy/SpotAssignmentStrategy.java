package strategy;

import enums.ParkingSpotType;
import enums.VehicleType;
import models.ParkingFloor;
import models.ParkingSpot;

import java.util.List;
import java.util.Optional;

public interface SpotAssignmentStrategy {
  Optional<ParkingSpot> findSpot(List<ParkingFloor> floors, VehicleType vehicleType);
}
