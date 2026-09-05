package state;

import enums.ElevatorStateType;
import models.ElevatorCar;

public interface ElevatorState {
  void move(ElevatorCar elevatorCar);
  void openDoor(ElevatorCar elevatorCar);
  void pressFloor(ElevatorCar elevatorCar, int floor);
  ElevatorStateType getType();
}
