package state;

import enums.ElevatorStateType;
import models.ElevatorCar;

public class DoorOpenState implements ElevatorState {
  @Override
  public void move(ElevatorCar elevatorCar) {

  }

  @Override
  public void openDoor(ElevatorCar elevatorCar) {

  }

  @Override
  public void pressFloor(ElevatorCar elevatorCar, int floor) {

  }

  @Override
  public ElevatorStateType getType() {
    return ElevatorStateType.DOORS_OPEN;
  }
}
