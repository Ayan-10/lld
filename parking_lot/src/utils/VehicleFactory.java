package utils;

import enums.VehicleType;
import models.Car;
import models.MotorCycle;
import models.Truck;
import models.Vehicle;

public final class VehicleFactory {

  private VehicleFactory() {
    // Utility class; no instances.
  }

  public static Vehicle create(VehicleType type, String licensePlate) {
    if (type == null) {
      throw new IllegalArgumentException("Vehicle type cannot be null");
    }

    return switch (type) {
      case MOTORCYCLE -> new MotorCycle(licensePlate);
      case CAR -> new Car(licensePlate);
      case TRUCK -> new Truck(licensePlate);
      default -> throw new IllegalArgumentException(
              "Unknown vehicle type: " + type
      );
    };
  }
}