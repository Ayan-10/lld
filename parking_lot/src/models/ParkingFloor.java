package models;

import enums.ParkingSpotType;
import enums.VehicleType;
import observers.SpotObserver;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class ParkingFloor {

  private final int floorNumber;

  private final Map<ParkingSpotType, List<ParkingSpot>> spotsByType;

  // ⬆ HARDEN (Pass 2, H3): counter type upgraded from Integer to AtomicInteger. The floor
  // OWNS one AtomicInteger per type and hands the very same instance to each spot it creates,
  // so the spot moves this exact counter inside its own lock. getAvailableCount() is now a
  // lock-free atomic read, and decrement/incrementAvailable() disappear (the spot does it).
  private final Map<ParkingSpotType, AtomicInteger> availableCountByType;

  public ParkingFloor(int floorNumber) {
    this.floorNumber = floorNumber;

    spotsByType = new EnumMap<>(ParkingSpotType.class);
    availableCountByType = new EnumMap<>(ParkingSpotType.class);

    for (ParkingSpotType type : ParkingSpotType.values()) {
      spotsByType.put(type, new ArrayList<>());
      availableCountByType.put(type, new AtomicInteger(0));
    }
  }

  // ⬆ HARDEN (Pass 2, H3): the floor is now the factory for its spots, so it can inject the
  // shared AtomicInteger. This replaces the old `new ParkingSpot(...)` + addSpot(spot) pair
  // and guarantees the spot decrements the exact counter this floor reports.
  public ParkingSpot createSpot(String id,
                                ParkingSpotType type,
                                List<SpotObserver> observers) {
    AtomicInteger counter = availableCountByType.get(type);
    ParkingSpot spot = new ParkingSpot(id, type, counter, observers);
    spotsByType.get(type).add(spot);
    counter.incrementAndGet(); // a fresh AVAILABLE spot bumps the live free-count
    return spot;
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

  // ⬆ HARDEN (Pass 2, H3): O(1) lock-free atomic read for the display board.
  public int getAvailableCount(ParkingSpotType t) {
    return availableCountByType.get(t).get();
  }

  // ⬆ HARDEN (Pass 2, H3): the orchestrator no longer moves the count — the spot does it in
  // lockstep inside tryAssign/freeSpot. containsSpot stays only so the lot can still locate a
  // spot's floor for non-counter needs (e.g. maintenance), but it's no longer on the hot path.
  public boolean containsSpot(ParkingSpot spot) {
    return spotsByType.getOrDefault(spot.getType(), new ArrayList<>()).contains(spot);
  }

  public int getFloorNumber() {
    return floorNumber;
  }
}
