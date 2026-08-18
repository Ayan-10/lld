package models;

import enums.ParkingSpotType;
import enums.VehicleType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ParkingFloor {

  private final int floorNumber;

  private final Map<ParkingSpotType, List<ParkingSpot>> spotsByType;

  private final Map<ParkingSpotType, Integer> availableCountByType;

  public ParkingFloor(int floorNumber) {
    this.floorNumber = floorNumber;

    spotsByType = new EnumMap<>(ParkingSpotType.class);
    availableCountByType = new EnumMap<>(ParkingSpotType.class);

    for (ParkingSpotType type : ParkingSpotType.values()) {
      spotsByType.put(type, new ArrayList<>());
      availableCountByType.put(type, 0);
    }
  }

  public void addSpot(ParkingSpot spot) {
    ParkingSpotType type = spot.getType();

    spotsByType.get(type).add(spot);

    int currentCount = availableCountByType.get(type);
    availableCountByType.put(type, currentCount + 1);
  }

  public List<ParkingSpot> candidateSpots(VehicleType v) {
    List<ParkingSpot> candidates = new ArrayList<>();

    if (v == VehicleType.MOTORCYCLE) {
      candidates.addAll(spotsByType.get(ParkingSpotType.MOTORCYCLE));
      candidates.addAll(spotsByType.get(ParkingSpotType.COMPACT));
      candidates.addAll(spotsByType.get(ParkingSpotType.LARGE));

    } else if (v == VehicleType.CAR) {
      candidates.addAll(spotsByType.get(ParkingSpotType.COMPACT));
      candidates.addAll(spotsByType.get(ParkingSpotType.LARGE));

    } else if (v == VehicleType.TRUCK) {
      candidates.addAll(spotsByType.get(ParkingSpotType.LARGE));
    }

    return candidates;
  }

  public Optional<ParkingSpot> findAvailableSpot(VehicleType v) {
    for (ParkingSpot spot : candidateSpots(v)) {
      if (spot.isAvailable() && spot.canFit(v)) {
        return Optional.of(spot);
      }
    }

    return Optional.empty();
  }

  // FIX #2: the live free-count must move as spots are taken/freed. These two methods
  // let the park/unpark flow keep availableCountByType in sync (Pass 1: plain int math;
  // Pass 2 will make this atomic/lock-guarded in lockstep with the spot status).
  public void decrementAvailable(ParkingSpotType t) {
    availableCountByType.put(t, availableCountByType.get(t) - 1);
  }

  public void incrementAvailable(ParkingSpotType t) {
    availableCountByType.put(t, availableCountByType.get(t) + 1);
  }

  // FIX #2: helper so the orchestrator can find the owning floor of a spot when freeing.
  public boolean containsSpot(ParkingSpot spot) {
    return spotsByType.getOrDefault(spot.getType(), new ArrayList<>()).contains(spot);
  }

  public int getAvailableCount(ParkingSpotType t) {
    return availableCountByType.get(t);
  }

  public int getFloorNumber() {
    return floorNumber;
  }
}